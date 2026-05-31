package io.github.jiangood.openadmin.modules.flowable.controller;


import cn.hutool.core.util.StrUtil;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.util.PageTool;
import io.github.jiangood.openadmin.framework.log.Log;
import io.github.jiangood.openadmin.framework.auth.LoginTool;
import io.github.jiangood.openadmin.modules.flowable.dto.response.MonitorTaskResponse;
import io.github.jiangood.openadmin.modules.flowable.dto.vo.ProcessDefinitionVO;
import io.github.jiangood.openadmin.modules.flowable.dto.vo.ProcessInstanceVO;
import io.github.jiangood.openadmin.modules.flowable.service.ProcessService;
import io.github.jiangood.openadmin.modules.flowable.utils.FlowablePageTool;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 流程监控
 */
@RequestMapping("admin/flowable/monitor")
@RestController
@AllArgsConstructor
public class MonitorController {

    private RepositoryService repositoryService;
    private RuntimeService runtimeService;
    private TaskService taskService;
    private SysUserService sysUserService;
    private ProcessService processService;
    private HistoryService historyService;

    @GetMapping("definitionPage")
    public AjaxResult processDefinition(Pageable pageable) {
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

        Page<ProcessDefinition> page = FlowablePageTool.queryPage(query, pageable);

        Page<ProcessDefinitionVO> page2 = PageTool.convert(page, ProcessDefinitionVO::from);


        return AjaxResult.ok().data(page2);
    }

    @GetMapping("instancePage")
    public AjaxResult instancePage(Pageable pageable) {
        ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
        Page<ProcessInstance> page = FlowablePageTool.queryPage(query, pageable);

        Page<ProcessInstanceVO> page2 = PageTool.convert(page, ProcessInstanceVO::from);

        return AjaxResult.ok().data(page2);
    }

    @Log("关闭流程实例")
    @PreAuthorize("hasAuthority('flowableInstance:close')")
    @GetMapping("processInstance/close")
    public AjaxResult processInstanceClose(String id) {
        String name = LoginTool.getUser().getName();
        runtimeService.deleteProcessInstance(id, name + "手动关闭");

        return AjaxResult.ok();
    }

    @GetMapping("instance/vars")
    public AjaxResult instanceVars(String id) {
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


        return AjaxResult.ok().data(new PageImpl<>(list));
    }


    @GetMapping("task")
    public AjaxResult task(String assignee, Pageable pageable) {
        TaskQuery query = taskService.createTaskQuery();

        if (StrUtil.isNotEmpty(assignee)) {
            query = processService.buildUserTodoTaskQuery(assignee);
        }
        query.orderByTaskCreateTime().desc();
        Page<Task> taskPage = FlowablePageTool.queryPage(query, pageable);
        List<Task> list = taskPage.getContent();

        Set<String> instanceIds = list.stream().map(TaskInfo::getProcessInstanceId).collect(Collectors.toSet());
        List<ProcessInstance> processInstanceList = runtimeService.createProcessInstanceQuery().active().processInstanceIds(instanceIds).list();
        Map<String, String> idName = processInstanceList.stream().collect(Collectors.toMap(Execution::getId, ProcessInstance::getName));


        List<MonitorTaskResponse> responseList = list.stream().map(t -> {
            MonitorTaskResponse r = new MonitorTaskResponse();
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


        return AjaxResult.ok().data(new PageImpl<>(responseList, pageable, taskPage.getTotalElements()));
    }

    @Log("设置任务处理人")
    @PreAuthorize("hasAuthority('flowableTask:setAssignee')")
    @RequestMapping("setAssignee")
    public AjaxResult setAssignee(@RequestBody SetAssigneeRequest request) {
        taskService.setAssignee(request.taskId(), request.assignee());
        return AjaxResult.ok().msg("设置任务处理人成功");
    }

    public record SetAssigneeRequest(String taskId, String assignee) {
    }


}
