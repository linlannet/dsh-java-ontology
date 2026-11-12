# DeepSeek Harness Ontology v0.2.2 需求规格

> 版本：`v0.2.2` | 基线：`v0.2.1` | 主线：**静态本体引擎化与管理界面可用化** | 架构：七模块 Maven 多模块（Spring Boot 3.5.16 / Java 17） | 双栈：Java 0.2.2 产品层 D0，Python v0.3.1 契约镜像
>
> 本规格覆盖 v0.2.2 将“七模型 YAML 从设计文档变为运行时引擎”的全部需求领域，需求事实来自同目录 `static-ontology-implementation-design.md`（v1.0）、`design-review.md`（2026-10-21 评审结论）与 `adr/ADR-001~005`。姊妹文档 `dynamic-ontology-plan.md` 描述的是 **0.2.3+ 动态本体**，不属于本版本交付范围，仅作为后续演进依据（见 §7）。
>
> **仓库范围声明**：
> 1. 平台侧：`dsh-java-ontology`（`dsh-onto-base` / `dsh-onto-flow` / `dsh-onto-qa` / `dsh-onto-viz` / `dsh-onto-metric` / `dsh-onto-app` / `dsh-onto-plugin`）。
> 2. 业务侧：独立仓库 `dsh-ontolab-siis`（`dsh-ontolab-siis-app` / `dsh-ontolab-siis-plugin`），仅在被本规格显式引用时纳入变更范围。
>
> **依赖与改造约束**：
> 1. **零新增第三方依赖**：YAML 解析复用 `spring-boot-starter-web` 传递提供的 snakeyaml；不引入图数据库客户端、规则引擎、前端框架或构建工具。
> 2. **不新增 Maven 模块**：引擎落在 `dsh-onto-base`，模型 API 落在 `dsh-onto-flow`，界面落在 `dsh-onto-app`。
> 3. **不改既有公开 API 契约**：既有 `/api/workspace/*`、`/api/ontologies/*`、`/api/ontology/*`、`/api/auth/*` 的路径、请求与响应结构保持不变；仅**新增** `/api/model/*` 4 个只读端点。
> 4. **YAML 是模型唯一事实源**：实体/字段/枚举/生命周期以 YAML 为准；物理表映射以 `onto_ontology_type` 注册表为准；代码常量仅作为编译期快照并由一致性测试锁定。
> 5. **不引入动态本体运行时**：M2 行为链、M3 规则解释器、M6 流程解释器不在本版本实现。
>
> **本阶段边界**：本文档只定义“做什么（What）”与验收标准，不包含类签名、SQL DDL、页面像素稿与排期等“怎么做（How）”细节；实现细节见同目录 `design.md`，任务拆解见 `tasks.md`。

---

# **0. 产品层基线与双栈对齐**

> 本章依据 `ai-pilot/poc/TASK-202611-001/design.md`（v0.3.1，2026-11-08 同步 Java 0.2.2）、`00-需求总览.md`、`01-平台层需求规格.md`（v0.4，72 条）与 `03-非功能需求与验收.md`。它不扩大 0.2.2 的实现范围，只明确“本版本在产品层基线中的位置”和“与 Python 双栈的契约边界”。

## 0.1 两层需求模型

1. **产品层**：跨项目复用的本体注册、知识沉淀、汇聚、报告、权限与模型元数据能力；唯一需求基线为 `01-平台层需求规格.md`（72 条：68 P0 / 4 P1）。Java 版与 Python 版共同实现。
2. **项目层**：siis 等具体项目的业务页面、业务流程与业务数据；需求由项目仓自管理，不并入产品层。
3. 本目录的 v0.2.2 是**Java 栈的产品层 D0（静态本体引擎化）**；SIIS 的 demo 数据、管理台业务视图属于项目层/实现控制台，不作为产品层 API 契约。

## 0.2 双栈分工

| 维度 | `dsh-java-ontology`（本版本） | `dsh-ontology`（Python，ai-pilot v0.3.1） |
|------|-------------------------------|------------------------------------------|
| 定位 | Java 栈产品层，D0 静态本体 + 模型元数据 | Python 栈产品层，M1-M4 全量演进 |
| 应用端口 | 平台 8100；SIIS 业务 8101 | 8093；Harness 8090 |
| 需求基线 | 本目录 spec + 72 条 PLT | 同一 72 条 PLT |
| 数据基线 | 沿用既有 46 表（当前 `dsh_java_ontodb` 实库），0.2.2 **不新增表** | 38 表 + 19 张新增表 = 57 表 |
| 契约 | Java 为基线，导出 OpenAPI 片段 | 镜像 Java 路径/结构/错误码 |
| 本版本能力 | 静态本体引擎、Schema 校验、模型 API、晋升 9 类、管理台 | M2-M5 元模型、OWL2 RL/SHACL、规则引擎、Qdrant 检索、19 表 |

> 端口口径修正：ai-pilot 原稿写“Java 8092”，实际平台应用为 **8100**、SIIS 为 **8101**（见 `dsh-onto-app/application.yml` 与 SIIS README）。8092 只能作为未来外部映射端口，不得写入代码默认值。

## 0.3 0.2.2 能力与 PLT 需求映射

| 0.2.2 能力 | 对应 PLT 需求 | 说明 |
|-----------|--------------|------|
| YAML 规范化/模型归位 | PLT-N4-P0-1（元数据模型驱动） | 模型改 YAML 后行为变化，不改代码 |
| 模型引擎/注册表 | PLT-O5-P0-1（类型元数据注册）+ PLT-O5-P0-2（属性元数据） | 专表 + 元数据注册；字段白名单由模型生成 |
| Schema 校验 | PLT-O5（field_schema）+ PLT-P 域（质量约束） | 写入期校验，不等同于发布期质量门禁 |
| 晋升/匹配 9 类 | PLT-O4-P0-1 / PLT-O4-P0-2 | 对齐 `match` 与 `promote` |
| 模型 API | PLT-O5 + PLT-N2-P0-1（API 规范） | 新增契约，需双栈镜像 |
| 管理台 7 视图 | 项目层/实现控制台 | 不纳入产品层 API 基线；Python 版不要求页面 |
| CORS/跨端口 | PLT-N2-P0-2（端口隔离）+ N1 部署 | 只影响 Java 部署拓扑 |
| 演示数据 | 项目层演示 | 不入产品层迁移与契约基线 |
| 工程治理 | PLT-N2 / 质量门禁 | 构建、版本、测试基线 |

## 0.4 契约同步（0.2.2 新增）

Java 0.2.2 新增以下 5 个只读端点，必须作为产品层契约镜像到 Python 栈：

