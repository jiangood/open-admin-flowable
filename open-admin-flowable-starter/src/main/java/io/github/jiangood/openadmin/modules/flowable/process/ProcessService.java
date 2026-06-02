package io.github.jiangood.openadmin.modules.flowable.process;


import cn.hutool.core.util.StrUtil;
import io.github.jiangood.openadmin.modules.flowable.config.FlowableProperties;
import io.github.jiangood.openadmin.modules.flowable.process.ProcessMeta;
import io.github.jiangood.openadmin.modules.flowable.common.dto.TaskHandleType;
import io.github.jiangood.openadmin.modules.flowable.service.BpmnDiagramService;
import io.github.jiangood.openadmin.modules.flowable.listener.ProcessListener;
import io.github.jiangood.openadmin.modules.flowable.process.ProcessMetaService;
import io.github.jiangood.openadmin.util.SpringTool;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.task.Comment;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class ProcessService {

    private final TaskService taskService;
    private final RuntimeService runtimeService;
    private final HistoryService historyService;
    private final BpmnDiagramService bpmnDiagramService;
    private final FlowableProperties flowableProperties;
    private final RepositoryService repositoryService;
    private final ProcessMetaService processMetaService;

    public HistoricProcessInstance getLatestProcessInstance(String bizKey) {
        List<HistoricProcessInstance> list = historyService.createHistoricProcessInstanceQuery()
                .processInstanceBusinessKey(bizKey)
                .notDeleted()
                .orderByProcessInstanceStartTime().desc()
                .list();
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    public void handle(String userId, TaskHandleType result, String taskId, String comment, Map<String, Object> formData) {
        Assert.notNull(userId, "用户Id不能为空");
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        Assert.state(task != null, "任务已经处理过，请勿重复操作");

        String processInstanceId = task.getProcessInstanceId();

        String assignee = task.getAssignee();
        if (StrUtil.isNotEmpty(assignee)) {
            Assert.state(assignee.equals(userId), "处理人不一致");
        }

        taskService.setAssignee(taskId, userId);

        if (result == TaskHandleType.APPROVE) {
            if (formData != null && !formData.isEmpty()) {
                ProcessListener listener = getBizListener(task);
                if (listener != null) {
                    HistoricProcessInstance instance = historyService.createHistoricProcessInstanceQuery()
                            .processInstanceId(processInstanceId).singleResult();
                    String initiator = instance.getStartUserId();
                    String businessKey = instance.getBusinessKey();
                    String formKey = task.getFormKey();
                    if (formKey == null) {
                        formKey = (String) task.getProcessVariables().get("GLOBAL_FORM_KEY");
                    }
                    listener.onFormSubmit(formKey, formData, initiator, userId, businessKey, processInstanceId, taskId, comment);
                }
            }
            comment = "【" + task.getName() + "】：" + result.getMessage() + "。" + comment;
            addComment(processInstanceId, taskId, userId, comment);
            taskService.complete(taskId);
            return;
        }

        if (result == TaskHandleType.REJECT) {
            comment = "【" + task.getName() + "】：" + result.getMessage() + "。" + comment;
            addComment(processInstanceId, taskId, userId, comment);
            switch (flowableProperties.getRejectType()) {
                case DELETE:
                    closeAndDelete(comment, task);
                    break;
                case MOVE_BACK:
                    this.moveBack(task);
                    break;
            }
        }
    }

    private void closeAndDelete(String comment, Task task) {
        runtimeService.deleteProcessInstance(task.getProcessInstanceId(), comment);
    }

    private void moveBack(Task task) {
        log.debug("开始回退任务 {}", task);
        List<UserTask> userTaskList = bpmnDiagramService.findPreActivity(task);
        for (UserTask userTask : userTaskList) {
            log.debug("回退任务 {}", userTask);
        }

        List<String> ids = userTaskList.stream().map(t -> t.getId()).collect(Collectors.toList());

        if (ids.isEmpty()) {
            this.closeAndDelete("回退节点为空，终止流程", task);
            return;
        }

        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(task.getProcessInstanceId())
                .moveSingleExecutionToActivityIds(task.getExecutionId(), ids)
                .changeState();
    }

    private ProcessListener getBizListener(Task task) {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(task.getProcessDefinitionId()).singleResult();
        if (processDefinition == null) {
            log.warn("未找到流程定义：{}", task.getProcessDefinitionId());
            return null;
        }
        ProcessMeta meta = processMetaService.findOne(processDefinition.getKey());
        if (meta != null && meta.getListener() != null) {
            return SpringTool.getBean(meta.getListener());
        }
        return null;
    }

    private void addComment(String processInstanceId, String taskId, String taskAssignee, String comment) {
        Comment commentEntity = taskService.addComment(taskId, processInstanceId, comment);
        commentEntity.setUserId(taskAssignee);
        taskService.saveComment(commentEntity);
    }
}
