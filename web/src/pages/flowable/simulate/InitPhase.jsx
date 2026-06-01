import React from "react";
import {Button, Empty, Form, Input, Select, Splitter, Spin, Table, Tag, Typography} from "antd";
import {StringUtils} from "@jiangood/open-admin";
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
