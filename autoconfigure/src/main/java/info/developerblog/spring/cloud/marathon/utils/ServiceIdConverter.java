package info.developerblog.spring.cloud.marathon.utils;

/**
 * Created by aleksandr on 12.01.17.
 */
public class ServiceIdConverter {
    public static String convertToMarathonId(String serviceId) {
        return "/" + serviceId.replace('.', '/');
    }

    public static String convertToServiceId(String marathonId) {
        if (marathonId.startsWith("/")) {
            marathonId = marathonId.substring(1);
        }

        return marathonId.replace('/', '.');
    }
}
