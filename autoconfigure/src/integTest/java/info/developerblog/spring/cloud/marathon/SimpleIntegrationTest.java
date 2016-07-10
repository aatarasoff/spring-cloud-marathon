package info.developerblog.spring.cloud.marathon;

import info.developerblog.spring.cloud.marathon.discovery.MarathonDiscoveryClientAutoConfiguration;
import info.developerblog.spring.cloud.marathon.discovery.ribbon.RibbonMarathonAutoConfiguration;
import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.model.v2.*;
import mesosphere.marathon.client.utils.MarathonException;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * Created by aleksandr on 09.07.16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringApplicationConfiguration(classes = SimpleIntegrationTest.TestConfig.class)
@WebIntegrationTest(randomPort = true)
@DirtiesContext
public class SimpleIntegrationTest {
    @Autowired
    Marathon client;

    @Autowired
    DiscoveryClient discoveryClient;

    @Before
    public void setup() throws InterruptedException {
        try {
            createApplication();

            Thread.sleep(30000);
        } catch (MarathonException e) {
            //it's ok, cause we already deploy application
        }
    }

    @Test
    public void check_app_is_registered() {
        assertEquals(1, discoveryClient.getServices().size());
    }

    @Test
    public void check_all_instances_is_running() {
        assertEquals(2, discoveryClient.getInstances("test-marathon-app").size());
    }

    private void createApplication() throws MarathonException {
        App app = new App();
        app.setId("test-marathon-app");
        app.setCpus(0.5);
        app.setMem(512.0);
        app.setInstances(2);

        Container container = new Container();
        container.setType("DOCKER");

        Docker docker = new Docker();
        docker.setImage("test-marathon-app");
        docker.setNetwork("BRIDGE");

        Port port = new Port(9090);
        port.setProtocol("tcp");
        port.setServicePort(9090);
        docker.setPortMappings(Collections.singletonList(port));

        container.setDocker(docker);

        app.setContainer(container);

        client.createApp(app);
    }

    @Configuration
    @EnableAutoConfiguration
    @Import({
            MarathonAutoConfiguration.class,
            MarathonDiscoveryClientAutoConfiguration.class,
            RibbonMarathonAutoConfiguration.class
    })
    public static class TestConfig {
    }
}
