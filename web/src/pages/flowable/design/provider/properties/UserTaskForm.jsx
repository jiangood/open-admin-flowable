import {Form} from "antd";
import {FieldRemoteSelect, FieldRemoteSelectMultipleInline, StringUtils} from "@jiangood/open-admin";
import React from "react";

export default function UserTaskForm(props) {
    const {element, modeling} = props
    let initialValues = {
        assignee: element.businessObject.assignee,
        candidateGroups: element.businessObject.candidateGroups,
        candidateUsers: StringUtils.split(element.businessObject.candidateUsers, ',')
    };
    return (<div style={{padding: 8}}>
        <Form layout='vertical'
              size="small"
              initialValues={initialValues}
              onValuesChange={(changedValues) => {
                  modeling.updateProperties(element, changedValues);
              }}>
            <Form.Item label="办理人" name='assignee'>
                <FieldRemoteSelect url='admin/flowable/model/assigneeOptions'/>
            </Form.Item>
            <Form.Item label="候选组" name='candidateGroups'>
                <FieldRemoteSelect url='admin/flowable/model/candidateGroupsOptions'/>
            </Form.Item>
            <Form.Item label="候选人" name='candidateUsers'>
                <FieldRemoteSelectMultipleInline url='admin/flowable/model/candidateUsersOptions'/>
            </Form.Item>
        </Form>
    </div>)
}
