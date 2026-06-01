package io.github.jiangood.openadmin.modules.flowable.service;

import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.modules.flowable.constant.FlowableConstants;
import io.github.jiangood.openadmin.modules.flowable.domain.ProcessMeta;
import io.github.jiangood.openadmin.util.ImgTool;
import io.github.jiangood.openadmin.modules.system.entity.SysUser;
import io.github.jiangood.openadmin.modules.system.service.SysUserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.HistoryService;
import org.flowable.engine.IdentityService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.task.Comment;
import org.flowable.task.api.Task;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

/**
 * 流程仿真服务 — 与实际业务隔离（通过 _process_simulation 变量标记）
 * 不走 FlowableTemplate，不触发 ProcessListener，不校验 assignee
 */
@Slf4j
@Service
@AllArgsConstructor
public class SimulateService {

    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final HistoryService historyService;
    private final RepositoryService repositoryService;
    private final IdentityService identityService;
    private final SysUserService sysUserService;
    private final BpmnDiagramService bpmnDiagramService;
    private final ProcessMetaService processMetaService;

    /**
     * 获取流程模型元数据
     */
    public ProcessMeta getModelMeta(String modelId) {
        Assert.hasText(modelId, "id不能为空");
        org.flowable.engine.repository.Model model = repositoryService.getModel(modelId);
        Assert.notNull(model, "流程模型不存在");
        return processMetaService.findOne(model.getKey());
    }

    /**
     * 启动仿真流程
     *
     * @param key         流程定义 key
     * @param bizKey      业务标识
     * @param initiatorId 模拟发起人 userId
     * @param variables   自定义流程变量
     * @return 流程实例 ID
     */
    public String startSimulation(String key, String bizKey, String initiatorId, Map<String, Object> variables) {
        Assert.hasText(key, "流程编码不能为空");
        Assert.hasText(initiatorId, "发起人不能为空");

        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(key).active().latestVersion().singleResult();
        Assert.notNull(definition, "流程尚未部署，请设计后部署。编码：" + key);

        SysUser initiator = sysUserService.findById(initiatorId)
                .orElseThrow(() -> new IllegalArgumentException("发起人不存在：" + initiatorId));

        if (variables == null) {
            variables = new HashMap<>();
        }

        // 注入仿真标记 + 发起人用户变量
        injectSimulationVariables(variables, initiator, bizKey);

        // 设置发起人身份（GlobalProcessListener 依赖 startUserId）
        identityService.setAuthenticatedUserId(initiatorId);

        ProcessInstance instance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionId(definition.getId())
                .businessKey(bizKey)
                .variables(variables)
                .name("【仿真】" + definition.getName())
                .start();

        log.info("仿真流程已启动: instanceId={}, key={}, initiator={}", instance.getId(), key, initiatorId);
        return instance.getId();
    }

