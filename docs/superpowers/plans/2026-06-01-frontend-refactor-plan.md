# 前端代码重构实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 对 open-admin-flowable 前端进行精准代码质量重构，消除重复、提取公共组件、拆分大文件。

**Architecture:** 重构不改变组件外部行为。新建公共组件放 `src/components/`，拆分后子组件放原目录。每个任务独立可测，按顺序执行保证后续任务依赖先完成。

**Tech Stack:** React 19, UmiJS 4, Ant Design 6, `@jiangood/open-admin`

---

### Task 1: 删除 console.log

**Files:**
- Modify: `web/src/pages/flowable/design/provider/properties/ConditionDesign.jsx:135`

- [ ] **Step 1: 删除调试日志**

将 `ConditionDesign.jsx:135` 的 `console.log('流程id', processId)` 删除。

```jsx
// 删除这一行:
console.log('流程id', processId)
```

- [ ] **Step 2: 确认改动**

Run: `Get-Content -LiteralPath "web/src/pages/flowable/design/provider/properties/ConditionDesign.jsx" | Select-String "console.log"`

Expected: 没有输出（表示无 console.log 残留）

- [ ] **Step 3: 提交**

```bash
git add web/src/pages/flowable/design/provider/properties/ConditionDesign.jsx
git commit -m "refactor: remove debug console.log in ConditionDesign"
```

---

### Task 2: 提取 ProcessImageViewer 公共组件

**Files:**
- Create: `web/src/components/ProcessImageViewer.jsx`
- Modify: `web/src/pages/flowable/user-task/form.jsx:45-53`

- [ ] **Step 1: 创建 ProcessImageViewer 组件**

```jsx
import { Modal } from "antd";

export default function ProcessImageViewer({ imageUrl }) {
  const show = () => {
    Modal.info({
      title: '流程图',
      width: '70vw',
      content: (
        <div style={{ width: '100%', overflow: 'auto', maxHeight: '80vh' }}>
          <img src={imageUrl} style={{ maxWidth: '100%' }} />
        </div>
      ),
    });
  };

  return <a onClick={show}>查看流程图</a>;
}
```

- [ ] **Step 2: 替换 user-task/form.jsx 中的 onImgClick 方法**

在 `form.jsx` 顶部导入 ProcessImageViewer，删除 `onImgClick` 方法，在 `renderProcess` 中将图片替换为 ProcessImageViewer。

修改导入：
```jsx
import React from "react";
import {Button, Card, Empty, Form, Input, message, Modal, Radio, Spin, Splitter, Table, Tabs, Typography,} from "antd";
import {history} from "umi";
import {FormRegistryUtils, Gap, HttpUtils, Page, PageUtils} from "@jiangood/open-admin";
import {FormOutlined, ShareAltOutlined} from "@ant-design/icons";
import ProcessImageViewer from "@/components/ProcessImageViewer";
```

删除 `onImgClick` 方法（`form.jsx:45-53`）：
```jsx
    onImgClick = () => {
        Modal.info({
            title: '流程图',
            width: '70vw',
            content: <div style={{width: '100%', overflow: 'auto', maxHeight: '80vh'}}>
                <img src={this.state.data.img}/>
            </div>
        })
    };
```

修改 `renderProcess` 中的 img 元素（约第143行），从：
```jsx
        <img src={img} style={{maxWidth: '100%'}}
             onClick={this.onImgClick}/>
```
改为：
```jsx
        <ProcessImageViewer imageUrl={img}/>
```

同时，form.jsx 中不再需要 `Modal` 导入（因为删除了 `onImgClick`）。删除 `Modal` 从 antd 导入中：
```jsx
import {Button, Card, Empty, Form, Input, message, Radio, Spin, Splitter, Table, Tabs, Typography,} from "antd";
```

- [ ] **Step 3: 验证改动**

确认文件正确保存，无语法错误。确保 `form.jsx` 中 `onImgClick` 方法已被完全移除且没有其他引用。

- [ ] **Step 4: 提交**

