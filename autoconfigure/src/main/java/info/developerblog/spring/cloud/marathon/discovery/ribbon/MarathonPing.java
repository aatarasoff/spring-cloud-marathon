package info.developerblog.spring.cloud.marathon.discovery.ribbon;

import com.netflix.loadbalancer.IPing;
import com.netflix.loadbalancer.Server;

/**
 * Created by aleksandr on 07.07.16.
 */
public class MarathonPing implements IPing {
    @Override
    public boolean isAlive(Server server) {
        return true;
    }
}
