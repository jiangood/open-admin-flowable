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
    }).catch(e => {
      message.error(e?.message || '加载历史记录失败');
      this.setState({historyLoading: false});
    });
  }

  handleStart = values => {
    this.setState({submitting: true});
    HttpUtils.post('admin/flowable/simulate/start', values).then(rs => {
      message.success('仿真流程已启动');
      this.loadStatus(rs.instanceId);
    }).catch(e => {
      message.error(e?.message || '启动仿真失败');
      this.setState({submitting: false});
    });
  }

  loadStatus = (instanceId) => {
    this.setState({loading: true, instanceId, phase: PHASE_RUNNING, taskFormValues: {}});
    HttpUtils.get('admin/flowable/simulate/status', {instanceId}).then(rs => {
      const phase = rs.finished ? PHASE_FINISHED : PHASE_RUNNING;
      this.setState({status: rs, phase, loading: false, submitting: false});
    }).catch(e => {
      message.error(e?.message || '获取状态失败');
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
      message.error(e?.message || '操作失败');
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
