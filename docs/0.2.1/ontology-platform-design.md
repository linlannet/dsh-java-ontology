# 多语种国际问题研究平台 — 产品层设计文档 v0.2.1

> **版本**：v0.2.1 ｜ **日期**：2026-10-18 ｜ **状态**：P0 修复完成
>
> **定位**：dsh-java-ontology **产品层**（ontology platform）设计文档。覆盖 `dsh_java_ontodb` 26 张已建表 + 6 张设计中表 + 2 视图，以及跨项目共享的平台级能力（本体注册、全局知识汇聚、报告与告警、租户与认证、应用层聚合）。
>
> **本文档覆盖**：双数据库架构、`onto_` 前缀 26 表规划、5 个产品层 Controller、三级知识架构、DualDataSourceConfig 路由、P0 修复决策、ADR-001 存储范式。
>
> **关联文档**：
> - `docs/0.2.1/ontology-platform-spec.md` — 产品层 EARS 需求规格
> - `docs/0.2.1/ontology-platform-tasks.md` — 产品层实现任务
> - `docs/0.2.1/ONTO_DB_DESIGN.md` — `dsh_java_ontodb` 数据库 DDL 设计
> - `docs/0.2.1/ADR-001-ontology-storage-paradigm.md` — 存储范式决策记录
> - `docs/0.2.1/siis-platform-design.md` — SIIS 项目层设计（`dsh_java_siisdb` 36 表）

---

# 一、平台架构

## 1.1 四横架构（产品层视角）

```
基礎层：多语种本体库（onto_ontology_* 9 表 + 七模型 YAML 驱动 + LLM 赋能本体对齐）
    ↓
采集层：多语种信息监测（SIIS 项目层 gwy_track_* 11 表，Java 爬虫 + LLM 关键词）
    ↓
分析层：多语种信息分析（SIIS 项目层 gwy_analyze_* 7 表，LLM NLP + 知识图谱 + 关系分析）
    ↓
应用层：报告与决策（onto_report_* 7 表）+ 全局知识汇聚（onto_global_* 5 表）+ 应用层聚合
```

产品层向下沉淀共享本体与全局知识，向上为 SIIS / factory / ecommerce / city-ops 等项目层提供复用底座。项目层各自维护 `gwy_*` 业务表，通过双数据源接入产品层 `onto_*` 共享表。

## 1.2 v0.2.1 技术架构（双数据库）

```
┌──────────────────────────────────────────────────────────────┐
│              dsh-java Harness (:8090)                        │
│  ┌────────────┐ ┌────────────┐ ┌────────────┐               │
│  │ Agent 运行时│ │ Skill 加载 │ │ Workflow   │               │
│  │ ReactLoop  │ │ SkillService│ │ Orchestrator│              │
│  └────────────┘ └────────────┘ └────────────┘               │
│  ┌────────────────────────────────────────────────────┐     │
│  │              LLM Provider (流式/SSE)                │     │
│  └────────────────────────────────────────────────────┘     │
│  ┌────────────────────────────────────────────────────┐     │
│  │              Tool Registry (14 个工具)              │     │
│  └────────────────────────────────────────────────────┘     │
└──────────────────────────┬───────────────────────────────────┘
                           │ SPI 加载
┌──────────────────────────▼───────────────────────────────────┐
│           dsh-java-ontology-plugin                           │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐        │
│  │  Tools   │ │  Skills  │ │  Agents  │ │  YAML    │        │
│  │  (14个)  │ │  (4个)   │ │  (3个)   │ │  (7模型) │        │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘        │
└──────────────────────────┬───────────────────────────────────┘
                           │ HTTP (AdminApiClient)
┌──────────────────────────▼───────────────────────────────────┐
│           dsh-java-ontology-app (:8092)                      │
│  ┌────────────────────────────────────────────────────┐     │
│  │  Controller Layer (5 个产品层 Controller, 26 表)    │     │
│  └──────────────────────┬─────────────────────────────┘     │
│  ┌──────────────────────▼─────────────────────────────────┐ │
│  │  Service Layer (4 域 Service + AggregationService)     │ │
│  └──────────────────────┬─────────────────────────────────┘ │
│  ┌──────────────────────▼─────────────────────────────────┐ │
│  │  Repository Layer (DualDataSourceConfig + getJdbc 路由)│ │
│  └──────────────────────┬─────────────────────────────────┘ │
└──────────────────────────┬───────────────────────────────────┘
                           │ 双数据源
┌──────────────────────────▼───────────────────────────────────┐
│  ┌─────────────────────────┐  ┌──────────────────────────┐  │
│  │  dsh_java_ontodb        │  │  dsh_java_siisdb         │  │
│  │  产品层共享库           │  │  SIIS 项目层库           │  │
│  │  onto_* 26 表 + 6 设计中│  │  gwy_* 36 表             │  │
│  │  + 2 视图               │  │                          │  │
│  │  跨项目复用             │  │  SIIS 专属               │  │
│  └─────────────────────────┘  └──────────────────────────┘  │
└──────────────────────────────────────────────────────────────┘
```

