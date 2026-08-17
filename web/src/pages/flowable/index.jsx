import {Button, Card, Empty, Form, message, Modal, Popconfirm, Skeleton, Space, Table} from 'antd';
import React from 'react';
import {FieldUserSelect, Gap, HttpClient, Page, PageUtils, PermActions, ProTable} from "@jiangood/open-admin";

class InstanceViewModal extends React.Component {
    state = {
        commentList: [],
        id: null,
        img: null,
        loading: true,
        errorMsg: null,
    }

    componentDidMount() {
        const {id, businessKey} = this.props;
        const reqParams = {id};
        if (businessKey) reqParams.businessKey = businessKey;
        HttpClient.get('admin/flowable/user-task/getInstanceInfo', reqParams).then(rs => {
            this.setState({
                ...rs.data,
                commentList: rs.data.commentList,
                img: rs.data.img,
                id: rs.data.id,
            })
        }).catch(e => {
            this.setState({errorMsg: e?.message ?? '加载失败'})
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
            return <Empty description={this.state.errorMsg}/>
        }

        const {commentList, img, loading, id} = this.state
        if (loading) {
            return <Skeleton/>
        }

        return <>
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
                              request={() => HttpClient.get('admin/flowable/monitor/instance/vars', {id})}
                    />
                </Card>
            )}
        </>;
    }
}

export default class extends React.Component {

    actionRef = React.createRef();
    taskTableRef = React.createRef();
    instanceTableRef = React.createRef();
    definitionTableRef = React.createRef();
    assigneeFormRef = React.createRef();

    state = {
        taskModalOpen: false,
        instanceModalOpen: false,
        definitionModalOpen: false,
        viewModalOpen: false,
        viewInstanceId: null,
        assigneeFormOpen: false,
        assigneeFormValues: {},
    }

    columns = [
        {
            title: '名称',
            dataIndex: 'name',
        },
        {
            title: '代码',
            dataIndex: 'key'
        },
        {
            title: '版本',
            dataIndex: 'version',
        },
        {
            title: '更新时间',
            dataIndex: 'lastUpdateTime',
        },
        {
            title: '操作',
            dataIndex: 'option',
            render: (_, record) => (
                <Space>
                    <Button size='small' type='primary'
                            onClick={() => PageUtils.open('/flowable/design' + '?id=' + record.id, '流程设计' + record.name)}> 设计 </Button>
                    <PermActions actions={[
                        {
                            label: '删除',
                            perm: 'flowable/model:delete',
                            confirm: '是否确定删除流程模型',
                            danger: true,
                            onClick: () => this.handleDelete(record),
                        }
                    ]}/>
                </Space>
            ),
        },
    ];

    handleDelete = row => {
        HttpClient.post('admin/flowable/model/delete', {id: row.id}).then(rs => {
            this.actionRef.current.reload();
        })
    }

    onClickSetAssignee = id => {
        this.setState({assigneeFormOpen: true, assigneeFormValues: {taskId: id}})
    };

    submitSetAssignee = values => {
        HttpClient.post('admin/flowable/monitor/setAssignee', values).then(() => {
            this.setState({assigneeFormOpen: false})
            this.taskTableRef.current.reload()
        }).catch(e => {
            message.error(e?.message || '指定处理人失败');
        })
    };

    instanceColumns = [
        {
            title: 'ID',
            dataIndex: 'id',
            key: 'id',
        },
        {
            title: '名称',
            dataIndex: 'name',
            key: 'name',
        },
        {
            title: '流程定义名称',
            dataIndex: 'processDefinitionName',
            key: 'processDefinitionName',
        },
        {
            title: '流程定义键',
            dataIndex: 'processDefinitionKey',
            key: 'processDefinitionKey',
        },
        {
            title: '版本',
            dataIndex: 'processDefinitionVersion',
            key: 'processDefinitionVersion',
        },
        {
            title: '业务键',
            dataIndex: 'businessKey',
            key: 'businessKey',
        },
        {
            title: '状态',
            dataIndex: 'suspended',
            key: 'suspended',
            render: (value) => value ? '已挂起' : '运行中',
        },
        {
            title: '开始时间',
            dataIndex: 'startTime',
            key: 'startTime',
        },
        {
            dataIndex: 'options',
            title: '操作',
            fixed: 'right',
            render: (_, r) => {
                return <Space>
                    <Button size='small' onClick={() => this.setState({viewModalOpen: true, viewInstanceId: r.id})}>查看</Button>
                    <Popconfirm title={'关闭流程'}
                                onConfirm={() => this.closeInstance(r.id)}>
                        <Button size='small'>终止</Button>
                    </Popconfirm>
                </Space>
            }
        }
    ]

    closeInstance = (id) => {
        HttpClient.get('admin/flowable/monitor/processInstance/close', {id}).then((rs) => {
            this.instanceTableRef.current.reload()
        }).catch(e => {
            message.error(e?.message || '关闭流程失败');
        })
    }

