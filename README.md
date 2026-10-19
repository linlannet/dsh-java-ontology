# dsh-java-ontology

一个面向本体知识工程的平台，采用 **base + flow + SIIS** 三层架构：底层静态本体定义与规则逻辑（base）、中层动态本体与工作台编排（flow）、上层 SIIS 传统业务四域（ontology/trace/display/analyze）。同时提供两个 `dsh-java` Java Native Plugin，让 AI Agent 可以安全地查询、检索本体知识库。

## 功能特性

- **本体库管理**：多本体库配置，支持新建、删除、列表查看
- **AI 对话助手**：自然语言查询本体库，流式输出思考过程 → 工具调用 → 最终结论
- **概念查询**：按本体 ID 和查询表达式检索概念定义、层级关系、实体属性
- **模糊搜索**：按关键词快速定位概念和实体
- **多租户权限**：JWT + BCrypt 认证，主任/研究员/访客三级角色
- **工作流编排**：状态机流转 + 任务依赖 + 并行执行
- **爬取引擎**：限流 + 代理轮换 + 重试 + 定时调度
- **多语种 NLP**：中/英/法/阿四语种策略 + 本体匹配 + 图谱归一化
- **全局知识聚合**：三级知识架构（个人→公共→全局）+ 知识图谱汇聚
- **Harness 插件**：15 个原子工具注册到 dsh-java，供 Agent 调用

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
├── dsh-onto-visulization/                  可视化包：前端静态资源（HTML/CSS/JS）
├── dsh-onto-app/                           聚合应用：base + flow + visulization（端口 8100）
├── dsh-onto-plugin/                        插件输出：3 个平台工具（workspace/knowledge/director）
└── dsh-ontoext-siis/                       SIIS 子项目
    ├── dsh-ontoext-siis-app/               SIIS 应用：四域业务 + 完整 schema（端口 8101）
    └── dsh-ontoext-siis-plugin/            SIIS 插件输出：12 个业务工具
```

```text
浏览器 ──► Admin UI (8100) ──► Admin API ──► Ontology Store
               │
               └─ AI 对话 ──► dsh-java (8090)
                                 ├─► dsh-onto-plugin (3 工具) ──► 平台 API
                                 └─► dsh-ontoext-siis-plugin (12 工具) ──► SIIS API
```

### 表命名规范

| 前缀 | 所属层 | 模块 | 表数量 |
| --- | --- | --- | --- |
| `onto_sys_` | 认证权限 | flow | 1 |
| `onto_common_` | 个人知识沉淀 | flow | 4 |
| `onto_global_` | 全局知识聚合 | flow | 5 |
| `gwy_ontology_` | 本体库 | SIIS | 14 |
| `gwy_track_` | 追踪采集 | SIIS | 11 |
| `gwy_analyze_` | 分析预警 | SIIS | 7 |
| `gwy_display_` | 展示呈现 | SIIS | 10 |

共 52 张表 + 2 视图（`v_unified_monitor`、`v_unified_kg`）。

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

### 3. 启动 SIIS 应用（四域业务）

```bash
java -jar dsh-ontoext-siis/dsh-ontoext-siis-app/target/dsh-ontoext-siis-app-0.2.1-exec.jar
```

或开发模式：

```bash
mvn spring-boot:run -pl dsh-ontoext-siis/dsh-ontoext-siis-app
```

打开 <http://127.0.0.1:8101>。

### 4. 使用 AI 助手（可选）

AI 对话依赖 `dsh-java`（默认 `http://127.0.0.1:8090`）提供 Agent 运行时，并需要安装本仓库的插件（见下文）。

## 安装 Harness 插件

先启动 `dsh-java`（端口 8090），然后分别安装平台插件和 SIIS 插件：

### 平台插件（3 工具）

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

### SIIS 插件（12 工具）

```bash
JAR="$PWD/dsh-ontoext-siis/dsh-ontoext-siis-plugin/target/dsh-ontoext-siis-plugin-0.2.1.jar"

curl -X POST http://localhost:8090/api/harness/plugins/install \
  -H 'Content-Type: application/json' \
  -d "{\"pluginId\":\"dsh-ontoext-siis\",\"displayName\":\"DSH Ontoext SIIS Plugin\",\"pluginVersion\":\"0.2.1\",\"runtimeType\":\"JAVA_NATIVE\",\"sourcePath\":\"$JAR\",\"entrypoint\":\"dsh-ontoext-siis-plugin-0.2.1.jar\"}"

curl -X POST http://localhost:8090/api/harness/plugins/activate \
  -H 'Content-Type: application/json' \
  -d '{"pluginId":"dsh-ontoext-siis"}'
```

| 工具 | 说明 |
| --- | --- |
| `plugin__dsh-ontoext-siis__ontology_list` | 列出本体版本 |
| `plugin__dsh-ontoext-siis__ontology_query` | 查询概念定义与层级 |
| `plugin__dsh-ontoext-siis__ontology_search` | 模糊搜索概念 |
| `plugin__dsh-ontoext-siis__kg_query` | 知识图谱查询 |
| `plugin__dsh-ontoext-siis__kg_search` | 知识图谱搜索 |
| `plugin__dsh-ontoext-siis__relation_analysis` | 双边关系分析 |
| `plugin__dsh-ontoext-siis__alert_query` | 预警查询 |
| `plugin__dsh-ontoext-siis__topic_etc` | 话题管理 |
| `plugin__dsh-ontoext-siis__topic_create` | 创建话题 |
| `plugin__dsh-ontoext-siis__crawl_execute` | 执行爬取 |
| `plugin__dsh-ontoext-siis__nlp_extract` | NLP 实体抽取 |
| `plugin__dsh-ontoext-siis__report_generate` | 报告生成 |

## 配置项

| 配置 | 环境变量 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `server.port` | — | `8100` / `8101` | 聚合应用 / SIIS 应用端口 |
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
       ↑                  ↑
       └── dsh-onto-plugin │
                           └── dsh-ontoext-siis-app
                                   └── dsh-ontoext-siis-plugin
dsh-onto-visulization ←── dsh-onto-app
```

- **base**：不依赖任何业务模块，仅依赖 Spring Boot 基础设施
- **flow**：依赖 base，包含认证授权 + 工作台 + 全局聚合
- **app**：聚合 base + flow + visulization，可独立部署的平台应用
- **SIIS app**：依赖 base + flow，包含四域业务 + 完整 schema
- **plugin**：依赖 base，输出平台工具 JAR
- **SIIS plugin**：依赖 base，输出业务工具 JAR

## 安全边界

- **AI 链路工具化**：插件注册 15 个原子工具，Agent 通过工具调用访问业务能力。
- 查询默认 15 秒超时、最多返回 100 条结果。
- JWT 认证 + 三级角色权限（主任/研究员/访客）+ 多租户数据隔离。
- 生产部署前必须修改默认 JWT 密钥、数据库密码，并配置 CORS/CSRF 策略。

## 测试

```bash
mvn test
```

7 个模块共 115 个测试，覆盖单元测试、集成测试和端到端测试。
