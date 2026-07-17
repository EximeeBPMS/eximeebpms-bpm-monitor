package org.eximeebpms.bpm.extension.monitor;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptViolationEvent;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptViolationListener;
import org.springframework.stereotype.Component;

/**
 * Increments the {@code eximeebpms.script.violations} counter on every script violation.
 * Registers as a Spring bean so that {@code DefaultProcessEngineConfiguration} picks it up
 * automatically via {@code ApplicationContext.getBeansOfType(ScriptViolationListener.class)}.
 *
 * <p>Tags: {@code process.definition.key}, {@code activity.id}, {@code language},
 * {@code origin}, {@code rule.code}.
 */
@Component
public class ScriptViolationMeterListener implements ScriptViolationListener {

  private final TaggedCounter violationsCounter;

  public ScriptViolationMeterListener(MeterRegistry meterRegistry) {
    this.violationsCounter = new TaggedCounter(Meters.SCRIPT_VIOLATIONS.getMeterName(), meterRegistry);
  }

  @Override
  public void onViolation(ScriptViolationEvent event) {
    Tags tags = ScriptViolationMeterTags.createTags(
        event.processDefinitionKey(),
        event.activityId(),
        event.language(),
        event.origin() != null ? event.origin().name() : null,
        event.ruleCode());
    violationsCounter.increment(tags);
  }
}
