# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

### Backend (Spring Boot 4.0.6 / Java 21 / Flowable 8.0.0)
```bash
mvnw spring-boot:run -pl open-admin-flowable-example  # Start on port 8082, context-path: /process
mvnw clean install                    # Build all modules
mvnw clean install -pl open-admin-flowable-starter -Ppublish  # Build and publish starter to Maven Central
```
Required: MySQL database `open-admin-process` on localhost:3306. DB config in `open-admin-flowable-example/src/main/resources/application.yml`.

### Frontend (UmiJS 4 / React 19 / Ant Design 6)
```bash
cd web
npm install       # Install dependencies
npm run dev       # Start dev server on port 8000 (proxies to localhost:8082)
```
Frontend .env at `web/.env` configures port, proxy target, and theme colors.

## Project Architecture

A **Flowable BPMN 2.0 workflow engine plugin** for the `open-admin` framework. Ships as a Maven library (`io.github.jiangood:open-admin-flowable-starter`) published to Maven Central, plus an optional Spring Boot app + frontend for development/testing.

### Backend — two Maven modules

- `open-admin-flowable-starter` — library published to Maven Central (`io.github.jiangood:open-admin-flowable-starter`)
- `open-admin-flowable-example` — runnable Spring Boot app for local development (not published)

Flowable engine integration layered as a Spring Boot auto-configuration module:

| Layer | Key Files | Role |
|---|---|---|
| **Auto Config** | `FlowableAutoConfiguration`, `FlowableConfig` | Conditionally configures Flowable engine (ID generator, event listeners) when `ProcessEngine` is on classpath |
| **Metadata** | `ProcessMeta`, `ProcessVariable`, `FormDefinition`, `ProcessListener` | Domain models for process definitions |
| **Meta Sources** | `IProcessMetaDao`, `ProcessMetaDaoYmlImpl` | Reads `flowable-process-definition*.yml` from classpath as process definitions |
| **Initializer** | `FlowableProcessInitializer` | On startup, creates Flowable Model entries for each YAML-defined process |
| **Event System** | `GlobalProcessListener`, `ProcessListener` interface, `ProcessEventType` | Global engine event listener dispatches to per-definition `ProcessListener` beans |
| **Core Service** | `ProcessService` | Start process instances, handle tasks (approve/reject), query todo/done tasks, model CRUD |
| **Model Service** | `BpmnDiagramService` | BPMN model graph operations (find prev/next nodes), generate process diagram images |
| **Controllers** | `ModelController`, `MonitorController`, `UserTaskController` | REST endpoints under `/admin/flowable/` (model CRUD/deploy, user task mgmt, admin monitoring) |
| **Properties** | `FlowableProperties` | `flowable.reject-type` — `DELETE` terminates flow on reject, `MOVE_BACK` rolls back to previous node |

#### Process Definition Flow
1. YAML files (`src/main/resources/data/flowable-process-definition*.yml`) define process key, name, listener class, variables, forms
2. On startup, `FlowableProcessInitializer` reads YAML → creates Flowable Model entries
3. Users visually design the BPMN via the designer UI → save model XML
4. Deploy saves XML + creates a Flowable Deployment (activates the process definition)
5. Business code calls `ProcessService.start(key, bizKey, variables)` to launch a process instance

#### Task Handling
- **APPROVE**: `taskService.complete(taskId)` — moves to next node
- **REJECT (DELETE)**: Delete the process instance
- **REJECT (MOVE_BACK)`: Rollback to previous `UserTask` node via `runtimeService.createChangeActivityStateBuilder()`

#### Auto-set Process Variables
When starting a process, these variables are automatically populated from `LoginUser`:
`userId`, `userName`, `unitId`, `unitName`, `deptId`, `deptName`, `INITIATOR_DEPT_LEADER`, `BUSINESS_KEY`, `GLOBAL_FORM_KEY`

### Frontend — `web/src/pages/flowable/`

Pages built with `@jiangood/open-admin` shared components (ProTable, HttpUtils, Page, PageUtils, LinkButton):

| Path | File | Description |
|---|---|---|
| `/flowable` | `index.jsx` | Process model list — design/delete models, navigate to monitor pages |
| `/flowable/design?id=` | `design/index.jsx` | BPMN visual designer with bpmn-js, custom PropertiesPanel, deploy/save/XML/test |
| `/flowable/task` | `task/index.jsx` | User task center — 3 tabs: todo, done, my-started instances |
| `/flowable/task/form?taskId=` | `task/form.jsx` | Task handling form |
| `/flowable/monitor/definition` | `monitor/definition.jsx` | Deployed process definitions |
| `/flowable/monitor/instance` | `monitor/instance/index.jsx` | Running process instances |
| `/flowable/monitor/task` | `monitor/task.jsx` | Running tasks (admin) |
| `/flowable/test?id=` | `test/index.jsx` | Process testing page |

The `design/` sub-pages contain a custom **PropertiesPanel** with sections for various BPMN element types:
- **General** (`GeneralSection.jsx`) — name, ID
- **User** (`AssignmentSection.jsx`) — assignee, candidate users/groups
- **Form** (`FormProps.jsx`) — form key assignment
- **Condition** (`ConditionProps.jsx`, `ConditionDesign.jsx`) — sequence flow conditions
- **Multi-instance** (`MultiInstanceProps.jsx`) — loop characteristics
- **Delegate** (`DelegateExpressionProps.jsx`) — JavaDelegate service tasks

### API Routes
All under prefix (configured in `.env` `SERVLET_CONTEXT=/process`):
- `admin/flowable/model/...` — Model CRUD, deploy, options (delegates, forms, users, roles)
- `admin/flowable/my/...` — User tasks (todo/done/my instances, handle, instance info)
- `admin/flowable/monitor/...` — Admin monitoring (definitions, instances, tasks, close)
- `admin/flowable/test/...` — Test endpoints

### Example Files (in `open-admin-flowable-example` only)
- `LeaveProcessListener` — `ProcessListener` implementation for leave-request workflow
- `DemoDelegate`, `DemoDelegate2` — `JavaDelegate` beans for service tasks
- `flowable-process-definition-example.yml` — Example YAML process definition
