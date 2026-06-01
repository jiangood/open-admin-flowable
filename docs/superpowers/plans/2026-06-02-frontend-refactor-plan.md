# 前端代码组织重构 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**目标：** 将前端代码中的硬编码 API URL 和路由路径集中为常量、统一 monitor/ 目录结构、提取内联组件到 `src/components/flowable/`、统一错误处理、修复一个 bug。

**架构：** 分层引入常量（API 路径 + 路由路径）替换所有散落字符串；按 UmiJS 约定重排目录；提取 `user-task/index.jsx` 中 3 个 ProTable 和内联 `HistoryListPanel` 到共享组件目录。

**技术栈：** UmiJS 4 / React 19 / Ant Design 6 / @jiangood/open-admin

---

### Task 1: 创建常量文件

**Files:**
- Create: `web/src/constants/api.js`
- Create: `web/src/constants/routes.js`

- [ ] **Step 1: Create `web/src/constants/api.js`**

```js
export const MODEL_PAGE = 'admin/flowable/model/page'
export const MODEL_DETAIL = 'admin/flowable/model/detail'
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

export const SIMULATE_GET = 'admin/flowable/simulate/get'
export const SIMULATE_USERS = 'admin/flowable/simulate/users'
export const SIMULATE_LIST = 'admin/flowable/simulate/list'
export const SIMULATE_START = 'admin/flowable/simulate/start'
export const SIMULATE_STATUS = 'admin/flowable/simulate/status'
export const SIMULATE_TASK_HANDLE = 'admin/flowable/simulate/task/handle'
export const SIMULATE_DELETE = 'admin/flowable/simulate/delete'

export const USER_TASK_TODO_PAGE = 'admin/flowable/user-task/todoTaskPage'
export const USER_TASK_DONE_PAGE = 'admin/flowable/user-task/doneTaskPage'
export const USER_TASK_MY_INSTANCE = 'admin/flowable/user-task/myInstance'
export const USER_TASK_GET_INSTANCE_INFO = 'admin/flowable/user-task/getInstanceInfo'
export const USER_TASK_GET_INSTANCE_INFO_BY_TASK_ID = 'admin/flowable/user-task/getInstanceInfoByTaskId'
export const USER_TASK_HANDLE_TASK = 'admin/flowable/user-task/handleTask'

export const MONITOR_PROCESS_INSTANCE_CLOSE = 'admin/flowable/monitor/processInstance/close'
export const MONITOR_INSTANCE_PAGE = 'admin/flowable/monitor/instancePage'
export const MONITOR_SET_ASSIGNEE = 'admin/flowable/monitor/setAssignee'
export const MONITOR_TASK = 'admin/flowable/monitor/task'
export const MONITOR_DEFINITION_PAGE = 'admin/flowable/monitor/definitionPage'
export const MONITOR_INSTANCE_VARS = 'admin/flowable/monitor/instance/vars'

export const EXAMPLE_LEAVE_LIST = 'admin/flowable/example/leave/list'
export const EXAMPLE_LEAVE_DETAIL = 'admin/flowable/example/leave/detail'
export const EXAMPLE_LEAVE_START = 'admin/flowable/example/leave/start'
```

- [ ] **Step 2: Create `web/src/constants/routes.js`**

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

- [ ] **Step 3: Verify both files parse correctly**

Run: `node -e "require('./web/src/constants/api'); require('./web/src/constants/routes'); console.log('OK')"`
Expected: `OK`

- [ ] **Step 4: Commit**

```bash
git add web/src/constants/
git commit -m "refactor(frontend): add centralized API URL and route constants"
```

---

### Task 2: 提取共享组件到 src/components/flowable/

**Files:**
- Create: `web/src/components/flowable/todo-table.jsx`
- Create: `web/src/components/flowable/done-table.jsx`
- Create: `web/src/components/flowable/my-table.jsx`
- Create: `web/src/components/flowable/HistoryListPanel.jsx`

- [ ] **Step 1: Create `web/src/components/flowable/todo-table.jsx`**

