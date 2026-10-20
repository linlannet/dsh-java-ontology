# 静态本体实施方案设计（评审稿 v1.0）

> 目标版本：0.2.2 ｜ 状态：待评审 ｜ 日期：2026-10-19
> 姊妹文档：`dynamic-ontology-plan.md`（动态本体规划，本文档的非目标部分）
> 依据：`ontology-architecture-comparison.md` 路线 E 第二步"本体运行时层"

## 1. 背景与目标

### 1.1 问题陈述

| # | 问题 | 证据 |
|---|------|------|
| P1 | 七模型 YAML"空转"，不参与运行时 | comparison.md:135-151（项目自证）；Java 代码对 `ontology-models/` 零引用 |
| P2 | 管理前端仅 128 行且不可用：无登录流，API 调用全部 401 | `dsh-onto-app/src/main/resources/static/`（index.html 42 行 + app.js 68 行）；RoleInterceptor 仅放行 `@PublicApi` 的 5 个端点，`/api/ontologies` 要求 `@RequiresRole(1)` |
| P3 | 本体业务表全空，"本体库"有名无实 | V1.0.1 种子仅 24 行系统/元数据，`onto_ontology_version.entity_count=0` |
| P4 | 晋升路由缺口：`TYPE_TO_TABLE` 仅 6 类，M1 定义 8 类 | `OntologyPromotionService.java:47-54`（book/paper/hotspot 缺失，落入通用表） |
| P5 | YAML 本身不可机器解析：基座 m1 三域裸属性块；项目层 siis-object.yaml attributes 全空 | m1-object-model.yaml L78 起；siis-object.yaml L9-72 |

### 1.2 目标（G1-G5）

- **G1** YAML 引擎化：七模型从设计文档变为运行时引擎（加载 → 注册表 → Schema 校验）
- **G2** 管理界面：原生 HTML 管理台把本体库用起来（登录/浏览/维护/搜索/多语种/晋升）
- **G3** 晋升路由补齐至 9 类，与 `onto_ontology_type` 注册表、`OntologyMatchService` 三方一致
- **G4** 演示数据：中东/东南亚监测场景下国家 30+/组织 50+/人物 30+（四语种），效果可感知
- **G5** 全程零新依赖（snakeyaml 2.4 由 spring-boot-starter-web 传递提供，已验证依赖树）

### 1.3 非目标

- 动态本体四组件引擎化（M2 行为链/M3 规则引擎/M6 流程解释器）→ 见 `dynamic-ontology-plan.md`
- Neo4j 图层深化（SIIS 已有 `GraphSyncService` 可选双存储，ADR-005）
- MU 模型 14 菜单全量实现（本期聚焦静态本体 7 视图，其余视图列入后续版本）

## 2. 现状盘点

### 2.1 可复用资产

| 资产 | 位置 | 状态 |
|------|------|------|
| 七模型 YAML（基座） | `dsh-onto-base/.../ontology-models/`（m1~m7+mu+manifest） | 部分可解析（共享层 19 聚合根完整） |
| 七模型 YAML（siis 项目层） | `dsh-ontolab-siis-app/.../siis-models/`（7 文件+manifest，引用 base_manifest） | 骨架完整，attributes 全空 |
| 类型注册表 | `onto_ontology_type` 表，种子 14 类型→物理表映射（ADR-001 路线 A） | ✅ 可直接复用 |
| 元数据 CRUD | `OntologyMetaRepository`（base）6 类动态 CRUD + i18n upsert | ✅ |
| 匹配引擎 | `OntologyMatchService` 四级瀑布（主表精确→模糊→i18n 精确→模糊，跨库 JOIN） | ✅ |
| 业务 API | siis `OntologyCrudController` 18 端点；flow `GenericCrudController` 12 端点；`OntologyController` 7 端点 | ✅ |
| AI 对话 | `HarnessAiService`（同步）+ `HarnessAiStreamService`（SSE 流式） | ✅ 前端已接入 |
| 测试基线 | 144 个存量测试（H2 + Flyway，26 文件） | 回归门槛 |

### 2.2 缺口（本方案要解决的）

即 1.1 的 P1-P5，另加一个**高风险项**：

> ✅ **R0（已于 2026-10-19 解决）**：SIIS 子项目已从主仓库迁出为**独立仓库 `dsh-ontolab-siis`**（含 `dsh-ontolab-siis-app` / `dsh-ontolab-siis-plugin` / `docs`），运行时标识同步更名（插件 id、应用名、数据目录、工具前缀均为 `dsh-ontolab-siis`）。主仓库 `.gitignore` 改为忽略该独立仓库目录，旧 `dsh-ontoext-siis/` 目录已删除。