## 1.3 三级知识架构

| 层级 | 数据 | 表数 | 可见角色 | 说明 |
|------|------|------|---------|------|
| 个人层 | `onto_common_*` 租户私有表（tenant_id 隔离）| 4 | 本人 | 研究人员的私有发现实体、关系资产、向量索引、AI 对话历史 |
| 共享层 | `onto_ontology_*` 9 表 + `onto_global_*` 5 表 | 14 | 全体研究员 + 主任 | 本体注册表 + 各租户沉淀知识的全局汇聚 |
| 统一层 | `AggregationService` 应用层聚合（替代 `v_unified_kg` 视图）| 0（服务）| 主任 | 全平台监测仪表板 + 决策分析报告 |

**关键变更**：v0.2.1 起，统一层不再使用 `v_unified_kg` 视图做跨库聚合，改由 `AggregationService` 在应用层跨数据源聚合。原因见 P0 修复 §3。

---

# 二、双数据库架构

## 2.1 数据库划分

| 数据库 | 职责 | 表数 | 前缀 | 复用范围 |
|--------|------|------|------|---------|
| `dsh_java_ontodb` | 产品层共享底座 | 26 已建 + 6 设计中 + 2 视图 | `onto_` | siis / factory / ecommerce / city-ops 跨项目复用 |
| `dsh_java_siisdb` | SIIS 项目层业务 | 36 | `gwy_` | SIIS 专属 |

总表数：62（26 + 36）。

## 2.2 DualDataSourceConfig

SIIS app 通过 `DualDataSourceConfig` 同时连接两个数据库：

```java
@Configuration
@ConditionalOnProperty("dsh.dual-datasource.enabled")
public class DualDataSourceConfig {

    @Bean("siisJdbcTemplate")
    public JdbcTemplate siisJdbcTemplate(DataSource siisDataSource) {
        return new JdbcTemplate(siisDataSource);
    }

    @Bean("ontoJdbcTemplate")
    public JdbcTemplate ontoJdbcTemplate(DataSource ontoDataSource) {
        return new JdbcTemplate(ontoDataSource);
    }
}
```

- `siisJdbcTemplate` → `dsh_java_siisdb`（`gwy_` 表）
- `ontoJdbcTemplate` → `dsh_java_ontodb`（`onto_` 表）
- `@ConditionalOnProperty("dsh.dual-datasource.enabled")` 开关：生产开启，测试环境自动回退到单数据源

## 2.3 getJdbc 路由

`BaseRepository` 根据表名前缀路由到对应 `JdbcTemplate`：

```java
public abstract class BaseRepository<T> {

    @Autowired @Qualifier("siisJdbcTemplate")
    protected JdbcTemplate siisJdbcTemplate;

    @Autowired @Qualifier("ontoJdbcTemplate")
    protected JdbcTemplate ontoJdbcTemplate;

    protected JdbcTemplate getJdbc(String table) {
        if (table.startsWith("onto_")) {
            return ontoJdbcTemplate;
        }
        return siisJdbcTemplate;
    }

    protected abstract String getTableName();
    protected abstract RowMapper<T> getRowMapper();
}
```

## 2.4 跨库访问

跨库查询使用**全限定名**（`dsh_java_ontodb.onto_ontology_entity`），前提是 MySQL 用户对两库均有 SELECT 权限。产品层 Controller 默认只访问 `dsh_java_ontodb`；SIIS 项目层 Controller 通过 `getJdbc` 路由混合访问两库。

---

# 三、`dsh_java_ontodb` 26 表规划

## 3.1 表分类总览