```jsx
import {Button} from "antd";
import {HttpUtils, LinkButton, ProTable} from "@jiangood/open-admin";
import React from "react";
import {USER_TASK_TODO_PAGE} from "@/constants/api";

const columns = [
    {title: '发起人', dataIndex: 'instanceStarter'},
    {title: '流程名称', dataIndex: 'instanceName'},
    {title: '当前节点', dataIndex: 'taskName', width: 100},
    {title: '当前操作人', dataIndex: 'assigneeInfo', width: 100},
    {title: '发起时间', dataIndex: 'instanceStartTime'},
    {title: '任务创建时间', dataIndex: 'createTime'},
    {
        title: '操作', dataIndex: 'option',
        render: (_, record) => {
            let path = '/flowable/user-task/form?taskId=' + record.id;
            return <LinkButton type='primary' path={path} label='处理任务'>处理</LinkButton>;
        },
    },
];

export default function TodoTable() {
    return <ProTable
        showToolbarSearch={false}
        request={(params) => HttpUtils.get(USER_TASK_TODO_PAGE, params)}
        columns={columns}
        size='small'
    />;
}
```

- [ ] **Step 2: Create `web/src/components/flowable/done-table.jsx`**

```jsx
import {Button} from "antd";
import {HttpUtils, PageUtils, ProTable} from "@jiangood/open-admin";
import React from "react";
import {USER_TASK_DONE_PAGE} from "@/constants/api";
import {ROUTE_USER_INSTANCE_VIEW} from "@/constants/routes";

const columns = [
    {title: '流程名称', dataIndex: 'instanceName'},
    {title: '发起人', dataIndex: 'instanceStarter'},
    {title: '发起时间', dataIndex: 'instanceStartTime'},
    {title: '任务创建时间', dataIndex: 'createTime'},
    {title: '处理时间', dataIndex: 'endTime'},
    {title: '耗时', dataIndex: 'durationInfo'},
    {title: '处理节点', dataIndex: 'taskName'},
    {title: '操作人', dataIndex: 'assigneeInfo'},
    {
        title: '操作', dataIndex: 'option',
        render: (_, record) => (
            <Button size='small' onClick={() => PageUtils.open(ROUTE_USER_INSTANCE_VIEW + '?id=' + record.id, '流程信息')}>查看</Button>
        ),
    },
];

export default function DoneTable() {
    return <ProTable
        showToolbarSearch={false}
        request={(params) => HttpUtils.get(USER_TASK_DONE_PAGE, params)}
        columns={columns}
        size='small'
    />;
}
```

- [ ] **Step 3: Create `web/src/components/flowable/my-table.jsx`**

```jsx
import {Button} from "antd";
import {HttpUtils, PageUtils, ProTable} from "@jiangood/open-admin";
import React from "react";
import {USER_TASK_MY_INSTANCE} from "@/constants/api";
import {ROUTE_USER_INSTANCE_VIEW} from "@/constants/routes";

const columns = [
    {
        title: '流程名称', dataIndex: 'processDefinitionName',
        render(_, r) { return r.name || r.processDefinitionName; }
    },
    {title: '发起人', dataIndex: 'startUserName'},
    {title: '发起时间', dataIndex: 'startTime'},
    {title: '业务标识', dataIndex: 'businessKey'},
    {title: '结束时间', dataIndex: 'endTime'},
    {
        title: '流程状态', dataIndex: 'x',
        render(_, row) { return row.endTime == null ? '进行中' : '已结束'; }
    },
    {title: '终止原因', dataIndex: 'deleteReason'},
    {
        title: '操作', dataIndex: 'option',
        render: (_, record) => (
            <Button size='small' onClick={() => PageUtils.open(ROUTE_USER_INSTANCE_VIEW + '?id=' + record.id, '流程信息')}>查看</Button>
        ),
    },
];

export default function MyTable() {
    return <ProTable
        request={(params) => HttpUtils.get(USER_TASK_MY_INSTANCE, params)}
        columns={columns}
    />;
}
```

- [ ] **Step 4: Create `web/src/components/flowable/HistoryListPanel.jsx`**