## 3. 总体架构

```text
┌────────────────────────────────────────────────────────────────┐
│  管理界面（dsh-onto-app static/，原生 HTML+JS，端口 8100）        │
│  登录 │ 总览 │ 实体维护(表单schema驱动) │ 搜索 │ i18n │ 晋升 │ AI │
└──────┬─────────────────────────────────────────────┬───────────┘
       │ JWT Bearer                                   │ CORS(8101)
┌──────▼───────────────────────┐        ┌─────────────▼───────────┐
│ 平台 API（flow，8100）        │        │ SIIS API（siis-app，8101）│
│ /api/auth /api/ontologies    │        │ /api/ontology/*（18 端点）│
│ /api/workspace/* /api/model/*│        │ 匹配/晋升/i18n           │
└──────┬───────────────────────┘        └─────────────┬───────────┘
       │ 校验挂载                                      │ 校验挂载
┌──────▼──────────────────────────────────────────────▼───────────┐
│  OntologyModelEngine（dsh-onto-base 新包 ontology.engine）        │
│  ModelLoader(snakeyaml) → OntologyRegistry → SchemaValidator    │
│  输入：ontology-models/*.yaml（基座）+ siis-models/*.yaml（项目） │
└──────┬──────────────────────────────────────────────────────────┘
       │ 对齐
┌──────▼──────────────────────────────────────────────────────────┐
│  存储层：onto_ontology_type（14 类型注册表）│ gwy_ontology_*（8 专表）│
│  onto_ontology_{entity,term,location,...}（6 元数据表）           │
└─────────────────────────────────────────────────────────────────┘
```

模块归属依据 README 分层约定：base=静态本体定义+逻辑（引擎）；flow=编排（API）；app=聚合部署（界面）。

## 4. 详细设计

### 4.1 YAML 规范化（前置条件）

**修复规范**（一个实体节点的完整结构）：

```yaml
- name: 国家                      # 必填，中文名
  alias: OntologyCountry          # 必填，Java/路由用别名，全局唯一
  description: 国家信息库          # 必填
  localId: id                     # 本地主键字段名
  cardinality: ZERO_OR_MORE       # 子实体基数（聚合根可省略）
  cascadeDelete: true             # 级联删除（可省略，默认 false）
  tags: [本体域, 共享层]           # 必填，用于域过滤
  attributes:                     # 必填（可为空列表）
    - { name: isoCode, label: ISO代码, type: String, required: false, unique: true }
```

修复范围与工作量：

| 文件 | 问题 | 修复动作 |
|------|------|---------|
| m1-object-model.yaml（725 行） | track/analyze/display 三域约 25 个实体为裸属性块（无实体头） | 按上表规范补齐 `name/alias/localId/cardinality`，属性块归位；目标：52 实体全部可解析 |
| siis-object.yaml（288 行） | 8 个本体域聚合根 attributes 为空 | 从 m1 中对应实体属性 + DDL 字段回填（country/organization/person/hotspot/media/book/paper/bilateralRelation 全字段） |
| 其余 12 个 YAML | 格式完整 | 仅通过完整性校验，不动内容 |

**产出配套**：`ModelIntegrityChecker`（随引擎提供，见 4.2），启动时输出报告：实体总数、字段总数、别名重复、悬空引用（m7 的 objectRef/m2 的 ownerEntity 指向不存在的聚合根）。

### 4.2 OntologyModelEngine（base 模块，新包 `net.linlan.dsh.ontology.engine`）

| 组件 | 职责 | 要点 |
|------|------|------|
| `ModelLoader` | 解析 manifest + 七模型 YAML | snakeyaml `new Yaml()`；基座路径 `classpath:ontology-models/`，项目层 `classpath:siis-models/`（存在才加载，app 不含 siis-models 时只挂基座）；`base_manifest` 相对引用解析 |
| `OntologyRegistry` | 运行时注册表 | 双索引：`alias → AggregateDescriptor`、`category → AggregateDescriptor`；只读，启动时一次加载；提供 `aggregates()/byAlias()/byCategory()/attributeOf(alias)` 查询 |
| `AggregateDescriptor` / `AttributeDescriptor` | 元信息 record | 聚合根：id/name/alias/lifecycle/tags/attributes/子实体列表；字段：name/label/type(String/Long/Integer/Float/Boolean/Enum/Text/DateTime/Json/Blob)/required/unique/enumValues/defaultValue/description |
| `SchemaValidator` | 写操作校验 | `validate(category, Map<String,Object> payload)`：必填缺失/类型不匹配/枚举值非法/未知字段（警告级），返回字段级错误列表 |
| `TypeCatalog` | 常量：category→物理表 | 与 `onto_ontology_type` 种子 14 类型完全一致（country/organization/person/hotspot/media/book/paper → gwy_ 表；term/eventType/location/entity/version/i18n/bilateralRelation → onto_ 表） |

