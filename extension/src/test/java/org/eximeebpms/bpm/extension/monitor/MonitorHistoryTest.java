
package org.eximeebpms.bpm.extension.monitor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Objects;
import org.eximeebpms.bpm.engine.ProcessEngine;
import org.eximeebpms.bpm.engine.RepositoryService;
import org.eximeebpms.bpm.engine.externaltask.LockedExternalTask;
import org.eximeebpms.bpm.engine.repository.ProcessDefinition;
import org.eximeebpms.bpm.engine.runtime.Incident;
import org.eximeebpms.bpm.engine.runtime.ProcessInstance;
import org.eximeebpms.bpm.engine.task.Task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@SpringBootTest
class MonitorHistoryTest {

  @Autowired
  private ProcessEngine processEngine;

  @Autowired
  private MeterRegistry meterRegistry;

  @Test
  void contextLoads() {
    // Nothing to do
  }

  @BeforeEach
  void reset() {

    // Reset meters
    meterRegistry.clear();

    // Delete all process definitions and process instances
    RepositoryService repositoryService = processEngine.getRepositoryService();
    repositoryService.createProcessDefinitionQuery().list()
        .forEach(processDefinition -> repositoryService
            .deleteProcessDefinition(processDefinition.getId(), true));

  }

  @Test
  void processInstanceStartedCounter() {
    // GIVEN process definition
    ProcessDefinition processDefinition = MonitorTestUtils.createEmptyProcessDefinition(processEngine);

    // WHEN process instance started
    processEngine.getRuntimeService().startProcessInstanceById(processDefinition.getId());

    // THEN counter increamented
    Counter counter = meterRegistry.find(Meters.PROCESS_INSTANCES_STARTED.getMeterName())
        .tag(ProcessInstanceMeterTags.PROCESS_DEFINITION_ID.getTagName(),
            processDefinition.getId())
        .tag(ProcessInstanceMeterTags.PROCESS_DEFINITION_KEY.getTagName(),
            processDefinition.getKey())
        .counter();
    assertEquals(1.0d, Objects.requireNonNull(counter).count());

  }

  @Test
  void processInstanceEndedCounter() {
    // GIVEN process definition
    ProcessDefinition processDefinition = MonitorTestUtils.createEmptyProcessDefinition(processEngine);

    // WHEN process instance started
    processEngine.getRuntimeService().startProcessInstanceById(processDefinition.getId());

    // THEN counter increamented
    Counter counter = meterRegistry.find(Meters.PROCESS_INSTANCES_ENDED.getMeterName())
        .tag(ProcessInstanceMeterTags.PROCESS_DEFINITION_ID.getTagName(),
            processDefinition.getId())
        .tag(ProcessInstanceMeterTags.PROCESS_DEFINITION_KEY.getTagName(),
            processDefinition.getKey())
        .counter();
    assertEquals(1.0d, Objects.requireNonNull(counter).count());

  }

  @Test
  void incidentCreatedCounter() {
    // GIVEN process definition that creates incident
    ProcessDefinition processDefinition = MonitorTestUtils
        .createIncidentGeneratingProcessDefinition(processEngine);

    // WHEN process instance started
    processEngine.getRuntimeService().startProcessInstanceById(processDefinition.getId());
    MonitorTestUtils.waitUntilNoActiveJobs(processEngine, processDefinition.getId());

    // THEN counter incremented
    Counter counter = meterRegistry.find(Meters.INCIDENTS_CREATED.getMeterName())
        .tag(IncidentMeterTags.PROCESS_DEFINITION_ID.getTagName(), processDefinition.getId())
        .counter();
    assertEquals(1.0d, Objects.requireNonNull(counter).count());

  }

  @Test
  void incidentResolvedCounter() {
    // GIVEN process definition that creates incident
    ProcessDefinition processDefinition = MonitorTestUtils
        .createIncidentGeneratingProcessDefinition(processEngine);

    processEngine.getRuntimeService().startProcessInstanceById(processDefinition.getId());
    MonitorTestUtils.waitUntilNoActiveJobs(processEngine, processDefinition.getId());

    Incident incident = processEngine.getRuntimeService().createIncidentQuery()
        .processDefinitionId(processDefinition.getId()).singleResult();

    // WHEN incident resolved
    processEngine.getRuntimeService().resolveIncident(incident.getId());

    // THEN counter incremented
    Counter counter = meterRegistry.find(Meters.INCIDENTS_RESOLVED.getMeterName())
        .tag(IncidentMeterTags.PROCESS_DEFINITION_ID.getTagName(), processDefinition.getId())
        .counter();
    assertEquals(1.0d, Objects.requireNonNull(counter).count());

  }

  @Test
  void incidentDeletedCounter() {
    // GIVEN process definition that creates incident
    ProcessDefinition processDefinition = MonitorTestUtils
        .createIncidentGeneratingProcessDefinition(processEngine);

    processEngine.getRuntimeService().startProcessInstanceById(processDefinition.getId());
    MonitorTestUtils.waitUntilNoActiveJobs(processEngine, processDefinition.getId());

    // WHEN incident deleted (by removing process instance that has the incident)
    processEngine.getRepositoryService().deleteProcessDefinition(processDefinition.getId(), true);

    // THEN counter incremented
    Counter counter = meterRegistry.find(Meters.INCIDENTS_DELETED.getMeterName())
        .tag(IncidentMeterTags.PROCESS_DEFINITION_ID.getTagName(), processDefinition.getId())
        .counter();
    assertEquals(1.0d, Objects.requireNonNull(counter).count());

  }

