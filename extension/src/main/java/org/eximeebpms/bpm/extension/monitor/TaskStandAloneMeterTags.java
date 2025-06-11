package org.eximeebpms.bpm.extension.monitor;

import io.micrometer.core.instrument.Tags;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaskStandAloneMeterTags {

    TENANT_ID("tenant.id"), TASK_NAME("task.name");

    private final String tagName;

    public static Tags createTags(String tenantId, String taskName) {
        Tags tags = Tags.empty();

        if (tenantId != null) {
            tags = tags.and(TaskStandAloneMeterTags.TENANT_ID.getTagName(), tenantId);
        }

        tags = tags.and(TaskStandAloneMeterTags.TASK_NAME.getTagName(), taskName);

        return tags;

    }

}