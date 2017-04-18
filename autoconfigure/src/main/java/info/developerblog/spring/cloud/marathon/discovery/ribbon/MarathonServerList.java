package info.developerblog.spring.cloud.marathon.discovery.ribbon;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractServerList;
import info.developerblog.spring.cloud.marathon.discovery.MarathonDiscoveryProperties;
import info.developerblog.spring.cloud.marathon.utils.ServiceIdConverter;
import lombok.extern.slf4j.Slf4j;
import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.model.v2.*;
import mesosphere.marathon.client.MarathonException;


import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by aleksandr on 07.07.16.
 */
@Slf4j
public class MarathonServerList extends AbstractServerList<MarathonServer> {
    private Marathon client;
    private MarathonDiscoveryProperties properties;

    private String serviceId;
    private Map<String,String> queryMap;

    public MarathonServerList(Marathon client, MarathonDiscoveryProperties properties) {
        this.client = client;
        this.properties = properties;
        this.queryMap = new HashMap<>();
    }

    @Override
    public void initWithNiwsConfig(IClientConfig clientConfig) {
        serviceId = ServiceIdConverter.convertToMarathonId(clientConfig.getClientName());
        queryMap.put("id",serviceId);
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

            List<MarathonServer> instances = new ArrayList<>();

            /*
            Step 1 - Search for an application that matches the specific service id
             */
            try {
                GetAppResponse response = client.getApp(serviceId);

                if (response!=null && response.getApp()!=null)
                    instances.addAll(extractServiceInstances(response.getApp()));

            } catch (MarathonException e) {
            }

            if (instances.size()==0) {

                /*
                Step 2 - Search for all applications whose marathon id contains the service id (e.g. "*.{serviceId}*.")
                This is supported by the marathon api by passing a partial id as a query parameter
                 */
                GetAppsResponse appsResponse = client.getApps(queryMap);

                if (appsResponse!=null && appsResponse.getApps()!=null) {

                    log.debug("Discovered " + appsResponse.getApps().size() + " service" + ((appsResponse.getApps().size() == 1) ? "" : "s") + " with ids that contain [" + serviceId + "]");

                    for (App app : appsResponse.getApps()){

                        // Fetch data for this specific service id, to collect task information
                        GetAppResponse response = client.getApp(app.getId());

                        if (response!=null && response.getApp()!=null)
                            instances.addAll(extractServiceInstances(response.getApp()));

                    }

                }
            }

            log.debug("Discovered " + instances.size() + " service instance" + ((instances.size() == 1) ? "" : "s") + " for service id [" + serviceId + "]");
            return instances;


        } catch (MarathonException e) {
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
    private List<MarathonServer> extractServiceInstances(App app) {

        log.debug("Discovered service [" + app.getId() + "]");

        if (app.getTasks().size()==0)
            return Collections.emptyList();

        return app.getTasks()
                .stream()
                .map(task -> {
                    Collection<HealthCheckResults> healthChecks =
                            null != task.getHealthCheckResults()
                                    ? task.getHealthCheckResults()
                                    : new ArrayList<>();

                    return new MarathonServer(
                            task.getHost(),
                            task.getPorts().stream().findFirst().orElse(0),
                            healthChecks, app.getLabels()
                    );
                })
                .collect(Collectors.toList());

    }
}
