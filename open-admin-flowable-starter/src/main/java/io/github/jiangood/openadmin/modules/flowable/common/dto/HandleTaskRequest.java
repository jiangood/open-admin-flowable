package io.github.jiangood.openadmin.modules.flowable.common.dto;

import lombok.Data;

import java.util.Map;

@Data
public class HandleTaskRequest {

    TaskHandleType result;
    String taskId;
    String comment;
    Map<String, Object> formData;
}