| 域 | 表数 | 层级 | 表名 |
|----|------|------|------|
| **onto_sys_** | 1 | 系统 | onto_sys_user |
| **onto_common_** | 4 | 个人层 | onto_common_discovered_entity, onto_common_tenant_relation_asset, onto_common_personal_vector_index, onto_common_llm_chat_history |
| **onto_global_** | 5 | 共享层 | onto_global_discovered_entity, onto_global_bilateral_relation, onto_global_kg_node, onto_global_kg_edge, onto_global_aggregation_log |
| **onto_report_** | 7 | 个人层 | onto_report_rpt_template, onto_report_rpt_report, onto_report_alert_record, onto_report_information_publish, onto_report_push_record, onto_report_recommendation_record, onto_report_search_index |
| **onto_ontology_** | 9 | 共享层 | onto_ontology_version, onto_ontology_entity, onto_ontology_term, onto_ontology_event_type, onto_ontology_location, onto_ontology_i18n, onto_ontology_type, onto_ontology_attribute, onto_ontology_relation |
| **合计（已建）** | **26** | — | — |

## 3.2 设计中 6 表（onto_ingestion_*，规划中）

`onto_ingestion_*` 6 表为数据接入层规划，**暂未实施**，标记为规划中（见 P0 修复 §4）。涉及表名预留：

- onto_ingestion_source
- onto_ingestion_subscription
- onto_ingestion_task
- onto_ingestion_keyword_group
- onto_ingestion_data_import
- onto_ingestion_data_export

## 3.3 视图（2 个）

| 视图 | 状态 | 说明 |
|------|------|------|
| `v_unified_monitor` | 在用 | 统一监测视图 |
| `v_unified_kg` | **已弃用** | v0.2.1 起改用 `AggregationService` 应用层聚合（见 P0 修复 §3）|

## 3.4 公共字段

所有 26 张表包含以下公共字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT PK AUTO_INCREMENT | 主键 |
| `tenant_id` | BIGINT | 租户ID（共享层表为 NULL，个人层表必填）|
| `created_by` | VARCHAR(50) | 创建者 |
| `created_at` | DATETIME | 创建时间 |
| `updated_by` | VARCHAR(50) | 更新者 |
| `updated_at` | DATETIME | 更新时间 |
| `del_flag` | TINYINT DEFAULT 0 | 删除标志（0=正常 1=删除）|
| `remark` | VARCHAR(800) | 备注 |

**例外**：`onto_ontology_*` 9 张表的 `tenant_id` 为 NULL（共享层）；`onto_sys_user` 的 `tenant_id` 表示用户所属租户。

## 3.5 关键表设计要点

### onto_ontology_* 9 表（共享层本体注册表）

按 ADR-001 路线 A 设计（见 §六）：采用**专表 + `onto_ontology_type` 元数据注册表**范式，9 表分工：

| 表 | 职责 |
|----|------|
| onto_ontology_version | 本体版本管理 |
| onto_ontology_entity | 本体实体（统一实体表，替代 v2.0 的 country/organization/person/... 多实体表）|
| onto_ontology_term | 术语 |
| onto_ontology_event_type | 事件类型 |
| onto_ontology_location | 地点 |
| onto_ontology_i18n | 多语种国际化 |
| onto_ontology_type | 类型元数据注册表（路线 A 核心）|
| onto_ontology_attribute | 属性元数据 |
| onto_ontology_relation | 关系元数据 |

### onto_global_* 5 表（共享层全局汇聚）

| 表 | 职责 |
|----|------|
| onto_global_discovered_entity | 全局发现实体汇聚（UNIQUE: entity_name + entity_type）|
| onto_global_bilateral_relation | 全局双边关系汇聚（UNIQUE: subject + object + relation_type）|
| onto_global_kg_node | 全局知识图谱节点（按 ontology_entity_id 合并）|
| onto_global_kg_edge | 全局知识图谱边 |
| onto_global_aggregation_log | 汇聚日志 |

### onto_common_* 4 表（个人层）

| 表 | 职责 |
|----|------|
| onto_common_discovered_entity | 租户私有发现实体 |
| onto_common_tenant_relation_asset | 租户关系资产 |
| onto_common_personal_vector_index | 个人向量索引 |
| onto_common_llm_chat_history | AI 对话历史 |

### onto_report_* 7 表（个人层报告与告警）

| 表 | 职责 |
|----|------|
| onto_report_rpt_template | 报告模板 |
| onto_report_rpt_report | 报告实例 |
| onto_report_alert_record | 告警记录 |
| onto_report_information_publish | 信息发布 |
| onto_report_push_record | 推送记录 |
| onto_report_recommendation_record | 推荐记录 |
| onto_report_search_index | 搜索索引 |

