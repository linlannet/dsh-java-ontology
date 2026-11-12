# DeepSeek Harness Ontology v0.2.2 技术实现方案

> 版本：`v0.2.2` | 基线：`v0.2.1` | 架构：Spring Boot 3.5.16 / Java 17 / 七模块 Maven 多模块
>
> 本文档定义 v0.2.2 **静态本体引擎化与管理界面可用化**的技术实现。需求口径见同目录 `spec.md`；任务拆解见 `tasks.md`；发布说明见 `release-v0.2.2-development-notes.md`。
>
> 输入来源：`static-ontology-implementation-design.md`（v1.0）、`design-review.md`（2026-10-21）、`adr/ADR-001~005`、`dynamic-ontology-plan.md`（仅 0.2.3+ 路线参考）、`ai-pilot/poc/TASK-202611-001/design.md`（v0.3.1，产品层双栈基线）。
>
> 本文档在源方案基础上完成了 **16 项事实修正**（见 §1.5，含 3 项 ai-pilot 双栈对齐修正），并以当前仓库真实代码、DDL、测试计数为准。所有路径均相对于 `dsh-java-ontology` 仓库根；SIIS 路径相对于独立仓库 `dsh-ontolab-siis`。

---

# **1. 需求与存量功能关系分析**

## **1.1 版本目标与文档关系**

v0.2.2 的核心目标不是“再写一套本体代码”，而是把**已经存在的三块半成品**连接成闭环：

| 半成品 | 现状 | v0.2.2 要做的事 |
|--------|------|----------------|
| 七模型 YAML | 有文件，M1 大量裸属性块；siis-object 8 个聚合根属性为空 | 规范化、归位、去重、可解析 |
| 本体 CRUD / 匹配 / 晋升 | 端点齐全，但晋升只覆盖 6 类，匹配名称列不足 | 收敛 TypeCatalog，补齐 9 类与插入策略 |
| 模型元数据 | 数据库有 14 类注册表，代码里 4 份硬编码映射 | 引擎化 + 模型 API + 一致性测试 |
| 管理界面 | 静态壳 128 行，无登录、无数据、无路由 | 7 视图、JWT、动态表单、演示数据 |

因此采用“**基座新增引擎 + 两端薄挂载 + 前端重写 + 数据脚本**”的最小改造路径，不新增模块、不新增第三方依赖、不改既有 API。

## **1.2 存量资产复用 / 扩展 / 新增**

| 资产 | 处置 | 说明 |
|------|------|------|
| `dsh-onto-base` 七模型 YAML + `manifest.json` | 复用 + 修复 | 规范化 M1；新增引擎包 |
| `OntologyMetaRepository` | 复用 | 已有 list/save/delete/search/AI 5 个方法，作为模型元数据入口（注意：不是引擎注册表） |
| `dsh-onto-flow/GenericCrudController` | 复用 | 11 个端点基类，覆盖 track/analyze/display/workspace |
| `dsh-onto-flow/OntologyController` | 复用 | 7 个端点：list/save/delete/query/search/ai/ai/stream |
| `dsh-onto-flow/RoleInterceptor` | 复用 | fail-closed；仅 `@PublicApi` 放行 |
| `dsh-onto-app/static/*` | 重写 | 现 42 + 18 + 68 = 128 行，扩展为 7 视图 |
| `dsh-onto-app` Flyway `V1.0.0~V1.0.3` | 复用 | 不新增 schema；仅补演示数据脚本 |
| `dsh-ontolab-siis-app` 双数据源 | 复用 | SIIS 主库 + onto 库；晋升跨库写入 |
| `OntologyCrudController`（SIIS） | 复用 + 扩展 | 现 17 个端点；补 Schema 校验与 CORS |
| `OntologyPromotionService` | 改造 | 6 类 → 9 类；按类型名称列插入 |
| `OntologyMatchService` | 改造 | 6 类 → 9 类；补 3 个名称列 |
| `onto_ontology_type` 14 条种子 | 复用 | TypeCatalog 的数据库对照面 |
| `GraphSyncService` / `GraphQueryService` | 复用不动 | ADR-005 Deferred，不扩展 |
| `AgentDrivenOrchestrator` | 复用不动 | 不改变 Agent 编排链 |
| 根 `pom.xml` | 修复 | 移除已迁出的 SIIS `<module>`，恢复可构建 |

**新增**：`dsh-onto-base/.../ontology/engine/**`（引擎 7 个类）、`dsh-onto-flow/.../ModelController`、`dsh-onto-flow/.../SchemaValidationException` + 全局异常处理、SIIS `CorsConfig`、管理台 7 视图、3 个测试类、2 个演示 SQL。

## **1.3 存量功能逐项分析**

### 1.3.1 `dsh-onto-base`：模型文件与元数据仓储

- `dsh-onto-base/src/main/resources/ontology-models/manifest.json` 为基座清单；M1 共 19 个 `alias`，但存在大量**无实体头的裸属性块**（如 `m1-object-model.yaml` 第 78 行起、第 166 行起），YAML 解析器会把它们视为上一实体的属性，造成实体边界错乱。这是必须先修的 **P0**。
- `siis-object.yaml` 有 35 个聚合根 ID，但 8 个本体域聚合根（country/organization/person/hotspot/media/book/paper/bilateralRelation）`attributes` 为空；而对应字段却散落在 M1 裸块中。两份文件存在**同名同义重复**，若直接合并会产生重复 `alias`。
- `siis-models/manifest.json` 的 `base_manifest` 是文件系统相对路径（`../../dsh-onto-base/...`），打成 jar 后在 classpath 中不存在；加载器不能按该路径解析，只能按 classpath 约定加载基座清单。
- `OntologyMetaRepository` 提供 `list/save/delete/search/ai`，用于“模型元数据管理”页面；它不加载 YAML，也不构建注册表。本期不把它误当作引擎。

### 1.3.2 `dsh-onto-flow`：通用 CRUD 与本体入口

