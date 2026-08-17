import {Button, Input, InputNumber, Modal, Select} from "antd";
import {Component} from "react";
import {FieldBoolean, FieldTable, getToken, HttpClient, ObjectUtils, StringUtils} from "@jiangood/open-admin";
import {ConditionExpressionUtils} from "./ConditionExpressionUtils";


// 字符串的双引号
const QUOTE = '"';


const OPERATOR_DEFINITIONS = [
    {
        type: 'STRING',
        label: '等于',
        key: '==',
        component: Input,
    },
    {
        type: 'STRING',
        label: '不等于',
        key: '!=',
        component: Input,
    },
    {
        type: 'STRING',
        label: '包含',
        key: '.contains',
        component: Input,
    },
    {
        type: 'STRING',
        label: '开头等于',
        key: '.startWith',
        component: Input,
    },
    {
        type: 'STRING',
        label: '结尾等于',
        key: '.endWith',
        component: Input,
    },

    // =================== 数字 ================

    {
        type: 'NUMBER',
        label: '等于',
        key: '==',
        component: Input,
    },
    {
        type: 'NUMBER',
        label: '不等于',
        key: '!=',
        component: Input,
    },

    {
        type: 'NUMBER',
        label: '大于',
        key: '>',
        component: InputNumber,
    },
    {
        type: 'NUMBER',
        label: '小于',
        key: '<',
        component: InputNumber,
    },
    {
        type: 'NUMBER',
        label: '大于等于',
        key: '>=',
        component: InputNumber,
    },
    {
        type: 'NUMBER',
        label: '小于等于',
        key: '<=',
        component: InputNumber,
    },


    // ===================== 布尔值 =======================

    {
        type: 'BOOLEAN',
        label: '等于',
        key: '==',
        component: FieldBoolean,
    },

]


function encode(data) {
    let {left, op, right} = data;
    if (left == null || op == null || right == null) {
        return null
    }

    const isFun = op.startsWith('.')
    if (isFun) {
        return left + op + '("' + right + '")';
    }
    const isStr = right.startsWith('"')
    if (isStr) {
        right = '"' + right + '"';
    }

    return left + op + right;
}

function decode(expression) {
    const isFun = ConditionExpressionUtils.isFunction(expression);
    if (isFun) {
        return ConditionExpressionUtils.parseStrFunction(expression)
    }

    return ConditionExpressionUtils.parse(expression)
}


export class ConditionDesignButton extends Component {

    state = {
        open: false,
        varList: [],
        varOptions: [],
        editingArr: [], // 弹窗内本地编辑状态，点击确定后才提交
    }

    componentDidMount() {
        const {processId} = this.props;
        HttpClient.get('admin/flowable/model/varList', {code: processId}).then(rs => {
            const options = rs.data.map(r => {
                return {
                    label: r.label,
                    value: r.name
                }
            })
            this.setState({varList: rs.data, varOptions: options})
        })
    }

    // 弹窗内实时更新本地状态，不提交到模型
    handleTableChange = arr => {
        this.setState({ editingArr: arr })
    };

    // 确定：将本地状态提交到模型
    handleOk = () => {
        const str = this.convertArrToStr(this.state.editingArr)
        this.props.setValue(str, this.props.element, this.props.modeling, this.props.bpmnFactory)
        this.setState({ open: false })
    }

    // 取消：丢弃本地修改
    handleCancel = () => {
        this.setState({ open: false })
    }

    // 打开弹窗时，从当前元素读取最新表达式
    handleOpen = () => {
        let value = this.props.getValue(this.props.element);
        let arrValue = this.convertStrToArr(value);
        this.setState({ open: true, editingArr: arrValue })
    }

    getOptionsByItem = (record) => {
        let options = []
        let {varList} = this.state;
        let varItem = varList.find(t => t.name === record.left)

        if (varItem) {
            const {valueType} = varItem;
            const os = OPERATOR_DEFINITIONS.filter(o => o.type === valueType)
            for (let o of os) {
                options.push({
                    label: o.label,
                    value: o.key
                })
            }
        }

        return options;
    }

    columns = [
        {
            dataIndex: 'left', title: '变量名称',
            render: () => {
                return <Select options={this.state.varOptions} style={{width: 200}}></Select>
            }
        },
        {
            dataIndex: 'op', title: '操作符',
            render: (v, record) => {
                const options = this.getOptionsByItem(record)

                return <Select options={options} style={{width: 100}}></Select>
            }
        },
        {dataIndex: 'right', title: '值', width: 200},
    ];

    render() {
        const { editingArr } = this.state
        const previewExpression = this.convertArrToStr(editingArr)

        return <div style={{display: 'flex', justifyContent: 'right', padding: 8}}>
            <Button type='primary'
                    size='small'

                    styles={{
                        root: {
                            backgroundColor: getToken().colorPrimary
                        }
                    }}

                    onClick={this.handleOpen}

            >条件编辑器</Button>


            <Modal title='条件编辑器'
                   open={this.state.open}
                   width={600}
                   onOk={this.handleOk}
                   onCancel={this.handleCancel}
                   mask={{blur: false}}
                   destroyOnHidden
            >
                <FieldTable
                    columns={this.columns}
                    value={editingArr}
                    onChange={this.handleTableChange}
                />

                <div style={{
                    marginTop: 8,
                    color: '#999',
                    fontSize: 12,
                }}>
                    提示：暂不支持复杂表达式，复杂表达式请手动编辑
                </div>

                <div style={{
                    marginTop: 16,
                    padding: '8px 12px',
                    background: '#f6f8fa',
                    borderRadius: 6,
                    border: '1px solid #d9d9d9',
                    fontFamily: 'monospace',
                    fontSize: 13,
                    wordBreak: 'break-all',
                    minHeight: 36,
                    display: 'flex',
                    alignItems: 'center',
                }}>
                    <span style={{ color: '#999', marginRight: 8, flexShrink: 0 }}>表达式预览：</span>
                    <span style={{ color: previewExpression ? '#1a1a1a' : '#bbb' }}>
                        {previewExpression || '（空）'}
                    </span>
                </div>
            </Modal>

        </div>
    }

    convertStrToArr(value) {
        if (value) {
            value = StringUtils.removePrefix(value, "${")
            value = StringUtils.removeSuffix(value, "}")
            const strArr = StringUtils.split(value, '&&');
            return strArr.map(decode).filter(t => t != null)
        }
        return [];
    }


    convertArrToStr = arrValue => {
        const str = arrValue.map(encode).join('&&')

        return "${" + str + "}"
    };


}