---

# 四、Controller 规划（5 个产品层 Controller，覆盖 26 表）

## 4.1 设计原则

| 原则 | 说明 |
|------|------|
| **BaseController<T> 模板** | 所有 CRUD Controller 继承 BaseController，仅需提供 RowMapper + SQL 即可拥有完整 CRUD |
| **一域一 Controller** | 按 `onto_` 子前缀分组，每域一个 Controller 类 |
| **URL 规范** | `/api/{domain}/{entity}/{action}`，如 `/api/ontology/onto_ontology_entity/list` |
| **租户隔离** | BaseController 自动注入 tenant_id 过滤（个人层），共享层不注入 |
| **权限 fail-closed** | @PublicApi + @RequiresRole + RoleInterceptor，123/123 端点全覆盖 |
| **列名注入防护** | WRITABLE_FIELDS 白名单 + sanitizeRows 过滤内部列 |
| **统一响应** | 所有端点返回 `Result<T>`（{code, msg, data}）|

## 4.2 Controller 清单

| # | Controller | URL 前缀 | 覆盖表数 | 表名 | 层级 | 角色 |
|---|-----------|---------|---------|------|------|------|
| 1 | **OntologyController** | `/api/ontology/*` | 9 | onto_ontology_version, onto_ontology_entity, onto_ontology_term, onto_ontology_event_type, onto_ontology_location, onto_ontology_i18n, onto_ontology_type, onto_ontology_attribute, onto_ontology_relation | 共享层 | 全体可读，主任可写 |
| 2 | **GlobalKnowledgeController** | `/api/global/*` | 5 | onto_global_discovered_entity, onto_global_bilateral_relation, onto_global_kg_node, onto_global_kg_edge, onto_global_aggregation_log | 共享层 | 主任读写，研究员只读 |
| 3 | **ReportController** | `/api/report/*` | 7 | onto_report_rpt_template, onto_report_rpt_report, onto_report_alert_record, onto_report_information_publish, onto_report_push_record, onto_report_recommendation_record, onto_report_search_index | 个人层 | 研究员 CRUD |
| 4 | **AuthController** | `/api/auth/*` | 1 | onto_sys_user | 系统 | 公开（登录）/ 主任管理 |
| 5 | **AggregationController** | `/api/aggregation/*` | 0 | （应用层聚合，操作 onto_global_* 表，不直接映射单一表）| — | 主任 |
| | **合计** | | **22** | + AggregationController 编排 | | |

注：26 表中余下 4 张 `onto_common_*` 个人层表由 SIIS app 的 WorkspaceController（项目层）通过 `ontoJdbcTemplate` 访问，不单独建产品层 Controller。

## 4.3 各 Controller 端点规划

### 4.3.1 OntologyController（9 实体，共享层）

```
基础 CRUD（每个实体）：
  GET    /api/ontology/{entity}/list          — 分页列表（全体可读）
  GET    /api/ontology/{entity}/{id}          — 详情
  POST   /api/ontology/{entity}               — 新增（@RequiresRole SUPER_ADMIN）
  PUT    /api/ontology/{entity}/{id}          — 更新（@RequiresRole SUPER_ADMIN）
  DELETE /api/ontology/{entity}/{id}          — 删除（@RequiresRole SUPER_ADMIN）

实体列表：onto_ontology_version, onto_ontology_entity, onto_ontology_term, onto_ontology_event_type,
          onto_ontology_location, onto_ontology_i18n, onto_ontology_type, onto_ontology_attribute,
          onto_ontology_relation

特殊端点：
  POST /api/ontology/query                    — 自然语言跨实体查询
  GET  /api/ontology/search?keywords=         — 关键词搜索
  POST /api/ontology/entity/promote           — 发现实体升级到本体
  GET  /api/ontology/onto_ontology_entity?type=— 按类型过滤实体
  GET  /api/ontology/i18n?entityId=&lang=     — 多语种查询
```

**端点数**：9 × 5 + 5 = **50 个端点**

### 4.3.2 GlobalKnowledgeController（5 实体，共享层）

