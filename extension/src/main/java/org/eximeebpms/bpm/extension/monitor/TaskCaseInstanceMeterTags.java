package org.eximeebpms.bpm.extension.monitor;

import io.micrometer.core.instrument.Tags;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaskCaseInstanceMeterTags {

    TENANT_ID("tenant.id"), CASE_DEFINITION_ID("case.definition.id"), TASK_DEFINITION_KEY("task.definition.key");

    private final String tagName;

    public static Tags createTags(String tenantId, String caseDefinitionIdString, String taskDefinitionKey) {
        Tags tags = Tags.empty();

        if (tenantId != null) {
            tags = tags.and(TaskCaseInstanceMeterTags.TENANT_ID.getTagName(), tenantId);
        }

        tags = tags.and(TaskCaseInstanceMeterTags.CASE_DEFINITION_ID.getTagName(), caseDefinitionIdString);
        tags = tags.and(TaskCaseInstanceMeterTags.TASK_DEFINITION_KEY.getTagName(), taskDefinitionKey);

        return tags;

    }

}