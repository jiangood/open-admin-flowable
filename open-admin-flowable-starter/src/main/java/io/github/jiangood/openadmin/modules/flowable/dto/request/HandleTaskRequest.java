package io.github.jiangood.openadmin.modules.flowable.dto.request;

import io.github.jiangood.openadmin.modules.flowable.dto.TaskHandleType;
import lombok.Data;

@Data
public class HandleTaskRequest {

    TaskHandleType result;
    String taskId;
    String comment;
}