```
基础 CRUD（每个实体，共享层无 tenant_id 隔离）：
  GET    /api/global/{entity}/list            — 全体研究员可读
  GET    /api/global/{entity}/{id}            — 全体研究员可读
  POST   /api/global/{entity}                 — @RequiresRole SUPER_ADMIN
  PUT    /api/global/{entity}/{id}            — @RequiresRole SUPER_ADMIN
  DELETE /api/global/{entity}/{id}            — @RequiresRole SUPER_ADMIN

实体列表：onto_global_discovered_entity, onto_global_bilateral_relation,
          onto_global_kg_node, onto_global_kg_edge, onto_global_aggregation_log

特殊端点：
  GET  /api/global/kgNode/{id}/neighbors?depth=— 全局图谱邻居
  GET  /api/global/bilateralRelation?subject=&object=— 全局关系查询
  GET  /api/global/aggregationLog?startDate=&endDate=— 汇聚日志查询
```

**端点数**：5 × 5 + 3 = **28 个端点**

### 4.3.3 ReportController（7 实体，个人层）

```
基础 CRUD（每个实体，自动 tenant_id 隔离）：
  GET    /api/report/{entity}/list
  GET    /api/report/{entity}/{id}
  POST   /api/report/{entity}
  PUT    /api/report/{entity}/{id}
  DELETE /api/report/{entity}/{id}

实体列表：onto_report_rpt_template, onto_report_rpt_report, onto_report_alert_record,
          onto_report_information_publish, onto_report_push_record,
          onto_report_recommendation_record, onto_report_search_index

特殊端点：
  POST /api/report/rptReport/generate        — 生成报告（可指定 scope=GLOBAL）
  GET  /api/report/rptReport/{id}/sections   — 报告各 section 数据
  GET  /api/report/alertRecord?topicId=&level=— 告警过滤
  POST /api/report/informationPublish/{id}/publish — 发布信息
  POST /api/report/searchIndex/rebuild       — 重建搜索索引
  GET  /api/report/recommendation?userId=    — 个性化推荐
```

**端点数**：7 × 5 + 6 = **41 个端点**

### 4.3.4 AuthController（1 实体，系统）

```
  POST /api/auth/login                        — 登录（返回 JWT + tenantId + roleLevel）
  POST /api/auth/register                     — 注册
  GET  /api/auth/userinfo                     — 当前用户信息
  POST /api/auth/logout                       — 登出
  GET  /api/auth/user/list                    — 用户列表（@RequiresRole SUPER_ADMIN）
  POST /api/auth/user                         — 创建用户（@RequiresRole SUPER_ADMIN）
  PUT  /api/auth/user/{id}                    — 更新用户
  DELETE /api/auth/user/{id}                  — 删除用户
  PUT  /api/auth/user/{id}/role               — 修改角色（@RequiresRole SUPER_ADMIN）
```

**端点数**：**9 个端点**（其中 2 个 @PublicApi：login、register）

## 4.4 AggregationController（应用层聚合，替代 v_unified_kg 视图）

```
  POST /api/aggregation/trigger              — 手动触发汇聚（@RequiresRole SUPER_ADMIN）
  POST /api/aggregation/trigger/incremental  — 增量汇聚
  GET  /api/aggregation/status               — 汇聚状态
  GET  /api/aggregation/log                  — 汇聚日志
  GET  /api/aggregation/overview             — 主任决策仪表板（跨租户聚合）
  GET  /api/aggregation/overview/tenants     — 各租户活跃度
  GET  /api/aggregation/overview/hotspots    — 全局热点分布
  GET  /api/aggregation/overview/alerts      — 全局告警汇总
  GET  /api/aggregation/unifiedKg?query=     — 统一知识图谱查询（应用层聚合）
```

**端点数**：**9 个端点**

`AggregationService` 在应用层跨数据源聚合 `onto_global_*` + `onto_ontology_*`，替代原 `v_unified_kg` 视图的跨库 JOIN。

## 4.5 端点统计

| Controller | CRUD 端点 | 特殊端点 | 合计 |
|-----------|----------|---------|------|
| OntologyController | 45 | 5 | 50 |
| GlobalKnowledgeController | 25 | 3 | 28 |
| ReportController | 35 | 6 | 41 |
| AuthController | 0 | 9 | 9 |
| AggregationController | 0 | 9 | 9 |
| **产品层合计** | **105** | **32** | **137** |

注：产品层 137 端点中，123 端点已通过反向生成确认 API contract（121 @RequiresRole + 2 @PublicApi），余下为 SIIS 项目层 Controller 端点。

## 4.6 BaseController<T> 模板设计

