package io.github.jiangood.openadmin.modules.flowable.config;

import io.github.jiangood.openadmin.util.SpringTool;
import io.github.jiangood.openadmin.modules.flowable.domain.ProcessListener;
import io.github.jiangood.openadmin.modules.flowable.domain.ProcessMeta;
import io.github.jiangood.openadmin.modules.flowable.core.ProcessEventType;
import io.github.jiangood.openadmin.modules.flowable.service.ProcessMetaService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.engine.delegate.event.impl.FlowableProcessEventImpl;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

@Slf4j
@Component
@AllArgsConstructor
public class GlobalProcessListener implements FlowableEventListener {


    private ProcessMetaService processMetaService;


    @Override
    public void onEvent(FlowableEvent flowableEvent) {
        log.debug("流程事件 {}", flowableEvent.getType());

        if (!(flowableEvent instanceof FlowableProcessEventImpl event)) {
            return;
        }


        String typeName = flowableEvent.getType().name();
        ProcessEventType eventType = ProcessEventType.findByName(typeName);
        if (eventType == null) {
            return;
        }

        if (event.getExecution() == null) { return; }
        if (!(event.getExecution() instanceof ExecutionEntityImpl execution)) { return; }
        String definitionKey = execution.getProcessDefinitionKey();

        // 触发
        ProcessListener bizListener = getBizListener(definitionKey);
        if (bizListener == null) {
            return;
        }


        Map<String, Object> variables = execution.getVariables();
        // String initiator = (String) variables.get("INITIATOR");
        String businessKey = execution.getBusinessKey();
        String initiator = execution.getStartUserId();


        if (businessKey == null || initiator == null) {
            ExecutionEntityImpl processInstance = execution.getProcessInstance();
            businessKey = processInstance.getBusinessKey();
            initiator = processInstance.getStartUserId();
        }
        Assert.state(businessKey != null && initiator != null, "businessKey 或 initiator 不能为空");

        log.debug("流程事件 类型 {} 定义 {} 业务标识 {} 发起人 {} ", definitionKey, event.getType(), businessKey, initiator);
        bizListener.onProcessEvent(eventType, initiator, businessKey, variables);
    }

    private ProcessListener getBizListener(String definitionKey) {
        ProcessMeta meta = processMetaService.findOne(definitionKey);
        if (meta != null) {
            Class<? extends ProcessListener> listener = meta.getListener();
            if (listener != null) {
                ProcessListener bean = SpringTool.getBean(listener);
                return bean;
            }
        }
        return null;
    }


    @Override
    public boolean isFailOnException() {
        return true;
    }

    @Override
    public boolean isFireOnTransactionLifecycleEvent() {
        return false;
    }

    @Override
    public String getOnTransaction() {
        return null;
    }

    @Override
    public Collection<? extends org.flowable.common.engine.api.delegate.event.FlowableEventType> getTypes() {
        return Arrays.stream(FlowableEngineEventType.values())
                .filter(t -> ProcessEventType.findByName(t.name()) != null)
                .toList();
    }
}