| 端点 | 双栈约束 |
|------|---------|
| `GET /api/model/manifest` | 响应 `Result<T>`；`code/msg/data` 结构一致 |
| `GET /api/model/aggregates` | 同上；域分组字段一致 |
| `GET /api/model/aggregates/{alias}` | 未知 alias 双栈均返回 404 |
| `GET /api/model/integrity` | 报告字段名一致；不返回绝对路径/堆栈 |
| `GET /api/ontology/discovered` | 查询参数 `status/limit` 一致；仅返回 `DISCOVERED` |

契约基线文件为 `ai-pilot/contract/openapi-baseline.yaml`（当前尚未落盘，见 `tasks.md` T12）。在基线建立前，以本目录 `spec.md` §6.5 与 `design.md` §2.3 的字段定义为临时口径。

## 0.5 与 ai-pilot 设计的事实修正

| 编号 | ai-pilot 原稿 | 实际/修正 |
|------|--------------|-----------|
| A1 | Java 端口 8092（`03` 的 PLT-N2-P0-2 同写 8092） | 实际 8100；SIIS 8101；需求文本与设计均需在 T12 同步修正 |
| A2 | 契约基线已落地 | 目录尚未建立，需 T12 补齐 |
| A3 | 产品层 57 表 | Java 侧当前 46 表（`dsh_java_ontodb`，V1.0.7 后全部 `onto_` 前缀）；19 张新增表属 Python 0.3，0.2.2 不建表 |
| A4 | M1-M5 五层模型 | Java 七模型 YAML 为 M1/M2/M3/M5/M6/M7/MU；M4 场景/M5 权限元模型不在 0.2.2 |
| A5 | 角色 SUPER_ADMIN/RESEARCHER/GUEST | 与 Java 的主任(0)/研究员(1)/访客(2) 一一对应，仅命名不同 |

---

# **1. Component Positioning**

## **1.1 Core Responsibility**

本组件负责把 `dsh-onto-base` 中的七模型 YAML（M1/M2/M3/M5/M6/M7/MU）与 `dsh-ontolab-siis` 中的项目层七模型 YAML 加载为**运行时模型注册表**，并以此驱动三件事：

1. **模型元数据服务**：向管理界面与 Agent 暴露聚合根、字段、生命周期、完整性报告。
2. **写入 Schema 校验**：在通用 CRUD 与业务本体 CRUD 写库前，按 YAML 字段定义校验必填、类型、枚举与未知字段。
3. **类型路由一致性**：把“晋升路由、匹配引擎、类型注册表”三份重复映射收敛为同一事实源，覆盖 M1 本体域全部可晋升类型。

同时交付一个**原生 HTML 管理台**（登录、总览、实体维护、搜索、多语种、晋升、AI 对话 7 视图），使“本体库”从空表与 401 页面变为可日常使用的管理界面，并提供中东/东南亚监测场景的可重灌演示数据。

## **1.2 Core Inputs**

1. **基座模型清单**：`classpath:ontology-models/manifest.json` 与 7 个 YAML（`m1-object-model.yaml` 等）。
2. **项目模型清单**：`classpath:siis-models/manifest.json` 与 7 个 YAML（仅 SIIS 应用存在）。
3. **类型注册表数据**：`onto_ontology_type` 表的 14 条种子（type_code / table_name / display_name / domain / status）。
4. **管理台 HTTP 请求**：登录、模型查询、实体 CRUD、跨语种搜索、i18n 维护、匹配预览、晋升、AI 对话。
5. **既有业务数据**：`onto_ontology_*`（元数据 8 张：version/entity/term/event_type/location/i18n/attribute/type）、`onto_ontology_entity` + `onto_ontology_attribute`（8 类业务实体 EAV）、`onto_common_discovered_entity`。
6. **运行配置**：`dsh.ontology.schema-validation.enabled`、`dsh.ontology.model.*`、`dsh.siis.cors.allowed-origins` 等（见 §6.7）。

## **1.3 Core Outputs**

1. **模型注册表**：`alias → AggregateDescriptor`、`category → AggregateDescriptor` 双索引；启动时一次加载，运行期只读。
2. **模型 API 响应**：清单、聚合根树、单聚合根 Schema、完整性报告四类 JSON。
3. **校验结果**：字段级错误列表（字段名、规则、实际值、期望），用于 400 响应。
4. **一致性测试结果**：晋升路由 / 匹配路由 / TypeCatalog / `onto_ontology_type` 四方一致，漂移即失败。
5. **管理台页面**：7 个 hash 路由视图，登录后可用；401 自动跳登录、403 提示角色不足。
6. **演示数据 SQL**：`docs/dev-ops/demo/demo_ontology_siis.sql` 与 `demo_ontology_onto.sql`（可重灌、幂等）。
7. **模型完整性报告**：实体数、字段数、重复别名、悬空引用（M7 `objectRef` / M2 `ownerEntity` 指向不存在聚合根）。

## **1.4 Responsibility Boundaries**

本组件**不负责**：

1. **动态本体运行时**：M2 行为链事件化、M3 规则解释执行、M6 流程节点图解释执行、M1 lifecycle 非法迁移拦截均属于 0.2.3+（`dynamic-ontology-plan.md` D1-D4）。
2. **MU 模型 14 菜单全量实现**：本版本只落地静态本体 7 视图，其余 MU 视图列入后续版本。
3. **Neo4j 图层深化**：`GraphSyncService` / `GraphQueryService` 已就绪，但图算法、生产部署与数据迁移不在本版本（ADR-005 维持 Deferred）。
4. **向量检索**：不引入 Milvus / pgvector；`personal_vector_index` 维持“仅落库、不检索”（ADR-002）。
5. **沙盘/数字孪生**：不创建任何表与端点（ADR-003 Deferred）。
6. **报告双表数据迁移**：仅维持 ADR-004 的“`onto_report_instance` 主表、旧表只读”现状，不做阶段 2/3。
7. **业务数据内容治理**：演示数据仅用于演示与联调，不代表生产事实；人物仅收录公开客观履历，立场类字段留空。
8. **权限模型重构**：沿用 `RoleInterceptor` + `@RequiresRole` / `@PublicApi` 的三级角色（主任 0 / 研究员 1 / 访客 2），不新增权限点体系。
9. **前端框架引入**：管理台为原生 HTML + CSS + JS，无构建步骤、无 npm 依赖。
10. **第三方模型编辑器**：不实现图形化本体建模器；模型变更仍以 YAML + 迁移脚本为准。

---

# **2. Domain Terminology**

**静态本体（Static Ontology）**
: 以 M1 对象模型为核心的实体/字段/枚举/生命周期定义，描述“世界上有什么”，不含运行期行为与流程。

**动态本体（Dynamic Ontology）**
: 叠加在静态本体之上的 M2 行为、M3 规则、M6 流程与 lifecycle 状态机，描述“能做什么、按什么规则做”。本版本不实现。

**模型清单（Manifest）**
: 描述模型版本、领域、基座模型文件列表与项目模型文件列表的 JSON 文件；基座与项目各一份。

