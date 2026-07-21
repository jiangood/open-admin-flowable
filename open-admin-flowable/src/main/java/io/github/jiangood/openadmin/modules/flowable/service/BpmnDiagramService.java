package io.github.jiangood.openadmin.modules.flowable.service;


import io.github.jiangood.openadmin.modules.flowable.constant.FlowableConstants;
import io.github.jiangood.openadmin.util.FontTool;
import lombok.AllArgsConstructor;
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
 * BPMN 流程图操作：查询节点、生成高亮流程图
 */
@Service
@AllArgsConstructor
public class BpmnDiagramService {

    public static final String FONT_NAME = FontTool.getDefaultFontName();

    private final RuntimeService runtimeService;
    private final RepositoryService repositoryService;
    private final HistoryService historyService;

    /**
     * 查询任务的上一个节点
     */
    public List<UserTask> findPreActivity(Task task) {
        if (!(runtimeService.createExecutionQuery().executionId(task.getExecutionId()).singleResult() instanceof ExecutionEntity execution)) { return new ArrayList<>(); }
        String activityId = execution.getActivityId();

        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        FlowNode flowNode = (FlowNode) bpmnModel.getFlowElement(activityId);

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
     */
    public List<UserTask> findNextTaskList(Task task) {
        if (!(runtimeService.createExecutionQuery().executionId(task.getExecutionId()).singleResult() instanceof ExecutionEntity execution)) { return new ArrayList<>(); }
        String activityId = execution.getActivityId();

        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        FlowNode flowNode = (FlowNode) bpmnModel.getFlowElement(activityId);

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

        List<String> highlightedList = this.getHighlightedList(instance);
        BpmnModel bpmnModel = repositoryService.getBpmnModel(instance.getProcessDefinitionId());

        DefaultProcessDiagramGenerator generator = new DefaultProcessDiagramGenerator();

        double scaleFactor = 1.0;
        return generator.generateImage(bpmnModel,
                "jpg",
                highlightedList,
                highlightedList,
                FONT_NAME, FONT_NAME, FONT_NAME,
                null, scaleFactor,
                true);
    }

    // 获取应该高亮的节点及线
    public List<String> getHighlightedList(HistoricProcessInstance instance) {
        List<HistoricActivityInstance> list = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(instance.getId())
                .finished()
                .orderByHistoricActivityInstanceStartTime().asc()
                .list();

        List<String> deleteList = new LinkedList<>();
        for (int i = 0; i < list.size(); i++) {
            HistoricActivityInstance act = list.get(i);
            String deleteReason = act.getDeleteReason();
            if (deleteReason == null) continue;

            if (deleteReason.startsWith(FlowableConstants.DELETE_REASON_CHANGE_ACTIVITY_PREFIX)) {
                String toActivity = deleteReason.substring(FlowableConstants.DELETE_REASON_CHANGE_ACTIVITY_PREFIX.length());
                for (int j = i; j > 0; j--) {
                    HistoricActivityInstance pre = list.get(j);
                    String preActivityId = pre.getActivityId();
                    deleteList.add(preActivityId);
                    if (preActivityId.equals(toActivity)) break;
                }
            }
        }
        return list.stream()
                .map(HistoricActivityInstance::getActivityId)
                .filter(id -> !deleteList.contains(id))
                .collect(Collectors.toList());
    }

}
