package info.developerblog.spring.cloud.marathon.discovery.ribbon;

import com.netflix.client.config.IClientConfig;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DeploymentContext;
import com.netflix.loadbalancer.IPing;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;
import com.netflix.loadbalancer.ServerListFilter;
import info.developerblog.spring.cloud.marathon.discovery.MarathonDiscoveryProperties;
import mesosphere.marathon.client.Marathon;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * Created by aleksandr on 07.07.16.
 */
@Configuration
public class MarathonRibbonClientConfiguration {

    private Marathon client;
    private IClientConfig clientConfig;
    private MarathonDiscoveryProperties properties;

    @Autowired
    public MarathonRibbonClientConfiguration(Marathon client,
                                             IClientConfig clientConfig,
                                             MarathonDiscoveryProperties properties) {
        this.client = client;
        this.clientConfig = clientConfig;
        this.properties = properties;
    }

    @PostConstruct
    public void preprocess() {
        String zone = ConfigurationManager.getDeploymentContext().getValue(DeploymentContext.ContextKey.zone);

        if (StringUtils.isEmpty(zone)) {
            String availabilityZone = properties.getZone();

            if (availabilityZone != null) {
                ConfigurationManager.getDeploymentContext().setValue(DeploymentContext.ContextKey.zone, availabilityZone);
            }
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public ServerList<?> ribbonServerList() {
        MarathonServerList serverList = new MarathonServerList(client, properties);
        serverList.initWithNiwsConfig(clientConfig);
        return serverList;
    }

    @Bean
    public ServerListFilter<Server> ribbonServerListFilter() {
        MarathonServiceHealthCheckFilter filter = new MarathonServiceHealthCheckFilter();
        filter.initWithNiwsConfig(clientConfig);
        return filter;
    }

    @Bean
    @ConditionalOnMissingBean
    public IPing ribbonPing() {
        return new MarathonPing();
    }
}
