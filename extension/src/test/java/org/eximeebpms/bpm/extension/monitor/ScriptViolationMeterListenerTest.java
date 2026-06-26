package org.eximeebpms.bpm.extension.monitor;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Instant;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptOrigin;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSourceType;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptViolationEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScriptViolationMeterListenerTest {

  private SimpleMeterRegistry registry;
  private ScriptViolationMeterListener listener;

  @BeforeEach
  void setUp() {
    registry = new SimpleMeterRegistry();
    listener = new ScriptViolationMeterListener(registry);
  }

  @Test
  void shouldIncrementCounterOnViolation() {
    // given
    ScriptViolationEvent event = buildViolation("javascript", ScriptOrigin.USER, "RULE_CODE", "myProcess", "task1");

    // when
    listener.onViolation(event);

    // then
    Counter counter = registry.find(Meters.SCRIPT_VIOLATIONS.getMeterName())
        .tag(ScriptViolationMeterTags.LANGUAGE.getTagName(), "javascript")
        .tag(ScriptViolationMeterTags.ORIGIN.getTagName(), "USER")
        .tag(ScriptViolationMeterTags.RULE_CODE.getTagName(), "RULE_CODE")
        .tag(ScriptViolationMeterTags.PROCESS_DEFINITION_KEY.getTagName(), "myProcess")
        .tag(ScriptViolationMeterTags.ACTIVITY_ID.getTagName(), "task1")
        .counter();
    assertThat(counter).isNotNull();
    assertThat(counter.count()).isEqualTo(1.0);
  }

  @Test
  void shouldIncrementCounterMultipleTimesForRepeatedViolations() {
    // given
    ScriptViolationEvent event = buildViolation("groovy", ScriptOrigin.USER, "RULE_CODE", "proc", "task");

    // when
    listener.onViolation(event);
    listener.onViolation(event);
    listener.onViolation(event);

    // then
    Counter counter = registry.find(Meters.SCRIPT_VIOLATIONS.getMeterName())
        .tag(ScriptViolationMeterTags.LANGUAGE.getTagName(), "groovy")
        .counter();
    assertThat(counter).isNotNull();
    assertThat(counter.count()).isEqualTo(3.0);
  }

  @Test
  void shouldTrackSeparateCountersForDifferentLanguages() {
    // given
    ScriptViolationEvent jsEvent = buildViolation("javascript", ScriptOrigin.USER, "RULE", "proc", "task");
    ScriptViolationEvent groovyEvent = buildViolation("groovy", ScriptOrigin.USER, "RULE", "proc", "task");

    // when
    listener.onViolation(jsEvent);
    listener.onViolation(jsEvent);
    listener.onViolation(groovyEvent);

    // then
    Counter jsCounter = registry.find(Meters.SCRIPT_VIOLATIONS.getMeterName())
        .tag(ScriptViolationMeterTags.LANGUAGE.getTagName(), "javascript")
        .counter();
    Counter groovyCounter = registry.find(Meters.SCRIPT_VIOLATIONS.getMeterName())
        .tag(ScriptViolationMeterTags.LANGUAGE.getTagName(), "groovy")
        .counter();

    assertThat(jsCounter.count()).isEqualTo(2.0);
    assertThat(groovyCounter.count()).isEqualTo(1.0);
  }

  @Test
  void shouldOmitTagsForNullOptionalFields() {
    // given — violation with null processDefinitionKey, activityId, origin
    ScriptViolationEvent event = new ScriptViolationEvent(
        Instant.now(), null, null, null,
        "javascript", ScriptSourceType.INLINE_SOURCE, null,
        "RULE", "reason");

    // when
    listener.onViolation(event);

    // then — counter still registered (null tags are simply omitted by ScriptViolationMeterTags)
    Counter counter = registry.find(Meters.SCRIPT_VIOLATIONS.getMeterName())
        .tag(ScriptViolationMeterTags.LANGUAGE.getTagName(), "javascript")
        .tag(ScriptViolationMeterTags.RULE_CODE.getTagName(), "RULE")
        .counter();
    assertThat(counter).isNotNull();
    assertThat(counter.count()).isEqualTo(1.0);
  }

  @Test
  void shouldTrackSeparateCountersForDifferentRuleCodes() {
    // given
    ScriptViolationEvent rule1 = buildViolation("javascript", ScriptOrigin.USER, "RULE_ENV", "proc", "task");
    ScriptViolationEvent rule2 = buildViolation("javascript", ScriptOrigin.USER, "RULE_IO", "proc", "task");

    // when
    listener.onViolation(rule1);
    listener.onViolation(rule2);
    listener.onViolation(rule2);

    // then
    assertThat(registry.find(Meters.SCRIPT_VIOLATIONS.getMeterName())
        .tag(ScriptViolationMeterTags.RULE_CODE.getTagName(), "RULE_ENV")
        .counter().count()).isEqualTo(1.0);
    assertThat(registry.find(Meters.SCRIPT_VIOLATIONS.getMeterName())
        .tag(ScriptViolationMeterTags.RULE_CODE.getTagName(), "RULE_IO")
        .counter().count()).isEqualTo(2.0);
  }

  private ScriptViolationEvent buildViolation(String language, ScriptOrigin origin,
      String ruleCode, String processDefinitionKey, String activityId) {
    return new ScriptViolationEvent(
        Instant.now(), processDefinitionKey, null, activityId,
        language, ScriptSourceType.INLINE_SOURCE, origin,
        ruleCode, "test reason");
  }
}