```jsx
import {Button, Empty, Spin, Table, Tag, Typography} from "antd";
import {HistoryOutlined} from "@ant-design/icons";

export default function HistoryListPanel({list, loading, onView, onDelete}) {
    return (
        <div style={{paddingLeft: 16}}>
            <Typography.Title level={5}>
                <HistoryOutlined/> 仿真历史
            </Typography.Title>
            {loading ? <Spin/> : list.length === 0 ? (
                <Empty description="暂无仿真记录" image={Empty.PRESENTED_IMAGE_SIMPLE}/>
            ) : (
                <Table dataSource={list}
                       size="small" pagination={false} rowKey="instanceId"
                       columns={[
                           {
                               title: '实例', dataIndex: 'name', ellipsis: true,
                               render: (text, record) => (
                                   <a onClick={() => onView(record.instanceId)}>{text}</a>
                               ),
                           },
                           {title: '发起人', dataIndex: 'starter', width: 80},
                           {
                               title: '状态', dataIndex: 'finished', width: 80,
                               render: (finished, record) => finished ? (
                                   record.deleteReason ? (
                                       <Tag color="error">已终止</Tag>
                                   ) : (
                                       <Tag color="success">已完成</Tag>
                                   )
                               ) : (
                                   <Tag color="processing">运行中</Tag>
                               ),
                           },
                           {title: '发起时间', dataIndex: 'startTime', width: 100},
                           {
                               title: '操作', width: 60,
                               render: (_, record) => (
                                   <Button type="link" danger size="small"
                                           onClick={() => onDelete(record.instanceId)}>
                                       删除
                                   </Button>
                               ),
                           },
                       ]}/>
            )}
        </div>
    );
}
```

- [ ] **Step 5: Commit**

```bash
git add web/src/components/flowable/
git commit -m "refactor(frontend): extract inline ProTables and HistoryListPanel"
```

---

### Task 3: Refactor user-task/index.jsx + form.jsx

**Files:**
- Modify: `web/src/pages/flowable/user-task/index.jsx`
- Modify: `web/src/pages/flowable/user-task/form.jsx`

- [ ] **Step 1: Replace `user-task/index.jsx` with slim tab entry**

```jsx
import React from "react";
import {Tabs} from "antd";
import {Page, PageLoading} from "@jiangood/open-admin";
import TodoTable from "@/components/flowable/todo-table";
import DoneTable from "@/components/flowable/done-table";
import MyTable from "@/components/flowable/my-table";

export default class extends React.Component {
    state = { show: true }

    render() {
        if (!this.state.show) {
            return <PageLoading/>
        }

        const items = [
            {label: '待办任务', key: '1', children: <TodoTable/>},
            {label: '已办任务', key: '2', children: <DoneTable/>},
            {label: '我发起的', key: '3', children: <MyTable/>},
        ];

        return <Page padding>
            <Tabs defaultActiveKey="1" destroyOnHidden items={items}/>
        </Page>
    }
}
```

- [ ] **Step 2: Update `user-task/form.jsx` — import constants, fix catch**

Replace imports to include constants:
```jsx
import React from "react";
import {Button, Card, Empty, Form, Input, message, Radio, Spin, Splitter, Table, Tabs, Typography} from "antd";
import ProcessImageViewer from "@/components/ProcessImageViewer";
import {history} from "umi";
import {FormRegistryUtils, Gap, HttpUtils, Page, PageUtils} from "@jiangood/open-admin";
import {FormOutlined, ShareAltOutlined} from "@ant-design/icons";
import {USER_TASK_GET_INSTANCE_INFO_BY_TASK_ID, USER_TASK_HANDLE_TASK} from "@/constants/api";
```

Replace line 35: `HttpUtils.get("admin/flowable/user-task/getInstanceInfoByTaskId", {taskId})`
→ `HttpUtils.get(USER_TASK_GET_INSTANCE_INFO_BY_TASK_ID, {taskId})`

Replace line 37-38 catch:
```jsx
}).catch(e => {
    this.setState({errorMsg: e})
```
→
```jsx
}).catch(e => {
    message.error(e?.message || '获取任务信息失败');
    this.setState({errorMsg: e})
```

Replace line 54: `await HttpUtils.post("admin/flowable/user-task/handleTask", value)`
→ `await HttpUtils.post(USER_TASK_HANDLE_TASK, value)`