- `GenericCrudController` 共 **11 个端点**（categories/list/typed/page/getById/create/update/delete/count/search/query），全部 `@RequiresRole(1)`。`WorkspaceController`、`TrackController`、`AnalyzeController`、`DisplayController` 均继承它。
- `OntologyController` 共 **7 个端点**（list/save/delete/query/search/ai/ai/stream），是平台侧“本体列表 + AI”入口；不承载业务专表 CRUD。
- `RoleInterceptor` 为 fail-closed：只有 `@PublicApi` 放行。全仓库 `@PublicApi` 共 **5 个**（`AppController` 3 个 + `AuthController` 2 个）。新增 `/api/model/*` 必须显式 `@RequiresRole(1)`，否则会被拦截。
- `CorsConfig` 当前为通配配置；本版本**不修改平台 CORS**，只在 SIIS 新增加白名单配置，避免把 `*` 复制到新端口。

### 1.3.3 `dsh-onto-app`：静态壳与运行配置

- `static/index.html`（42 行）、`styles.css`（18 行）、`app.js`（68 行）合计 **128 行**，只有极简占位，没有登录、路由、数据请求。
- `application.yml` 端口 8100；`dsh.app.version` 仍为 `0.2.1`，与根 pom `0.2.2` 不一致（版本治理项）。
- Flyway 已启用，迁移为 `V1.0.0__init.sql`、`V1.0.1__seed_data.sql`（14 类型 + 用户/角色）。`V1.0.2/V1.0.3` 位于 metric 模块。演示数据**不能**放进迁移链，否则会污染生产。

### 1.3.4 `dsh-ontolab-siis`：业务 CRUD、匹配、晋升

- `OntologyCrudController` 共 **17 个端点**：categories/list/page/getById/create/update/delete/count/search/query/match/match-all/promote/get-i18n/upsert-i18n/delete-i18n/search-i18n。
- `OntologyPromotionService` 的 `TYPE_TO_TABLE` 只有 6 类：`country/organization/person/location/media/term`；且 `insertTypedEntity` 统一使用 `INSERT INTO <table> (name, ...)`。**hotspot 表名称列是 `hotspot_name`，book/paper 是 `title`**，统一用 `name` 会在补齐路由后直接 SQL 失败。
- `OntologyMatchService` 的 `ENTITY_TYPE_TO_TABLE` 6 类、`ENTITY_NAME_COLUMNS` 6 类；仅改晋升不改匹配，会出现“能晋升、预览永远 miss”的断裂。
- SIIS 应用端口 8101，双数据源 `siis` + `onto`；当前**没有 CORS 配置**。管理台 8100 调用 8101 会被浏览器拦截。
- SIIS 测试 70 个 `@Test` / 11 个文件；平台 74 个 / 11 个文件；合计 **144 个存量测试**（与评审口径一致）。

### 1.3.5 工程构建现状

主仓库根 `pom.xml` 仍声明 `dsh-ontolab-siis` 两个子模块，但该目录已被 `.gitignore` 忽略且仅剩 `target/`，实际源码在独立仓库。当前 `mvn -o -DskipTests validate` **直接失败**（子模块 `pom.xml` 不存在）。这是本版本所有验证的前置阻断项，必须先修。

## **1.4 参考源与设计依据映射**

| 设计内容 | 依据 |
|---------|------|
| 引擎位置与模块边界 | ADR-001 路线 A + 源设计 3.1 |
| YAML 写时校验 | 源设计 3.2 / 评审 RP-5 |
| 模型 API 4 端点 | 源设计 3.3 / 评审 RP-5 |
| 晋升 9 类与匹配同步 | 评审 §3.1 / §3.2（修正源设计） |
| TypeCatalog 14 类 | ADR-001 + `V1.0.1` 种子 + 实际 DDL |
| 不引入向量 / Neo4j 深化 / 数字孪生 | ADR-002 / ADR-005 / ADR-003 |
| 报告双表收敛 | ADR-004（维持现状） |
| 动态本体 D1-D4 | `dynamic-ontology-plan.md`，本期不实现 |

## **1.5 源方案与 ai-pilot 事实修正清单（评审后定稿）**

| 编号 | 源方案问题 | 事实 | 本方案处理 |
|------|-----------|------|-----------|
| C1 | 晋升只补 3 类到 9 类，未提匹配 | 匹配映射仍 6 类 | 同步补 `EntityTypeToTable` 与名称列 |
| C2 | 晋升插入统一 `name` | hotspot=`hotspot_name`、book/paper=`title` | TypeCatalog 增加 `nameColumn` + 插入策略 |
| C3 | 4.2 把 bilateralRelation 归到 `onto_` 表 | 当时实际是 `gwy_ontology_bilateral_relation` | 修正为 8 专表 + 6 元数据表；方案 A（V1.0.5）已统一归并 `onto_ontology_entity` |
| C4 | 测试基线写 115/144 两种口径 | 实际平台 74 + SIIS 70 = 144 | 文档统一为 144，注明统计方式 |
| C5 | 断言“M1 有 8 个本体域聚合根” | M1 只有裸块，8 个实体头在 siis-object | 归位 + 去重，见 2.1.3 |
| C6 | `sis-model.manifest.base_manifest` 按路径加载 | jar 内无该路径 | 改为 classpath 约定，字段仅声明 |
| C7 | 校验挂载在 Controller | Controller 是继承基类，改造成本高 | 挂到服务层写方法（见 2.6） |
| C8 | 未发现根 pom 失效模块 | 当前主仓库无法构建 | 列为 P0 前置任务 |
| C9 | 未发现 `dsh.app.version=0.2.1` | 与 0.2.2 不一致 | 版本治理任务 |
| C10 | Registry 需要 `category → descriptor` | YAML 无 `category` 字段 | 归一化新增可选 `category` |
| C11 | “零 Java 新增实体” | TypeCatalog 为常量，新增类型仍需改 Java | 本期接受常量 + 一致性测试；ADR-001 全元数据驱动列 0.2.3+ |
| C12 | 未处理 M1/siis-object 重复 alias | 同名本体域聚合根两处定义 | 基座承载共享本体域；项目层删除重复 |
| C13 | SIIS 跨端口未定义 CORS | 当前无 CORS | 新增白名单 `CorsConfig` |
| C14 | ai-pilot 设计写 Java 端口 8092 | 实际平台 8100、SIIS 8101 | 按实际实现修正；8092 仅可作外部映射 |
| C15 | ai-pilot 声称契约基线已落地 | `ai-pilot/contract/` 目录尚未建立 | 新增 T12，落盘 OpenAPI 基线 |
| C16 | ai-pilot 产品层 57 表 | Java 侧当前 46 表（V1.0.7 后全部 `onto_` 前缀） | 0.2.2 不新增表；19 张新增表属 Python 0.3 |

