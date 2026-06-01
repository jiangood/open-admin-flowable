package io.github.jiangood.openadmin.modules.flowable.example.controller;

import io.github.jiangood.openadmin.framework.auth.LoginTool;
import io.github.jiangood.openadmin.modules.flowable.example.entity.LeaveApply;
import io.github.jiangood.openadmin.modules.flowable.example.service.LeaveApplyService;
import io.github.jiangood.openadmin.modules.flowable.service.ProcessService;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("admin/flowable/example/leave")
@AllArgsConstructor
public class LeaveApplyController {

    private final LeaveApplyService leaveApplyService;
    private final ProcessService processService;

    @GetMapping("detail")
    public AjaxResult detail(String businessKey) {
        LeaveApply apply = leaveApplyService.getByBusinessKey(businessKey);
        return AjaxResult.ok().data(apply);
    }

    @GetMapping("list")
    public AjaxResult list() {
        return AjaxResult.ok().data(leaveApplyService.listAll());
    }

    @PostMapping("start")
    public AjaxResult start(@RequestBody Map<String, Object> params) {
        String bizKey = (String) params.get("businessKey");
        if (bizKey == null) {
            bizKey = "LEAVE_" + System.currentTimeMillis();
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("reason", params.get("reason"));
        variables.put("days", params.get("days"));
        variables.put("actualDays", params.get("days"));
        variables.put("leaveType", params.get("leaveType"));

        processService.start("leave_request", bizKey, variables);
        return AjaxResult.ok().data(bizKey).msg("发起成功");
    }
}