Replace line 55: `PageUtils.closeCurrent()` — keep as is.

Replace line 56-58 catch:
```jsx
} catch (error) {
    message.error(error)
```
→
```jsx
} catch (error) {
    message.error(error?.message || '操作失败')
```

- [ ] **Step 3: Commit**

```bash
git add web/src/pages/flowable/user-task/
git commit -m "refactor(frontend): extract ProTables to components, add constants to user-task pages"
```

---

### Task 4: 重构 monitor/ 目录结构 + 常量 + 错误处理

**Files:**
- Move: `web/src/pages/flowable/monitor/definition.jsx` → `web/src/pages/flowable/monitor/definition/index.jsx`
- Move: `web/src/pages/flowable/monitor/task.jsx` → `web/src/pages/flowable/monitor/task/index.jsx`
- Modify: `web/src/pages/flowable/monitor/task/index.jsx`
- Modify: `web/src/pages/flowable/monitor/definition/index.jsx`
- Modify: `web/src/pages/flowable/monitor/instance/index.jsx`

- [ ] **Step 1: Move definition.jsx → definition/index.jsx**

```bash
mkdir web\src\pages\flowable\monitor\definition
git mv web\src\pages\flowable\monitor\definition.jsx web\src\pages\flowable\monitor\definition\index.jsx
```

- [ ] **Step 2: Move task.jsx → task/index.jsx**

```bash
mkdir web\src\pages\flowable\monitor\task
git mv web\src\pages\flowable\monitor\task.jsx web\src\pages\flowable\monitor\task\index.jsx
```

- [ ] **Step 3: Update `monitor/definition/index.jsx` — add constants + message import**

Imports:
```jsx
import React from "react";
import {HttpUtils, ProTable} from "@jiangood/open-admin";
import {MONITOR_DEFINITION_PAGE} from "@/constants/api";
```
Replace line 82:
`request={(params) => HttpUtils.get('admin/flowable/monitor/definitionPage', params)}`
→ `request={(params) => HttpUtils.get(MONITOR_DEFINITION_PAGE, params)}`

- [ ] **Step 4: Update `monitor/task/index.jsx` — add constants + catch**

Imports:
```jsx
import {FieldUserSelect, HttpUtils, Page, ProTable} from "@jiangood/open-admin";
import {Button, Form, message, Modal} from "antd";
import React from "react";
import {MONITOR_SET_ASSIGNEE, MONITOR_TASK} from "@/constants/api";
```

Replace line 21:
`HttpUtils.post('admin/flowable/monitor/setAssignee',values).then(()=>{`
→
`HttpUtils.post(MONITOR_SET_ASSIGNEE, values).then(() => {`
Add `.catch()` after line 23 `})`:
```jsx
        }).catch(e => {
            message.error(e?.message || '指定处理人失败');
```
Add `message` to import from antd.

Replace line 75:
`request={(params) => HttpUtils.get('admin/flowable/monitor/task', params)}`
→ `request={(params) => HttpUtils.get(MONITOR_TASK, params)}`

- [ ] **Step 5: Update `monitor/instance/index.jsx` — constants + catch + fix bug**

Imports:
```jsx
import {Button, message, Popconfirm, Space} from "antd";
import {HttpUtils, PageUtils, ProTable} from "@jiangood/open-admin";
import React from "react";
import {MONITOR_PROCESS_INSTANCE_CLOSE, MONITOR_INSTANCE_PAGE} from "@/constants/api";
import {ROUTE_MONITOR_INSTANCE_VIEW} from "@/constants/routes";
```

Fix bug on line 55:
`PageUtils.open('/flowable/monitor/instance/view?id=', '查看流程')`
→
`PageUtils.open(ROUTE_MONITOR_INSTANCE_VIEW + '?id=' + r.id, '查看流程')`

Replace line 66:
`HttpUtils.get('admin/flowable/monitor/processInstance/close', {id}).then((rs) => {`
→
`HttpUtils.get(MONITOR_PROCESS_INSTANCE_CLOSE, {id}).then((rs) => {`
Add `.catch()` after line 68 `})`:
```jsx
        }).catch(e => {
            message.error(e?.message || '关闭流程失败');
```

