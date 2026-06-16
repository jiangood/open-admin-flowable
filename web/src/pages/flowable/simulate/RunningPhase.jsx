import React from "react";
import {Button, Card, Empty, Input, Select, Space, Splitter, Table, Tag, Typography} from "antd";
import {CheckCircleOutlined, CloseCircleOutlined} from "@ant-design/icons";
import ProcessImageViewer from "../../../components/ProcessImageViewer";

export default class RunningPhase extends React.Component {
    render() {
        const {status, submitting, taskFormValues, users, onLoadUsers,
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