**模型引擎（OntologyModelEngine）**
: 负责加载清单与 YAML、构建注册表、执行完整性检查与 Schema 校验的运行时组件，位于 `dsh-onto-base`。

**聚合根（Aggregate Root）**
: M1 中具有独立生命周期的实体，例如 `OntologyCountry`、`OntologyOrganization`、`SysUser`。

**子实体（Sub-entity）**
: 依附于聚合根的实体，具有 `localId`、`cardinality`、`cascadeDelete` 语义，例如 `OntologyI18n`。

**模型字段（Attribute）**
: 聚合根或子实体的属性定义，含 `name`、`label`、`type`、`required`、`unique`、`enumValues`、`defaultValue`、`description`。

**模型类别（category）**
: CRUD 与晋升使用的业务分类标识，例如 `country`、`organization`、`hotspot`、`term`、`bilateralRelation`；与 YAML 的 `alias` 通过归一化字段 `category` 建立映射。

**类型注册表（onto_ontology_type）**
: 记录 `type_code → table_name → display_name → domain` 的元数据表，14 条种子与 ADR-001 路线 A 对应。

**类型目录（TypeCatalog）**
: 引擎内的 14 类类型视图，至少包含 `typeCode`、`tableName`、`dataSource`、`nameColumn`、`promotable` 五个维度；是晋升与匹配的唯一事实源。

**晋升（Promotion）**
: 将 `onto_common_discovered_entity` 中的发现实体（`DISCOVERED`）匹配或写入本体专表，状态置为 `PROMOTED` 的过程。

**发现实体（Discovered Entity）**
: 由采集/NLP 抽取产生、尚未纳入本体的候选实体；本版本不改变其产生逻辑，仅补齐晋升目标路由。

**Schema 校验（Schema Validation）**
: 写库前依据聚合根字段定义对 payload 执行必填/类型/枚举/未知字段检查；未知字段默认仅告警。

**完整性报告（Integrity Report）**
: 启动时对全部模型执行的结构检查结果，含实体/字段计数、重复别名、悬空引用与解析错误。

**演示数据（Demo Dataset）**
: 可重灌的独立 SQL 脚本，覆盖中东/东南亚/大国三类样本，用于管理台与晋升工作台演示；不进入 Flyway schema 演进链。

**基线快照（Baseline Snapshot）**
: YAML 归一化前后实体/字段计数的快照断言，用于防止修复过程静默丢失字段。

---

# **3. Roles and Boundaries**

## **3.1 Core Roles**

| 角色 | 角色层级 | 本版本职责 |
|------|---------|-----------|
| 主任（DIRECTOR） | 0 | 全平台读写、跨租户查看、晋升审批（本版本仅记录状态，不实装审批流） |
| 研究员（RESEARCHER） | 1 | 租户内实体 CRUD、搜索、i18n 维护、晋升提交、AI 对话 |
| 访客（GUEST） | 2 | 共享层只读（既有能力，本版本不扩展） |
| 平台运维 | 不入库 | 模型加载排障、完整性报告查看、演示数据重灌 |
| Agent（Harness） | 不入库 | 通过既有插件工具只读查询本体；本版本不新增 Agent 写工具 |

## **3.2 External Systems**

| 系统 | 关系 | 本版本约束 |
|------|------|-----------|
| MySQL 8 / H2（`MODE=MySQL`） | 权威存储 | 不新增表；仅新增测试夹具与演示数据脚本 |
| `dsh-onto-flow` | 平台 API | 新增 `/api/model/*`；既有 API 不改 |
| `dsh-ontolab-siis-app` | 业务 API 与双数据源 | 补齐晋升/匹配路由；新增 CORS 白名单配置 |
| Harness（8090） | AI 对话后端 | 复用既有 `HarnessAiService` / `HarnessAiStreamService` |
| 飞书/Lark | 不在本版本范围 | 不涉及 |
| Neo4j | 可选图存储 | 默认关闭，不改动 |

## **3.3 Interaction Context**

1. **管理台上下文**：浏览器（8100）→ 登录（8100）→ 同源平台 API（8100）→ 跨端口业务 API（8101，需 CORS）。
2. **引擎上下文**：应用启动 → 解析基座/项目清单 → 构建注册表 → 执行完整性检查 → 暴露只读查询。
3. **写入上下文**：管理台/Agent → CRUD API → 服务层 Schema 校验 → 仓储 → MySQL。
4. **晋升上下文**：晋升工作台 → 匹配预览（8101）→ 晋升（8101）→ 专表写入 + 发现实体状态更新。
5. **AI 上下文**：管理台 → `/api/ontologies/ai[/stream]`（8100）→ Harness（8090）；本版本不改协议。

---

# **4. DFX Constraints**

## **4.1 Performance**

1. **R1** 模型加载应满足：基座 + 项目层（各 7 YAML）冷启动解析 ≤ 2 秒（开发机 SSD、无网络 IO）。
2. **R2** 注册表查询（`byAlias` / `byCategory` / `attributeOf`）应为 O(1) 内存查表，单次 ≤ 1 毫秒。
3. **R3** Schema 校验单请求 ≤ 5 毫秒（字段数 ≤ 50）；校验不得引入额外数据库查询。
4. **R4** 模型 API 单请求 ≤ 50 毫秒（不含首次 JIT）；聚合根树响应 ≤ 200 KB。
5. **R5** 管理台首屏在本地环境 ≤ 2 秒可交互（原生 HTML，无构建产物）。
6. **R6** 演示数据脚本单库执行 ≤ 60 秒；`TRUNCATE + 重插` 可重复执行。

## **4.2 Reliability**

1. **R7** 基座模型加载失败必须**启动失败**（fail-fast），不得以空注册表继续运行。
2. **R8** 项目层模型缺失时允许降级为“仅基座模式”；项目层存在但解析失败时，SIIS 应用必须启动失败（由配置 `project-required=true` 控制）。
3. **R9** 模型完整性检查失败分级：解析错误 → 启动失败；重复别名/悬空引用 → 启动失败；字段计数漂移 → 告警。
4. **R10** 晋升写入必须事务化：专表插入与发现实体状态更新在同一事务内，任一失败整体回滚。
5. **R11** 管理台请求失败必须可见：401 跳登录、403 提示角色不足、400 展示字段级错误、5xx 展示可读错误码。

## **4.3 Security**

1. **R12** 所有新增 `/api/model/*` 端点必须标注 `@RequiresRole(1)`；未标注端点由 `RoleInterceptor` fail-closed 拒绝。
2. **R13** 管理台 JWT 仅存 `localStorage`，请求统一携带 `Authorization: Bearer <token>`；不得写入 Cookie 或 URL。
3. **R14** 日志不得输出 JWT、密码、数据库连接串；演示脚本不得包含真实个人敏感信息。
4. **R15** SQL 必须参数化；现有仓储中的表名拼接仅允许来自 `TypeCatalog` 白名单，禁止接收用户输入表名。
5. **R16** CORS 白名单必须可配置，默认仅允许 `http://127.0.0.1:8100` 与 `http://localhost:8100`；禁止沿用 `*` 通配到新端口。

