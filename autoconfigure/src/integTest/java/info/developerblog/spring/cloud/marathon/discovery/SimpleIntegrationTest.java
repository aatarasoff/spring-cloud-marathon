package info.developerblog.spring.cloud.marathon.discovery;

import info.developerblog.spring.cloud.marathon.MarathonAutoConfiguration;
import info.developerblog.spring.cloud.marathon.discovery.ribbon.RibbonMarathonAutoConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

/**
 * Created by aleksandr on 09.07.16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SimpleIntegrationTest.TestConfig.class)
@WebIntegrationTest(randomPort = true)
@DirtiesContext
public class SimpleIntegrationTest {

    @Autowired
    DiscoveryClient discoveryClient;

    @Test
    public void check_app_is_registered() {
        assertEquals(1, discoveryClient.getServices().size());
    }

    @Test
    public void check_all_instances_is_running() {
        assertEquals(2, discoveryClient.getInstances("test-marathon-app").size());
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