**设计决策**：
- 双事实源问题（TypeCatalog 常量 vs onto_ontology_type 表）用**一致性测试**锁定：`TypeCatalogConsistencyTest` 启动 H2 后比对两者，漂移即测试失败
- 引擎不依赖 Spring Data/JPA，纯 POJO + snakeyaml，保持 base 零业务依赖

### 4.3 Schema 校验挂载

| 挂载点 | 位置 | 时机 |
|--------|------|------|
| flow 工作台 | `GenericCrudController` create/update | 写库前 `SchemaValidator.validate()`，失败返回 400 + 字段明细 |
| siis 本体域 | `OntologyCrudController` create/update | 同上 |

- 开关：`dsh.ontology.schema-validation.enabled`（默认 true），异常时可通过配置回退为仅日志告警
- 只校验 `TypeCatalog` 覆盖的 category，其余 category 透传（不影响存量）

### 4.4 晋升路由补齐（G3）

`OntologyPromotionService.TYPE_TO_TABLE` 从 6 类扩到 9 类（新增 hotspot/book/paper）：

```java
"country"      → "gwy_ontology_country",
"organization" → "gwy_ontology_organization",
"person"       → "gwy_ontology_person",
"hotspot"      → "gwy_ontology_hotspot",   // 新增
"media"        → "gwy_ontology_media",
"book"         → "gwy_ontology_book",      // 新增
"paper"        → "gwy_ontology_paper",     // 新增
"location"     → "onto_ontology_location",
"term"         → "onto_ontology_term"
```

一致性保障：`PromotionRouteConsistencyTest` 断言 `TYPE_TO_TABLE` ⊆ `TypeCatalog` ⊆ `onto_ontology_type` 种子，三方漂移即失败。

**同步补齐**：`OntologyMatchService.java:52` 另有 `ENTITY_TYPE_TO_TABLE`（与 `TYPE_TO_TABLE` 同份 6 类，重复代码），必须一并扩到 9 类——否则匹配引擎对 hotspot/book/paper 永远 miss，晋升工作台"匹配预览"失效。建议顺带抽取为 `TypeCatalog.promotionTargets()` 共享常量（与 4.2 TypeCatalog 统一），消除两处重复。

### 4.5 管理界面（G2，原生 HTML+JS，符合轻量原则）

**视图清单（7 个）**：

| 视图 | 路由（hash） | 功能 | 对接 API |
|------|-------------|------|---------|
| 登录 | `#/login` | 账号密码 → JWT（localStorage），修复 P2 401 缺陷 | `POST /api/auth/login` |
| 总览 | `#/overview` | 模型树（M1 聚合根按域分组）+ 各 category 实体计数 + 引擎完整性报告 | `GET /api/model/*`、`GET /api/workspace/{category}/count` |
| 实体维护 | `#/entities/{category}` | 列表+分页+CRUD 表单；**表单字段由 `/api/model/aggregates/{alias}` 动态渲染**（M1 引擎的直接应用） | `GET/POST/PUT/DELETE /api/workspace/{category}*`（onto 库）；`/api/ontology/{category}*`（gwy 库，8 类） |
| 本体搜索 | `#/search` | 关键词跨语种搜索（主表+i18n） | `GET /api/ontology/search`、`/api/ontology/i18n/search` |
| 多语种 | `#/i18n/{category}/{id}` | 四语种翻译维护（en/fr/ar 必备，ru/es/ja/de 可选） | `GET/POST/DELETE /api/ontology/i18n/*` |
| 晋升工作台 | `#/promote` | 发现实体列表（DISCOVERED）→ 一键匹配预览 → 晋升（含 8 类目标类型选择） | `GET /api/ontology/match`、`POST /api/ontology/entity/promote` |
| AI 对话 | `#/ai` | 保留现有同步+SSE 流式双模式 | `POST /api/ontologies/ai[/stream]` |

