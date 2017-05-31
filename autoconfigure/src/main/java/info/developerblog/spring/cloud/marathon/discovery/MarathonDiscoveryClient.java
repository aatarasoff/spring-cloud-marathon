package info.developerblog.spring.cloud.marathon.discovery;

import info.developerblog.spring.cloud.marathon.utils.ServiceIdConverter;
import lombok.extern.slf4j.Slf4j;
import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.model.v2.*;
import mesosphere.marathon.client.utils.MarathonException;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by aleksandr on 07.07.16.
 */
@Slf4j
public class MarathonDiscoveryClient implements DiscoveryClient {

    private static final String SPRING_CLOUD_MARATHON_DISCOVERY_CLIENT_DESCRIPTION = "Spring Cloud Marathon Discovery Client";

    private static final String ALL_SERVICES = "*";

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

            boolean ignoreServiceId = ALL_SERVICES.equals(serviceId); // "*" indicates fetch all services

            List<ServiceInstance> instances = new ArrayList<>();

            if (!ignoreServiceId) {
            /*
            Step 1 - Search for an application that matches the specific service id
             */
                try {
                    GetAppResponse response = client.getApp(ServiceIdConverter.convertToMarathonId(serviceId));

                    if (response != null && response.getApp() != null)
                        instances.addAll(extractServiceInstances(response.getApp()));

                } catch (MarathonException e) {
                    log.error(e.getMessage(), e);
                }
            }

            if (instances.isEmpty()) {

                /*
                Step 2 - Search for all applications whose marathon id contains the service id (e.g. "*.{serviceId}*.")
                This is supported by the marathon api by passing a partial id as a query parameter
                 */
                Map<String,String> queryMap = new HashMap<>();
                queryMap.put("id",ServiceIdConverter.convertToMarathonId(serviceId));

                GetAppsResponse appsResponse = (ignoreServiceId)?client.getApps():client.getApps(queryMap);

                if (appsResponse!=null && appsResponse.getApps()!=null) {

                	List<App> apps = appsResponse.getApps();
                	
                	log.debug("Discovered {} service{}{}", apps.size(), apps.size() == 1 ? "" : "s", ignoreServiceId ? "" : String.format(" with ids that contain [%s]", serviceId));

                    for (App app : apps){

                        // Fetch data for this specific service id, to collect task information
                        GetAppResponse response = client.getApp(app.getId());

                        if (response!=null && response.getApp()!=null)
                            instances.addAll(extractServiceInstances(response.getApp()));

                    }
                }
            }

            log.debug("Discovered {} service{}{}", instances.size(), instances.size() == 1 ? "" : "s", ignoreServiceId ? "" : String.format(" with ids that contain [%s]", serviceId));
            return instances;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }


    /**
     * Extract instances of a service for a specific marathon application
     *
     * @param app
     * @return
     */
    private List<ServiceInstance> extractServiceInstances(App app) {

    	log.debug("Discovered service [{}]", app.getId());

        if (app.getTasks().isEmpty()) {
            return Collections.emptyList();
        }

        return app.getTasks()
                .parallelStream()
                .filter(task -> null == task.getHealthCheckResults() ||
                        task.getHealthCheckResults()
                                .stream()
                                .allMatch(HealthCheckResult::isAlive)
                )
                .map(task -> new DefaultServiceInstance(
                        ServiceIdConverter.convertToServiceId(task.getAppId()),
                        task.getHost(),
                        task.getPorts().stream().findFirst().orElse(0),
                        false
                )).map(serviceInstance -> {
                    if (app.getLabels() != null && !app.getLabels().isEmpty())
                        serviceInstance.getMetadata().putAll(app.getLabels());
                    return serviceInstance;
                })
                .collect(Collectors.toList());

    }


    @Override
    public List<String> getServices() {
        try {
            return client.getApps()
                    .getApps()
                    .parallelStream()
                    .map(App::getId)
                    .map(ServiceIdConverter::convertToServiceId)
                    .collect(Collectors.toList());
        } catch (MarathonException e) {
            log.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