```bash
git add web/src/components/ProcessImageViewer.jsx web/src/pages/flowable/user-task/form.jsx
git commit -m "refactor: extract ProcessImageViewer component"
```

---

### Task 3: 合并 Instance View 组件

**Files:**
- Create: `web/src/components/InstanceView.jsx`
- Modify: `web/src/pages/flowable/user-task/instance/view.jsx`
- Modify: `web/src/pages/flowable/monitor/instance/view.jsx`

- [ ] **Step 1: 创建公共 InstanceView 组件**

`web/src/components/InstanceView.jsx`:
```jsx
import React from "react";
import {Gap, HttpUtils, Page, PageUtils, ProTable} from "@jiangood/open-admin";
import {Card, Empty, Skeleton, Table} from "antd";

export default class extends React.Component {
    state = {
        commentList: [],
        vars: {},
        id: null,
        starter: null,
        startTime: null,
        name: null,
        img: null,
        loading: true,
        errorMsg: null,
    }

    componentDidMount() {
        const params = PageUtils.currentParams();
        const { id, businessKey } = params;

        HttpUtils.get("admin/flowable/user-task/getInstanceInfo", {id, businessKey}).then(rs => {
            this.setState(rs)
            this.setState({
                commentList: rs.commentList,
                img: rs.img,
                id: rs.id,
            })
        }).catch(e => {
            this.setState({errorMsg: e})
        }).finally(() => {
            this.setState({loading: false})
        })
    }

    getCommentColumns() {
        return [
            {dataIndex: 'content', title: '操作'},
            {dataIndex: 'user', title: '处理人'},
            {dataIndex: 'time', title: '处理时间'},
        ];
    }

    render() {
        if (this.state.errorMsg) {
            return <Empty description={this.state.errorMsg}></Empty>
        }

        const {commentList, img, loading, id} = this.state
        if (loading) {
            return <Skeleton/>
        }

        return (
            <Page padding>
                <Card title='流程图'>
                    <img src={img} style={{maxWidth: '100%'}}/>
                </Card>
                <Gap/>
                <Card title='审批记录'>
                    <Table dataSource={commentList}
                           size='small'
                           pagination={false}
                           rowKey='id'
                           columns={this.getCommentColumns()}
                    />
                </Card>
                <Gap/>
                {this.props.showVariables && (
                    <Card title='流程变量'>
                        <ProTable columns={[
                            {dataIndex: 'key', title: '变量名'},
                            {dataIndex: 'value', title: '变量值'},
                        ]}
                                  rowKey='key'
                                  request={() => HttpUtils.get('admin/flowable/monitor/instance/vars', {id})}
                        />
                    </Card>
                )}
            </Page>
        )
    }
}
```

- [ ] **Step 2: 简化 user-task/instance/view.jsx**

将 `web/src/pages/flowable/user-task/instance/view.jsx` 整体替换为：
```jsx
import InstanceView from "@/components/InstanceView";

export default function UserTaskInstanceView() {
    return <InstanceView />;
}
```

- [ ] **Step 3: 简化 monitor/instance/view.jsx**

将 `web/src/pages/flowable/monitor/instance/view.jsx` 整体替换为：
```jsx
import InstanceView from "@/components/InstanceView";

export default function MonitorInstanceView() {
    return <InstanceView showVariables />;
}
```

- [ ] **Step 4: 验证改动**

Run: `Get-ChildItem -Path "web/src/pages/flowable/user-task/instance/view.jsx", "web/src/pages/flowable/monitor/instance/view.jsx", "web/src/components/InstanceView.jsx"`

确认三个文件均存在。确认简化后的两个文件只有几行。

- [ ] **Step 5: 提交**

```bash
git add web/src/components/InstanceView.jsx web/src/pages/flowable/user-task/instance/view.jsx web/src/pages/flowable/monitor/instance/view.jsx
git commit -m "refactor: merge duplicate InstanceView into single component"
```

---

### Task 4: 拆分 simulate/index.jsx

