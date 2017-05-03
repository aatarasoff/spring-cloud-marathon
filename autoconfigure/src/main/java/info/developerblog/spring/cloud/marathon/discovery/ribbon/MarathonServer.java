package info.developerblog.spring.cloud.marathon.discovery.ribbon;

import com.netflix.loadbalancer.Server;
import mesosphere.marathon.client.model.v2.HealthCheckResult;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by aleksandr on 07.07.16.
 */
public class MarathonServer extends Server {

    private Collection<HealthCheckResult> healthChecks;
    private Map<String,String> metaData;

    public MarathonServer(String host, int port, Collection<HealthCheckResult> healthChecks) {
        super(host, port);
        this.healthChecks = healthChecks;
        this.metaData = new HashMap<>();
    }

    public MarathonServer(String host, int port, Collection<HealthCheckResult> healthChecks, Map<String,String> metaData) {
        super(host, port);
        this.healthChecks = healthChecks;
        this.metaData = (metaData!=null)?metaData:new HashMap<>();
    }

    public boolean isHealthChecksPassing() {
        return healthChecks.parallelStream()
                .allMatch(HealthCheckResult::isAlive);
    }


    /**
     * returns true if all metaDataFilters are found to be present
     * returns true if no metaDataFilters are provided
     *
     * @param metaDataFilters
     * @return
     */
    public boolean hasMetaData(Map<String,String> metaDataFilters) {

        if (metaDataFilters.size()==0) return true;

        final Set<Map.Entry<String, String>> attributes = Collections.unmodifiableSet(metaDataFilters.entrySet());
        return metaData.entrySet().containsAll(attributes);
    }
}
