import React from 'react';
import {Form, Input} from 'antd';

export default function GeneralSection({element, modeling}) {
    return (
        <Form
            layout="vertical"
            initialValues={{name: element.businessObject?.name || ''}}
            onValuesChange={(changedValues) => {
                modeling.updateProperties(element, changedValues);
            }}
        >
            <Form.Item label="ID">
                <Input value={element.id} disabled variant="borderless"/>
            </Form.Item>
            <Form.Item label="名称" name="name">
                <Input placeholder="元素名称"/>
            </Form.Item>
        </Form>
    );
}
