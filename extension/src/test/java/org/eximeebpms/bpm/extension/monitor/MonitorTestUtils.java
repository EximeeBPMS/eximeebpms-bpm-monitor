package org.eximeebpms.bpm.extension.monitor;

import org.eximeebpms.bpm.engine.ProcessEngine;
import org.eximeebpms.bpm.engine.repository.ProcessDefinition;

import org.eximeebpms.bpm.model.bpmn.Bpmn;
import org.eximeebpms.bpm.model.bpmn.BpmnModelInstance;

public class MonitorTestUtils {

  public static ProcessDefinition createIncidentGeneratingProcessDefinition(ProcessEngine processEngine) {
    String processKey = "incident-generating-process";
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(processKey)
        .id(processKey)
        .camundaHistoryTimeToLive(5000)
        .startEvent()
        .serviceTask().camundaAsyncBefore().camundaAsyncAfter()
        .camundaClass(IncidentCreator.class).endEvent().done();
    String deploymentId = processEngine.getRepositoryService().createDeployment()
        .addModelInstance(processKey + ".bpmn", modelInstance).deploy().getId();
    return processEngine.getRepositoryService().createProcessDefinitionQuery().deploymentId(deploymentId)
        .singleResult();
  }

  public static boolean waitUntilNoActiveJobs(ProcessEngine processEngine, String processDefinitionId) {
    return waitUntilNoActiveJobs(processEngine, processDefinitionId, 1);
  }

  private static boolean waitUntilNoActiveJobs(ProcessEngine processEngine, String processDefinitionId, long wait) {
    long timeout = System.currentTimeMillis() + wait;
    while (System.currentTimeMillis() < timeout) {
      long jobCount = processEngine.getManagementService().createJobQuery()
          .processDefinitionId(processDefinitionId).active().count();
      if (jobCount == 0) {
        return true;
      }

      processEngine.getManagementService().createJobQuery().list()
          .forEach(job -> processEngine.getManagementService().executeJob(job.getId()));
    }
    return false;
  }

  /***
   * Creates and deploys a process definition with only start and end events to
   * the process engine.
   *
   * @param processEngine
   * @return the deployed process definition
   */
  public static ProcessDefinition createEmptyProcessDefinition(ProcessEngine processEngine) {
    String processKey = "empty-process";
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(processKey)
        .id(processKey)
        .camundaHistoryTimeToLive(5000)
        .startEvent()
        .endEvent()
        .done();
    String deploymentId = processEngine.getRepositoryService().createDeployment()
        .addModelInstance(processKey + ".bpmn", modelInstance).deploy().getId();
    return processEngine.getRepositoryService().createProcessDefinitionQuery().deploymentId(deploymentId)
        .singleResult();
  }

  public static ProcessDefinition createUserTaskProcessDefinition(ProcessEngine processEngine) {
    String processKey = "user-task-process";
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(processKey)
        .id(processKey)
        .camundaHistoryTimeToLive(5000)
        .startEvent()
        .userTask("userTask1")
        .camundaAsyncAfter()
        .endEvent()
        .done();
    String deploymentId = processEngine.getRepositoryService().createDeployment()
        .addModelInstance(processKey + ".bpmn", modelInstance).deploy().getId();
    return processEngine.getRepositoryService().createProcessDefinitionQuery().deploymentId(deploymentId)
        .singleResult();
  }

  public static ProcessDefinition createExternalTaskProcessDefinition(ProcessEngine processEngine) {
    String processKey = "external-task-process";
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(processKey)
        .id(processKey)
        .camundaHistoryTimeToLive(5000)
        .startEvent()
        .serviceTask()
        .camundaExternalTask("test-topic")
        .endEvent()
        .done();
    String deploymentId = processEngine.getRepositoryService().createDeployment()
        .addModelInstance(processKey + ".bpmn", modelInstance).deploy().getId();
    return processEngine.getRepositoryService().createProcessDefinitionQuery().deploymentId(deploymentId)
        .singleResult();
  }

}
