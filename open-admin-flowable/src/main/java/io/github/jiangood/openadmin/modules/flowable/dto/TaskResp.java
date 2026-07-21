package io.github.jiangood.openadmin.modules.flowable.dto;

import lombok.Data;


@Data
public class TaskResp {
    String id;

    String instanceId;
    String instanceName;
    String instanceStartTime;
    String instanceStarter;


    String createTime;
    String taskName;
    String assigneeInfo;
    String durationInfo;

    String formKey;


}
