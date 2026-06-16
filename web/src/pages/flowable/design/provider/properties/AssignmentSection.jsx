import {Form, Radio} from "antd";
import {FieldRemoteSelect, FieldRemoteSelectMultipleInline, StringUtils} from "@jiangood/open-admin";
import React from "react";

export default function AssignmentSection(props) {
    const {element, modeling} = props
    const businessObject = element.businessObject

    const getMode = () => {
        if (businessObject.assignee) return 'assignee'
        if (businessObject.candidateGroups) return 'candidateGroups'
        if (businessObject.candidateUsers) return 'candidateUsers'
        return 'assignee'
    }

    const [mode, setMode] = React.useState(getMode())
    const [formKey, setFormKey] = React.useState(0)

    const initialValues = {
        assignee: businessObject.assignee,
        candidateGroups: businessObject.candidateGroups,
        candidateUsers: StringUtils.split(businessObject.candidateUsers, ','),
    }

    const handleModeChange = (e) => {
        const newMode = e.target.value
        if (newMode === mode) return

        // 切换模式时清空所有指派字段，确保三选一
        modeling.updateProperties(element, {
            assignee: undefined,
            candidateGroups: undefined,
            candidateUsers: undefined,
        })

        setMode(newMode)
        setFormKey(k => k + 1)
    }

    return (<div style={{padding: 8}}>
        <Radio.Group
            optionType="button"
            buttonStyle="solid"
            value={mode}
            onChange={handleModeChange}
            options={[
                {label: '直接指派', value: 'assignee'},
                {label: '候选组', value: 'candidateGroups'},
                {label: '候选人', value: 'candidateUsers'},
            ]}
            style={{marginBottom: 12, display: 'flex'}}
        />
        <Form key={formKey} layout='vertical' initialValues={initialValues}
              onValuesChange={(changedValues) => {
                  modeling.updateProperties(element, changedValues);
              }}>
            {mode === 'assignee' && (
                <Form.Item label="办理人" name='assignee'>
                    <FieldRemoteSelect url='admin/flowable/model/assigneeOptions'/>
                </Form.Item>
            )}
            {mode === 'candidateGroups' && (
                <Form.Item label="候选组" name='candidateGroups'>
                    <FieldRemoteSelect url='admin/flowable/model/candidateGroupsOptions'/>
                </Form.Item>
            )}
            {mode === 'candidateUsers' && (
                <Form.Item label="候选人" name='candidateUsers'>
                    <FieldRemoteSelectMultipleInline url='admin/flowable/model/candidateUsersOptions'/>
                </Form.Item>
            )}
        </Form>
    </div>)
}
