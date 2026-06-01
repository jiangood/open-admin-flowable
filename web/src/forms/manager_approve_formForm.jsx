import {useEffect, useState} from "react";
import {Form, Input, InputNumber, Skeleton} from "antd";
import {HttpUtils} from "@jiangood/open-admin";

export default function ({value, onChange, id}) {
    const [loading, setLoading] = useState(false);
    const [initialValues, setInitialValues] = useState(value || {});

    useEffect(() => {
        if (id) {
            setLoading(true);
            HttpUtils.get('admin/flowable/example/leave/detail', {businessKey: id})
                .then(data => {
                    setInitialValues({
                        reason: data.reason,
                        days: data.days,
                        actualDays: data.actualDays ?? data.days,
                        leaveType: data.leaveType,
                    });
                })
                .finally(() => setLoading(false));
        }
    }, [id]);

    const onValuesChange = (_, allValues) => {
        onChange?.(allValues);
    };

    if (loading) return <Skeleton active/>;

    return <div>
        经理审批
        <Form onValuesChange={onValuesChange} initialValues={initialValues}>
            <Form.Item label="请假事由" name="reason">
                <Input disabled/>
            </Form.Item>
            <Form.Item label="申请天数" name="days">
                <InputNumber disabled/>
            </Form.Item>
            <Form.Item label="请假类型" name="leaveType">
                <Input disabled/>
            </Form.Item>
            <Form.Item label="实际批假天数" name="actualDays"
                       rules={[{required: true, message: '请输入实际批假天数'}]}>
                <InputNumber min={0} max={365} step={0.5}/>
            </Form.Item>
        </Form>
    </div>
}