```java
public abstract class BaseController<T extends BaseEntity> {

    protected abstract BaseService<T> getService();
    protected abstract String getEntityName();

    @GetMapping("/{entity}/list")
    public Result<PageResult<T>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        return Result.success(getService().pageQuery(pageNum, pageSize, keyword));
    }

    @GetMapping("/{entity}/{id}")
    public Result<T> get(@PathVariable Long id) {
        return Result.success(getService().getById(id));
    }

    @PostMapping("/{entity}")
    public Result<T> add(@RequestBody T entity) {
        return Result.success(getService().insert(entity));
    }

    @PutMapping("/{entity}/{id}")
    public Result<T> update(@PathVariable Long id, @RequestBody T entity) {
        entity.setId(id);
        return Result.success(getService().update(entity));
    }

    @DeleteMapping("/{entity}/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        getService().deleteById(id);
        return Result.success();
    }
}
```

---

# 五、Service 层设计

## 5.1 Service 分组

| Service | 职责 | 依赖 |
|---------|------|------|
| **OntologyService** | 9 实体 CRUD + 综合查询 + 实体升级 | OntologyMatchService |
| **GlobalKnowledgeService** | 5 汇聚表 CRUD + 全局查询 | KnowledgeAggregator |
| **ReportService** | 7 实体 CRUD + 报告生成 + 推送 | ReportGenerator, PushService |
| **AuthService** | 登录/注册/JWT/用户管理 | JwtUtil, TenantContext |
| **AggregationService** | 应用层跨数据源聚合（替代 v_unified_kg）| GlobalKnowledgeService, OntologyService |
| **KnowledgeAggregator** | 知识汇聚（定时/事件/手动）| 所有 Repository |

## 5.2 AggregationService 编排

```
aggregateUnifiedKg(query):
  1. → onto_global_kg_node 跨租户节点聚合
  2. → onto_global_kg_edge 跨租户边聚合
  3. → onto_ontology_entity 本体对齐
  4. → 应用层合并（替代 v_unified_kg 视图 SQL）
  5. → 返回统一知识图谱

onTopicCompleted(topicId):
  1. → KnowledgeSedimentation.sediment(topicId)  // 知识沉淀
  2. → KnowledgeAggregator.aggregateIncremental(topicId)  // 增量汇聚
```

---

# 六、ADR-001 存储范式（路线 A 决策摘要）

**决策**：本体存储采用**路线 A — 专表 + `onto_ontology_type` 元数据注册表**。

| 对比项 | 路线 A（采纳）| 路线 B（弃用）|
|--------|--------------|--------------|
| 实体存储 | `onto_ontology_entity` 统一表 + `onto_ontology_type` 类型元数据 | 每类型一表（country/organization/person/...）|
| 扩展新类型 | 仅在 `onto_ontology_type` 注册一行，零 DDL | 新建表 + DDL + Controller |
| 查询 | 按 type 过滤统一表 | 跨表 UNION |
| 字段约束 | 类型特定约束在 `onto_ontology_attribute` 元数据 | 表 schema 强约束 |

**采纳理由**：
1. 跨项目复用需要灵活扩展类型，避免每接入一个新项目就改 DDL
2. `onto_ontology_type` 元数据注册表支持运行时新增类型
3. 与七模型 YAML 驱动天然契合（M1 YAML → type 注册）

详见 `docs/0.2.1/ADR-001-ontology-storage-paradigm.md`。

---

# 七、P0 修复（2026-10-18 全部完成）

| # | 修复项 | 设计决策 |
|---|--------|---------|
| 1 | **权限 fail-closed** | @PublicApi + @RequiresRole + RoleInterceptor，123/123 端点全覆盖。无注解端点默认拒绝（fail-closed）|
| 2 | **列名注入防护** | WRITABLE_FIELDS 白名单 + sanitizeRows 过滤内部列（tenant_id / del_flag 等不可外部注入）|
| 3 | **共享层唯一化** | 双库分离（`dsh_java_ontodb` + `dsh_java_siisdb`）；`v_unified_kg` 改用 `AggregationService` 应用层聚合，避免跨库视图 JOIN |
| 4 | **ingestion 降级** | `onto_ingestion_*` 6 表标记为规划中，暂不实施；采集层由 SIIS 项目层 `gwy_track_*` 承担 |
| 5 | **ADR-001 存储范式** | 路线 A（专表 + `onto_ontology_type` 元数据注册表），见 §六 |
| 6 | **API contract** | 123 端点代码反向生成 API contract 文档，确保实现与文档一致 |

