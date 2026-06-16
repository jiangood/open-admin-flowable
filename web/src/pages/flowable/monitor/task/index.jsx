import {FieldUserSelect, HttpUtils, Page, ProTable} from "@jiangood/open-admin";
import {Button, Form, message, Modal} from "antd";
import React from "react";

export default class extends React.Component {


    state = {
        assigneeFormOpen:false,
        assigneeFormValues:{}
    }

    assigneeFormRef = React.createRef()
    taskTableRef = React.createRef()

    onClickSetAssignee = id => {
        this.setState({assigneeFormOpen:true,assigneeFormValues:{taskId:id}})
    };

    submitSetAssignee = values => {
        HttpUtils.post('admin/flowable/monitor/setAssignee',values).then(()=>{
            this.setState({assigneeFormOpen:false})
            this.taskTableRef.current.reload()
        }).catch(e => {
            message.error(e?.message || '指定处理人失败');
        })
    };
    render() {
        return <Page padding>
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
                        dataIndex:'id',
                        render:(id)=>{
                            return <Button size='small' onClick={()=>this.onClickSetAssignee(id)}>指定处理人</Button>
                        }
                    }
                ]}
                request={(params) => HttpUtils.get('admin/flowable/monitor/task', params)}
            >
                <Form.Item label='受理人' name='assignee'>
                    <FieldUserSelect />
                </Form.Item>
            </ProTable>
            <Modal title='指定处理人'
                   open={this.state.assigneeFormOpen}
                   onOk={()=>this.assigneeFormRef.current.submit()}
                   onCancel={()=>this.setState({assigneeFormOpen:false})}
                   destroyOnHidden
            >
                <Form ref={this.assigneeFormRef} onFinish={this.submitSetAssignee} initialValues={this.state.assigneeFormValues}>
                    <Form.Item name='taskId' noStyle>
                    </Form.Item>
                    <Form.Item name='assignee' label='用户'>
                        <FieldUserSelect />
                    </Form.Item>
                </Form>
            </Modal>
        </Page>
    }

}
