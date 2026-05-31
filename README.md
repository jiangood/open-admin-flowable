# open-admin-flowable

open-admin 的 Flowable BPMN 2.0 工作流引擎插件，提供流程设计、部署、审批、监控等完整功能。

## Maven 引入

```xml
<dependency>
    <groupId>io.github.jiangood</groupId>
    <artifactId>open-admin-flowable</artifactId>
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
process:
  list:
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

## 项目结构

```
src/main/java/.../modules/flowable/
├── FlowableTemplate.java        # 对外公开 API（推荐使用）
├── FlowableConstants.java       # 常量定义
├── config/                      # 引擎配置、流程初始化、事件监听
├── core/                        # 属性配置、事件类型
├── dao/                         # 流程元数据 DAO
├── dto/                         # 请求/响应 DTO & VO
├── service/                     # 内部业务服务层
├── controller/                  # REST 控制器
└── utils/                       # 工具类
```

## License

MIT
