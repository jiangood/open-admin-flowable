package io.github.jiangood.openadmin.modules.flowable.controller;

import cn.hutool.core.lang.Dict;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.util.dto.IdReq;
import io.github.jiangood.openadmin.util.dto.Option;
import io.github.jiangood.openadmin.util.PageTool;
import io.github.jiangood.openadmin.util.SpringTool;
import io.github.jiangood.openadmin.util.annotation.RemarkTool;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.framework.log.Log;
import io.github.jiangood.openadmin.modules.flowable.config.meta.FormDefinition;
import io.github.jiangood.openadmin.modules.flowable.config.meta.ProcessMeta;
import io.github.jiangood.openadmin.modules.flowable.config.meta.ProcessVariable;
import io.github.jiangood.openadmin.modules.flowable.dto.vo.ModelPageVO;
import io.github.jiangood.openadmin.modules.flowable.service.ProcessMetaService;
import io.github.jiangood.openadmin.modules.flowable.utils.FlowablePageTool;
import io.github.jiangood.openadmin.modules.system.entity.SysRole;
import io.github.jiangood.openadmin.modules.system.entity.SysUser;
import io.github.jiangood.openadmin.modules.system.service.SysRoleService;
import io.github.jiangood.openadmin.modules.system.service.SysUserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.Process;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ModelQuery;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流程模型控制器
 */
@Slf4j
@RestController
@RequestMapping("admin/flowable/model")
@AllArgsConstructor
public class ModelController {

    private SysUserService sysUserService;
    private SysRoleService roleService;


    private RepositoryService repositoryService;

    private ProcessMetaService processMetaService;

    @PreAuthorize("hasAuthority('flowableModel:design')")
    @RequestMapping("page")
    public AjaxResult page(String searchText, Pageable pageable) throws Exception {
        ModelQuery query = repositoryService.createModelQuery();
        if (searchText != null) {
            query.modelNameLike(searchText);
        }
        Page<Model> page = FlowablePageTool.queryPage(query, pageable);
        Page<ModelPageVO> page2 = PageTool.convert(page, ModelPageVO::from);


        return AjaxResult.ok().data(page2);
    }

    @GetMapping("detail")
    public AjaxResult detail(String id) {
        Model model = repositoryService.getModel(id);
        Assert.notNull(model, "流程未定义");

        byte[] source = repositoryService.getModelEditorSource(id);

        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("name", model.getName());
        data.put("key", model.getKey());
        data.put("content", new String(source, StandardCharsets.UTF_8));

        return AjaxResult.ok().data(data);
    }


    @PreAuthorize("hasAuthority('flowableModel:design')")
    @PostMapping("delete")
    public AjaxResult delete(@Valid @RequestBody  IdReq id) {
        repositoryService.deleteModel(id.getId());
        return AjaxResult.ok().msg("删除模型成功");
    }

    @PreAuthorize("hasAuthority('flowableModel:design')")
    @PostMapping("saveContent")
    public AjaxResult saveContent(@RequestBody ModelRequest param) {
        Assert.hasText(param.content(), "内容不能为空");
        repositoryService.addModelEditorSource(param.id(), param.content().getBytes(StandardCharsets.UTF_8));
        return AjaxResult.ok().msg("保存成功");
    }

    @Log("部署流程模型")
    @PreAuthorize("hasAuthority('flowableModel:deploy')")
    @PostMapping("deploy")
    public AjaxResult deploy(@RequestBody ModelRequest param) {
        String xml = param.content();
        String id = param.id();
        Assert.hasText(xml, "内容不能为空");
        repositoryService.addModelEditorSource(id, xml.getBytes(StandardCharsets.UTF_8));

        log.info("保存成功，准备部署");

        Model m = repositoryService.getModel(id);
        BpmnModel bpmnModel = BpmnModelUtils.xmlToModel(xml);


        Process mainProcess = bpmnModel.getMainProcess();
        mainProcess.setExecutable(true);
        String key = m.getKey();
        mainProcess.setId(key);
        mainProcess.setName(m.getName());

        // 校验模型
        BpmnModelUtils.validateModel(bpmnModel);

        String resourceName = m.getName() + ".bpmn20.xml";

        repositoryService.createDeployment()
                .addBpmnModel(resourceName, bpmnModel)
                .name(m.getName())
                .key(key)
                .deploy();


        return AjaxResult.ok().msg("部署成功");
    }

