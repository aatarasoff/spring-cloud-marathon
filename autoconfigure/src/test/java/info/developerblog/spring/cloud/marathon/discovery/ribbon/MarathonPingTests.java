package info.developerblog.spring.cloud.marathon.discovery.ribbon;

import com.netflix.loadbalancer.Server;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by aleksandr on 10.07.16.
 */
public class MarathonPingTests {
    @Test
    public void test_marathon_server() {
        assertTrue(new MarathonPing()
                .isAlive(
                        new MarathonServer("host", 9090, Collections.emptyList())
                )
        );
    }

    @Test
    public void test_not_marathon_server() {
        assertFalse(new MarathonPing().isAlive(new Server("host:9090")));
    }
}