**Files:**
- Create: `web/src/pages/flowable/simulate/InitPhase.jsx`
- Create: `web/src/pages/flowable/simulate/RunningPhase.jsx`
- Create: `web/src/pages/flowable/simulate/FinishedPhase.jsx`
- Modify: `web/src/pages/flowable/simulate/index.jsx`

- [ ] **Step 1: 创建 InitPhase.jsx**

`web/src/pages/flowable/simulate/InitPhase.jsx`:
```jsx
import React from "react";
import {Button, Empty, Form, Input, Select, Splitter, Spin, Table, Typography} from "antd";
import {HttpUtils, StringUtils} from "@jiangood/open-admin";
import {HistoryOutlined, PlayCircleOutlined} from "@ant-design/icons";

export default class InitPhase extends React.Component {
    render() {
        const {model, users, submitting, historyList, historyLoading,
               onStart, onLoadUsers, onViewHistory, onDeleteHistory} = this.props;

        return (
            <Splitter>
                <Splitter.Panel defaultSize="55%">
                    <Form onFinish={onStart} layout="vertical">
                        <Form.Item name="key" noStyle initialValue={model.key}/>

                        <Form.Item label="业务标识" name="id"
                                   rules={[{required: true, message: '请输入业务标识'}]}
                                   initialValue={StringUtils.random(16)}>
                            <Input/>
                        </Form.Item>

                        <Form.Item label="发起人" name="initiatorId"
                                   rules={[{required: true, message: '请选择发起人'}]}>
                            <Select showSearch placeholder="搜索并选择用户"
                                    filterOption={false}
                                    onSearch={onLoadUsers}
                                    options={users.map(u => ({label: u.name, value: u.id}))}
                                    style={{width: 300}}/>
                        </Form.Item>

                        {model.variables?.map(item => (
                            <Form.Item key={item.name} name={['variables', item.name]} label={item.label}>
                                <Input/>
                            </Form.Item>
                        ))}

                        <Form.Item>
                            <Button type="primary" htmlType="submit" icon={<PlayCircleOutlined/>}
                                    loading={submitting}>
                                启动仿真
                            </Button>
                        </Form.Item>
                    </Form>
                </Splitter.Panel>
                <Splitter.Panel defaultSize="45%">
                    <HistoryListPanel list={historyList} loading={historyLoading}
                                     onView={onViewHistory} onDelete={onDeleteHistory}/>
                </Splitter.Panel>
            </Splitter>
        );
    }
}

function HistoryListPanel({list, loading, onView, onDelete}) {
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

Wait, I need to fix the HistoryListPanel — it uses `Tag` but doesn't import it. Let me fix the import line:

```jsx
import {Button, Empty, Form, Input, Select, Splitter, Spin, Table, Tag, Typography} from "antd";
```

- [ ] **Step 2: 创建 RunningPhase.jsx**

`web/src/pages/flowable/simulate/RunningPhase.jsx`:
```jsx
import React from "react";
import {Button, Card, Empty, Input, Select, Space, Splitter, Table, Tag, Typography} from "antd";
import {CheckCircleOutlined, CloseCircleOutlined} from "@ant-design/icons";
import ProcessImageViewer from "@/components/ProcessImageViewer";

