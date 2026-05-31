import {Form, Input, InputNumber} from "antd";

export default function ({value, onChange}) {
    const onValuesChange = (_, allValues) => {
        onChange?.(allValues);
    };

    return <div>
        demo表单示例
        <Form onValuesChange={onValuesChange} initialValues={value}>
            <Form.Item label="事由" name="reason">
                <Input/>
            </Form.Item>
            <Form.Item label="请假天数" name="days">
                <InputNumber/>
            </Form.Item>
        </Form>
    </div>
}