## **4.4 Maintainability**

1. **R17** 新增一个“已建物理表”的本体类型，理想路径为：YAML 增加聚合根 + `onto_ontology_type` 增加一行 + 迁移脚本建表；本期 `TypeCatalog` 常量仍需 1 处 Java 更新，但**不得再维护晋升与匹配两份映射副本**。
2. **R18** 所有类型映射必须由 `TypeCatalogConsistencyTest` 与 `PromotionRouteConsistencyTest` 锁定；任一映射漂移必须导致测试失败。
3. **R19** YAML 字段规范必须可在 `ModelIntegrityChecker` 报告中定位到文件与节点路径。
4. **R20** 管理台代码保持“单页 + hash 路由 + 原生 JS”，单文件不超过 800 行；新增视图不得引入框架。

## **4.5 Compatibility**

1. **R21** 既有 API 路径、请求体、响应包装 `Result<T>` / `PageResult<T>` 保持不变；新增字段只允许“追加”且默认可忽略。
2. **R22** 既有 144 个存量测试（平台 74 + SIIS 70，`@Test` 计数）必须全绿；新增测试不得修改既有断言以“迁就实现”。
3. **R23** 数据库兼容 MySQL 8 与 H2 `MODE=MySQL`；不新增表，不改变既有列类型。
4. **R24** 版本标识统一为 `0.2.2`；`dsh-onto-app/src/main/resources/application.yml` 中残留的 `0.2.1` 必须修正。
5. **R25** 主仓库根 `pom.xml` 必须可独立构建；不得保留指向已迁出 SIIS 子模块的失效 `<module>` 声明。

---

# **5. Core Capabilities**

## **5.1 YAML 规范化与模型归位**

### **5.1.1 Business Rules**

1. **BR-5.1-1** 每个实体节点必须具备 `name`、`alias`、`description`、`attributes`；聚合根必须具有 `id`、`aggregateType`、`lifecycle`、`tags`；子实体必须具有 `localId`、`cardinality`、`cascadeDelete`。
2. **BR-5.1-2** `alias` 全局唯一；基座层与项目层不得定义同一 `alias` 的实体。
3. **BR-5.1-3** 本体域共享实体（country / organization / person / hotspot / media / book / paper / bilateralRelation）归位到**基座 M1**；SIIS 个人层实体（track / analyze / display 域）归位到**项目层 siis-object**；项目层删除与基座重复的本体域聚合根。
4. **BR-5.1-4** 每个参与 CRUD 校验的聚合根必须声明 `category`，取值与 `onto_ontology_type.type_code` 一致；非 CRUD 实体可省略。
5. **BR-5.1-5** `m1-object-model.yaml` 中不得存在“裸属性块”（无实体头的 `- { name: ... }` 列表）；所有属性必须挂在实体 `attributes` 下。
6. **BR-5.1-6** `siis-object.yaml` 的 8 个本体域聚合根 `attributes` 不得为空；字段来源以 DDL 为准（评审 RP-6）。
7. **BR-5.1-7** 归一化必须保持字段语义不变；允许调整缩进、补齐实体头、移动节点位置，不允许删除既有字段或改变字段名。
8. **BR-5.1-8** 归一化后必须生成基线快照：基座与项目层各自的聚合根数、字段数、枚举数；快照进入测试断言。

### **5.1.2 Interaction Flow**

1. 读取基座 `manifest.json` → 解析 7 个 YAML → 遇到裸属性块时报错并给出行号。
2. 读取项目 `manifest.json`（存在时）→ 解析 7 个 YAML → 校验 `base_manifest` 仅作声明、不按文件系统路径解析。
3. 合并模型：基座先加载，项目层覆盖同名非本体域实体；发现重复 `alias` 立即失败。
4. 为每个聚合根建立 `category` 映射（YAML `category` 优先，其次 `TypeCatalog` 别名映射）。
5. 输出完整性报告并写入启动日志；测试环境同时断言快照。

### **5.1.3 Exception Scenarios**

| 场景 | 处理 |
|------|------|
| YAML 语法错误 | 启动失败，报告文件、行号、列号 |
| 实体缺 `alias` 或 `attributes` | 启动失败，报告节点路径 |
| 基座与项目层 `alias` 重复 | 启动失败，列出双方文件与节点 |
| `category` 不在 `onto_ontology_type` 种子 | 启动失败（或按配置降级为告警，默认失败） |
| 项目层缺失 | 主应用正常启动（仅基座）；SIIS 应用按 `project-required` 决定 |
| 字段回填遗漏 | `ModelIntegrityChecker` 报告字段数低于基线，测试失败 |

## **5.2 模型引擎与注册表**

### **5.2.1 Business Rules**

1. **BR-5.2-1** 引擎位于 `dsh-onto-base`，包 `net.linlan.dsh.ontology.engine`；不得依赖 `dsh-onto-flow` / `dsh-ontolab-siis-app`。
2. **BR-5.2-2** 引擎组件至少包含 `ModelLoader`、`OntologyRegistry`、`AggregateDescriptor`、`AttributeDescriptor`、`SchemaValidator`、`ModelIntegrityChecker`、`TypeCatalog`。
3. **BR-5.2-3** 注册表在启动时一次性构建，运行期只读；对外只暴露查询方法，不暴露可变集合。
4. **BR-5.2-4** `AggregateDescriptor` 必须包含：`id`、`name`、`alias`、`category`（可空）、`lifecycle`、`tags`、`attributes`、`subEntities`、`sourceFile`。
5. **BR-5.2-5** `AttributeDescriptor` 必须包含：`name`、`label`、`type`、`required`、`unique`、`enumValues`、`defaultValue`、`description`、`sourcePath`。
6. **BR-5.2-6** 支持字段类型：`String`、`Long`、`Integer`、`Float`、`Boolean`、`Enum`、`Text`、`DateTime`、`Json`、`Blob`；未知类型视为模型错误。
7. **BR-5.2-7** 引擎不访问数据库；`TypeCatalog` 的数据库一致性由测试与应用启动校验负责。

### **5.2.2 Interaction Flow**

1. 应用启动 → `OntologyEngineConfiguration` 创建 `ModelLoader`。
2. `ModelLoader` 解析基座 + 项目层 → 产出 `List<AggregateDescriptor>`。
3. `OntologyRegistry` 构建 `alias` 与 `category` 双索引。
4. `ModelIntegrityChecker` 校验重复别名、悬空引用、字段数基线。
5. 注册表以只读 Bean 暴露给 flow / SIIS 服务层。

### **5.2.3 Exception Scenarios**

