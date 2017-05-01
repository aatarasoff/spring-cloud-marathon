package info.developerblog.spring.cloud.marathon;

import info.developerblog.spring.cloud.marathon.actuator.MarathonEndpoint;
import info.developerblog.spring.cloud.marathon.actuator.MarathonHealthIndicator;
import mesosphere.dcos.client.RibbonDCOSClient;
import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.RibbonMarathonClient;
import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by aleksandr on 07.07.16.
 */
@Configuration
@EnableConfigurationProperties
@ConditionalOnMarathonEnabled
public class MarathonAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public MarathonProperties marathonProperties() {
        return new MarathonProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public Marathon marathonClient(MarathonProperties properties) {

        if (properties.isDcosAuthentication()) {

            return new RibbonDCOSClient.Builder(properties.getEndpoint())
                    .withListOfServers(properties.getListOfServers())
                    .withUsername(properties.getUsername())
                    .withPassword(properties.getPassword())
                    .withPrivateKey(properties.getPrivateKey())
                    .build();

        } else {

            return new RibbonMarathonClient.Builder(properties.getEndpoint())
                    .withListOfServers(properties.getListOfServers())
                    .withToken(properties.getToken())
                    .withUsername(properties.getUsername())
                    .withPassword(properties.getPassword())
                    .build();
        }
    }

    @Configuration
    @ConditionalOnClass(Endpoint.class)
    protected static class MarathonHealthConfig {

        @Bean
        @ConditionalOnMissingBean
        public MarathonEndpoint marathonEndpoint(Marathon client) {
            return new MarathonEndpoint(client);
        }

        @Bean
        @ConditionalOnMissingBean
        public MarathonHealthIndicator healthIndicator(Marathon client) {
            return new MarathonHealthIndicator(client);
        }

    }
}
