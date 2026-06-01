package io.github.jiangood.openadmin.modules.flowable.controller;

import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.modules.flowable.service.SimulateService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("admin/flowable/simulate")
@AllArgsConstructor
public class SimulateController {

    private final SimulateService simulateService;

    /**
     * 获取模型元数据
     */
    @GetMapping("get")
    public AjaxResult get(String id) {
        return AjaxResult.ok().data(simulateService.getModelMeta(id));
    }

    /**
     * 启动仿真流程
     */
    @PostMapping("start")
    public AjaxResult start(@Valid @RequestBody StartRequest request) {
        String instanceId = simulateService.startSimulation(
                request.key(), request.id(), request.initiatorId(), request.variables());
        return AjaxResult.ok().data(Map.of("instanceId", instanceId)).msg("仿真流程已启动");
    }

    /**
     * 获取仿真状态
     */
    @GetMapping("status")
    public AjaxResult status(String instanceId) {
        return AjaxResult.ok().data(simulateService.getStatus(instanceId));
    }

    /**
     * 处理仿真任务
     */
    @PostMapping("task/handle")
    public AjaxResult handleTask(@Valid @RequestBody HandleRequest request) {
        simulateService.handleTask(request.taskId(), request.action(), request.comment(), request.handleUserId());
        return AjaxResult.ok().msg("处理成功");
    }

    /**
     * 查询可选用户列表
     */
    @GetMapping("users")
    public AjaxResult users(String searchText) {
        return AjaxResult.ok().data(simulateService.listUsers(searchText));
    }

    /**
     * 查询仿真历史列表
     */
    @GetMapping("list")
    public AjaxResult list(String key) {
        return AjaxResult.ok().data(simulateService.listHistory(key));
    }

    /**
     * 物理删除仿真历史
     */
    @PostMapping("delete")
    public AjaxResult delete(@Valid @RequestBody DeleteRequest request) {
        simulateService.deleteHistory(request.instanceId());
        return AjaxResult.ok().msg("仿真历史已删除");
    }

    public record DeleteRequest(
            @NotBlank String instanceId
    ) {
    }

    public record StartRequest(
            @NotBlank String key,
            @NotBlank String id,
            @NotBlank String initiatorId,
            Map<String, Object> variables
    ) {
    }

    public record HandleRequest(
            @NotBlank String taskId,
            @NotBlank String action,
            String comment,
            @NotBlank String handleUserId
    ) {
    }
}
