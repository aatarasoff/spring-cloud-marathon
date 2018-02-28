package info.developerblog.spring.cloud.marathon;

import info.developerblog.spring.cloud.marathon.discovery.MarathonDiscoveryClientAutoConfiguration;
import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.MarathonException;
import mesosphere.marathon.client.model.v2.App;
import mesosphere.marathon.client.model.v2.GetAppsResponse;
import mesosphere.marathon.client.model.v2.GetServerInfoResponse;
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
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.IsAnything.anything;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Created by aleksandr on 12.08.16.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MarathonEndpointTests.TestConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
public class MarathonEndpointTests {
    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @Before
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();
    }

    @Test
    public void test_endpoint() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/actuator/marathon"))
                .andExpect(MockMvcResultMatchers.jsonPath("serverInfo", anything()))
                .andExpect(MockMvcResultMatchers.jsonPath("apps[0].id", equalTo("test-app")))
                .andReturn()
                .getResponse();
        Assert.assertEquals(200, response.getStatus());
    }

    @Configuration
    @EnableDiscoveryClient
    @EnableAutoConfiguration
    @Import({ MarathonAutoConfiguration.class, MarathonDiscoveryClientAutoConfiguration.class })
    public static class TestConfig {
        @Bean
        public Marathon marathonClient(MarathonProperties properties) throws MarathonException {
            Marathon client = mock(Marathon.class);

            when(client.getServerInfo()).thenReturn(new GetServerInfoResponse());

            GetAppsResponse appsResponse = new GetAppsResponse();
            App app = new App();
            app.setId("test-app");
            appsResponse.setApps(Collections.singletonList(app));
            when(client.getApps()).thenReturn(appsResponse);

            return client;
        }
    }
}
