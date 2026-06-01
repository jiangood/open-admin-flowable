package io.github.jiangood.openadmin.modules.flowable.model;

import cn.hutool.core.lang.Dict;
import io.github.jiangood.openadmin.modules.flowable.common.utils.FlowablePageTool;
import static io.github.jiangood.openadmin.modules.flowable.common.utils.ModelTool.*;
import io.github.jiangood.openadmin.util.PageTool;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.Process;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ModelQuery;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class ModelService {

    private final RepositoryService repositoryService;

    public Page<ModelPageVO> page(String searchText, Pageable pageable) {
        ModelQuery query = repositoryService.createModelQuery();
        if (searchText != null) {
            query.modelNameLike(searchText);
        }
        Page<Model> page = FlowablePageTool.queryPage(query, pageable);
        return PageTool.convert(page, ModelPageVO::from);
    }

    public Map<String, Object> detail(String id) {
        Model model = repositoryService.getModel(id);
        Assert.notNull(model, "流程未定义");
        byte[] source = repositoryService.getModelEditorSource(id);
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("name", model.getName());
        data.put("key", model.getKey());
        data.put("content", new String(source, StandardCharsets.UTF_8));
        return data;
    }

    public void delete(String id) {
        repositoryService.deleteModel(id);
    }

    public void saveContent(String id, String content) {
        Assert.hasText(content, "内容不能为空");
        repositoryService.addModelEditorSource(id, content.getBytes(StandardCharsets.UTF_8));
    }

    public void deploy(String id, String xml) {
        Assert.hasText(xml, "内容不能为空");
        repositoryService.addModelEditorSource(id, xml.getBytes(StandardCharsets.UTF_8));

        Model m = repositoryService.getModel(id);
        BpmnModel bpmnModel = xmlToModel(xml);

        Process mainProcess = bpmnModel.getMainProcess();
        mainProcess.setExecutable(true);
        String key = m.getKey();
        mainProcess.setId(key);
        mainProcess.setName(m.getName());

        validateModel(bpmnModel);

        String resourceName = m.getName() + ".bpmn20.xml";

        repositoryService.createDeployment()
                .addBpmnModel(resourceName, bpmnModel)
                .name(m.getName())
                .key(key)
                .deploy();
    }

    public Page<Dict> definitionPage(String key, Pageable pageable) {
        Assert.notNull(key, "编码不能为空");
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(key)
                .orderByProcessDefinitionVersion().desc();

        Page<ProcessDefinition> page = FlowablePageTool.queryPage(query, pageable);
        return PageTool.convert(page, d -> Dict.create()
                .set("id", d.getId())
                .set("key", d.getKey())
                .set("name", d.getName())
                .set("version", d.getVersion())
        );
    }

    public String getDefinitionContent(String id) {
        Assert.notNull(id, "id不能为空");
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(id).singleResult();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(definition.getId());
        return modelToXml(bpmnModel);
    }
}
