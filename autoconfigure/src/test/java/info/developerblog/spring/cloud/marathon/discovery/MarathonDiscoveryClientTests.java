package info.developerblog.spring.cloud.marathon.discovery;

import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.model.v2.*;
import mesosphere.marathon.client.MarathonException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.unitils.reflectionassert.ReflectionAssert;
import org.unitils.reflectionassert.ReflectionComparatorMode;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by aleksandr on 08.07.16.
 */
public class MarathonDiscoveryClientTests {
    private static MarathonDiscoveryClient discoveryClient;
    private static Marathon marathonClient = mock(Marathon.class);

    @BeforeClass
    public static void setup() {
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
        GetAppResponse appResponse = new GetAppResponse();

        when(marathonClient.getApp("/app1"))
                .thenReturn(appResponse);

        appResponse.setApp(new App());
        appResponse.getApp().setTasks(new ArrayList<>());

        Task taskWithNoHealthChecks = new Task();
        taskWithNoHealthChecks.setAppId("/app1");
        taskWithNoHealthChecks.setHost("host1");
        taskWithNoHealthChecks.setPorts(
                IntStream.of(9090)
                .boxed()
                .collect(Collectors.toList())
        );

        appResponse.getApp().getTasks().add(taskWithNoHealthChecks);

        Task taskWithAllGoodHealthChecks = new Task();
        taskWithAllGoodHealthChecks.setAppId("/app1");
        taskWithAllGoodHealthChecks.setHost("host2");
        taskWithAllGoodHealthChecks.setPorts(
                IntStream.of(9090, 9091)
                        .boxed()
                        .collect(Collectors.toList())
        );

        HealthCheckResults healthCheckResult = new HealthCheckResults();
        healthCheckResult.setAlive(true);

        HealthCheckResults badHealthCheckResult = new HealthCheckResults();
        badHealthCheckResult.setAlive(false);

        List<HealthCheckResults> healthCheckResults = new ArrayList<>();
        healthCheckResults.add(healthCheckResult);
        healthCheckResults.add(healthCheckResult);

        taskWithAllGoodHealthChecks.setHealthCheckResults(healthCheckResults);

        appResponse.getApp().getTasks().add(taskWithAllGoodHealthChecks);

        Task taskWithOneBadHealthCheck = new Task();
        taskWithOneBadHealthCheck.setAppId("/app1");
        taskWithOneBadHealthCheck.setHost("host3");
        taskWithOneBadHealthCheck.setPorts(
                IntStream.of(9090)
                        .boxed()
                        .collect(Collectors.toList())
        );

        List<HealthCheckResults> withBadHealthCheckResults = new ArrayList<>();
        withBadHealthCheckResults.add(healthCheckResult);
        withBadHealthCheckResults.add(badHealthCheckResult);

        taskWithOneBadHealthCheck.setHealthCheckResults(withBadHealthCheckResults);

        appResponse.getApp().getTasks().add(taskWithOneBadHealthCheck);

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
                discoveryClient.getInstances("app1"),
                ReflectionComparatorMode.LENIENT_ORDER
        );
    }

    @Test
    public void test_list_of_instances_containsId_with_labels() throws MarathonException {
        GetAppResponse appResponse = new GetAppResponse();
        GetAppsResponse appsResponse = new GetAppsResponse();
        Map<String,String> queryMap = new HashMap<String,String>(){{put("id","/app");}};

        when(marathonClient.getApps(queryMap))
                .thenReturn(appsResponse);

        appsResponse.setApps(new LinkedList<>());

        App app1 = new App();
        appsResponse.getApps().add(app1);
        app1.setId("/group1/app");
        app1.setTasks(new ArrayList<>());
        app1.setLabels(new HashMap<String,String>(){{put("group","group1");}});

        Task taskWithNoHealthChecks = new Task();
        taskWithNoHealthChecks.setAppId("/group1/app");
        taskWithNoHealthChecks.setHost("host1");
        taskWithNoHealthChecks.setPorts(
                IntStream.of(9090)
                        .boxed()
                        .collect(Collectors.toList())
        );

        app1.getTasks().add(taskWithNoHealthChecks);

        GetAppResponse app1Response = new GetAppResponse();
        app1Response.setApp(app1);
        when(marathonClient.getApp("/group1/app")).thenReturn(app1Response);

        App app2 = new App();
        appsResponse.getApps().add(app2);
        app2.setId("/group2/app");
        app2.setTasks(new ArrayList<>());
        app2.setLabels(new HashMap<String,String>(){{put("group","group2");}});

        Task taskWithAllGoodHealthChecks = new Task();
        taskWithAllGoodHealthChecks.setAppId("/group2/app");
        taskWithAllGoodHealthChecks.setHost("host2");
        taskWithAllGoodHealthChecks.setPorts(
                IntStream.of(9090, 9091)
                        .boxed()
                        .collect(Collectors.toList())
        );

        HealthCheckResults healthCheckResult = new HealthCheckResults();
        healthCheckResult.setAlive(true);

        HealthCheckResults badHealthCheckResult = new HealthCheckResults();
        badHealthCheckResult.setAlive(false);

        List<HealthCheckResults> healthCheckResults = new ArrayList<>();
        healthCheckResults.add(healthCheckResult);
        healthCheckResults.add(healthCheckResult);

        taskWithAllGoodHealthChecks.setHealthCheckResults(healthCheckResults);

        app2.getTasks().add(taskWithAllGoodHealthChecks);

        GetAppResponse app2Response = new GetAppResponse();
        app2Response.setApp(app2);
        when(marathonClient.getApp("/group2/app")).thenReturn(app2Response);


        App app3 = new App();
        appsResponse.getApps().add(app3);
        app3.setId("/group3/app");
        app3.setTasks(new ArrayList<>());
        app3.setLabels(new HashMap<String,String>(){{put("group","group3");}});

        Task taskWithOneBadHealthCheck = new Task();
        taskWithOneBadHealthCheck.setAppId("/group3/app");
        taskWithOneBadHealthCheck.setHost("host3");
        taskWithOneBadHealthCheck.setPorts(
                IntStream.of(9090)
                        .boxed()
                        .collect(Collectors.toList())
        );

        List<HealthCheckResults> withBadHealthCheckResults = new ArrayList<>();
        withBadHealthCheckResults.add(healthCheckResult);
        withBadHealthCheckResults.add(badHealthCheckResult);

        taskWithOneBadHealthCheck.setHealthCheckResults(withBadHealthCheckResults);

        app3.getTasks().add(taskWithOneBadHealthCheck);

        GetAppResponse app3Response = new GetAppResponse();
        app3Response.setApp(app3);
        when(marathonClient.getApp("/group3/app")).thenReturn(app3Response);


        ServiceInstance svc1 = new DefaultServiceInstance("group1.app", "host1", 9090, false);
        svc1.getMetadata().put("group","group1");

        ServiceInstance svc2 = new DefaultServiceInstance("group2.app", "host2", 9090, false);
        svc2.getMetadata().put("group","group2");

        ReflectionAssert.assertReflectionEquals(
                "should be two tasks",
                new ArrayList<ServiceInstance>() {{
                    add(svc1);
                    add(svc2);
                }},
                discoveryClient.getInstances("app"),
                ReflectionComparatorMode.LENIENT_ORDER
        );
    }
}