  @Test
  void taskCreatedCounter() {
    // GIVEN process definition with a user task
    ProcessDefinition processDefinition = MonitorTestUtils.createUserTaskProcessDefinition(processEngine);

    // WHEN process instance started, opening the user task
    processEngine.getRuntimeService().startProcessInstanceById(processDefinition.getId());

    // THEN counter incremented
    Counter counter = meterRegistry.find(Meters.TASKS_CREATED.getMeterName())
        .tag(TaskProcessInstanceMeterTags.PROCESS_DEFINITION_ID.getTagName(), processDefinition.getId())
        .tag(TaskProcessInstanceMeterTags.TASK_DEFINITION_KEY.getTagName(), "userTask1")
        .counter();
    assertEquals(1.0d, Objects.requireNonNull(counter).count());
  }

  @Test
  void taskCompletedCounter() {
    // GIVEN process instance with an open user task
    ProcessDefinition processDefinition = MonitorTestUtils.createUserTaskProcessDefinition(processEngine);
    processEngine.getRuntimeService().startProcessInstanceById(processDefinition.getId());
    Task task = processEngine.getTaskService().createTaskQuery()
        .processDefinitionId(processDefinition.getId()).singleResult();

    // WHEN task completed
    processEngine.getTaskService().complete(task.getId());

    // THEN counter incremented
    Counter counter = meterRegistry.find(Meters.TASKS_COMPLETED.getMeterName())
        .tag(TaskProcessInstanceMeterTags.PROCESS_DEFINITION_ID.getTagName(), processDefinition.getId())
        .tag(TaskProcessInstanceMeterTags.TASK_DEFINITION_KEY.getTagName(), "userTask1")
        .counter();
    assertEquals(1.0d, Objects.requireNonNull(counter).count());
  }

  @Test
  void taskDeletedCounter() {
    // GIVEN a stand-alone task, not attached to a process instance
    // (TaskService.deleteTask() rejects tasks that belong to a running process instance,
    // and deleting a process instance itself removes its tasks via a cascade path that
    // never produces a TASK_INSTANCE_COMPLETE history event — see MonitorHistoryListener)
    Task task = processEngine.getTaskService().newTask();
    task.setName("stand-alone-task");
    processEngine.getTaskService().saveTask(task);

    // WHEN task deleted
    processEngine.getTaskService().deleteTask(task.getId());

    // THEN counter incremented
    Counter counter = meterRegistry.find(Meters.TASKS_DELETED.getMeterName())
        .tag(TaskStandAloneMeterTags.TASK_NAME.getTagName(), "stand-alone-task")
        .counter();
    assertEquals(1.0d, Objects.requireNonNull(counter).count());
  }

  @Test
  void externalTaskStartedCounter() {
    // GIVEN process definition with an external task
    ProcessDefinition processDefinition = MonitorTestUtils.createExternalTaskProcessDefinition(processEngine);

    // WHEN process instance started, creating the external task
    processEngine.getRuntimeService().startProcessInstanceById(processDefinition.getId());

    // THEN counter incremented
    Counter counter = meterRegistry.find(Meters.EXTERNAL_TASKS_STARTED.getMeterName())
        .tag(ExternalTaskMeterTags.PROCESS_DEFINITION_ID.getTagName(), processDefinition.getId())
        .tag(ExternalTaskMeterTags.TOPIC_NAME.getTagName(), "test-topic")
        .counter();
    assertEquals(1.0d, Objects.requireNonNull(counter).count());
  }

  @Test
  void externalTaskEndedCounter_onSuccess() {
    // GIVEN process instance with an open external task
    ProcessDefinition processDefinition = MonitorTestUtils.createExternalTaskProcessDefinition(processEngine);
    processEngine.getRuntimeService().startProcessInstanceById(processDefinition.getId());
    List<LockedExternalTask> lockedTasks = processEngine.getExternalTaskService()
        .fetchAndLock(1, "worker1").topic("test-topic", 60000).execute();

    // WHEN external task completed successfully
    processEngine.getExternalTaskService().complete(lockedTasks.get(0).getId(), "worker1");

    // THEN counter incremented
    Counter counter = meterRegistry.find(Meters.EXTERNAL_TASKS_ENDED.getMeterName())
        .tag(ExternalTaskMeterTags.PROCESS_DEFINITION_ID.getTagName(), processDefinition.getId())
        .tag(ExternalTaskMeterTags.TOPIC_NAME.getTagName(), "test-topic")
        .counter();
    assertEquals(1.0d, Objects.requireNonNull(counter).count());
  }

  @Test
  void externalTaskEndedCounter_onDelete() {
    // GIVEN process instance with an open external task
    ProcessDefinition processDefinition = MonitorTestUtils.createExternalTaskProcessDefinition(processEngine);
    ProcessInstance processInstance = processEngine.getRuntimeService()
        .startProcessInstanceById(processDefinition.getId());

    // WHEN the process instance (and its open external task) is deleted
    processEngine.getRuntimeService().deleteProcessInstance(processInstance.getId(), "test");

    // THEN counter incremented
    Counter counter = meterRegistry.find(Meters.EXTERNAL_TASKS_ENDED.getMeterName())
        .tag(ExternalTaskMeterTags.PROCESS_DEFINITION_ID.getTagName(), processDefinition.getId())
        .tag(ExternalTaskMeterTags.TOPIC_NAME.getTagName(), "test-topic")
        .counter();
    assertEquals(1.0d, Objects.requireNonNull(counter).count());
  }
}
