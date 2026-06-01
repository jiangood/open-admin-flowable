package io.github.jiangood.openadmin.modules.flowable.example.service;

import io.github.jiangood.openadmin.modules.flowable.example.entity.LeaveApply;
import io.github.jiangood.openadmin.modules.flowable.example.repository.LeaveApplyRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class LeaveApplyService {

    private final LeaveApplyRepository leaveApplyRepository;

    public LeaveApply getByBusinessKey(String businessKey) {
        return leaveApplyRepository.findByBusinessKey(businessKey).orElse(null);
    }

    public LeaveApply create(String businessKey, Map<String, Object> variables) {
        LeaveApply apply = new LeaveApply();
        apply.setBusinessKey(businessKey);
        apply.setReason((String) variables.get("reason"));
        apply.setDays(toInt(variables.get("days")));
        apply.setActualDays(toInt(variables.get("actualDays")));
        apply.setLeaveType((String) variables.get("leaveType"));
        apply.setStatus("审批中");
        LocalDateTime now = LocalDateTime.now();
        apply.setCreateTime(now);
        apply.setUpdateTime(now);
        return leaveApplyRepository.save(apply);
    }

    public LeaveApply updateFormData(String businessKey, Map<String, Object> formData) {
        LeaveApply apply = leaveApplyRepository.findByBusinessKey(businessKey)
                .orElseThrow(() -> new RuntimeException("业务数据不存在：" + businessKey));
        if (formData.containsKey("reason")) {
            apply.setReason((String) formData.get("reason"));
        }
        if (formData.containsKey("days")) {
            apply.setDays(toInt(formData.get("days")));
        }
        if (formData.containsKey("actualDays")) {
            apply.setActualDays(toInt(formData.get("actualDays")));
        }
        if (formData.containsKey("leaveType")) {
            apply.setLeaveType((String) formData.get("leaveType"));
        }
        apply.setUpdateTime(LocalDateTime.now());
        return leaveApplyRepository.save(apply);
    }

    public void updateStatus(String businessKey, String status) {
        LeaveApply apply = leaveApplyRepository.findByBusinessKey(businessKey)
                .orElseThrow(() -> new RuntimeException("业务数据不存在：" + businessKey));
        apply.setStatus(status);
        apply.setUpdateTime(LocalDateTime.now());
        leaveApplyRepository.save(apply);
    }

    public List<LeaveApply> listAll() {
        return leaveApplyRepository.findAll();
    }

    private Integer toInt(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.intValue();
        if (value instanceof String s) return Integer.parseInt(s);
        return null;
    }
}