| 场景 | 处理 |
|------|------|
| 基座清单缺失 | 启动失败，提示 `classpath:ontology-models/manifest.json` 不存在 |
| 清单列为空 | 启动失败 |
| 未知字段类型 | 启动失败，列出实体与字段 |
| 悬空引用（`objectRef` / `ownerEntity`） | 启动失败；修复后必须重跑完整性检查 |
| 注册表被并发修改 | 设计上禁止；只读视图抛出 `UnsupportedOperationException` |

## **5.3 Schema 校验挂载**

### **5.3.1 Business Rules**

1. **BR-5.3-1** 校验挂载点为**服务层写方法**：`WorkspaceCrudService.create/update` 与 `OntologyCrudService.create/update`；控制器不承载业务规则。
2. **BR-5.3-2** 校验仅覆盖 `TypeCatalog` 中 `promotable` 或显式声明 `category` 的类型；未覆盖类别原样透传。
3. **BR-5.3-3** 校验规则：必填缺失 → 拒绝；类型不匹配 → 拒绝；枚举值非法 → 拒绝；未知字段 → 告警（默认不拒绝）。
4. **BR-5.3-4** 失败响应为 HTTP 400，响应体沿用 `Result.badRequest`，`data` 为字段级错误列表：
   `{field, rule, expected, actual, message}`。
5. **BR-5.3-5** 开关 `dsh.ontology.schema-validation.enabled` 默认 `true`；关闭时仅记录告警日志，不改变写库行为。
6. **BR-5.3-6** 更新（PUT）仅校验请求中出现的字段；不得因未提交字段缺失而拒绝。
7. **BR-5.3-7** 校验不得改变既有字段的写入值（不做隐式类型转换，除 `Integer/Long/Float` 的数字字符串显式转换）。

### **5.3.2 Interaction Flow**

1. 请求进入 CRUD 服务 → 读取 `category`。
2. 注册表命中聚合根 → `SchemaValidator.validate(category, data)`。
3. 通过 → 调用仓储；失败 → 抛出校验异常 → 全局异常处理器转为 400。
4. 校验耗时与结果写入应用日志（不记录完整 payload，避免敏感信息）。

### **5.3.3 Exception Scenarios**

| 场景 | 处理 |
|------|------|
| 类别未在注册表 | 透传写库（保持存量兼容） |
| 枚举值大小写不一致 | 默认严格匹配；仅在字段 `caseInsensitive=true` 时忽略大小写 |
| 未知字段 | 告警日志 + 继续写入（存量调用方兼容） |
| 校验组件未装配（单测/裁剪环境） | 跳过校验，记录一次性告警 |
| 类型为 `Json` 但值为字符串 | 允许，交由数据库/前端解析；校验仅检查非空 |

## **5.4 晋升路由与匹配补齐**

### **5.4.1 Business Rules**

1. **BR-5.4-1** 晋升路由从 6 类补齐到 **9 类**：`country`、`organization`、`person`、`hotspot`、`media`、`book`、`paper`、`location`、`term`。
2. **BR-5.4-2** 匹配引擎 `ENTITY_TYPE_TO_TABLE` 必须同步补齐到 9 类；仅改晋升路由会导致匹配预览对 hotspot/book/paper 永久 miss。
3. **BR-5.4-3** 匹配引擎的 `ENTITY_NAME_COLUMNS` 必须补齐：`hotspot → hotspot_name`、`book → title`、`paper → title`。
4. **BR-5.4-4** 晋升插入必须按类型选择名称列：`country/organization/person/media/location → name`、`hotspot → hotspot_name`、`book/paper → title`、`term → term_name`；不得统一使用 `name`。
5. **BR-5.4-5** `TypeCatalog` 是晋升、匹配、Schema 校验的唯一事实源；`OntologyPromotionService` 与 `OntologyMatchService` 不再各自持有映射副本。
6. **BR-5.4-6** `TypeCatalog` 覆盖 14 类，其中 `bilateralRelation → onto_ontology_entity`（`category=bilateralRelation`，EAV，走本体库数据源），不因“晋升目标 9 类”而遗漏。
7. **BR-5.4-7** `bilateralRelation`、`eventType`、`entity`、`version`、`i18n` 不进入晋升目标；其数据由汇聚/系统流程维护（范围边界）。
8. **BR-5.4-8** 晋升事务：匹配命中 → 仅关联并更新状态；匹配未命中 → 插入目标专表 → 更新发现实体 `status=PROMOTED`、`ontology_entity_id`；两步同事务。
9. **BR-5.4-9** 目标类型为空时使用发现实体原类型；**显式指定 `targetType` 且不在 9 类内 → 拒绝 400**；**`targetType` 为空且原类型不在 9 类内 → 落入 `onto_ontology_entity` 通用表并记录 `targetTable`**（保留存量兼容行为）。
10. **BR-5.4-10** 三方一致性测试必须断言：`PromotionTargets ⊆ TypeCatalog`、`TypeCatalog == onto_ontology_type`（type_code/table_name 对齐）、`MatchRoutes == PromotionRoutes`。

### **5.4.2 Interaction Flow**

1. 晋升工作台选择发现实体 → 调用 `GET /api/ontology/match?name&type`（匹配预览）。
2. 命中 → 展示已存在实体，晋升时只关联。
3. 未命中 → 选择目标类型（9 类之一）→ `POST /api/ontology/entity/promote`。
4. 服务读取 `TypeCatalog` → 选择数据源（`gwy_` → SIIS；`onto_` → onto）→ 按 `nameColumn` 插入 → 更新发现实体。
5. 返回 `{status, discoveredEntityId, ontologyEntityId, targetTable}`。

### **5.4.3 Exception Scenarios**

| 场景 | 处理 |
|------|------|
| 发现实体不存在/已删除 | 抛 `IllegalArgumentException`，接口 400/404 |
| 目标类型显式指定但未知 | 400，提示 9 类合法值；不得静默落入通用表 |
| 目标类型为空且原类型不在 9 类内 | 按 BR-5.4-9 存量兼容分支落入 `onto_ontology_entity`，返回 `targetTable` |
| 目标专表必填列（如 `title`）为空 | 事务回滚，返回 400 |
| 匹配引擎与晋升路由不一致 | 启动一致性检查告警；测试环境失败 |
| 双数据源未配置 | `onto_` 表回退主数据源（单库模式），`gwy_` 始终主数据源 |
| 并发晋升同一发现实体 | 依赖状态条件更新（`WHERE status='DISCOVERED'`），第二次返回幂等结果或冲突 |

## **5.5 模型元数据 API**

### **5.5.1 Business Rules**

1. **BR-5.5-1** 新增 4 个只读端点，统一前缀 `/api/model`，全部标注 `@RequiresRole(1)`：
   - `GET /api/model/manifest`
   - `GET /api/model/aggregates`
   - `GET /api/model/aggregates/{alias}`
   - `GET /api/model/integrity`
