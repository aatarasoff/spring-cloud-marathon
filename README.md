# Spring Cloud Marathon

[![Join the chat at https://gitter.im/aatarasoff/spring-cloud-marathon](https://badges.gitter.im/aatarasoff/spring-cloud-marathon.svg)](https://gitter.im/aatarasoff/spring-cloud-marathon?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) [![Build Status](https://travis-ci.org/aatarasoff/spring-cloud-marathon.svg?branch=master)](https://travis-ci.org/aatarasoff/spring-cloud-marathon) [![Coverage Status](https://coveralls.io/repos/github/aatarasoff/spring-cloud-marathon/badge.svg?branch=master)](https://coveralls.io/github/aatarasoff/spring-cloud-marathon?branch=master)

This project helps with integration between [Spring Cloud](http://projects.spring.io/spring-cloud/) and [Marathon framework](https://mesosphere.github.io/marathon/) for [Apache Mesos](http://mesos.apache.org/)

## How to connect the project

Add `jcenter` repository:
```groovy
repositories {
    jcenter()
}
```

or for maven:
```xml
<repositories>
    <repository>
        <id>jcenter</id>
        <url>http://jcenter.bintray.com/</url>
    </repository>
</repositories>
```

And add dependency with latest version (or feel free to choose specific)
```groovy
compile 'info.developerblog.spring.cloud:spring-cloud-marathon-starter:+'
```

or for maven:
```xml
<dependency>
    <groupId>info.developerblog.spring.cloud</groupId>
    <artifactId>spring-cloud-marathon-starter</artifactId>
    <version>x.y.z</version>
</dependency>
```

## Motivation

Mesos and Marathon helps you to orchestrate microservices or other artifacts in distributed systems. In fact Marathon keeps information about current configuration including location of service, its healhchecks etc. So, do we need third-party system that keeps configuration and provides service discovery features? If you don't have any serious reason the answer will be "No".

## Supported Spring Cloud patterns

`DiscoveryClient` implementation (supports Ribbon, Feign and Zuul).

`Client Load Balancing` with Ribbon and Hystrix.

Other distributed systems patterns from Spring Cloud.

## Marathon configuration

All configuration should be provided in `bootstrap.yml`

### Development mode (without Marathon)

```yml
spring.cloud.marathon.enabled: false
```

### Standalone mode (single-host Marathon)

```yml
spring:
    cloud:
        marathon:
            scheme: http       #url scheme
            host: marathon     #marathon host
            port: 8080         #marathon port
```

### Production mode

Authentication via Marathon, providing a list of marathon masters:
```yml
spring:
    cloud:
        marathon:
            listOfServers: m1:8080,m2:8080,m3:8080       #list of marathon masters
            token: <dcos_acs_token>                      #DC/OS HTTP API Token (optional)
            username: marathon                           #username for basic auth (optional)
            password: mesos                              #password for basic auth (optional)
```

or, provide a load balanced marathon endpoint:
```yml
spring:
    cloud:
        marathon:
            endpoint: http://marathon.local:8080         #override scheme+host+port
            token: <dcos_acs_token>                      #DC/OS HTTP API Token (optional)
            username: marathon                           #username for basic auth (optional)
            password: mesos                              #password for basic auth (optional)
```

Other configuration for services and discovery you can see in [official documentation](http://cloud.spring.io/spring-cloud-static/Camden.SR3/)

### Authentication

Spring Cloud Marathon supports four methods of authentication:

- No Authentication (using Marathon Endpoint)
- Basic Authentication (using Marathon Endpoint)
- Token Authentication (using Marathon Endpoint)

#### No Authentication

Do not specify username, password, token or dcosPrivateKey
Specify a load balanced endpoint or listOfServers that describe the Marathon Endpoint. e.g.

```yml
spring:
    cloud:
        marathon:
            listOfServers: m1:8080,m2:8080,m3:8080      #list of marathon masters
```

#### Basic Authentication

Provide a username & password.  Specify an endpoint or listOfServers that describe the Marathon Endpoint. e.g.

```yml
spring:
    cloud:
        marathon:
            listOfServers: m1:8080,m2:8080,m3:8080       #list of marathon masters
            username: marathon                           #username for basic auth (optional)
            password: mesos                              #password for basic auth (optional)
```

#### Token Authentication

Provide a HTTP API token (note: tokens expire after 5 days).  Specify a load balanced endpoint or listOfServers that describe the Marathon Endpoint. e.g.

```yml
spring:
    cloud:
        marathon:
            listOfServers: m1:8080,m2:8080,m3:8080       #list of marathon masters
            token: <dcos_acs_token>                      #DC/OS HTTP API Token (optional)
```

### Services configuration

There is one specific moment for services notation and their configuration. In Marathon service id has following pattern:
```
/group/path/app
```

and symbol `/` is not allowed as a virtual host in Feign or RestTemplate. So we cannot use original service id as Spring Cloud service id. Instead of `/` in this implementation other separator: `.` is used. That means that service with id: `/group/path/app` has internal presentation: `group.path.app`.

And you should configure them like:
```yml
group.path.app:
    ribbon:
        <your settings are here>
```

If a specific service cannot be located by the id, then a second lookup is performed for services that contain the given id. e.g.
A service has been deployed using three different Marathon service ids:
```
/group1/path/app
/group2/path/app
/group3/path/app
```
A client is configured for the service name only, excluding the group & path from the id:
```yml
app:
    ribbon:
        <your settings are here>
```
Service Tasks for all three services will be discovered & used by ribbon.

Sometimes it is useful discover services that are advertising a specific capability; by API version for example:
Three versions of a service have been deployed, with the following Marathon service ids and labels:
```
/group1/path/app  "labels":{ "API_VERSION" : "V1" }
/group2/path/app  "labels":{ "API_VERSION" : "V2" }
/group3/path/app  "labels":{ "API_VERSION" : "V2" }
```
A client is configured to expect the "V2" API Version:
```yml
app:
    ribbon:
        <your settings are here>
        MetaDataFilter:
            API_VERSION: V2
```
Only service instances that contain "app" in the service id and have matching labels will be discovered & used by ribbon.

Where multiple values are specified for MetaDataFilter, all values must match service labels before ribbon will use the service instance:
```yml
app:
    ribbon:
        <your settings are here>
        MetaDataFilter:
            API_VERSION: V2
            ENVIRONMENT: UAT
            APPLICATION_OWNER: fred.bloggs@company.com
```

Combining the loose service id matching with service label filtering permits us to get creative with service discovery:
```yml
group.path:
    ribbon:
        <your settings are here>
        MetaDataFilter:
            CUSTOMER_ENTITY: V1
```
i.e. select any service deployed to /group/path that supports Version 1 of the Customer Entity

## Running the example

Build sample application docker image:
```bash
./gradlew dockerBuild
```

Install native docker on Linux or docker for MacOS X or Windows and run `docker-compose` for local environment deployment with zookeeper, mesos and marathon:
```bash
docker-compose up -d
```

Add following record into your `/etc/hosts` file:
```bash
127.0.0.1 mesos-slave
```

Then upload `test-marathon-app-manifest.json` as application manifest:
```bash
curl -XPOST http://<marathon_host>:8080/v2/apps?force=true -H "Content-Type: application/json" --data-binary @test-marathon-app-manifest.json -v
```

and run the example application:
```bash
./gradlew bootRun
```

Now you may test application by curl:
```bash
curl localhost:9090/instances
curl localhost:9090/feign
```

## Enjoy!
