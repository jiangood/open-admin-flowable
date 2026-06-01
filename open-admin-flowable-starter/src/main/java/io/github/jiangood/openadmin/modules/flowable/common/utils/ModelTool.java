package io.github.jiangood.openadmin.modules.flowable.common.utils;

import cn.hutool.core.util.StrUtil;
import io.github.jiangood.openadmin.util.SpringTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.*;
import org.flowable.common.engine.impl.util.io.BytesStreamSource;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.validation.ProcessValidator;
import org.flowable.validation.ProcessValidatorFactory;
import org.flowable.validation.ValidationError;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流程模型service接口实现类
 */
@Slf4j
public class ModelTool {


    public static BpmnModel xmlToModel(String xml) {
        BpmnXMLConverter converter = new BpmnXMLConverter();
        byte[] xmlBytes = xml.getBytes(StandardCharsets.UTF_8);
        BytesStreamSource streamSource = new BytesStreamSource(xmlBytes);

        return converter.convertToBpmnModel(streamSource, true, true);
    }

    public static String modelToXml(BpmnModel model) {
        BpmnXMLConverter converter = new BpmnXMLConverter();
        byte[] bytes = converter.convertToXML(model, StandardCharsets.UTF_8.name());
        return new String(bytes, StandardCharsets.UTF_8);
    }


    public static void validateModel(BpmnModel model) {
        ProcessValidator validator = new ProcessValidatorFactory().createDefaultProcessValidator();

        // 默认校验
        List<ValidationError> errors = validator.validate(model);
        if (!errors.isEmpty()) {
            ValidationError error = errors.get(0);
            String problem = error.getProblem();

            Map<String, String> translates = new HashMap<>();
            translates.put("flowable-exclusive-gateway-seq-flow-without-conditions", "请设置分支条件");
            String msg = translates.get(problem);

            Assert.state(false, StrUtil.emptyToDefault(msg, error.getDefaultDescription()));
        }
        Process mainProcess = model.getMainProcess();

        // task 必须命名


        // 修改和检验模型
        for (FlowElement flowElement : mainProcess.getFlowElements()) {
            // 校验是否都分配对象
            if (flowElement instanceof UserTask task) {
                Assert.hasText(task.getName(), "部署失败：存在用户任务未命名的节点");


                if (task.getAssignee() == null &&
                        CollectionUtils.isEmpty(task.getCandidateUsers()) &&
                        CollectionUtils.isEmpty(task.getCandidateGroups())) {
                    throw new IllegalArgumentException("请指定分配对象");
                }

                String assignmentType = getAttr(task, "assignmentType");

                if (StringUtils.isNotEmpty(assignmentType)) {
                    // 重要，将指定人或组的类型放到category，方便后续使用
                    task.setCategory(assignmentType);
                }

            }

            // 设置发起人变量标识
            if (flowElement instanceof StartEvent startEvent) {
                startEvent.setInitiator("INITIATOR");
            }

            // 条件表达式测试
            if (flowElement instanceof SequenceFlow sequenceFlow) {
                String conditionExpression = sequenceFlow.getConditionExpression();

                if (StringUtils.isEmpty(conditionExpression)) {
                    continue;
                }

                try {
                    ProcessEngineConfigurationImpl processEngineConfiguration = SpringTool.getBean(ProcessEngineConfigurationImpl.class);
                    processEngineConfiguration.getExpressionManager().createExpression(conditionExpression);
                } catch (Exception e) {
                    throw new IllegalArgumentException("条件表达式异常:" + e.getMessage());
                }

            }
        }


    }


    /**
     * 获取属性，忽略namespace
     *
     * @return
     */
    private static String getAttr(FlowElement el, String name) {
        Map<String, List<ExtensionAttribute>> attrs = el.getAttributes();
        if (attrs.isEmpty()) {
            return null;
        }
        List<ExtensionAttribute> attributes = attrs.get(name);

        if (attributes == null) {
            return null;
        }

        Assert.state(attributes.size() <= 1, "属性过多，请指定namespace");

        if (attributes.size() == 1) {
            return attributes.get(0).getValue();
        }

        return null;
    }


}