2. **BR-5.5-2** 端点由 `dsh-onto-flow` 的 `ModelController` 提供；因 `flow` 同时被 8100 与 8101 依赖，两个应用均暴露，但内容按各自 classpath 模型而定。
3. **BR-5.5-3** 响应结构沿用 `Result<T>`；列表按 `tags`/域分组，单聚合根返回完整字段与子实体。
4. **BR-5.5-4** `integrity` 返回启动时缓存的报告，不每次重新解析 YAML。
5. **BR-5.5-5** 未知 `alias` 返回 404 `Result.notFound`；不得返回空对象。
6. **BR-5.5-6** 模型 API 只读；不得提供模型上传、在线编辑、热加载 YAML 的端点。

### **5.5.2 Interaction Flow**

1. 管理台总览 → `GET /api/model/aggregates` → 渲染模型树与计数。
2. 进入实体维护 → `GET /api/model/aggregates/{alias}` → `renderForm(schema, data)`。
3. 运维排障 → `GET /api/model/integrity` → 查看重复别名/悬空引用。
4. 版本核对 → `GET /api/model/manifest` → 核对基座/项目层版本与模型清单。

### **5.5.3 Exception Scenarios**

| 场景 | 处理 |
|------|------|
| 引擎未就绪 | 应用启动失败，不暴露 503 空壳 |
| 别名不存在 | 404 + 可读消息 |
| 未登录 | 401（`RoleInterceptor`） |
| 角色不足 | 403 |
| 响应过大（全量聚合根） | 仅返回摘要字段；完整字段走单聚合根端点 |

## **5.6 管理界面（7 视图）**

### **5.6.1 Business Rules**

1. **BR-5.6-1** 管理台落在 `dsh-onto-app/src/main/resources/static/`，原生 HTML + CSS + JS，单页 + hash 路由；不得引入前端框架或构建步骤。
2. **BR-5.6-2** 视图与路由固定为：
   `#/login`、`#/overview`、`#/entities/{category}`、`#/search`、`#/i18n/{category}/{id}`、`#/promote`、`#/ai`。
3. **BR-5.6-3** 登录视图调用 `POST /api/auth/login`，成功后保存 JWT 到 `localStorage`；所有后续请求经 `apiFetch()` 自动附加 Bearer。
4. **BR-5.6-4** `apiFetch()` 统一处理：401 → 清 token + 跳 `#/login`；403 → 提示“角色不足”；400 → 展示字段级错误；5xx → 展示错误码。
5. **BR-5.6-5** 实体维护表单由 `/api/model/aggregates/{alias}` 动态渲染：`type=Enum` → `<select>`；`type=Text` → `<textarea>`；`required=true` → 必填标记；其余 → `<input>`。
6. **BR-5.6-6** 总览必须展示：按域分组的模型树、各 category 计数、完整性报告摘要（实体数/字段数/重复别名/悬空引用）。
7. **BR-5.6-7** 搜索视图支持主表关键词与 i18n 跨语种搜索；结果必须展示命中语种与命中列。
8. **BR-5.6-8** 多语种视图至少支持 `en/fr/ar` 三语种维护；`ru/es/ja/de` 作为可选项。
9. **BR-5.6-9** 晋升工作台必须展示 `DISCOVERED` 列表、匹配预览（命中/未命中徽标）、9 类目标类型选择与晋升结果。
10. **BR-5.6-10** AI 视图复用既有同步 + SSE 流式双模式；不得改动 Harness 协议。
11. **BR-5.6-11** 深色主题对齐 `mu-ui-model.yaml` 的 `theme.colors`；侧边栏 6 项导航（不含登录）。

### **5.6.2 Interaction Flow**

1. 打开 `http://127.0.0.1:8100/` → 未登录 → `#/login`。
2. 输入账号密码 → 获取 token → 跳 `#/overview`。
3. 总览点击某聚合根 → `#/entities/{category}` → 列表/分页/CRUD。
4. 列表点击“多语种” → `#/i18n/{category}/{id}` → 维护翻译。
5. 晋升菜单 → `#/promote` → 匹配预览 → 晋升。
6. AI 菜单 → `#/ai` → 同步或流式对话。

### **5.6.3 Exception Scenarios**

| 场景 | 处理 |
|------|------|
| token 过期 | 401 → 清 token → 登录页 |
| 跨端口 8101 不可达 | 页面提示“业务服务未启动”，其余视图可用 |
| 表单 Schema 加载失败 | 展示错误，不渲染空表单 |
| 枚举字段无 `enumValues` | 退化为文本输入并提示模型缺陷 |
| 晋升目标类型未选择 | 前端阻止提交并提示 |
| 演示数据未导入 | 总览计数为 0，页面给出导入指引 |

## **5.7 登录与跨端口访问**

### **5.7.1 Business Rules**

1. **BR-5.7-1** 管理台（8100）调用 SIIS API（8101）必须通过 CORS；SIIS 应用新增 `CorsConfig`，origin 白名单来自 `dsh.siis.cors.allowed-origins`。
2. **BR-5.7-2** 默认白名单仅 `http://127.0.0.1:8100`、`http://localhost:8100`；生产部署可追加域名，禁止 `*`。
3. **BR-5.7-3** 允许方法 `GET/POST/PUT/DELETE/OPTIONS`；允许头 `Authorization, Content-Type`；预检缓存 3600 秒。
4. **BR-5.7-4** 既有 `dsh-onto-flow/CorsConfig` 的通配配置不得复制到 SIIS；SIIS 必须使用白名单。
5. **BR-5.7-5** 跨端口请求必须携带同一 JWT；SIIS 与平台共享 `JWT_SECRET` 配置约定（部署期环境变量）。

### **5.7.2 Interaction Flow**

1. 浏览器登录 8100 → 获得 token。
2. 访问 SIIS API → 浏览器发送 OPTIONS 预检 → SIIS 返回允许 origin。
3. 实际请求携带 `Authorization` → SIIS `RoleInterceptor` 校验角色 → 返回 `Result<T>`。
4. 预检失败 → 浏览器控制台 CORS 错误；页面提示跨端口配置缺失。

### **5.7.3 Exception Scenarios**

| 场景 | 处理 |
|------|------|
| origin 不在白名单 | 403/浏览器拦截；日志记录 origin |
| 允许凭据与通配符冲突 | 禁止 `*` + `allowCredentials=true` 组合 |
| JWT 密钥不一致 | 401；部署检查清单提示统一密钥 |
| SIIS 未启动 | 页面降级提示，不阻塞平台视图 |

## **5.8 演示数据与中立性**

### **5.8.1 Business Rules**

