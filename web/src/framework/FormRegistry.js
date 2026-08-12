class FormRegistry {
    static forms = new Map();

    static register(formKey, formComponent) {
        if (!formKey || !formComponent) {
            throw new Error("表单 Key 和组件不能为空！");
        }
        if (FormRegistry.forms.has(formKey)) {
            console.warn(`表单 "${formKey}" 已存在，将被覆盖！`);
        }
        FormRegistry.forms.set(formKey, formComponent);
    }

    static get(formKey) {
        const formComponent = FormRegistry.forms.get(formKey);
        if (!formComponent) {
            console.warn(`表单 "${formKey}" 未注册！`);
        }
        return formComponent ?? null;
    }

    static has(formKey) {
        return FormRegistry.forms.has(formKey);
    }

    static getAllKeys() {
        return Array.from(FormRegistry.forms.keys());
    }
}

export {FormRegistry};
export default FormRegistry;
