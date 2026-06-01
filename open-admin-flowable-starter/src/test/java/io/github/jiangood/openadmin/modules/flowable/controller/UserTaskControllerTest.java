package io.github.jiangood.openadmin.modules.flowable.controller;

import io.github.jiangood.openadmin.framework.auth.LoginTool;
import io.github.jiangood.openadmin.framework.config.security.LoginUser;
import io.github.jiangood.openadmin.modules.flowable.dto.TaskHandleType;
import io.github.jiangood.openadmin.modules.flowable.dto.response.TaskResponse;
import io.github.jiangood.openadmin.modules.flowable.service.ProcessService;
import io.github.jiangood.openadmin.modules.flowable.service.UserTaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserTaskControllerTest {

    private MockMvc mockMvc;
    private UserTaskService userTaskService;
    private ProcessService processService;
    private MockedStatic<LoginTool> loginToolMock;

    private static final String TEST_USER_ID = "U001";
    private static final String TEST_USER_NAME = "测试用户";

    @BeforeEach
    void setUp() {
        userTaskService = mock(UserTaskService.class);
        processService = mock(ProcessService.class);
        UserTaskController controller = new UserTaskController(userTaskService, processService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        loginToolMock = mockStatic(LoginTool.class);
        loginToolMock.when(LoginTool::getUserId).thenReturn(TEST_USER_ID);

        LoginUser mockUser = mock(LoginUser.class);
        lenient().when(mockUser.getId()).thenReturn(TEST_USER_ID);
        lenient().when(mockUser.getName()).thenReturn(TEST_USER_NAME);
        loginToolMock.when(LoginTool::getUser).thenReturn(mockUser);
    }

    @AfterEach
    void tearDown() {
        if (loginToolMock != null) {
            loginToolMock.close();
        }
    }

    @Nested
    class TaskQueryTests {

        @Test
        void getTodoCount_shouldReturnCount() throws Exception {
            when(processService.findUserTaskCount(TEST_USER_ID)).thenReturn(5L);

            mockMvc.perform(get("/admin/flowable/user-task/todoCount"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(5));

            verify(processService).findUserTaskCount(TEST_USER_ID);
        }

        @Test
        void queryTodoTaskPage_shouldReturnPaginatedTasks() throws Exception {
            TaskResponse task = new TaskResponse();
            task.setId("task1");
            task.setTaskName("待办任务");
            Page<TaskResponse> page = new PageImpl<>(List.of(task), PageRequest.of(0, 10), 1);
            when(processService.findUserTaskList(any(), eq(TEST_USER_ID))).thenReturn(page);

            mockMvc.perform(get("/admin/flowable/user-task/todoTaskPage")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.content[0].taskName").value("待办任务"))
                    .andExpect(jsonPath("$.data.totalElements").value(1));

            verify(processService).findUserTaskList(any(), eq(TEST_USER_ID));
        }

        @Test
        void todoTaskPage_whenEmpty_shouldReturnEmptyPage() throws Exception {
            Page<TaskResponse> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
            when(processService.findUserTaskList(any(), eq(TEST_USER_ID))).thenReturn(emptyPage);

            mockMvc.perform(get("/admin/flowable/user-task/todoTaskPage")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.empty").value(true));
        }

        @Test
        void doneTaskPage_shouldReturnPaginatedDoneTasks() throws Exception {
            TaskResponse task = new TaskResponse();
            task.setId("task2");
            task.setTaskName("已办任务");
            Page<TaskResponse> page = new PageImpl<>(List.of(task), PageRequest.of(0, 10), 1);
            when(processService.findUserTaskDoneList(any(), eq(TEST_USER_ID))).thenReturn(page);

            mockMvc.perform(get("/admin/flowable/user-task/doneTaskPage")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.content[0].taskName").value("已办任务"));

            verify(processService).findUserTaskDoneList(any(), eq(TEST_USER_ID));
        }
    }

    @Nested
    class MyInstanceTests {

        @Test
        void myInstance_shouldReturnMyStartedInstances() throws Exception {
            Page<Map<String, Object>> page = new PageImpl<>(List.of(Map.of(
                    "id", "inst1", "businessKey", "BIZ001", "startUserName", TEST_USER_NAME
            )), PageRequest.of(0, 10), 1);
            when(userTaskService.queryMyInstance(any(), any())).thenReturn(page);

            mockMvc.perform(get("/admin/flowable/user-task/myInstance")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.content[0].id").value("inst1"))
                    .andExpect(jsonPath("$.data.content[0].businessKey").value("BIZ001"))
                    .andExpect(jsonPath("$.data.content[0].startUserName").value(TEST_USER_NAME));

            verify(userTaskService).queryMyInstance(any(), any());
        }

        @Test
        void myInstance_whenNoInstances_shouldReturnEmptyPage() throws Exception {
            Page<Map<String, Object>> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
            when(userTaskService.queryMyInstance(any(), any())).thenReturn(emptyPage);

            mockMvc.perform(get("/admin/flowable/user-task/myInstance")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.empty").value(true));
        }
    }

    @Nested
    class HandleTaskTests {

        @Test
        void handleTask_approve_shouldSucceed() throws Exception {
            mockMvc.perform(post("/admin/flowable/user-task/handleTask")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    { "result": "APPROVE", "taskId": "task1", "comment": "同意" }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("处理成功"));

            verify(processService).handle(eq(TEST_USER_ID), eq(TaskHandleType.APPROVE),
                    eq("task1"), eq("同意"), isNull());
        }

        @Test
        void handleTask_reject_shouldSucceed() throws Exception {
            mockMvc.perform(post("/admin/flowable/user-task/handleTask")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    { "result": "REJECT", "taskId": "task2", "comment": "不同意" }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(processService).handle(eq(TEST_USER_ID), eq(TaskHandleType.REJECT),
                    eq("task2"), eq("不同意"), isNull());
        }

        @Test
        void handleTask_withFormData_shouldSucceed() throws Exception {
            mockMvc.perform(post("/admin/flowable/user-task/handleTask")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    { "result": "APPROVE", "taskId": "task3", "comment": "批准",
                                      "formData": {"days": 3, "reason": "事假"} }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(processService).handle(eq(TEST_USER_ID), eq(TaskHandleType.APPROVE),
                    eq("task3"), eq("批准"), anyMap());
        }
    }

    @Nested
    class InstanceInfoTests {

        @Test
        void getInstanceInfo_byBusinessKey_shouldReturnInfo() throws Exception {
            HistoricProcessInstance mockInstance = mock(HistoricProcessInstance.class);
            when(mockInstance.getId()).thenReturn("inst1");
            when(processService.getLatestProcessInstance("BIZ001")).thenReturn(mockInstance);
            when(userTaskService.queryInstanceInfo("inst1")).thenReturn(Map.of(
                    "name", "测试流程", "starter", TEST_USER_NAME,
                    "id", "inst1", "businessKey", "BIZ001",
                    "processDefinitionKey", "leave_request"
            ));

            mockMvc.perform(get("/admin/flowable/user-task/getInstanceInfo")
                            .param("businessKey", "BIZ001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.name").value("测试流程"))
                    .andExpect(jsonPath("$.data.starter").value(TEST_USER_NAME))
                    .andExpect(jsonPath("$.data.id").value("inst1"))
                    .andExpect(jsonPath("$.data.businessKey").value("BIZ001"))
                    .andExpect(jsonPath("$.data.processDefinitionKey").value("leave_request"));

            verify(processService).getLatestProcessInstance("BIZ001");
            verify(userTaskService).queryInstanceInfo("inst1");
        }

        @Test
        void getInstanceInfo_byId_shouldReturnInfo() throws Exception {
            when(userTaskService.queryInstanceInfo("inst2")).thenReturn(Map.of(
                    "id", "inst2", "name", "直接查询"
            ));

            mockMvc.perform(get("/admin/flowable/user-task/getInstanceInfo")
                            .param("id", "inst2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value("inst2"))
                    .andExpect(jsonPath("$.data.name").value("直接查询"));
        }

        @Test
        void getInstanceInfoByTaskId_shouldReturnInfo() throws Exception {
            when(userTaskService.getInstanceInfoByTask("task1")).thenReturn(Map.of(
                    "formKey", "manager_approve_form", "taskId", "task1",
                    "id", "inst1", "starter", TEST_USER_NAME
            ));

            mockMvc.perform(get("/admin/flowable/user-task/getInstanceInfoByTaskId")
                            .param("taskId", "task1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.formKey").value("manager_approve_form"))
                    .andExpect(jsonPath("$.data.taskId").value("task1"))
                    .andExpect(jsonPath("$.data.id").value("inst1"))
                    .andExpect(jsonPath("$.data.starter").value(TEST_USER_NAME));

            verify(userTaskService).getInstanceInfoByTask("task1");
        }
    }
}
