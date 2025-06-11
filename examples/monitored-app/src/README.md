# Overview
EximeeBPMS monitoring adds metric collection on process instances, incidents, tasks, and external tasks for EximeeBPMS BPM running in a Spring Boot application.

Metrics are exposed using [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html) which can then be used as part of your application monitoring solution (for example, with [Prometheus](https://prometheus.io/))

# Usage


## Metrics
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




Micrometer adds Micrometer meters spring boot
Micrometer

History event listener for counter meters

Gauge meters for open process instance, incidents, tasks, and external tasks

## Implementations

## Example monitoring processes using Prometheus
TODO


## Tags
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


## Properties
- `eximeebpms.monitoring.snapshot.enabled` - Whether the gauge snapshot monitoring is enabled. The snapshot monitoringFor clusters with multiple instances using the same database only one running instance needs to have this enabled. *Default `true`*
- `eximeebpms.monitoring.snapshot.updateRate` - Rate in milliseconds to refresh the snapshot of metrics. *Default `10000` (10 seconds)*.
