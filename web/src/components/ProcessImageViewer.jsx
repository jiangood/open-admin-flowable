import { Modal } from "antd";

export default function ProcessImageViewer({ imageUrl }) {
  const show = () => {
    Modal.info({
      title: '流程图',
      width: '70vw',
      content: (
        <div style={{ width: '100%', overflow: 'auto', maxHeight: '80vh' }}>
          <img src={imageUrl} style={{ maxWidth: '100%' }} />
        </div>
      ),
    });
  };

  return <a onClick={show}>查看流程图</a>;
}