**工程要点**：
- 单文件页面 + hash 路由（延续现有 index.html 风格，无框架、无构建步骤）
- 统一 `apiFetch()` 封装：自动携带 `Authorization: Bearer`、401 跳登录、403 提示角色不足
- 深色主题对齐 `mu-ui-model.yaml` theme.colors（bgGlobal #0f1419 / primaryBlue #3b82f6 等），侧边栏 6 项导航
- 表单渲染器：`renderForm(schema, data)` 约 120 行通用函数（input/select/textarea 按字段 type/enumValues 生成）

**跨端口（决策点 RP-2）**：app(8100) 页面调用 siis-app(8101) API 需 CORS。推荐方案：siis-app 增加允许 `http://127.0.0.1:8100` 的 CORS 配置（origin 白名单，可配置项 `dsh.siis.cors.allowed-origins`）。备选：Nginx 反代合并（部署期再定，不影响开发）。

### 4.6 演示数据规格（G4）

**交付形式（决策点 RP-3，推荐方案 A）**：独立 SQL 脚本，放 `docs/dev-ops/demo/`：
- `demo_ontology_onto.sql`（onto 库：i18n 翻译、version 计数回填、发现实体样本）
- `demo_ontology_siis.sql`（siis 库：gwy_ontology_* 8 表业务数据）

理由：i18n 的 `entity_id` 跨库依赖 gwy 表插入后的自增 ID，Flyway 双应用迁移顺序不可控；演示数据需要"可重灌"（TRUNCATE+重插），不宜混入 schema 演进链。脚本头部提供执行顺序说明（先 siis 后 onto）。

**数据内容清单**：

| 表 | 数量 | 内容说明 |
|----|------|---------|
| gwy_ontology_country | 32 | 中东 16（沙特/伊朗/埃及/阿联酋/卡塔尔/约旦/以色列/土耳其/伊拉克/叙利亚/黎巴嫩/科威特/巴林/阿曼/也门/巴勒斯坦）+ 东南亚 8（印尼/马来西亚/越南/泰国/新加坡/菲律宾/缅甸/柬埔寨）+ 大国 8（中/美/俄/英/法/德/日/印）；含 iso_code/continent/capital/language/currency |
| gwy_ontology_organization | 52 | 国际组织（UN/阿盟/GCC/OPEC/IAEA…）+ 各国政府机构（外交部/国防部级）+ NGO（红十字/无国界医生…）+ 大学（开罗大学/新加坡国立…）+ 企业；org_type 五类均衡 |
| gwy_ontology_person | 34 | 各国政要/外交官/学者/记者；仅客观字段（姓名/性别/国籍/职业/头衔/所属机构），bio 用公开履历，portraitUrl 留空 |
| gwy_ontology_media | 22 | Al Jazeera/Arab News/Janne Afrique/The Star…按 newspaper/magazine/tv/radio/web/news_agency 六类型；**biasRating 一律留空**（中立性原则） |
| gwy_ontology_hotspot | 6 | 红海航运/加沙局势/南海态势/缅甸局势/伊朗核问题/萨赫勒地带；含 keywords JSON/heatScore/riskLevel |
| gwy_ontology_bilateral_relation | 20 | 中-美/中-沙/美-伊/俄-乌/法-德 等；relationType/relationLevel 客观描述 |
| onto_ontology_i18n | ≥600 | 上述实体中重点对象（国家 32+组织 52+人物 34+媒体 22=140 个）的 en/fr/ar 三语种 name；唯一键 (entity_id, category, lang) |
| onto_common_discovered_entity | 12 | **刻意不含于本体的样本**（如新兴武装组织/小众新闻站/新任官员变动的机构名），status=DISCOVERED，供晋升工作台演示 |
| onto_ontology_term | 20 | 地缘术语（主权/制裁/斡旋/走廊/飞地…）含 definition |
| onto_ontology_location | 15 | 曼德海峡/霍尔木兹海峡/苏伊士运河/马六甲海峡 等战略地点，含 loc_type/parent 关系 |

**中立性原则**：涉及立场字段（biasRating/风险判断描述）留空或采用客观中性表述；人物仅收录广为流传的公开事实。

### 4.7 顺带修复

- P2 401 缺陷随登录视图解决
- `onto_ontology_version.entity_count` 演示脚本回填真实计数
- ~~建议（RP-1）~~ **已完成**：SIIS 已迁为独立仓库 `dsh-ontolab-siis` 并在其中纳管版本控制

## 5. API 设计（新增）

`ModelController`（flow，`/api/model/*`，`@RequiresRole(1)`）：

