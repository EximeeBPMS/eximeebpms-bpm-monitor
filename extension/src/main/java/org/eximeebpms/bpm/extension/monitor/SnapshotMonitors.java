package org.eximeebpms.bpm.extension.monitor;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

import org.eximeebpms.bpm.engine.ProcessEngine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;

@Component
@PropertySource("classpath:library.properties")
@ConditionalOnProperty(value = "eximeebpms.monitoring.snapshot.enabled", havingValue = "true", matchIfMissing = true)
@EnableScheduling
public class SnapshotMonitors {

    private final ProcessEngine processEngine;

    private final MeterRegistry meterRegistry;

    List<Monitor> monitors = new ArrayList<>();

    public SnapshotMonitors(ProcessEngine processEngine, MeterRegistry meterRegistry) {
        this.processEngine = processEngine;
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    void init() {
        monitors = List.of(new ProcessInstanceSnapshotMonitor(processEngine, meterRegistry),
                new IncidentSnapshotMonitor(processEngine, meterRegistry),
                new TaskSnapshotMonitor(processEngine, meterRegistry),
                new ExternalTaskSnapshotMonitor(processEngine, meterRegistry));
    }

    @Scheduled(fixedDelayString = "${eximeebpms.monitoring.snapshot.updateRate}")
    public void update() {
        monitors.forEach(Monitor::update);
    }

}
