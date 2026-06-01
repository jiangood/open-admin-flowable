# 前端代码组织重构设计文档

日期: 2026-06-02
状态: 草稿

## 概述

在 2026-06-01 首次重构（组件提取）基础上，进一步对 `open-admin-flowable` 前端代码进行代码组织重构，聚焦常量集中、目录结构统一、组件合理归位、错误处理规范化。

**原则**：不改变外部行为、不引入新依赖、不升级技术栈。

---

## 1. 常量集中管理

### 1.1 API URL 常量

新建 `web/src/constants/api.js`，按模块分组导出所有 API 路径。

```js
// admin/flowable/model/
export const MODEL_PAGE = 'admin/flowable/model/page'
export const MODEL_DETAIL = 'admin/flowable/model/detail'
export const MODEL_CREATE = 'admin/flowable/model/create'
export const MODEL_SAVE_CONTENT = 'admin/flowable/model/saveContent'
export const MODEL_DEPLOY = 'admin/flowable/model/deploy'
export const MODEL_DELETE = 'admin/flowable/model/delete'
export const MODEL_GET_DEFINITION_CONTENT = 'admin/flowable/model/getDefinitionContent'
export const MODEL_DEFINITION_PAGE = 'admin/flowable/model/definitionPage'
export const MODEL_FORM_OPTIONS = 'admin/flowable/model/formOptions'
export const MODEL_JAVA_DELEGATE_OPTIONS = 'admin/flowable/model/javaDelegateOptions'
export const MODEL_VAR_LIST = 'admin/flowable/model/varList'
export const MODEL_ASSIGNEE_OPTIONS = 'admin/flowable/model/assigneeOptions'
export const MODEL_CANDIDATE_GROUPS_OPTIONS = 'admin/flowable/model/candidateGroupsOptions'
export const MODEL_CANDIDATE_USERS_OPTIONS = 'admin/flowable/model/candidateUsersOptions'

// admin/flowable/simulate/
export const SIMULATE_GET = 'admin/flowable/simulate/get'
export const SIMULATE_USERS = 'admin/flowable/simulate/users'
export const SIMULATE_LIST = 'admin/flowable/simulate/list'
export const SIMULATE_START = 'admin/flowable/simulate/start'
export const SIMULATE_STATUS = 'admin/flowable/simulate/status'
export const SIMULATE_TASK_HANDLE = 'admin/flowable/simulate/task/handle'
export const SIMULATE_DELETE = 'admin/flowable/simulate/delete'

// admin/flowable/user-task/
export const USER_TASK_TODO_PAGE = 'admin/flowable/user-task/todoTaskPage'
export const USER_TASK_DONE_PAGE = 'admin/flowable/user-task/doneTaskPage'
export const USER_TASK_MY_INSTANCE = 'admin/flowable/user-task/myInstance'
export const USER_TASK_GET_INSTANCE_INFO = 'admin/flowable/user-task/getInstanceInfo'
export const USER_TASK_GET_INSTANCE_INFO_BY_TASK_ID = 'admin/flowable/user-task/getInstanceInfoByTaskId'
export const USER_TASK_HANDLE_TASK = 'admin/flowable/user-task/handleTask'

// admin/flowable/monitor/
export const MONITOR_PROCESS_INSTANCE_CLOSE = 'admin/flowable/monitor/processInstance/close'
export const MONITOR_INSTANCE_PAGE = 'admin/flowable/monitor/instancePage'
export const MONITOR_SET_ASSIGNEE = 'admin/flowable/monitor/setAssignee'
export const MONITOR_TASK = 'admin/flowable/monitor/task'
export const MONITOR_DEFINITION_PAGE = 'admin/flowable/monitor/definitionPage'
export const MONITOR_INSTANCE_VARS = 'admin/flowable/monitor/instance/vars'

// admin/flowable/example/
export const EXAMPLE_LEAVE_LIST = 'admin/flowable/example/leave/list'
export const EXAMPLE_LEAVE_DETAIL = 'admin/flowable/example/leave/detail'
export const EXAMPLE_LEAVE_START = 'admin/flowable/example/leave/start'
```

### 1.2 路由路径常量

新建 `web/src/constants/routes.js`。

