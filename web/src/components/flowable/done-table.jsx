import {Button} from "antd";
import {HttpUtils, PageUtils, ProTable} from "@jiangood/open-admin";
import React from "react";
import {USER_TASK_DONE_PAGE} from "@/constants/api";
import {ROUTE_USER_INSTANCE_VIEW} from "@/constants/routes";

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
            <Button size='small' onClick={() => PageUtils.open(ROUTE_USER_INSTANCE_VIEW + '?id=' + record.id, '流程信息')}>查看</Button>
        ),
    },
];

export default function DoneTable() {
    return <ProTable
        showToolbarSearch={false}
        request={(params) => HttpUtils.get(USER_TASK_DONE_PAGE, params)}
        columns={columns}
        size='small'
    />;
}
