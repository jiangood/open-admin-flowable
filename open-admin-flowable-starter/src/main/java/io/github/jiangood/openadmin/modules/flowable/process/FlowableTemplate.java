package io.github.jiangood.openadmin.modules.flowable.process;

import cn.hutool.core.date.DateUtil;
import io.github.jiangood.openadmin.framework.auth.LoginTool;
import io.github.jiangood.openadmin.framework.config.security.LoginUser;
import io.github.jiangood.openadmin.modules.flowable.constant.FlowableConstants;
import io.github.jiangood.openadmin.modules.flowable.process.ProcessMeta;
import io.github.jiangood.openadmin.modules.flowable.process.ProcessVariable;
import io.github.jiangood.openadmin.modules.flowable.process.ProcessMetaService;
import lombok.AllArgsConstructor;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.engine.IdentityService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.text.MessageFormat;
import java.util.*;

@Component
@AllArgsConstructor
public class FlowableTemplate {

    private final ProcessMetaService processMetaService;
    private final RepositoryService repositoryService;
    private final RuntimeService runtimeService;
    private final IdentityService identityService;

    public void startProcess(String key, String bizKey, Map<String, Object> variables) {
        startProcess(key, bizKey, null, variables);
    }

    public void startProcess(String key, String bizKey, String title, Map<String, Object> variables) {
        Assert.notNull(key, "流程编码不能为空");
        if (variables == null) { variables = new HashMap<>(); }

        LoginUser user = LoginTool.getUser();
        ProcessMeta meta = processMetaService.findOne(key);
        Assert.notNull(meta, "流程元数据定义不存在：" + key);

        String startUserId = user.getId();
        Assert.hasText(startUserId, "当前登录人员ID不能为空");
        injectUserVariables(variables, user, bizKey, meta);

        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(key).active().latestVersion().singleResult();
        Assert.notNull(definition, "流程尚未部署，请设计后部署。编码：" + key);

        if (title == null) { title = buildDefaultTitle(user, definition, bizKey); }

        long instanceCount = runtimeService.createProcessInstanceQuery()
                .processInstanceBusinessKey(bizKey).active().count();
        Assert.state(instanceCount == 0, "该业务标识的流程审批中，请勿重复提交");

        validateRequiredVariables(meta, variables);
        validateRelativeVariables(definition, variables);

        identityService.setAuthenticatedUserId(startUserId);
        runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey(key).businessKey(bizKey)
                .variables(variables).name(title).start();
    }

    private void injectUserVariables(Map<String, Object> variables, LoginUser user, String bizKey, ProcessMeta meta) {
        variables.put(FlowableConstants.VAR_USER_ID, user.getId());
        variables.put(FlowableConstants.VAR_USER_NAME, user.getName());
        variables.put(FlowableConstants.VAR_UNIT_ID, user.getUnitId());
        variables.put(FlowableConstants.VAR_UNIT_NAME, user.getUnitName());
        variables.put(FlowableConstants.VAR_DEPT_ID, user.getDeptId());
        variables.put(FlowableConstants.VAR_DEPT_NAME, user.getDeptName());
        variables.put(FlowableConstants.VAR_INITIATOR_DEPT_LEADER, user.getDeptLeaderId());
        variables.put("BUSINESS_KEY", bizKey);
        variables.put("GLOBAL_FORM_KEY", meta.getGlobalFormKey() != null ? meta.getGlobalFormKey() : meta.getKey());
    }

    private String buildDefaultTitle(LoginUser user, ProcessDefinition definition, String bizKey) {
        String day = DateUtil.format(new Date(), "yyyy年M月d日");
        return MessageFormat.format("{0}({1}){2}发起的【{3}】(业务单号:{4})",
                user.getName(), user.getDeptName() != null ? user.getDeptName() : "未知部门",
                day, definition.getName(), bizKey);
    }

    private void validateRequiredVariables(ProcessMeta meta, Map<String, Object> variables) {
        List<ProcessVariable> list = meta.getVariables();
        if (CollectionUtils.isEmpty(list)) return;
        for (ProcessVariable item : list) {
            String name = item.getName();
            Assert.state(variables.containsKey(name), "流程异常，必填变量未设置：" + item.getLabel() + ":" + name);
            Assert.notNull(variables.get(name), "流程异常，必填变量未设置：" + item.getLabel() + ":" + name);
        }
    }

    private void validateRelativeVariables(ProcessDefinition definition, Map<String, Object> variables) {
        BpmnModel bpmnModel = repositoryService.getBpmnModel(definition.getId());
        for (FlowElement el : bpmnModel.getMainProcess().getFlowElements()) {
            if (el instanceof org.flowable.bpmn.model.UserTask ut) {
                if (ut.getAssignee() != null && ut.getAssignee().contains(FlowableConstants.VAR_INITIATOR_DEPT_LEADER)) {
                    Assert.notNull(variables.get(FlowableConstants.VAR_INITIATOR_DEPT_LEADER), "操作失败：发起用户的部门负责人为空");
                }
            }
        }
    }
}
