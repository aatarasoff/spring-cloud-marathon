package info.developerblog.spring.cloud.marathon.discovery;

import info.developerblog.spring.cloud.marathon.ConditionalOnMarathonEnabled;
import mesosphere.marathon.client.Marathon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by aleksandr on 07.07.16.
 */
@Configuration
@ConditionalOnMarathonEnabled
@ConditionalOnProperty(value = "spring.cloud.marathon.discovery.enabled", matchIfMissing = true)
@EnableConfigurationProperties
public class MarathonDiscoveryClientAutoConfiguration {

    @Autowired
    private Marathon marathonClient;

    @Bean
    public MarathonDiscoveryProperties marathonDiscoveryProperties() {
        return new MarathonDiscoveryProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public MarathonDiscoveryClient marathonDiscoveryClient(MarathonDiscoveryProperties discoveryProperties) {
        MarathonDiscoveryClient discoveryClient = new MarathonDiscoveryClient(marathonClient);
        return discoveryClient;
    }
}
