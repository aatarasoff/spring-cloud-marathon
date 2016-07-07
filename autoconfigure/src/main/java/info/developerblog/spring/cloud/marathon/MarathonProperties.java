package info.developerblog.spring.cloud.marathon;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotNull;

/**
 * Created by aleksandr on 07.07.16.
 */
@ConfigurationProperties("spring.cloud.marathon")
@Data
public class MarathonProperties {
    @NotNull
    private String scheme = "http";

    @NotNull
    private String host = "localhost";

    @NotNull
    private int port = 8080;

    private boolean enabled = true;
}
