import {Button} from "antd";
import {HttpUtils, PageUtils, ProTable} from "@jiangood/open-admin";
import React from "react";
import {USER_TASK_MY_INSTANCE} from "@/constants/api";
import {ROUTE_USER_INSTANCE_VIEW} from "@/constants/routes";

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
            <Button size='small' onClick={() => PageUtils.open(ROUTE_USER_INSTANCE_VIEW + '?id=' + record.id, '流程信息')}>查看</Button>
        ),
    },
];

export default function MyTable() {
    return <ProTable
        request={(params) => HttpUtils.get(USER_TASK_MY_INSTANCE, params)}
        columns={columns}
    />;
}
