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

Something will be here

## Supported patterns

Spring Cloud `DiscoveryClient` implementation (supports Ribbon, Feign and Zuul)

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
