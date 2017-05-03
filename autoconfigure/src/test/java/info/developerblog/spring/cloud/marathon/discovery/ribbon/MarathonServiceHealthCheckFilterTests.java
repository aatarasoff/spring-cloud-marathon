package info.developerblog.spring.cloud.marathon.discovery.ribbon;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.Server;
import mesosphere.marathon.client.model.v2.HealthCheckResult;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Test
    public void test_filtered_list_of_servers_by_label() {

        Map<String,Object> properties = new HashMap<>();
        properties.put("MetaDataFilter.ENVIRONMENT","DEV");
        properties.put("MetaDataFilter.APP_VERSION","V1");

        Map<String,String> metaDataFilter = new HashMap<>();
        metaDataFilter.put("ENVIRONMENT","DEV");
        metaDataFilter.put("APP_VERSION","V1");

        Map<String,String> metaDataFilter2 = new HashMap<>();
        metaDataFilter2.put("ENVIRONMENT","QA");
        metaDataFilter2.put("APP_VERSION","V2");

        List<HealthCheckResult> passingHealthChecks = new ArrayList<>();
        List<HealthCheckResult> failingHealthChecks = new ArrayList<>();

        HealthCheckResult passingResults = new HealthCheckResult();
        passingResults.setAlive(true);
        passingHealthChecks.add(passingResults);

        HealthCheckResult failingResults = new HealthCheckResult();
        failingResults.setAlive(false);
        failingHealthChecks.add(failingResults);

        IClientConfig config = mock(IClientConfig.class);
        when(config.getClientName()).thenReturn("service1");
        when(config.getProperties()).thenReturn(properties);

        MarathonServiceHealthCheckLabelFilter filter = new MarathonServiceHealthCheckLabelFilter();
        filter.initWithNiwsConfig(config);

        List<Server> servers = new ArrayList<>();

        Server serverWithPassingChecks = new MarathonServer("localhost",9091,passingHealthChecks,metaDataFilter);
        servers.add(serverWithPassingChecks);

        Server serverWithPassingChecks2 = new MarathonServer("localhost",9092,passingHealthChecks,metaDataFilter2);
        servers.add(serverWithPassingChecks2);

        Server serverWithNotPassingChecks = new MarathonServer("localhost",9093,failingHealthChecks,metaDataFilter);
        servers.add(serverWithNotPassingChecks);

        Assert.assertEquals(1, filter.getFilteredListOfServers(servers).size());
    }

}
