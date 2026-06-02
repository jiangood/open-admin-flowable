import {Button, Popconfirm, Space} from 'antd';
import React from 'react';
import {ButtonList, HttpUtils, Page, PageUtils, ProTable} from "@jiangood/open-admin";
import {MODEL_PAGE, MODEL_DELETE} from "@/constants/api";
import {ROUTE_DESIGN, ROUTE_MONITOR_TASK, ROUTE_MONITOR_INSTANCE, ROUTE_MONITOR_DEFINITION} from "@/constants/routes";

export default class extends React.Component {


    actionRef = React.createRef();


    columns = [
        {
            title: '名称',
            dataIndex: 'name',
        },
        {
            title: '代码',
            dataIndex: 'key'
        },


        {
            title: '版本',
            dataIndex: 'version',
        },
        {
            title: '更新时间',
            dataIndex: 'lastUpdateTime',
        },


        {
            title: '操作',
            dataIndex: 'option',
            render: (_, record) => (
                <Space>
                    <Button size='small' type='primary'
                            onClick={() => PageUtils.open(ROUTE_DESIGN + '?id=' + record.id, '流程设计' + record.name)}> 设计 </Button>
                    <Popconfirm perm='flowable/model:delete' title={'是否确定删除流程模型'}
                                onConfirm={() => this.handleDelete(record)}>
                        <Button size='small' danger>删除</Button>
                    </Popconfirm>
                </Space>
            ),
        },
    ];


    handleDelete = row => {
        HttpUtils.post(MODEL_DELETE, {id: row.id}).then(rs => {
            this.actionRef.current.reload();
        })
    }


    render() {

        return <Page padding>
            <ProTable
                actionRef={this.actionRef}
                request={(params) => HttpUtils.get(MODEL_PAGE, params)}
                columns={this.columns}
                showToolbarSearch={true}
                toolBarRender={() => {
                    return <ButtonList>
                        <Button onClick={() => PageUtils.open(ROUTE_MONITOR_TASK, "运行中的任务")}>
                            运行中的任务
                        </Button>
                        <Button onClick={() => PageUtils.open(ROUTE_MONITOR_INSTANCE, "运行中的流程实例")}>
                            运行中的流程实例
                        </Button>
                        <Button onClick={() => PageUtils.open(ROUTE_MONITOR_DEFINITION, "已部署的流程定义")}>
                            已部署的流程定义
                        </Button>
                    </ButtonList>
                }}
            />


        </Page>
    }


}


