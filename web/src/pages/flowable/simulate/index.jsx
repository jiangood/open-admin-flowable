import React from "react";
import {Button, Card, Empty, Form, Input, message, Modal, Select, Space, Spin, Splitter, Table, Tag, Typography} from "antd";
import {HttpUtils, PageLoading, PageUtils, StringUtils} from "@jiangood/open-admin";
import {CheckCircleOutlined, CloseCircleOutlined, HistoryOutlined, PlayCircleOutlined, ReloadOutlined} from "@ant-design/icons";

const PHASE_INIT = 'init';
const PHASE_RUNNING = 'running';
const PHASE_FINISHED = 'finished';

export default class extends React.Component {

  state = {
    phase: PHASE_INIT,
    loading: false,
    submitting: false,

    // init phase
    model: undefined,
    users: [],
    historyList: [],
    historyLoading: false,

    // running / finished phase
    instanceId: null,
    status: null,

    // task form values: { [taskId]: { assignee, comment } }
    taskFormValues: {},
  }

  componentDidMount() {
    const params = PageUtils.currentParams();
    const id = this.id = params.id;

    // 加载模型元数据
    HttpUtils.get('admin/flowable/simulate/get', {id}).then(rs => {
      this.setState({model: rs}, this.loadHistory);
    });

    // 加载用户列表
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

  // ========== Init Phase ==========

  handleStart = values => {
    this.setState({submitting: true});
    HttpUtils.post('admin/flowable/simulate/start', values).then(rs => {
      const instanceId = rs.instanceId;
      message.success('仿真流程已启动');
      this.loadStatus(instanceId);
    }).catch(e => {
      message.error(e);
      this.setState({submitting: false});
    });
  }

  // ========== Running / Finished Phase ==========

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

  onImgClick = () => {
    Modal.info({
      title: '流程图',
      width: '70vw',
      content: <div style={{width: '100%', overflow: 'auto', maxHeight: '80vh'}}>
        <img src={this.state.status?.img} style={{maxWidth: '100%'}}/>
      </div>
    });
  }

  // ========== Render ==========

  render() {
    const {model} = this.state;

    if (model === undefined) {
      return <PageLoading/>;
    }

    const {phase, loading, submitting, status} = this.state;

    return (
      <Card title={'流程仿真 / 【' + model.name + '】 / ' + model.key}
            extra={phase === PHASE_FINISHED ? (
              <Space>
                <Button icon={<HistoryOutlined/>} onClick={this.handleReset}>历史记录</Button>
                <Button icon={<ReloadOutlined/>} onClick={this.handleReset}>重新仿真</Button>
              </Space>
            ) : null}>
        {phase === PHASE_INIT && this.renderInitPhase()}
        {phase !== PHASE_INIT && (
          loading ? <Spin style={{display: 'block', margin: '80px auto'}}/> :
            status ? this.renderRunningPhase() : null
        )}
      </Card>
    );
  }

  renderInitPhase = () => {
    const {model, users, submitting, historyList, historyLoading} = this.state;
    return (
      <Splitter>
        <Splitter.Panel defaultSize="55%">
          <Form onFinish={this.handleStart} layout="vertical">
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
                      onSearch={this.loadUsers}
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
          {this.renderHistoryList(historyList, historyLoading)}
        </Splitter.Panel>
      </Splitter>
    );
  }

  renderHistoryList = (list, loading) => (
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
                     <a onClick={() => this.handleViewHistory(record.instanceId)}>{text}</a>
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
                             onClick={() => this.handleDeleteHistory(record.instanceId)}>
                       删除
                     </Button>
                   ),
                 },
               ]}/>
      )}
    </div>
  );

  renderRunningPhase = () => {
    const {status, submitting, taskFormValues, users} = this.state;
    const {img, commentList, tasks, finished, deleteReason} = status;

    return (
      <Splitter>
        <Splitter.Panel defaultSize="60%">
          <div style={{paddingRight: 16}}>
            <Typography.Title level={5}>流程图</Typography.Title>
            <img src={img} style={{maxWidth: '100%', cursor: 'pointer'}}
                 onClick={this.onImgClick}/>
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
                            onSearch={this.loadUsers}
                            onChange={value => this.handleAssigneeChange(task.taskId, value)}
                            options={users.map(u => ({label: u.name, value: u.id}))}/>
                  </div>
                  <div>
                    <Typography.Text strong>审批意见：</Typography.Text>
                    <Input.TextArea rows={2}
                                    value={(taskFormValues[task.taskId] || {}).comment}
                                    onChange={e => this.handleCommentChange(task.taskId, e)}/>
                  </div>
                  <Space>
                    <Button type="primary"
                            icon={<CheckCircleOutlined/>}
                            loading={submitting}
                            onClick={() => this.handleTask(task.taskId, 'APPROVE')}>
                      同意
                    </Button>
                    <Button danger
                            icon={<CloseCircleOutlined/>}
                            loading={submitting}
                            onClick={() => this.handleTask(task.taskId, 'REJECT')}>
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
