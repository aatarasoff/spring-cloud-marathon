package info.developerblog.spring.cloud.marathon;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by aleksandr on 08.07.16.
 */
public class MarathonPropertiesTests {
    @Test
    public void test_default_endpoint_generation() {
        Assert.assertEquals(
                "http://localhost:8080",
                new MarathonProperties().getEndpoint()
        );
    }

    @Test
    public void test_custom_endpoint_generation() {
        MarathonProperties marathonProperties = new MarathonProperties();
        marathonProperties.setScheme("https");
        marathonProperties.setHost("marathon-host");
        marathonProperties.setPort(9090);

        Assert.assertEquals(
                "https://marathon-host:9090",
                marathonProperties.getEndpoint()
        );
    }
}
