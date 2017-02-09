package info.developerblog.spring.cloud.marathon.discovery.ribbon;

import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerListFilter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by aleksandr on 07.07.16.
 */
public class MarathonServiceHealthCheckFilter implements ServerListFilter<Server> {
    @Override
    public List<Server> getFilteredListOfServers(List<Server> servers) {
        return servers.stream().filter(Server::isAlive).collect(Collectors.toList());
    }
}
