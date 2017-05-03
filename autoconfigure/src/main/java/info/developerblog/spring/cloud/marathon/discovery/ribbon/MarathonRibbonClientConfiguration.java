package info.developerblog.spring.cloud.marathon.discovery.ribbon;

import com.fasterxml.jackson.core.type.TypeReference;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.*;
import info.developerblog.spring.cloud.marathon.discovery.MarathonDiscoveryProperties;
import mesosphere.marathon.client.Marathon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.netflix.ribbon.PropertiesFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by aleksandr on 07.07.16.
 */
@Configuration
public class MarathonRibbonClientConfiguration {

    @Autowired
    private Marathon client;

    public MarathonRibbonClientConfiguration() {
    }

    @Bean
    @ConditionalOnMissingBean
    public ServerList<?> ribbonServerList(IClientConfig config, MarathonDiscoveryProperties properties) {
        MarathonServerList serverList = new MarathonServerList(client, properties);
        serverList.initWithNiwsConfig(config);
        return serverList;
    }

    @Bean
    public ServerListFilter<Server> ribbonServerListFilter(IClientConfig config) {
        MarathonServiceHealthCheckLabelFilter filter = new MarathonServiceHealthCheckLabelFilter();
        filter.initWithNiwsConfig(config);
        return filter;
    }

    @Bean
    @ConditionalOnMissingBean
    public IPing ribbonPing() {
        return new MarathonPing();
    }
}
