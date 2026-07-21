package io.github.jiangood.openadmin.modules.flowable.utils;

import io.github.jiangood.openadmin.modules.flowable.common.utils.ModelTool;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.UserTask;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ModelToolTest {

    @Test
    void testXmlToModel_roundtrip() {
        String xml = """
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             targetNamespace="http://flowable.org/bpmn">
  <process id="testProcess" name="Test Process" isExecutable="true">
    <startEvent id="startEvent1" name="Start"></startEvent>
  </process>
</definitions>""";

        BpmnModel model = ModelTool.xmlToModel(xml);
        assertNotNull(model);
        assertNotNull(model.getMainProcess());
        assertEquals("testProcess", model.getMainProcess().getId());

        String xml2 = ModelTool.modelToXml(model);
        BpmnModel model2 = ModelTool.xmlToModel(xml2);
        assertNotNull(model2);
        assertEquals("testProcess", model2.getMainProcess().getId());
    }

    @Test
    void testValidateModel_taskWithoutAssignee_shouldFail() {
        BpmnModel model = new BpmnModel();
        org.flowable.bpmn.model.Process process = new org.flowable.bpmn.model.Process();
        process.setId("testProcess");
        process.setExecutable(true);
        UserTask task = new UserTask();
        task.setId("userTask1");
        task.setName("Test Task");
        process.addFlowElement(task);
        model.addProcess(process);

        assertThrows(IllegalArgumentException.class, () -> ModelTool.validateModel(model));
    }
}