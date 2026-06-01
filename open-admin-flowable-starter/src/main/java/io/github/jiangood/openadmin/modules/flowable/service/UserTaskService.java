package io.github.jiangood.openadmin.modules.flowable.service;

import cn.hutool.core.date.DateUtil;
import io.github.jiangood.openadmin.framework.config.security.LoginUser;
import io.github.jiangood.openadmin.util.ImgTool;
import io.github.jiangood.openadmin.util.PageTool;
import io.github.jiangood.openadmin.modules.flowable.dto.response.CommentResponse;
import io.github.jiangood.openadmin.modules.flowable.utils.FlowablePageTool;
import lombok.AllArgsConstructor;
import org.flowable.engine.HistoryService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.task.Comment;
import org.flowable.task.api.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.awt.image.BufferedImage;
import java.util.*;

@Service
@AllArgsConstructor
public class UserTaskService {

    private final TaskService taskService;
    private final HistoryService historyService;
    private final ProcessService processService;

    public Page<Map<String, Object>> queryMyInstance(Pageable pageable, LoginUser loginUser) {
        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();
        query.startedBy(loginUser.getId());
        query.orderByProcessInstanceStartTime().desc();
        query.includeProcessVariables();

        Page<HistoricProcessInstance> page = FlowablePageTool.queryPage(query, pageable);
        return PageTool.convert(page, instance -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", instance.getId());
            map.put("name", instance.getName());
            map.put("processDefinitionName", instance.getProcessDefinitionName());
            map.put("startTime", instance.getStartTime());
            map.put("endTime", instance.getEndTime());
            map.put("businessKey", instance.getBusinessKey());
            map.put("deleteReason", instance.getDeleteReason());
            String startUserId = instance.getStartUserId();
            if (startUserId != null) {
                map.put("startUserName", processService.getUserName(startUserId));
            }
            return map;
        });
    }

    public Map<String, Object> queryInstanceInfo(String processInstanceId) {
        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();
        query.processInstanceId(processInstanceId);
        query.notDeleted();
        query.includeProcessVariables()
                .orderByProcessInstanceStartTime()
                .desc();

        List<HistoricProcessInstance> list = query.listPage(0, 1);
        Assert.state(!list.isEmpty(), "暂无流程信息");
        HistoricProcessInstance instance = list.get(0);

        Map<String, Object> data = new HashMap<>();

        List<Comment> processInstanceComments = taskService.getProcessInstanceComments(processInstanceId);
        List<CommentResponse> commentList = processInstanceComments.stream()
                .sorted(Comparator.comparing(Comment::getTime))
                .map(c -> new CommentResponse(c, processService.getUserName(c.getUserId())))
                .toList();
        data.put("commentList", commentList);
        data.put("instanceCommentList", commentList);

        try {
            BufferedImage image = processService.drawImage(instance.getId());
            String base64 = ImgTool.toBase64DataUri(image);
            data.put("img", base64);
        } catch (Exception e) {
            data.put("img", null);
        }

        String instanceName = instance.getName();
        if (instanceName == null) {
            instanceName = instance.getProcessDefinitionName();
        }
        data.put("startTime", DateUtil.format(instance.getStartTime(), "yyyy-MM-dd HH:mm:ss"));
        data.put("starter", processService.getUserName(instance.getStartUserId()));
        data.put("name", instanceName);
        data.put("id", instance.getId());
        data.put("processDefinitionKey", instance.getProcessDefinitionKey());
        data.put("businessKey", instance.getBusinessKey());

        return data;
    }

    public Map<String, Object> getInstanceInfoByTask(String taskId) {
        Assert.notNull(taskId, "taskId不能为空");

        Task task = taskService.createTaskQuery().taskId(taskId).includeProcessVariables().singleResult();
        String processInstanceId = task.getProcessInstanceId();

        Map<String, Object> data = queryInstanceInfo(processInstanceId);

        String formKey = task.getFormKey();
        if (formKey == null) {
            formKey = (String) task.getProcessVariables().get("GLOBAL_FORM_KEY");
        }

        if (formKey == null) {
            HistoricProcessInstance instance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(processInstanceId).singleResult();
            formKey = instance.getProcessDefinitionKey();
        }

        data.put("formKey", formKey);
        data.put("taskId", taskId);

        return data;
    }
}