1. **BR-5.8-1** 演示数据以**独立 SQL 双脚本**交付，放 `docs/dev-ops/demo/`：`demo_ontology_siis.sql`（业务专表）与 `demo_ontology_onto.sql`（元数据/i18n/发现实体）。
2. **BR-5.8-2** 脚本必须可重灌：先 `TRUNCATE`/`DELETE` 指定范围，再插入；执行顺序为“先 SIIS 后 onto”，并在头部注明。
3. **BR-5.8-3** 演示数据不得进入 Flyway 迁移链；脚本仅用于演示/联调环境。
4. **BR-5.8-4** 数据规模基线：国家 32（中东 16 + 东南亚 8 + 大国 8）、组织 ≥50、人物 ≥30、媒体 ≥20、热点 ≥6、双边关系 ≥20、i18n ≥600（重点 140 个实体 × en/fr/ar）、发现实体 12、术语 20、地名 15。
5. **BR-5.8-5** 中立性：`biasRating` 一律留空；风险描述使用客观中性表述；人物仅公开履历，`portraitUrl` 留空。
6. **BR-5.8-6** 演示数据不包含真实个人隐私、未公开信息或敏感立场判断。
7. **BR-5.8-7** onto 库的 `onto_ontology_version.entity_count` 必须回填真实计数，避免“有名无实”。
8. **BR-5.8-8** 演示数据导入后，晋升工作台至少 12 条 `DISCOVERED` 可供演示。

### **5.8.2 Interaction Flow**

1. 在 SIIS 库执行 `demo_ontology_siis.sql`。
2. 在 onto 库执行 `demo_ontology_onto.sql`（依赖前一步自增 ID 的 i18n 记录在脚本内以名称/类型定位）。
3. 启动 8100/8101 → 总览显示计数 → 搜索/多语种/晋升可用。
4. 重灌：重复执行两脚本，计数回到基线。

### **5.8.3 Exception Scenarios**

| 场景 | 处理 |
|------|------|
| 执行顺序颠倒 | i18n 关联失败；脚本头部说明并要求先 SIIS |
| 重复执行主键冲突 | 脚本使用 TRUNCATE/DELETE 或幂等 upsert |
| 字符集错误 | 脚本声明 `SET NAMES utf8mb4` |
| 计数与实体不一致 | 版本计数回填脚本与实体表同批执行 |

## **5.9 版本发布与工程治理**

### **5.9.1 Business Rules**

1. **BR-5.9-1** 根 `pom.xml` 的 `<modules>` 必须与实际存在的模块一致；已迁出的 `dsh-ontolab-siis` 子模块必须从主仓库根 pom 移除，保证主仓库可独立 `mvn validate`。
2. **BR-5.9-2** 版本标识统一为 `0.2.2`：根 pom、7 个平台模块 pom、CHANGELOG、`dsh-onto-app/application.yml` 的 `dsh.app.version`、部署脚本与镜像 tag。
3. **BR-5.9-3** `CHANGELOG.md` 的 `0.2.2` 分节必须包含本版本静态本体交付项，而非仅版本对齐。
4. **BR-5.9-4** 四件套文档与既有设计文档并存：`spec.md` / `design.md` / `tasks.md` / `release-v0.2.2-development-notes.md` 为本版本标准产物；`static-ontology-implementation-design.md` / `dynamic-ontology-plan.md` / `design-review.md` 作为来源与评审证据保留。
5. **BR-5.9-5** 验收门槛：`mvn validate` 通过、平台侧 `mvn test` 全绿（74 存量 + 新增）、SIIS 侧 `mvn test` 全绿（70 存量 + 新增）。
6. **BR-5.9-6** 发布说明必须明确“已实现 / 未实现 / 范围外”，不得把规划项宣称为已完成。

### **5.9.2 Interaction Flow**

1. 修正根 pom → `mvn -DskipTests validate` 通过。
2. 实现引擎/API/界面/路由 → 各模块测试。
3. 更新 CHANGELOG 与版本标识 → 执行全量测试。
4. 导入演示数据 → 端到端冒烟 → 输出开发说明。

### **5.9.3 Exception Scenarios**

| 场景 | 处理 |
|------|------|
| 根 pom 仍含失效模块 | 构建失败；必须先修 pom（本版本 P0） |
| 版本号遗漏 | 发布检查清单阻断 |
| 测试数与文档口径不一致 | 以实际 `@Test` 计数为准；文档记录统计方式 |
| 演示数据污染正式库 | 脚本仅限 demo 环境，文件头部警示 |

---

# **6. Data Constraints**

## **6.1 AggregateDescriptor / AttributeDescriptor**

| 结构 | 字段 | 约束 |
|------|------|------|
| `AggregateDescriptor` | `id` | 非空；建议 `AGG-*` 命名 |
| | `name` | 非空中文名 |
| | `alias` | 非空、全局唯一、Java/路由可用 |
| | `category` | 可空；参与 CRUD 时必须与 `onto_ontology_type.type_code` 一致 |
| | `lifecycle` | 可空列表；聚合根必须至少 1 项 |
| | `tags` | 非空列表；用于域分组 |
| | `attributes` | 非空列表（允许空列表但报告告警） |
| | `subEntities` | 可空；子实体必须含 `localId/cardinality` |
| | `sourceFile` | 非空；用于排障定位 |
| `AttributeDescriptor` | `name` | 非空、实体内唯一 |
| | `label` | 非空 |
| | `type` | 10 类之一 |
| | `required` | 布尔，默认 false |
| | `unique` | 布尔，默认 false |
| | `enumValues` | `type=Enum` 时非空 |
| | `defaultValue` | 字符串；需与类型兼容 |
| | `description` | 可空 |
| | `sourcePath` | 文件 + 节点路径 |

## **6.2 Model Manifest**

1. 基座清单固定路径 `classpath:ontology-models/manifest.json`，含 `model_type=MANIFEST`、`version`、`platform`、`base_models[7]`、`projects[]`。
2. 项目清单固定路径 `classpath:siis-models/manifest.json`，含 `base_manifest`（仅声明，不按文件系统路径解析）、`models[7]`。
3. 清单中文件顺序不影响加载；缺失文件必须报错。
4. 基座与项目层版本号应一致（当前均为 `3.0`）；不一致仅告警。

## **6.3 TypeCatalog（14 类）**

| type_code | table_name | 数据源 | 名称列 | 可晋升 |
|-----------|-----------|--------|--------|--------|
| country | gwy_ontology_country | SIIS | name | 是 |
| organization | gwy_ontology_organization | SIIS | name | 是 |
| person | gwy_ontology_person | SIIS | name | 是 |
| hotspot | gwy_ontology_hotspot | SIIS | hotspot_name | 是 |
| media | gwy_ontology_media | SIIS | name | 是 |
| book | gwy_ontology_book | SIIS | title | 是 |
| paper | gwy_ontology_paper | SIIS | title | 是 |
| bilateralRelation | gwy_ontology_bilateral_relation | SIIS | 无（country_a/country_b） | 否 |
| location | onto_ontology_location | ONTO | name | 是 |
| term | onto_ontology_term | ONTO | term_name | 是 |
| eventType | onto_ontology_event_type | ONTO | event_type | 否 |
| entity | onto_ontology_entity | ONTO | entity_name | 否 |
| version | onto_ontology_version | ONTO | version_name | 否 |
| i18n | onto_ontology_i18n | ONTO | name | 否 |

