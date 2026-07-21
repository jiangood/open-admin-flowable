package io.github.jiangood.openadmin.modules.flowable.dto;

import lombok.Data;

import java.util.Map;

@Data
public class HandleTaskReq {

    TaskHandleType result;
    String taskId;
    String comment;
    Map<String, Object> formData;
}
