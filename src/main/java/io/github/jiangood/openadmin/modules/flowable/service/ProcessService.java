package io.github.jiangood.openadmin.modules.flowable.service;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import io.github.jiangood.openadmin.util.FriendlyTool;
import io.github.jiangood.openadmin.util.PageTool;
import io.github.jiangood.openadmin.framework.config.security.LoginUser;
import io.github.jiangood.openadmin.framework.auth.LoginTool;
import io.github.jiangood.openadmin.modules.flowable.config.meta.ProcessMeta;
import io.github.jiangood.openadmin.modules.flowable.config.meta.ProcessVariable;
import io.github.jiangood.openadmin.modules.flowable.core.FlowableProperties;
import io.github.jiangood.openadmin.modules.flowable.dto.TaskHandleType;
import io.github.jiangood.openadmin.modules.flowable.dto.response.TaskResponse;
import io.github.jiangood.openadmin.modules.flowable.utils.FlowablePageTool;
import io.github.jiangood.openadmin.modules.flowable.utils.ModelTool;
import io.github.jiangood.openadmin.modules.system.entity.SysRole;
import io.github.jiangood.openadmin.modules.system.entity.SysUser;
import io.github.jiangood.openadmin.modules.system.service.SysUserService;
import io.github.jiangood.openadmin.modules.flowable.FlowableConstants;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.*;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ProcessDefinition;
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
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class ProcessService {

    private final TaskService taskService;
    private final RuntimeService runtimeService;
    private final SysUserService sysUserService;
    private final HistoryService historyService;
    private final BpmnDiagramService bpmnDiagramService;
    private final FlowableProperties flowableProperties;
    private final RepositoryService repositoryService;
    private final IdentityService identityService;
    private final ProcessMetaService processMetaService;

    public void start(String processDefinitionKey, String bizKey, Map<String, Object> variables) {
        start(processDefinitionKey, bizKey, null, variables);
    }

    public void start(String key, String bizKey, String title, Map<String, Object> variables) {
        Assert.notNull(key, "key不能为空");
        if (variables == null) {
            variables = new HashMap<>();
        }

        LoginUser user = LoginTool.getUser();
        ProcessMeta meta = processMetaService.findOne(key);
        Assert.notNull(meta, "流程元数据定义不存在：" + key);

        // 添加一些发起人的相关信息
        String startUserId = user.getId();
        Assert.hasText(startUserId, "当前登录人员ID不能为空");
        variables.put(FlowableConstants.VAR_USER_ID, startUserId);
        variables.put(FlowableConstants.VAR_USER_NAME, user.getName());
        variables.put(FlowableConstants.VAR_UNIT_ID, user.getUnitId());
        variables.put(FlowableConstants.VAR_UNIT_NAME, user.getUnitName());
        variables.put(FlowableConstants.VAR_DEPT_ID, user.getDeptId());
        variables.put(FlowableConstants.VAR_DEPT_NAME, user.getDeptName());
        variables.put(FlowableConstants.VAR_DEPT_LEADER, user.getDeptLeaderId());   // 部门领导
        variables.put("BUSINESS_KEY", bizKey);
        variables.put("GLOBAL_FORM_KEY", meta.getGlobalFormKey() != null ? meta.getGlobalFormKey() : meta.getKey()); // 全局表单key


        // 流程名称
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(key).active()
                .latestVersion()
                .singleResult();
        Assert.notNull(definition, "流程尚未部署，请设计后部署。编码：" + key);


        if (title == null) {
            String day = cn.hutool.core.date.DateUtil.format(new Date(), "yyyy年MM月dd日");
            title = MessageFormat.format("{0}({1}){2}发起的【{3}】(业务单号:{4})",
                    user.getName(),
                    user.getDeptName() != null ? user.getDeptName() : "未知部门",
                    day,
                    definition.getName(),
                    bizKey);
        }

        long instanceCount = runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(bizKey).active().count();
        Assert.state(instanceCount == 0, "流程审批中，请勿重复提交");

        // 判断必填流程变量

        List<ProcessVariable> variableList = meta.getVariables();
        if (!CollectionUtils.isEmpty(variableList)) {
            for (ProcessVariable formItem : variableList) {
                String name = formItem.getName();
                Assert.state(variables.containsKey(name), "流程异常, 必填变量未设置：" + formItem.getLabel() + ":" + name);
                Object v = variables.get(name);
                Assert.notNull(v, "流程异常, 必填变量未设置：" + formItem.getLabel() + ":" + name);
            }
        }

        // 判断相对变量，如部门领导
        BpmnModel bpmnModel = repositoryService.getBpmnModel(definition.getId());
        for (FlowElement flowElement : bpmnModel.getMainProcess().getFlowElements()) {
            if (flowElement instanceof org.flowable.bpmn.model.UserTask ut) {
                if (ut.getAssignee() != null && ut.getAssignee().contains(FlowableConstants.VAR_DEPT_LEADER)) {
                    Assert.notNull(variables.get(FlowableConstants.VAR_DEPT_LEADER), "操作失败：发起用户的部门领导为空");
                }
            }
        }


        // 设置发起人, 该方法会自动设置流程变量 INITIATOR -> startUserId
        identityService.setAuthenticatedUserId(startUserId);

        // 启动
        runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey(key)
                .businessKey(bizKey)
                .variables(variables)
                .name(title)
                .start();
    }

    /**
     * 获取最新的历史流程实例
     * 根据业务键查询历史流程实例，并按开始时间降序排列，返回最近的一个流程实例
     * <p>
     * 为什么不直接使用单个结果，因为可能存在多个结果
     *
     * @param bizKey 业务键，用于标识特定业务流程的唯一标识符
     * @return 最新的历史流程实例，如果未找到则返回null
     */
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

    public void deleteModel(String modelId) {
        repositoryService.deleteModel(modelId);
    }

    public Model getModel(String modelId) {
        return repositoryService.getModel(modelId);
    }

    public List<ProcessDefinition> findAllProcessDefinition() {
        return repositoryService.createProcessDefinitionQuery().active().orderByProcessDefinitionKey().asc().list();
    }


    /**
     * 初始化流程模型， 保存到数据库中
     *
     * @param meta
     * @return 模型ID
     */
    public Model initModel(ProcessMeta meta) {
        String key = meta.getKey();
        String name = meta.getName();
        log.info("初始化流程定义 {} {}  ", key, name);

        Model model = repositoryService.createModelQuery().modelKey(key).singleResult();
        if (model != null) {
            return model;
        }

        Model m = repositoryService.newModel();
        m.setName(name);
        m.setKey(key);
        repositoryService.saveModel(m);


        String xml = createDefaultModelXml(key, name);
        log.info("生成流程默认xml内容\n{}", xml);
        repositoryService.addModelEditorSource(m.getId(), xml.getBytes(StandardCharsets.UTF_8));
        return m;
    }

    private String createDefaultModelXml(String key, String name) {
        Assert.state(key.length() <= 16, "流程key长度不能超过16个字符");
        // create default model xml
        BpmnModel bpmnModel = new BpmnModel();
        Process proc = new Process();
        proc.setExecutable(true);
        proc.setId(key);
        proc.setName(name);
        bpmnModel.addProcess(proc);

        StartEvent startEvent = new StartEvent();
        startEvent.setId("StartEvent_1");
        proc.addFlowElement(startEvent);
        bpmnModel.addGraphicInfo(startEvent.getId(), new GraphicInfo(150, 100, 36, 36));


        return ModelTool.modelToXml(bpmnModel);
    }


    public void deleteProcessDefinitionByKey(String key) {
        List<Deployment> list = repositoryService.createDeploymentQuery().processDefinitionKey(key).list();
        for (Deployment d : list) {
            repositoryService.deleteDeployment(d.getId(), true);
        }
    }


    public long findUserTaskCount(String userId) {
        TaskQuery taskQuery = buildUserTodoTaskQuery(userId);
        return taskQuery.count();
    }

    public Page<TaskResponse> findUserTaskList(Pageable pageable, String userId) {
        TaskQuery query = buildUserTodoTaskQuery(userId);

        Page<Task> page = FlowablePageTool.queryPage(query, pageable);
        if (page.isEmpty()) {
            return Page.empty();
        }


        // 填充流程信息
        Set<String> instanceIds = page.stream().map(TaskInfo::getProcessInstanceId).collect(Collectors.toSet());
        Map<String, ProcessInstance> instanceMap = runtimeService.createProcessInstanceQuery().processInstanceIds(instanceIds).list().stream().collect(Collectors.toMap(Execution::getId, t -> t));

        Page<TaskResponse> page2 = PageTool.convert(page, task -> {
            ProcessInstance instance = instanceMap.get(task.getProcessInstanceId());
            TaskResponse r = new TaskResponse();
            convert(r, task);
            r.setInstanceName(instance.getName());
            r.setInstanceStartTime(FriendlyTool.getPastTime(instance.getStartTime()));
            r.setInstanceStarter(sysUserService.getNameById(instance.getStartUserId()));
            return r;
        });

        return page2;
    }

    public Page<TaskResponse> findUserTaskDoneList(Pageable pageable, String userId) {
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
        Map<String, HistoricProcessInstance> instanceMap = historyService.createHistoricProcessInstanceQuery().processInstanceIds(instanceIds).list()
                .stream().collect(Collectors.toMap(HistoricProcessInstance::getId, t -> t));


        Page<TaskResponse> page2 = PageTool.convert(page, task -> {
            HistoricProcessInstance instance = instanceMap.get(task.getProcessInstanceId());

            TaskResponse r = new TaskResponse();
            this.convert(r, task);
            r.setInstanceName(instance.getName());
            r.setInstanceStartTime(FriendlyTool.getPastTime(instance.getStartTime()));
            r.setInstanceStarter(sysUserService.getNameById(instance.getStartUserId()));
            r.setDurationInfo(FriendlyTool.getTimeDiff(task.getCreateTime(), task.getEndTime()));
            return r;
        });

        return page2;
    }

    public void handle(String userId, TaskHandleType result, String taskId, String comment) {
        Assert.notNull(userId, "用户Id不能为空");
        //校验任务是否存在
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        Assert.state(task != null, "任务已经处理过，请勿重复操作");

        //获取流程实例id
        String processInstanceId = task.getProcessInstanceId();
        comment = "【" + task.getName() + "】：" + result.getMessage() + "。" + comment;
        addComment(processInstanceId, taskId, userId, comment);

        String assignee = task.getAssignee();
        if (StrUtil.isNotEmpty(assignee)) {
            Assert.state(assignee.equals(userId), "处理人不一致");
        }

        taskService.setAssignee(taskId, userId);


        if (result == TaskHandleType.APPROVE) {
            taskService.complete(taskId);
            return;
        }

        // 点击拒绝（不同意）
        if (result == TaskHandleType.REJECT) {
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

    // 回退上一个节点
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


    public String getUserName(String userId) {
        if (userId == null) {
            return null;
        }
        return sysUserService.getNameById(userId);
    }


    private void addComment(String processInstanceId, String taskId, String taskAssignee, String comment) {
        // addComment 持久化时 userId 为空，需补设后再 save
        Comment commentEntity = taskService.addComment(taskId, processInstanceId, comment);
        commentEntity.setUserId(taskAssignee);
        taskService.saveComment(commentEntity);
    }


    public BufferedImage drawImage(String instanceId) {
        return bpmnDiagramService.drawImage(instanceId);
    }


    public TaskQuery buildUserTodoTaskQuery(String userId) {
        TaskQuery query = taskService.createTaskQuery().active();

        query.or();
        query.taskAssignee(userId);
        query.taskCandidateUser(userId);

        // 人员及 分组
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

    private void convert(TaskResponse r, TaskInfo task) {
        r.setId(task.getId());
        r.setTaskName(task.getName());
        r.setCreateTime(FriendlyTool.getPastTime(task.getCreateTime()));
        r.setAssigneeInfo(sysUserService.getNameById(task.getAssignee()));
        r.setFormKey(task.getFormKey());
        r.setInstanceId(task.getProcessInstanceId());
    }
}
