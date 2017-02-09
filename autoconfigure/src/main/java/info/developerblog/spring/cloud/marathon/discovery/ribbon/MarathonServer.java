package info.developerblog.spring.cloud.marathon.discovery.ribbon;

import com.netflix.loadbalancer.Server;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Created by aleksandr on 07.07.16.
 */
public class MarathonServer extends Server {
    MarathonMetaInfo metaInfo = null;

    public MarathonServer(String host, int port) {
        super(host, port);
    }

    public MarathonServer withMetaInfo(MarathonMetaInfo metaInfo) {
        this.metaInfo = metaInfo;
        return this;
    }

    @Override
    public MetaInfo getMetaInfo() {
        return metaInfo;
    }

    @Data
    @RequiredArgsConstructor
    public static final class MarathonMetaInfo implements MetaInfo {
        @NonNull
        String instanceId;

        @Override
        public String getAppName() {
            return null;
        }

        @Override
        public String getServerGroup() {
            return null;
        }

        @Override
        public String getServiceIdForDiscovery() {
            return null;
        }
    }
}
