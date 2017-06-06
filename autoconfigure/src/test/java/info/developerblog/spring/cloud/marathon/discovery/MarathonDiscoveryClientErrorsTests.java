package info.developerblog.spring.cloud.marathon.discovery;

import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.utils.MarathonException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by aleksandr on 10.07.16.
 */
public class MarathonDiscoveryClientErrorsTests {
    private static MarathonDiscoveryClient discoveryClient;
    private static Marathon marathonClient = mock(Marathon.class);

    @BeforeClass
    public static void setup() throws MarathonException {
        discoveryClient = new MarathonDiscoveryClient(marathonClient);

        when(marathonClient.getApps()).thenThrow(new MarathonException(404, "Not Found"));
        when(marathonClient.getApp(anyString())).thenThrow(new MarathonException(404, "Not Found"));
    }

    @Test
    public void check_handle_marathon_exception() {
        Assert.assertTrue(discoveryClient.getServices().isEmpty());
        Assert.assertTrue(discoveryClient.getInstances("test-marathon-app").isEmpty());
    }
}
