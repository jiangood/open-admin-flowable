import React from "react";
import {Button, Card, message, Modal, Space, Splitter} from "antd";

import 'bpmn-js/dist/assets/diagram-js.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css'
import BpmnModeler from 'bpmn-js/lib/Modeler'

import './index.css'
import customTranslate from "./customTranslate/customTranslate";
import contextPad from "./contextPad";
import {CloudUploadOutlined, SaveOutlined} from "@ant-design/icons";
import {HttpUtils, PageLoading, PageUtils, ProTable} from "@jiangood/open-admin";
import 'bpmn-js/dist/assets/bpmn-js.css';
import '@bpmn-io/properties-panel/assets/properties-panel.css';
import {BpmnPropertiesPanelModule, BpmnPropertiesProviderModule} from 'bpmn-js-properties-panel';

import flowablePropertiesProviderModule from './provider';
import flowableJson from './descriptors/flowable';

export default class extends React.Component {


    state = {
        id: null,
        model: null,

        deployedModal: false
    }

    bpmRef = React.createRef()


    async componentDidMount() {
        let params = PageUtils.currentParams()
        this.state.id = params.id
        const rs = await HttpUtils.get('admin/flowable/model/detail', {id: this.state.id})
        this.setState({model: rs}, this.initBpmn)
    }

    initBpmn = () => {
        let container = this.bpmRef.current;
        let xml = this.state.model.content;

        this.bpmnModeler = new BpmnModeler({
            container: container,
            propertiesPanel: {parent: '#js-properties-panel',},
            additionalModules: [
                {translate: ['value', customTranslate]},
                contextPad,
                BpmnPropertiesPanelModule,
                BpmnPropertiesProviderModule,
                flowablePropertiesProviderModule
            ],
            moddleExtensions: {
                flowable: flowableJson
            }
        });

        console.log('导入的xml内容如下')
        console.log(xml)
        this.bpmnModeler.importXML(xml)
        this.bpmnModeler.on('element.contextmenu', e => e.preventDefault()) // 关闭右键，影响操作
    };


    showXML = () => {
        this.bpmnModeler.saveXML({format: true}).then(res => {
            Modal.info({content: <pre style={{overflowX: "auto", height: '64vh'}}>{res.xml}</pre>, width: 1024})
        })
    }


    handleSave = async () => {
        let id = this.state.id;
        const hide = message.loading('正在保存...', 0)
        try {
            const res = await this.bpmnModeler.saveXML();
            await HttpUtils.post('admin/flowable/model/saveContent', {id, content: res.xml});
        } finally {
            hide()
        }

    }
    handleDeploy = async () => {
        let id = this.state.id;
        const hide = message.loading('正在部署...', 0)
        try {
            const res = await this.bpmnModeler.saveXML();
            await HttpUtils.post('admin/flowable/model/deploy', {id, content: res.xml});
        } finally {
            hide()
        }
    }


    render() {
        if (this.state.model == null) {
            return <PageLoading />
        }
        return <Card title={'流程设计  ' + this.state.model?.name}
                     extra={<Space>
                         <Button type='primary' icon={<SaveOutlined/>} onClick={this.handleSave}>暂存</Button>
                         <Button type='primary' danger icon={<CloudUploadOutlined/>}
                                 onClick={this.handleDeploy}>部署</Button>
                         <Button onClick={this.showXML}>XML</Button>
                         <Button
                             onClick={() => PageUtils.open('/flowable/test?id=' + this.state.id, "流程测试")}> 测试 </Button>

                         <Button title='查看已部署的历史版本' onClick={() => {
                             this.setState({deployedModal: true})
                         }}>历史版本</Button>
                     </Space>}>


            <Splitter style={{minHeight: 'calc(100vh - 200px)'}}>
                <Splitter.Panel>
                    <div ref={this.bpmRef} style={{width: '100%', height: '100%'}}></div>
                </Splitter.Panel>

                <Splitter.Panel defaultSize={300}>
                    <div id={'js-properties-panel'}></div>
                </Splitter.Panel>
            </Splitter>


            <Modal title='已部署版本' width={800} footer={null}
                   open={this.state.deployedModal}
                   destroyOnHidden
                   onCancel={() => this.setState({deployedModal: false})}>

                <ProTable
                    columns={[
                        {
                            dataIndex: 'key',
                            title: '编码'
                        },
                        {
                            dataIndex: 'name',
                            title: '名称'
                        },
                        {
                            dataIndex: 'version',
                            title: '版本号'
                        }, {
                            title: '操作',
                            dataIndex:'id',
                            render:(id)=> {
                                return <Button type='primary' onClick={()=>{
                                    HttpUtils.get('admin/flowable/model/getDefinitionContent',{id}).then(xml=>{
                                        this.bpmnModeler.importXML(xml)
                                        this.setState({deployedModal:false})
                                    })
                                }}>加载</Button>
                            }
                        }
                    ]}
                    request={params => {
                        params.key = this.state.model.key
                        return HttpUtils.get('admin/flowable/model/definitionPage', params)
                    }}>

                </ProTable>

            </Modal>

        </Card>
    }


}
