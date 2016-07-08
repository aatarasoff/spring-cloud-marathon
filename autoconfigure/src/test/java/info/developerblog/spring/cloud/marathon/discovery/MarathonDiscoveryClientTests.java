package info.developerblog.spring.cloud.marathon.discovery;

import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.model.v2.*;
import mesosphere.marathon.client.utils.MarathonException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.unitils.reflectionassert.ReflectionAssert;
import org.unitils.reflectionassert.ReflectionComparatorMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by aleksandr on 08.07.16.
 */
public class MarathonDiscoveryClientTests {
    private MarathonDiscoveryClient discoveryClient;
    private Marathon marathonClient = mock(Marathon.class);

    @Before
    public void setup() {
        discoveryClient = new MarathonDiscoveryClient(
                marathonClient,
                new MarathonDiscoveryProperties()
        );
    }

    @Test
    public void test_nullable_local_instance() {
        Assert.assertNull(discoveryClient.getLocalServiceInstance());
    }

    @Test
    public void test_list_of_servers() throws MarathonException {
        GetAppsResponse appsResponse = new GetAppsResponse();

        when(marathonClient.getApps())
                .thenReturn(appsResponse);

        appsResponse.setApps(new ArrayList<>());

        ReflectionAssert.assertReflectionEquals(
                "should be no one element",
                Collections.emptyList(),
                discoveryClient.getServices(),
                ReflectionComparatorMode.LENIENT_ORDER
        );

        //add first application
        App app1 = new App();
        app1.setId("app1");

        appsResponse.getApps().add(app1);

        ReflectionAssert.assertReflectionEquals(
                "should be only one element",
                Collections.singletonList("app1"),
                discoveryClient.getServices(),
                ReflectionComparatorMode.LENIENT_ORDER
        );

        //add another application
        App app2 = new App();
        app2.setId("app2");

        appsResponse.getApps().add(app2);

        ReflectionAssert.assertReflectionEquals(
                "should be two elements",
                IntStream.of(1,2)
                        .mapToObj(index -> "app" + index)
                        .collect(Collectors.toList()),
                discoveryClient.getServices(),
                ReflectionComparatorMode.LENIENT_ORDER
        );
    }

    @Test
    public void test_list_of_instances() throws MarathonException {
        GetAppTasksResponse tasksResponse = new GetAppTasksResponse();

        when(marathonClient.getAppTasks("service1"))
                .thenReturn(tasksResponse);

        tasksResponse.setTasks(new ArrayList<>());

        Task taskWithNoHealthChecks = new Task();
        taskWithNoHealthChecks.setAppId("app1");
        taskWithNoHealthChecks.setHost("host1");
        taskWithNoHealthChecks.setPorts(
                IntStream.of(9090)
                .boxed()
                .collect(Collectors.toList())
        );

        tasksResponse.getTasks().add(taskWithNoHealthChecks);

        Task taskWithAllGoodHealthChecks = new Task();
        taskWithAllGoodHealthChecks.setAppId("app1");
        taskWithAllGoodHealthChecks.setHost("host2");
        taskWithAllGoodHealthChecks.setPorts(
                IntStream.of(9090, 9091)
                        .boxed()
                        .collect(Collectors.toList())
        );

        HealthCheckResult healthCheckResult = new HealthCheckResult();
        healthCheckResult.setAlive(true);

        HealthCheckResult badHealthCheckResult = new HealthCheckResult();
        badHealthCheckResult.setAlive(false);

        List<HealthCheckResult> healthCheckResults = new ArrayList<>();
        healthCheckResults.add(healthCheckResult);
        healthCheckResults.add(healthCheckResult);

        taskWithAllGoodHealthChecks.setHealthCheckResults(healthCheckResults);

        tasksResponse.getTasks().add(taskWithAllGoodHealthChecks);

        Task taskWithOneBadHealthCheck = new Task();
        taskWithOneBadHealthCheck.setAppId("app1");
        taskWithOneBadHealthCheck.setHost("host3");
        taskWithOneBadHealthCheck.setPorts(
                IntStream.of(9090)
                        .boxed()
                        .collect(Collectors.toList())
        );

        List<HealthCheckResult> withBadHealthCheckResults = new ArrayList<>();
        withBadHealthCheckResults.add(healthCheckResult);
        withBadHealthCheckResults.add(badHealthCheckResult);

        taskWithOneBadHealthCheck.setHealthCheckResults(withBadHealthCheckResults);

        tasksResponse.getTasks().add(taskWithOneBadHealthCheck);

        ReflectionAssert.assertReflectionEquals(
                "should be two tasks",
                IntStream.of(1,2)
                    .mapToObj(index ->
                            new DefaultServiceInstance(
                                "app1",
                                "host" + index,
                                9090,
                                false
                            )
                    ).collect(Collectors.toList()),
                discoveryClient.getInstances("service1"),
                ReflectionComparatorMode.LENIENT_ORDER
        );
    }
}
