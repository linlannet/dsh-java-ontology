# dsh-java-ontology

简体中文 | [English](./README.en.md)

> **TL;DR** · 本体知识工程平台（Java）：base + flow + 领域能力包三层架构，聚合应用监听 **8100**，输出 3 个 Harness 工具；配套 SIIS 工程提供四域业务与 12 个业务工具。
> **定位** · WorkBuddy 对标（`dsh-java` / DSH-Desktop）生态下的下游资源服务；统一身份/租户由 **linlan-emis（BuddyOne）** 提供，本平台作为 OAuth2 Resource Server（0.3.0 规划）。
> **Keywords**: Ontology, Spring Boot, Java 17, MyBatis, Flyway, JWT, OIDC, Harness Plugin, Knowledge Graph
> **AI 入口**: [llms.txt](./llms.txt) · [AGENTS.md](./AGENTS.md)

[![License](https://img.shields.io/badge/license-MIT-green.svg)](./LICENSE)
![java](https://img.shields.io/badge/Java-17-orange.svg)
![spring boot](https://img.shields.io/badge/Spring%20Boot-3.5.16-brightgreen.svg)

## 简介

一个面向本体知识工程的平台，采用 **base + flow + 领域能力包** 三层架构：底层静态本体定义、规则与通用基础设施（`dsh-onto-base`），中层动态本体、认证授权与工作台编排（`dsh-onto-flow`），上层按需组合的领域能力包（问数 `qa`、可视化 `viz`、指标 `metric`）。`dsh-onto-app` 将其聚合为可独立部署的平台应用（端口 8100），`dsh-onto-plugin` 输出 `dsh-java` Java Native Plugin（3 个原子工具），让 AI Agent 安全地查询、检索本体知识库。仓库同时内置 3 份 Agent 配置与 4 份 Skill 资源。

当前版本 **0.2.2**，与 `dsh-java` 0.2.2 契约对齐。

**身份分层（0.3.0 规划）**：`linlan-emis/backend`（Java）与 `linlan-emis/py-api`（Python）是 BuddyOne 的两条路线，统一签发 OIDC token；本平台不再自建 IdP，改为通过 JWKS 验签并消费 `tenant_id` / `role_level`。设计见 [docs/0.3.0/auth-tenant-design.md](docs/0.2.3/auth-tenant-design.md)；当前 0.2.2 仍为内置 JWT + BCrypt 过渡态。

## 功能特性

- **本体库管理**：多本体库配置，支持新建、删除、列表查看
- **AI 对话助手**：自然语言查询本体库，流式输出思考过程 → 工具调用 → 最终结论
- **概念查询**：按本体 ID 和查询表达式检索概念定义、层级关系、实体属性
- **模糊搜索**：按关键词快速定位概念和实体
- **多租户权限（过渡态）**：JWT + BCrypt 认证，主任/研究员/访客三级角色；0.3.0 起身份改由 linlan-emis 统一签发，本平台只验签
- **工作流编排**：状态机流转 + 任务依赖 + 并行执行
- **全局知识聚合**：三级知识架构（个人→公共→全局）+ 知识图谱汇聚
- **问数报告**：模板 / 章节 / 图表 / 会话 / 任务的报告与问数编排
- **指标建模**：业务对象 / 流程 / 决策点 + 原子 / 派生 / 复合指标 + 依赖树
- **Harness 插件**：3 个原子工具注册到 `dsh-java`，供 Agent 调用

## 版本历史

全部版本变更见 [CHANGELOG](./CHANGELOG.md)。本 README 只保留稳定的架构、模块与使用说明。

## 目录

- [1. 架构](#1-架构)
- [2. 表命名规范](#2-表命名规范)
- [3. 环境要求](#3-环境要求)
- [4. 快速开始](#4-快速开始)
- [5. 配置项](#5-配置项)
- [6. 模块依赖关系](#6-模块依赖关系)
- [7. 配套工程](#7-配套工程)
- [8. 目录结构](#8-目录结构)
- [9. 安全边界](#9-安全边界)
- [10. 已知事项](#10-已知事项)
- [11. 测试](#11-测试)
- [12. 文档索引](#12-文档索引)
- [参与贡献](#参与贡献)
- [许可证](#许可证)

---

## 1. 架构

```text
dsh-java-ontology/
├── dsh-onto-base/                          基础包：静态本体定义 + 规则 + 逻辑
│   ├── src/main/java/net/linlan/dsh/common/        通用基础设施（PageRequest/Result/BaseEntity/BaseRepository 等）
│   ├── src/main/java/net/linlan/dsh/ontology/plugin/  插件基类（AdminApiClient + BaseOntologyTool）
│   └── src/main/resources/ontology-models/          七模型 YAML（m1~m7 + manifest）
├── dsh-onto-flow/                          编排包：动态本体 + 工作台 + 多用户权限
│   ├── src/main/java/net/linlan/dsh/sys/           认证授权（JWT + BCrypt + 角色拦截；0.3.0 起改为 JWKS 验签）
│   ├── src/main/java/net/linlan/dsh/common/entity/  Common 实体（DiscoveredEntity/LlmChatHistory 等）
│   ├── src/main/java/net/linlan/dsh/plat/          平台编排（OntologyController/HarnessAi/Workline）
│   ├── src/main/java/net/linlan/dsh/global/        全局知识聚合（Aggregation/GlobalKnowledge/KnowledgeAggregator）
│   └── src/main/java/net/linlan/dsh/workspace/     统一工作台（WorkspaceController）
├── dsh-onto-qa/                            问数包：数据分析 + 报告 + 问数（模板/章节/图表/会话/任务）
├── dsh-onto-viz/                           可视化包：前端静态资源（HTML/CSS/JS）
├── dsh-onto-metric/                        指标包：业务对象/流程/决策点 + 原子/派生/复合指标 + 依赖树
├── dsh-onto-app/                           聚合应用：base + flow + qa + viz + metric（端口 8100）
├── dsh-onto-plugin/                        插件输出：3 个平台工具（workspace/knowledge/director）
├── agents/                                 Agent 配置 3 份（director-assistant/research-assistant/topic-creator）
├── skills/                                 Skill 4 份（hotspot-analysis/knowledge-extraction/report-generation/topic-creation）
└── docs/                                   设计文档（0.2.1 规格 / 0.2.2 设计 / 契约 / 运维）
```

```text
ontology_db 仅是旧默认占位名，实际统一为 `dsh_java_ontodb`。浏览器 ──► Admin UI (8100) ──► Admin API ──► Onto DB (默认 `dsh_java_ontodb`)
               │
               └─ AI 对话 ──► dsh-java (8090)
                                 └─► dsh-onto-plugin (3 工具) ──► 平台 API
```

## 2. 表命名规范

| 前缀 | 业务域 | 模块 | 表数量 |
| --- | --- | --- | --- |
| `onto_sys_` | 认证权限 | flow | 4 |
| `onto_common_` | 个人知识沉淀 | flow | 6 |
| `onto_notify_` | 消息与订阅 | flow | 2 |
| `onto_ontology_` | 本体库 | base | 9 |
| `onto_report_` | 问数与报告 | qa | 7 |
| `onto_global_` | 全局知识聚合 | flow | 5 |
| `onto_metric_` | 指标 | metric | 5 |

共 38 张表，由 Flyway 脚本 `V1.0.0~V1.0.1`（`dsh-onto-app`）与 `V1.0.2`（`dsh-onto-metric`）创建。

> 拆分遗留：`V1.0.0__init_schema.sql` 中仍包含合并期的 36 张 `gwy_*` 业务表，SIIS 迁出为独立工程后将从本仓库移除。

## 3. 环境要求

- JDK 17+
- Maven 3.9+
- MySQL 8.0+
- 可选：`dsh-java`（AI 对话能力依赖它提供 Agent 运行时）

## 4. 快速开始

### 4.1 构建

```bash
git clone git@github.com:linlannet/dsh-java-ontology.git
cd dsh-java-ontology

# 只构建平台七包（推荐；根 pom 仍声明 SIIS 嵌套模块）
mvn -pl dsh-onto-app -am package -DskipTests
```

> 根 pom 的 `<modules>` 仍包含 `dsh-ontolab-siis/*`，该目录被 `.gitignore` 忽略。未放置 SIIS 源码时，全量 `mvn package` 会因缺少子模块失败；请使用 `-pl dsh-onto-app -am`，或先按[配套工程](#7-配套工程)放置 SIIS 源码。

### 4.2 启动聚合应用（平台）

```bash
java -jar dsh-onto-app/target/dsh-onto-app-0.2.2-exec.jar
```

或开发模式：

```bash
mvn spring-boot:run -pl dsh-onto-app
```

打开 <http://127.0.0.1:8100>。

### 4.3 使用 AI 助手（可选）

AI 对话依赖 `dsh-java`（默认 `http://127.0.0.1:8090`）提供 Agent 运行时，并需要安装本仓库的插件（见 4.4）。

### 4.4 安装 Harness 插件（3 工具）

先启动 `dsh-java`（端口 8090）：

```bash
JAR="$PWD/dsh-onto-plugin/target/dsh-onto-plugin-0.2.2.jar"

curl -X POST http://localhost:8090/api/harness/plugins/install \
  -H 'Content-Type: application/json' \
  -d "{\"pluginId\":\"dsh-onto-plugin\",\"displayName\":\"DSH Onto Plugin\",\"pluginVersion\":\"0.2.2\",\"runtimeType\":\"JAVA_NATIVE\",\"sourcePath\":\"$JAR\",\"entrypoint\":\"dsh-onto-plugin-0.2.2.jar\"}"

curl -X POST http://localhost:8090/api/harness/plugins/activate \
  -H 'Content-Type: application/json' \
  -d '{"pluginId":"dsh-onto-plugin"}'
```

| 工具 | 说明 |
| --- | --- |
| `plugin__dsh-onto-plugin__workspace_overview` | 工作台概览 |
| `plugin__dsh-onto-plugin__knowledge_aggregate` | 知识聚合 |
| `plugin__dsh-onto-plugin__director_overview` | 主任决策概览 |

> 四域业务所需的另外 12 个业务工具由配套工程 `dsh-ontolab-siis` 提供，安装方式见 [dsh-ontolab-siis](https://github.com/linlannet/dsh-ontolab-siis)。

## 5. 配置项

| 配置 | 环境变量 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `server.port` | — | `8100` | 聚合应用端口 |
| `spring.datasource.url` | `DB_HOST` / `DB_PORT` / `DB_NAME` | `127.0.0.1:3306/dsh_java_ontodb` | 数据库连接 |
| `spring.datasource.username` | `DB_USER` | `root` | 数据库用户 |
| `spring.datasource.password` | `DB_PASSWORD` | `root` | 数据库密码 |
| `dsh.ontology.data-directory` | — | `~/.dsh-java-ontology` | 本体库配置存储目录 |
| `dsh.ontology.query-timeout-seconds` | — | `15` | 查询执行超时 |
| `dsh.ontology.max-results` | — | `100` | 查询最大返回结果数 |
| `dsh.ontology.harness.base-url` | `DSH_HARNESS_BASE_URL` | `http://127.0.0.1:8090` | Harness 地址 |
| `dsh.ontology.jwt.secret` | `JWT_SECRET` | （内置默认） | JWT 签名密钥；生产必须覆盖 |
| `dsh.ontology.auth.default-director-enabled` | `DSH_AUTH_DEFAULT_DIRECTOR` | `true` | 未携带有效 JWT 时按主任（roleLevel=0、跨租户）放行；桌面端暂无登录界面时使用，生产应设为 `false` |

> 0.3.0 规划：验签改为 **linlan-emis** 的 `issuer` + JWKS（`dsh.auth.issuer` / `dsh.auth.jwks-uri` / `dsh.auth.audience`），`dsh.ontology.jwt.secret` 仅过渡期保留；详见 [docs/0.3.0/auth-tenant-design.md](docs/0.2.3/auth-tenant-design.md)。

## 6. 模块依赖关系

```text
dsh-onto-base  ←── dsh-onto-flow  ←── dsh-onto-app
       ↑
       └── dsh-onto-plugin
dsh-onto-qa / dsh-onto-viz / dsh-onto-metric ←── dsh-onto-app
```

- **base**：不依赖任何业务模块，仅依赖 Spring Boot 基础设施
- **flow**：依赖 base，包含认证授权 + 工作台 + 全局聚合
- **qa**：依赖 base + flow，报告与问数编排
- **viz**：前端静态资源包
- **metric**：依赖 base，指标建模与依赖树
- **app**：聚合 base + flow + qa + viz + metric，可独立部署的平台应用
- **plugin**：依赖 base，输出 3 个平台工具的插件 JAR

## 7. 配套工程

| 工程 | 位置 | 说明 |
| --- | --- | --- |
| `dsh-ontolab-siis` | `dsh-ontolab-siis/`（独立目录 + 独立父 pom，目录被本仓库 `.gitignore` 忽略） | 四域业务（ontology/trace/display/analyze）+ 38 张 `gwy_*` 表 + 12 个业务工具，端口 8101，文档见 [dsh-ontolab-siis](https://github.com/linlannet/dsh-ontolab-siis) |
| `linlan-emis`（BuddyOne） | `/Users/mac/Workspace/linlannet/linlan-emis`（兄弟仓库） | 统一身份/租户中台：Java `backend` + Python `py-api`；本平台作为其 Resource Server（0.3.0 规划） |

配套关系：`dsh-ontolab-siis` 依赖本仓库的 `dsh-onto-base` / `flow` / `qa` / `viz` / `metric` 0.2.2；构建时需与主仓库位于同一父目录，详见其 README。

## 8. 目录结构

```text
dsh-java-ontology/
├── dsh-onto-base/ / dsh-onto-flow/ / dsh-onto-qa/
├── dsh-onto-viz/ / dsh-onto-metric/ / dsh-onto-app/ / dsh-onto-plugin/
├── agents/                  Agent 配置 3 份
├── skills/                  Skill 4 份
├── docs/
│   ├── 0.2.1/               平台设计、规格、DB 设计、架构对比、插件拆分、热点分析
│   ├── 0.2.2/               设计评审、静态/动态本体方案
│   ├── 0.3.0/               身份/租户接入设计（linlan-emis = BuddyOne，本平台 = RS）
│   ├── contract/            权限契约（permissions.yaml）
│   └── dev-ops/             本体库 schema（dsh_java_ontodb.sql）
├── AGENTS.md                AI Agent 工程约定
├── llms.txt                 AI Agent 快速索引
└── CHANGELOG.md             版本变更记录
```

## 9. 安全边界

- **AI 链路工具化**：本仓库插件注册 3 个原子工具，Agent 通过工具调用访问平台能力。
- 查询默认 15 秒超时、最多返回 100 条结果。
- JWT 认证 + 三级角色权限（主任/研究员/访客）+ 多租户数据隔离（过渡态）；0.3.0 起改为验签 linlan-emis 签发的 token，本平台不再自签。
- 生产部署前必须修改默认 JWT 密钥、数据库密码，并配置 CORS/CSRF 策略。

## 10. 已知事项

- 根 pom 的 `<modules>` 只包含平台七包；`dsh-ontolab-siis` 使用独立父 pom，全量构建平台无需放置 SIIS 源码。
- `dsh.app.version` 与 `plugin.yaml` 的 `version` 均已统一为 `0.2.2`，与 pom 一致。
- `V1.0.0__init_schema.sql` 仍保留 36 张 `gwy_*` 拆分遗留表。
- 平台插件 3 工具；12 个业务工具在 `dsh-ontolab-siis`。

## 11. 测试

```bash
mvn -pl dsh-onto-base,dsh-onto-app -am test
```

16 个测试类（`dsh-onto-base` 6 个、`dsh-onto-app` 10 个）共 110 个测试方法，覆盖单元测试、模型引擎、集成测试和端到端测试。

## 12. 文档索引

| 文档 | 内容 |
| --- | --- |
| [CHANGELOG.md](./CHANGELOG.md) | 版本记录、索引与 AI 检索提示 |
| [AGENTS.md](./AGENTS.md) | AI Agent 使用的结构、命令与约束 |
| [llms.txt](./llms.txt) | 机器可读摘要与命令索引 |
| [docs/0.2.1/](./docs/0.2.1) | 平台设计与规格、`ONTO_DB_DESIGN`、架构对比、插件拆分、热点分析 |
| [docs/0.2.2/](./docs/0.2.2) | 设计评审、静态/动态本体实施方案 |
| [docs/0.3.0/auth-tenant-design.md](docs/0.2.3/auth-tenant-design.md) | 身份/租户接入设计：linlan-emis（BuddyOne）为 IdP，本平台为 Resource Server |
| [docs/contract/permissions.yaml](./docs/contract/permissions.yaml) | 权限契约 |
| [docs/dev-ops/dsh_java_ontodb.sql](./docs/dev-ops/dsh_java_ontodb.sql) | 本体库 schema |
| [dsh-ontolab-siis](https://github.com/linlannet/dsh-ontolab-siis) | 配套 SIIS 工程说明 |

## 参与贡献

1. Fork 本仓库
2. 创建特性分支：`git checkout -b feat/xxxx`
3. 提交前运行 `mvn -pl dsh-onto-base,dsh-onto-app -am test`
4. 发起 Pull Request

## 许可证

[MIT © 2026 shuxiaobo](./LICENSE)