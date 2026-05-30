package io.github.jiangood.openadmin.modules.flowable.service;


import io.github.jiangood.openadmin.util.FontTool;
import jakarta.annotation.Resource;
import org.flowable.bpmn.model.*;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.image.impl.DefaultProcessDiagramGenerator;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 流程图相关
 */
@Service
public class FlowableModelService {

    public static final String FONT_NAME = FontTool.getDefaultFontName();

    @Resource
    private RuntimeService runtimeService;

    @Resource
    private RepositoryService repositoryService;


    @Resource
    private HistoryService historyService;


    /**
     * 查询任务的上一个节点
     *
     * @param task
     * @return
     */
    public List<UserTask> findPreActivity(Task task) {
        ExecutionEntity execution = (ExecutionEntity) runtimeService.createExecutionQuery().executionId(task.getExecutionId()).singleResult();
        String activityId = execution.getActivityId();


        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        FlowNode flowNode = (FlowNode) bpmnModel.getFlowElement(activityId); // 当前节点

        List<UserTask> result = new ArrayList<>();
        List<SequenceFlow> incomingFlows = flowNode.getIncomingFlows();
        for (SequenceFlow sequenceFlow : incomingFlows) {
            FlowElement targetFlowElement = sequenceFlow.getSourceFlowElement();

            if (targetFlowElement instanceof UserTask) {
                result.add((UserTask) targetFlowElement);
            }
        }
        return result;

    }

    /**
     * 查询任务下一个节点
     *
     * @param task
     * @return
     */
    public List<UserTask> findNextTaskList(Task task) {
        ExecutionEntity execution = (ExecutionEntity) runtimeService.createExecutionQuery().executionId(task.getExecutionId()).singleResult();
        String activityId = execution.getActivityId();


        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        FlowNode flowNode = (FlowNode) bpmnModel.getFlowElement(activityId); // 当前节点

        List<UserTask> result = new ArrayList<>();
        List<SequenceFlow> outgoingFlows = flowNode.getOutgoingFlows();
        for (SequenceFlow sequenceFlow : outgoingFlows) {
            FlowElement targetFlowElement = sequenceFlow.getTargetFlowElement();

            if (targetFlowElement instanceof UserTask) {
                result.add((UserTask) targetFlowElement);
            }
        }
        return result;
    }


    public BufferedImage drawImage(String instanceId) {
        HistoricProcessInstance instance = historyService.createHistoricProcessInstanceQuery().processInstanceId(instanceId).singleResult();


        List<String> highLightedList = this.getHighLightedList(instance);
        BpmnModel bpmnModel = repositoryService.getBpmnModel(instance.getProcessDefinitionId());


        DefaultProcessDiagramGenerator generator = new DefaultProcessDiagramGenerator();

        double scaleFactor = 1.0;
        BufferedImage img = generator.generateImage(bpmnModel,
                "jpg",
                highLightedList,
                highLightedList,
                FONT_NAME, FONT_NAME, FONT_NAME,
                null, scaleFactor,
                true);
        return img;
    }


    // 获取应该高亮的节点及线
    public List<String> getHighLightedList(HistoricProcessInstance instance) {

        // 获取实例历史节点
        List<HistoricActivityInstance> list = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(instance.getId())
                .finished()
                .orderByHistoricActivityInstanceStartTime().asc()
                .list();


        // 不显示回退节点
        List<String> deleteList = new LinkedList<>();
        for (int i = 0; i < list.size(); i++) {
            HistoricActivityInstance act = list.get(i);
            String deleteReason = act.getDeleteReason();
            if (deleteReason == null) {
                continue;
            }

            // 回退的删除原因： Change activity to Activity_03ve4yj
            if (deleteReason.startsWith("Change activity to ")) {
                // 回退的目标节点
                String toActivity = deleteReason.replace("Change activity to ", "");

                // 将目标节点和当前节点之间的元素都加入到删除列表
                for (int j = i; j > 0; j--) {
                    HistoricActivityInstance pre = list.get(j);
                    String preActivityId = pre.getActivityId();
                    deleteList.add(preActivityId);
                    if (preActivityId.equals(toActivity)) {
                        break;
                    }
                }

            }

        }
        return list.stream().map(HistoricActivityInstance::getActivityId).filter(id -> !deleteList.contains(id)).collect(Collectors.toList());
    }


}
