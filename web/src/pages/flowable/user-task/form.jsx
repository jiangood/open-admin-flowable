import React from "react";
import {Button, Card, Empty, Form, Input, message, Radio, Spin, Splitter, Table, Typography} from "antd";
import ProcessImageViewer from "../../../components/ProcessImageViewer";
import {FormRegistryUtils, Gap, HttpUtils, Page, PageUtils} from "@jiangood/open-admin";

export default class extends React.Component {

    state = {
        submitLoading: false,


        formData: {},


        instanceCommentList: [],
        vars: {},


        data: {
            taskId: null,
            commentList: [],
            img: null
        },
        loading: true,

        errorMsg: null
    }

    componentDidMount() {
        const {taskId} = PageUtils.currentParams()


        HttpUtils.get('admin/flowable/user-task/getInstanceInfoByTaskId', {taskId}).then(rs => {
            this.setState({data: rs})
        }).catch(e => {
            if (e?.message?.includes('任务已被处理')) {
                message.warning('此任务已被处理');
                setTimeout(() => PageUtils.closeCurrent(), 1500);
            } else {
                message.error(e?.message || '获取任务信息失败');
            }
            this.setState({errorMsg: e})
        }).finally(() => {
            this.setState({loading: false})
        })


    }


    handleTask = async value => {
        this.setState({submitLoading: true});
        try {
            if (value.result === 'APPROVE') {
                value.formData = this.state.formData
            }
            value.taskId = this.state.data.taskId
            await HttpUtils.post('admin/flowable/user-task/handleTask', value)

            PageUtils.closeCurrent()
        } catch (error) {
            message.error(error?.message || '操作失败')
        } finally {
            this.setState({submitLoading: false})
        }

    }

    render() {
        const {submitLoading} = this.state

        const {data, loading} = this.state
        const {commentList, img} = data
        if (loading) {
            return <Spin/>
        }
        return <Page padding>

            <Splitter>
                <Splitter.Panel>
                    <Typography.Title level={4}>{data.name}</Typography.Title>
                    <Typography.Text type="secondary">{data.starter} &nbsp;&nbsp; {data.startTime}</Typography.Text>
                    <Gap></Gap>
                    {this.renderForm()}
                </Splitter.Panel>
                <Splitter.Panel>
                    <Card title='审批意见'>
                        <Form
                            layout='vertical'
                            onFinish={this.handleTask}
                            disabled={submitLoading}
                        >

                            <Form.Item label='审批结果' name='result' rules={[{required: true, message: '请选择'}]}
                                       initialValue={'APPROVE'}>
                                <Radio.Group>
                                    <Radio value='APPROVE'>同意</Radio>
                                    <Radio value='REJECT'>不同意</Radio>
                                </Radio.Group>
                            </Form.Item>
                            <Form.Item label='审批意见' name='comment'
                                       rules={[{required: true, message: '请输入审批意见'}]}>
                                <Input.TextArea/>
                            </Form.Item>
                            <div>
                                <Button type='primary' htmlType='submit' loading={submitLoading}
                                        size={"middle"}>提交</Button>
                            </div>
                        </Form>
                    </Card>
                    <Gap></Gap>
                    {this.renderProcess(img, commentList)}
                </Splitter.Panel>

            </Splitter>


        </Page>


    }

    renderProcess = (img, commentList) => <Card title='处理记录'>
        <ProcessImageViewer imageUrl={img}/>
        <Gap></Gap>
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
    </Card>;

    renderForm = () => {
        const {data, formData} = this.state
        const {businessKey} = data
        const formKey = data.formKey;
        const formName = data.formKey + 'Form'

        let ExForm = FormRegistryUtils.get(formName);
        if (!ExForm) {
            console.error(" 表单不存在： " + formName + "，请检查表单源代码：src/forms/" + formName + ".jsx")
            return <Empty description={"表单不存在： " + formName}></Empty>
        }

        return <ExForm id={businessKey} formKey={formKey} value={formData}
                       onChange={v => this.setState({formData: v})}></ExForm>
    }
}
