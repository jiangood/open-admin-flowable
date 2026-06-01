package io.github.jiangood.openadmin.modules.flowable.service;

import io.github.jiangood.openadmin.framework.config.security.LoginUser;
import io.github.jiangood.openadmin.modules.flowable.dto.response.CommentResponse;
import org.flowable.engine.HistoryService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.task.Comment;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.awt.image.BufferedImage;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Answers.RETURNS_SELF;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserTaskServiceTest {

    @Mock
    private TaskService taskService;
    @Mock
    private HistoryService historyService;
    @Mock
    private ProcessService processService;

    private UserTaskService service;

    void initService() {
        service = new UserTaskService(taskService, historyService, processService);
    }

    @Nested
    class QueryMyInstanceTests {

        @Test
        void shouldQueryAndConvertInstances() {
            HistoricProcessInstance instance = mock();
            lenient().when(instance.getId()).thenReturn("inst1");
            lenient().when(instance.getName()).thenReturn("请假流程");
            lenient().when(instance.getProcessDefinitionName()).thenReturn("请假流程");
            lenient().when(instance.getProcessDefinitionKey()).thenReturn("leave_request");
            lenient().when(instance.getBusinessKey()).thenReturn("BIZ001");
            lenient().when(instance.getStartUserId()).thenReturn("U001");
            lenient().when(instance.getStartTime()).thenReturn(new Date());
            lenient().when(instance.getEndTime()).thenReturn(null);
            lenient().when(instance.getDeleteReason()).thenReturn(null);

            HistoricProcessInstanceQuery query = mock(HistoricProcessInstanceQuery.class, RETURNS_DEEP_STUBS);
            when(query.count()).thenReturn(1L);
            when(query.listPage(0, 10)).thenReturn(List.of(instance));
            when(historyService.createHistoricProcessInstanceQuery()).thenReturn(query);
            when(processService.getUserName("U001")).thenReturn("张三");

            LoginUser user = mock(LoginUser.class);
            when(user.getId()).thenReturn("U001");

            initService();
            Page<Map<String, Object>> result = service.queryMyInstance(PageRequest.of(0, 10), user);

            assertEquals(1, result.getTotalElements());
            Map<String, Object> item = result.getContent().getFirst();
            assertEquals("inst1", item.get("id"));
            assertEquals("请假流程", item.get("name"));
            assertEquals("请假流程", item.get("processDefinitionName"));
            assertEquals("BIZ001", item.get("businessKey"));
            assertEquals("张三", item.get("startUserName"));
            assertNotNull(item.get("startTime"));
            assertNull(item.get("endTime"));
            assertNull(item.get("deleteReason"));

            verify(query).startedBy("U001");
            verify(query).orderByProcessInstanceStartTime();
            verify(query).includeProcessVariables();
        }

        @Test
        void whenNoInstances_shouldReturnEmptyPage() {
            HistoricProcessInstanceQuery query = mock(HistoricProcessInstanceQuery.class, RETURNS_DEEP_STUBS);
            when(query.count()).thenReturn(0L);
            when(query.listPage(0, 10)).thenReturn(List.of());
            when(historyService.createHistoricProcessInstanceQuery()).thenReturn(query);

            LoginUser user = mock(LoginUser.class);
            when(user.getId()).thenReturn("U001");

            initService();
            Page<Map<String, Object>> result = service.queryMyInstance(PageRequest.of(0, 10), user);

            assertTrue(result.isEmpty());
            assertEquals(0, result.getTotalElements());
        }

        @Test
        void startedByAndOrderByTimeAreCalled() {
            HistoricProcessInstanceQuery query = mock(HistoricProcessInstanceQuery.class, RETURNS_DEEP_STUBS);
            when(query.count()).thenReturn(0L);
            when(query.listPage(0, 10)).thenReturn(List.of());
            when(historyService.createHistoricProcessInstanceQuery()).thenReturn(query);

            LoginUser user = mock(LoginUser.class);
            when(user.getId()).thenReturn("U002");

            initService();
            service.queryMyInstance(PageRequest.of(0, 10), user);

            verify(query).startedBy("U002");
            verify(query).orderByProcessInstanceStartTime();
            verify(query).includeProcessVariables();
        }
    }

    @Nested
    class QueryInstanceInfoTests {

        @Test
        void shouldReturnFullInfoWithCommentsAndDiagram() {
            HistoricProcessInstance instance = mock();
            lenient().when(instance.getId()).thenReturn("inst1");
            lenient().when(instance.getName()).thenReturn("请假流程");
            lenient().when(instance.getProcessDefinitionName()).thenReturn("请假流程");
            lenient().when(instance.getProcessDefinitionKey()).thenReturn("leave_request");
            lenient().when(instance.getBusinessKey()).thenReturn("BIZ001");
            lenient().when(instance.getStartUserId()).thenReturn("U001");
            lenient().when(instance.getStartTime()).thenReturn(new Date());
            lenient().when(instance.getEndTime()).thenReturn(null);
            lenient().when(instance.getDeleteReason()).thenReturn(null);

            HistoricProcessInstanceQuery query = mock(HistoricProcessInstanceQuery.class, RETURNS_DEEP_STUBS);
            when(query.listPage(0, 1)).thenReturn(List.of(instance));
            when(historyService.createHistoricProcessInstanceQuery()).thenReturn(query);

            Comment comment = mock();
            when(comment.getTime()).thenReturn(new Date());
            when(comment.getFullMessage()).thenReturn("同意");
            when(comment.getId()).thenReturn("c1");
            when(comment.getUserId()).thenReturn("U001");
            when(taskService.getProcessInstanceComments("inst1")).thenReturn(List.of(comment));
            when(processService.getUserName(any())).thenReturn("张三");
            when(processService.drawImage("inst1"))
                    .thenReturn(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));

            initService();
            Map<String, Object> result = service.queryInstanceInfo("inst1");

            assertEquals("请假流程", result.get("name"));
            assertEquals("leave_request", result.get("processDefinitionKey"));
            assertEquals("BIZ001", result.get("businessKey"));
            assertEquals("张三", result.get("starter"));
            assertEquals("inst1", result.get("id"));
            assertNotNull(result.get("startTime"));
            assertNotNull(result.get("img"));

            @SuppressWarnings("unchecked")
            List<CommentResponse> comments = (List<CommentResponse>) result.get("commentList");
            assertEquals(1, comments.size());
            assertEquals("同意", comments.getFirst().getContent());
            assertEquals("张三", comments.getFirst().getUser());

            @SuppressWarnings("unchecked")
            List<CommentResponse> instanceComments = (List<CommentResponse>) result.get("instanceCommentList");
            assertSame(comments, instanceComments);
        }

        @Test
        void whenNoInstance_shouldThrow() {
            HistoricProcessInstanceQuery query = mock(HistoricProcessInstanceQuery.class, RETURNS_DEEP_STUBS);
            when(query.listPage(0, 1)).thenReturn(List.of());
            when(historyService.createHistoricProcessInstanceQuery()).thenReturn(query);

            initService();
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> service.queryInstanceInfo("nonexistent"));
            assertTrue(ex.getMessage().contains("暂无流程信息"));
        }

        @Test
        void whenImageFails_shouldSetImgNull() {
            HistoricProcessInstance instance = mock();
            lenient().when(instance.getId()).thenReturn("inst1");
            lenient().when(instance.getName()).thenReturn(null);
            lenient().when(instance.getProcessDefinitionName()).thenReturn("请假流程");
            lenient().when(instance.getProcessDefinitionKey()).thenReturn("leave_request");
            lenient().when(instance.getBusinessKey()).thenReturn("BIZ001");
            lenient().when(instance.getStartUserId()).thenReturn("U001");
            lenient().when(instance.getStartTime()).thenReturn(new Date());

            HistoricProcessInstanceQuery query = mock(HistoricProcessInstanceQuery.class, RETURNS_DEEP_STUBS);
            when(query.listPage(0, 1)).thenReturn(List.of(instance));
            when(historyService.createHistoricProcessInstanceQuery()).thenReturn(query);
            when(taskService.getProcessInstanceComments("inst1")).thenReturn(List.of());
            when(processService.getUserName(any())).thenReturn("用户");
            when(processService.drawImage("inst1")).thenThrow(new RuntimeException("draw failed"));

            initService();
            Map<String, Object> result = service.queryInstanceInfo("inst1");

            assertEquals("请假流程", result.get("name"));
            assertNull(result.get("img"));
        }

        @Test
        void whenNameNull_shouldUseProcessDefinitionName() {
            HistoricProcessInstance instance = mock();
            lenient().when(instance.getId()).thenReturn("inst1");
            lenient().when(instance.getName()).thenReturn(null);
            lenient().when(instance.getProcessDefinitionName()).thenReturn("请假流程");
            lenient().when(instance.getProcessDefinitionKey()).thenReturn("leave_request");
            lenient().when(instance.getBusinessKey()).thenReturn("BIZ001");
            lenient().when(instance.getStartUserId()).thenReturn("U001");
            lenient().when(instance.getStartTime()).thenReturn(new Date());

            HistoricProcessInstanceQuery query = mock(HistoricProcessInstanceQuery.class, RETURNS_DEEP_STUBS);
            when(query.listPage(0, 1)).thenReturn(List.of(instance));
            when(historyService.createHistoricProcessInstanceQuery()).thenReturn(query);
            when(taskService.getProcessInstanceComments("inst1")).thenReturn(List.of());
            when(processService.getUserName("U001")).thenReturn("张三");
            when(processService.drawImage("inst1"))
                    .thenReturn(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));

            initService();
            Map<String, Object> result = service.queryInstanceInfo("inst1");

            assertEquals("请假流程", result.get("name"));
        }
    }

    @Nested
    class GetInstanceInfoByTaskTests {

        @Test
        void shouldReturnInfoWithFormKey() {
            HistoricProcessInstance histInst = mock();
            lenient().when(histInst.getId()).thenReturn("inst1");
            lenient().when(histInst.getName()).thenReturn("请假流程");
            lenient().when(histInst.getProcessDefinitionName()).thenReturn("请假流程");
            lenient().when(histInst.getProcessDefinitionKey()).thenReturn("leave_request");
            lenient().when(histInst.getBusinessKey()).thenReturn("BIZ001");
            lenient().when(histInst.getStartUserId()).thenReturn("U001");
            lenient().when(histInst.getStartTime()).thenReturn(new Date());

            HistoricProcessInstanceQuery detailQuery = mock(HistoricProcessInstanceQuery.class, RETURNS_DEEP_STUBS);
            when(detailQuery.listPage(0, 1)).thenReturn(List.of(histInst));
            when(historyService.createHistoricProcessInstanceQuery()).thenReturn(detailQuery);

            Task task = mock();
            when(task.getProcessInstanceId()).thenReturn("inst1");
            when(task.getFormKey()).thenReturn("manager_approve_form");

            TaskQuery taskQuery = mock(TaskQuery.class, RETURNS_SELF);
            when(taskQuery.singleResult()).thenReturn(task);
            when(taskService.createTaskQuery()).thenReturn(taskQuery);

            when(taskService.getProcessInstanceComments("inst1")).thenReturn(List.of());
            when(processService.getUserName(any())).thenReturn("用户");
            when(processService.drawImage("inst1"))
                    .thenReturn(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));

            initService();
            Map<String, Object> result = service.getInstanceInfoByTask("task1");

            assertEquals("manager_approve_form", result.get("formKey"));
            assertEquals("task1", result.get("taskId"));
            assertEquals("inst1", result.get("id"));
        }

        @Test
        void whenFormKeyNull_shouldUseGlobalFormKey() {
            HistoricProcessInstance histInst = mock();
            lenient().when(histInst.getId()).thenReturn("inst1");
            lenient().when(histInst.getName()).thenReturn("请假流程");
            lenient().when(histInst.getProcessDefinitionName()).thenReturn("请假流程");
            lenient().when(histInst.getProcessDefinitionKey()).thenReturn("leave_request");
            lenient().when(histInst.getBusinessKey()).thenReturn("BIZ001");
            lenient().when(histInst.getStartUserId()).thenReturn("U001");
            lenient().when(histInst.getStartTime()).thenReturn(new Date());

            HistoricProcessInstanceQuery detailQuery = mock(HistoricProcessInstanceQuery.class, RETURNS_DEEP_STUBS);
            when(detailQuery.listPage(0, 1)).thenReturn(List.of(histInst));
            when(historyService.createHistoricProcessInstanceQuery()).thenReturn(detailQuery);

            Task task = mock();
            when(task.getProcessInstanceId()).thenReturn("inst1");
            when(task.getFormKey()).thenReturn(null);
            when(task.getProcessVariables()).thenReturn(Map.of("GLOBAL_FORM_KEY", "global_form"));

            TaskQuery taskQuery = mock(TaskQuery.class, RETURNS_SELF);
            when(taskQuery.singleResult()).thenReturn(task);
            when(taskService.createTaskQuery()).thenReturn(taskQuery);

            when(taskService.getProcessInstanceComments("inst1")).thenReturn(List.of());
            when(processService.getUserName(any())).thenReturn("用户");
            when(processService.drawImage("inst1"))
                    .thenReturn(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));

            initService();
            Map<String, Object> result = service.getInstanceInfoByTask("task1");

            assertEquals("global_form", result.get("formKey"));
        }

        @Test
        void whenFormKeyAndGlobalFormKeyNull_shouldUseProcessDefinitionKey() {
            HistoricProcessInstance histInst = mock();
            lenient().when(histInst.getId()).thenReturn("inst1");
            lenient().when(histInst.getName()).thenReturn("请假流程");
            lenient().when(histInst.getProcessDefinitionName()).thenReturn("请假流程");
            lenient().when(histInst.getProcessDefinitionKey()).thenReturn("leave_request");
            lenient().when(histInst.getBusinessKey()).thenReturn("BIZ001");
            lenient().when(histInst.getStartUserId()).thenReturn("U001");
            lenient().when(histInst.getStartTime()).thenReturn(new Date());

            HistoricProcessInstanceQuery detailQuery = mock(HistoricProcessInstanceQuery.class, RETURNS_DEEP_STUBS);
            when(detailQuery.listPage(0, 1)).thenReturn(List.of(histInst));

            HistoricProcessInstanceQuery compatQuery = mock(HistoricProcessInstanceQuery.class, RETURNS_DEEP_STUBS);
            when(compatQuery.processInstanceId("inst1").singleResult()).thenReturn(histInst);

            when(historyService.createHistoricProcessInstanceQuery())
                    .thenReturn(detailQuery)
                    .thenReturn(compatQuery);

            Task task = mock();
            when(task.getProcessInstanceId()).thenReturn("inst1");
            when(task.getFormKey()).thenReturn(null);
            when(task.getProcessVariables()).thenReturn(Map.of());

            TaskQuery taskQuery = mock(TaskQuery.class, RETURNS_SELF);
            when(taskQuery.singleResult()).thenReturn(task);
            when(taskService.createTaskQuery()).thenReturn(taskQuery);

            when(taskService.getProcessInstanceComments("inst1")).thenReturn(List.of());
            when(processService.getUserName(any())).thenReturn("用户");
            when(processService.drawImage("inst1"))
                    .thenReturn(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));

            initService();
            Map<String, Object> result = service.getInstanceInfoByTask("task1");

            assertEquals("leave_request", result.get("formKey"));
        }

        @Test
        void whenTaskIdNull_shouldThrow() {
            initService();
            assertThrows(IllegalArgumentException.class, () -> service.getInstanceInfoByTask(null));
        }
    }
}
