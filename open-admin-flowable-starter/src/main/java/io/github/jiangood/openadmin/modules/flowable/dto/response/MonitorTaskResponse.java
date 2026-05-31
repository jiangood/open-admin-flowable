package io.github.jiangood.openadmin.modules.flowable.dto.response;


import lombok.Data;

import java.util.Date;

/**
 * 流程监控 - 运行中的任务
 */
@Data
public class MonitorTaskResponse {

    private String id;
    private String name;
    private String taskDefinitionKey;
    private String processDefinitionId;
    private String processInstanceId;
    private String processInstanceName;

    private String assignee;
    private String assigneeLabel;
    private String executionId;
    private Date startTime;
    private String tenantId;


}
