package info.developerblog.spring.cloud.marathon.discovery.ribbon;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractServerList;
import info.developerblog.spring.cloud.marathon.discovery.MarathonDiscoveryProperties;
import info.developerblog.spring.cloud.marathon.utils.ServiceIdConverter;
import lombok.extern.slf4j.Slf4j;
import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.model.v2.App;
import mesosphere.marathon.client.model.v2.HealthCheck;
import mesosphere.marathon.client.model.v2.HealthCheckResult;
import mesosphere.marathon.client.utils.MarathonException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by aleksandr on 07.07.16.
 */
@Slf4j
public class MarathonServerList extends AbstractServerList<MarathonServer> {
    private Marathon client;
    private MarathonDiscoveryProperties properties;

    private String serviceId;

    public MarathonServerList(Marathon client, MarathonDiscoveryProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public void initWithNiwsConfig(IClientConfig clientConfig) {
        serviceId = ServiceIdConverter.convertToMarathonId(clientConfig.getClientName());
    }

    @Override
    public List<MarathonServer> getInitialListOfServers() {
        return getServers();
    }

    @Override
    public List<MarathonServer> getUpdatedListOfServers() {
        return getServers();
    }

    private List<MarathonServer> getServers() {
        if (this.client == null) {
            return Collections.emptyList();
        }

        try {
            final App app = client.getApp(serviceId).getApp();

            return app.getTasks()
                    .parallelStream()
                    .map(task -> {
                        Collection<HealthCheckResult> healthChecks =
                                null != task.getHealthCheckResults()
                                    ? task.getHealthCheckResults()
                                    : new ArrayList<>();

                        return new MarathonServer(
                                task.getHost(),
                                task.getPorts().stream().findFirst().orElse(0),
                                healthChecks
                        );
                    })
                    .collect(Collectors.toList());
        } catch (MarathonException e) {
            log.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