> **基线变更（方案 A / V1.0.5）**：上表 8 类业务实体的 `gwy_ontology_*` 源表已归并为 `onto_ontology_entity`（`category` 区分 country/organization/person/hotspot/media/book/paper/bilateralRelation）+ `onto_ontology_attribute`（EAV），
> `TypeCatalog` 的 `srcTable` 统一为 `onto_ontology_entity`、`dataSource=ONTO`；SIIS 库 8 张 `gwy_ontology_*` 仅作项目层存量同步源保留（`OntologyEavMigrationRunner` 幂等搬迁）。

> 约束：表名、数据源、名称列、可晋升标志必须与 `onto_ontology_type` 种子及实际 DDL 对齐；`bilateralRelation` 归入 `onto_ontology_entity`（category=bilateralRelation），不再单独建表。

## **6.4 Promotion Targets（9 类）**

1. 晋升目标集合固定为 §6.3 中 `可晋升=是` 的 9 类。
2. `PromotionTargets ⊆ TypeCatalog`，且与 `OntologyMatchService` 可匹配类型集合完全相等。
3. `bilateralRelation/eventType/entity/version/i18n` 不参与发现实体晋升；发现实体若为这些类型且未显式指定目标类型，按 BR-5.4-9 的存量兼容分支落入通用表并记录。
4. 目标类型为空时使用原类型；原类型必须小写归一（`ORGANIZATION → organization`）。

## **6.5 Model API 数据约束**

| 端点 | 顶层结构 | 关键约束 |
|------|---------|---------|
| `/api/model/manifest` | `Result<ManifestVO>` | 含基座/项目版本、模型文件、聚合根与字段计数 |
| `/api/model/aggregates` | `Result<List<DomainGroupVO>>` | 按 `tags` 分组；每项含 alias/name/category/attributeCount |
| `/api/model/aggregates/{alias}` | `Result<AggregateDescriptor>` | 含 attributes/subEntities/lifecycle/sourceFile |
| `/api/model/integrity` | `Result<IntegrityReport>` | 含 aggregateCount/attributeCount/duplicateAliases/danglingRefs/errors |

响应不得包含：绝对文件路径、堆栈、数据库连接信息、YAML 原文。

## **6.6 Demo Data 约束**

| 表 | 基线数量 | 字段约束 |
|----|---------|---------|
| gwy_ontology_country | 32 | `iso_code` 唯一非空 |
| gwy_ontology_organization | ≥50 | `org_type` 五类均衡 |
| gwy_ontology_person | ≥30 | 仅客观字段；`portrait_url` 空 |
| gwy_ontology_media | ≥20 | `bias_rating` 一律空 |
| gwy_ontology_hotspot | ≥6 | `keywords` 为 JSON 字符串；`risk_level` 在枚举内 |
| onto_ontology_entity（category=bilateralRelation） | ≥20 | `country_a`/`country_b` 属性非空（EAV） |
| onto_ontology_i18n | ≥600 | 唯一键 `(entity_id, category, lang)` |
| onto_common_discovered_entity | 12 | `status='DISCOVERED'` |
| onto_ontology_term | 20 | `term_name` 非空 |
| onto_ontology_location | 15 | `parent_id` 允许为空 |

## **6.7 Configuration Items**

| 配置键 | 默认值 | 说明 |
|--------|--------|------|
| `dsh.ontology.schema-validation.enabled` | `true` | Schema 校验总开关 |
| `dsh.ontology.model.project-required` | `false` | 项目层模型缺失是否启动失败；SIIS 应用建议 `true` |
| `dsh.ontology.model.unknown-field-policy` | `warn` | `warn` / `reject` |
| `dsh.siis.cors.allowed-origins` | `http://127.0.0.1:8100,http://localhost:8100` | SIIS CORS 白名单 |
| `dsh.ontology.demo.enabled` | `false` | 演示数据提示开关（不自动导入） |
| `spring.flyway.enabled` | `true` | 维持现状；演示脚本不进入迁移链 |

## **6.8 Database Constraints**

1. 不新增表、不修改既有列类型；`onto_ontology_type` 14 条种子维持。
2. 晋升写入目标表必须存在，且名称列非空；名称列错误应在测试中拦截。
3. i18n 唯一键 `(entity_id, category, lang)` 不得破坏。
4. 演示数据脚本必须使用 `utf8mb4`，不得写入 BLOB/二进制大对象。
5. 软删除统一 `del_flag=0/1`；模型 API 与晋升仅处理 `del_flag=0`。

---

# **7. 范围外与后续版本（dynamic-ontology-plan.md）**

| 阶段 | 内容 | 版本 | 状态 |
|------|------|------|------|
| D0 | 静态本体引擎化（本规格） | 0.2.2 | 本版本交付 |
| D1 | 规则引擎化（M3 → RuleEngine + 热加载 + 审计） | 0.2.3+ | 规划 |
| D2 | 行为链事件化（M2 syncTriggers → ApplicationEvent） | 0.2.3+ | 规划 |
| D3 | 流程引擎化（M6 nodeGraph 解释器 + 审批流） | 0.2.3+ | 规划 |
| D4 | Action 层（Agent 直连 Action，链路 5→3 层） | 0.2.3+ | 规划 |

> 本版本为 D1-D4 预留的接口边界：`OntologyRegistry` 只读模型视图、`TypeCatalog` 统一类型事实源、`/api/model/*` 元数据端点。D1-D4 不得在本版本提前实装，避免范围蔓延与测试基线失稳。

---

# **附：v0.2.2 能力清单与版本范围汇总**

| 能力 | 交付物 | 验收口径 |
|------|--------|---------|
| YAML 规范化 | m1 + siis-object 修复与归位 | 引擎解析零错误；快照计数不下降 |
| 模型引擎 | `dsh-onto-base/ontology.engine` | 双索引可用；完整性报告可查 |
| Schema 校验 | 服务层挂载 + 开关 | 必填/类型/枚举非法返回 400 |
| 晋升与匹配 | TypeCatalog + 9 类路由 | 四方一致性测试绿；hotspot/book/paper 可晋升 |
| 模型 API | `/api/model/*` 4 端点 | 401/403/404 行为正确；响应不含敏感信息 |
| 管理界面 | 7 视图 | 登录→浏览→CRUD→搜索→i18n→晋升→AI 全链路可走通 |
| 跨端口 | SIIS CORS 白名单 | 8100 调 8101 成功；非白名单被拒 |
| 演示数据 | 双 SQL 脚本 | 可重灌；计数与实体一致 |
| 工程治理 | 根 pom 修正 + 版本统一 | `mvn validate` 通过；全量测试绿 |