Replace line 77:
`request={(params) => HttpUtils.get('admin/flowable/monitor/instancePage', params)}`
→ `request={(params) => HttpUtils.get(MONITOR_INSTANCE_PAGE, params)}`

- [ ] **Step 6: Commit**

```bash
git add web/src/pages/flowable/monitor/
git commit -m "refactor(frontend): restructure monitor/ dir, add constants, fix view bug and missing catches"
```

---

### Task 5: 重构 simulate/index.jsx

**Files:**
- Modify: `web/src/pages/flowable/simulate/index.jsx`

- [ ] **Step 1: Update imports**

```jsx
import React from "react";
import {Button, Card, message, Modal, Space, Spin} from "antd";
import {HttpUtils, PageLoading, PageUtils} from "@jiangood/open-admin";
import InitPhase from "./InitPhase";
import RunningPhase from "./RunningPhase";
import FinishedPhase from "./FinishedPhase";
import {SIMULATE_GET, SIMULATE_USERS, SIMULATE_LIST, SIMULATE_START, SIMULATE_STATUS, SIMULATE_TASK_HANDLE, SIMULATE_DELETE} from "@/constants/api";
```

- [ ] **Step 2: Replace all 7 hardcoded API URLs**

| Line | Original | Replacement |
|------|----------|-------------|
| 34 | `'admin/flowable/simulate/get'` | `SIMULATE_GET` |
| 42 | `'admin/flowable/simulate/users'` | `SIMULATE_USERS` |
| 51 | `'admin/flowable/simulate/list'` | `SIMULATE_LIST` |
| 60 | `'admin/flowable/simulate/start'` | `SIMULATE_START` |
| 71 | `'admin/flowable/simulate/status'` | `SIMULATE_STATUS` |
| 110 | `'admin/flowable/simulate/task/handle'` | `SIMULATE_TASK_HANDLE` |
| 147 | `'admin/flowable/simulate/delete'` | `SIMULATE_DELETE` |

- [ ] **Step 3: Fix silent catch on line 53**

```jsx
}).catch(() => {
    this.setState({historyLoading: false});
});
```
→
```jsx
}).catch(e => {
    message.error(e?.message || '加载历史记录失败');
    this.setState({historyLoading: false});
});
```

Fix catch on line 63:
```jsx
}).catch(e => {
    message.error(e);
    this.setState({submitting: false});
});
```
→
```jsx
}).catch(e => {
    message.error(e?.message || '启动仿真失败');
    this.setState({submitting: false});
});
```

Fix catch on line 74:
```jsx
}).catch(e => {
    message.error(e);
    this.setState({loading: false, submitting: false});
});
```
→
```jsx
}).catch(e => {
    message.error(e?.message || '获取状态失败');
    this.setState({loading: false, submitting: false});
});
```

Fix catch on line 118:
```jsx
}).catch(e => {
    message.error(e);
    this.setState({submitting: false});
});
```
→
```jsx
}).catch(e => {
    message.error(e?.message || '操作失败');
    this.setState({submitting: false});
});
```

- [ ] **Step 4: Commit**

```bash
git add web/src/pages/flowable/simulate/index.jsx
git commit -m "refactor(frontend): add constants and unify error handling in simulate/index.jsx"
```

---

### Task 6: 重构 design/ 文件

**Files:**
- Modify: `web/src/pages/flowable/design/index.jsx`
- Modify: `web/src/pages/flowable/design/provider/properties/FormProps.jsx`
- Modify: `web/src/pages/flowable/design/provider/properties/DelegateExpressionProps.jsx`
- Modify: `web/src/pages/flowable/design/provider/properties/ConditionDesign.jsx`
- Modify: `web/src/pages/flowable/design/provider/properties/AssignmentSection.jsx`

- [ ] **Step 1: Update `design/index.jsx`**

Imports:
```jsx
import {HttpUtils, PageLoading, PageUtils, ProTable} from "@jiangood/open-admin";
import {MODEL_DETAIL, MODEL_SAVE_CONTENT, MODEL_DEPLOY, MODEL_GET_DEFINITION_CONTENT, MODEL_DEFINITION_PAGE} from "@/constants/api";
import {ROUTE_SIMULATE} from "@/constants/routes";
```

