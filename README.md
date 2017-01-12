# Spring Cloud Marathon

[![Join the chat at https://gitter.im/aatarasoff/spring-cloud-marathon](https://badges.gitter.im/aatarasoff/spring-cloud-marathon.svg)](https://gitter.im/aatarasoff/spring-cloud-marathon?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) [![Build Status](https://travis-ci.org/aatarasoff/spring-cloud-marathon.svg?branch=master)](https://travis-ci.org/aatarasoff/spring-cloud-marathon) [![Coverage Status](https://coveralls.io/repos/github/aatarasoff/spring-cloud-marathon/badge.svg?branch=master)](https://coveralls.io/github/aatarasoff/spring-cloud-marathon?branch=master)

This project helps with integration between [Spring Cloud](http://projects.spring.io/spring-cloud/) and [Marathon framework](https://mesosphere.github.io/marathon/) for [Apache Mesos](http://mesos.apache.org/)

## How to connect the project

Add `jcenter` repository:
```
repositories {
    jcenter()
}
```

or for maven:
```
<repositories>
    <repository>
        <id>jcenter</id>
        <url>http://jcenter.bintray.com/</url>
    </repository>
</repositories>
```

And add dependency with latest version (or feel free to choose specific)
```
compile 'info.developerblog.spring.cloud:spring-cloud-marathon-starter:+'
```

or for maven:
```
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-marathon-starter</artifactId>
    <version>0.4.0</version>
</dependency>
```

## Motivation

Mesos and Marathon helps you to orchestrate microservices or other artifacts in distributed systems. In fact Marathon keeps information about current configuration including localtion of service, its healhchecks etc. So, do we need third-party system that keeps configuration and provides service discovery features? If you don't have any serious reason the answer will be "No".

## Supported Spring Cloud patterns

`DiscoveryClient` implementation (supports Ribbon, Feign and Zuul).

`Client Load Balancing` with Ribbon and Hystrix.

Other distributed systems patterns from Spring Cloud.

## Marathon configuration

All configuration should be provided in `bootstrap.yml`

### Development mode (without Marathon)

```
spring.cloud.marathon.enabled: false
```

### Standalone mode (single-host Marathon)

```
spring:
    cloud:
        marathon:
            scheme: http       #url scheme
            host: marathon     #marathon host
            port: 8080         #marathon port
```

### Production mode

```
spring:
    cloud:
        marathon:
            endpoint: http://marathon                    #this overrides host and port options
            listOfServers: m1:8080,m2:8080,m3:8080       #list of marathon masters
            username: marathon                           #username for basic auth (optional)
            password: mesos                              #password for basic auth (optional)
```

Other configuration for services and discovery you can see in [official documentation](http://cloud.spring.io/spring-cloud-static/Camden.SR3/)

### Services configuration

There is one specific moment for services notation and their configuration. In Marathon service id has following pattern:
```
/group/path/app
```

and symbol `/` is not allowed as a virtual host in Feign or RestTemplate. So we cannot use original service id as Spring Cloud service id. Instead of `/` in this implementation other separator: `.` is used. That means that service with id: `/group/path/app` has internal presentation: `group.path.app`.

And you should configure them like:
```
group.path.app:
    ribbon:
        <your settings are here>
```

## Running the example

Build sample application docker image:
```
./gradlew dockerBuild
```

Install native docker on Linux or docker for MacOS X or Windows and run `docker-compose` for local environment deployment with zookeeper, mesos and marathon:
```
docker-compose up -d
```

Add following record into your `/etc/hosts` file:
```
127.0.0.1 mesos-slave
```

Then upload `test-marathon-app-manifest.json` as application manifest:
```
curl -XPOST http://<marathon_host>:8080/v2/apps?force=true -H "Content-Type: application/json" --data-binary @test-marathon-app-manifest.json -v
```

and run the example application:
```
./gradlew bootRun
```

Now you may test application by curl:
```
curl localhost:9090/instances
curl localhost:9090/feign
```

## Enjoy!
