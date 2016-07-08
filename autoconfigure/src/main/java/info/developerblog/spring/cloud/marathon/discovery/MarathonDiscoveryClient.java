package info.developerblog.spring.cloud.marathon.discovery;

import lombok.extern.slf4j.Slf4j;
import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.model.v2.App;
import mesosphere.marathon.client.model.v2.HealthCheckResult;
import mesosphere.marathon.client.utils.MarathonException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by aleksandr on 07.07.16.
 */
@Slf4j
public class MarathonDiscoveryClient implements DiscoveryClient {

    private static final String SPRING_CLOUD_MARATHON_DISCOVERY_CLIENT_DESCRIPTION = "Spring Cloud Marathon Discovery Client";

    private final Marathon client;
    private final MarathonDiscoveryProperties properties;

    public MarathonDiscoveryClient(Marathon client, MarathonDiscoveryProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public String description() {
        return SPRING_CLOUD_MARATHON_DISCOVERY_CLIENT_DESCRIPTION;
    }

    @Override
    public ServiceInstance getLocalServiceInstance() {
        return null;
    }

    @Override
    public List<ServiceInstance> getInstances(String serviceId) {
        try {
            return client.getAppTasks(serviceId)
                    .getTasks()
                    .parallelStream()
                    .filter(task -> null == task.getHealthCheckResults() ||
                            task.getHealthCheckResults()
                            .stream()
                            .allMatch(HealthCheckResult::isAlive)
                    )
                    .map(task -> new DefaultServiceInstance(
                            task.getAppId(),
                            task.getHost(),
                            task.getPorts().stream().findFirst().orElse(0),
                            false
                    ))
                    .collect(Collectors.toList());
        } catch (MarathonException e) {
            log.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> getServices() {
        try {
            return client.getApps()
                    .getApps()
                    .parallelStream()
                    .map(App::getId)
                    .collect(Collectors.toList());
        } catch (MarathonException e) {
            log.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
