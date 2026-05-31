package io.github.jiangood.openadmin.modules.flowable.controller;


import io.github.jiangood.openadmin.framework.config.security.LoginUser;
import io.github.jiangood.openadmin.util.ImgTool;
import io.github.jiangood.openadmin.util.PageTool;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.framework.auth.LoginTool;
import io.github.jiangood.openadmin.modules.flowable.dto.request.HandleTaskRequest;
import io.github.jiangood.openadmin.modules.flowable.dto.response.CommentResponse;
import io.github.jiangood.openadmin.modules.flowable.dto.response.TaskResponse;
import io.github.jiangood.openadmin.modules.flowable.service.ProcessService;
import io.github.jiangood.openadmin.modules.flowable.utils.FlowablePageTool;
import lombok.AllArgsConstructor;
import org.flowable.engine.HistoryService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.task.Comment;
import org.flowable.task.api.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 用户侧功能，待办，处理，查看流程等
 * 每个人都可以看自己任务，故而没有权限注解
 */
@RestController
@RequestMapping("admin/flowable/user-task")
@AllArgsConstructor
public class UserTaskController {

    private TaskService taskService;
    private HistoryService historyService;
    private ProcessService processService;

    @GetMapping("todoCount")
    public AjaxResult getTodoCount() {
        String userId = LoginTool.getUserId();
        long userTaskCount = processService.findUserTaskCount(userId);
        return AjaxResult.ok().data(userTaskCount);
    }

    @RequestMapping("todoTaskPage")
    public AjaxResult queryTodoTaskPage(Pageable pageable) {
        String userId = LoginTool.getUserId();
        Page<TaskResponse> page = processService.findUserTaskList(pageable, userId);

        return AjaxResult.ok().data(page);
    }

    @RequestMapping("doneTaskPage")
    public AjaxResult doneTaskPage(Pageable pageable) {
        String userId = LoginTool.getUserId();

        Page<TaskResponse> page = processService.findUserTaskDoneList(pageable, userId);
        return AjaxResult.ok().data(page);
    }


    // 我发起的
    @GetMapping("myInstance")
    public AjaxResult myInstance(Pageable pageable) {
        LoginUser loginUser = LoginTool.getUser();


        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();
        query.startedBy(loginUser.getId());


        query.orderByProcessInstanceStartTime().desc();
        query.includeProcessVariables();

        Page<HistoricProcessInstance> page = FlowablePageTool.queryPage(query, pageable);
        Page<Map<String, Object>> page2 = PageTool.convert(page, instance -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", instance.getId());
            map.put("processDefinitionName", instance.getProcessDefinitionName());
            map.put("startTime", instance.getStartTime());
            map.put("endTime", instance.getEndTime());
            map.put("businessKey", instance.getBusinessKey());
            map.put("deleteReason", instance.getDeleteReason());
            String startUserId = instance.getStartUserId();
            if (startUserId != null) {
                map.put("startUserName", processService.getUserName(startUserId));
            }
            return map;
        });

        return AjaxResult.ok().data(page2);
    }


    @PostMapping("handleTask")
    public AjaxResult handle(@RequestBody HandleTaskRequest param) {
        String user = LoginTool.getUserId();
        processService.handle(user, param.getResult(), param.getTaskId(), param.getComment());
        return AjaxResult.ok().msg("处理成功");
    }


    /**
     * 流程处理信息
     *
     * @return 处理流程及流程图
     */
    @GetMapping("getInstanceInfo")
    public AjaxResult instanceByBusinessKey(String businessKey, String id) throws IOException {
        Assert.state(businessKey != null || id != null, "id或businessKey不能同时为空");

        if (id == null) {
            HistoricProcessInstance instance = processService.getLatestProcessInstance(businessKey);
            id = instance.getId();
        }
        Map<String, Object> data = queryInstanceInfo(id);

        return AjaxResult.ok().data(data);
    }

    /**
     * 流程处理信息
     *
     * @return 处理流程及流程图
     */
    @GetMapping("getInstanceInfoByTaskId")
    public AjaxResult getInstanceInfoByTaskId(String taskId) throws IOException {
        Assert.notNull(taskId, "taskId不能为空");

        Task task = taskService.createTaskQuery().taskId(taskId).includeProcessVariables().singleResult();
        String processInstanceId = task.getProcessInstanceId();

        Map<String, Object> data = queryInstanceInfo(processInstanceId);

        String formKey = task.getFormKey();
        if (formKey == null) {
            formKey = (String) task.getProcessVariables().get("GLOBAL_FORM_KEY");
        }

        // 兼容性代码 TODO 老系统运行几个月后可移除
        if (formKey == null) {
            HistoricProcessInstance instance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
            formKey = instance.getProcessDefinitionKey();
        }

        // 增加表单key
        data.put("formKey", formKey);
        data.put("taskId", taskId);


        return AjaxResult.ok().data(data);
    }

    private Map<String, Object> queryInstanceInfo(String processInstanceId) throws IOException {
        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();
        query.processInstanceId(processInstanceId);
        query.notDeleted();
        query.includeProcessVariables()
                .orderByProcessInstanceStartTime()
                .desc();

        List<HistoricProcessInstance> list = query
                .listPage(0, 1);
        Assert.state(!list.isEmpty(), "暂无流程信息");
        HistoricProcessInstance instance = list.get(0);


        Map<String, Object> data = new HashMap<>();

        // 处理意见
        List<Comment> processInstanceComments = taskService.getProcessInstanceComments(processInstanceId);
        List<CommentResponse> commentList = processInstanceComments.stream()
                .sorted(Comparator.comparing(Comment::getTime))
                .map(c -> new CommentResponse(c, processService.getUserName(c.getUserId())))
                .toList();
        data.put("commentList", commentList);
        data.put("instanceCommentList", commentList);

        // 图片
        {
            BufferedImage image = processService.drawImage(instance.getId());
            String base64 = ImgTool.toBase64DataUri(image);
            data.put("img", base64);
        }

        {
            String instanceName = instance.getName();
            if (instanceName == null) {
                instanceName = instance.getProcessDefinitionName();
            }
            data.put("startTime", cn.hutool.core.date.DateUtil.format(instance.getStartTime(), "yyyy-MM-dd HH:mm:ss"));
            data.put("starter", processService.getUserName(instance.getStartUserId()));
            data.put("name", instanceName);
            data.put("id", instance.getId());

            data.put("processDefinitionKey", instance.getProcessDefinitionKey());
            data.put("businessKey", instance.getBusinessKey());
        }
        return data;
    }

}