export default class RunningPhase extends React.Component {
    render() {
        const {status, submitting, taskFormValues, users,
               onAssigneeChange, onCommentChange, onTask} = this.props;
        const {img, commentList, tasks, finished, deleteReason} = status;

        return (
            <Splitter>
                <Splitter.Panel defaultSize="60%">
                    <div style={{paddingRight: 16}}>
                        <Typography.Title level={5}>流程图</Typography.Title>
                        <ProcessImageViewer imageUrl={img}/>
                        <div style={{marginTop: 16}}>
                            <Typography.Title level={5}>处理记录</Typography.Title>
                            <Table dataSource={commentList || []}
                                   size="small" pagination={false} rowKey="time"
                                   columns={[
                                       {dataIndex: 'content', title: '操作'},
                                       {dataIndex: 'user', title: '处理人', width: 120},
                                       {dataIndex: 'time', title: '处理时间', width: 160},
                                   ]}/>
                        </div>
                    </div>
                </Splitter.Panel>
                <Splitter.Panel defaultSize="40%">
                    <div style={{paddingLeft: 16}}>
                        <Space style={{marginBottom: 12}}>
                            <Typography.Text strong>实例名称：</Typography.Text>
                            <Typography.Text>{status.name}</Typography.Text>
                        </Space>
                        <br/>
                        <Space style={{marginBottom: 12}}>
                            <Typography.Text strong>业务标识：</Typography.Text>
                            <Typography.Text>{status.businessKey}</Typography.Text>
                        </Space>
                        <br/>
                        <Space style={{marginBottom: 12}}>
                            <Typography.Text strong>发起人：</Typography.Text>
                            <Typography.Text>{status.starter}</Typography.Text>
                        </Space>
                        <br/>
                        <Space style={{marginBottom: 12}}>
                            <Typography.Text strong>发起时间：</Typography.Text>
                            <Typography.Text>{status.startTime}</Typography.Text>
                        </Space>
                        <br/>
                        <Space style={{marginBottom: 16}}>
                            <Typography.Text strong>状态：</Typography.Text>
                            {finished ? (
                                deleteReason ? (
                                    <Tag icon={<CloseCircleOutlined/>} color="error">已终止</Tag>
                                ) : (
                                    <Tag icon={<CheckCircleOutlined/>} color="success">已完成</Tag>
                                )
                            ) : (
                                <Tag color="processing">运行中</Tag>
                            )}
                        </Space>

                        {finished && deleteReason && (
                            <Card size="small" title="终止原因" style={{marginBottom: 16}}>
                                <Typography.Text type="secondary">{deleteReason}</Typography.Text>
                            </Card>
                        )}

                        {!finished && tasks?.map(task => (
                            <Card key={task.taskId} size="small" title={task.taskName}
                                  style={{marginBottom: 12}}>
                                <Space direction="vertical" style={{width: '100%'}}>
                                    <div>
                                        <Typography.Text strong>处理人：</Typography.Text>
                                        <Select showSearch placeholder="选择处理人"
                                                value={(taskFormValues[task.taskId] || {}).assignee}
                                                style={{width: '100%'}}
                                                filterOption={false}
                                                onSearch={onLoadUsers}
                                                onChange={value => onAssigneeChange(task.taskId, value)}
                                                options={users.map(u => ({label: u.name, value: u.id}))}/>
                                    </div>
                                    <div>
                                        <Typography.Text strong>审批意见：</Typography.Text>
                                        <Input.TextArea rows={2}
                                                        value={(taskFormValues[task.taskId] || {}).comment}
                                                        onChange={e => onCommentChange(task.taskId, e)}/>
                                    </div>
                                    <Space>
                                        <Button type="primary"
                                                icon={<CheckCircleOutlined/>}
                                                loading={submitting}
                                                onClick={() => onTask(task.taskId, 'APPROVE')}>
                                            同意
                                        </Button>
                                        <Button danger
                                                icon={<CloseCircleOutlined/>}
                                                loading={submitting}
                                                onClick={() => onTask(task.taskId, 'REJECT')}>
                                            不同意
                                        </Button>
                                    </Space>
                                </Space>
                            </Card>
                        ))}

                        {!finished && (!tasks || tasks.length === 0) && (
                            <Empty description="暂无活跃任务"/>
                        )}
                    </div>
                </Splitter.Panel>
            </Splitter>
        );
    }
}
```

Wait, I notice `RunningPhase` uses `onLoadUsers` in the template but I need to make sure it's passed as a prop. Let me fix this — in the prop destructuring I need to add `onLoadUsers`:

```jsx
const {status, submitting, taskFormValues, users, onLoadUsers,
       onAssigneeChange, onCommentChange, onTask} = this.props;
```

- [ ] **Step 3: 创建 FinishedPhase.jsx**

`web/src/pages/flowable/simulate/FinishedPhase.jsx`:
```jsx
import React from "react";
import {Button, Empty, Space, Spin, Table, Tag, Typography} from "antd";
import {HistoryOutlined, ReloadOutlined} from "@ant-design/icons";

