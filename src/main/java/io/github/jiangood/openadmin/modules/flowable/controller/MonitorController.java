package io.github.jiangood.openadmin.modules.flowable.controller;

import io.github.jiangood.openadmin.framework.auth.LoginTool;
import io.github.jiangood.openadmin.framework.log.Log;
import io.github.jiangood.openadmin.modules.flowable.service.MonitorService;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Process monitoring controller: manage and query process definitions, instances, and tasks
 */
@RequestMapping("admin/flowable/monitor")
@RestController
@AllArgsConstructor
public class MonitorController {

    private final MonitorService monitorService;

    @GetMapping("definitionPage")
    public AjaxResult processDefinition(Pageable pageable) {
        return AjaxResult.ok().data(monitorService.findProcessDefinitionPage(pageable));
    }

    @GetMapping("instancePage")
    public AjaxResult instancePage(Pageable pageable) {
        return AjaxResult.ok().data(monitorService.findProcessInstancePage(pageable));
    }

    @Log("close process instance")
    @PreAuthorize("hasAuthority('flowableInstance:close')")
    @GetMapping("processInstance/close")
    public AjaxResult closeProcessInstance(String id) {
        String name = LoginTool.getUser().getName() + " manually closed";
        monitorService.closeProcessInstance(id, name);
        return AjaxResult.ok();
    }

    @GetMapping("instance/vars")
    public AjaxResult instanceVars(String id) {
        List<Map<String, Object>> list = monitorService.getInstanceVariables(id);
        return AjaxResult.ok().data(list);
    }

    @GetMapping("task")
    public AjaxResult task(String assignee, Pageable pageable) {
        return AjaxResult.ok().data(monitorService.findMonitorTasks(assignee, pageable));
    }

    @Log("set task assignee")
    @PreAuthorize("hasAuthority('flowableTask:setAssignee')")
    @RequestMapping("setAssignee")
    public AjaxResult setAssignee(@Valid @RequestBody SetAssigneeRequest request) {
        monitorService.setTaskAssignee(request.taskId(), request.assignee());
        return AjaxResult.ok().msg("set assignee success");
    }

    public record SetAssigneeRequest(
            @NotBlank String taskId,
            @NotBlank String assignee) {
    }
}