package org.eximeebpms.bpm.extension.monitor;

import lombok.Getter;

/**
 * Meters to expose
 */
@Getter
public enum Meters {

    PROCESS_INSTANCES_STARTED("eximeebpms.process.instances.started"),
    PROCESS_INSTANCES_ENDED("eximeebpms.process.instances.ended"),
    PROCESS_INSTANCES_RUNNING("eximeebpms.process.instances.running.total"),
    PROCESS_INSTANCES_SUSPENDED("eximeebpms.process.instances.running.suspended.total"),
    INCIDENTS_CREATED("eximeebpms.incidents.created"),
    INCIDENTS_DELETED("eximeebpms.incidents.deleted"),
    INCIDENTS_RESOLVED("eximeebpms.incidents.resolved"),
    INCIDENTS_OPEN("eximeebpms.incidents.open.total"),
    INCIDENTS_OPEN_NEWEST("eximeebpms.incidents.open.age.newest.seconds"),
    INCIDENTS_OPEN_OLDEST("eximeebpms.incidents.open.age.oldest.seconds"),
    TASKS_CREATED("eximeebpms.tasks.created"),
    TASKS_COMPLETED("eximeebpms.tasks.completed"),
    TASKS_DELETED("eximeebpms.tasks.deleted"),
    TASKS_OPEN("eximeebpms.tasks.open.total"),
    TASKS_OPEN_NEWEST("eximeebpms.tasks.open.age.newest.seconds"),
    TASKS_OPEN_OLDEST("eximeebpms.tasks.open.age.oldest.seconds"),
    EXTERNAL_TASKS_STARTED("eximeebpms.external.tasks.started"),
    EXTERNAL_TASKS_ENDED("eximeebpms.external.tasks.ended"),
    EXTERNAL_TASKS_OPEN("eximeebpms.external.tasks.open.total"),
    EXTERNAL_TASKS_OPEN_ERROR("eximeebpms.external.tasks.open.error.total");

    Meters(String meterName) {
        this.meterName = meterName;
    }

    private final String meterName;

}
