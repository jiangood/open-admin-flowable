import React from "react";
import {Button, Card, Form, Input, Modal} from "antd";
import {HttpUtils, PageLoading, PageUtils, StringUtils} from "@jiangood/open-admin";

export default class extends React.Component {

  state = {
    model: undefined
  }


  componentDidMount() {
    let params = PageUtils.currentParams()
    const id = this.id = params.id

    HttpUtils.get('admin/flowable/simulate/get', {id}).then(rs=>{
        this.setState({model: rs})
    })

  }


  render() {
    if(this.state.model === undefined){
      return <PageLoading />
    }

    return <Card title={'流程仿真 / 【' + this.state.model.name + "】 / " + this.state.model.key }>
      <Form onFinish={this.onFinish} layout='vertical' >
        <Form.Item name='key' noStyle initialValue={this.state.model.key}>
        </Form.Item>

        <Form.Item name='id' label='业务标识(相当于业务表的id)' rules={[{required: true}]} initialValue={StringUtils.random(16)}>
          <Input />
        </Form.Item>

        {this.state.model.variables.map(item=><Form.Item key={item.name} name={item.name} label={item.label}>
          <Input />
        </Form.Item>)}



        <Form.Item label='   ' colon={false}>
          <Button htmlType="submit" type='primary'>提交</Button>
        </Form.Item>
      </Form>
    </Card>
  }

  onFinish = values => {
    HttpUtils.post('admin/flowable/simulate/submit', values).then(rs=>{
      Modal.confirm({content: '跳转任务列表?', onOk: () => {
        PageUtils.open('/flowable/monitor/task')
      }})
    })
  };
}
