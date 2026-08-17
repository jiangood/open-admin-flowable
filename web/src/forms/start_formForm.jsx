import {Form, Input, InputNumber, Select} from "antd";

export default function ({value, onChange, id}) {
    if (value == null) {
        value = {};
    }

    const onValuesChange = (_, allValues) => {
        onChange?.(allValues);
    };

    return <div>
        请假申请
        <Form onValuesChange={onValuesChange} initialValues={value}>
            <Form.Item label="事由" name="reason" rules={[{required: true, message: '请输入请假事由'}]}>
                <Input/>
            </Form.Item>
            <Form.Item label="申请天数" name="days" rules={[{required: true, message: '请输入申请天数'}]}>
                <InputNumber min={0.5} max={365} step={0.5}/>
            </Form.Item>
            <Form.Item label="请假类型" name="leaveType" rules={[{required: true, message: '请选择请假类型'}]}>
                <Select options={[
                    {label: '事假', value: '事假'},
                    {label: '病假', value: '病假'},
                    {label: '年假', value: '年假'},
                    {label: '婚假', value: '婚假'},
                ]}/>
            </Form.Item>
        </Form>
    </div>
}
