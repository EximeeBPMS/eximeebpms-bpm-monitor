![](https://img.shields.io/badge/Compatible%20with-EximeeBPMS-26d07c)
# Introduction
This extensions allows you to easily add application monitoring for your EximeeBPMS project on Spring Boot.

The extension creates [Micrometer](https://micrometer.io/) gauge and counter meters for EximeeBPMS's Process Instances, Incidents, User Tasks, and External Tasks. These metrics are exposed using Spring Boot's Actuator which provides a vendor-nuetral facade to expose metrics to many popular monitoring systems (i.e. Elastic, Prometheus). See [here](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.metrics).

Your chosen monitoring system can then use these metrics for monitoring, health checks and alarming.

# Quick Start Guide
Quick start guide for Maven project
1. Include the monitor extension dependency
``` xml
<dependency>
    <groupId>org.eximeebpms.bpm.extension.monitor</groupId>
    <artifactId>eximeebpms-bpm-spring-boot-monitor</artifactId>
    <version>0.1.7</version>
</dependency>
```
2. Add the `@EnableMonitoring` annotation to your application entrypoint class.
``` java
@SpringBootApplication
@org.eximeebpms.bpm.extension.monitor.EnableMonitoring
public class MyEximeeBPMSApp {

    public static void main(String[] args) {

        SpringApplication.run(MyEximeeBPMSApp.class, args);
    }

}

```
# Examples
## Simple Example
Sample EximeeBPMS SpringBoot application with the monitoring extension added. The sample application runs a process definition (`examples/monitored-app/src/main/resources/process.bpmn`) that will generate process instances, tasks, stand-alone tasks, external tasks, and incidents to produce the metrics for monitoring.

1. Run the application:
``` sh
cd examples/monitored-app
mvn spring-boot:run
```
2. EximeeBPMS metrics are available with Spring Actuator: http://localhost:8080/actuator/prometheus
```
...
# HELP eximeebpms_process_instances_running_suspended_total
# TYPE eximeebpms_process_instances_running_suspended_total gauge
eximeebpms_process_instances_running_suspended_total{process_definition_id="process:1:6e37439c-231a-11ec-b12a-00155dc41a84",process_definition_key="process",} 0.0
# HELP eximeebpms_tasks_open_age_newest_seconds
# TYPE eximeebpms_tasks_open_age_newest_seconds gauge
eximeebpms_tasks_open_age_newest_seconds{task_name="Task A",} 3.0
...
```

## Prometheus Cluster Example
Example setup with a cluster of application nodes running on Docker swarm and monitored with Prometheus.

1. Run the application in docker stack (deploys docker stack called `my-monitored-app`):
    ``` sh
    cd examples/docker-stack-prometheus
    ./run.sh
    ```

2. Wait for the application stack to start-up (may take a couple of minutes) and the 4 EximeeBPMS instances in the cluster to be scraped by Prometheus. Status of the target instances can be checked here: http://localhost:9090/targets

3. Run Prometheus queries on eximeebpms metrics: http://localhost:9090

    For example, the process definition called `process` in the application is expected to start every 5 seconds (~60 times in 5 minutes) across all instances, this can be checked with the query:

    ```
    sum(increase(eximeebpms_process_instances_started_total{process_definition_key="process"}[5m]))
    ```
4. Remove the docker stack and its database volume with:
    ``` sh
    docker stack rm my-monitored-app
    docker volume rm my-monitored-app_db-data
    ```

# Guide

## Metrics
The extension provides metrics on Process Instances, Incidents, Tasks, and External Tasks:
- `eximeebpms.process.instances.started`
- `eximeebpms.process.instances.ended`
- `eximeebpms.process.instances.running.total`
- `eximeebpms.process.instances.running.suspended.total`
- `eximeebpms.incidents.created`
- `eximeebpms.incidents.deleted`
- `eximeebpms.incidents.resolved`
- `eximeebpms.incidents.open.total`
- `eximeebpms.incidents.open.age.newest.seconds`
- `eximeebpms.incidents.open.age.oldest.seconds`
- `eximeebpms.tasks.created`
- `eximeebpms.tasks.completed`
- `eximeebpms.tasks.deleted`
- `eximeebpms.tasks.open.total`
- `eximeebpms.tasks.open.age.newest.seconds`
- `eximeebpms.tasks.open.age.oldest.seconds`
- `eximeebpms.external.tasks.started`
- `eximeebpms.external.tasks.ended`
- `eximeebpms.external.tasks.open.total`
- `eximeebpms.external.tasks.open.error.total`

## Tags
Metrics include the following tags:
- Process instance tags:
    - `tenant.id`
    - `process.definition.id`
    - `process.definition.key`
- Incident tags:
    - `tenant.id`
    - `process.definition.id`
    - `process.definition.key`
    - `activity.id`
    - `failed.activity.id`
    - `incident.type`
- User task (related to a process instance) tags:
    - `tenant.id`
    - `process.definition.id`
    - `process.definition.key`
    - `task.definition.id`
- User task (related to a case instance) tags:
    - `tenant.id`
    - `case.definition.id`
    - `task.definition.id`
- User task (created stand-alone) tags:
    - `tenant.id`
    - `task.name`
- External task tags:
    - `tenant.id`
    - `topic.name`
    - `activity.id`
    - `process.definition.id`
    - `process.definition.key`

## Cluster considerations
When running an applcaition in a cluster using a shared database, it is only necassary for **one** of the instances to be retreiving the guage metrics which provide the current snapshot from the database (for example, `eximeebpms.process.instances.running.total` is a guage metric and would have the same value for all instances). However it is important that counter metrics are being monitored on all instances by the monitoring system. For example, `eximeebpms.process.instances.started` metric will provide the count of process instances started on the instance since the instance started, so to alert if a particular process start count drops below a threshold then all instances in the cluster need to be monitored.

The `eximeebpms.monitoring.snapshot.enabled` Spring Boot property can be set to true on a single instance in the cluster.

In the below docker-compose fragment using Docker Stack, one service is set to monitor the snapshots which has only one replica, while the other service which can have many replicas to scale with load has snapshot monitoring disabled. See the `docker-stack-prometheus` example to see this in action.
```
  my-monitored-app:
    image: my-monitored-app
    deploy:
      replicas: 3
    ports:
      - "8080:8080"
    networks:
      - eximeebpms-overlay
    environment:
      SPRING_DATASOURCE_DRIVER_CLASS_NAME: org.postgresql.Driver
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/process-engine
      SPRING_DATASOURCE_USERNAME: admin
      SPRING_DATASOURCE_PASSWORD: admin
      EXIMEEBPMS_MONITORING_SNAPSHOT_ENABLED: "false"

  my-monitored-app-snapshot-enabled:
    image: my-monitored-app
    deploy:
      replicas: 1
    networks:
      - eximeebpms-overlay
    ports:
      - "8081:8080"
    environment:
      SPRING_DATASOURCE_DRIVER_CLASS_NAME: org.postgresql.Driver
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/process-engine
      SPRING_DATASOURCE_USERNAME: admin
      SPRING_DATASOURCE_PASSWORD: admin
      EXIMEEBPMS_MONITORING_SNAPSHOT_ENABLED: "true"
```

## Spring Boot properties
The extension provides the following Spring Boot properties which can be configured in the application (i.e. `resources/application.properties` file or similar).
- `eximeebpms.monitoring.snapshot.enabled` - Whether the gauge snapshot monitoring is enabled. For clusters with multiple instances using the same database only one running instance needs to have this enabled. *Default `true`*
- `eximeebpms.monitoring.snapshot.updateRate` - Rate in milliseconds to refresh the snapshot of metrics. *Default `10000` (10 seconds)*.

## Reporting limitations
The extension uses counter metrics and does not do any lookups on EximeeBPMS's history database which keeps the monitoring extension light-weight and scalable. When an instance is stopped or crashes the monitoring system may not have been synced with the latest metrics and when the instance starts again the counts will reset to 0. So the counter metrics in the monitoring system may not be *EXACT*, which is likely important for reporting use cases but not so relevant for application monitoring.

Note: A suitable reporting soltuion for EximeeBPMS would either do lookups on the history database or hooked in with a history event handler, which is not the purpose of this extension. See [here](https://docs.eximeebpms.org/manual/latest/user-guide/process-engine/history/).


# License
MIT License
# Maintainer
 - [bsorahan](https://github.com/bsorahan)

 # Development
  The project uses the maven-release-plugin to deploy artifacts to [OSSRH](https://central.sonatype.org/publish/publish-guide/) and release deployments are promoted to [Maven Central](https://search.maven.org/)
 ## Snapshot Deployment
 1. Ensure that the project version is a SNAPSHOT (update POM version as necassary with `mvn versions:set -DnewVersion={{target release}}-SNAPSHOT`)
 2. Deply the snapshot
  ``` sh
mvn clean deploy -P release
 ```
3. Newly deployed snapshot artifacts deployed here: https://s01.oss.sonatype.org/content/repositories/snapshots/com/squashedbug/camunda/bpm/extension/monitor/

 ## Release Deployment (into Maven Central)
 ``` sh
mvn release:clean release:prepare
mvn release:perform
 ```
