import React from "react";
import {Button, Card, Empty, Form, Input, message, Modal, Radio, Skeleton, Spin, Splitter, Table, Tabs, Typography} from "antd";
import {FormRegistryUtils, Gap, HttpUtils, Page, PageLoading, PageUtils, ProTable} from "@jiangood/open-admin";

function TodoTable({onProcess}) {
    const columns = [
        {title: '发起人', dataIndex: 'instanceStarter'},
        {title: '流程名称', dataIndex: 'instanceName'},
        {title: '当前节点', dataIndex: 'taskName', width: 100},
        {title: '当前操作人', dataIndex: 'assigneeInfo', width: 100},
        {title: '发起时间', dataIndex: 'instanceStartTime'},
        {title: '任务创建时间', dataIndex: 'createTime'},
        {
            title: '操作', dataIndex: 'option',
            render: (_, record) => (
                <Button size='small' type='primary' onClick={() => onProcess(record)}>处理</Button>
            ),
        },
    ];
    return <ProTable
        showToolbarSearch={false}
        request={(params) => HttpUtils.get('admin/flowable/user-task/todoTaskPage', params)}
        columns={columns}
        size='small'
    />;
}

function DoneTable({onView}) {
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
                <Button size='small' onClick={() => onView(record)}>查看</Button>
            ),
        },
    ];
    return <ProTable
        showToolbarSearch={false}
        request={(params) => HttpUtils.get('admin/flowable/user-task/doneTaskPage', params)}
        columns={columns}
        size='small'
    />;
}

function MyTable({onView}) {
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
                <Button size='small' onClick={() => onView(record)}>查看</Button>
            ),
        },
    ];
    return <ProTable
        request={(params) => HttpUtils.get('admin/flowable/user-task/myInstance', params)}
        columns={columns}
    />;
}

class FormModal extends React.Component {
    state = {
        submitLoading: false,
        flowVisible: false,
        formData: {},
        instanceCommentList: [],
        vars: {},
        data: {taskId: null, commentList: [], img: null},
        loading: true,
        errorMsg: null,
    }

    componentDidMount() {
        const taskId = this.props.taskId ?? PageUtils.currentParams()?.taskId
        HttpUtils.get('admin/flowable/user-task/getInstanceInfoByTaskId', {taskId}).then(rs => {
            this.setState({data: rs})
        }).catch(e => {
            if (e?.message?.includes('任务已被处理')) {
                message.warning('此任务已被处理');
                setTimeout(() => {
                    this.props.onClose?.();
                    if (!this.props.onClose) PageUtils.closeCurrent();
                }, 1500);
            } else {
                message.error(e?.message || '获取任务信息失败');
            }
            this.setState({errorMsg: e})
        }).finally(() => {
            this.setState({loading: false})
        })
    }

    handleTask = async value => {
        this.setState({submitLoading: true});
        try {
            if (value.result === 'APPROVE') {
                value.formData = this.state.formData
            }
            value.taskId = this.state.data.taskId
            await HttpUtils.post('admin/flowable/user-task/handleTask', value)
            this.props.onClose?.();
            if (!this.props.onClose) PageUtils.closeCurrent()
        } catch (error) {
            message.error(error?.message || '操作失败')
        } finally {
            this.setState({submitLoading: false})
        }
    }

    renderProcess = (commentList) => <Card title='处理记录'>
        <Table dataSource={commentList}
               size='small'
               pagination={false}
               rowKey='id'
               columns={[
                   {dataIndex: 'content', title: '操作'},
                   {dataIndex: 'user', title: '处理人'},
                   {dataIndex: 'time', title: '处理时间'},
               ]}
        />
    </Card>;

    renderForm = () => {
        const {data, formData} = this.state
        const {businessKey} = data
        const formKey = data.formKey;
        const formName = data.formKey + 'Form'

        let ExForm = FormRegistryUtils.get(formName);
        if (!ExForm) {
            console.error(" 表单不存在： " + formName + "，请检查表单源代码：src/forms/" + formName + ".jsx")
            return <Empty description={"表单不存在： " + formName}></Empty>
        }

        return <ExForm id={businessKey} formKey={formKey} value={formData}
                       onChange={v => this.setState({formData: v})}></ExForm>
    }

