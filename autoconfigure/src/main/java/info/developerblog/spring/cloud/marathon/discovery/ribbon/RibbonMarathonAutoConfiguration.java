package info.developerblog.spring.cloud.marathon.discovery.ribbon;

import info.developerblog.spring.cloud.marathon.ConditionalOnMarathonEnabled;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.ribbon.RibbonAutoConfiguration;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.context.annotation.Configuration;

/**
 * Created by aleksandr on 07.07.16.
 */
@Configuration
@EnableConfigurationProperties
@ConditionalOnMarathonEnabled
@ConditionalOnBean(SpringClientFactory.class)
@ConditionalOnProperty(value = "spring.cloud.marathon.ribbon.enabled", matchIfMissing = true)
@AutoConfigureAfter(RibbonAutoConfiguration.class)
@RibbonClients(defaultConfiguration = MarathonRibbonClientConfiguration.class)
public class RibbonMarathonAutoConfiguration {
}