---

# 八、Repository 层设计

## 8.1 BaseRepository<T>（双数据源路由）

```java
public abstract class BaseRepository<T> {

    @Autowired @Qualifier("siisJdbcTemplate")
    protected JdbcTemplate siisJdbcTemplate;

    @Autowired @Qualifier("ontoJdbcTemplate")
    protected JdbcTemplate ontoJdbcTemplate;

    protected JdbcTemplate getJdbc(String table) {
        if (table.startsWith("onto_")) {
            return ontoJdbcTemplate;
        }
        return siisJdbcTemplate;
    }

    protected abstract String getTableName();
    protected abstract RowMapper<T> getRowMapper();
    protected abstract Map<String, Object> toMap(T entity);

    protected String appendTenantFilter(String sql) {
        if (TenantContext.isSharedLayer(getTableName())) {
            return sql;
        }
        return sql + " AND tenant_id = ?";
    }

    public T findById(Long id) { ... }
    public PageResult<T> pageQuery(int pageNum, int pageSize, String keyword) { ... }
    public T insert(T entity) { ... }
    public T update(T entity) { ... }
    public void deleteById(Long id) { ... }
}
```

## 8.2 租户隔离机制

| 层级 | 表 | tenant_id 处理 |
|------|-----|---------------|
| 个人层 | `onto_common_*`, `onto_report_*` | BaseRepository 自动注入 `WHERE tenant_id = ?` |
| 共享层 | `onto_ontology_*`, `onto_global_*` | 不注入 tenant_id 过滤，全体可读 |
| 统一层 | `AggregationService` 应用层聚合 | 仅 SUPER_ADMIN 可访问，跨租户聚合 |

---

# 九、七模型 YAML 驱动

## 9.1 YAML 模型库

```
ontology-models/
├── manifest.json
├── m1-object-model.yaml      # 26 实体（onto_* 产品层 + 双库路由）
├── m2-behavior-model.yaml    # CRUD + 工作主线编排 + syncTriggers
├── m3-rule-model.yaml        # 告警规则 + 垃圾数据检验 + 实体升级规则
├── m5-actor-model.yaml       # 多租户 RBAC + ABAC（SUPER_ADMIN/RESEARCHER/GUEST）
├── m6-flow-model.yaml        # 工作流 + 汇聚流程
├── m7-report-model.yaml      # 7 维分析 + 报告模板 + 主任决策报告
└── mu-ui-model.yaml          # 工作台界面（可选）
```

## 9.2 M1 → DDL → Controller 生成管线

```
M1 YAML → DDL 生成 → MySQL 建表（dsh_java_ontodb 26 表）
M1 YAML → Model + RowMapper 生成 → BaseEntity 子类
M1 YAML → Repository 生成 → BaseRepository 子类（含 getJdbc 路由）
M1 YAML → Service 生成 → BaseService 子类
M1 YAML → Controller 生成 → BaseController 子类（CRUD 端点自动生成）
M2 YAML → 特殊端点 + syncTriggers 编排
M5 YAML → 权限种子（角色 + 资源 + 租户隔离 + fail-closed）
M6 YAML → 工作流编排 + 汇聚流程
M7 YAML → 报告模板 + API 端点
```

---

# 十、Plugin 设计（14 Tool + 4 Skill + 3 Agent）

## 10.1 Tool（14 个）

| 工具名 | 类型 | Controller 对应 | LLM |
|--------|------|----------------|-----|
| ontology_query | 只读 | OntologyController | — |
| ontology_search | 只读 | OntologyController | LLM embed |
| ontology_promote | 写 | OntologyController | — |
| kg_query | 只读 | GlobalKnowledgeController | — |
| kg_search | 只读 | GlobalKnowledgeController | — |
| relation_analysis | 只读 | GlobalKnowledgeController | — |
| topic_create | 写 | （SIIS 项目层）| LLM |
| crawl_execute | 写 | （SIIS 项目层）| — |
| nlp_extract | 写 | （SIIS 项目层）| LLM |
| report_generate | 写 | ReportController | LLM |
| alert_query | 只读 | ReportController | — |
| workspace_overview | 只读 | （SIIS 项目层）| — |
| knowledge_aggregate | 写 | AggregationController | — |
| director_overview | 只读 | AggregationController | — |

## 10.2 Skill（4 个）

