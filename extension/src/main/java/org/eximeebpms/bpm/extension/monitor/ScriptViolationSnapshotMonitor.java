package org.eximeebpms.bpm.extension.monitor;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.eximeebpms.bpm.engine.ProcessEngine;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptViolationStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Exposes {@code eximeebpms.script.violations.total} as a Micrometer gauge reflecting
 * the current total violation count from {@link ScriptViolationStore}.
 *
 * <p>The gauge is pull-based: Micrometer calls the supplier on each scrape, so there
 * is no scheduled refresh needed. Returns 0 when script security is disabled.
 */
@Component
@ConditionalOnProperty(value = "eximeebpms.monitoring.snapshot.enabled", havingValue = "true", matchIfMissing = true)
public class ScriptViolationSnapshotMonitor {

  private final ProcessEngine processEngine;
  private final MeterRegistry meterRegistry;

  public ScriptViolationSnapshotMonitor(ProcessEngine processEngine, MeterRegistry meterRegistry) {
    this.processEngine = processEngine;
    this.meterRegistry = meterRegistry;
  }

  @PostConstruct
  void init() {
    ScriptViolationStore store = ((ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration())
        .getScriptViolationStore();
    Gauge.builder(Meters.SCRIPT_VIOLATIONS_TOTAL.getMeterName(), store, s -> (double) s.getTotalCount())
        .description("Total number of script violations recorded since engine start")
        .register(meterRegistry);
  }
}
