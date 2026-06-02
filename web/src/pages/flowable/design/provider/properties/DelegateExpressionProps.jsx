import {Select} from 'antd';
import {useEffect, useState} from 'react';
import {HttpUtils} from "@jiangood/open-admin";
import {MODEL_JAVA_DELEGATE_OPTIONS} from "@/constants/api";

export default function DelegateExpressionField({element, modeling}) {
    const [options, setOptions] = useState([]);

    useEffect(() => {
        HttpUtils.get(MODEL_JAVA_DELEGATE_OPTIONS).then(rs => {
            setOptions((rs || []).map(o => typeof o === 'string' ? {label: o, value: o} : o));
        });
    }, []);

    const value = element.businessObject?.delegateExpression || '';

    return (
        <div style={{padding: 8}}>
            <div style={{marginBottom: 4, fontSize: 12, color: '#666'}}>delegateExpression</div>
            <Select
                style={{width: '100%'}}
                value={value || undefined}
                placeholder="实现JavaDelegate接口的Bean名称， 如 ${demoDelegate}"
                options={options}
                onChange={(val) => modeling.updateProperties(element, {delegateExpression: val || ''})}
                allowClear
            />
        </div>
    );
}
