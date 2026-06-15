package org.eximeebpms.bpm.extension.monitor;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.eximeebpms.bpm.engine.ProcessEngine;
import org.eximeebpms.bpm.engine.runtime.Job;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class FailedJobSnapshotMonitor extends Monitor {

  public FailedJobSnapshotMonitor(ProcessEngine processEngine, MeterRegistry meterRegistry) {
    super(processEngine, meterRegistry);
  }

  @Override
  protected List<String> getGaugeNames() {
    return Stream.of(Meters.JOBS_FAILED)
        .map(Meters::getMeterName)
        .toList();
  }

  @Override
  protected Collection<MultiGaugeData> retrieveGaugesData() {
    final Map<String, MultiGaugeData> map = new HashMap<>();
    final List<Job> jobs = getProcessEngine()
        .getManagementService()
        .createJobQuery()
        .withException()
        .unlimitedList();

    jobs.forEach(job -> {
      if (job.getProcessDefinitionId() == null) {
        return;
      }
      String groupByKey = job.getProcessDefinitionId();
      MultiGaugeData data = map.get(groupByKey);

      if (data == null) {
        Map<String, Long> gaugeValues = new HashMap<>();
        gaugeValues.put(Meters.JOBS_FAILED.getMeterName(), 0L);

        Tags tags = buildTags(job);
        data = new MultiGaugeData(gaugeValues, tags);
      }

      data.gaugesValues.merge(Meters.JOBS_FAILED.getMeterName(), 1L, Long::sum);
      map.put(groupByKey, data);
    });

    return map.values();
  }

  private Tags buildTags(Job job) {
    Tags tags = Tags.empty();
    if (job.getTenantId() != null) {
      tags = tags.and(ProcessInstanceMeterTags.TENANT_ID.getTagName(), job.getTenantId());
    }
    tags = tags.and(ProcessInstanceMeterTags.PROCESS_DEFINITION_ID.getTagName(), job.getProcessDefinitionId());
    tags = tags.and(ProcessInstanceMeterTags.PROCESS_DEFINITION_KEY.getTagName(), job.getProcessDefinitionKey());
    return tags;
  }
}
