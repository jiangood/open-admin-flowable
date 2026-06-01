import React from "react";
import {Button, Empty, Space, Spin, Table, Tag} from "antd";
import {HistoryOutlined, ReloadOutlined} from "@ant-design/icons";

export default class FinishedPhase extends React.Component {
    render() {
        const {historyList, historyLoading,
               onReset, onViewHistory, onDeleteHistory} = this.props;

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
