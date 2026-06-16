import {Select} from 'antd';
import {useEffect, useState} from 'react';
import {HttpUtils} from "@jiangood/open-admin";

export default function FormField({element, modeling, processId}) {
    const [options, setOptions] = useState([]);

    useEffect(() => {
        HttpUtils.get('admin/flowable/model/formOptions', {code: processId}).then(rs => {
            setOptions((rs || []).map(o => typeof o === 'string' ? {label: o, value: o} : o));
        });
    }, [processId]);

    const value = element.businessObject?.formKey || '';

    return (
        <div style={{padding: 8}}>
            <div style={{marginBottom: 4, fontSize: 12, color: '#666'}}>选择表单</div>
            <Select
                style={{width: '100%'}}
                value={value || undefined}
                options={[{value: '', label: '<留空>'}, ...options]}
                onChange={(val) => modeling.updateProperties(element, {formKey: val || ''})}
                allowClear
            />
        </div>
    );
}
