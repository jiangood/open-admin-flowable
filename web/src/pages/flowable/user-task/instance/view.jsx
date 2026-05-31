import React from "react";
import {Gap, HttpUtils, Page, PageUtils, ProTable} from "@jiangood/open-admin";
import {Card, Empty, Skeleton, Table} from "antd";

export default class extends React.Component {
    state = {
        instanceCommentList: [],
        vars: {},

        id: null,
        starter: null,
        startTime: null,
        name: null,

        data: {
            commentList: [],
            img: null
        },
        loading: true,

        errorMsg: null
    }


    componentDidMount() {
        const { id} = PageUtils.currentParams()

        HttpUtils.get("admin/flowable/user-task/getInstanceInfo", {id}).then(rs => {
            this.setState(rs)
            this.setState({data: rs})
        }).catch(e => {
            this.setState({errorMsg: e})
        }).finally(() => {
            this.setState({loading: false})
        })

    }


    render() {

        if (this.state.errorMsg) {
            return <Empty description={this.state.errorMsg}></Empty>
        }

        const {data, loading} = this.state
        const {commentList, img} = data
        if (loading) {
            return <Skeleton/>
        }


        return <Page padding>
            <Card title='流程图'>
                <img src={img} style={{maxWidth: '100%'}}/>
            </Card>
            <Gap/>
            <Card title='审批记录'>
                <Table dataSource={commentList}
                       size='small'
                       pagination={false}
                       rowKey='id'
                       columns={[
                           {
                               dataIndex: 'content',
                               title: '操作'
                           },
                           {
                               dataIndex: 'user',
                               title: '处理人',
                           },
                           {
                               dataIndex: 'time',
                               title: '处理时间'
                           },
                       ]}
                />
            </Card>


        </Page>
    }


}