## **1.6 产品层基线与双栈对齐（ai-pilot design v0.3.1）**

### 1.6.1 基线事实

| 项 | 事实 | 对 0.2.2 的约束 |
|----|------|----------------|
| 产品层需求 | `01-平台层需求规格.md` v0.4，72 条（68 P0 / 4 P1） | 0.2.2 只承接静态本体相关 PLT-O1/O3/O4/O5 与 PLT-N2/N4 |
| 双栈 | Java 版 + Python 版（`dsh-ontology`）共同实现同一产品层 | 契约同路径/同结构/同错误码 |
| 数据基线 | Java 46 表（`dsh_java_ontodb` 实库）；Python 目标 57 表（38 + 19） | 0.2.2 不建表；19 张新增表不纳入本期 |
| 端口 | Java 平台 8100、SIIS 8101；Python 8093；Harness 8090 | 8092 仅可作外部映射，不写默认配置 |
| 模型口径 | ai-pilot M1-M5（M1 实体/M2 行为/M3 规则/M4 场景/M5 权限） | Java 七模型 YAML 为 M1/M2/M3/M5/M6/M7/MU；M4/M5 元模型不在 0.2.2 |

### 1.6.2 契约同步设计

0.2.2 新增 5 个只读端点（4 个 `/api/model/*` + 1 个 `/api/ontology/discovered`），需要进入产品层契约基线：

1. **不引入 springdoc/swagger**：本期坚持零新增依赖，不为了导出 OpenAPI 增加 `springdoc-openapi`。
2. **以测试固化契约**：`ModelControllerTest` 与 SIIS `DiscoveredControllerTest` 断言请求参数、响应字段、错误码；测试样例即“可执行契约”。
3. **手写基线片段**：将 5 个端点的 path/parameters/schema/responses 写入 `ai-pilot/contract/openapi-baseline.yaml`（目录当前不存在，T12 创建）。
4. **双栈 diff**：Python 侧以该基线做 `openapi-diff`；Java 侧变更必须先改基线再改实现，避免反向漂移。
5. **结构约束**：响应统一 `{code, msg, data}`；未知 alias 404；未登录 401；角色不足 403。

### 1.6.3 范围切分

| 能力 | Java 0.2.2 | Python v0.3.1 | 说明 |
|------|-----------|---------------|------|
| 静态本体引擎 | 交付 | 计划 | 同一 PLT-O5 语义 |
| Schema 写入校验 | 交付 | 计划 | Java 先落地，Python 镜像 |
| 模型 API | 交付 4 端点 | 镜像 | 契约基线 |
| 晋升 9 类 | 交付 | 镜像 | 目标类型与名称列一致 |
| M2-M5 元模型 | 不交付 | 交付 | 0.2.3+ Java 追赶 |
| OWL2 RL / SHACL | 不交付 | 交付 | Python 专属本期 |
| Qdrant/Redis/arq | 不交付 | 交付 | 不引入 Java 依赖 |
| 19 张新增表 | 不交付 | 交付 | Java 46 表不动 |
| 管理台 7 视图 | 交付（实现控制台） | 不要求 | 非产品层契约 |



---

# **2. 增量设计**

## **2.1 实现模型**

### 2.1.1 上下文视图

```text
┌──────────────────────────── 浏览器 8100 ────────────────────────────┐
│  static/index.html + app.js + styles.css（7 视图 / hash 路由 / JWT） │
└───────────────┬───────────────────────────────┬─────────────────────┘
                │ 同源 /api/*                    │ 跨端口 /api/ontology/*
                ▼                               ▼
┌──────────────────────────────┐   ┌──────────────────────────────────┐
│ dsh-onto-app (8100)          │   │ dsh-ontolab-siis-app (8101)      │
│  ├─ AuthController           │   │  ├─ OntologyCrudController (17)  │
│  ├─ OntologyController (7)   │   │  ├─ OntologyMatchService         │
│  ├─ ModelController (新增4)   │   │  ├─ OntologyPromotionService     │
│  └─ Workspace/Track/... (11) │   │  └─ CorsConfig（新增白名单）      │
│         │                    │   │         │                        │
│         ▼                    │   │         ▼                        │
│  dsh-onto-flow               │   │  dsh-onto-base (依赖)            │
│  dsh-onto-base  ◄────────────┼───┼─ OntologyModelEngine             │
│   ├─ ontology-models/*.yaml  │   │  ├─ ModelLoader                  │
│   ├─ ontology/engine/**      │   │  ├─ OntologyRegistry             │
│   └─ OntologyMetaRepository  │   │  ├─ SchemaValidator              │
└───────────────┬──────────────┘   │  └─ TypeCatalog                  │
                │                  └──────────────┬───────────────────┘
                ▼                                 ▼
         MySQL onto 库                     MySQL siis 库 + onto 库
                                      （siis-models/*.yaml 仅 SIIS 侧加载）
```

### 2.1.2 模块职责

| 层 | 模块 | 职责 | 本版本变更 |
|----|------|------|-----------|
| 模型层 | `dsh-onto-base` | YAML、引擎、描述符、TypeCatalog、校验器、完整性检查、元数据仓储 | 新增 `ontology.engine` 包；修复 YAML |
| 平台 API | `dsh-onto-flow` | 通用 CRUD、本体入口、模型 API、角色拦截、CORS | 新增 `ModelController`；服务层挂校验 |
| 业务 API | `dsh-ontolab-siis-app` | 业务专表 CRUD、匹配、晋升、i18n | 改晋升/匹配；加 CORS；挂校验 |
| 应用层 | `dsh-onto-app` | 启动、Flyway、静态资源 | 重写 static；修版本号 |
| 数据层 | `docs/dev-ops/demo` | 演示数据 | 新增双 SQL |
| 测试 | 各模块 | 一致性、引擎、校验、界面冒烟 | 新增 3 类测试 |

### 2.1.3 数据流

