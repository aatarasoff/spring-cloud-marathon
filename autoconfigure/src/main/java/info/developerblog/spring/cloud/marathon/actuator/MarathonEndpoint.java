package info.developerblog.spring.cloud.marathon.actuator;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.model.v2.App;
import mesosphere.marathon.client.model.v2.GetServerInfoResponse;
import mesosphere.marathon.client.utils.MarathonException;
import org.springframework.boot.actuate.endpoint.AbstractEndpoint;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Created by aleksandr on 12.08.16.
 */
@ConfigurationProperties(prefix = "endpoints.marathon", ignoreUnknownFields = false)
@Slf4j
public class MarathonEndpoint extends AbstractEndpoint<MarathonEndpoint.MarathonData> {

    private Marathon marathon;

    public MarathonEndpoint(Marathon marathon) {
        super("marathon", false, true);
        this.marathon = marathon;
    }

    @Override
    public MarathonData invoke() {
        try {
            return MarathonData.builder()
                    .serverInfo(marathon.getServerInfo())
                    .apps(marathon.getApps().getApps())
                    .build();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return MarathonData.builder().build();
    }

    @Data
    @Builder
    public static class MarathonData {
        GetServerInfoResponse serverInfo;
        List<App> apps;
    }
}
