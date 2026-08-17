import React, { createRef } from "react";
import {Button, Card, message, Modal, Space, Splitter} from "antd";

import 'bpmn-js/dist/assets/diagram-js.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css'
import BpmnModeler from 'bpmn-js/lib/Modeler'

import './index.css'
import customTranslate from "./customTranslate/customTranslate";
import contextPad from "./contextPad";
import {CloudUploadOutlined, DownloadOutlined, SaveOutlined, UploadOutlined} from "@ant-design/icons";
import {HttpClient, PageLoading, PageUtils, ProTable} from "@jiangood/open-admin";
import 'bpmn-js/dist/assets/bpmn-js.css';
import flowableJson from './descriptors/flowable';
import PropertiesPanel from './PropertiesPanel';

export default class extends React.Component {


    state = {
        id: null,
        model: null,
        bpmnModeler: null,
        deployedModal: false
    }

    bpmRef = React.createRef()
    fileRef = createRef()


    async componentDidMount() {
        const params = PageUtils.currentParams()
        const rs = await HttpClient.get('admin/flowable/model/detail', {id: params.id})
        this.setState({model: rs.data, id: params.id}, this.initBpmn)
    }

    initBpmn = () => {
        let container = this.bpmRef.current;
        let xml = this.state.model.content;

        this.bpmnModeler = new BpmnModeler({
            container: container,
            additionalModules: [
                {translate: ['value', customTranslate]},
                contextPad,
            ],
            moddleExtensions: {
                flowable: flowableJson
            }
        });

        console.log('导入的xml内容如下')
        console.log(xml)
        this.bpmnModeler.importXML(xml)
        this.bpmnModeler.on('element.contextmenu', e => e.preventDefault()) // 关闭右键，影响操作
        this.setState({bpmnModeler: this.bpmnModeler});
    };


    handleExportXML = async () => {
        const res = await this.bpmnModeler.saveXML({format: true});
        const blob = new Blob([res.xml], {type: 'application/xml'});
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `${this.state.model.key}.bpmn20.xml`;
        a.click();
        URL.revokeObjectURL(url);
    }

    handleImportXML = () => {
        this.fileRef.current.click();
    }

    handleFileChange = (e) => {
        const file = e.target.files[0];
        if (!file) return;
        const reader = new FileReader();
        reader.onload = (evt) => {
            this.bpmnModeler.importXML(evt.target.result);
        };
        reader.readAsText(file);
        e.target.value = '';
    }


    handleSave = async () => {
        let id = this.state.id;
        const hide = message.loading('正在保存...', 0)
        try {
            const res = await this.bpmnModeler.saveXML();
            await HttpClient.post('admin/flowable/model/saveContent', {id, content: res.xml});
        } finally {
            hide()
        }

    }
    handleDeploy = async () => {
        let id = this.state.id;
        const hide = message.loading('正在部署...', 0)
        try {
            const res = await this.bpmnModeler.saveXML();
            await HttpClient.post('admin/flowable/model/deploy', {id, content: res.xml});
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
                         <Button type='primary' icon={<SaveOutlined/>} onClick={this.handleSave}>保存</Button>
                         <Button type='primary' danger icon={<CloudUploadOutlined/>}
                                 onClick={this.handleDeploy}>部署</Button>
                         <Button icon={<DownloadOutlined/>} onClick={this.handleExportXML}>导出XML</Button>
                         <Button icon={<UploadOutlined/>} onClick={this.handleImportXML}>导入XML</Button>
                         <Button
                             onClick={() => PageUtils.open('/flowable/simulate' + '?id=' + this.state.id, "流程仿真")}> 仿真 </Button>

                         <Button title='查看已部署的历史版本' onClick={() => {
                             this.setState({deployedModal: true})
                         }}>历史版本</Button>
                     </Space>}>


            <Splitter style={{minHeight: 'calc(100vh - 200px)'}}>
                <Splitter.Panel>
                    <div ref={this.bpmRef} style={{width: '100%', height: '100%'}}></div>
                </Splitter.Panel>

                <Splitter.Panel defaultSize={300}>
                    <PropertiesPanel modeler={this.state.bpmnModeler} />
                </Splitter.Panel>
            </Splitter>

            <input ref={this.fileRef} type="file" accept=".xml" style={{display: 'none'}} onChange={this.handleFileChange}/>

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
                            render:(_, record)=> {
                                return <Button type='primary' onClick={()=>{
                                    HttpClient.get('admin/flowable/model/getDefinitionContent',{id: record.id}).then(rs=>{
                                        this.bpmnModeler.importXML(rs.data)
                                        this.setState({deployedModal:false})
                                    })
                                }}>加载</Button>
                            }
                        }
                    ]}
                    request={params => {
                        params.key = this.state.model.key
                        return HttpClient.get('admin/flowable/model/definitionPage', params)
                    }}>

                </ProTable>

            </Modal>

        </Card>
    }


}