1. **启动流**：`OntologyEngineConfiguration` → `ModelLoader` 读基座/项目清单 → 解析 YAML → 构建 `AggregateDescriptor` → `OntologyRegistry` 建双索引 → `ModelIntegrityChecker` 校验 → 只读 Bean。
2. **读流**：管理台 → `ModelController` → `OntologyRegistry` → DTO。
3. **写流**：管理台 → CRUD Controller → Service（校验）→ Repository → DB。
4. **晋升流**：晋升工作台 → `OntologyCrudController` → `OntologyMatchService`（9 类匹配）→ `OntologyPromotionService`（TypeCatalog 选表/选列）→ 事务写两库 → 返回结果。

### 2.1.4 归位与去重策略（C5/C12）

**目标**：基座 = 共享层（系统 + 本体域 8 类 + 本体元数据），项目层 = SIIS 个人层（track/analyze/display）。**同一 alias 只允许出现一次**。

1. 在 `m1-object-model.yaml` 中，为 8 个本体域裸属性块补实体头（`id` / `name` / `alias` / `category` / `aggregateType` / `lifecycle` / `tags` / `attributes`）。
2. 将 M1 中 track/analyze/display 域裸属性块**移除**，其字段迁移到 `siis-object.yaml` 对应已有聚合根下。
3. 从 `siis-object.yaml` 删除 8 个本体域聚合根（country/org/person/hotspot/media/book/paper/bilateralRelation），改为引用基座注册表（前端按 `alias` 查询即可）。
4. 保留 `siis-object.yaml` 的个人层 27 个聚合根，并回填其空 `attributes`。
5. 基线快照：基座聚合根数、项目层聚合根数、总字段数、枚举字段数；由 `ModelIntegrityCheckerTest` 固定，只允许在评审后更新。

> 若评审坚持“M1 必须承载 52 实体”，则替代方案是：项目层不定义任何重复 alias，并在 `ModelLoader` 中实现“项目层同名覆盖”策略。本方案选择更清晰的物理去重，避免运行期覆盖语义。

## **2.2 模型引擎详细设计**

### 2.2.1 包与类

| 类 | 包 | 职责 |
|----|----|------|
| `OntologyModelEngine` | `net.linlan.dsh.ontology.engine` | 门面：加载、注册表、完整性报告的统一入口 |
| `ModelLoader` | `...engine.loader` | 读 manifest + YAML，产出描述符；只做解析与结构转换 |
| `ModelManifest` | `...engine.model` | 清单 DTO：版本、模型类型、文件列表、基座/项目标识 |
| `AggregateDescriptor` | `...engine.model` | 聚合根描述符（不可变） |
| `AttributeDescriptor` | `...engine.model` | 字段描述符（不可变） |
| `OntologyRegistry` | `...engine.registry` | alias/category 双索引；只读查询 |
| `SchemaValidator` | `...engine.validation` | 必填/类型/枚举/未知字段校验 |
| `ModelIntegrityChecker` | `...engine.validation` | 重复 alias、悬空引用、空属性、基线计数 |
| `TypeCatalog` | `...engine.catalog` | 14 类类型事实源（常量快照） |
| `TypeCatalogEntry` | `...engine.catalog` | 单类型：typeCode/tableName/dataSource/nameColumn/promotable |
| `OntologyEngineConfiguration` | `...engine.config` | Spring 装配；`@ConditionalOnProperty` 开关 |
| `SchemaValidationException` | `net.linlan.dsh.common.exception` | 携带字段级错误；由 flow 全局异常处理转 400 |

### 2.2.2 清单与 YAML 加载

```text
ModelLoader.load():
  1. 读 classpath:ontology-models/manifest.json（必须存在）
  2. 校验 model_type=MANIFEST、version、base_models[7] 非空
  3. 逐个读取 base_models[*].file；Yaml(SafeConstructor) 解析
  4. 若存在 classpath:siis-models/manifest.json：
       a. 读项目清单，忽略 base_manifest 的 filesystem 路径
       b. 逐个读取 models[*].file
       c. 与基座合并；重复 alias → ModelLoadException
  5. 将每个实体节点转换为 AggregateDescriptor；记录 sourceFile/sourcePath
  6. 返回 EngineSnapshot(descriptors, manifests, errors)
```

关键约束：

1. 使用 SnakeYAML `SafeConstructor`，禁止 `!!java.*` 反序列化。
2. 每个 YAML 根节点必须含 `model_type`、`version`、`entities`。
3. 裸属性块检测：若列表项是 `Map` 且含 `name/type` 但无 `alias`，且其父节点无 `attributes` 键，则判定为“裸块”，报告文件+行号并终止。
4. `base_manifest` 仅写入报告，不参与资源解析。
5. 文件顺序无关；缺失文件一律失败，不做静默跳过。

### 2.2.3 注册表

```java
public final class OntologyRegistry {
    private final Map<String, AggregateDescriptor> byAlias;      // alias -> descriptor
    private final Map<String, AggregateDescriptor> byCategory;   // category -> descriptor
    private final List<AggregateDescriptor> all;

    public Optional<AggregateDescriptor> byAlias(String alias) { ... }
    public Optional<AggregateDescriptor> byCategory(String category) { ... }
    public Collection<String> categories() { ... }
    public List<AggregateDescriptor> byTag(String tag) { ... }
}
```

- `byCategory` 仅索引声明了 `category` 的聚合根（14 类）。
- 返回集合使用 `List.copyOf` / `Map.copyOf`，禁止调用方修改。
- 注册表不缓存校验结果，避免请求间串味。

### 2.2.4 描述符与 category 归一

```java
public record AggregateDescriptor(
    String id, String name, String alias, String category,
    List<String> lifecycle, List<String> tags,
    List<AttributeDescriptor> attributes,
    List<AggregateDescriptor> subEntities,
    String sourceFile
) {}

public record AttributeDescriptor(
    String name, String label, String type,
    boolean required, boolean unique,
    List<String> enumValues, String defaultValue,
    String description, String sourcePath
) {}
```

`category` 解析顺序：

1. YAML 实体节点显式 `category`；
2. `TypeCatalog` 的 `alias → typeCode` 映射（兼容存量 YAML）；
3. 都没有 → `null`（不参与 CRUD 校验与 category 索引）。

YAML 需为 14 个类型补 `category`，示例：

```yaml
  - id: AGG-ONT-COUNTRY-001
    name: 国家
    alias: OntologyCountry
    category: country
    aggregateType: AGGREGATE_ROOT
    lifecycle: [有效, 无效]
    tags: [本体域, 共享层]
    attributes:
      - { name: id, label: 主键, type: Long, required: true, unique: true }
      - { name: name, label: 国家名称, type: String, required: true }
```