    render() {
        const {submitLoading} = this.state
        const {data, loading} = this.state
        const {commentList, img} = data
        if (loading) {
            return <Spin/>
        }
        const {flowVisible} = this.state;
        const formContent = <>
            <Splitter>
                <Splitter.Panel>
                    <Typography.Title level={4}>{data.name}</Typography.Title>
                    <Typography.Text type="secondary">{data.starter} &nbsp;&nbsp; {data.startTime}</Typography.Text>
                    <Gap></Gap>
                    {this.renderForm()}
                </Splitter.Panel>
                <Splitter.Panel defaultSize={400}>
                    <Card title='审批意见'>
                        <Form layout='vertical' onFinish={this.handleTask} disabled={submitLoading}>
                            <Form.Item label='审批结果' name='result' rules={[{required: true, message: '请选择'}]}
                                       initialValue={'APPROVE'}>
                                <Radio.Group>
                                    <Radio value='APPROVE'>同意</Radio>
                                    <Radio value='REJECT'>不同意</Radio>
                                </Radio.Group>
                            </Form.Item>
                            <Form.Item label='审批意见' name='comment'
                                       rules={[{required: true, message: '请输入审批意见'}]}>
                                <Input.TextArea/>
                            </Form.Item>
                            <div style={{display: 'flex', gap: 8}}>
                                <Button type='primary' htmlType='submit' loading={submitLoading}
                                        size={"middle"}>提交</Button>
                                <Button size="middle"
                                        onClick={() => this.setState({flowVisible: true})}>流程图</Button>
                            </div>
                        </Form>
                    </Card>
                    <Gap></Gap>
                    {this.renderProcess(commentList)}
                </Splitter.Panel>
            </Splitter>
            <Modal title="流程图" open={flowVisible}
                   onCancel={() => this.setState({flowVisible: false})}
                   footer={null} width="80vw">
                <img src={img} style={{maxWidth: '100%'}}/>
            </Modal>
        </>;

        if (this.props.embedded) return formContent;
        return <Page padding>{formContent}</Page>
    }
}

class ViewModal extends React.Component {
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
        const id = this.props.id ?? params.id;
        const businessKey = this.props.businessKey ?? params.businessKey;

        const reqParams = {id};
        if (businessKey) reqParams.businessKey = businessKey;
        HttpUtils.get('admin/flowable/user-task/getInstanceInfo', reqParams).then(rs => {
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

        const content = <>
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
        </>;

        if (this.props.embedded) return content;
        return <Page padding>{content}</Page>
    }
}

export default class extends React.Component {
    state = {
        show: true,
        formModal: {visible: false, taskId: null},
        viewModal: {visible: false, id: null, title: ''},
        refreshKey: 0,
    }

    render() {
        if (!this.state.show) {
            return <PageLoading/>
        }

        const {formModal, viewModal} = this.state;

        const items = [
            {
                label: '待办任务', key: '1', children: <TodoTable
                    key={this.state.refreshKey}
                    onProcess={(record) => this.setState({formModal: {visible: true, taskId: record.id}})}
                />
            },
            {
                label: '已办任务', key: '2', children: <DoneTable
                    onView={(record) => this.setState({viewModal: {visible: true, id: record.instanceId, title: '流程信息'}})}
                />
            },
            {
                label: '我发起的', key: '3', children: <MyTable
                    onView={(record) => this.setState({viewModal: {visible: true, id: record.id, title: '流程信息'}})}
                />
            },
        ];

        return <>
            <Page padding>
                <Tabs defaultActiveKey="1" destroyOnHidden items={items}/>
            </Page>
            <Modal
                title="处理任务"
                open={formModal.visible}
                onCancel={() => this.setState({formModal: {visible: false, taskId: null}})}
                width="80vw"
                footer={null}
                destroyOnClose
                maskClosable={false}
            >
                {formModal.taskId && (
                    <FormModal
                        taskId={formModal.taskId}
                        embedded
                        onClose={() => this.setState(prev => ({
                            formModal: {visible: false, taskId: null},
                            refreshKey: prev.refreshKey + 1,
                        }))}
                    />
                )}
            </Modal>
            <Modal
                title={viewModal.title}
                open={viewModal.visible}
                onCancel={() => this.setState({viewModal: {visible: false, id: null, title: ''}})}
                width="80vw"
                footer={null}
                destroyOnClose
            >
                {viewModal.id && (
                    <ViewModal
                        id={viewModal.id}
                        embedded
                    />
                )}
            </Modal>
        </>
    }
}
