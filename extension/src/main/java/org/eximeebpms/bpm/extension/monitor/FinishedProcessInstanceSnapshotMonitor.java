package org.eximeebpms.bpm.extension.monitor;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.eximeebpms.bpm.engine.ProcessEngine;
import org.eximeebpms.bpm.engine.history.CleanableHistoricProcessInstanceReportResult;

public class FinishedProcessInstanceSnapshotMonitor extends Monitor {

  public FinishedProcessInstanceSnapshotMonitor(ProcessEngine processEngine, MeterRegistry meterRegistry) {
    super(processEngine, meterRegistry);
  }

  @Override
  protected List<String> getGaugeNames() {
    return Stream.of(Meters.PROCESS_INSTANCES_FINISHED)
        .map(Meters::getMeterName)
        .toList();
  }

  @Override
  protected Collection<MultiGaugeData> retrieveGaugesData() {
    final Map<String, MultiGaugeData> map = new HashMap<>();
    final List<CleanableHistoricProcessInstanceReportResult> report = getProcessEngine()
        .getHistoryService()
        .createCleanableHistoricProcessInstanceReport()
        .compact()
        .list();

    report.forEach(result -> {
      Map<String, Long> gaugeValues = new HashMap<>();
      gaugeValues.put(Meters.PROCESS_INSTANCES_FINISHED.getMeterName(), result.getFinishedProcessInstanceCount());

      Tags tags = buildTags(result);
      map.put(result.getProcessDefinitionId(), new MultiGaugeData(gaugeValues, tags));
    });

    return map.values();
  }

  private Tags buildTags(CleanableHistoricProcessInstanceReportResult result) {
    Tags tags = Tags.empty();
    if (result.getTenantId() != null) {
      tags = tags.and(ProcessInstanceMeterTags.TENANT_ID.getTagName(), result.getTenantId());
    }
    tags = tags.and(ProcessInstanceMeterTags.PROCESS_DEFINITION_ID.getTagName(), result.getProcessDefinitionId());
    tags = tags.and(ProcessInstanceMeterTags.PROCESS_DEFINITION_KEY.getTagName(), result.getProcessDefinitionKey());
    return tags;
  }
}
