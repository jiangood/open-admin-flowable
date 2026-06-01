package io.github.jiangood.openadmin.modules.flowable.example;

import io.github.jiangood.openadmin.modules.flowable.enums.ProcessEventType;
import io.github.jiangood.openadmin.modules.flowable.listener.ProcessListener;
import io.github.jiangood.openadmin.modules.flowable.example.service.LeaveApplyService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@AllArgsConstructor
public class LeaveProcessListener implements ProcessListener {

    private final LeaveApplyService leaveApplyService;

    @Override
    public void onProcessEvent(ProcessEventType type, String initiator, String businessKey, Map<String, Object> variables) {
        if (type == ProcessEventType.PROCESS_STARTED) {
            leaveApplyService.create(businessKey, variables);
        } else if (type == ProcessEventType.PROCESS_CANCELLED) {
            leaveApplyService.updateStatus(businessKey, "已拒绝");
        } else if (type == ProcessEventType.PROCESS_COMPLETED) {
            leaveApplyService.updateStatus(businessKey, "已通过");
        }
    }

    @Override
    public void onFormSubmit(String formKey, Map<String, Object> formData, String initiator, String approver,
                             String businessKey, String processInstanceId, String taskId, String comment) {
        leaveApplyService.updateFormData(businessKey, formData);
    }
}
