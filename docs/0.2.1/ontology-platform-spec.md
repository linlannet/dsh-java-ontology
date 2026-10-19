# 多语种国际问题研究平台 — 产品层需求规格 v0.2.1

> **版本**：v0.2.1 ｜ **日期**：2026-10-18 ｜ **状态**：已实施（P0 修复全部完成）
>
> **定位**：覆盖 `dsh_java_ontodb` 产品层共享数据库 26 表（已建）+ 6 表（设计中）+ 2 视图的平台级共享能力 EARS 需求规格。SIIS 项目层（`dsh_java_siisdb` 36 表）需求见 `siis-platform-spec.md`。
>
> **关联文档**：`ontology-platform-tasks.md`、`ontology-platform-design.md`、`ONTO_DB_DESIGN.md`、`ADR-001-ontology-storage-paradigm.md`
>
> - **需求编号体系**：`PLT-{域}{编号}-{优先级}`，如 `PLT-O1-P0`
> - **EARS 类型**：Ubiquitous / Event-driven / State-driven / Optional / Unwanted
> - **优先级**：P0 平台必需 / P1 平台增强 / P2 生态扩展
> - **域编号**：O=ontology, C=common, G=global, R=report, S=system, P=P0修复, N=非功能

---

# 1. 平台概述

## 1.1 平台定位

构建**跨项目复用的产品层共享本体与知识能力平台**，基于 dsh-java Harness Agent/Skill/Tool 架构与七模型 YAML 驱动，为 siis/factory/ecommerce/city-ops 等项目层提供统一的本体注册、知识汇聚、报告与权限基座。

## 1.2 双数据库架构

| 数据库 | 角色 | 表数 | 前缀 | 复用范围 |
|--------|------|------|------|---------|
| `dsh_java_ontodb` | 产品层共享 | 26（已建）+ 6（设计中）+ 2 视图 | `onto_*` | siis/factory/ecommerce/city-ops |
| `dsh_java_siisdb` | SIIS 项目层 | 36 | `gwy_*` | siis 专属 |

总表数（含 SIIS）：62（26 + 36）。

## 1.3 onto_ 表族分布（dsh_java_ontodb 26 表已建）

| 表族 | 表数 | 表名 |
|------|------|------|
| `onto_sys_` | 1 | onto_sys_user |
| `onto_common_` | 4 | onto_common_discovered_entity, onto_common_tenant_relation_asset, onto_common_personal_vector_index, onto_common_llm_chat_history |
| `onto_global_` | 5 | onto_global_discovered_entity, onto_global_bilateral_relation, onto_global_kg_node, onto_global_kg_edge, onto_global_aggregation_log |
| `onto_report_` | 7 | onto_report_rpt_template, onto_report_rpt_report, onto_report_alert_record, onto_report_information_publish, onto_report_push_record, onto_report_recommendation_record, onto_report_search_index |
| `onto_ontology_` | 9 | onto_ontology_version, onto_ontology_entity, onto_ontology_term, onto_ontology_event_type, onto_ontology_location, onto_ontology_i18n, onto_ontology_type, onto_ontology_attribute, onto_ontology_relation |

设计中 6 表（`onto_ingestion_*`）暂未实施，标记为规划中（见 PLT-P4-P0-1）。

2 视图：`v_unified_monitor`（数据库视图）、`v_unified_kg`（已改用 AggregationService 应用层聚合，见 PLT-N6-P0-2）。

## 1.4 三级知识架构

| 层级 | 数据 | 表族 | 聚合方式 |
|------|------|------|---------|
| 个人层 | 租户私有（tenant_id 隔离）| `onto_common_*`（4 表）| 物理隔离 |
| 共享层 | 跨租户共享本体与汇聚 | `onto_ontology_*`（9 表）+ `onto_global_*`（5 表）| 物理表 |
| 统一层 | 跨租户聚合查询 | AggregationService 应用层聚合 | 应用层（替代 `v_unified_kg` 视图）|

---

# 2. 需求

## 2.1 域 O — ontology 本体注册（共享层，9 实体）

### PLT-O1-P0-1（Ubiquitous）本体实体 CRUD
The system shall provide CRUD endpoints for all 9 onto_ontology_* entities (onto_ontology_version, onto_ontology_entity, onto_ontology_term, onto_ontology_event_type, onto_ontology_location, onto_ontology_i18n, onto_ontology_type, onto_ontology_attribute, onto_ontology_relation) at `/api/ontology/{entity}/*`, accessible to all authenticated users for read operations and SUPER_ADMIN for write operations.

