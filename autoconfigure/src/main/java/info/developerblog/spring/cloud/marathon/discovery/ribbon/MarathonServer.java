package info.developerblog.spring.cloud.marathon.discovery.ribbon;

import com.netflix.loadbalancer.Server;

/**
 * Created by aleksandr on 07.07.16.
 */
public class MarathonServer extends Server {
    public MarathonServer(String host, int port) {
        super(host, port);
    }
}