### 2.2.5 Schema 校验器

规则实现：

| 规则 | 行为 |
|------|------|
| `required=true` 且值为 `null`/空串 | `required` 违规 |
| `type=Long/Integer/Float` 且值无法解析 | `type` 违规（允许数字字符串显式转换） |
| `type=Boolean` 且值不是 `true/false` | `type` 违规 |
| `type=Enum` 且值不在 `enumValues` | `enum` 违规 |
| 未知字段 | `unknown` 告警；`unknown-field-policy=reject` 时升级为错误 |
| `type=Json` 且值为字符串 | 放行；仅非空检查 |
| 更新场景 | 仅校验提交字段；不校验未提交字段 |

校验结果：

```java
public record FieldViolation(
    String field, String rule, Object expected, Object actual, String message
) {}
```

`SchemaValidator.validate(String category, Map<String,Object> data, boolean partial)`
返回 `List<FieldViolation>`；服务层若列表非空则抛 `SchemaValidationException`。

### 2.2.6 完整性检查

`ModelIntegrityChecker.check(EngineSnapshot)`：

1. 空 `attributes` 聚合根 → 错误（本体域 14 类不允许空）。
2. 重复 `alias` → 错误，列出双方 `sourceFile`。
3. 重复字段名（同一实体）→ 错误。
4. `type=Enum` 但 `enumValues` 为空 → 错误。
5. M7 `objectRef` / M2 `ownerEntity` 指向不存在 `alias` → 错误。
6. 基线计数漂移 → 告警（测试中为失败）。
7. 输出 `IntegrityReport`，启动日志 INFO 摘要 + WARN 明细。

### 2.2.7 TypeCatalog

```java
public record TypeCatalogEntry(
    String typeCode, String tableName, DataSource dataSource,
    String nameColumn, boolean promotable
) {}
```

14 条常量见 `spec.md` §6.3。关键点：

1. `bilateralRelation` → `onto_ontology_entity`（`category=bilateralRelation`，EAV），`dataSource=ONTO`，`nameColumn=null`（名称由 `country_a`/`country_b` 属性组成），`promotable=false`。
   > 方案 A / V1.0.5 变更：8 类业务实体统一归并 `onto_ontology_entity` + `onto_ontology_attribute`，不再使用 `gwy_ontology_*` 专表。
2. `hotspot` → `hotspot_name`；`book/paper` → `title`；`term` → `term_name`；`eventType` → `event_type`；`entity` → `entity_name`；`version` → `version_name`；其余 `name`。
3. `promotable=true` 的 9 条必须与 `OntologyPromotionService` 的目标集合完全一致。
4. `TypeCatalog` 不读取数据库；`TypeCatalogConsistencyTest` 用 `JdbcTemplate` 查询 `onto_ontology_type` 比对 `type_code/table_name`。
5. ADR-001 的“全元数据驱动”改造（Repository 读取 table_name）列入 0.2.3+；本期以常量 + 测试锁定，避免改动面扩大（C11）。

### 2.2.8 装配与开关

```java
@Configuration
@ConditionalOnProperty(name = "dsh.ontology.model.enabled", havingValue = "true", matchIfMissing = true)
public class OntologyEngineConfiguration {
    @Bean ModelLoader modelLoader(ObjectMapper mapper) { ... }
    @Bean OntologyModelEngine engine(ModelLoader loader) { ... } // 启动即加载
    @Bean SchemaValidator schemaValidator(OntologyModelEngine engine) { ... }
}
```

- 基座加载失败 → Bean 创建异常 → 应用启动失败（满足 BR-5.2-1 / R7）。
- 项目层缺失 → `project-required=false` 时仅基座；SIIS 配 `true`。
- 加载结果写入 `application.log`：基座/项目版本、实体数、字段数、耗时。

## **2.3 模型 API 设计（新增）**

`dsh-onto-flow/src/main/java/net/linlan/dsh/plat/controller/ModelController.java`

| 方法 | 路径 | 权限 | 响应 |
|------|------|------|------|
| GET | `/api/model/manifest` | `@RequiresRole(1)` | `Result<ManifestVO>` |
| GET | `/api/model/aggregates` | `@RequiresRole(1)` | `Result<List<DomainGroupVO>>` |
| GET | `/api/model/aggregates/{alias}` | `@RequiresRole(1)` | `Result<AggregateVO>` |
| GET | `/api/model/integrity` | `@RequiresRole(1)` | `Result<IntegrityVO>` |

