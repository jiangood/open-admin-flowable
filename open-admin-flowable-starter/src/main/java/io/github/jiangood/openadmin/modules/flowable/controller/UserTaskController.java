package io.github.jiangood.openadmin.modules.flowable.controller;

import io.github.jiangood.openadmin.framework.config.security.LoginUser;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.framework.auth.LoginTool;
import io.github.jiangood.openadmin.modules.flowable.dto.request.HandleTaskRequest;
import io.github.jiangood.openadmin.modules.flowable.dto.response.TaskResponse;
import io.github.jiangood.openadmin.modules.flowable.service.ProcessService;
import io.github.jiangood.openadmin.modules.flowable.service.UserTaskService;
import lombok.AllArgsConstructor;
import org.flowable.engine.history.HistoricProcessInstance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("admin/flowable/user-task")
@AllArgsConstructor
public class UserTaskController {

    private final UserTaskService userTaskService;
    private final ProcessService processService;

    @GetMapping("todoCount")
    public AjaxResult getTodoCount() {
        String userId = LoginTool.getUserId();
        long userTaskCount = userTaskService.findUserTaskCount(userId);
        return AjaxResult.ok().data(userTaskCount);
    }

    @RequestMapping("todoTaskPage")
    public AjaxResult queryTodoTaskPage(Pageable pageable) {
        String userId = LoginTool.getUserId();
        Page<TaskResponse> page = userTaskService.findUserTaskList(pageable, userId);
        return AjaxResult.ok().data(page);
    }

    @RequestMapping("doneTaskPage")
    public AjaxResult doneTaskPage(Pageable pageable) {
        String userId = LoginTool.getUserId();
        Page<TaskResponse> page = userTaskService.findUserTaskDoneList(pageable, userId);
        return AjaxResult.ok().data(page);
    }

    @GetMapping("myInstance")
    public AjaxResult myInstance(Pageable pageable) {
        LoginUser loginUser = LoginTool.getUser();
        Page<Map<String, Object>> page = userTaskService.queryMyInstance(pageable, loginUser);
        return AjaxResult.ok().data(page);
    }

    @PostMapping("handleTask")
    public AjaxResult handle(@RequestBody HandleTaskRequest param) {
        String user = LoginTool.getUserId();
        processService.handle(user, param.getResult(), param.getTaskId(), param.getComment(), param.getFormData());
        return AjaxResult.ok().msg("处理成功");
    }

    @GetMapping("getInstanceInfo")
    public AjaxResult instanceByBusinessKey(String businessKey, String id) {
        Assert.state(businessKey != null || id != null, "id或businessKey不能同时为空");
        String processInstanceId = id;
        if (processInstanceId == null) {
            HistoricProcessInstance instance = processService.getLatestProcessInstance(businessKey);
            processInstanceId = instance.getId();
        }
        Map<String, Object> data = userTaskService.queryInstanceInfo(processInstanceId);
        return AjaxResult.ok().data(data);
    }

    @GetMapping("getInstanceInfoByTaskId")
    public AjaxResult getInstanceInfoByTaskId(String taskId) {
        Map<String, Object> data = userTaskService.getInstanceInfoByTask(taskId);
        return AjaxResult.ok().data(data);
    }
}