| 方法 | 路径 | 返回 | 说明 |
|------|------|------|------|
| GET | `/api/model/manifest` | 基座+项目层模型清单、版本、domain | 来自 manifest 解析 |
| GET | `/api/model/aggregates` | 聚合根树（按域分组，含实体/字段计数） | Registry 全量投影 |
| GET | `/api/model/aggregates/{alias}` | 单聚合根完整 schema（attributes/lifecycle/子实体） | 表单渲染数据源 |
| GET | `/api/model/integrity` | 完整性报告（实体数/字段数/别名重复/悬空引用） | ModelIntegrityChecker 输出 |

复用既有 API：`/api/auth/login`、`/api/ontologies*`（7）、`/api/workspace/*`（12）、siis `/api/ontology/*`（18）。

## 6. 实施计划

| 阶段 | 内容 | 交付物 | 预估 | 验收标准 |
|------|------|--------|------|---------|
| S1 | YAML 规范化 | m1 修复 + siis-object.yaml 回填 | 2-3 人日 | 引擎解析 52 实体零错误；m2/m3/m5/m6/m7 的引用无悬空 |
| S2 | 引擎 + Model API | base engine 包 + flow ModelController | 4-5 人日 | `/api/model/*` 4 端点可用；TypeCatalog 一致性测试绿 |
| S3 | 校验挂载 + 晋升补齐 | GenericCrudController/OntologyCrudController 校验；TYPE_TO_TABLE 8 类 | 2 人日 | 必填/枚举非法请求 400；晋升路由三方一致性测试绿 |
| S4 | 管理界面 | 7 视图 + 登录流 + CORS | 5-7 人日 | E2E：登录→浏览→建实体（schema 表单）→搜索→i18n→晋升→AI 对话 |
| S5 | 演示数据 + 联调 | docs/dev-ops/demo/ 双脚本 + README 更新 | 2-3 人日 | 数据重灌幂等；144 存量测试全绿 + 新增测试全绿 |

合计 **15-20 人日**，建议作为 **0.2.2** 版本发布（CHANGELOG 增量记录）。

## 7. 测试策略

- **base**：`OntologyModelEngineTest`——解析完整性（52 实体/字段计数快照）、别名唯一、schema 校验正反用例（合法 payload 通过；缺必填/错类型/非法枚举分别拒绝）
- **app**：`ModelControllerTest`（4 端点）、`SchemaValidationIntegrationTest`（经 `/api/workspace` 写入触发校验）、界面 E2E 冒烟（登录 token 流转）
- **siis-app**：`PromotionRouteTest`（8 类型全路由断言，含 book/paper 新增）、`TypeCatalogConsistencyTest`
- **回归门槛**：`mvn test` 存量 144 测试 + SIIS 侧测试全部保持绿色

## 8. 风险与决策点（请评审人重点裁决）

| 编号 | 决策点 | 推荐 | 备选 |
|------|--------|------|------|
| RP-1 | ~~SIIS 是否纳入 git~~ **已解决** | ✅ 2026-10-19 迁为独立仓库 `dsh-ontolab-siis` | — |
| RP-2 | 跨端口方案 | siis-app 增加 CORS 白名单（配置项） | Nginx 反代 / 界面拆两套 |
| RP-3 | 演示数据交付形式 | 独立 SQL 双脚本（docs/dev-ops/demo/，可重灌） | Flyway 迁移（顺序不可控，不推荐）/ @Profile("demo") Runner |
| RP-4 | M1 修复可能引起 m7/mu 引用漂移 | 修复时同步核对 objectRef/screenRef 引用，IntegrityChecker 兜底 | 仅修不查（不推荐） |
| RP-5 | Schema 校验对存量调用方的影响 | 默认开启 + 配置开关可关 + 未知字段仅告警 | 仅日志模式观察一周再开启 |
| RP-6 | siis-object.yaml attributes 回填的字段来源 | 以 DDL 为准（m1 同步对齐） | 以 m1 为准（DDL 可能更全） |

## 9. 评审清单

1. 模块归属（引擎进 base）是否符合分层预期？
2. 界面 7 视图范围是否合适？是否需要在第一期加入"图谱可视化"（建议二期）？
3. 演示数据规模（国家 32/组织 52/人物 34/媒体 22）与语种范围（en/fr/ar 三语）是否满足演示诉求？
4. RP-1 至 RP-6 六个决策点的裁决。
5. 0.2.2 版本号与 15-20 人日排期是否可接受？
