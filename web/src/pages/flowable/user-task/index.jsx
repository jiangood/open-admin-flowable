import React from "react";
import {Button, Tabs} from "antd";
import {HttpUtils, LinkButton, Page, PageLoading, PageUtils, ProTable} from "@jiangood/open-admin";


export default class extends React.Component {

    state = {
        show: true
    }

    render() {
        if (!this.state.show) {
            return <PageLoading/>
        }

        const items = [
            {label: '待办任务', key: '1', children: this.renderTodo()},
            {label: '已办任务', key: '2', children: this.renderDone()},
            {label: '我发起的', key: '3', children: this.renderMyStart()},
        ]

        return <Page padding>
            <Tabs defaultActiveKey="1" destroyOnHidden items={items}>

            </Tabs>
        </Page>
    }


    renderTodo = () => <ProTable
        showToolbarSearch={false}
        request={(params) => HttpUtils.get("admin/flowable/user-task/todoTaskPage", params)}
        columns={[

            {
                title: '发起�?,
                dataIndex: 'instanceStarter'
            },
            {
                title: '流程名称',
                dataIndex: 'instanceName',
            },
            {
                title: '当前节点',
                dataIndex: 'taskName',
                width: 100,
            },
            {
                title: '当前操作�?,
                dataIndex: 'assigneeInfo',
                width: 100
            },

            {
                title: '发起时间',
                dataIndex: 'instanceStartTime',
            },
            {
                title: '任务创建时间',
                dataIndex: 'createTime',
            },

            {
                title: '操作',
                dataIndex: 'option',
                render: (_, record) => {
                    let path = '/flowable/user-task/form?taskId=' + record.id;
                    return (
                        <LinkButton
                            type='primary'
                            path={path}
                            label='处理任务'>处理</LinkButton>
                    );
                },
            },
        ]}
        size='small'
    />;

    renderDone = () => <ProTable
        showToolbarSearch={false}
        request={(params) => HttpUtils.get("admin/flowable/user-task/doneTaskPage", params)}
        columns={[
            {
                title: '流程名称',
                dataIndex: 'instanceName',
            },
            {
                title: '发起�?,
                dataIndex: 'instanceStarter'
            },
            {
                title: '发起时间',
                dataIndex: 'instanceStartTime',
            },
            {
                title: '任务创建时间',
                dataIndex: 'createTime',
            },
            {
                title: '处理时间',
                dataIndex: 'endTime',
            },
            {
                title: '耗时',
                dataIndex: 'durationInfo',
            },
            {
                title: '处理节点',
                dataIndex: 'taskName'
            },
            {
                title: '操作�?,
                dataIndex: 'assigneeInfo'
            },


            {
                title: '操作',
                dataIndex: 'option',
                render: (_, record) => (
                     <Button size='small' onClick={() => PageUtils.open('/flowable/user-task/instance/view?id='+record.id, '流程信息') }> 查看 </Button>
                ),
            },
        ]}
        size='small'
    />;

    renderMyStart = () => <ProTable
        request={(params) => HttpUtils.get("admin/flowable/user-task/myInstance", params)}
        columns={[

            {
                title: '流程名称',
                dataIndex: 'processDefinitionName',
                render(_, r) {
                    return r.name || r.processDefinitionName
                }
            },
            {
                title: '发起�?,
                dataIndex: 'startUserName',
            },
            {
                title: '发起时间',
                dataIndex: 'startTime',
            },
            {
                title: '业务标识',
                dataIndex: 'businessKey',

            },


            {
                title: '结束时间',
                dataIndex: 'endTime',
            },

            {
                title: '流程状�?,
                dataIndex: 'x',
                render(_, row) {
                    return row.endTime == null ? '进行�? : '已结�?
                }
            },
            {
                title: '终止原因',
                dataIndex: 'deleteReason',
            },


            {
                title: '操作',
                dataIndex: 'option',
                render: (_, record) => (
                 <Button size='small' onClick={() => PageUtils.open('/flowable/user-task/instance/view?id='+record.id, '流程信息') }> 查看 </Button>

            ),
            },
        ]}
    />;
}