export default class FinishedPhase extends React.Component {
    render() {
        const {status, historyList, historyLoading,
               onReset, onViewHistory, onDeleteHistory} = this.props;
        const {deleteReason} = status;

        return (
            <div>
                <Space style={{marginBottom: 16}}>
                    <Button icon={<HistoryOutlined/>} onClick={onReset}>历史记录</Button>
                    <Button icon={<ReloadOutlined/>} onClick={onReset}>重新仿真</Button>
                </Space>

                {historyLoading ? <Spin style={{display: 'block', margin: '80px auto'}}/> : (
                    historyList.length === 0 ? (
                        <Empty description="暂无仿真记录" image={Empty.PRESENTED_IMAGE_SIMPLE}/>
                    ) : (
                        <Table dataSource={historyList}
                               size="small" pagination={false} rowKey="instanceId"
                               columns={[
                                   {
                                       title: '实例', dataIndex: 'name', ellipsis: true,
                                       render: (text, record) => (
                                           <a onClick={() => onViewHistory(record.instanceId)}>{text}</a>
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
                                                   onClick={() => onDeleteHistory(record.instanceId)}>
                                               删除
                                           </Button>
                                       ),
                                   },
                               ]}/>
                    )
                )}
            </div>
        );
    }
}
```

- [ ] **Step 4: 简化 simulate/index.jsx**

将 `web/src/pages/flowable/simulate/index.jsx` 整体替换为：
```jsx
import React from "react";
import {Button, Card, message, Modal, Space, Spin} from "antd";
import {HttpUtils, PageLoading, PageUtils} from "@jiangood/open-admin";

import InitPhase from "./InitPhase";
import RunningPhase from "./RunningPhase";
import FinishedPhase from "./FinishedPhase";

const PHASE_INIT = 'init';
const PHASE_RUNNING = 'running';
const PHASE_FINISHED = 'finished';

export default class extends React.Component {

  state = {
    phase: PHASE_INIT,
    loading: false,
    submitting: false,

    model: undefined,
    users: [],
    historyList: [],
    historyLoading: false,

    instanceId: null,
    status: null,

    taskFormValues: {},
  }

  componentDidMount() {
    const params = PageUtils.currentParams();
    const id = this.id = params.id;

    HttpUtils.get('admin/flowable/simulate/get', {id}).then(rs => {
      this.setState({model: rs}, this.loadHistory);
    });

    this.loadUsers();
  }

  loadUsers = (searchText) => {
    HttpUtils.get('admin/flowable/simulate/users', {searchText}).then(rs => {
      this.setState({users: rs || []});
    });
  }

  loadHistory = () => {
    const {model} = this.state;
    if (!model?.key) return;
    this.setState({historyLoading: true});
    HttpUtils.get('admin/flowable/simulate/list', {key: model.key}).then(rs => {
      this.setState({historyList: rs || [], historyLoading: false});
    }).catch(() => {
      this.setState({historyLoading: false});
    });
  }

  handleStart = values => {
    this.setState({submitting: true});
    HttpUtils.post('admin/flowable/simulate/start', values).then(rs => {
      message.success('仿真流程已启动');
      this.loadStatus(rs.instanceId);
    }).catch(e => {
      message.error(e);
      this.setState({submitting: false});
    });
  }

  loadStatus = (instanceId) => {
    this.setState({loading: true, instanceId, phase: PHASE_RUNNING, taskFormValues: {}});
    HttpUtils.get('admin/flowable/simulate/status', {instanceId}).then(rs => {
      const phase = rs.finished ? PHASE_FINISHED : PHASE_RUNNING;
      this.setState({status: rs, phase, loading: false, submitting: false});
    }).catch(e => {
      message.error(e);
      this.setState({loading: false, submitting: false});
    });
  }

  handleAssigneeChange = (taskId, value) => {
    this.setState(prev => ({
      taskFormValues: {
        ...prev.taskFormValues,
        [taskId]: { ...prev.taskFormValues[taskId], assignee: value }
      }
    }));
  }