    @GetMapping("javaDelegateOptions")
    public AjaxResult javaDelegateOptions() {
        Map<String, JavaDelegate> beans = SpringTool.getBeansOfType(JavaDelegate.class);
        List<Option> options = new ArrayList<>();
        for (Map.Entry<String, JavaDelegate> e : beans.entrySet()) {
            String beanName = e.getKey();
            JavaDelegate value = e.getValue();
            Class<? extends JavaDelegate> cls = value.getClass();
            log.info("{}: {}", beanName, cls);
            String remark = RemarkTool.getRemark(cls);

            String label = remark == null ? beanName : remark;
            String key = "${" + beanName + "}";
            options.add(new Option(key, label));
        }

        return AjaxResult.ok().data(options);
    }

    @GetMapping("formOptions")
    public AjaxResult formOptions(String code) {
        ProcessMeta meta = processMetaService.findOne(code);
        List<FormDefinition> formList = meta.getForms();
        if (formList == null) {
            formList = new ArrayList<>();
        }

        List<Option> options = Option.convertList(formList, FormDefinition::getKey, FormDefinition::getLabel);

        return AjaxResult.ok().data(options);
    }

    @GetMapping("assigneeOptions")
    public AjaxResult assigneeOptions(String searchText) {
        List<Option> list = queryUserOptions(searchText);

        list.add(0, new Option("${INITIATOR}", "发起人"));
        list.add(1, new Option("${INITIATOR_DEPT_LEADER}", "部门负责人"));

        return AjaxResult.ok().data(list);
    }

    @GetMapping("candidateGroupsOptions")
    public AjaxResult candidateGroupsOptions() {
        List<Option> list = new ArrayList<>();

        List<SysRole> roleList = roleService.findAll(Sort.by("seq", "name"));

        for (SysRole sysRole : roleList) {
            list.add(new Option(sysRole.getId(), sysRole.getName()));
        }


        return AjaxResult.ok().data(list);
    }

    @GetMapping("candidateUsersOptions")
    public AjaxResult candidateUsersOptions(String searchText) {
        return AjaxResult.ok().data(queryUserOptions(searchText));
    }

    private List<Option> queryUserOptions(String searchText) {
        Spec<SysUser> spec = Spec.of();
        List<SysUser> userList = sysUserService.findAll(spec.orLike(searchText, "name", "account", "phone"), Sort.by("name"));

        List<Option> list = new ArrayList<>();
        for (SysUser sysUser : userList) {
            list.add(new Option(sysUser.getId(), sysUser.getName()));
        }
        return list;
    }

    @GetMapping("varList")
    public AjaxResult varOptions(String code) {
        ProcessMeta meta = processMetaService.findOne(code);
        List<ProcessVariable> variables = meta.getVariables();

        return AjaxResult.ok().data(variables);
    }

    @GetMapping("definitionPage")
    public AjaxResult definitionPage(String key, Pageable pageable) {
        Assert.notNull(key, "编码不能为空");
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionKey(key)
                .orderByProcessDefinitionVersion().desc();

        Page<ProcessDefinition> page = FlowablePageTool.queryPage(query, pageable);
        Page<Dict> page2 = PageTool.convert(page, d -> Dict.create()
                .set("id", d.getId())
                .set("key", d.getKey())
                .set("name", d.getName())
                .set("version", d.getVersion())
        );

        return AjaxResult.ok().data(page2);
    }

    @GetMapping("getDefinitionContent")
    public AjaxResult getDefinitionContent(String id) {
        Assert.notNull(id, "id不能为空");
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().processDefinitionId(id).singleResult();

        BpmnModel bpmnModel = repositoryService.getBpmnModel(definition.getId());
        String xml = BpmnModelUtils.modelToXml(bpmnModel);

        return AjaxResult.ok().data(xml).msg("加载流程xml成功");
    }


    public record ModelRequest(String id, String content) {
    }

}
