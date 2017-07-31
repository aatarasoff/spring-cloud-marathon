package info.developerblog.spring.cloud.marathon.discovery.ribbon;

import com.netflix.client.config.IClientConfig;
import info.developerblog.spring.cloud.marathon.discovery.MarathonDiscoveryProperties;
import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.MarathonException;
import mesosphere.marathon.client.model.v2.App;
import mesosphere.marathon.client.model.v2.GetAppResponse;
import mesosphere.marathon.client.model.v2.GetAppsResponse;
import mesosphere.marathon.client.model.v2.Task;
import org.junit.Before;
import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;
import org.unitils.reflectionassert.ReflectionComparatorMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author alexander.tarasov
 */
public class MarathonServerListFetchZoneTests {
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

        Map<String, Object> properties = new HashMap<>();
        properties.put("ZonePattern",".+\\.(.+)");
        when(config.getProperties()).thenReturn(properties);

        serverList.initWithNiwsConfig(config);

        GetAppResponse appResponse = new GetAppResponse();

        when(marathonClient.getApp("/service1"))
                .thenReturn(appResponse);

        GetAppsResponse appsResponse = new GetAppsResponse();

        when(marathonClient.getApps())
                .thenReturn(appsResponse);

        App app = new App();
        appResponse.setApp(app);

        app.setId("/service1");
        app.setTasks(IntStream.of(1,2)
                .mapToObj(index -> {
                    Task task = new Task();
                    task.setHost("host" + index + ".dc1");
                    task.setPorts(IntStream.of(9090, 9091)
                            .boxed()
                            .collect(Collectors.toList()));
                    task.setHealthCheckResults(Collections.emptyList());
                    return task;
                }).collect(Collectors.toList())
        );

        Task withNullHealthChecks = new Task();
        withNullHealthChecks.setHost("host1.dc2");
        withNullHealthChecks.setPorts(IntStream.of(9090, 9091)
                .boxed()
                .collect(Collectors.toList()));
        app.getTasks().add(withNullHealthChecks);

        List<App> apps = new ArrayList<>();
        apps.add(app);
        appsResponse.setApps(apps);

    }

    @Test
    public void test_zone_extracted_list_of_servers() throws MarathonException {
        ReflectionAssert.assertReflectionEquals(
                "should be two servers",
                IntStream.of(1,2)
                        .mapToObj(index ->
                                new MarathonServer(
                                        "host" + index + ".dc1",
                                        9090,
                                        Collections.emptyList())
                                .withZone("dc1")
                        ).collect(Collectors.toList()),
                serverList.getInitialListOfServers().stream()
                    .filter(server -> server.getZone().equals("dc1"))
                    .collect(Collectors.toList()),
                ReflectionComparatorMode.LENIENT_ORDER
        );
    }
}