### PLT-O1-P0-2（Ubiquitous）本体共享可见性
The system shall designate all 9 onto_ontology_* tables as shared-layer data, not filtered by tenant_id, accessible to all tenants for read operations.

### PLT-O2-P0-1（Ubiquitous）多业务方向 domain 列
The system shall add a `domain` column to onto_ontology_entity, onto_ontology_term, onto_ontology_event_type, and onto_ontology_relation, accepting values from {siis, factory, ecommerce, city-ops, common}, enabling multi-business-direction ontology partitioning and filtering via `GET /api/ontology/entity?domain=`.

### PLT-O2-P0-2（Ubiquitous）本体综合查询
The system shall provide `POST /api/ontology/query` that accepts a natural language query and searches across all 9 onto_ontology_* entity types, returning matched entities with type, domain, and relevance score.

### PLT-O3-P0-1（Ubiquitous）本体关键词搜索
The system shall provide `GET /api/ontology/search?keywords=&domain=` that performs keyword search across all onto_ontology_* entities, supporting multi-language terms (zh/en/fr/ar) and optional domain filtering.

### PLT-O3-P0-2（Ubiquitous）i18n 多语种术语解析
The system shall resolve entity display names via onto_ontology_i18n by (entity_id, locale), falling back to the default locale when the requested locale is missing.

### PLT-O4-P0-1（Ubiquitous）本体实体对齐
The system shall provide an `OntologyMatchService.match(entityName, entityType, domain)` that attempts to match a given entity name against the ontology knowledge base within the specified domain, returning the matched onto_ontology_entity ID or null.

### PLT-O4-P0-2（Event-driven）发现实体升级
When a discovered entity's mentionCount exceeds the threshold (default 5), the system shall support promoting it to the shared ontology via `POST /api/ontology/entity/promote`, creating an onto_ontology_entity record and linking all related KgNodes.

### PLT-O5-P0-1（Ubiquitous）本体类型元数据注册表
The system shall use onto_ontology_type as the metadata registry for all ontology entity types (per ADR-001 route A), recording type code, display name, schema, and domain for each entity type.

### PLT-O5-P0-2（Ubiquitous）本体属性元数据
The system shall use onto_ontology_attribute to register attribute metadata (attribute code, data type, i18n label, validation rule) for each onto_ontology_type, decoupling attribute definitions from physical schema.

### PLT-O6-P0-1（Ubiquitous）本体关系元数据
The system shall use onto_ontology_relation to register relation metadata (relation code, source type, target type, direction, i18n label) for typed bilateral relations across ontology entities.

### PLT-O6-P0-2（Ubiquitous）本体版本管理
The system shall track ontology versions via onto_ontology_version table, recording each bulk import/update with version number, domain, change description, and timestamp.

### PLT-O7-P1-1（Ubiquitous）事件类型与地点本体
The system shall provide CRUD for onto_ontology_event_type and onto_ontology_location, supporting domain-scoped event taxonomies and hierarchical location registries (country/region/city).

---

## 2.2 域 C — common 个人层知识沉淀（个人层，4 实体）

### PLT-C1-P0-1（Ubiquitous）common 实体 CRUD
The system shall provide CRUD endpoints for all 4 onto_common_* entities (onto_common_discovered_entity, onto_common_tenant_relation_asset, onto_common_personal_vector_index, onto_common_llm_chat_history) at `/api/common/{entity}/*`, with automatic tenant_id isolation.

### PLT-C1-P0-2（Event-driven）新实体自动沉淀
When a KgNode is created during analysis and its ontologyEntityId is null, the system shall store it as a tenant-private discovered entity in onto_common_discovered_entity.

### PLT-C2-P0-1（Event-driven）关系自动沉淀
When a BilateralRelation is computed, the system shall upsert an onto_common_tenant_relation_asset record, accumulating the tenant's relationship knowledge across topics.

### PLT-C2-P0-2（Ubiquitous）工作台概览
The system shall provide `GET /api/common/overview` returning the current tenant's workspace summary: {totalTopics, totalArticles, totalReports, totalAlerts, recentTopics[], recentReports[]}.

### PLT-C3-P0-1（Ubiquitous）知识资产查询
The system shall provide `GET /api/common/knowledge` returning the tenant's accumulated knowledge assets: {discoveredEntities[], accumulatedRelations[], topicHistory[], entityFrequency[]}.

### PLT-C3-P0-2（Ubiquitous）向量索引管理
The system shall manage onto_common_personal_vector_index per tenant for personal embedding indexes, supporting create/rebuild/delete operations scoped by tenant_id.

