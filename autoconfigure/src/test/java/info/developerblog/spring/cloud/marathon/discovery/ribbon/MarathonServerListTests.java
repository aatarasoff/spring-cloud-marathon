package info.developerblog.spring.cloud.marathon.discovery.ribbon;

import com.netflix.client.config.IClientConfig;
import info.developerblog.spring.cloud.marathon.discovery.MarathonDiscoveryProperties;
import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.model.v2.GetAppTasksResponse;
import mesosphere.marathon.client.model.v2.HealthCheckResult;
import mesosphere.marathon.client.model.v2.Task;
import mesosphere.marathon.client.utils.MarathonException;
import org.junit.Before;
import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;
import org.unitils.reflectionassert.ReflectionComparatorMode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    }

    @Test
    public void test_initial_list_of_servers() throws MarathonException {
        ReflectionAssert.assertReflectionEquals(
                "should be two servers",
                IntStream.of(1,2)
                        .mapToObj(index ->
                                new MarathonServer(
                                        "host" + index,
                                        9090
                                )
                        ).collect(Collectors.toList()),
                serverList.getInitialListOfServers(),
                ReflectionComparatorMode.LENIENT_ORDER
        );
    }

    @Test
    public void test_updated_list_of_servers() throws MarathonException {
        ReflectionAssert.assertReflectionEquals(
                "should be two servers",
                IntStream.of(1,2)
                        .mapToObj(index ->
                                new MarathonServer(
                                        "host" + index,
                                        9090
                                )
                        ).collect(Collectors.toList()),
                serverList.getUpdatedListOfServers(),
                ReflectionComparatorMode.LENIENT_ORDER
        );
    }
}
