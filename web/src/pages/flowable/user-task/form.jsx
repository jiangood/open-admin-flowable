import React from "react";
import {Button, Card, Empty, Form, Input, message, Modal, Radio, Spin, Splitter, Table, Tabs, Typography,} from "antd";
import {history} from "umi";
import {FormRegistryUtils, Gap, HttpUtils, Page, PageUtils} from "@jiangood/open-admin";
import {FormOutlined, ShareAltOutlined} from "@ant-design/icons";

export default class extends React.Component {

    state = {
        submitLoading: false,


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


    externalFormRef = React.createRef()

    componentDidMount() {
        const {taskId} = PageUtils.currentParams()


        HttpUtils.get("admin/flowable/user-task/getInstanceInfoByTaskId", {taskId}).then(rs => {
            this.setState({data: rs})
        }).catch(e => {
            this.setState({errorMsg: e})
        }).finally(() => {
            this.setState({loading: false})
        })


    }

    onImgClick = () => {
        Modal.info({
            title: '流程图',
            width: '70vw',
            content: <div style={{width: '100%', overflow: 'auto', maxHeight: '80vh'}}>
                <img src={this.state.data.img}/>
            </div>
        })
    };


    handleTask = async value => {
        this.setState({submitLoading: true});
        try {
            if (value.result === 'APPROVE') {
                const fn = this.externalFormRef.current?.submit
                if (fn) {
                    await fn()
                }
            }

            value.taskId = this.state.data.taskId
            await HttpUtils.post("admin/flowable/user-task/handleTask", value)

            PageUtils.closeCurrent()
        } catch (error) {
            message.error(error)
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
                    <Tabs
                        items={[
                            {
                                key: '1',
                                label: '表单',
                                icon: <FormOutlined/>,
                                children: this.renderForm()
                            },
                            {
                                key: '2',
                                label: '流程图',
                                icon: <ShareAltOutlined/>,
                                children: this.renderProcess(img, commentList)
                            }
                        ]}>

                    </Tabs>
                </Splitter.Panel>
                <Splitter.Panel defaultSize={400}>
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
                </Splitter.Panel>

            </Splitter>


        </Page>


    }

    renderProcess = (img, commentList) => <Card title='处理记录'>
        <img src={img} style={{maxWidth: '100%'}}
             onClick={this.onImgClick}/>
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
        const {data} = this.state
        const {businessKey} = data
        const formKey = data.formKey;
        const formName = data.formKey + 'Form'

        let ExForm = FormRegistryUtils.get(formName);
        if (!ExForm) {
            console.error(" 表单不存在： " + formName + "，请检查表单源代码：src/forms/" + formName + ".jsx")
            return <Empty description={"表单不存在： " + formName}></Empty>
        }

        return <ExForm id={businessKey} formKey={formKey} ref={this.externalFormRef}></ExForm>
    }
}
