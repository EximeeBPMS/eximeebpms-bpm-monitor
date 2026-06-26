package org.eximeebpms.bpm.extension.monitor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.eximeebpms.bpm.engine.ProcessEngine;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptViolationStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScriptViolationSnapshotMonitorTest {

  @Mock
  private ProcessEngine processEngine;
  @Mock
  private ProcessEngineConfigurationImpl engineConfiguration;
  @Mock
  private ScriptViolationStore violationStore;

  private SimpleMeterRegistry registry;

  @BeforeEach
  void setUp() {
    registry = new SimpleMeterRegistry();
    when(processEngine.getProcessEngineConfiguration()).thenReturn(engineConfiguration);
    when(engineConfiguration.getScriptViolationStore()).thenReturn(violationStore);
  }

  @Test
  void shouldRegisterGaugeWithCurrentTotalCount() {
    // given
    when(violationStore.getTotalCount()).thenReturn(42L);
    ScriptViolationSnapshotMonitor monitor = new ScriptViolationSnapshotMonitor(processEngine, registry);

    // when
    monitor.init();

    // then
    Gauge gauge = registry.find(Meters.SCRIPT_VIOLATIONS_TOTAL.getMeterName()).gauge();
    assertThat(gauge).isNotNull();
    assertThat(gauge.value()).isEqualTo(42.0);
  }

  @Test
  void shouldReturnZeroWhenStoreIsEmpty() {
    // given
    when(violationStore.getTotalCount()).thenReturn(0L);
    ScriptViolationSnapshotMonitor monitor = new ScriptViolationSnapshotMonitor(processEngine, registry);

    // when
    monitor.init();

    // then
    Gauge gauge = registry.find(Meters.SCRIPT_VIOLATIONS_TOTAL.getMeterName()).gauge();
    assertThat(gauge).isNotNull();
    assertThat(gauge.value()).isEqualTo(0.0);
  }

  @Test
  void shouldReflectUpdatedStoreTotalCountOnSubsequentScrapes() {
    // given — Gauge is pull-based; the supplier is called on each registry read
    when(violationStore.getTotalCount()).thenReturn(5L, 10L);
    ScriptViolationSnapshotMonitor monitor = new ScriptViolationSnapshotMonitor(processEngine, registry);
    monitor.init();

    Gauge gauge = registry.find(Meters.SCRIPT_VIOLATIONS_TOTAL.getMeterName()).gauge();
    assertThat(gauge).isNotNull();

    // when / then — first scrape
    assertThat(gauge.value()).isEqualTo(5.0);
    // second scrape reflects updated store
    assertThat(gauge.value()).isEqualTo(10.0);
  }

  @Test
  void shouldRegisterGaugeWithCorrectMeterName() {
    // given
    ScriptViolationSnapshotMonitor monitor = new ScriptViolationSnapshotMonitor(processEngine, registry);

    // when
    monitor.init();

    // then
    assertThat(registry.find(Meters.SCRIPT_VIOLATIONS_TOTAL.getMeterName()).gauge()).isNotNull();
  }
}