  handleCommentChange = (taskId, e) => {
    const value = e.target.value;
    this.setState(prev => ({
      taskFormValues: {
        ...prev.taskFormValues,
        [taskId]: { ...prev.taskFormValues[taskId], comment: value }
      }
    }));
  }

  handleTask = (taskId, action) => {
    const {taskFormValues} = this.state;
    const formValue = taskFormValues[taskId] || {};
    const handleUserId = formValue.assignee;

    if (!handleUserId) {
      message.warning('请选择处理人');
      return;
    }

    this.setState({submitting: true});
    HttpUtils.post('admin/flowable/simulate/task/handle', {
      taskId,
      action,
      comment: formValue.comment || '',
      handleUserId,
    }).then(() => {
      message.success(action === 'APPROVE' ? '已同意' : '已拒绝');
      this.loadStatus(this.state.instanceId);
    }).catch(e => {
      message.error(e);
      this.setState({submitting: false});
    });
  }

  handleReset = () => {
    this.setState({
      phase: PHASE_INIT,
      instanceId: null,
      status: null,
      loading: false,
      submitting: false,
      taskFormValues: {},
    }, this.loadHistory);
  }

  handleViewHistory = (instanceId) => {
    this.loadStatus(instanceId);
  }

  handleDeleteHistory = (instanceId) => {
    Modal.confirm({
      title: '确认删除',
      content: '确定要物理删除此仿真记录吗？删除后不可恢复。',
      okText: '确认删除',
      okType: 'danger',
      cancelText: '取消',
      onOk: () => {
        HttpUtils.post('admin/flowable/simulate/delete', {instanceId}).then(() => {
          message.success('仿真记录已删除');
          this.loadHistory();
        });
      },
    });
  }

  render() {
    const {model} = this.state;

    if (model === undefined) {
      return <PageLoading/>;
    }

    const {phase, loading, submitting, status, users, historyList, historyLoading, taskFormValues} = this.state;

    return (
      <Card title={'流程仿真 / 【' + model.name + '】 / ' + model.key}
            extra={phase === PHASE_FINISHED ? (
              <Space>
                <Button onClick={this.handleReset}>重新仿真</Button>
              </Space>
            ) : null}>
        {phase === PHASE_INIT && (
          <InitPhase
            model={model}
            users={users}
            submitting={submitting}
            historyList={historyList}
            historyLoading={historyLoading}
            onStart={this.handleStart}
            onLoadUsers={this.loadUsers}
            onViewHistory={this.handleViewHistory}
            onDeleteHistory={this.handleDeleteHistory}
          />
        )}
        {phase === PHASE_RUNNING && (
          loading ? <Spin style={{display: 'block', margin: '80px auto'}}/> :
            status ? (
              <RunningPhase
                status={status}
                submitting={submitting}
                taskFormValues={taskFormValues}
                users={users}
                onLoadUsers={this.loadUsers}
                onAssigneeChange={this.handleAssigneeChange}
                onCommentChange={this.handleCommentChange}
                onTask={this.handleTask}
              />
            ) : null
        )}
        {phase === PHASE_FINISHED && (
          loading ? <Spin style={{display: 'block', margin: '80px auto'}}/> :
            status ? (
              <FinishedPhase
                status={status}
                historyList={historyList}
                historyLoading={historyLoading}
                onReset={this.handleReset}
                onViewHistory={this.handleViewHistory}
                onDeleteHistory={this.handleDeleteHistory}
              />
            ) : null
        )}
      </Card>
    );
  }
}
```

- [ ] **Step 5: 验证改动**

Run: `Get-ChildItem -Path "web/src/pages/flowable/simulate/"`

Expected 输出包含:
```
InitPhase.jsx
RunningPhase.jsx
FinishedPhase.jsx
index.jsx
```

确认 `index.jsx` 行数从 400 降至约 170 行。

- [ ] **Step 6: 提交**

```bash
git add web/src/pages/flowable/simulate/
git commit -m "refactor: split simulate/index.jsx into sub-components"
```
