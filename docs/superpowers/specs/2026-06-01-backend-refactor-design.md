# Backend Refactor: Package Reorganization & Code Classification

Date: 2026-06-01

## Motivation

The current backend has a flat layered structure (controller/service/dto/domain all in top-level packages) which makes it hard to locate code related to a given feature. Specific issues:

- `ModelController.java` (259 lines) mixes model CRUD, deployment, and unrelated option-queries
- `service/` package holds 7 classes covering disparate domains (process, task, monitor, simulate, diagram)
- `domain/` package mixes YAML definition metadata with no clear boundary from runtime DTOs
- `dto/` package is split into sub-packages but still flat — DTOs belong to specific modules

## Design

### Package Structure

Replace flat layered packaging with module-based packaging. Each business domain gets its own package containing its Controller, Service(s), and domain-specific DTOs.

```
io.github.jiangood.openadmin.modules.flowable
├── model/                  ← Process model management
├── process/                ← Process definition metadata + core process operations
├── task/                   ← User task (todo/done/my-instances)
├── monitor/                ← Admin monitoring
├── simulate/               ← Process simulation (test)
├── diagram/                ← BPMN diagram generation and analysis
├── config/                 ← Auto-configuration, properties (unchanged)
├── listener/               ← Event listeners (unchanged)
├── enums/                  ← Enumerations (unchanged)
├── constant/               ← Constants (unchanged)
└── common/                 ← Shared code
    ├── dto/                ← Cross-module DTOs (HandleTaskRequest, TaskHandleType)
    └── utils/              ← Shared utilities (FlowablePageTool, ModelTool)
```

### Module Contents

**model/** — Process model CRUD, deployment, designer options
```
ModelController.java       ← Thin, delegates to services
ModelService.java          ← Business logic: page/detail/delete/saveContent/deploy/definition queries
ModelOptionsService.java   ← Designer option queries: javaDelegateOptions/formOptions/assigneeOptions/candidateGroupsOptions/candidateUsersOptions/varList
ModelPageVO.java           ← View object for model list page
ModelRequest.java          ← Request record (extracted from inner record)
```

**process/** — YAML process definitions + core process runtime
```
ProcessMeta.java           ← YAML-defined process metadata (moved from domain/)
ProcessVariable.java       ← YAML-defined variable definition (moved from domain/)
FormDefinition.java        ← YAML-defined form definition (moved from domain/)
ProcessMetaService.java    ← Reads flowable-process-definition*.yml, caches ProcessMeta
ProcessService.java        ← Core process ops: start/approve/reject/moveBack (keep current scope)
FlowableTemplate.java      ← Template for external biz code to start processes (moved from root)
```

**task/** — User-facing task operations
```
UserTaskController.java
UserTaskService.java       ← Todo task query, done task query, my-instance query, instance info
TaskResponse.java          ← Response DTO for task list
CommentResponse.java       ← Response DTO for comments
```

**monitor/** — Admin monitoring
```
MonitorController.java
MonitorService.java        ← Definition/instance/task queries, close instance, set assignee
MonitorTaskResponse.java   ← Response DTO for monitor task list
ProcessDefinitionVO.java   ← View object for process definition
ProcessInstanceVO.java     ← View object for process instance
SetAssigneeRequest.java    ← Request record (extracted from inner record)
```

**simulate/** — Process simulation (test)
```
SimulateController.java
SimulateService.java       ← Start/status/handle/list/delete simulation (kept as single service)
```

**diagram/** — BPMN graph operations
```
BpmnDiagramService.java    ← findPreActivity/findNextTaskList/drawImage/getHighlightedList
```

### ModelController Split

`ModelController.java` (259 lines) currently does three distinct things:

1. **Model CRUD + Deployment** → `ModelService`
2. **Designer option queries** → `ModelOptionsService`
3. **Request mapping** → stays in `ModelController` (thin)

This separates business logic from auxiliary queries. `ModelOptionsService` only queries for Spring beans (`JavaDelegate`, `SysUser`, `SysRole`, `ProcessMeta`) to feed the designer's PropertiesPanel dropdowns.

### Domain Model Separation Strategy

| Category | Current Location | New Location |
|---|---|---|
| YAML definition models (ProcessMeta, ProcessVariable, FormDefinition) | `domain/` | `process/` |
| Runtime view objects (ProcessDefinitionVO, ProcessInstanceVO, ModelPageVO) | `dto/vo/` | their respective modules |
| Runtime response DTOs (TaskResponse, CommentResponse, MonitorTaskResponse) | `dto/response/` | their respective modules |
| Cross-module request/type DTOs (HandleTaskRequest, TaskHandleType) | `dto/request/`, `dto/` | `common/dto/` |

### Module Internal Convention

Files within a module are flat (no sub-packages like `model/controller/`):

```
model/
├── ModelController.java
├── ModelService.java
├── ModelOptionsService.java
├── ModelPageVO.java
└── ModelRequest.java
```

Each module file count: 2-6 files. No module should exceed 6 files.

### Example Module Impact

Only `LeaveApplyController.java` imports from the starter:
- `import ...flowable.FlowableTemplate` → changes to `import ...flowable.process.FlowableTemplate`

`ProcessListener` and `ProcessEventType` imports remain unchanged (listener/ and enums/ stay).

### Test Impact

`UserTaskServiceTest.java` imports `CommentResponse` — path changes from `dto.response.CommentResponse` to `task.CommentResponse`.

## Summary of File Movements

| File | From | To |
|---|---|---|
| FlowableTemplate.java | root | process/ |
| ProcessMeta.java | domain/ | process/ |
| ProcessVariable.java | domain/ | process/ |
| FormDefinition.java | domain/ | process/ |
| ProcessMetaService.java | service/ | process/ |
| ProcessService.java | service/ | process/ |
| ProcessModelService.java | service/ | model/ |
| ModelController.java | controller/ | model/ |
| ModelPageVO.java | dto/vo/ | model/ |
| UserTaskController.java | controller/ | task/ |
| UserTaskService.java | service/ | task/ |
| TaskResponse.java | dto/response/ | task/ |
| CommentResponse.java | dto/response/ | task/ |
| MonitorController.java | controller/ | monitor/ |
| MonitorService.java | service/ | monitor/ |
| MonitorTaskResponse.java | dto/response/ | monitor/ |
| ProcessDefinitionVO.java | dto/vo/ | monitor/ |
| ProcessInstanceVO.java | dto/vo/ | monitor/ |
| SimulateController.java | controller/ | simulate/ |
| SimulateService.java | service/ | simulate/ |
| BpmnDiagramService.java | service/ | diagram/ |
| HandleTaskRequest.java | dto/request/ | common/dto/ |
| TaskHandleType.java | dto/ | common/dto/ |
| FlowablePageTool.java | utils/ | common/utils/ |
| ModelTool.java | utils/ | common/utils/ |
