package org.eximeebpms.bpm.extension.monitor;

import io.micrometer.core.instrument.Tags;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ScriptViolationMeterTags {

  PROCESS_DEFINITION_KEY("process.definition.key"),
  ACTIVITY_ID("activity.id"),
  LANGUAGE("language"),
  ORIGIN("origin"),
  RULE_CODE("rule.code");

  private final String tagName;

  public static Tags createTags(String processDefinitionKey, String activityId,
      String language, String origin, String ruleCode) {
    Tags tags = Tags.empty();
    tags = addIfNotNull(tags, PROCESS_DEFINITION_KEY, processDefinitionKey);
    tags = addIfNotNull(tags, ACTIVITY_ID, activityId);
    tags = addIfNotNull(tags, LANGUAGE, language);
    tags = addIfNotNull(tags, ORIGIN, origin);
    tags = addIfNotNull(tags, RULE_CODE, ruleCode);
    return tags;
  }

  private static Tags addIfNotNull(Tags tags, ScriptViolationMeterTags tag, String value) {
    return value == null ? tags : tags.and(tag.getTagName(), value);
  }
}
