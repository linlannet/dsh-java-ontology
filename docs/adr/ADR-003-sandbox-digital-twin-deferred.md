# ADR-003 沙盘/数字孪生功能降级标注

- **状态：** Deferred（2026-10-18）
- **决策者：** dsh-java-ontology 设计组
- **关联文档：** platform-function-design.md

## 背景

设计评审建议沙盘/孪生功能不再借用 `analysis/tasks`，需要独立设计 `sim_world` / `twin_dashboard` 最小模型。此前为快速落地，沙盘/孪生复用分析任务通道，导致语义混淆、表结构耦合、API 边界不清。评审结论：必须从 `analysis/tasks` 解耦，单独建模。

## 决策

**标注"未设计"，v0.2.1 不实施。**

在 v0.2.1 版本中，沙盘/数字孪生功能不进入实现范围，相关章节统一标记为 @Deferred，不创建对应数据表与 API 端点。

## 理由

1. **当前无沙盘/孪生业务需求**：无业务方提出明确诉求，无落地场景与验收方。
2. **借用 analysis/tasks 的方案已移除**：为避免耦合扩散，原复用方案在 v0.2.1 设计中删除，不再保留过渡态。
3. **后续有需求时再补独立设计**：待业务触发后，按 `sim_world` / `twin_dashboard` 最小模型独立设计，避免空壳占位。

## 后续触发条件

- 业务方提出沙盘/孪生需求（含明确场景、数据源、验收标准）。
- 触发后流程：立项 → 独立建模 → ADR-004（或后续编号）补设计 → 进入实现排期。

## 影响

- `platform-function-design.md` 中沙盘/孪生章节标注 @Deferred（保留章节占位，内容标注"未设计"）。
- 不创建 `sim_world` / `twin_dashboard` 表，DDL 中不出现相关定义。
- API contract 中无相关端点，OpenAPI/Swagger 文档不声明沙盘/孪生路径。
- 代码层不新增 `SimWorld` / `TwinDashboard` 类与 Service，避免空实现误导调用方。
