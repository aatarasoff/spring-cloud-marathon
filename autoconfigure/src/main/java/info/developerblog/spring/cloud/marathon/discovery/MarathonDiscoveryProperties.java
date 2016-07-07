package info.developerblog.spring.cloud.marathon.discovery;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by aleksandr on 07.07.16.
 */
@ConfigurationProperties("spring.cloud.marathon.discovery")
@Data
public class MarathonDiscoveryProperties {
    private boolean enabled = true;
}
