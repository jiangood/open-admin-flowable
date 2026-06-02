package io.github.jiangood.openadmin.modules.flowable.monitor;

import jakarta.validation.constraints.NotBlank;

public record SetAssigneeRequest(
        @NotBlank String taskId,
        @NotBlank String assignee) {
}
