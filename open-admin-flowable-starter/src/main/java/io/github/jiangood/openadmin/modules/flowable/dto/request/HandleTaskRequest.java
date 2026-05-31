package io.github.jiangood.openadmin.modules.flowable.dto.request;

import io.github.jiangood.openadmin.modules.flowable.dto.TaskHandleType;
import lombok.Data;

import java.util.Map;

@Data
public class HandleTaskRequest {

    TaskHandleType result;
    String taskId;
    String comment;
    Map<String, Object> formData;
}
