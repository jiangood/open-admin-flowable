package io.github.jiangood.openadmin.modules.flowable.dto;

import org.flowable.engine.runtime.ProcessInstance;

import java.util.Date;
import java.util.Map;

public record ProcessInstanceResp(
        String id,
        String processDefinitionId,
        String processDefinitionName,
        String processDefinitionKey,
        int processDefinitionVersion,
        String processDefinitionCategory,
        String deploymentId,
        String businessKey,
        String businessStatus,
        boolean suspended,
        Map<String, Object> processVariables,
        String tenantId,
        String name,
        String description,
        String localizedName,
        String localizedDescription,
        Date startTime,
        String startUserId,
        String callbackId,
        String callbackType,
        String parentId,
        String rootProcessInstanceId,
        String superExecutionId,
        String activityId
) {
    public static ProcessInstanceResp from(ProcessInstance pi) {
        return new ProcessInstanceResp(
                pi.getId(), pi.getProcessDefinitionId(),
                pi.getProcessDefinitionName(), pi.getProcessDefinitionKey(),
                pi.getProcessDefinitionVersion(), pi.getProcessDefinitionCategory(),
                pi.getDeploymentId(), pi.getBusinessKey(), pi.getBusinessStatus(),
                pi.isSuspended(), pi.getProcessVariables(), pi.getTenantId(),
                pi.getName(), pi.getDescription(), pi.getLocalizedName(),
                pi.getLocalizedDescription(), pi.getStartTime(), pi.getStartUserId(),
                pi.getCallbackId(), pi.getCallbackType(), pi.getParentId(),
                pi.getRootProcessInstanceId(), pi.getSuperExecutionId(),
                pi.getActivityId()
        );
    }
}
