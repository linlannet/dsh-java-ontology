# dsh-java-ontology v2.0 工作主线规格文档

> 基于 **dsh-java Harness Agent/Skill/Tool 架构**，非传统 CRUD 应用。

## 文件索引

| 文件 | 说明 |
|------|------|
| `../../../dsh-ontolab-siis/docs/design-v2.0.md` | v2.0 架构设计（Agent/Skill/Tool/YAML 驱动），位于 SIIS 独立工程 `dsh-ontolab-siis/` |
| `hotspot-analysis-spec.md` | EARS 需求规格（101 条需求 / 14 组）|
| `hotspot-analysis-design.md` | 七阶段流水线详细设计 |
| `tasks.md` | 编码任务清单（96 任务 / 12 Phase）|

## v2.0 架构

```
用户 → dsh-java Harness(AI对话) → Agent(LLM编排)
    → Skill(热点分析/主题创建/报告生成/知识萃取)
    → Tool(12个原子工具，由 SIIS 插件 dsh-ontolab-siis 注册)
    → Tool(3个平台工具，由 dsh-onto 插件注册)
    → SQLite(七模型YAML自动建库)
```

> 工具归属按仓库边界拆分：`dsh-ontolab-siis` 插件注册 12 个业务工具，`dsh-onto` 插件注册 3 个平台工具，详见各工程 README。

## 核心能力

- **Agent 编排**：用户与 AI 对话，Agent 用 LLM 理解意图→选工具→编排工作流
- **Skill 复用**：工作流打包为 SKILL.md（参考 sharptoolbox 格式）
- **Tool 原子化**：12 个业务工具（SIIS 插件）+ 3 个平台工具（dsh-onto 插件）注册到 Harness ToolRegistry
- **七模型 YAML 驱动**：M1→DDL, M2→Service, M7→API, 自动生成
- **多租户工作台**：每用户=租户，本体共享/工作台隔离
- **多语种**：zh/en/fr/ar 四语种采集+NLP+报告
- **知识沉淀**：分析产出反哺个人知识库，可升级到共享本体
- **大模型赋能**：通过 Harness LLM Provider 调用（不直接集成 SDK）

## 与 v1.0 的关系

- **v1.0**（`docs/1.0/`）：传统若依框架，gwy-web/gwy-crawl/gwy-nlp，人工表单操作
- **v2.0**（`docs/2.0/`）：dsh-java Harness 插件，Agent/Skill/Tool，AI 对话操作
