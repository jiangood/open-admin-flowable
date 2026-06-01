# 前端代码重构设计文档

日期: 2026-06-01
状态: 已批准

## 概述

对 `open-admin-flowable` 前端代码进行精准的代码质量重构，聚焦 3 个具体痛点 + 1 个清理项，不改变外部行为。

## 1. 合并 Instance View 组件

**问题**：`src/pages/flowable/user-task/instance/view.jsx` 和 `src/pages/flowable/monitor/instance/view.jsx` 高度重复（相同 API、相同评论表、相同流程图展示）。

**方案**：
- 新建 `src/components/InstanceView.jsx` 作为公共组件
- 接受 `showVariables?: boolean` prop 控制是否显示流程变量卡片（monitor 版需要）
- 两个原文件改为导入该组件并精简

**文件变化**：
- `+` `src/components/InstanceView.jsx`
- `~` `src/pages/flowable/user-task/instance/view.jsx`
- `~` `src/pages/flowable/monitor/instance/view.jsx`

## 2. 拆分 simulate/index.jsx

**问题**：`src/pages/flowable/simulate/index.jsx` 单文件 360 行，一个组件用状态路由处理 init / running / finished 三阶段，逻辑混杂难以维护。

**方案**：按阶段拆分为三个独立子组件，父组件仅做状态路由和数据传递。

| 文件 | 职责 |
|---|---|
| `simulate/InitPhase.jsx` | 选择流程定义、填写表单、发起流程 |
| `simulate/RunningPhase.jsx` | 流程图展示、评论列表、推进/驳回操作 |
| `simulate/FinishedPhase.jsx` | 已完成流程历史列表 |
| `simulate/index.jsx` | 仅保留状态路由，精简至 ~50 行 |

**文件变化**：
- `+` `src/pages/flowable/simulate/InitPhase.jsx`
- `+` `src/pages/flowable/simulate/RunningPhase.jsx`
- `+` `src/pages/flowable/simulate/FinishedPhase.jsx`
- `~` `src/pages/flowable/simulate/index.jsx`

## 3. 提取 ProcessImageViewer 组件

**问题**：`src/pages/flowable/user-task/form.jsx` 和 `src/pages/flowable/simulate/RunningPhase`（原 simulate/index.jsx）中重复实现"点击查看流程图大图"逻辑。

**方案**：
- 新建 `src/components/ProcessImageViewer.jsx`
- 接收 `imageUrl: string` prop，点击触发 Modal.info 全屏展示
- 两处引用替换

**文件变化**：
- `+` `src/components/ProcessImageViewer.jsx`
- `~` `src/pages/flowable/user-task/form.jsx`
- `~` `src/pages/flowable/simulate/RunningPhase.jsx`（拆出来的新文件，引用新组件）

## 4. Cleanup

- 删除 `src/pages/flowable/design/provider/properties/ConditionDesign.jsx` 第 135 行的 `console.log('流程id', processId)`

## 不变事项

- 不引入状态管理、TypeScript、lint
- 不改变 class component 模式
- 不改变 API 调用方式
- 不改变 UI 表现
- 不修改现有功能逻辑
