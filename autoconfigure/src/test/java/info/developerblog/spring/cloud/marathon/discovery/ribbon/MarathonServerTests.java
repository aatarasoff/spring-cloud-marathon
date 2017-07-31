package info.developerblog.spring.cloud.marathon.discovery.ribbon;

import mesosphere.marathon.client.model.v2.HealthCheckResults;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertTrue;

/**
 * Created by aleksandr on 11.07.16.
 */
public class MarathonServerTests {
    @Test
    public void test_without_healthchecks() {
        MarathonServer server = new MarathonServer("host", 9090, Collections.emptyList());
        assertTrue(server.isHealthChecksPassing());
    }

    @Test
    public void test_with_all_good_healthchecks() {
        Collection<HealthCheckResults> healthChecks =
                IntStream.rangeClosed(1,10)
                    .mapToObj(index -> {
                        HealthCheckResults healthCheck = new HealthCheckResults();
                        healthCheck.setAlive(true);
                        return healthCheck;
                    }).collect(Collectors.toList());
        MarathonServer server = new MarathonServer("host", 9090, healthChecks);
        assertTrue(server.isHealthChecksPassing());
    }

    @Test
    public void test_with_bad_healthchecks() {
        Collection<HealthCheckResults> healthChecks =
                IntStream.rangeClosed(1,2)
                        .mapToObj(index -> {
                            HealthCheckResults healthCheck = new HealthCheckResults();
                            healthCheck.setAlive(false);
                            return healthCheck;
                        }).collect(Collectors.toList());
        MarathonServer server = new MarathonServer("host", 9090, healthChecks);
        assertFalse(server.isHealthChecksPassing());
    }
}
