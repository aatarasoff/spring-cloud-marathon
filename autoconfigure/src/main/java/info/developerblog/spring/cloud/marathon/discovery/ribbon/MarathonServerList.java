package info.developerblog.spring.cloud.marathon.discovery.ribbon;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractServerList;
import com.netflix.loadbalancer.Server;
import info.developerblog.spring.cloud.marathon.discovery.MarathonDiscoveryProperties;
import info.developerblog.spring.cloud.marathon.utils.ServiceIdConverter;
import lombok.extern.slf4j.Slf4j;
import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.model.v2.App;
import mesosphere.marathon.client.model.v2.GetAppResponse;
import mesosphere.marathon.client.model.v2.GetAppsResponse;
import mesosphere.marathon.client.model.v2.HealthCheckResult;
import mesosphere.marathon.client.utils.MarathonException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by aleksandr on 07.07.16.
 */
@Slf4j
public class MarathonServerList extends AbstractServerList<MarathonServer> {

    private static final String IGNORESERVICEID_PROPERTY = "IgnoreServiceId";
    private static final String ZONE_PATTERN = "ZonePattern";

    private static final String METADATAFILTER_PROPERTY = "MetaDataFilter";

    private static final String METADATAFILTER_KEY = METADATAFILTER_PROPERTY + ".";

    private static final String NOT_EQUAL = "!=";
    private static final String EQUAL = "==";
    private static final String SET_IN = "in";
    private static final String SET_NOTIN = "notin";
    private static final String LABEL_SEPARATOR = ",";

    private Map<String,String> metaDataFilter;

    private Marathon client;
    private MarathonDiscoveryProperties properties;

    private String serviceId;
    private boolean ignoreServiceId;
    private Map<String,String> queryMap;
    private Pattern zonePattern = null;

    public MarathonServerList(Marathon client, MarathonDiscoveryProperties properties) {
        this.client = client;
        this.properties = properties;
        this.queryMap = new HashMap<>();
        this.ignoreServiceId = false;
        metaDataFilter = new HashMap<>();
    }

    @Override
    public void initWithNiwsConfig(IClientConfig clientConfig) {
        serviceId = ServiceIdConverter.convertToMarathonId(clientConfig.getClientName());

        if (clientConfig.getProperties().containsKey(IGNORESERVICEID_PROPERTY)) {
            ignoreServiceId = "true".equalsIgnoreCase(clientConfig.getProperties().get(IGNORESERVICEID_PROPERTY).toString());
        }

        clientConfig.getProperties().entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(METADATAFILTER_KEY))
                .forEach(entry -> metaDataFilter.put(entry.getKey().replace(METADATAFILTER_KEY, ""), entry.getValue().toString()));


        // Filter list of services by service id
        if (!ignoreServiceId){
            queryMap.put("id",serviceId);
        }

        // Filter list of services by service labels
        if (metaDataFilter.size()>0){

            String labelSelectorQuery = "";

            int count = 0;
            for (Map.Entry<String,String> entry : metaDataFilter.entrySet()){

                if (count>0)
                    labelSelectorQuery += LABEL_SEPARATOR;

                labelSelectorQuery += entry.getKey() + getEquality(entry.getValue()) + entry.getValue();
                count++;
            }

            queryMap.put("label",labelSelectorQuery);
        }

        if (clientConfig.getProperties().containsKey(ZONE_PATTERN)) {
            String zonePatternRaw = clientConfig.getProperties().get(ZONE_PATTERN).toString();
            try {
                zonePattern = Pattern.compile(zonePatternRaw);
            } catch (Exception e) {
            	if (log.isErrorEnabled())
                    log.error("Could not parse zone pattern: " + zonePatternRaw, e);
            }
        }
    }

    /**
     * If the value is already prefixed with an equality operator then return an empty string
     * else return EQUALS by default
     *
     * @param value
     * @return
     */
    private String getEquality(String value) {

        if (value.startsWith(EQUAL) || value.startsWith(NOT_EQUAL)) {
            return "";
        } else if (value.startsWith(SET_IN) || value.startsWith(SET_NOTIN)) {
            return " ";
        } else {
            return EQUAL;
        }
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

            if (!ignoreServiceId) {
            /*
            Step 1 - Search for an application that matches the specific service id
             */
                try {
                    GetAppResponse response = client.getApp(serviceId);

                    if (response != null && response.getApp() != null)
                        instances.addAll(extractServiceInstances(response.getApp()));

                } catch (MarathonException e) {
                    log.error(e.getMessage(), e);
                }
            }

            if (instances.size()==0) {

                /*
                Step 2 - Search for all applications whose marathon id contains the service id (e.g. "*.{serviceId}*.")
                This is supported by the marathon api by passing a partial id as a query parameter
                 */
                GetAppsResponse appsResponse = client.getApps(queryMap);

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

            log.debug("Discovered {} service instance{}{}", instances.size(), instances.size() == 1 ? "" : "s", ignoreServiceId ? "" : String.format(" with ids that contain [%]", serviceId));
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
    private List<MarathonServer> extractServiceInstances(App app) {

    	log.debug("Discovered service [{}]", app.getId());

        if (app.getTasks().size()==0)
            return Collections.emptyList();

        return app.getTasks()
                .stream()
                .map(task -> {
                    Collection<HealthCheckResult> healthChecks =
                            null != task.getHealthCheckResults()
                                    ? task.getHealthCheckResults()
                                    : new ArrayList<>();

                    return new MarathonServer(
                            task.getHost(),
                            task.getPorts().stream().findFirst().orElse(0),
                            healthChecks
                    ).withZone(extractZoneFromHostname(task.getHost()));
                })
                .collect(Collectors.toList());

    }

    private String extractZoneFromHostname(String host) {
        if (zonePattern == null) {
            return Server.UNKNOWN_ZONE;
        }

        Matcher matcher = zonePattern.matcher(host);
        if (matcher.find()) {
            return matcher.group(1);
        }

        log.warn("Zone was not fetched by pattern {} from hostname {}", zonePattern.pattern(), host);

        return Server.UNKNOWN_ZONE;
    }
}
