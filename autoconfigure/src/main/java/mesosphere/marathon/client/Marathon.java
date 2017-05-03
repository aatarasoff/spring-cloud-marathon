package mesosphere.marathon.client;

import java.util.List;
import java.util.Map;

import feign.Headers;
import feign.QueryMap;
import mesosphere.marathon.client.model.v2.App;
import mesosphere.marathon.client.model.v2.DeleteAppTaskResponse;
import mesosphere.marathon.client.model.v2.DeleteAppTasksResponse;
import mesosphere.marathon.client.model.v2.Deployment;
import mesosphere.marathon.client.model.v2.GetAppResponse;
import mesosphere.marathon.client.model.v2.GetAppTasksResponse;
import mesosphere.marathon.client.model.v2.GetAppsResponse;
import mesosphere.marathon.client.model.v2.GetEventSubscriptionRegisterResponse;
import mesosphere.marathon.client.model.v2.GetEventSubscriptionsResponse;
import mesosphere.marathon.client.model.v2.GetServerInfoResponse;
import mesosphere.marathon.client.model.v2.GetTasksResponse;
import mesosphere.marathon.client.model.v2.Group;
import mesosphere.marathon.client.model.v2.QueueResponse;
import mesosphere.marathon.client.model.v2.Result;
import mesosphere.marathon.client.utils.MarathonException;

import feign.Param;
import feign.RequestLine;

public interface Marathon {
    // Apps
    @RequestLine("GET /v2/apps")
    @Headers({"X-API-Source: marathon/client"})
    GetAppsResponse getApps() throws MarathonException;

    @RequestLine("GET /v2/apps")
    @Headers({"X-API-Source: marathon/client"})
    GetAppsResponse getApps(@QueryMap Map<String, String> queryMap) throws MarathonException;

    @RequestLine("GET /v2/apps/{id}")
    @Headers({"X-API-Source: marathon/client"})
    GetAppResponse getApp(@Param("id") String id) throws MarathonException;

    @RequestLine("GET /v2/apps/{id}/tasks")
    @Headers({"X-API-Source: marathon/client"})
    GetAppTasksResponse getAppTasks(@Param("id") String id) throws MarathonException;

    @RequestLine("GET /v2/tasks")
    @Headers({"X-API-Source: marathon/client"})
    GetTasksResponse getTasks() throws MarathonException;

    @RequestLine("POST /v2/apps")
    @Headers({"X-API-Source: marathon/client"})
    App createApp(App app) throws MarathonException;

    @RequestLine("PUT /v2/apps/{app_id}?force={force}")
    @Headers({"X-API-Source: marathon/client"})
    Result updateApp(@Param("app_id") String appId, App app,
                     @Param("force") boolean force) throws MarathonException;

    @RequestLine("POST /v2/apps/{id}/restart?force={force}")
    @Headers({"X-API-Source: marathon/client"})
    void restartApp(@Param("id") String id,@Param("force") boolean force) throws MarathonException;

    @RequestLine("DELETE /v2/apps/{id}")
    @Headers({"X-API-Source: marathon/client"})
    Result deleteApp(@Param("id") String id) throws MarathonException;

    @RequestLine("DELETE /v2/apps/{app_id}/tasks?host={host}&scale={scale}")
    @Headers({"X-API-Source: marathon/client"})
    DeleteAppTasksResponse deleteAppTasks(@Param("app_id") String appId,
                                          @Param("host") String host, @Param("scale") String scale) throws MarathonException;

    @RequestLine("DELETE /v2/apps/{app_id}/tasks/{task_id}?scale={scale}")
    DeleteAppTaskResponse deleteAppTask(@Param("app_id") String appId,
                                        @Param("task_id") String taskId, @Param("scale") String scale) throws MarathonException;

    // Groups
    @RequestLine("POST /v2/groups")
    @Headers({"X-API-Source: marathon/client"})
    Result createGroup(Group group) throws MarathonException;

    @RequestLine("DELETE /v2/groups/{id}")
    @Headers({"X-API-Source: marathon/client"})
    Result deleteGroup(@Param("id") String id) throws MarathonException;

    @RequestLine("GET /v2/groups/{id}")
    @Headers({"X-API-Source: marathon/client"})
    Group getGroup(@Param("id") String id) throws MarathonException;

    // Tasks

    // Deployments
    @RequestLine("GET /v2/deployments")
    @Headers({"X-API-Source: marathon/client"})
    List<Deployment> getDeployments() throws MarathonException;

    @RequestLine("DELETE /v2/deployments/{deploymentId}")
    @Headers({"X-API-Source: marathon/client"})
    void cancelDeploymentAndRollback(@Param("deploymentId") String id) throws MarathonException;

    @RequestLine("DELETE /v2/deployments/{deploymentId}?force=true")
    @Headers({"X-API-Source: marathon/client"})
    void cancelDeployment(@Param("deploymentId") String id) throws MarathonException;

    // Event Subscriptions

    @RequestLine("POST /v2/eventSubscriptions?callbackUrl={url}")
    @Headers({"X-API-Source: marathon/client"})
    public GetEventSubscriptionRegisterResponse register(@Param("url") String url) throws MarathonException;

    @RequestLine("DELETE /v2/eventSubscriptions?callbackUrl={url}")
    @Headers({"X-API-Source: marathon/client"})
    public GetEventSubscriptionRegisterResponse unregister(@Param("url") String url) throws MarathonException;

    @RequestLine("GET /v2/eventSubscriptions")
    @Headers({"X-API-Source: marathon/client"})
    public GetEventSubscriptionsResponse subscriptions() throws MarathonException;

    // Queue
    @RequestLine("GET /v2/queue")
    @Headers({"X-API-Source: marathon/client"})
    QueueResponse getQueue() throws MarathonException;

    // Server Info
    @RequestLine("GET /v2/info")
    @Headers({"X-API-Source: marathon/client"})
    GetServerInfoResponse getServerInfo() throws MarathonException;

    // Miscellaneous


}
