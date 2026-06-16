import React from "react";
import {Button, Form, Input, Select, Splitter} from "antd";
import {StringUtils} from "@jiangood/open-admin";
import {PlayCircleOutlined} from "@ant-design/icons";
import HistoryListPanel from "../../../components/flowable/HistoryListPanel";

export default class InitPhase extends React.Component {
    render() {
        const {model, users, submitting, historyList, historyLoading,
               onStart, onLoadUsers, onViewHistory, onDeleteHistory} = this.props;

        return (
            <Splitter>
                <Splitter.Panel defaultSize="55%">
                    <Form onFinish={onStart} layout="vertical">
                        <Form.Item name="key" noStyle initialValue={model.key}/>

                        <Form.Item label="业务标识" name="id"
                                   rules={[{required: true, message: '请输入业务标识'}]}
                                   initialValue={StringUtils.random(16)}>
                            <Input/>
                        </Form.Item>

                        <Form.Item label="发起人" name="initiatorId"
                                   rules={[{required: true, message: '请选择发起人'}]}>
                            <Select showSearch placeholder="搜索并选择用户"
                                    filterOption={false}
                                    onSearch={onLoadUsers}
                                    options={users.map(u => ({label: u.name, value: u.id}))}
                                    style={{width: 300}}/>
                        </Form.Item>

                        {model.variables?.map(item => (
                            <Form.Item key={item.name} name={['variables', item.name]} label={item.label}>
                                <Input/>
                            </Form.Item>
                        ))}

                        <Form.Item>
                            <Button type="primary" htmlType="submit" icon={<PlayCircleOutlined/>}
                                    loading={submitting}>
                                启动仿真
                            </Button>
                        </Form.Item>
                    </Form>
                </Splitter.Panel>
                <Splitter.Panel defaultSize="45%">
                    <HistoryListPanel list={historyList} loading={historyLoading}
                                     onView={onViewHistory} onDelete={onDeleteHistory}/>
                </Splitter.Panel>
            </Splitter>
        );
    }
}
