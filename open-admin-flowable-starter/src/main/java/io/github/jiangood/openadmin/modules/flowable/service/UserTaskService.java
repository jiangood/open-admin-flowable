package io.github.jiangood.openadmin.modules.flowable.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import io.github.jiangood.openadmin.framework.config.security.LoginUser;
import io.github.jiangood.openadmin.modules.flowable.common.utils.FlowablePageTool;
import io.github.jiangood.openadmin.modules.flowable.dto.CommentResp;
import io.github.jiangood.openadmin.modules.flowable.dto.TaskResp;
import io.github.jiangood.openadmin.modules.system.entity.SysRole;
import io.github.jiangood.openadmin.modules.system.entity.SysUser;
import io.github.jiangood.openadmin.modules.system.service.SysUserService;
import io.github.jiangood.openadmin.util.FriendlyTool;
import io.github.jiangood.openadmin.util.ImgTool;
import io.github.jiangood.openadmin.util.PageTool;
import lombok.AllArgsConstructor;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.task.Comment;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskInfo;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.history.HistoricTaskInstanceQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserTaskService {

    private final TaskService taskService;
    private final HistoryService historyService;
    private final RuntimeService runtimeService;
    private final SysUserService sysUserService;
    private final BpmnDiagramService bpmnDiagramService;

    // ========== User Task Query ==========

    public long findUserTaskCount(String userId) {
        TaskQuery taskQuery = buildUserTodoTaskQuery(userId);
        return taskQuery.count();
    }

    public Page<TaskResp> findUserTaskList(Pageable pageable, String userId) {
        TaskQuery query = buildUserTodoTaskQuery(userId);

        Page<Task> page = FlowablePageTool.queryPage(query, pageable);
        if (page.isEmpty()) {
            return Page.empty();
        }

        Set<String> instanceIds = page.stream().map(TaskInfo::getProcessInstanceId).collect(Collectors.toSet());
        Map<String, ProcessInstance> instanceMap = runtimeService.createProcessInstanceQuery()
                .processInstanceIds(instanceIds).list().stream()
                .collect(Collectors.toMap(Execution::getId, t -> t));

        return PageTool.convert(page, task -> {
            ProcessInstance instance = instanceMap.get(task.getProcessInstanceId());
            TaskResp r = new TaskResp();
            convert(r, task);
            r.setInstanceName(instance.getName());
            r.setInstanceStartTime(FriendlyTool.getPastTime(instance.getStartTime()));
            r.setInstanceStarter(sysUserService.getNameById(instance.getStartUserId()));
            return r;
        });
    }

    public Page<TaskResp> findUserTaskDoneList(Pageable pageable, String userId) {
        HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(userId)
                .finished()
                .includeProcessVariables()
                .orderByHistoricTaskInstanceEndTime().desc();

        Page<HistoricTaskInstance> page = FlowablePageTool.queryPage(query, pageable);
        if (page.isEmpty()) {
            return Page.empty();
        }

        Set<String> instanceIds = page.stream().map(TaskInfo::getProcessInstanceId).collect(Collectors.toSet());
        Map<String, HistoricProcessInstance> instanceMap = historyService.createHistoricProcessInstanceQuery()
                .processInstanceIds(instanceIds).list()
                .stream().collect(Collectors.toMap(HistoricProcessInstance::getId, t -> t));

        return PageTool.convert(page, task -> {
            HistoricProcessInstance instance = instanceMap.get(task.getProcessInstanceId());
            TaskResp r = new TaskResp();
            convert(r, task);
            r.setInstanceName(instance.getName());
            r.setInstanceStartTime(FriendlyTool.getPastTime(instance.getStartTime()));
            r.setInstanceStarter(sysUserService.getNameById(instance.getStartUserId()));
            r.setDurationInfo(FriendlyTool.getTimeDiff(task.getCreateTime(), task.getEndTime()));
            return r;
        });
    }

    public TaskQuery buildUserTodoTaskQuery(String userId) {
        TaskQuery query = taskService.createTaskQuery().active();

        query.or();
        query.taskAssignee(userId);
        query.taskCandidateUser(userId);

        SysUser user = sysUserService.findById(userId).orElse(null);
        Set<SysRole> roles = user.getRoles();
        if (CollUtil.isNotEmpty(roles)) {
            for (SysRole role : roles) {
                query.taskCandidateGroup(role.getId());
            }
        }
        query.endOr();

        query.orderByTaskCreateTime().desc();

        return query;
    }

    private void convert(TaskResp r, TaskInfo task) {
        r.setId(task.getId());
        r.setTaskName(task.getName());
        r.setCreateTime(FriendlyTool.getPastTime(task.getCreateTime()));
        r.setAssigneeInfo(sysUserService.getNameById(task.getAssignee()));
        r.setFormKey(task.getFormKey());
        r.setInstanceId(task.getProcessInstanceId());
    }

    // ========== Utility ==========

    public String getUserName(String userId) {
        if (userId == null) {
            return null;
        }
        return sysUserService.getNameById(userId);
    }

    public BufferedImage drawImage(String instanceId) {
        return bpmnDiagramService.drawImage(instanceId);
    }

    // ========== Instance Query ==========

    public Page<Map<String, Object>> queryMyInstance(Pageable pageable, LoginUser loginUser) {
        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();
        query.startedBy(loginUser.getId());
        query.orderByProcessInstanceStartTime().desc();
        query.includeProcessVariables();

        Page<HistoricProcessInstance> page = FlowablePageTool.queryPage(query, pageable);
        return PageTool.convert(page, instance -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", instance.getId());
            map.put("name", instance.getName());
            map.put("processDefinitionName", instance.getProcessDefinitionName());
            map.put("startTime", instance.getStartTime());
            map.put("endTime", instance.getEndTime());
            map.put("businessKey", instance.getBusinessKey());
            map.put("deleteReason", instance.getDeleteReason());
            String startUserId = instance.getStartUserId();
            if (startUserId != null) {
                map.put("startUserName", getUserName(startUserId));
            }
            return map;
        });
    }

    public Map<String, Object> queryInstanceInfo(String processInstanceId) {
        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();
        query.processInstanceId(processInstanceId);
        query.notDeleted();
        query.includeProcessVariables()
                .orderByProcessInstanceStartTime()
                .desc();

        List<HistoricProcessInstance> list = query.listPage(0, 1);
        Assert.state(!list.isEmpty(), "暂无流程信息");
        HistoricProcessInstance instance = list.get(0);

        Map<String, Object> data = new HashMap<>();

        List<Comment> processInstanceComments = taskService.getProcessInstanceComments(processInstanceId);
        List<CommentResp> commentList = processInstanceComments.stream()
                .sorted(Comparator.comparing(Comment::getTime))
                .map(c -> new CommentResp(c, getUserName(c.getUserId())))
                .toList();
        data.put("commentList", commentList);
        data.put("instanceCommentList", commentList);

        try {
            BufferedImage image = drawImage(instance.getId());
            String base64 = ImgTool.toBase64DataUri(image);
            data.put("img", base64);
        } catch (Exception e) {
            data.put("img", null);
        }

        String instanceName = instance.getName();
        if (instanceName == null) {
            instanceName = instance.getProcessDefinitionName();
        }
        data.put("startTime", DateUtil.format(instance.getStartTime(), "yyyy-MM-dd HH:mm:ss"));
        data.put("starter", getUserName(instance.getStartUserId()));
        data.put("name", instanceName);
        data.put("id", instance.getId());
        data.put("processDefinitionKey", instance.getProcessDefinitionKey());
        data.put("businessKey", instance.getBusinessKey());

        return data;
    }

    public Map<String, Object> getInstanceInfoByTask(String taskId) {
        Assert.notNull(taskId, "taskId不能为空");

        Task task = taskService.createTaskQuery().taskId(taskId).includeProcessVariables().singleResult();
        String processInstanceId = task.getProcessInstanceId();

        Map<String, Object> data = queryInstanceInfo(processInstanceId);

        String formKey = task.getFormKey();
        if (formKey == null) {
            formKey = (String) task.getProcessVariables().get("GLOBAL_FORM_KEY");
        }

        if (formKey == null) {
            HistoricProcessInstance instance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(processInstanceId).singleResult();
            formKey = instance.getProcessDefinitionKey();
        }

        data.put("formKey", formKey);
        data.put("taskId", taskId);

        return data;
    }
}
