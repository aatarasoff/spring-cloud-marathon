package info.developerblog.spring.cloud.marathon;

import info.developerblog.spring.cloud.marathon.discovery.MarathonDiscoveryClientAutoConfiguration;
import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.MarathonException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Created by aleksandr on 08.08.16.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MarathonHealthConfigErrorTests.TestConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
public class MarathonHealthConfigErrorTests {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private Marathon client;

    private MockMvc mvc;

    @Before
    public void setup() throws MarathonException {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();
        when(client.getApps()).thenThrow(new RuntimeException());
    }

    @Test
    public void test_unhealthy_health_endpoint() throws Exception {
        Assert.assertEquals(503, mvc.perform(get("/actuator/health")).andReturn().getResponse().getStatus());
    }

    @Configuration
    @EnableDiscoveryClient
    @EnableAutoConfiguration
    @Import({ MarathonAutoConfiguration.class, MarathonDiscoveryClientAutoConfiguration.class })
    public static class TestConfig {
        @Bean
        public Marathon marathonClient(MarathonProperties properties) {
            return mock(Marathon.class);
        }
    }
}
