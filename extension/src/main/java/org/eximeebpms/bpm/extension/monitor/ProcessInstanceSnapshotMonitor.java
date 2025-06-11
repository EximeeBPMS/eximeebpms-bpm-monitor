package org.eximeebpms.bpm.extension.monitor;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import java.util.stream.Stream;
import org.eximeebpms.bpm.engine.ProcessEngine;
import org.eximeebpms.bpm.engine.repository.ProcessDefinition;
import org.eximeebpms.bpm.engine.runtime.ProcessInstance;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;

public class ProcessInstanceSnapshotMonitor extends Monitor {

    public ProcessInstanceSnapshotMonitor(ProcessEngine processEngine, MeterRegistry meterRegistry) {
        super(processEngine, meterRegistry);
    }

    @Override
    protected List<String> getGaugeNames() {

        return Stream.of(Meters.PROCESS_INSTANCES_RUNNING, Meters.PROCESS_INSTANCES_SUSPENDED)
                .map(Meters::getMeterName).collect(Collectors.toList());
    }

    @Override
    protected Collection<MultiGaugeData> retrieveGaugesData() {
        Map<String, MultiGaugeData> map = new HashMap<>();
        List<ProcessInstance> pis = getProcessEngine().getRuntimeService().createProcessInstanceQuery().unlimitedList();

        for (ProcessInstance pi : pis) {
            String groupByKey = pi.getProcessDefinitionId();
            MultiGaugeData data = map.get(groupByKey);

            if (data == null) {
                Map<String, Long> gaugeValues = new HashMap<>();
                gaugeValues.put(Meters.PROCESS_INSTANCES_RUNNING.getMeterName(), 0L);
                gaugeValues.put(Meters.PROCESS_INSTANCES_SUSPENDED.getMeterName(), 0L);

                ProcessDefinition processDefinition = getProcessDefinition(pi.getProcessDefinitionId());
                Tags tags = ProcessInstanceMeterTags.createTags(pi.getTenantId(), pi.getProcessDefinitionId(),
                        processDefinition.getKey());

                data = new MultiGaugeData(gaugeValues, tags);
            }

            data.gaugesValues.merge(Meters.PROCESS_INSTANCES_RUNNING.getMeterName(), 1L, Long::sum);

            if (pi.isSuspended()) {
                data.gaugesValues.merge(Meters.PROCESS_INSTANCES_SUSPENDED.getMeterName(), 1L, Long::sum);
            }
            map.put(groupByKey, data);

        }

        return map.values();
    }

}
