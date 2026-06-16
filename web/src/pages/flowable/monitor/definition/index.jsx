import React from "react";
import {HttpUtils, ProTable} from "@jiangood/open-admin";

export default class extends React.Component {

    columns = [
        {
            title: 'ID',
            dataIndex: 'id',
        },
        {
            title: '分类',
            dataIndex: 'category',
        },
        {
            title: '名称',
            dataIndex: 'name',
        },
        {
            title: '键',
            dataIndex: 'key',
        },
        {
            title: '描述',
            dataIndex: 'description',
        },
        {
            title: '版本',
            dataIndex: 'version',
        },
        {
            title: '资源名称',
            dataIndex: 'resourceName',
        },
        {
            title: '部署ID',
            dataIndex: 'deploymentId',
        },
        {
            title: '图表资源名称',
            dataIndex: 'diagramResourceName',
        },
        {
            title: '是否有开始表单键',
            dataIndex: 'hasStartFormKey',
            render: (value) => value ? '是' : '否',
        },
        {
            title: '是否有图形符号',
            dataIndex: 'hasGraphicalNotation',
            render: (value) => value ? '是' : '否',
        },
        {
            title: '是否挂起',
            dataIndex: 'suspended',
            render: (value) => value ? '是' : '否',
        },
        {
            title: '租户ID',
            dataIndex: 'tenantId',
        },
        {
            title: '派生自',
            dataIndex: 'derivedFrom',
        },
        {
            title: '根派生来源',
            dataIndex: 'derivedFromRoot',
        },
        {
            title: '派生版本',
            dataIndex: 'derivedVersion',
        },


    ]

    render() {
        return <ProTable
            search={false}
            columns={this.columns}
            request={(params) => HttpUtils.get('admin/flowable/monitor/definitionPage', params)}
        >

        </ProTable>
    }
}
