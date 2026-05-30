import { Input } from 'antd';
import { useRef, useState } from 'react';
import { ConditionDesignButton } from './ConditionDesign';

function getExpressionValue(element) {
  const condition = element.businessObject?.conditionExpression;
  return condition ? condition.body : '';
}

function setExpressionValue(value, element, modeling, moddle) {
  const businessObject = element.businessObject;
  let conditionExpression = businessObject.conditionExpression;

  if (!value) {
    modeling.updateProperties(element, { conditionExpression: undefined });
    return;
  }

  if (!conditionExpression) {
    conditionExpression = moddle.create('bpmn:FormalExpression');
    modeling.updateProperties(element, { conditionExpression });
  }

  modeling.updateModdleProperties(element, conditionExpression, { body: value });
}

export default function ConditionSection({ element, modeling, moddle, processId }) {
  const [value, setValue] = useState(getExpressionValue(element));
  const timerRef = useRef(null);

  const handleChange = (e) => {
    const v = e.target.value;
    setValue(v);
    clearTimeout(timerRef.current);
    timerRef.current = setTimeout(() => {
      setExpressionValue(v, element, modeling, moddle);
    }, 300);
  };

  return (
    <div style={{ padding: 8 }}>
      <div style={{ marginBottom: 4, fontSize: 12, color: '#666' }}>
        条件表达式(JUEL)
      </div>
      <Input.TextArea
        value={value}
        onChange={handleChange}
        placeholder="条件表达式(JUEL)"
        rows={3}
      />
      <div style={{ display: 'flex', justifyContent: 'right', marginTop: 8 }}>
        <ConditionDesignButton
          element={element}
          modeling={modeling}
          bpmnFactory={moddle}
          processId={processId}
          getValue={getExpressionValue}
          setValue={(v, el, mod, mdl) => {
            setExpressionValue(v, el || element, mod || modeling, mdl || moddle);
            setValue(v);
          }}
        />
      </div>
    </div>
  );
}
