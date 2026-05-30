import React from 'react';
import { Collapse } from 'antd';
import { is } from 'bpmn-js/lib/util/ModelUtil';

import GeneralSection from './provider/properties/GeneralSection.jsx';
import DelegateExpressionField from './provider/properties/DelegateExpressionProps.jsx';
import AssignmentSection from './provider/properties/AssignmentSection.jsx';
import FormField from './provider/properties/FormProps.jsx';
import ConditionSection from './provider/properties/ConditionProps.jsx';
import MultiInstanceSection from './provider/properties/MultiInstanceProps.jsx';

export default class PropertiesPanel extends React.Component {
  state = {
    element: null,
    services: null,
  };

  componentDidMount() {
    this.initModeler(this.props.modeler);
  }

  componentDidUpdate(prevProps) {
    if (this.props.modeler && this.props.modeler !== prevProps.modeler) {
      this.initModeler(this.props.modeler);
    }
  }

  initModeler(modeler) {
    if (!modeler) return;

    this.setState({
      services: {
        modeling: modeler.get('modeling'),
        moddle: modeler.get('moddle'),
        canvas: modeler.get('canvas'),
      },
    });

    modeler.on('selection.changed', this.onSelectionChanged);

    const selection = modeler.get('selection');
    if (selection) {
      const selected = selection.get();
      if (selected && selected.length > 0) {
        this.setState({ element: selected[0] });
      }
    }
  }

  componentWillUnmount() {
    const { modeler } = this.props;
    if (modeler) {
      modeler.off('selection.changed', this.onSelectionChanged);
    }
  }

  onSelectionChanged = (e) => {
    const element = e.newSelection?.[0] || null;
    this.setState({ element });
  };

  render() {
    const { element, services } = this.state;
    if (!element || !services) {
      return (
        <div style={{ padding: 16, color: '#999', fontSize: 13 }}>
          选择一个节点以编辑属性
        </div>
      );
    }

    const { modeling, moddle, canvas } = services;
    const rootElement = canvas.getRootElement();
    const processId = rootElement?.id;

    const items = [
      {
        key: 'general',
        label: '通用',
        children: (
          <div key={element.id}>
            <GeneralSection element={element} modeling={modeling} />
          </div>
        ),
      },
    ];

    if (is(element, 'bpmn:ServiceTask')) {
      items.push({
        key: 'processBean',
        label: '处理器',
        children: (
          <div key={element.id}>
            <DelegateExpressionField element={element} modeling={modeling} />
          </div>
        ),
      });
    }

    if (is(element, 'bpmn:UserTask')) {
      items.push({
        key: 'user',
        label: '用户',
        children: (
          <div key={element.id}>
            <AssignmentSection element={element} modeling={modeling} />
          </div>
        ),
      });
      items.push({
        key: 'form',
        label: '表单',
        children: (
          <div key={element.id}>
            <FormField
              element={element}
              modeling={modeling}
              processId={processId}
            />
          </div>
        ),
      });
    }

    if (is(element, 'bpmn:SequenceFlow')) {
      items.push({
        key: 'condition',
        label: '条件',
        children: (
          <div key={element.id}>
            <ConditionSection
              element={element}
              modeling={modeling}
              moddle={moddle}
              processId={processId}
            />
          </div>
        ),
      });
    }

    const loopChar = element.businessObject?.loopCharacteristics;
    if (loopChar && is(loopChar, 'bpmn:MultiInstanceLoopCharacteristics')) {
      items.push({
        key: 'multiInstance',
        label: '多实例（集合设置）',
        children: (
          <div key={element.id}>
            <MultiInstanceSection element={element} modeling={modeling} />
          </div>
        ),
      });
    }

    return (
      <Collapse
        items={items}
        defaultActiveKey={items.map((i) => i.key)}
        style={{ borderRadius: 0 }}
      />
    );
  }
}
