# open-admin-flowable

![Maven Central](https://img.shields.io/maven-central/v/io.github.jiangood/open-admin-flowable-starter)
![npm](https://img.shields.io/npm/v/@jiangood/open-admin-flowable)

open-admin 的 Flowable BPMN 2.0 工作流引擎插件，提供流程设计、部署、审批、监控等完整功能。

## Maven 引入

```xml
<dependency>
    <groupId>io.github.jiangood</groupId>
    <artifactId>open-admin-flowable-starter</artifactId>
    <version>2.0.0</version>
</dependency>
```

## 使用方式

### 发起流程

业务模块通过注入 `FlowableTemplate` 发起流程：

```java
@Autowired
private FlowableTemplate flowableTemplate;

Map<String, Object> vars = new HashMap<>();
vars.put("days", 3);
vars.put("reason", "事假");
flowableTemplate.startProcess("leave_request", bizId, vars);

// 或指定自定义标题
flowableTemplate.startProcess("leave_request", bizId, "请假-张三", vars);
```

**参数说明：**

| 参数 | 类型 | 说明 |
|------|------|------|
| `key` | String | 流程定义编码，与 YAML 配置文件中的 key 对应 |
| `bizKey` | String | 业务标识，需在业务域内唯一，重复提交会被拦截 |
| `variables` | Map | 流程变量，必填项按 YAML 定义自动校验 |
| `title` | String | 流程标题，可选，为 null 时自动生成 |

**自动注入的变量：**

发起流程时 `FlowableTemplate` 会自动注入当前用户上下文，无需手动设置：

`userId`, `userName`, `unitId`, `unitName`, `deptId`, `deptName`, `INITIATOR_DEPT_LEADER`, `BUSINESS_KEY`, `GLOBAL_FORM_KEY`

### 流程定义

在 `src/main/resources/data/` 下创建 `flowable-process-definition-*.yml` 文件定义流程：

```yaml
definitions:
  - key: "leave_request"
    name: "请假流程"
    listener: com.example.LeaveProcessListener
    variables:
      - name: "days"
        label: "请假天数"
        value-type: number
        required: true
      - name: "reason"
        label: "请假原因"
        required: true
    forms:
      - key: "start_form"
        label: "请假申请单"
```

### 表单定义

流程中每个节点（开始事件、用户任务）可绑定一个表单，表单由 **YAML 元数据** + **前端组件** 两部分组成。

#### 1. YAML 定义表单元数据

在 `flowable-process-definition-*.yml` 的 `forms` 列表中配置：

```yaml
definitions:
  - key: "leave_request"
    name: "请假流程"
    forms:
      - key: "start_form"            # 表单标识，需全局唯一
        label: "请假申请表"           # 表单名称，显示在设计师下拉框
      - key: "manager_approve_form"
        label: "经理审批表"
      - key: "finish_view"
        label: "流程结果查看"
```

#### 2. 前端表单组件

在前端 `web/src/forms/` 目录下创建对应的 React 组件。

**命名规则：** 前端组件通过 `formKey + 'Form'` 查找，例如 YAML 中 `formKey: start_form`，则对应组件名 `start_formForm`。

**组件约定：** 组件接收 `value`（当前表单数据）和 `onChange`（数据变化回调）两个 props，通过 `onValuesChange` 将表单数据实时同步给父组件。

示例 `web/src/forms/start_formForm.jsx`：

```jsx
import { Form, Input, InputNumber } from "antd";

export default function ({ value, onChange }) {
  const onValuesChange = (_, allValues) => {
    onChange?.(allValues);
  };

  return (
    <Form onValuesChange={onValuesChange} initialValues={value}>
      <Form.Item label="事由" name="reason">
        <Input />
      </Form.Item>
      <Form.Item label="请假天数" name="days">
        <InputNumber />
      </Form.Item>
    </Form>
  );
}
```

#### 3. 自动注册

框架的 UmiJS 插件 `@jiangood/open-admin/config/common-plugin` 在构建时自动扫描 `web/src/forms/*.jsx`，无需手动注册即可生效。**只需将组件文件放入 `web/src/forms/` 目录即可。**

#### 4. 在 BPMN 设计中绑定表单

打开流程设计器，选中 **UserTask** 节点 → 右侧属性面板「表单」下拉框 → 选择对应表单 key（即 YAML 中定义的 `key`）。保存后，表单 key 会写入 BPMN XML 的 `flowable:formKey` 属性。

#### 5. 运行时表单解析

流程流转到该节点时，`user-task/form.jsx` 从后端获取任务信息得到 `formKey`，拼接为 `<formKey>Form`，通过 `FormRegistryUtils.get(name)` 查找前端注册的组件并渲染。

#### 6. 表单数据提交

用户点击「同意」时，前端将表单数据（`formData`）随审批请求一同提交到后端。后端在处理 APPROVE 时，会调用 `ProcessListener.onFormSubmit(initiator, approver, businessKey, formData)`，业务方在 `ProcessListener` 实现类中覆盖该方法即可接收并持久化表单数据：

```java
@Component
public class MyProcessListener implements ProcessListener {
    @Override
    public void onProcessEvent(ProcessEventType type, String initiator, String businessKey, Map<String, Object> variables) {
    }

    @Override
    public void onFormSubmit(String initiator, String approver, String businessKey, Map<String, Object> formData) {
        // 在此处保存业务数据
    }
}
```

## 项目结构

```
open-admin-flowable/
├── pom.xml                              # 父 POM (多模块)
├── open-admin-flowable-starter/         # ← 发布到 Maven Central
│   ├── pom.xml
│   └── src/main/java/.../
│       ├── FlowableTemplate.java        # 对外公开 API（推荐使用）
│       ├── FlowableConstants.java       # 常量定义
│       ├── config/                      # 引擎配置、流程初始化、事件监听
│       ├── core/                        # 属性配置、事件类型
│       ├── dao/                         # 流程元数据 DAO
│       ├── dto/                         # 请求/响应 DTO & VO
│       ├── service/                     # 内部业务服务层
│       ├── controller/                  # REST 控制器
│       └── utils/                       # 工具类
└── open-admin-flowable-example/             # 本地开发应用
    ├── pom.xml
    └── src/main/java/.../
        ├── ProcessBootApplication.java  # 启动入口
        ├── example/                     # 示例代码
        └── controller/TestController.java # 测试接口
```

## License

MIT
