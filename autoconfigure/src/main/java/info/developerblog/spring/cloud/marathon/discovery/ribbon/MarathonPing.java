package info.developerblog.spring.cloud.marathon.discovery.ribbon;

import com.netflix.loadbalancer.IPing;
import com.netflix.loadbalancer.Server;

/**
 * Created by aleksandr on 07.07.16.
 */
public class MarathonPing implements IPing {
    private boolean sseEnabled;
    private MarathonSSEUpdater sseUpdater;

    public MarathonPing(boolean sseEnabled, MarathonSSEUpdater sseUpdater) {
        this.sseEnabled = sseEnabled;
        this.sseUpdater = sseUpdater;
    }

    @Override
    public boolean isAlive(Server server) {
        if (sseEnabled) {
            sseUpdater.updateServerAliveStatus(server);
        }

        return server.isAlive();
    }
}