    /**
     * 获取仿真状态
     */
    public Map<String, Object> getStatus(String instanceId) {
        Assert.hasText(instanceId, "instanceId 不能为空");

        Map<String, Object> data = new HashMap<>();

        // 判断流程是否结束
        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(instanceId)
                .singleResult();

        boolean finished = instance == null;
        data.put("finished", finished);

        // 基本信息
        HistoricProcessInstance historicInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(instanceId)
                .includeProcessVariables()
                .singleResult();
        Assert.notNull(historicInstance, "流程实例不存在");

        data.put("name", historicInstance.getName());
        data.put("startTime", cn.hutool.core.date.DateUtil.format(historicInstance.getStartTime(), "yyyy-MM-dd HH:mm:ss"));
        data.put("starter", sysUserService.getNameById(historicInstance.getStartUserId()));
        data.put("businessKey", historicInstance.getBusinessKey());
        data.put("processDefinitionKey", historicInstance.getProcessDefinitionKey());
        data.put("deleteReason", historicInstance.getDeleteReason());

        // 活跃任务
        if (!finished) {
            List<Task> tasks = taskService.createTaskQuery()
                    .processInstanceId(instanceId)
                    .active()
                    .orderByTaskCreateTime().asc()
                    .list();
            List<Map<String, Object>> taskList = tasks.stream().map(t -> {
                Map<String, Object> tm = new HashMap<>();
                tm.put("taskId", t.getId());
                tm.put("taskName", t.getName());
                tm.put("assignee", t.getAssignee());
                tm.put("assigneeName", sysUserService.getNameById(t.getAssignee()));
                tm.put("createTime", cn.hutool.core.date.DateUtil.format(t.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
                tm.put("formKey", t.getFormKey());
                return tm;
            }).toList();
            data.put("tasks", taskList);
        } else {
            data.put("tasks", Collections.emptyList());
        }

        // 处理记录
        List<Comment> comments = taskService.getProcessInstanceComments(instanceId);
        List<Map<String, Object>> commentList = comments.stream()
                .sorted(Comparator.comparing(Comment::getTime))
                .map(c -> {
                    Map<String, Object> cm = new HashMap<>();
                    cm.put("content", c.getFullMessage());
                    cm.put("user", sysUserService.getNameById(c.getUserId()));
                    cm.put("time", cn.hutool.core.date.DateUtil.format(c.getTime(), "yyyy-MM-dd HH:mm:ss"));
                    return cm;
                })
                .toList();
        data.put("commentList", commentList);

        // 高亮流程图
        BufferedImage image = bpmnDiagramService.drawImage(instanceId);
        try {
            String base64 = ImgTool.toBase64DataUri(image);
            data.put("img", base64);
        } catch (IOException e) {
            log.warn("生成仿真流程图失败", e);
            data.put("img", null);
        }

        return data;
    }

    /**
     * 处理仿真任务
     */
    public void handleTask(String taskId, String action, String comment, String handleUserId) {
        Assert.hasText(taskId, "taskId 不能为空");
        Assert.hasText(action, "action 不能为空");
        Assert.hasText(handleUserId, "handleUserId 不能为空");

        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        Assert.state(task != null, "任务已处理，请勿重复操作");

        String processInstanceId = task.getProcessInstanceId();

        // 仿真模式：不校验 assignee，直接设置处理人
        taskService.setAssignee(taskId, handleUserId);

        String commentText = "【仿真-" + task.getName() + "】：" +
                ("APPROVE".equals(action) ? "同意" : "不同意") +
                (comment != null ? "。" + comment : "");
        addComment(processInstanceId, taskId, handleUserId, commentText);

        if ("APPROVE".equals(action)) {
            taskService.complete(taskId);
            log.info("仿真任务已处理: taskId={}, action=APPROVE, handleUserId={}", taskId, handleUserId);
        } else if ("REJECT".equals(action)) {
            runtimeService.deleteProcessInstance(processInstanceId, commentText);
            log.info("仿真流程已终止: instanceId={}, handleUserId={}", processInstanceId, handleUserId);
        } else {
            throw new IllegalArgumentException("不支持的操作：" + action);
        }
    }

    /**
     * 查询可选用户列表（发起人/处理人）
     */
    public List<Map<String, Object>> listUsers(String searchText) {
        List<SysUser> users;
        if (searchText != null && !searchText.isEmpty()) {
            users = sysUserService.findAll(
                    Spec.<SysUser>of().orLike(searchText, "name", "account", "phone"),
                    Sort.by("name"));
        } else {
            users = sysUserService.findAll(Sort.by("name"));
        }
        return users.stream().map(u -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("name", u.getName());
            return m;
        }).toList();
    }

    /**
     * 查询仿真历史列表
     */
    public List<Map<String, Object>> listHistory(String processDefinitionKey) {
        Assert.hasText(processDefinitionKey, "流程编码不能为空");

        List<HistoricProcessInstance> list = historyService.createHistoricProcessInstanceQuery()
                .processDefinitionKey(processDefinitionKey)
                .variableValueEquals(FlowableConstants.VAR_SIMULATION, "true")
                .orderByProcessInstanceStartTime().desc()
                .list();

        return list.stream().map(instance -> {
            Map<String, Object> m = new HashMap<>();
            m.put("instanceId", instance.getId());
            m.put("name", instance.getName());
            m.put("businessKey", instance.getBusinessKey());
            m.put("starter", sysUserService.getNameById(instance.getStartUserId()));
            m.put("startTime", cn.hutool.core.date.DateUtil.format(instance.getStartTime(), "yyyy-MM-dd HH:mm:ss"));
            m.put("endTime", instance.getEndTime() != null
                    ? cn.hutool.core.date.DateUtil.format(instance.getEndTime(), "yyyy-MM-dd HH:mm:ss")
                    : null);
            m.put("deleteReason", instance.getDeleteReason());
            // 判断是否还在运行（无结束时间则进行中）
            m.put("finished", instance.getEndTime() != null);
            return m;
        }).toList();
    }

    /**
     * 物理删除仿真历史（彻底清除 runtime + history 数据）
     */
    public void deleteHistory(String instanceId) {
        Assert.hasText(instanceId, "instanceId 不能为空");

        // 如果流程还在运行，先终止
        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(instanceId)
                .singleResult();
        if (instance != null) {
            runtimeService.deleteProcessInstance(instanceId, "仿真历史删除");
        }

        // 物理删除历史数据
        historyService.deleteHistoricProcessInstance(instanceId);
        log.info("仿真历史已物理删除: instanceId={}", instanceId);
    }

    private void injectSimulationVariables(Map<String, Object> variables, SysUser initiator, String bizKey) {
        variables.put(FlowableConstants.VAR_USER_ID, initiator.getId());
        variables.put(FlowableConstants.VAR_USER_NAME, initiator.getName());
        variables.put("BUSINESS_KEY", bizKey);
        variables.put("GLOBAL_FORM_KEY", bizKey);
        // 仿真标记 — 用于查询过滤
        variables.put(FlowableConstants.VAR_SIMULATION, "true");
        log.info("注入仿真变量: userId={}, bizKey={}", initiator.getId(), bizKey);
    }

    private void addComment(String processInstanceId, String taskId, String userId, String comment) {
        Comment commentEntity = taskService.addComment(taskId, processInstanceId, comment);
        commentEntity.setUserId(userId);
        taskService.saveComment(commentEntity);
    }
}