| Skill | 触发词 | 编排 |
|-------|--------|------|
| hotspot-analysis | 热点分析/区域分析 | 七阶段全流程 |
| topic-creation | 创建主题/新建分析 | 主题+关键词+采集源 |
| report-generation | 生成报告/周报/月报 | 报告生成 |
| knowledge-extraction | 知识萃取/实体抽取 | LLM 萃取 |

## 10.3 Agent（3 个）

| Agent | 角色 | 工具集 |
|-------|------|--------|
| research-assistant | 研究员助手 | ontology/kg/relation/topic/report/alert/workspace |
| topic-creator | 主题创建助手 | ontology/topic_create/workspace |
| director-assistant | 主任决策助手 | director_overview/knowledge_aggregate/ontology/kg/relation/report |

---

# 十一、角色与权限

| 角色 | role_level | 可见范围 | Controller 权限 |
|------|-----------|---------|----------------|
| SUPER_ADMIN（主任） | 0 | 全部（跨租户） | 全部 Controller + AggregationController + GlobalKnowledgeController 写权限 |
| RESEARCHER（研究员） | 1 | 自己私有 + 共享层只读 | OntologyController 只读 + Report CRUD + Global 只读 |
| GUEST（访客） | 2 | 共享层只读 | OntologyController 只读 + Global 只读 |

权限模型为 fail-closed：未显式标注 @PublicApi 或 @RequiresRole 的端点一律拒绝访问。

---

# 十二、部署架构

```
┌─────────────────────────────┐  ┌─────────────────────────────┐
│  MySQL 8.0 (:3306)          │  │  MySQL 8.0 (:3306)          │
│  database: dsh_java_ontodb  │  │  database: dsh_java_siisdb  │
│  onto_* 26 表 + 6 设计中    │  │  gwy_* 36 表                │
│  + 2 视图                   │  │                             │
└──────────────┬──────────────┘  └──────────────┬──────────────┘
               │                                │
               └──────────────┬─────────────────┘
                              │ DualDataSourceConfig
┌─────────────────────────────▼─────────────────┐
│  dsh-java-ontology-app / SIIS app              │
│  (:8092)                                       │
│  Spring Boot 3.5.16                            │
│  DualDataSourceConfig + getJdbc 路由           │
│  产品层 5 Controller / 137 端点                │
│  + SIIS 项目层 Controller                      │
└─────────────────────────────┬─────────────────┘
                              │ HTTP
┌─────────────────────────────▼─────────────────┐
│  dsh-java Harness (:8090)                     │
│  + ontology-plugin                            │
│  14 Tool / 4 Skill / 3 Agent                  │
│  LLM Provider (SSE)                           │
└────────────────────────────────────────────────┘
```

---

# 十三、测试规划

| 模块 | 测试数 | 说明 |
|------|--------|------|
| dsh-onto-app | 48 | 产品层 Controller + Service + Repository |
| SIIS app | 52 | SIIS 项目层 Controller + 双数据源路由 |
| **合计** | **100** | — |

测试环境通过 `dsh.dual-datasource.enabled=false` 自动回退到单数据源，避免双库部署开销。

---

# 十四、v2.0 → v0.2.1 对照

| 对比项 | v2.0 | v0.2.1 |
|--------|------|---------|
| 数据库架构 | 单库 `ontology_db`（51 表）| 双库 `dsh_java_ontodb`（26+6+2）+ `dsh_java_siisdb`（36）|
| 表前缀 | `gwy_*` 混合 | `onto_*` 产品层 + `gwy_*` SIIS 项目层分离 |
| 总表数 | 51 | 62（26 + 36）|
| 本体存储 | 多实体表（country/organization/person/...）| 路线 A：`onto_ontology_entity` 统一表 + `onto_ontology_type` 元数据 |
| Controller | 10 个（312 端点）| 产品层 5 个（137 端点）+ SIIS 项目层 Controller |
| API 端点 | 312 | 123（已反向生成 contract，121 @RequiresRole + 2 @PublicApi）|
| 统一层聚合 | `v_unified_kg` 视图 | `AggregationService` 应用层聚合 |
| 权限 | @RequiresRole（部分覆盖）| fail-closed + 123/123 全覆盖 |
| 列名注入 | 无防护 | WRITABLE_FIELDS 白名单 + sanitizeRows |
| 数据源 | 单 JdbcTemplate | DualDataSourceConfig + getJdbc 路由 |
| ingestion | 在建 | 降级为规划中 |
| 测试 | — | 100（dsh-onto-app 48 + SIIS 52）|
