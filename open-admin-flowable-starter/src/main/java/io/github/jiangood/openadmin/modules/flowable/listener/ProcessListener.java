package io.github.jiangood.openadmin.modules.flowable.listener;

import io.github.jiangood.openadmin.modules.flowable.enums.ProcessEventType;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 流程定义接口
 */
public interface ProcessListener {


    /**
     * @param type
     * @param initiator   发起人
     * @param businessKey 业务标识，如key
     * @param variables   变量
     */
    @Transactional
    void onProcessEvent(ProcessEventType type, String initiator, String businessKey, Map<String, Object> variables);

    default void onFormSubmit(String formKey, Map<String, Object> formData, String initiator, String approver, String businessKey, String processInstanceId, String taskId, String comment) {
    }

}
