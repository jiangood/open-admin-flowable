import {useEffect, useState} from "react";
import {Descriptions, Skeleton, Tag} from "antd";
import {HttpClient} from "@jiangood/open-admin";

export default function ({id}) {
    const [loading, setLoading] = useState(false);
    const [data, setData] = useState(null);

    useEffect(() => {
        if (id) {
            setLoading(true);
            HttpClient.get('admin/flowable/example/leave/detail', {businessKey: id})
                .then(rs => setData(rs.data))
                .finally(() => setLoading(false));
        }
    }, [id]);

    if (loading) return <Skeleton active/>;
    if (!data) return null;

    const colorMap = {审批中: 'processing', 已通过: 'success', 已拒绝: 'error'};

    return <div>
        流程结果
        <Descriptions bordered column={1} size="small">
            <Descriptions.Item label="请假事由">{data.reason}</Descriptions.Item>
            <Descriptions.Item label="请假类型">{data.leaveType}</Descriptions.Item>
            <Descriptions.Item label="申请天数">{data.days}</Descriptions.Item>
            <Descriptions.Item label="实际批假天数">{data.actualDays}</Descriptions.Item>
            <Descriptions.Item label="状态">
                <Tag color={colorMap[data.status] || 'default'}>{data.status}</Tag>
            </Descriptions.Item>
        </Descriptions>
    </div>
}
