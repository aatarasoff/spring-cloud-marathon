# Spring Cloud Marathon

[![Join the chat at https://gitter.im/aatarasoff/spring-cloud-marathon](https://badges.gitter.im/aatarasoff/spring-cloud-marathon.svg)](https://gitter.im/aatarasoff/spring-cloud-marathon?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) [![Build Status](https://travis-ci.org/aatarasoff/spring-cloud-marathon.svg?branch=master)](https://travis-ci.org/aatarasoff/spring-cloud-marathon) [![Coverage Status](https://coveralls.io/repos/github/aatarasoff/spring-cloud-marathon/badge.svg?branch=master)](https://coveralls.io/github/aatarasoff/spring-cloud-marathon?branch=master)

## What it is about

This project helps with integration between [Spring Cloud](http://projects.spring.io/spring-cloud/) and [Marathon framework](https://mesosphere.github.io/marathon/) for [Apache Mesos](http://mesos.apache.org/)

## How to connect the project

At the moment you need to build and deploy artifact into maven local:
```
./gradlew publishToMavenLocal
```
Then connect local maven repository:
```
repositories {
    mavenLocal()
}
```

```
compile 'info.developerblog.spring.cloud:spring-cloud-marathon-starter:${VERSION}'
```

## Supported patterns

Spring Cloud `DiscoveryClient` implementation (supports Ribbon and Zuul)

## Running the example

Build sample application docker image:
```
./gradlew dockerBuild
```

Install native docker on Linux or docker-beta on MacOS X or Windows.
Then go to 'example' project and run `docker-compose`:
```
docker-compose up -d
```

Upload `test-marathon-app-manifest.json` as application manifest:
```
curl -XPOST http://localhost:8080/v2/apps?force=true -H "Content-Type: application/json" --data-binary @test-marathon-app-manifest.json -v
```

Then go back to project root directory && run the example application:
```
./gradlew bootRun
```

Add following record into your `/etc/hosts` file:
```
127.0.0.1 mesos-slave
```

And test application by curl:
```
curl localhost:9090/instances
curl localhost:9090/feign
```

## Enjoy!