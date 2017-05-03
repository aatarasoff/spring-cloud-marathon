package info.developerblog.spring.cloud.marathon.discovery.ribbon;

import com.netflix.client.IClientConfigAware;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractServerListFilter;
import com.netflix.loadbalancer.Server;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by jamcarter on 4/6/17.
 */
@Slf4j
public class MarathonServiceHealthCheckLabelFilter extends AbstractServerListFilter<Server> implements IClientConfigAware {

    private static final String metaDataFilterKey = "MetaDataFilter.";

    private Map<String,String> metaDataFilter;

    private String clientName;

    public MarathonServiceHealthCheckLabelFilter() {
        clientName = "unknown";
        metaDataFilter = new HashMap<>();
    }

    @Override
    public void initWithNiwsConfig(IClientConfig niwsClientConfig) {

        try {

            clientName = niwsClientConfig.getClientName();

            /*
                client.ribbon.MetaDataFilter.key1=value1
                client.ribbon.MetaDataFilter.key12=value1

                or

                client:
                    ribbon:
                        MetaDataFilter:
                            key1: value1
                            key2: value2

             */

            niwsClientConfig.getProperties().entrySet().stream()
                    .filter(entry -> entry.getKey().startsWith(metaDataFilterKey))
                    .forEach(entry -> metaDataFilter.put(entry.getKey().replace(metaDataFilterKey,""),entry.getValue().toString()));

            log.debug("Client [" + clientName + "] Registered " + metaDataFilter.size() + " MetaData Filter" + ((metaDataFilter.size()==1)?"":"s"));
            metaDataFilter.entrySet().stream()
                    .forEach(entry -> log.debug("Client [" + clientName + "] MetaData Key [" + entry.getKey() + "] Value [" + entry.getValue() + "]"));


        } catch (Exception e){
            log.warn("Unable to fetch map values for MetaDataFilter",e);
        }

    }

    @Override
    public List<Server> getFilteredListOfServers(List<Server> servers) {

        List<Server> result = servers
                .stream()
                .filter(server ->
                        server instanceof MarathonServer &&
                                ((MarathonServer)server).isHealthChecksPassing() &&
                                ((MarathonServer)server).hasMetaData(metaDataFilter)
                )
                .collect(Collectors.toList());

        int numFiltered = servers.size() - result.size();

        log.debug("Client [" + clientName + "] Applied " + metaDataFilter.size() + " filter" + ((metaDataFilter.size()==1)?"":"s") + " and excluded " + numFiltered + " service instance" + ((numFiltered==1)?"":"s"));

        return result;
    }

}