Replace URLs:
- Line 33: `'admin/flowable/model/detail'` → `MODEL_DETAIL`
- Line 92: `'admin/flowable/model/saveContent'` → `MODEL_SAVE_CONTENT`
- Line 103: `'admin/flowable/model/deploy'` → `MODEL_DEPLOY`
- Line 122: `'/flowable/simulate?id='` → `` ROUTE_SIMULATE + `?id=` ``
- Line 165: `'admin/flowable/model/getDefinitionContent'` → `MODEL_GET_DEFINITION_CONTENT`
- Line 175: `'admin/flowable/model/definitionPage'` → `MODEL_DEFINITION_PAGE`

- [ ] **Step 2: Update `FormProps.jsx`**

```jsx
import {Select} from 'antd';
import {useEffect, useState} from 'react';
import {HttpUtils} from "@jiangood/open-admin";
import {MODEL_FORM_OPTIONS} from "@/constants/api";
```
Replace line 9: `'admin/flowable/model/formOptions'` → `MODEL_FORM_OPTIONS`

- [ ] **Step 3: Update `DelegateExpressionProps.jsx`**

```jsx
import {Select} from 'antd';
import {useEffect, useState} from 'react';
import {HttpUtils} from "@jiangood/open-admin";
import {MODEL_JAVA_DELEGATE_OPTIONS} from "@/constants/api";
```
Replace line 9: `'admin/flowable/model/javaDelegateOptions'` → `MODEL_JAVA_DELEGATE_OPTIONS`

- [ ] **Step 4: Update `ConditionDesign.jsx`**

```jsx
import {FieldBoolean, FieldTable, HttpUtils, ObjectUtils, StringUtils, ThemeUtils} from "@jiangood/open-admin";
import {MODEL_VAR_LIST} from "@/constants/api";
```
Replace line 135: `'admin/flowable/model/varList'` → `MODEL_VAR_LIST`

- [ ] **Step 5: Update `AssignmentSection.jsx`**

```jsx
import {Form, Radio} from "antd";
import {FieldRemoteSelect, FieldRemoteSelectMultipleInline, StringUtils} from "@jiangood/open-admin";
import React from "react";
import {MODEL_ASSIGNEE_OPTIONS, MODEL_CANDIDATE_GROUPS_OPTIONS, MODEL_CANDIDATE_USERS_OPTIONS} from "@/constants/api";
```
Replace URLs:
- Line 59: `'admin/flowable/model/assigneeOptions'` → `MODEL_ASSIGNEE_OPTIONS`
- Line 64: `'admin/flowable/model/candidateGroupsOptions'` → `MODEL_CANDIDATE_GROUPS_OPTIONS`
- Line 69: `'admin/flowable/model/candidateUsersOptions'` → `MODEL_CANDIDATE_USERS_OPTIONS`

- [ ] **Step 6: Commit**

```bash
git add web/src/pages/flowable/design/
git commit -m "refactor(frontend): add constants to design/ pages and property panels"
```

---

### Task 7: 重构剩余文件 — flowable/index.jsx + InstanceView + example + forms

**Files:**
- Modify: `web/src/pages/flowable/index.jsx`
- Modify: `web/src/components/InstanceView.jsx`
- Modify: `web/src/pages/example/index.jsx`
- Modify: `web/src/forms/finish_viewForm.jsx`
- Modify: `web/src/forms/manager_approve_formForm.jsx`

- [ ] **Step 1: Update `flowable/index.jsx`**

```jsx
import {Button, Popconfirm, Space} from 'antd';
import React from 'react';
import {ButtonList, HttpUtils, Page, PageUtils, ProTable} from "@jiangood/open-admin";
import {MODEL_PAGE, MODEL_DELETE} from "@/constants/api";
import {ROUTE_DESIGN, ROUTE_MONITOR_TASK, ROUTE_MONITOR_INSTANCE, ROUTE_MONITOR_DEFINITION} from "@/constants/routes";
```

