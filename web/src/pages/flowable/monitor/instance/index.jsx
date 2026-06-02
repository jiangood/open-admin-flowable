import {Button, message, Popconfirm, Space} from "antd";
import {HttpUtils, PageUtils, ProTable} from "@jiangood/open-admin";
import React from "react";
import {MONITOR_PROCESS_INSTANCE_CLOSE, MONITOR_INSTANCE_PAGE} from "@/constants/api";
import {ROUTE_MONITOR_INSTANCE_VIEW} from "@/constants/routes";

export default class extends React.Component {

        columns = [
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
                    <Button size='small' onClick={() => PageUtils.open(ROUTE_MONITOR_INSTANCE_VIEW + '?id=' + r.id, '查看流程')}>查看</Button>
                    <Popconfirm title={'关闭流程'}
                                onConfirm={() => this.close(r.id)}>
                        <Button size='small' >终止</Button>
                    </Popconfirm></Space>
            }
        }

    ]

    close = (id) => {
        HttpUtils.get(MONITOR_PROCESS_INSTANCE_CLOSE, {id}).then((rs) => {
            this.tableRef.current.reload()
        }).catch(e => {
            message.error(e?.message || '关闭流程失败');
        })
    }

    tableRef = React.createRef()

    render() {
        return <ProTable
            actionRef={this.tableRef}
            columns={this.columns}
            request={(params) => HttpUtils.get(MONITOR_INSTANCE_PAGE, params)}
        >

        </ProTable>
    }
}
