import {Button, Empty, Spin, Table, Tag, Typography} from "antd";
import {HistoryOutlined} from "@ant-design/icons";

export default function HistoryListPanel({list, loading, onView, onDelete}) {
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