Replace URLs:
- Line 38: `'/flowable/design?id='` → `ROUTE_DESIGN + '?id='`
- Line 50: `'admin/flowable/model/delete'` → `MODEL_DELETE`
- Line 61: `'admin/flowable/model/page'` → `MODEL_PAGE`
- Line 66: `'/flowable/monitor/task'` → `ROUTE_MONITOR_TASK`
- Line 69: `'/flowable/monitor/instance'` → `ROUTE_MONITOR_INSTANCE`
- Line 72: `'/flowable/monitor/definition'` → `ROUTE_MONITOR_DEFINITION`

- [ ] **Step 2: Update `InstanceView.jsx`**

```jsx
import {HttpUtils, Page, PageUtils, ProTable} from "@jiangood/open-admin";
import {USER_TASK_GET_INSTANCE_INFO, MONITOR_INSTANCE_VARS} from "@/constants/api";
```

Replace line 24: `"admin/flowable/user-task/getInstanceInfo"` → `USER_TASK_GET_INSTANCE_INFO`
Replace line 78: `'admin/flowable/monitor/instance/vars'` → `MONITOR_INSTANCE_VARS`

- [ ] **Step 3: Update `example/index.jsx`**

```jsx
import {HttpUtils, Page} from "@jiangood/open-admin";
import {EXAMPLE_LEAVE_LIST, EXAMPLE_LEAVE_DETAIL, EXAMPLE_LEAVE_START} from "@/constants/api";
import {USER_TASK_GET_INSTANCE_INFO} from "@/constants/api";
```

Replace URLs:
- Line 25: `'admin/flowable/example/leave/list'` → `EXAMPLE_LEAVE_LIST`
- Line 33: `'admin/flowable/example/leave/detail'` → `EXAMPLE_LEAVE_DETAIL`
- Line 40: `'admin/flowable/user-task/getInstanceInfo'` → `USER_TASK_GET_INSTANCE_INFO`
- Line 53: `'admin/flowable/example/leave/start'` → `EXAMPLE_LEAVE_START`

Fix line 43 catch: `message.error(e)` → `message.error(e?.message || '加载失败')`
Fix line 58 catch: `message.error(e)` → `message.error(e?.message || '发起失败')`

- [ ] **Step 4: Update `finish_viewForm.jsx`**

```jsx
import {HttpUtils} from "@jiangood/open-admin";
import {EXAMPLE_LEAVE_DETAIL} from "@/constants/api";
```
Replace line 12: `'admin/flowable/example/leave/detail'` → `EXAMPLE_LEAVE_DETAIL`

- [ ] **Step 5: Update `manager_approve_formForm.jsx`**

```jsx
import {HttpUtils} from "@jiangood/open-admin";
import {EXAMPLE_LEAVE_DETAIL} from "@/constants/api";
```
Replace line 12: `'admin/flowable/example/leave/detail'` → `EXAMPLE_LEAVE_DETAIL`

- [ ] **Step 6: Commit**

```bash
git add web/src/pages/flowable/index.jsx web/src/components/InstanceView.jsx web/src/pages/example/index.jsx web/src/forms/
git commit -m "refactor(frontend): replace remaining hardcoded URLs with constants"
```

---

### Task 8: 更新 InitPhase.jsx 使用提取后的 HistoryListPanel

**Files:**
- Modify: `web/src/pages/flowable/simulate/InitPhase.jsx`

- [ ] **Step 1: Update `InitPhase.jsx` imports and replace HistoryListPanel usage**

```jsx
import React from "react";
import {Button, Form, Input, Select, Splitter} from "antd";
import {StringUtils} from "@jiangood/open-admin";
import {PlayCircleOutlined} from "@ant-design/icons";
import HistoryListPanel from "@/components/flowable/HistoryListPanel";

Remove lines 55-100 (the inline `function HistoryListPanel` definition). The JSX on line 47 already renders `<HistoryListPanel .../>` — no change needed in the JSX, only the import changes from "local function" to "imported component".

- [ ] **Step 2: Commit**

```bash
git add web/src/pages/flowable/simulate/InitPhase.jsx
git commit -m "refactor(frontend): use extracted HistoryListPanel in simulate InitPhase"
```
