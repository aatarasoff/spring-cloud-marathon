package info.developerblog.spring.cloud.marathon;

import info.developerblog.spring.cloud.marathon.discovery.MarathonDiscoveryClientAutoConfiguration;
import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.utils.MarathonException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Created by aleksandr on 12.08.16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MarathonEndpointErrorTests.TestConfig.class)
@WebIntegrationTest(randomPort = true)
@DirtiesContext
public class MarathonEndpointErrorTests {
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
        when(client.getServerInfo()).thenThrow(new RuntimeException());
    }

    @Test
    public void test_unhealthy_endpoint() throws Exception {
        Assert.assertEquals(200, mvc.perform(get("/marathon")).andReturn().getResponse().getStatus());
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
