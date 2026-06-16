import React from "react";
import {Tabs} from "antd";
import {Page, PageLoading} from "@jiangood/open-admin";
import TodoTable from "../../../components/flowable/todo-table";
import DoneTable from "../../../components/flowable/done-table";
import MyTable from "../../../components/flowable/my-table";

export default class extends React.Component {
    state = { show: true }

    render() {
        if (!this.state.show) {
            return <PageLoading/>
        }

        const items = [
            {label: '待办任务', key: '1', children: <TodoTable/>},
            {label: '已办任务', key: '2', children: <DoneTable/>},
            {label: '我发起的', key: '3', children: <MyTable/>},
        ];

        return <Page padding>
            <Tabs defaultActiveKey="1" destroyOnHidden items={items}/>
        </Page>
    }
}
