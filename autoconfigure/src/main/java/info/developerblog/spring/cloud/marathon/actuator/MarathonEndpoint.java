package info.developerblog.spring.cloud.marathon.actuator;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.model.v2.GetServerInfoResponse;
import mesosphere.marathon.client.model.v2.VersionedApp;

/**
 * Created by aleksandr on 12.08.16.
 */
@Slf4j
@Endpoint(id = "marathon")
public class MarathonEndpoint {

    private Marathon marathon;

    @Autowired
    public MarathonEndpoint(Marathon marathon) {
        this.marathon = marathon;
    }

    @ReadOperation
    public MarathonData info() {
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
        List<VersionedApp> apps;
    }
}
