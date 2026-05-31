package io.github.jiangood.openadmin.modules.flowable.controller;

import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.modules.flowable.domain.ProcessMeta;
import io.github.jiangood.openadmin.modules.flowable.service.ProcessMetaService;
import io.github.jiangood.openadmin.modules.flowable.service.ProcessService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("admin/flowable/simulate")
@AllArgsConstructor
public class SimulateController {

    private ProcessService processService;
    private ProcessMetaService processMetaService;
    private RepositoryService repositoryService;

    @GetMapping("get")
    public AjaxResult get(String id) {
        Assert.hasText(id, "id不能为空");
        Model model = repositoryService.getModel(id);
        Assert.notNull(model, "流程模型不存在");
        ProcessMeta meta = processMetaService.findOne(model.getKey());
        return AjaxResult.ok().data(meta);
    }


    @PostMapping("submit")
    public AjaxResult submit(@Valid @RequestBody SubmitRequest request) {
        processService.start(request.key(), request.id(), request.variables());

        return AjaxResult.ok().msg("提交仿真流程成功");
    }

    public record SubmitRequest(
            @NotBlank String id,
            @NotBlank String key,
            Map<String, Object> variables
    ) {
    }

}
