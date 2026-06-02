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

  return (
    <div onClick={show} style={{ cursor: 'pointer', border: '1px solid #d9d9d9', borderRadius: 8, padding: 8, background: '#fafafa' }}>
      <img src={imageUrl} style={{ maxWidth: '100%', maxHeight: 200, display: 'block', margin: '0 auto' }} />
    </div>
  );
}
