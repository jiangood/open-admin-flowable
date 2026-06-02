package io.github.jiangood.openadmin.modules.flowable.controller;

import io.github.jiangood.openadmin.framework.log.Log;
import io.github.jiangood.openadmin.modules.flowable.dto.ModelPageResp;
import io.github.jiangood.openadmin.modules.flowable.dto.ModelRequest;
import io.github.jiangood.openadmin.modules.flowable.service.ModelService;
import io.github.jiangood.openadmin.modules.flowable.service.ModelOptionsService;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.util.dto.IdReq;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("admin/flowable/model")
@AllArgsConstructor
public class ModelController {

    private final ModelService modelService;
    private final ModelOptionsService modelOptionsService;

    @PreAuthorize("hasAuthority('flowableModel:design')")
    @RequestMapping("page")
    public AjaxResult page(String searchText, Pageable pageable) {
        Page<ModelPageResp> page = modelService.page(searchText, pageable);
        return AjaxResult.ok().data(page);
    }

    @GetMapping("detail")
    public AjaxResult detail(String id) {
        return AjaxResult.ok().data(modelService.detail(id));
    }

    @PreAuthorize("hasAuthority('flowableModel:design')")
    @PostMapping("delete")
    public AjaxResult delete(@Valid @RequestBody IdReq id) {
        modelService.delete(id.getId());
        return AjaxResult.ok().msg("删除模型成功");
    }

    @PreAuthorize("hasAuthority('flowableModel:design')")
    @PostMapping("saveContent")
    public AjaxResult saveContent(@RequestBody ModelRequest param) {
        modelService.saveContent(param.id(), param.content());
        return AjaxResult.ok().msg("保存成功");
    }

    @Log("部署流程模型")
    @PreAuthorize("hasAuthority('flowableModel:deploy')")
    @PostMapping("deploy")
    public AjaxResult deploy(@RequestBody ModelRequest param) {
        modelService.deploy(param.id(), param.content());
        return AjaxResult.ok().msg("部署成功");
    }

    @GetMapping("javaDelegateOptions")
    public AjaxResult javaDelegateOptions() {
        return AjaxResult.ok().data(modelOptionsService.javaDelegateOptions());
    }

    @GetMapping("formOptions")
    public AjaxResult formOptions(String code) {
        return AjaxResult.ok().data(modelOptionsService.formOptions(code));
    }

    @GetMapping("assigneeOptions")
    public AjaxResult assigneeOptions(String searchText) {
        return AjaxResult.ok().data(modelOptionsService.assigneeOptions(searchText));
    }

    @GetMapping("candidateGroupsOptions")
    public AjaxResult candidateGroupsOptions() {
        return AjaxResult.ok().data(modelOptionsService.candidateGroupsOptions());
    }

    @GetMapping("candidateUsersOptions")
    public AjaxResult candidateUsersOptions(String searchText) {
        return AjaxResult.ok().data(modelOptionsService.candidateUsersOptions(searchText));
    }

    @GetMapping("varList")
    public AjaxResult varOptions(String code) {
        return AjaxResult.ok().data(modelOptionsService.varOptions(code));
    }

    @GetMapping("definitionPage")
    public AjaxResult definitionPage(String key, Pageable pageable) {
        return AjaxResult.ok().data(modelService.definitionPage(key, pageable));
    }

    @GetMapping("getDefinitionContent")
    public AjaxResult getDefinitionContent(String id) {
        return AjaxResult.ok().data(modelService.getDefinitionContent(id));
    }
}
