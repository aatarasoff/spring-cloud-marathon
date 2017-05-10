package info.developerblog.spring.cloud.marathon.discovery.ribbon;

import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerListFilter;
import org.springframework.cloud.netflix.ribbon.ZonePreferenceServerListFilter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by aleksandr on 07.07.16.
 */
public class MarathonServiceHealthCheckFilter extends ZonePreferenceServerListFilter {
    @Override
    public List<Server> getFilteredListOfServers(List<Server> servers) {
        List<Server> filtered = servers
                .stream()
                .filter(server ->
                        server instanceof MarathonServer &&
                                ((MarathonServer) server).isHealthChecksPassing()
                ).collect(Collectors.toList());

        return super.getFilteredListOfServers(filtered);
    }
}
