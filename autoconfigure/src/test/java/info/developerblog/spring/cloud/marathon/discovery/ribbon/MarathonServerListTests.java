package info.developerblog.spring.cloud.marathon.discovery.ribbon;

import com.netflix.client.config.IClientConfig;
import info.developerblog.spring.cloud.marathon.discovery.MarathonDiscoveryProperties;
import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.model.v2.*;
import mesosphere.marathon.client.utils.MarathonException;
import org.junit.Before;
import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;
import org.unitils.reflectionassert.ReflectionComparatorMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by aleksandr on 08.07.16.
 */
public class MarathonServerListTests {
    private MarathonServerList serverList;
    private Marathon marathonClient = mock(Marathon.class);

    @Before
    public void setup() throws MarathonException {
        serverList = new MarathonServerList(
                marathonClient,
                new MarathonDiscoveryProperties()
        );

        IClientConfig config = mock(IClientConfig.class);
        when(config.getClientName()).thenReturn("service1");

        serverList.initWithNiwsConfig(config);

        GetAppResponse appResponse = new GetAppResponse();

        when(marathonClient.getApp("/service1"))
                .thenReturn(appResponse);

        App app = new App();
        appResponse.setApp(app);

        app.setTasks(IntStream.of(1,2)
                        .mapToObj(index -> {
                            Task task = new Task();
                            task.setHost("host" + index);
                            task.setPorts(IntStream.of(9090, 9091)
                                    .boxed()
                                    .collect(Collectors.toList()));
                            task.setHealthCheckResults(Collections.emptyList());
                            return task;
                        }).collect(Collectors.toList())
        );

        Task withNullHealthChecks = new Task();
        withNullHealthChecks.setHost("host3");
        withNullHealthChecks.setPorts(IntStream.of(9090, 9091)
                .boxed()
                .collect(Collectors.toList()));
        app.getTasks().add(withNullHealthChecks);
    }

    @Test
    public void test_initial_list_of_servers() throws MarathonException {
        ReflectionAssert.assertReflectionEquals(
                "should be three servers",
                IntStream.of(1,2,3)
                        .mapToObj(index ->
                                new MarathonServer(
                                        "host" + index,
                                        9090,
                                        Collections.emptyList())
                        ).collect(Collectors.toList()),
                serverList.getInitialListOfServers(),
                ReflectionComparatorMode.LENIENT_ORDER
        );
    }

    @Test
    public void test_updated_list_of_servers() throws MarathonException {
        ReflectionAssert.assertReflectionEquals(
                "should be three servers",
                IntStream.of(1,2,3)
                        .mapToObj(index ->
                                new MarathonServer(
                                        "host" + index,
                                        9090,
                                        Collections.emptyList())
                        ).collect(Collectors.toList()),
                serverList.getUpdatedListOfServers(),
                ReflectionComparatorMode.LENIENT_ORDER
        );
    }

    @Test
    public void test_with_null_client() {
        MarathonServerList serverList = new MarathonServerList(null, new MarathonDiscoveryProperties());
        assertTrue(serverList.getInitialListOfServers().isEmpty());
    }
}
