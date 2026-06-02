package io.github.jiangood.openadmin.modules.flowable.service;

import io.github.jiangood.openadmin.modules.flowable.process.ProcessMeta;
import io.github.jiangood.openadmin.modules.flowable.common.utils.ModelTool;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.GraphicInfo;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.StartEvent;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class ProcessModelService {

    private final RepositoryService repositoryService;

    public Model initModel(ProcessMeta meta) {
        String key = meta.getKey();
        String name = meta.getName();
        log.info("初始化流程定义 {} {}  ", key, name);

        Model model = repositoryService.createModelQuery().modelKey(key).singleResult();
        if (model != null) {
            return model;
        }

        Model m = repositoryService.newModel();
        m.setName(name);
        m.setKey(key);
        repositoryService.saveModel(m);

        String xml = createDefaultModelXml(key, name);
        log.info("生成流程默认xml内容\n{}", xml);
        repositoryService.addModelEditorSource(m.getId(), xml.getBytes(StandardCharsets.UTF_8));
        return m;
    }

    private String createDefaultModelXml(String key, String name) {
        Assert.state(key.length() <= 16, "流程key长度不能超过16个字符");
        BpmnModel bpmnModel = new BpmnModel();
        Process proc = new Process();
        proc.setExecutable(true);
        proc.setId(key);
        proc.setName(name);
        bpmnModel.addProcess(proc);

        StartEvent startEvent = new StartEvent();
        startEvent.setId("StartEvent_1");
        proc.addFlowElement(startEvent);
        bpmnModel.addGraphicInfo(startEvent.getId(), new GraphicInfo(150, 100, 36, 36));

        return ModelTool.modelToXml(bpmnModel);
    }

    public void deleteModel(String modelId) {
        repositoryService.deleteModel(modelId);
    }

    public Model getModel(String modelId) {
        return repositoryService.getModel(modelId);
    }

    public List<ProcessDefinition> findAllProcessDefinition() {
        return repositoryService.createProcessDefinitionQuery().active().orderByProcessDefinitionKey().asc().list();
    }

    public void deleteProcessDefinitionByKey(String key) {
        List<Deployment> list = repositoryService.createDeploymentQuery().processDefinitionKey(key).list();
        for (Deployment d : list) {
            repositoryService.deleteDeployment(d.getId(), true);
        }
    }
}
