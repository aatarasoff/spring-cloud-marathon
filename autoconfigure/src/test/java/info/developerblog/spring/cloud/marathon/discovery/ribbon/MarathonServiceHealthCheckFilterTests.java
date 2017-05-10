package info.developerblog.spring.cloud.marathon.discovery.ribbon;

import com.netflix.loadbalancer.Server;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by aleksandr on 11.07.16.
 */
public class MarathonServiceHealthCheckFilterTests {

    @Test
    public void test_filtered_list_of_servers() {
        MarathonServiceHealthCheckFilter filter = new MarathonServiceHealthCheckFilter();
        List<Server> servers = new ArrayList<>();

        MarathonServer serverWithPassingChecks = mock(MarathonServer.class);
        when(serverWithPassingChecks.isHealthChecksPassing()).thenReturn(true);
        servers.add(serverWithPassingChecks);

        MarathonServer serverWithNotPassingChecks = mock(MarathonServer.class);
        when(serverWithNotPassingChecks.isHealthChecksPassing()).thenReturn(false);
        servers.add(serverWithNotPassingChecks);

        Assert.assertEquals(1, filter.getFilteredListOfServers(servers).size());
    }

}
