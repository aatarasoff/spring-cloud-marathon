package info.developerblog.spring.cloud.marathon.discovery.ribbon;

import com.netflix.client.ClientFactory;
import com.netflix.client.config.DefaultClientConfigImpl;
import com.netflix.client.config.IClientConfig;
import com.netflix.config.ConfigurationManager;
import com.netflix.loadbalancer.Server;
import com.netflix.ribbon.transport.netty.RibbonTransport;
import com.netflix.ribbon.transport.netty.http.SSEClient;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import io.reactivex.netty.protocol.text.sse.ServerSentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.Ordered;
import rx.Observable;
import rx.Subscription;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by aleksandr on 17.01.17.
 */
@Slf4j
public class MarathonSSEUpdater implements SmartLifecycle {
    private Map<String, Boolean> updatedHealth = new ConcurrentHashMap<>();
    private Observable<ServerSentEvent> observable;
    private volatile Subscription subscription;

    MarathonSSEUpdater() {
        ConfigurationManager.getConfigInstance().setProperty("marathon.ribbon.listOfServers", "localhost:8080");
        IClientConfig clientConfig = ClientFactory.getNamedConfig("marathon");
        observable = SSEClient.<ByteBuf>sseClientBuilder().withClientConfig(clientConfig)
                .withLoadBalancer(loadBalancer)
                .withClientConfig(config)
                .withRetryHandler(getDefaultHttpRetryHandlerWithConfig(config))
                .withPipelineConfigurator(pipelineConfigurator)
                .build()
                .submit(HttpClientRequest
                        .createGet("http://marathon/v2/events")
                        .withHeader("Accept", "text/event-stream")
                ).doOnNext(resp -> log.info(resp.getStatus().toString()))
                .doOnError(err -> log.error(err.getMessage(), err))
                .doOnCompleted(() -> log.info("completed"))
                .flatMap(HttpClientResponse::getContent);
    }

    @Override
    public void start() {
        log.info("start updater");
        if (null == subscription) {
            subscription = observable.subscribe(
                    sse -> log.info(sse.getEventData()),
                    err -> log.error(err.getMessage(), err),
                    () -> log.info("completed in subscription")
            );
        }
    }

    @Override
    public void stop() {
        log.info("stop updater");
        updatedHealth.clear();

        if (null != subscription) {
            subscription.unsubscribe();
            subscription = null;
        }
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        try {
            stop();
        } catch (Exception e) {
            log.error("Could not stop marathon sse updater", e);
        }
        callback.run();
    }

    @Override
    public boolean isRunning() {
        return null != subscription && !subscription.isUnsubscribed();
    }

    @Override
    public int getPhase() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    void updateServerAliveStatus(Server server) {
        String instanceId = server.getMetaInfo().getInstanceId();

        if (updatedHealth.containsKey(instanceId)) {
            server.setAlive(updatedHealth.get(instanceId));
        }
    }
}
