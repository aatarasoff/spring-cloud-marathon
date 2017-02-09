package info.developerblog.spring.cloud.marathon.discovery.ribbon;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.*;
import info.developerblog.spring.cloud.marathon.discovery.MarathonDiscoveryProperties;
import mesosphere.marathon.client.Marathon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by aleksandr on 07.07.16.
 */
@Configuration
public class MarathonRibbonClientConfiguration {
    @Autowired
    private Marathon client;

    @Autowired
    private MarathonDiscoveryProperties properties;

    public MarathonRibbonClientConfiguration() {
    }

    @Bean
    public MarathonSSEUpdater sseUpdater() {
        return new MarathonSSEUpdater();
    }

    @Bean
    @ConditionalOnMissingBean
    public ServerList<?> ribbonServerList(IClientConfig config) {
        MarathonServerList serverList = new MarathonServerList(client);
        serverList.initWithNiwsConfig(config);
        return serverList;
    }

    @Bean
    public ServerListFilter<Server> ribbonServerListFilter() {
        return new MarathonServiceHealthCheckFilter();
    }

    @Bean
    @ConditionalOnMissingBean
    public IPing ribbonPing(MarathonDiscoveryProperties properties, MarathonSSEUpdater sseUpdater) {
        return new MarathonPing(properties.isSseEnabled(), sseUpdater);
    }
}