### PLT-C4-P1-1（Ubiquitous）AI 对话历史
The system shall store all AI Agent conversations in onto_common_llm_chat_history with session_id, agent_name, role, and content for audit and context recovery.

---

## 2.3 域 G — global 知识汇聚（共享层，5 实体 + 应用层聚合）

### PLT-G1-P0-1（Ubiquitous）汇聚表 CRUD
The system shall provide CRUD endpoints for all 5 onto_global_* entities (onto_global_discovered_entity, onto_global_bilateral_relation, onto_global_kg_node, onto_global_kg_edge, onto_global_aggregation_log) at `/api/global/{entity}/*`, with read access for all researchers and write access for SUPER_ADMIN only.

### PLT-G1-P0-2（Ubiquitous）全局图谱邻居查询
The system shall provide `GET /api/global/kgNode/{id}/neighbors?depth=` for querying the global knowledge graph.

### PLT-G2-P0-1（Ubiquitous）AggregationService 应用层聚合
The system shall implement an `AggregationService` that aggregates cross-tenant data from onto_common_* and onto_ontology_* into onto_global_* via application-layer joins, replacing the v_unified_kg database view (per P0 fix #3).

### PLT-G2-P0-2（Ubiquitous）定时汇聚
The system shall run a scheduled aggregation job (default: hourly) that merges all tenant-private data into onto_global_* tables and logs the operation in onto_global_aggregation_log.

### PLT-G2-P0-3（Event-driven）事件触发增量汇聚
When a tenant completes the analysis pipeline, the system shall trigger an incremental aggregation merging only newly produced data.

### PLT-G3-P0-1（State-driven）汇聚去重
While aggregating, the system shall deduplicate entities by normalized name + type + domain, summing mentionCount and merging tenantId lists.

### PLT-G3-P0-2（Ubiquitous）手动汇聚
The system shall provide `POST /api/global/aggregate` for SUPER_ADMIN to manually trigger full or incremental aggregation.

### PLT-G4-P0-1（Ubiquitous）汇聚日志查询
The system shall provide `GET /api/global/aggregate/log` for querying aggregation history.

---

## 2.4 域 R — report 报告与展示（共享层，7 实体）

### PLT-R1-P0-1（Ubiquitous）report 实体 CRUD
The system shall provide CRUD endpoints for all 7 onto_report_* entities (onto_report_rpt_template, onto_report_rpt_report, onto_report_alert_record, onto_report_information_publish, onto_report_push_record, onto_report_recommendation_record, onto_report_search_index) at `/api/report/{entity}/*`, with automatic tenant_id isolation for personal reports and shared access for templates.

### PLT-R1-P0-2（Ubiquitous）报告生成
The system shall provide `POST /api/report/rptReport/generate` that accepts {topicId, templateId?, lang?, scope?} and generates an analysis report. When scope=GLOBAL, the report uses cross-tenant aggregated data (requires SUPER_ADMIN).

### PLT-R2-P0-1（Ubiquitous）报告分节查询
The system shall provide `GET /api/report/rptReport/{id}/sections` returning the report's 7 sections (overview, findings, relations, trends, risks, recommendations, appendix).

### PLT-R2-P0-2（Ubiquitous）告警过滤查询
The system shall provide `GET /api/report/alertRecord?topicId=&level=` for filtering alerts by topic and severity level.

### PLT-R3-P0-1（Ubiquitous）信息发布
The system shall provide `POST /api/report/informationPublish/{id}/publish` for publishing analyzed information.

### PLT-R3-P0-2（Ubiquitous）搜索索引重建
The system shall provide `POST /api/report/searchIndex/rebuild` for rebuilding the search index.

### PLT-R4-P0-1（Ubiquitous）个性化推荐
The system shall provide `GET /api/report/recommendation?userId=` returning personalized recommendations based on user's research history and preferences.

### PLT-R4-P0-2（Ubiquitous）推送记录审计
The system shall record all report/alert push events in onto_report_push_record with channel, recipient, payload, and delivery status for audit.

---

## 2.5 域 S — system 系统管理（1 实体 + JWT + fail-closed 权限）

### PLT-S1-P0-1（Ubiquitous）用户登录
The system shall provide `POST /api/auth/login` that validates credentials against onto_sys_user, returns a JWT token containing {userId, tenantId, roleLevel, roleName}, and establishes the tenant context.

### PLT-S1-P0-2（Ubiquitous）用户注册
The system shall provide `POST /api/auth/register` for creating new researcher accounts with a default tenant_id and role_level=1 (RESEARCHER).

### PLT-S2-P0-1（Ubiquitous）角色定义
The system shall define three roles: SUPER_ADMIN (role_level=0, cross-tenant), RESEARCHER (role_level=1, tenant-private + shared read), GUEST (role_level=2, shared read-only).

### PLT-S2-P0-2（State-driven）租户数据隔离
While a request carries a valid JWT with tenantId, the system shall automatically filter all personal-layer queries by the tenant's tenantId, preventing cross-tenant data access.

### PLT-S3-P0-1（Ubiquitous）fail-closed 权限拦截
The system shall enforce fail-closed authorization via @PublicApi + @RequiresRole annotations and a RoleInterceptor, where any endpoint not explicitly annotated with @PublicApi requires an authenticated JWT and an @RequiresRole role check; missing annotations default to denial (per P0 fix #1).

### PLT-S3-P0-2（Ubiquitous）用户管理
The system shall provide user management endpoints (list/create/update/delete/role-change) at `/api/auth/user/*`, accessible to SUPER_ADMIN only.

---

## 2.6 域 P — P0 修复（2026-10-18 全部完成）

### PLT-P1-P0-1（Ubiquitous）权限 fail-closed 全覆盖
The system shall enforce fail-closed authorization on all 123 API endpoints via @PublicApi + @RequiresRole + RoleInterceptor, where 121 endpoints carry @RequiresRole and 2 endpoints carry @PublicApi, achieving 123/123 endpoint coverage (per P0 fix #1, completed 2026-10-18).

### PLT-P2-P0-1（Ubiquitous）列名注入防护
The system shall defend against column-name injection via WRITABLE_FIELDS whitelist per entity and a sanitizeRows filter that rejects any field not in the whitelist, applied to all dynamic-order/dynamic-field query endpoints (per P0 fix #2).

### PLT-P3-P0-1（Ubiquitous）共享层唯一化与双库分离
The system shall separate shared-layer data into dsh_java_ontodb (onto_* prefix) and project-layer data into dsh_java_siisdb (gwy_* prefix), eliminating cross-database join ambiguity and ensuring the shared layer is uniquely owned by the ontology platform (per P0 fix #3).

### PLT-P4-P0-1（State-driven）ingestion 降级为规划中
The system shall mark all onto_ingestion_* tables (6 tables) as planned/not-yet-implemented, gated by @ConditionalOnProperty, and exclude them from the runtime schema initialization until formally promoted (per P0 fix #4).

### PLT-P5-P0-1（Ubiquitous）ADR-001 存储范式路线 A
The system shall adopt ADR-001 route A (specialized tables + onto_ontology_type metadata registry) for ontology storage, where each ontology category has a dedicated physical table and onto_ontology_type/onto_ontology_attribute/onto_ontology_relation serve as the metadata registry (per P0 fix #5).

### PLT-P6-P0-1（Ubiquitous）API contract 代码反向生成
The system shall derive the API contract (123 endpoints) from the controller code via reverse generation, keeping the OpenAPI/contract artifact synchronized with the implementation rather than authored manually (per P0 fix #6).

---

## 2.7 域 N — 非功能性需求

### PLT-N1-P0-1（Ubiquitous）双数据库
The system shall use MySQL 8.0 as the persistence layer with two physical databases: dsh_java_ontodb (product-layer shared, onto_* prefix, 26 tables + 2 views) and dsh_java_siisdb (SIIS project-layer, gwy_* prefix, 36 tables), each managed by a separate HikariCP connection pool.

### PLT-N1-P0-2（Ubiquitous）DualDataSourceConfig
The system shall configure a `DualDataSourceConfig` that provisions two DataSource beans (ontodbDataSource, siisdbDataSource) with corresponding JdbcTemplate beans, and routes persistence operations by prefix/bean qualifier.

### PLT-N1-P0-3（Ubiquitous）@ConditionalOnProperty 数据源切换
The system shall gate the siisdbDataSource and all SIIS-scoped beans via @ConditionalOnProperty(name="dsh.siis.enabled", havingValue="true"), so that deployments without SIIS load only the ontodb data source.

### PLT-N1-P0-4（Ubiquitous）数据库迁移
The system shall initialize all 26 ontodb tables idempotently on application startup via Flyway or startup SQL script; the 6 onto_ingestion_* tables shall remain skipped per P0 fix #4.

### PLT-N2-P0-1（Ubiquitous）API 规范
All API endpoints shall be prefixed with `/api/`, return JSON with unified wrapper {code, msg, data}, and use standard HTTP status codes.

### PLT-N2-P0-2（Ubiquitous）端口隔离
The dsh-java-ontology-app shall run on port 8092, separate from the dsh-java Harness on port 8090.

### PLT-N3-P0-1（Ubiquitous）插件通过 HTTP 调用
The dsh-java-ontology-plugin shall call the app's REST API via AdminApiClient (HTTP), not direct database access.

### PLT-N4-P0-1（Ubiquitous）七模型 YAML 驱动
The system shall use seven YAML models (M1-M7+MU) to drive schema generation, CRUD code generation, rule engine, permission seeds, workflow orchestration, report templates, and UI configuration.

### PLT-N5-P0-1（Ubiquitous）SSE 流式输出
The system shall support Server-Sent Events for streaming AI Agent responses to the client.

### PLT-N6-P0-1（Ubiquitous）统一监控视图
The system shall provide a `v_unified_monitor` view joining onto_sys_user, onto_common_*, and onto_global_aggregation_log for platform-wide monitoring, materialized as a database view.

### PLT-N6-P0-2（Ubiquitous）统一知识图谱应用层聚合
The system shall provide unified knowledge graph access via AggregationService application-layer aggregation (replacing the v_unified_kg database view), joining onto_global_kg_node/onto_global_kg_edge with onto_ontology_entity at query time.

---

# 3. 需求汇总

| 域 | 需求数 | P0 | P1 | P2 |
|----|--------|----|----|----|
| O ontology 本体注册 | 13 | 12 | 1 | 0 |
| C common 个人层沉淀 | 7 | 6 | 1 | 0 |
| G global 知识汇聚 | 8 | 8 | 0 | 0 |
| R report 报告展示 | 8 | 8 | 0 | 0 |
| S system 系统管理 | 6 | 6 | 0 | 0 |
| P P0 修复 | 6 | 6 | 0 | 0 |
| N 非功能 | 11 | 11 | 0 | 0 |
| **合计** | **59** | **57** | **2** | **0** |

---

# 4. 验证场景

## 场景 1：双数据库部署

1. 部署 dsh-java-ontology-app，配置 `dsh_java_ontodb` 数据源
2. 启动时 Flyway 初始化 26 张 `onto_*` 表 + 2 视图
3. 配置 `dsh.siis.enabled=true` → 加载 siisdbDataSource → DualDataSourceConfig 双库就绪
4. 配置 `dsh.siis.enabled=false` → 仅加载 ontodbDataSource → SIIS bean 跳过

## 场景 2：多业务方向本体注册

1. SUPER_ADMIN 创建 onto_ontology_type（domain=siis, type=hotspot）
2. SUPER_ADMIN 注册 onto_ontology_attribute（type=hotspot, attr=risk_level）
3. SIIS 研究员查询 `GET /api/ontology/entity?domain=siis` → 仅返回 siis 域实体
4. factory 研究员查询 `GET /api/ontology/entity?domain=factory` → 仅返回 factory 域实体

## 场景 3：fail-closed 权限

1. 未认证请求访问 `/api/ontology/entity` → 401（无 @PublicApi）
2. RESEARCHER 请求访问 `/api/auth/user/list` → 403（需 SUPER_ADMIN）
3. 任意请求访问 @PublicApi 端点 → 通过（仅 2 个）
4. 新增端点未加 @RequiresRole → RoleInterceptor 拒绝（fail-closed）

## 场景 4：三级知识流转

1. 租户 T-A 分析产生 KgNode（ontologyEntityId=null）→ 沉淀到 onto_common_discovered_entity
2. mentionCount > 5 → 升级到 onto_ontology_entity（共享层）
3. AggregationService 定时汇聚 → onto_global_discovered_entity
4. 主任查询 v_unified_kg 等价 API → 应用层聚合 onto_global_kg_node + onto_ontology_entity

## 场景 5：列名注入防护

1. 攻击者请求 `GET /api/ontology/entity?orderBy=(SELECT password FROM onto_sys_user)` → sanitizeRows 拒绝
2. 攻击者请求 `GET /api/ontology/entity?fields=*` → WRITABLE_FIELDS 白名单过滤
3. 合法请求 `GET /api/ontology/entity?orderBy=created_at` → 通过

## 场景 6：ingestion 降级

1. 启动时 onto_ingestion_* 6 表不创建（@ConditionalOnProperty 关闭）
2. 调用 ingestion API → 404 / 503（规划中）
3. 正式启用 → 配置开关 → Flyway 创建 6 表 → API 可用
