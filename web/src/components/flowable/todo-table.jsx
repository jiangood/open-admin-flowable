import {HttpUtils, LinkButton, ProTable} from "@jiangood/open-admin";
import React from "react";

const columns = [
    {title: '发起人', dataIndex: 'instanceStarter'},
    {title: '流程名称', dataIndex: 'instanceName'},
    {title: '当前节点', dataIndex: 'taskName', width: 100},
    {title: '当前操作人', dataIndex: 'assigneeInfo', width: 100},
    {title: '发起时间', dataIndex: 'instanceStartTime'},
    {title: '任务创建时间', dataIndex: 'createTime'},
    {
        title: '操作', dataIndex: 'option',
        render: (_, record) => {
            let path = '/flowable/user-task/form?taskId=' + record.id;
            return <LinkButton type='primary' path={path} label='处理任务'>处理</LinkButton>;
        },
    },
];

export default function TodoTable() {
    return <ProTable
        showToolbarSearch={false}
        request={(params) => HttpUtils.get('admin/flowable/user-task/todoTaskPage', params)}
        columns={columns}
        size='small'
    />;
}
