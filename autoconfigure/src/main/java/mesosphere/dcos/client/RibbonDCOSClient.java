package mesosphere.dcos.client;

import com.netflix.client.ClientFactory;
import com.netflix.client.DefaultLoadBalancerRetryHandler;
import com.netflix.client.config.IClientConfig;
import com.netflix.config.ConfigurationManager;
import feign.Feign;
import feign.Response;
import feign.codec.ErrorDecoder;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.ribbon.LBClient;
import feign.ribbon.LBClientFactory;
import feign.ribbon.RibbonClient;
import mesosphere.client.common.ModelUtils;
import mesosphere.dcos.client.model.DCOSAuthCredentials;
import org.apache.commons.io.IOUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * Created by aleksandr on 11.01.17.
 */
public class RibbonDCOSClient extends DCOSClient {

    static final String DCOS_SERVICE_ID_RIBBON_PREFIX = "dcos.ribbon.";
    static final String DEFAULT_DCOS_ENDPOINT = "http://dcos";
    static final int DEFAULT_DCOS_SERVICE_RETRY_COUNT = 3;
    static final int DEFAULT_DCOS_SERVICE_CONNECTION_TIMEOUT = 1000;
    static final int DEFAULT_DCOS_SERVICE_READ_TIMEOUT = 5000;

    public static class Builder {
        private String baseEndpoint;
        private String dcosEndpoint;

        private String listOfServers;
        private String username;
        private String password;
        private String privateKey;

        private int maxRetryCount = DEFAULT_DCOS_SERVICE_RETRY_COUNT;
        private int connectionTimeout = DEFAULT_DCOS_SERVICE_CONNECTION_TIMEOUT;
        private int readTimeout = DEFAULT_DCOS_SERVICE_READ_TIMEOUT;

        public Builder(String baseEndpoint) {
            this.baseEndpoint = baseEndpoint;
        }

        public Builder withListOfServers(String listOfServers) {
            this.listOfServers = listOfServers;
            return this;
        }

        public Builder withUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder withPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder withPrivateKey(String privateKey){
            this.privateKey = privateKey;
            return this;
        }

        public DCOS build() {
            return build(true);
        }

        public DCOS build(boolean isAuthenticated) {

            if (null == listOfServers) {
                if (!StringUtils.isEmpty(privateKey) && isAuthenticated){
                    DCOSAuthCredentials credentials = DCOSAuthCredentials.forServiceAccount(username,privateKey);
                    return getInstance(baseEndpoint, credentials);

                }else if (!StringUtils.isEmpty(username) && isAuthenticated) {
                    DCOSAuthCredentials credentials = DCOSAuthCredentials.forUserAccount(username,password);
                    return getInstance(baseEndpoint, credentials);

                } else {
                    return getInstance(baseEndpoint);
                }
            } else {

                setDCOSRibbonProperty("listOfServers", listOfServers);
                setDCOSRibbonProperty("OkToRetryOnAllOperations", Boolean.TRUE.toString());
                setDCOSRibbonProperty("MaxAutoRetriesNextServer", maxRetryCount);
                setDCOSRibbonProperty("ConnectTimeout", connectionTimeout);
                setDCOSRibbonProperty("ReadTimeout", readTimeout);

                Feign.Builder builder = Feign.builder()
                        .client(RibbonClient.builder().lbClientFactory(new MarathonLBClientFactory()).build())
                        .encoder(new GsonEncoder(ModelUtils.GSON))
                        .decoder(new GsonDecoder(ModelUtils.GSON))
                        .errorDecoder(new DCOSErrorDecoder());

                if (isAuthenticated) {
                    DCOSAuthCredentials credentials = null;
                    if (!StringUtils.isEmpty(privateKey)) {
                        credentials = DCOSAuthCredentials.forServiceAccount(username, privateKey);
                    } else if (!StringUtils.isEmpty(username)) {
                        credentials = DCOSAuthCredentials.forUserAccount(username, password);
                    }

                    // Need to use a non-authenticated DCOSClient instance to perform the authorization and token refresh to avoid
                    // the complexity of synchronizing around checking whether a token needs to be refreshed.
                    builder.requestInterceptor(new DCOSAuthTokenHeaderInterceptor(credentials, build(false)));
                }

                builder.requestInterceptor(new DCOSAPIInterceptor());

                return builder.target(DCOS.class, DEFAULT_DCOS_ENDPOINT);
            }
        }

        void setDCOSRibbonProperty(String suffix, Object value) {
            ConfigurationManager.getConfigInstance().setProperty(DCOS_SERVICE_ID_RIBBON_PREFIX + suffix, value);
        }
    }

    private static class DCOSErrorDecoder implements ErrorDecoder {
        @Override
        public Exception decode(String methodKey, Response response) {

            String details;
            try {
                details = IOUtils.toString(response.body().asInputStream(), "UTF-8");
            } catch (NullPointerException | IOException e) {
                details = "Unable to read response body";
            }
            return new DCOSException(response.status(), response.reason(), methodKey, details);
        }
    }

    public static class MarathonLBClientFactory implements LBClientFactory {

        @Override
        public LBClient create(String clientName) {
            LBClient client = new Default().create(clientName);
            IClientConfig config = ClientFactory.getNamedConfig(clientName);
            client.setRetryHandler(new DefaultLoadBalancerRetryHandler(config));
            return client;
        }
    }

}