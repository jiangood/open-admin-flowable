import React from "react";
import {Gap, HttpUtils, Page, PageUtils, ProTable} from "@jiangood/open-admin";
import {Card, Empty, Skeleton, Table} from "antd";
export default class extends React.Component {
    state = {
        commentList: [],
        vars: {},
        id: null,
        starter: null,
        startTime: null,
        name: null,
        img: null,
        loading: true,
        errorMsg: null,
    }

    componentDidMount() {
        const params = PageUtils.currentParams();
        const { id, businessKey } = params;

        const reqParams = {id};
        if (businessKey) reqParams.businessKey = businessKey;
        HttpUtils.get('admin/flowable/user-task/getInstanceInfo', reqParams).then(rs => {
            this.setState(rs)
            this.setState({
                commentList: rs.commentList,
                img: rs.img,
                id: rs.id,
            })
        }).catch(e => {
            this.setState({errorMsg: e})
        }).finally(() => {
            this.setState({loading: false})
        })
    }

    getCommentColumns() {
        return [
            {dataIndex: 'content', title: '操作'},
            {dataIndex: 'user', title: '处理人'},
            {dataIndex: 'time', title: '处理时间'},
        ];
    }

    render() {
        if (this.state.errorMsg) {
            return <Empty description={this.state.errorMsg}></Empty>
        }

        const {commentList, img, loading, id} = this.state
        if (loading) {
            return <Skeleton/>
        }

        return (
            <Page padding>
                <Card title='流程图'>
                    <img src={img} style={{maxWidth: '100%'}}/>
                </Card>
                <Gap/>
                <Card title='审批记录'>
                    <Table dataSource={commentList}
                           size='small'
                           pagination={false}
                           rowKey='id'
                           columns={this.getCommentColumns()}
                    />
                </Card>
                <Gap/>
                {this.props.showVariables && (
                    <Card title='流程变量'>
                        <ProTable columns={[
                            {dataIndex: 'key', title: '变量名'},
                            {dataIndex: 'value', title: '变量值'},
                        ]}
                                  rowKey='key'
                                  request={() => HttpUtils.get('admin/flowable/monitor/instance/vars', {id})}
                        />
                    </Card>
                )}
            </Page>
        )
    }
}
