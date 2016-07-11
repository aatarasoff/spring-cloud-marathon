package info.developerblog.spring.cloud.marathon.discovery.ribbon;

import com.netflix.loadbalancer.IPing;
import com.netflix.loadbalancer.Server;
import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.model.v2.HealthCheckResult;
import mesosphere.marathon.client.utils.MarathonException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by aleksandr on 07.07.16.
 */
public class MarathonPing implements IPing {

    public MarathonPing() {
    }

    @Override
    public boolean isAlive(Server server) {
        return server instanceof MarathonServer &&
                ((MarathonServer) server).isHealthChecksPassing();
    }
}
