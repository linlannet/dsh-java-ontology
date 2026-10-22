# dsh-java-ontology

一个面向本体知识工程的平台，采用 **base + flow + 领域能力包** 三层架构：底层静态本体定义、规则与通用基础设施（`dsh-onto-base`），中层动态本体、认证授权与工作台编排（`dsh-onto-flow`），上层按需组合的领域能力包（问数 `qa`、可视化 `viz`、指标 `metric`）。`dsh-onto-app` 将其聚合为可独立部署的平台应用（端口 8100），`dsh-onto-plugin` 输出 `dsh-java` Java Native Plugin（3 个原子工具），让 AI Agent 安全地查询、检索本体知识库。仓库同时内置 3 份 Agent 配置与 4 份 Skill 资源。

## 功能特性

- **本体库管理**：多本体库配置，支持新建、删除、列表查看
- **AI 对话助手**：自然语言查询本体库，流式输出思考过程 → 工具调用 → 最终结论
- **概念查询**：按本体 ID 和查询表达式检索概念定义、层级关系、实体属性
- **模糊搜索**：按关键词快速定位概念和实体
- **多租户权限**：JWT + BCrypt 认证，主任/研究员/访客三级角色
- **工作流编排**：状态机流转 + 任务依赖 + 并行执行
- **全局知识聚合**：三级知识架构（个人→公共→全局）+ 知识图谱汇聚
- **问数报告**：模板 / 章节 / 图表 / 会话 / 任务的报告与问数编排
- **指标建模**：业务对象 / 流程 / 决策点 + 原子 / 派生 / 复合指标 + 依赖树
- **Harness 插件**：3 个原子工具注册到 dsh-java，供 Agent 调用

## 架构

```text
dsh-java-ontology/
├── dsh-onto-base/                          基础包：静态本体定义 + 规则 + 逻辑
│   ├── src/main/java/net/linlan/dsh/common/        通用基础设施（PageRequest/Result/BaseEntity/BaseRepository 等）
│   ├── src/main/java/net/linlan/dsh/ontology/plugin/  插件基类（AdminApiClient + BaseOntologyTool）
│   └── src/main/resources/ontology-models/          七模型 YAML（m1~m7 + manifest）
├── dsh-onto-flow/                          编排包：动态本体 + 工作台 + 多用户权限
│   ├── src/main/java/net/linlan/dsh/sys/           认证授权（JWT + BCrypt + 角色拦截）
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
└── docs/                                   设计文档（0.2.1 规格 / 0.2.2 设计 / ADR / 契约 / 运维）
```

```text
浏览器 ──► Admin UI (8100) ──► Admin API ──► Onto DB (默认 ontology_db)
               │
               └─ AI 对话 ──► dsh-java (8090)
                                 └─► dsh-onto-plugin (3 工具) ──► 平台 API
```

### 表命名规范

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

## 环境要求

- JDK 17+
- Maven 3.9+
- MySQL 8.0+
- 可选：dsh-java（AI 对话能力依赖它提供 Agent 运行时）

## 快速开始

### 1. 构建

```bash
git clone git@github.com:linlannet/dsh-java-ontology.git
cd dsh-java-ontology
mvn package -DskipTests
```

### 2. 启动聚合应用（平台）

```bash
java -jar dsh-onto-app/target/dsh-onto-app-0.2.1-exec.jar
```

或开发模式：

```bash
mvn spring-boot:run -pl dsh-onto-app
```

打开 <http://127.0.0.1:8100>。

### 3. 使用 AI 助手（可选）

AI 对话依赖 `dsh-java`（默认 `http://127.0.0.1:8090`）提供 Agent 运行时，并需要安装本仓库的插件（见下文）。

### 4. 安装 Harness 插件（3 工具）

先启动 `dsh-java`（端口 8090）：

```bash
JAR="$PWD/dsh-onto-plugin/target/dsh-onto-plugin-0.2.1.jar"

curl -X POST http://localhost:8090/api/harness/plugins/install \
  -H 'Content-Type: application/json' \
  -d "{\"pluginId\":\"dsh-onto\",\"displayName\":\"DSH Onto Plugin\",\"pluginVersion\":\"0.2.1\",\"runtimeType\":\"JAVA_NATIVE\",\"sourcePath\":\"$JAR\",\"entrypoint\":\"dsh-onto-plugin-0.2.1.jar\"}"

curl -X POST http://localhost:8090/api/harness/plugins/activate \
  -H 'Content-Type: application/json' \
  -d '{"pluginId":"dsh-onto"}'
```

| 工具 | 说明 |
| --- | --- |
| `plugin__dsh-onto__workspace_overview` | 工作台概览 |
| `plugin__dsh-onto__knowledge_aggregate` | 知识聚合 |
| `plugin__dsh-onto__director_overview` | 主任决策概览 |

> 四域业务所需的另外 12 个业务工具由配套工程 `dsh-ontolab-siis` 提供，安装方式见 [dsh-ontolab-siis/README.md](dsh-ontolab-siis/README.md)。

## 配置项

| 配置 | 环境变量 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `server.port` | — | `8100` | 聚合应用端口 |
| `spring.datasource.url` | `DB_HOST` / `DB_PORT` / `DB_NAME` | `127.0.0.1:3306/ontology_db` | 数据库连接 |
| `spring.datasource.username` | `DB_USER` | `root` | 数据库用户 |
| `spring.datasource.password` | `DB_PASSWORD` | `root` | 数据库密码 |
| `dsh.ontology.data-directory` | — | `~/.dsh-java-ontology` | 本体库配置存储目录 |
| `dsh.ontology.query-timeout-seconds` | — | `15` | 查询执行超时 |
| `dsh.ontology.max-results` | — | `100` | 查询最大返回结果数 |
| `dsh.ontology.harness.base-url` | `DSH_HARNESS_BASE_URL` | `http://127.0.0.1:8090` | Harness 地址 |
| `dsh.ontology.jwt.secret` | `JWT_SECRET` | （内置默认） | JWT 签名密钥 |

## 模块依赖关系

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

## 配套工程

| 工程 | 位置 | 说明 |
| --- | --- | --- |
| `dsh-ontolab-siis` | `dsh-ontolab-siis/`（独立目录 + 独立父 pom，不在本仓库版本控制内） | 四域业务（ontology/trace/display/analyze）+ 38 张 `gwy_*` 表 + 12 个业务工具，端口 8101，文档见 [dsh-ontolab-siis/README.md](dsh-ontolab-siis/README.md) |

## 安全边界

- **AI 链路工具化**：本仓库插件注册 3 个原子工具，Agent 通过工具调用访问平台能力。
- 查询默认 15 秒超时、最多返回 100 条结果。
- JWT 认证 + 三级角色权限（主任/研究员/访客）+ 多租户数据隔离。
- 生产部署前必须修改默认 JWT 密钥、数据库密码，并配置 CORS/CSRF 策略。

## 文档

- `docs/0.2.1`：平台设计与规格、ONTO_DB_DESIGN、架构对比、插件拆分方案、热点分析 spec 与任务清单
- `docs/0.2.2`：设计评审、静态/动态本体实施方案、方法论与实践文章
- `docs/adr`：ADR-001~005 架构决策记录
- `docs/contract/permissions.yaml`：权限契约
- `docs/dev-ops/dsh_java_ontodb.sql`：本体库 schema

## 测试

```bash
mvn test
```

13 个测试类（`dsh-onto-base` 3 个、`dsh-onto-app` 10 个）共 74 个测试方法，覆盖单元测试、集成测试和端到端测试。