    definitionColumns = [
        {
            title: 'ID',
            dataIndex: 'id',
        },
        {
            title: '分类',
            dataIndex: 'category',
        },
        {
            title: '名称',
            dataIndex: 'name',
        },
        {
            title: '键',
            dataIndex: 'key',
        },
        {
            title: '描述',
            dataIndex: 'description',
        },
        {
            title: '版本',
            dataIndex: 'version',
        },
        {
            title: '资源名称',
            dataIndex: 'resourceName',
        },
        {
            title: '部署ID',
            dataIndex: 'deploymentId',
        },
        {
            title: '图表资源名称',
            dataIndex: 'diagramResourceName',
        },
        {
            title: '是否有开始表单键',
            dataIndex: 'hasStartFormKey',
            render: (value) => value ? '是' : '否',
        },
        {
            title: '是否有图形符号',
            dataIndex: 'hasGraphicalNotation',
            render: (value) => value ? '是' : '否',
        },
        {
            title: '是否挂起',
            dataIndex: 'suspended',
            render: (value) => value ? '是' : '否',
        },
        {
            title: '租户ID',
            dataIndex: 'tenantId',
        },
        {
            title: '派生自',
            dataIndex: 'derivedFrom',
        },
        {
            title: '根派生来源',
            dataIndex: 'derivedFromRoot',
        },
        {
            title: '派生版本',
            dataIndex: 'derivedVersion',
        },
    ]

    render() {
        return <Page padding>
            <ProTable
                actionRef={this.actionRef}
                request={(params) => HttpClient.get('admin/flowable/model/page', params)}
                columns={this.columns}
                toolBarRender={() => {
                    return <Space>
                        <Button onClick={() => this.setState({taskModalOpen: true})}>
                            运行中的任务
                        </Button>
                        <Button onClick={() => this.setState({instanceModalOpen: true})}>
                            运行中的流程实例
                        </Button>
                        <Button onClick={() => this.setState({definitionModalOpen: true})}>
                            已部署的流程定义
                        </Button>
                    </Space>
                }}
            />

            <Modal title='运行中的任务' width={1200}
                   open={this.state.taskModalOpen}
                   onCancel={() => this.setState({taskModalOpen: false})}
                   footer={null} destroyOnClose
            >
                <ProTable
                    actionRef={this.taskTableRef}
                    columns={[
                        {
                            dataIndex: 'processInstanceName',
                            title: '实例名称'
                        },
                        {
                            dataIndex: 'name',
                            title: '任务名称',
                        },
                        {
                            dataIndex: 'assigneeLabel',
                            title: '处理人'
                        },
                        {
                            dataIndex: 'id',
                            title: '任务标识',
                        },
                        {
                            dataIndex: 'processDefinitionId',
                            title: '定义'
                        },
                        {
                            dataIndex: 'processInstanceId',
                            title: '实例'
                        },
                        {
                            dataIndex: 'startTime',
                            title: '开始时间'
                        },
                        {
                            dataIndex: 'tenantId',
                            title: '租户'
                        },
                        {
                            dataIndex: 'id',
                            render: (id) => {
                                return <Button size='small' onClick={() => this.onClickSetAssignee(id)}>指定处理人</Button>
                            }
                        }
                    ]}
                    request={(params) => HttpClient.get('admin/flowable/monitor/task', params)}
                    searchFormRender={() => (
                        <>
                            <Form.Item label='受理人' name='assignee'>
                                <FieldUserSelect/>
                            </Form.Item>
                        </>
                    )}
                />
            </Modal>

            <Modal title='指定处理人'
                   open={this.state.assigneeFormOpen}
                   onOk={() => this.assigneeFormRef.current.submit()}
                   onCancel={() => this.setState({assigneeFormOpen: false})}
                   destroyOnClose
            >
                <Form ref={this.assigneeFormRef} onFinish={this.submitSetAssignee}
                      initialValues={this.state.assigneeFormValues}>
                    <Form.Item name='taskId' noStyle>
                    </Form.Item>
                    <Form.Item name='assignee' label='用户'>
                        <FieldUserSelect/>
                    </Form.Item>
                </Form>
            </Modal>

            <Modal title='运行中的流程实例' width={1200}
                   open={this.state.instanceModalOpen}
                   onCancel={() => this.setState({instanceModalOpen: false})}
                   footer={null} destroyOnClose
            >
                <ProTable
                    actionRef={this.instanceTableRef}
                    columns={this.instanceColumns}
                    request={(params) => HttpClient.get('admin/flowable/monitor/instancePage', params)}
                />
            </Modal>

            <Modal title='实例详情' width={1000}
                   open={this.state.viewModalOpen}
                   onCancel={() => this.setState({viewModalOpen: false})}
                   footer={null} destroyOnClose
            >
                {this.state.viewInstanceId && (
                    <InstanceViewModal id={this.state.viewInstanceId} showVariables/>
                )}
            </Modal>

            <Modal title='已部署的流程定义' width={1200}
                   open={this.state.definitionModalOpen}
                   onCancel={() => this.setState({definitionModalOpen: false})}
                   footer={null} destroyOnClose
            >
                <ProTable
                    actionRef={this.definitionTableRef}
                    columns={this.definitionColumns}
                    request={(params) => HttpClient.get('admin/flowable/monitor/definitionPage', params)}
                />
            </Modal>
        </Page>
    }
}
