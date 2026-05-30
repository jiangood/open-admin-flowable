import {getBusinessObject, is} from 'bpmn-js/lib/util/ModelUtil';
import {Form, Input} from 'antd';

function getLoopCharacteristics(element) {
    const bo = getBusinessObject(element);
    return bo.loopCharacteristics;
}

export default function MultiInstanceSection({element, modeling}) {
    const loopCharacteristics = getLoopCharacteristics(element);

    if (!loopCharacteristics || !is(loopCharacteristics, 'bpmn:MultiInstanceLoopCharacteristics')) {
        return [];
    }

    const initialValues = {
        collection: loopCharacteristics.get('collection') || '',
        elementVariable: loopCharacteristics.get('elementVariable') || '',
    };

    return (
        <Form
            layout="vertical"
            initialValues={initialValues}
            onValuesChange={(changedValues) => {
                modeling.updateModdleProperties(element, loopCharacteristics, changedValues);
            }}
        >
            <Form.Item label="集合" name="collection">
                <Input placeholder="如 userList"/>
            </Form.Item>
            <Form.Item label="元素变量" name="elementVariable">
                <Input placeholder="如 user"/>
            </Form.Item>
        </Form>
    );
}
