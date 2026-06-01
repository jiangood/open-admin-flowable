import React from "react";
import {Button, Descriptions, Form, Input, InputNumber, message, Modal, Select, Space, Table, Tag} from "antd";
import {HttpUtils, Page} from "@jiangood/open-admin";

export default class extends React.Component {
    state = {
        data: [],
        loading: false,
        startModal: false,
        startLoading: false,
        detail: null,
        detailOpen: false,
    }

    componentDidMount() {
        this.loadList();
    }

    loadList = async () => {
        this.setState({loading: true});
        try {
            const data = await HttpUtils.get('admin/flowable/example/leave/list');
            this.setState({data: Array.isArray(data) ? data : []});
        } finally {
            this.setState({loading: false});
        }
    }

    showDetail = async (businessKey) => {
        const detail = await HttpUtils.get('admin/flowable/example/leave/detail', {businessKey});
        this.setState({detail, detailOpen: true});
    }

    handleStart = async (values) => {
        this.setState({startLoading: true});
        try {
            await HttpUtils.post('admin/flowable/example/leave/start', values);
            message.success('发起成功');
            this.setState({startModal: false});
            this.loadList();
        } catch (e) {
            message.error(e);
        } finally {
            this.setState({startLoading: false});
        }
    }

    render() {
        const {data, loading, startModal, startLoading, detail, detailOpen} = this.state;

        const colorMap = {审批中: 'processing', 已通过: 'success', 已拒绝: 'error'};

        const columns = [
            {title: '业务编号', dataIndex: 'businessKey'},
            {title: '请假事由', dataIndex: 'reason'},
            {title: '请假类型', dataIndex: 'leaveType'},
            {title: '申请天数', dataIndex: 'days'},
            {title: '实际批假天数', dataIndex: 'actualDays'},
            {
                title: '状态', dataIndex: 'status',
                render: (v) => <Tag color={colorMap[v] || 'default'}>{v}</Tag>
            },
            {title: '操作', render: (_, record) => (
                <Button size="small" onClick={() => this.showDetail(record.businessKey)}>查看</Button>
            )}
        ];

        return <Page padding>
            <Space style={{marginBottom: 16}}>
                <Button type="primary" onClick={() => this.setState({startModal: true})}>发起请假申请</Button>
                <Button onClick={this.loadList}>刷新</Button>
            </Space>

            <Table rowKey="id" loading={loading} dataSource={data} columns={columns} size="small" pagination={false}/>

            <Modal title="发起请假申请" open={startModal}
                   onCancel={() => this.setState({startModal: false})}
                   footer={null} destroyOnClose>
                <Form layout="vertical" onFinish={this.handleStart}>
                    <Form.Item label="请假事由" name="reason" rules={[{required: true, message: '请输入请假事由'}]}>
                        <Input/>
                    </Form.Item>
                    <Form.Item label="请假天数" name="days" rules={[{required: true, message: '请输入请假天数'}]}>
                        <InputNumber min={0.5} max={365} step={0.5} style={{width: '100%'}}/>
                    </Form.Item>
                    <Form.Item label="请假类型" name="leaveType" rules={[{required: true, message: '请选择请假类型'}]}>
                        <Select options={[
                            {label: '事假', value: '事假'},
                            {label: '病假', value: '病假'},
                            {label: '年假', value: '年假'},
                            {label: '婚假', value: '婚假'},
                        ]}/>
                    </Form.Item>
                    <Form.Item>
                        <Button type="primary" htmlType="submit" loading={startLoading}>提交</Button>
                    </Form.Item>
                </Form>
            </Modal>

            <Modal title="请假详情" open={detailOpen}
                   onCancel={() => this.setState({detailOpen: false})}
                   footer={null} destroyOnClose>
                {detail && (
                    <Descriptions bordered column={1} size="small">
                        <Descriptions.Item label="业务编号">{detail.businessKey}</Descriptions.Item>
                        <Descriptions.Item label="请假事由">{detail.reason}</Descriptions.Item>
                        <Descriptions.Item label="请假类型">{detail.leaveType}</Descriptions.Item>
                        <Descriptions.Item label="申请天数">{detail.days}</Descriptions.Item>
                        <Descriptions.Item label="实际批假天数">{detail.actualDays}</Descriptions.Item>
                        <Descriptions.Item label="状态">
                            <Tag color={colorMap[detail.status]}>{detail.status}</Tag>
                        </Descriptions.Item>
                    </Descriptions>
                )}
            </Modal>
        </Page>
    }
}
