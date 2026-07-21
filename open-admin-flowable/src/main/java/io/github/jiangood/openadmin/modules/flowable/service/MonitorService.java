package io.github.jiangood.openadmin.modules.flowable.service;

import cn.hutool.core.util.StrUtil;
import io.github.jiangood.openadmin.modules.flowable.common.utils.FlowablePageTool;
import io.github.jiangood.openadmin.modules.flowable.constant.FlowableConstants;
import io.github.jiangood.openadmin.modules.flowable.dto.MonitorTaskResp;
import io.github.jiangood.openadmin.modules.flowable.dto.ProcessDefinitionResp;
import io.github.jiangood.openadmin.modules.flowable.dto.ProcessInstanceResp;
import io.github.jiangood.openadmin.modules.system.service.SysUserService;
import lombok.AllArgsConstructor;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceQuery;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskInfo;
import org.flowable.task.api.TaskQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Process monitoring service: management and query of process definitions, instances, and tasks
 */
@Service
@AllArgsConstructor
public class MonitorService {

    private final RepositoryService repositoryService;
    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final HistoryService historyService;
    private final SysUserService sysUserService;
    private final UserTaskService userTaskService;

    public Page<ProcessDefinitionResp> findProcessDefinitionPage(Pageable pageable) {
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();
        Page<ProcessDefinition> page = FlowablePageTool.queryPage(query, pageable);
        return page.map(ProcessDefinitionResp::from);
    }

    public Page<ProcessInstanceResp> findProcessInstancePage(Pageable pageable) {
        ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
        Page<ProcessInstance> page = FlowablePageTool.queryPage(query, pageable);
        return page.map(ProcessInstanceResp::from);
    }

    public void closeProcessInstance(String id, String reason) {
        Assert.notNull(id, "id cannot be null");
        runtimeService.deleteProcessInstance(id, reason);
    }

    public List<Map<String, Object>> getInstanceVariables(String id) {
        HistoricProcessInstance instance = historyService.createHistoricProcessInstanceQuery()
                .includeProcessVariables()
                .processInstanceId(id)
                .singleResult();
        Map<String, Object> processVariables = instance.getProcessVariables();
        List<Map<String, Object>> list = new ArrayList<>();
        processVariables.forEach((k, v) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("key", k);
            item.put("value", v);
            list.add(item);
        });
        return list;
    }

    public Page<MonitorTaskResp> findMonitorTasks(String assignee, Pageable pageable) {
        TaskQuery query = taskService.createTaskQuery();
        if (StrUtil.isNotEmpty(assignee)) {
            query = userTaskService.buildUserTodoTaskQuery(assignee);
        }
        query.orderByTaskCreateTime().desc();
        Page<Task> taskPage = FlowablePageTool.queryPage(query, pageable);
        List<Task> list = taskPage.getContent();

        Set<String> instanceIds = list.stream().map(TaskInfo::getProcessInstanceId).collect(Collectors.toSet());
        Map<String, String> idName;
        if (!instanceIds.isEmpty()) {
            List<ProcessInstance> processInstanceList = runtimeService.createProcessInstanceQuery()
                    .active().processInstanceIds(instanceIds).list();
            idName = processInstanceList.stream()
                    .collect(Collectors.toMap(Execution::getId, ProcessInstance::getName));
        } else {
            idName = new HashMap<>();
        }

        List<MonitorTaskResp> responseList = list.stream().map(t -> {
            MonitorTaskResp r = new MonitorTaskResp();
            r.setId(t.getId());
            r.setName(t.getName());
            r.setTaskDefinitionKey(t.getTaskDefinitionKey());
            r.setProcessDefinitionId(t.getProcessDefinitionId());
            r.setProcessInstanceId(t.getProcessInstanceId());
            r.setProcessInstanceName(idName.get(t.getProcessInstanceId()));
            r.setAssignee(t.getAssignee());
            r.setAssigneeLabel(sysUserService.getNameById(t.getAssignee()));
            r.setExecutionId(t.getExecutionId());
            r.setStartTime(t.getCreateTime());
            r.setTenantId(t.getTenantId());
            return r;
        }).toList();

        return new PageImpl<>(responseList, pageable, taskPage.getTotalElements());
    }

    public void setTaskAssignee(String taskId, String assignee) {
        Assert.notNull(taskId, "taskId cannot be null");
        Assert.notNull(assignee, "assignee cannot be null");
        taskService.setAssignee(taskId, assignee);
    }
}
