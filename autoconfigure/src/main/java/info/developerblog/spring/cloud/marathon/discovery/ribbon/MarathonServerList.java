package info.developerblog.spring.cloud.marathon.discovery.ribbon;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractServerList;
import info.developerblog.spring.cloud.marathon.discovery.MarathonDiscoveryProperties;
import lombok.extern.slf4j.Slf4j;
import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.model.v2.HealthCheckResult;
import mesosphere.marathon.client.utils.MarathonException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by aleksandr on 07.07.16.
 */
@Slf4j
public class MarathonServerList extends AbstractServerList<MarathonServer> {
    private Marathon client;
    private MarathonDiscoveryProperties properties;

    private String serviceId;

    public MarathonServerList(Marathon client, MarathonDiscoveryProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public void initWithNiwsConfig(IClientConfig clientConfig) {
        serviceId = clientConfig.getClientName();
    }

    @Override
    public List<MarathonServer> getInitialListOfServers() {
        return getServers();
    }

    @Override
    public List<MarathonServer> getUpdatedListOfServers() {
        return getServers();
    }

    private List<MarathonServer> getServers() {
        if (this.client == null) {
            return Collections.emptyList();
        }

        try {
            return client.getAppTasks(serviceId)
                    .getTasks()
                    .parallelStream()
                    .filter(task -> null == task.getHealthCheckResults() ||
                            task.getHealthCheckResults()
                                    .stream()
                                    .allMatch(HealthCheckResult::isAlive)
                    )
                    .map(task -> new MarathonServer(
                            task.getHost(),
                            task.getPorts().stream().findFirst().orElse(0)
                    ))
                    .collect(Collectors.toList());
        } catch (MarathonException e) {
            log.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
