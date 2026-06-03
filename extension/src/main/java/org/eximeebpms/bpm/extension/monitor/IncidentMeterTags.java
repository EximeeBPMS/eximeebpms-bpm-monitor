package org.eximeebpms.bpm.extension.monitor;

import io.micrometer.core.instrument.Tags;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum IncidentMeterTags {

  TENANT_ID("tenant.id"), PROCESS_DEFINITION_ID("process.definition.id"),
  PROCESS_DEFINITION_KEY("process.definition.key"), ACTIVITY_ID("activity.id"),
  FAILED_ACTIVITY_ID("failed.activity.id"), INCIDENT_TYPE("incident.type");

  private final String tagName;

  public static Tags createTags(String tenantId, String processDefinitionId, String processDefinitionKey,
      String activityId, String failedActivityId, String incidentType) {
    Tags tags = Tags.empty();

    if (tenantId != null) {
      tags = tags.and(IncidentMeterTags.TENANT_ID.getTagName(), tenantId);
    }

    tags = addIfNotNull(tags, IncidentMeterTags.PROCESS_DEFINITION_ID, processDefinitionId);
    tags = addIfNotNull(tags, IncidentMeterTags.PROCESS_DEFINITION_KEY, processDefinitionKey);
    tags = addIfNotNull(tags, IncidentMeterTags.ACTIVITY_ID, activityId);
    if (failedActivityId != null && !Objects.equals(activityId, failedActivityId)) {
      tags = tags.and(IncidentMeterTags.FAILED_ACTIVITY_ID.getTagName(), failedActivityId);
    }
    tags = addIfNotNull(tags, IncidentMeterTags.INCIDENT_TYPE, incidentType);

    return tags;

  }

  private static Tags addIfNotNull(Tags tags, IncidentMeterTags tag, String value) {
    return value == null ? tags : tags.and(tag.getTagName(), value);
  }

}
