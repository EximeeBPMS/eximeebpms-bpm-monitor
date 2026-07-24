package org.eximeebpms.bpm.extension.monitor;

import org.eximeebpms.bpm.engine.ProcessEngine;
import org.eximeebpms.bpm.engine.impl.history.event.HistoricExternalTaskLogEntity;
import org.eximeebpms.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity;
import org.eximeebpms.bpm.engine.impl.history.event.HistoricIncidentEventEntity;
import org.eximeebpms.bpm.engine.impl.history.event.HistoricTaskInstanceEventEntity;
import org.eximeebpms.bpm.engine.impl.history.event.HistoryEventTypes;
import org.eximeebpms.bpm.engine.impl.persistence.entity.TaskEntity;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;

@Component
public class MonitorHistoryListener {

  private final TaggedCounter processInstancesStartedCounter;
  private final TaggedCounter processInstancesEndedCounter;

  private final TaggedCounter incidentsCreatedCounter;
  private final TaggedCounter incidentsResolvedCounter;
  private final TaggedCounter incidentsDeletedCounter;

  private final TaggedCounter tasksCreatedCounter;
  private final TaggedCounter tasksCompletedCounter;
  private final TaggedCounter tasksDeletedCounter;

  private final TaggedCounter externalTasksStartedCounter;
  private final TaggedCounter externalTasksEndedCounter;

  public MonitorHistoryListener(final MeterRegistry meterRegistry) {
    processInstancesStartedCounter = new TaggedCounter(Meters.PROCESS_INSTANCES_STARTED.getMeterName(),
        meterRegistry);
    processInstancesEndedCounter = new TaggedCounter(Meters.PROCESS_INSTANCES_ENDED.getMeterName(), meterRegistry);

    incidentsCreatedCounter = new TaggedCounter(Meters.INCIDENTS_CREATED.getMeterName(), meterRegistry);
    incidentsResolvedCounter = new TaggedCounter(Meters.INCIDENTS_RESOLVED.getMeterName(), meterRegistry);
    incidentsDeletedCounter = new TaggedCounter(Meters.INCIDENTS_DELETED.getMeterName(), meterRegistry);

    tasksCreatedCounter = new TaggedCounter(Meters.TASKS_CREATED.getMeterName(), meterRegistry);
    tasksCompletedCounter = new TaggedCounter(Meters.TASKS_COMPLETED.getMeterName(), meterRegistry);
    tasksDeletedCounter = new TaggedCounter(Meters.TASKS_DELETED.getMeterName(), meterRegistry);

    externalTasksStartedCounter = new TaggedCounter(Meters.EXTERNAL_TASKS_STARTED.getMeterName(), meterRegistry);
    externalTasksEndedCounter = new TaggedCounter(Meters.EXTERNAL_TASKS_ENDED.getMeterName(), meterRegistry);
  }

  @EventListener
  public void onHistoricProcessInstanceEvent(HistoricProcessInstanceEventEntity historyEvent) {

    if (historyEvent.isEventOfType(HistoryEventTypes.PROCESS_INSTANCE_START)) {
      processInstancesStartedCounter.increment(createProcessInstanceTags(historyEvent));
    } else if (historyEvent.isEventOfType(HistoryEventTypes.PROCESS_INSTANCE_END)) {
      processInstancesEndedCounter.increment(createProcessInstanceTags(historyEvent));
    }
  }

  private Tags createProcessInstanceTags(HistoricProcessInstanceEventEntity historyEvent) {
    return createProcessInstanceTags(historyEvent.getProcessDefinitionId(), historyEvent.getProcessDefinitionKey());
  }

  private Tags createProcessInstanceTags(String processDefinitionId, String processDefinitionKey) {
    return Tags.of(ProcessInstanceMeterTags.PROCESS_DEFINITION_ID.getTagName(), processDefinitionId)
        .and(ProcessInstanceMeterTags.PROCESS_DEFINITION_KEY.getTagName(), processDefinitionKey);
  }

  @EventListener
  public void onHistoricIncidentEvent(HistoricIncidentEventEntity historyEvent) {

    if (historyEvent.isEventOfType(HistoryEventTypes.INCIDENT_CREATE)) {
      incidentsCreatedCounter.increment(createIncidentTags(historyEvent));
    } else if (historyEvent.isEventOfType(HistoryEventTypes.INCIDENT_DELETE)) {
      incidentsDeletedCounter.increment(createIncidentTags(historyEvent));
    } else if (historyEvent.isEventOfType(HistoryEventTypes.INCIDENT_RESOLVE)) {
      incidentsResolvedCounter.increment(createIncidentTags(historyEvent));
    }
  }

  private Tags createIncidentTags(HistoricIncidentEventEntity historyEvent) {
    return IncidentMeterTags.createTags(historyEvent.getTenantId(), historyEvent.getProcessDefinitionId(),
        historyEvent.getProcessDefinitionKey(), historyEvent.getActivityId(),
        historyEvent.getFailedActivityId(), historyEvent.getIncidentType());
  }

  @EventListener
  public void onHistoricTaskInstanceEvent(HistoricTaskInstanceEventEntity historyEvent) {

    if (historyEvent.isEventOfType(HistoryEventTypes.TASK_INSTANCE_CREATE)) {
      tasksCreatedCounter.increment(createTaskTags(historyEvent));
    } else if (historyEvent.isEventOfType(HistoryEventTypes.TASK_INSTANCE_COMPLETE)) {
      // The engine fires TASK_INSTANCE_COMPLETE for both real completion and deletion,
      // distinguished only by deleteReason; there is no separate delete event.
      if (TaskEntity.DELETE_REASON_COMPLETED.equals(historyEvent.getDeleteReason())) {
        tasksCompletedCounter.increment(createTaskTags(historyEvent));
      } else {
        tasksDeletedCounter.increment(createTaskTags(historyEvent));
      }
    }
  }

  private Tags createTaskTags(HistoricTaskInstanceEventEntity historyEvent) {
    if (historyEvent.getProcessDefinitionId() != null) {
      // HistoricTaskInstanceEventEntity carries no case instance/definition reference,
      // so case-related tasks fall back to the stand-alone tag set below.
      return TaskProcessInstanceMeterTags.createTags(historyEvent.getTenantId(),
          historyEvent.getProcessDefinitionId(), historyEvent.getProcessDefinitionKey(),
          historyEvent.getTaskDefinitionKey());
    }
    return TaskStandAloneMeterTags.createTags(historyEvent.getTenantId(), historyEvent.getName());
  }

  @EventListener
  public void onHistoricExternalTaskLogEvent(HistoricExternalTaskLogEntity historyEvent) {
    // HistoricExternalTaskLogEntity tracks its log type via a "state" field, not the
    // generic eventType used by isEventOfType() — the engine never sets eventType on it.
    if (historyEvent.isCreationLog()) {
      externalTasksStartedCounter.increment(createExternalTaskTags(historyEvent));
    } else if (historyEvent.isSuccessLog() || historyEvent.isDeletionLog()) {
      externalTasksEndedCounter.increment(createExternalTaskTags(historyEvent));
    }
  }

  private Tags createExternalTaskTags(HistoricExternalTaskLogEntity historyEvent) {
    return ExternalTaskMeterTags.createTags(historyEvent.getTenantId(), historyEvent.getProcessDefinitionId(),
        historyEvent.getProcessDefinitionKey(), historyEvent.getActivityId(), historyEvent.getTopicName());
  }

}