```js
export const ROUTE_DESIGN = '/flowable/design'
export const ROUTE_SIMULATE = '/flowable/simulate'
export const ROUTE_TASK_FORM = '/flowable/user-task/form'
export const ROUTE_USER_INSTANCE_VIEW = '/flowable/user-task/instance/view'
export const ROUTE_MONITOR_TASK = '/flowable/monitor/task'
export const ROUTE_MONITOR_INSTANCE = '/flowable/monitor/instance'
export const ROUTE_MONITOR_DEFINITION = '/flowable/monitor/definition'
export const ROUTE_MONITOR_INSTANCE_VIEW = '/flowable/monitor/instance/view'
```

### 1.3 替换范围

涉及 15+ 个文件，每处 `HttpUtils.get/post('admin/flowable/...')` 替换为 `HttpUtils.get(XXX)`，每处 `PageUtils.open('/flowable/...')` 替换为 `PageUtils.open(ROUTE_XXX + params, title)`。

---

## 2. 目录结构统一

将 `monitor/` 下平级文件移入子目录，与 `instance/` 保持一致。

```
调整前:
  monitor/
    definition.jsx
    task.jsx
    instance/
      index.jsx
      view.jsx

调整后:
  monitor/
    definition/
      index.jsx    ← 从 definition.jsx 移入
    task/
      index.jsx    ← 从 task.jsx 移入
    instance/
      index.jsx
      view.jsx
```

UmiJS 文件系统路由约定：`definition.jsx` 和 `definition/index.jsx` 均映射到同一路由 `/flowable/monitor/definition`，移动安全无影响。同理适用于 `task.jsx` → `task/index.jsx`。

---

## 3. 组件合理归位

### 3.1 user-task 表格组件提取

`user-task/index.jsx`（160 行）中内联定义了三个 ProTable，提取到 `src/components/flowable/` 下：

| 新文件 | 职责 |
|--------|------|
| `src/components/flowable/todo-table.jsx` | 待办任务 ProTable |
| `src/components/flowable/done-table.jsx` | 已办任务 ProTable |
| `src/components/flowable/my-table.jsx` | 我发起 ProTable |

`user-task/index.jsx` 精简为路由入口，仅保留 tab 切换逻辑，三个 tab 分别引用上述组件。

### 3.2 history-list-panel 提取

`simulate/InitPhase.jsx` 中内联定义 `HistoryListPanel`，提取到 `src/components/flowable/history-list-panel.jsx`。

---

## 4. Bug 修复

`web/src/pages/flowable/monitor/instance/index.jsx` 第 55 行：

```js
// 修改前: 缺少 record.id
PageUtils.open('/flowable/monitor/instance/view?id=', '查看流程')

// 修改后
PageUtils.open(ROUTE_MONITOR_INSTANCE_VIEW + '?id=' + record.id, '查看流程')
```

---

## 5. 错误处理统一

当前 `.catch()` 使用情况：

| 文件 | 行数 | 问题 |
|------|------|------|
| `simulate/index.jsx` | L53,63,74,118 | L53 为静默 `.catch(() => {})`，其余为 `.catch(e => {...})`，内部处理不一致 |
| `user-task/form.jsx` | L37 | `.catch(e => {...})` |
| `monitor/instance/index.jsx` | L66 | `HttpUtils.get('.../close')` 仅 `.then()`，**缺 catch** |
| `monitor/task/index.jsx` | L21 | `HttpUtils.post('.../setAssignee')` 仅 `.then()`，**缺 catch** |

> ProTable 的 `request` prop 内部已处理错误，无需额外 catch。

对上述 **有 catch 的改为统一格式**，**缺 catch 的补上**：

```js
.catch(e => message.error(e?.message || '操作失败'))
```

---

## 6. 变更清单

| 变更项 | 类型 | 新增文件 | 修改文件 | 移动文件 |
|--------|------|---------|---------|---------|
| 常量文件 | 新增 | 2 | 0 | 0 |
| monitor 目录统一 | 移动 | 0 | 0 | 2 |
| 表格组件提取 | 新增+修改 | 4 | 1 | 0 |
| history-list-panel 提取 | 新增+修改 | 1 | 1 | 0 |
| bug 修复 | 修改 | 0 | 1 | 0 |
| 错误处理统一 | 修改 | 0 | 6 | 0 |
| 引用常量替换 | 修改 | 0 | 15+ | 0 |

总计：约 7 个新文件，20+ 个文件修改。

---

## 7. 不变事项

- 不改变 UI 表现
- 不改变功能逻辑
- 不引入 TypeScript
- 不引入状态管理
- 不改变 Class Component 模式
- 不引入新依赖