响应示例：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "baseVersion": "3.0",
    "projectVersion": "3.0",
    "modelFiles": ["m1-object-model.yaml", "..."],
    "aggregateCount": 54,
    "attributeCount": 620
  }
}
```

约束：

1. 不返回 `sourceFile` 的绝对路径（返回相对 `classpath:` 路径）。
2. 不返回 YAML 原文、堆栈、DB 连接信息。
3. `aggregates` 只返回摘要（alias/name/category/attributeCount/tags），完整字段走单实体端点。
4. 未知 `alias` → 404 `Result.notFound`。
5. `integrity` 读取启动缓存，不重新解析。

## **2.4 数据设计**

### 2.4.1 不新增表

本版本不建表、不改列。`onto_ontology_type` 14 条种子维持；仅演示脚本插入业务数据。

### 2.4.2 类型路由表（实现口径）

| typeCode | tableName | 数据源 | nameColumn | promotable |
|----------|-----------|--------|------------|------------|
| country | onto_ontology_entity | ONTO | name | true |
| organization | onto_ontology_entity | ONTO | name | true |
| person | onto_ontology_entity | ONTO | name | true |
| hotspot | onto_ontology_entity | ONTO | hotspot_name | true |
| media | onto_ontology_entity | ONTO | name | true |
| book | onto_ontology_entity | ONTO | title | true |
| paper | onto_ontology_entity | ONTO | title | true |
| bilateralRelation | onto_ontology_entity | ONTO | null | false |
| location | onto_ontology_location | ONTO | name | true |
| term | onto_ontology_term | ONTO | term_name | true |
| eventType | onto_ontology_event_type | ONTO | event_type | false |
| entity | onto_ontology_entity | ONTO | entity_name | false |
| version | onto_ontology_version | ONTO | version_name | false |
| i18n | onto_ontology_i18n | ONTO | name | false |

### 2.4.3 演示数据

- `docs/dev-ops/demo/demo_ontology_siis.sql`：国家 32、组织 50、人物 30、媒体 20、热点 6、双边关系 20、书籍/论文若干；`SET NAMES utf8mb4`；先清理后插入。
- `docs/dev-ops/demo/demo_ontology_onto.sql`：术语 20、地名 15、i18n ≥600、发现实体 12、版本计数回填。
- 执行顺序：SIIS → onto。脚本头部注释写明目标库、顺序、重灌方式、中立性约束。
- 不进入 Flyway；`dsh.ontology.demo.enabled` 仅控制页面提示，不自动执行。

## **2.5 分层落位清单**

| 文件 | 动作 | 说明 |
|------|------|------|
| `dsh-onto-base/src/main/resources/ontology-models/m1-object-model.yaml` | 修改 | 补 8 个实体头 + category；移除个人层裸块 |
| `.../ontology-models/siis-object.yaml`（SIIS 仓库） | 修改 | 删除 8 个重复聚合根；回填个人层属性 |
| `.../ontology-models/m2~m7*.yaml` | 检查 | 确认实体头完整，不强制修改 |
| `dsh-onto-base/.../ontology/engine/**` | 新增 | 7 个类 + config |
| `dsh-onto-base/.../common/exception/SchemaValidationException.java` | 新增 | 校验异常 |
| `dsh-onto-flow/.../plat/controller/ModelController.java` | 新增 | 4 端点 |
| `dsh-onto-flow/.../common/exception/GlobalExceptionHandler.java` | 修改/新增 | 校验异常 → 400 |
| `dsh-onto-flow/.../workspace/service/WorkspaceCrudService.java` | 修改 | 写方法挂校验 |
| `dsh-ontolab-siis-app/.../ontology/service/OntologyCrudService.java` | 修改 | 写方法挂校验 |
| `.../ontology/service/OntologyPromotionService.java` | 修改 | TypeCatalog + nameColumn + 事务 |
| `.../ontology/service/OntologyMatchService.java` | 修改 | TypeCatalog + 9 类 + 名称列 |
| `dsh-ontolab-siis-app/.../config/CorsConfig.java` | 新增 | 白名单 |
| `dsh-onto-app/src/main/resources/static/index.html` | 重写 | 7 视图容器 |
| `.../static/app.js` | 重写 | 路由/apiFetch/渲染 |
| `.../static/styles.css` | 重写 | 深色主题 |
| `dsh-onto-app/src/main/resources/application.yml` | 修改 | `dsh.app.version: 0.2.2` |
| 根 `pom.xml` | 修改 | 移除失效 SIIS modules |
| `docs/dev-ops/demo/demo_ontology_*.sql` | 新增 | 演示数据 |
| 测试类 | 新增 | Engine/Consistency/Validation |
| `CHANGELOG.md` | 修改 | 0.2.2 分节 |

## **2.6 Schema 校验挂载设计**

**挂载点选择**：服务层写方法，而非 Controller。

- 平台：`WorkspaceCrudService.create/update`（`CrudService` 的实现之一）。
- 业务：`OntologyCrudService.create/update`。
- 原因：`GenericCrudController` 是抽象基类，端点方法被 4 个控制器继承；在 Controller 挂载需要改基类构造函数，波及全部子类与 74 个存量测试。服务层挂载点更窄。
- 兼容策略：为避免修改既有构造函数（保持 144 个存量测试可编译），采用可选字段注入：

```java
@Autowired(required = false)
private SchemaValidator schemaValidator;

public Long create(String category, Map<String, Object> data) {
    if (schemaValidator != null) {
        schemaValidator.validateOrThrow(category, data, false);
    }
    return repository.create(category, data);
}
```

- 单测手动 `new WorkspaceCrudService(repository)` 时 `schemaValidator=null`，行为不变。
- 生产环境 Bean 必然存在；`enabled=false` 时校验器内部直接返回空列表。
- 异常映射：`SchemaValidationException` → **HTTP 400**，响应体 `Result.badRequest` + `data.fieldViolations`。全局处理器用 `ResponseEntity.badRequest()` 返回，既符合 `spec.md` §5.3.4，也不改变既有成功响应的 `Result` 风格。

> 设计取舍：字段注入通常不推荐，但此处用于“零测试破坏”的最小改造；后续 0.2.3 可改为构造注入并统一升级测试。

## **2.7 晋升与匹配改造**

### 2.7.1 现状

- 晋升 6 类、匹配 6 类；`insertTypedEntity` 统一 `name`。
- 目标专表名称列：`hotspot_name` / `title` / `title`，统一 `name` 必然失败。
- `bilateralRelation` 无 `name` 列，不能走晋升。

### 2.7.2 改造

```java
// OntologyPromotionService
private final TypeCatalog typeCatalog;
...
TypeCatalogEntry entry = typeCatalog.require(targetType);
String table = entry.tableName();
String nameColumn = entry.nameColumn(); // null 时拒绝晋升
jdbcTemplate.update(
    "INSERT INTO " + table + " (" + nameColumn + ", description, lang, del_flag) VALUES (?, ?, ?, 0)",
    name, description, lang
);
```

- 表名/列名必须来自 `TypeCatalog` 白名单，禁止字符串拼接用户输入。
- 匹配：

```java
private static final Map<String, String> NAME_COLUMNS = TypeCatalog.nameColumns(); // 9 类
```

- `match(name,type)` 与 `matchAll` 均走 TypeCatalog；类型小写归一。
- 晋升事务：

```java
// 按目标数据源选择事务管理器；跨库不做强一致事务
public Map<String,Object> promote(Long id, String targetType) {
    // 1. 读取发现实体（禁止已晋升重复处理）
    // 2. 匹配：命中 -> 仅更新状态/关联
    // 3. 未命中 -> 按 TypeCatalog 选择数据源并插入目标表
    // 4. 条件更新 discovered_entity (WHERE id=? AND status='DISCOVERED')
    // 5. 任一步失败：保留 ontology_entity_id 供幂等重试/补偿
}
```

双数据源注意：`onto_ontology_entity` 在 onto 库；`gwy_` 在 SIIS 库。若目标为 `onto_`，使用 onto 事务管理器；若为 `gwy_`，使用 SIIS 事务管理器。跨库无法强一致，采用“先写目标表、后更新发现实体；失败则补偿删除”或分事务 + 幂等重试。设计选择：**分事务 + 幂等**，因为 MySQL 跨库事务不可用；在 `discovered_entity` 增加 `ontology_entity_id` 记录已写入目标 ID，失败重试时先查后写。

### 2.7.3 一致性测试

`TypeCatalogConsistencyTest`：

1. `TypeCatalog` 14 条 vs `onto_ontology_type` 14 条：`type_code` 集合相等、`table_name` 对齐。
2. `promotable` 9 条 == `OntologyPromotionService.targets()`。
3. `TypeCatalog.nameColumns()` == `OntologyMatchService.nameColumns()`。
4. `bilateralRelation.tableName` 以 `gwy_` 开头。

`PromotionRouteConsistencyTest`：

1. 9 类每类至少一条可晋升夹具。
2. hotspot 晋升写入 `hotspot_name`；book/paper 写入 `title`。
3. 目标类型为空 → 使用发现实体原类型；原类型在 9 类内则正常晋升。
4. 显式目标类型未知 → 400；目标类型为空且原类型不在 9 类内 → 落入 `onto_ontology_entity` 通用表（存量兼容），并记录 `targetTable`。

## **2.8 管理界面设计**

### 2.8.1 文件结构

```text
dsh-onto-app/src/main/resources/static/
├── index.html     # 骨架 + 侧边栏 + 视图容器
├── styles.css     # 深色主题（对齐 mu-ui-model.yaml theme.colors）
└── app.js         # hash 路由 + apiFetch + 视图渲染 + 表单渲染
```

无构建步骤、无 npm、无框架。单文件建议 ≤800 行；若超限，拆 `views/` 静态脚本并按顺序引入。

### 2.8.2 路由与 API 映射

| 路由 | 视图 | 主要 API |
|------|------|---------|
| `#/login` | 登录 | `POST /api/auth/login` |
| `#/overview` | 总览 | `GET /api/model/aggregates`；`GET /api/model/integrity`；`GET /api/workspace/{cat}/count`；`GET /api/ontology/{cat}/count`（8101） |
| `#/entities/{category}` | 实体维护 | `GET /api/model/aggregates/{alias}`；`GET /api/ontology/{category}/page`；`POST/PUT/DELETE /api/ontology/{category}` |
| `#/search` | 搜索 | `GET /api/ontology/search`；`GET /api/ontology/i18n/search` |
| `#/i18n/{category}/{id}` | 多语种 | `GET/POST /api/ontology/i18n/{category}/{id}`；`DELETE .../{lang}` |
| `#/promote` | 晋升 | `GET /api/ontology/discovered?status=DISCOVERED&limit=20`（新增只读）；`GET /api/ontology/match`；`POST /api/ontology/entity/promote` |
| `#/ai` | AI 对话 | `POST /api/ontologies/ai`；`POST /api/ontologies/ai/stream`（SSE） |

> 若“发现实体列表”无现成端点，本期在 SIIS 侧新增 `GET /api/ontology/discovered?status=DISCOVERED&limit=20`（只读，`@RequiresRole(1)`），避免前端直连数据库或平台接口错位。该端点计入“新增端点”变更。

### 2.8.3 前端关键实现

```javascript
const API_BASE = 'http://127.0.0.1:8101';   // SIIS
const LOCAL_BASE = '';                       // 8100 同源

async function apiFetch(path, options = {}) {
  const token = localStorage.getItem('dsh_token');
  const res = await fetch((path.startsWith('/api/ontology') ? API_BASE : LOCAL_BASE) + path, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(options.headers || {})
    }
  });
  if (res.status === 401) { localStorage.removeItem('dsh_token'); location.hash = '#/login'; throw new Error('未登录'); }
  if (res.status === 403) throw new Error('角色不足');
  const body = await res.json();
  if (body.code !== 200) throw body;
  return body;
}
```

表单渲染：

```javascript
function renderField(attr, value) {
  const required = attr.required ? 'required' : '';
  if (attr.type === 'Enum') {
    return `<select name="${attr.name}" ${required}>${(attr.enumValues||[]).map(v => `<option ${v===value?'selected':''}>${v}</option>`).join('')}</select>`;
  }
  if (attr.type === 'Text') return `<textarea name="${attr.name}" ${required}>${value ?? ''}</textarea>`;
  return `<input name="${attr.name}" value="${value ?? ''}" ${required}>`;
}
```

### 2.8.4 主题

- 背景 `#0f172a`，面板 `#111c2e`，主色 `#3b82f6`，成功 `#22c55e`，告警 `#f59e0b`，危险 `#ef4444`，文本 `#e2e8f0`，弱文本 `#94a3b8`。
- 侧边栏固定 220px；内容区最大 1440px；表格斑马纹；表单两列（窄屏一列）。
- 不引入图标库；使用纯文本符号或内联 SVG（可选）。

## **2.9 跨端口与安全设计**

### 2.9.1 SIIS CORS

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Value("${dsh.siis.cors.allowed-origins:http://127.0.0.1:8100,http://localhost:8100}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET","POST","PUT","DELETE","OPTIONS")
                .allowedHeaders("Authorization","Content-Type")
                .maxAge(3600);
    }
}
```

- 禁止 `allowedOrigins("*")` + `allowCredentials(true)`。
- 不修改平台 `CorsConfig`，避免影响既有 8100 同源路径。
- 预检失败日志记录 origin，便于排障。

### 2.9.2 认证

- 登录接口沿用 `POST /api/auth/login`，返回 JWT；管理台存 `localStorage`。
- 所有新增端点显式 `@RequiresRole(1)`；`/api/model/*` 不设为 `@PublicApi`。
- SIIS 与平台共享 `JWT_SECRET` 约定，部署清单中检查。

### 2.9.3 SQL 安全

- 表名/列名来自 `TypeCatalog` 白名单；用户输入不得进入表名/列名。
- 所有值使用 `?` 参数化。
- 显式目标类型必须 `typeCatalog.require()`，未知类型直接拒绝；目标为空且原类型不在 9 类内时，按存量兼容落入通用表。

## **2.10 测试设计**

| 测试类 | 模块 | 覆盖 |
|--------|------|------|
| `OntologyModelEngineTest` | base | 加载成功/缺失/语法错误/裸块检测/双索引/描述符字段 |
| `SchemaValidatorTest` | base | 必填/类型/枚举/未知字段/部分更新/开关 |
| `ModelIntegrityCheckerTest` | base | 重复 alias/悬空引用/空属性/基线计数 |
| `TypeCatalogConsistencyTest` | siis app | 14 类 vs 种子；9 类晋升；名称列；bilateralRelation 专表 |
| `PromotionRouteConsistencyTest` | siis app | hotspot/book/paper 插入列；未知类型拒绝；幂等 |
| `ModelControllerTest`（可选） | flow | 401/403/404/200；响应不含敏感字段 |
| `AdminConsoleSmokeTest`（手工/可选） | app | 7 路由可打开；登录后可 CRUD |

测试口径：

- 平台存量 74 + 新增 ≥15；SIIS 存量 70 + 新增 ≥10。
- 任何映射漂移必须由 `TypeCatalogConsistencyTest` 失败暴露；不得靠人工记忆。
- H2 `MODE=MySQL` 下执行建表/晋升夹具；演示 SQL 不进入测试。

## **2.11 版本与构建治理**

1. 根 `pom.xml` 移除：

```xml
<module>dsh-ontolab-siis/dsh-ontolab-siis-app</module>
<module>dsh-ontolab-siis/dsh-ontolab-siis-plugin</module>
```

2. `dsh-onto-app/src/main/resources/application.yml`：

```yaml
dsh:
  app:
    version: 0.2.2
```

3. `CHANGELOG.md` 增加 0.2.2 静态本体交付项（引擎、校验、模型 API、管理台、晋升 9 类、演示数据、工程治理）。
4. 全量验证：

```bash
# 平台
mvn -q -DskipTests validate
mvn -q test
# SIIS
cd ../dsh-ontolab-siis && mvn -q test
```

## **2.12 风险、兼容与回滚**

| 风险 | 影响 | 缓解 |
|------|------|------|
| YAML 归位误删字段 | 数据模型缺字段 | 基线快照 + 测试；逐块迁移，不整段重写 |
| 重复 alias 清理不彻底 | 启动失败 | 完整性检查 fail-fast；先跑离线检查脚本 |
| 晋升写入失败（名称列） | 晋升不可用 | TypeCatalog 名称列 + 专项测试 |
| 双库事务不一致 | 目标表已写、状态未更新 | 幂等：先查 `ontology_entity_id`，再写入；失败可重试 |
| 前端跨端口 CORS | 实体/晋升页空白 | 白名单配置 + 页面降级提示 |
| 既有 144 测试被破坏 | 回归失败 | 校验用可选注入；不改既有断言 |
| 根 pom 未修 | 无法构建 | P0 前置任务，先修再开发 |
| 演示数据污染生产 | 数据事故 | 独立脚本 + 头部警示 + 非 Flyway |

**回滚策略**：

1. 代码层：引擎与 ModelController、CORS、前端可整体回退到 v0.2.1 tag；YAML 归位需保留原始文件副本于 `docs/0.2.2/backup/`（可选，不进入 classpath）。
2. 数据层：无 schema 变更；演示数据仅在 demo 库，删除脚本内标记的数据即可。
3. 配置层：`dsh.ontology.model.enabled=false` 可关闭引擎装配；`schema-validation.enabled=false` 可关闭校验；`dsh.ontology.model.project-required=false` 允许项目层缺失。

---

# **3. 动态本体规划对齐（0.2.3+，本期不交付）**

`dynamic-ontology-plan.md` 的 D1-D4 不在 v0.2.2 范围，但本方案需保证不挡路：

| 规划阶段 | 依赖本版本的接口 | 本期预留 |
|---------|----------------|---------|
| D1 规则引擎化 | `OntologyRegistry` 只读模型视图；`/api/model/*` 元数据 | 已预留 |
| D2 行为链事件化 | `TypeCatalog` 统一类型；M2 模型可解析 | 已预留（本期仅解析，不执行） |
| D3 流程引擎化 | M6 `nodeGraph` 元数据 | 已预留（本期不解释） |
| D4 Action 层 | 模型元数据 API + 既有 Agent 工具 | 已预留（不改协议） |

**明确禁止**：本期不实现规则解释器、事件总线、流程节点执行器、Action 网关；否则测试基线、权限模型与发布范围都会失控。动态计划中 `M3 12 条规则` 与 `M6 正式/轻量双轨` 的具体数量以未来版本评审为准，本版本文档不引用为交付承诺。

---

# **4. ADR 对齐与追溯**

| ADR | 结论 | 本版本动作 |
|-----|------|-----------|
| ADR-001 本体存储路线 A | 物理专表 + 类型注册表 | TypeCatalog + 14 类一致性测试；全元数据驱动列 0.2.3+ |
| ADR-002 向量检索 | 不引入 | 不新增依赖；仅保留 `personal_vector_index` 落库 |
| ADR-003 沙盘数字孪生 | Deferred | 不建表、不建端点 |
| ADR-004 报告双表 | 收敛到主表，阶段 2/3 暂缓 | 不改动 |
| ADR-005 Neo4j | 骨架就绪，生产暂缓 | 不扩展图算法/部署 |

---

# **5. 交付与验收**

1. **构建**：主仓库 `mvn -o -DskipTests validate` 通过；SIIS 仓库独立 `mvn test` 通过。
2. **引擎**：基座 + 项目层加载零错误；完整性报告无重复 alias/悬空引用；基线快照不下降。
3. **校验**：缺失必填、错误类型、非法枚举均被拒绝；未知字段默认告警。
4. **晋升**：9 类可晋升；hotspot/book/paper 写入正确名称列；bilateralRelation 不可晋升且映射正确。
5. **API**：4 端点 200/401/403/404 行为正确；响应无敏感信息。
6. **界面**：7 路由可打开；登录后总览/实体/搜索/i18n/晋升/AI 全链路可走通；401 自动跳登录。
7. **跨端口**：8100 调 8101 成功；白名单外 origin 被拒。
8. **数据**：演示脚本可重灌；总览计数与实体一致。
9. **治理**：版本号统一 `0.2.2`；CHANGELOG 含交付项；144 存量测试 + 新增测试全绿。
