# 多语种国际问题研究平台 — 产品层任务清单 v0.2.1

> **版本**：v0.2.1 ｜ **日期**：2026-10-18 ｜ **状态**：✅ 全部完成
>
> **本文档**是 dsh-java-ontology **产品层**（ontology platform）的任务清单，覆盖 `dsh_java_ontodb` 26 张已建表 + 6 张设计中表 + 2 视图、平台级共享能力（Auth / ontology 域 / 三级架构 / 汇聚 / Plugin Tool / Skill / Agent / 七模型 YAML）。SIIS 项目层任务见 `siis-platform-tasks.md`。
>
> **关联**：
> - `ontology-platform-spec.md` — 产品层 EARS 需求规格
> - `ontology-platform-design.md` — 产品层设计（含 Controller 规划、双库 DataSource）
> - `ONTO_DB_DESIGN.md` — `dsh_java_ontodb` / `dsh_java_siisdb` 双库 DDL 设计
> - `ADR-001-ontology-storage-paradigm.md` — 本体存储范式 ADR（路线 A：专表 + onto_ontology_type 元数据注册表）
> - `siis-platform-tasks.md` — SIIS 项目层任务清单（36 表）
>
> - **技术栈**：Java 17 + Spring Boot 3.5.16 + JdbcTemplate + MySQL（双库） + dsh-java Harness Plugin API
> - **双库架构**：`dsh_java_ontodb`（产品层共享，26 表 + 2 视图，`onto_` 前缀） + `dsh_java_siisdb`（SIIS 项目层，36 表，`gwy_` 前缀）
> - **根包**：`com.howai.dsh.ontology`（app）/ `com.howai.dsh.ontology.plugin`（plugin）
> - **任务编号**：`PLT-T{phase}.{序号}`

---

# 一、任务总览

## 1.1 Phase 分布

| Phase | 优先级 | 任务数 | 目标 | 关联文档 |
|-------|--------|--------|------|---------|
| Phase 1 — 数据基座（双库） | P0 | 9 | DualDataSourceConfig + ontodb 26 表 DDL + siisdb 36 表 DDL + 通用基类 | ONTO_DB_DESIGN.md |
| Phase 2 — 七模型 YAML 建模 | P0 | 7 | M1-M7+MU YAML + manifest | ontology-platform-design.md |
| Phase 3 — ontology 域 CRUD（onto_ontology_* 9 表） | P0 | 11 | 9 实体 CRUD + onto_ontology_type 种子 + 综合查询 + 对齐 | ADR-001 |
| Phase 4 — track 域 CRUD（SIIS 项目层） | P0 | 12 | 11 实体 CRUD + 图谱查询 + 导入导出（见 siis-platform-tasks.md） | siis-platform-tasks.md |
| Phase 5 — analyze 域 CRUD（SIIS 项目层） | P0 | 10 | 7 实体 CRUD + NLP + 关系 + 告警（见 siis-platform-tasks.md） | siis-platform-tasks.md |
| Phase 6 — display 域 CRUD（onto_report_* 7 表） | P0 | 12 | 7 实体 CRUD + 报告 + 推送 + 推荐 | ontology-platform-design.md |
| Phase 7 — workspace 域 CRUD（onto_common_* 4 表） | P0 | 8 | 4 实体 CRUD + 知识沉淀 + 概览 | ontology-platform-design.md |
| Phase 8 — Auth + 系统管理（onto_sys_user） | P0 | 6 | onto_sys_user + JWT + 角色权限 | ontology-platform-spec.md |
| Phase 9 — 工作流编排 | P0 | 6 | WorklineOrchestrator 七阶段 | ontology-platform-design.md |
| Phase 10 — 互联网爬取引擎 | P0 | 7 | CrawlEngine/调度/限流/多源 | ontology-platform-design.md |
| Phase 11 — 多语种 NLP | P0 | 8 | 四语种策略/图谱归一化 | ontology-platform-design.md |
| Phase 12 — 三级知识架构 + 应用层聚合 | P0 | 9 | onto_global_* 5 表 + AggregationService + v_unified_monitor | ADR-001 |
| Phase 13 — Plugin Tool（15 个） | P0 | 14 | 15 原子工具 + AdminApiClient | ontology-platform-design.md |
| Phase 14 — Skill + Agent | P0 | 8 | 4 Skill + 3 Agent + 注册 | ontology-platform-design.md |
| Phase 15 — 集成测试 | P2 | 7 | 端到端 + Agent 对话 + 主任决策 | — |
| Phase 16 — P0 修复（双库架构对齐） | P0 | 6 | 权限/列名/共享层/ingestion/ADR/API contract | ADR-001 |
| **合计** | — | **140** | **v0.2.1 GA** | — |

> **与 siis-platform-tasks.md 的关系**：本产品层任务清单覆盖 `dsh_java_ontodb` 26 表 + 2 视图 + 平台级共享能力（Phase 1/2/3/6/7/8/12/13/14/16），SIIS 项目层 36 表的 track/analyze/display 域 CRUD 详尽任务见 `siis-platform-tasks.md`（本文档 Phase 4/5 仅列摘要条目以保持依赖关系完整）。

## 1.2 关键数字

| 维度 | 数量 | 说明 |
|------|------|------|
| 产品层表数（ontodb） | 26（已建）+ 6（设计中 onto_ingestion_*）= 32 | 另 2 视图（v_unified_monitor, v_unified_kg） |
| SIIS 项目层表数（siisdb） | 36 | gwy_ontology_*(8) + gwy_track_*(11) + gwy_analyze_*(7) + gwy_display_*(10) |
| 总表数 | 62（26 + 36） | 双库分离，跨项目复用 ontodb |
| API 端点 | 123 | 121 @RequiresRole + 2 @PublicApi |
| 测试 | 100 | dsh-onto-app 48 + SIIS 52 |
| Plugin Tool | 15 | 1 AdminApiClient + 15 原子工具 |
| Skill | 4 | hotspot-analysis / topic-creation / report-generation / knowledge-extraction |
| Agent | 3 | research-assistant / topic-creator / director-assistant |

---

# 二、Phase 1 — 数据基座（双库，9 任务，P0）✅

> 目标：双库 DataSource 配置、ontodb 26 表 DDL、siisdb 36 表 DDL、通用基类。**2026-10-13 全部完成。**

| 任务ID | 描述 | 模块 | 依赖 | 状态 |
|--------|------|------|------|------|
| PLT-T1.001 | `dsh_java_ontodb` 26 表 DDL（onto_sys/onto_common/onto_global/onto_report/onto_ontology 五组） | app | 无 | ✅ V1.0.0__init_ontodb_schema.sql |
| PLT-T1.002 | `dsh_java_siisdb` 36 表 DDL（gwy_ontology/gwy_track/gwy_analyze/gwy_display 四组） | app | 无 | ✅ V1.0.0__init_siisdb_schema.sql |
| PLT-T1.003 | DualDataSourceConfig + HikariCP + 双 JdbcTemplate（ontoJdbcTemplate / siisJdbcTemplate） | app | T1.001, T1.002 | ✅ application.yml + DualDataSourceConfig |
| PLT-T1.004 | BaseEntity + PageResult + Result 通用基类 | app | 无 | ✅ common 包 |
| PLT-T1.005 | BaseController + BaseService + BaseRepository 模板（支持 @DataSource 切换） | app | T1.004 | ✅ common 包 |
| PLT-T1.006 | 数据库启动初始化（Flyway 双库 migration） | app | T1.003 | ✅ Flyway 2 migration × 2 库 |
| PLT-T1.007 | Jackson + Web + CORS + 异常处理配置 | app | 无 | ✅ WebMvcConfig |
| PLT-T1.008 | WorklineOrchestrator 骨架（七阶段方法签名） | app | T1.005 | ✅ plat/workline |
| PLT-T1.009 | 单元测试基座（JUnit5 + Mockito + H2 双库） | app | T1.003 | ✅ BaseTest + H2 × 2 |

---

# 三、Phase 2 — 七模型 YAML 建模（7 任务，P0）✅

> 目标：M1-M7+MU 七模型 YAML + manifest。**2026-10-13 全部完成。**

| 任务ID | 描述 | 模块 | 状态 |
|--------|------|------|------|
| PLT-T2.001 | M1 对象模型 YAML（ontodb 26 实体 + siisdb 36 实体定义） | app | ✅ m1-object-model.yaml |
| PLT-T2.002 | M2 行为模型 YAML（CRUD + syncTriggers） | app | ✅ m2-behavior-model.yaml |
| PLT-T2.003 | M3 规则模型 YAML（告警 + 垃圾检验 + 升级规则） | app | ✅ m3-rule-model.yaml |
| PLT-T2.004 | M5 主体模型 YAML（RBAC + ABAC + 三级架构） | app | ✅ m5-actor-model.yaml |
| PLT-T2.005 | M6 流程模型 YAML（七阶段 + 汇聚流程） | app | ✅ m6-flow-model.yaml |
| PLT-T2.006 | M7 报表模型 YAML（7 维分析 + 报告模板） | app | ✅ m7-report-model.yaml |
| PLT-T2.007 | MU 界面模型 YAML + manifest.json | app | ✅ mu-ui-model.yaml + manifest.json |

---

# 四、Phase 3 — ontology 域 CRUD（onto_ontology_* 9 表，11 任务，P0）✅

> 目标：`onto_ontology_*` 9 表 CRUD + `onto_ontology_type` 种子数据 + 综合查询 + 对齐服务。**遵循 ADR-001 路线 A（专表 + onto_ontology_type 元数据注册表）**。**2026-10-13 全部完成，17 测试通过。**

| 任务ID | 描述 | 实体 | 端点 | 状态 |
|--------|------|------|------|------|
| PLT-T3.001 | OntologyVersion 实体 + CRUD | onto_ontology_version | /api/ontology/ontologyVersion/* | ✅ |
| PLT-T3.002 | OntologyEntity 实体 + CRUD + 过滤 | onto_ontology_entity | /api/ontology/onto_ontology_entity?type=&lang= | ✅ |
| PLT-T3.003 | OntologyTerm 实体 + CRUD | onto_ontology_term | /api/ontology/onto_ontology_term/* | ✅ |
| PLT-T3.004 | OntologyEventType 实体 + CRUD | onto_ontology_event_type | /api/ontology/onto_ontology_event_type/* | ✅ |
| PLT-T3.005 | OntologyLocation 实体 + CRUD | onto_ontology_location | /api/ontology/onto_ontology_location/* | ✅ |
| PLT-T3.006 | OntologyI18n 实体 + CRUD（多语种翻译） | onto_ontology_i18n | /api/ontology/onto_ontology_i18n/* | ✅ |
| PLT-T3.007 | OntologyType 元数据注册表 + 种子数据 | onto_ontology_type | /api/ontology/onto_ontology_type/* | ✅ ADR-001 路线 A |
| PLT-T3.008 | OntologyAttribute 实体 + CRUD | onto_ontology_attribute | /api/ontology/onto_ontology_attribute/* | ✅ |
| PLT-T3.009 | OntologyRelation 实体 + CRUD | onto_ontology_relation | /api/ontology/onto_ontology_relation/* | ✅ |
| PLT-T3.010 | 本体综合查询 POST /api/ontology/query + 关键词搜索 GET /api/ontology/search | — | 跨实体自然语言查询 + 关键词搜索 | ✅ |
| PLT-T3.011 | OntologyMatchService（实体对齐） + 实体升级 POST /api/ontology/entity/promote | — | match(name, type) → entityId；discovered → ontology | ✅ |

---

# 五、Phase 4 — track 域 CRUD（SIIS 项目层，12 任务，P0）✅

> 目标：11 个 track 实体 CRUD + 图谱查询 + 导入导出。**任务详尽清单见 `siis-platform-tasks.md`**。**2026-10-13 全部完成。**

| 任务ID | 描述 | 实体 | 端点 | 状态 |
|--------|------|------|------|------|
| PLT-T4.001 | Article 实体 + CRUD + 过滤 | gwy_track_article | /api/track/gwy_track_article?topicId=&lang= | ✅ |
| PLT-T4.002 | CrawlSource 实体 + CRUD | gwy_track_crawl_source | /api/track/crawlSource/* | ✅ |
| PLT-T4.003 | CrawlSubscription 实体 + CRUD | gwy_track_crawl_subscription | /api/track/crawlSubscription/* | ✅ |
| PLT-T4.004 | CrawlTask 实体 + CRUD + 执行/状态 | gwy_track_crawl_task | /api/track/crawlTask/{id}/execute, /status | ✅ |
| PLT-T4.005 | CrawlKeywordGroup 实体 + CRUD | gwy_track_crawl_keyword_group | /api/track/crawlKeywordGroup/* | ✅ |
| PLT-T4.006 | DataImport 实体 + CRUD + 上传 | gwy_track_data_import | /api/track/dataImport/upload | ✅ |
| PLT-T4.007 | DataExport 实体 + CRUD + 下载 | gwy_track_data_export | /api/track/dataExport/{id}/download | ✅ |
| PLT-T4.008 | KgNode 实体 + CRUD + 邻居查询 | gwy_track_kg_node | /api/track/kgNode/{id}/neighbors?depth= | ✅ |
| PLT-T4.009 | KgEdge 实体 + CRUD | gwy_track_kg_edge | /api/track/kgEdge/* | ✅ |
| PLT-T4.010 | KgBuildLog 实体 + CRUD | gwy_track_kg_build_log | /api/track/kgBuildLog/* | ✅ |
| PLT-T4.011 | NerElement 实体 + CRUD | gwy_track_ner_element | /api/track/nerElement/* | ✅ |
| PLT-T4.012 | TrackController 注册全部 11 实体 | — | OntologyController 组装 | ✅ |

---

# 六、Phase 5 — analyze 域 CRUD（SIIS 项目层，10 任务，P0）✅

> 目标：7 个 analyze 实体 CRUD + NLP + 关系 + 告警。**任务详尽清单见 `siis-platform-tasks.md`**。**2026-10-13 全部完成。**

| 任务ID | 描述 | 实体 | 端点 | 状态 |
|--------|------|------|------|------|
| PLT-T5.001 | TrackTopic 实体 + CRUD + 激活 | gwy_analyze_track_topic | /api/analyze/trackTopic/{id}/activate | ✅ |
| PLT-T5.002 | TrackElement 实体 + CRUD | gwy_analyze_track_element | /api/analyze/trackElement/* | ✅ |
| PLT-T5.003 | NlpEntity 实体 + CRUD + 按文章查 | gwy_analyze_nlp_entity | /api/analyze/nlpEntity?articleId= | ✅ |
| PLT-T5.004 | NlpTriple 实体 + CRUD | gwy_analyze_nlp_triple | /api/analyze/nlpTriple/* | ✅ |
| PLT-T5.005 | NlpSentiment 实体 + CRUD | gwy_analyze_nlp_sentiment | /api/analyze/nlpSentiment/* | ✅ |
| PLT-T5.006 | AlertRule 实体 + CRUD + 测试 | gwy_analyze_alert_rule | /api/analyze/alertRule/{id}/test | ✅ |
| PLT-T5.007 | BilateralRelation 实体 + CRUD + 按主题查 | onto_global_bilateral_relation | /api/analyze/bilateralRelation?topicId= | ✅ |
| PLT-T5.008 | NLP 抽取 API POST /api/analyze/nlp/extract | — | LLM 赋能 NLP | ✅ |
| PLT-T5.009 | 关系计算服务 RelationAnalyzer | — | BilateralRelation 自动计算 | ✅ |
| PLT-T5.010 | 告警引擎 AlertEngine | — | AlertRule 评估 → AlertRecord | ✅ |

---

# 七、Phase 6 — display 域 CRUD（onto_report_* 7 表，12 任务，P0）✅

> 目标：`onto_report_*` 7 表 CRUD + 报告 + 推送 + 推荐。**2026-10-13 全部完成。**

| 任务ID | 描述 | 实体 | 端点 | 状态 |
|--------|------|------|------|------|
| PLT-T6.001 | RptTemplate 实体 + CRUD | onto_report_rpt_template | /api/display/rptTemplate/* | ✅ |
| PLT-T6.002 | RptReport 实体 + CRUD + 生成 + 分节 | onto_report_rpt_report | /api/display/rptReport/generate, /sections | ✅ |
| PLT-T6.003 | AlertRecord 实体 + CRUD + 过滤 | onto_report_alert_record | /api/display/alertRecord?topicId=&level= | ✅ |
| PLT-T6.004 | InformationPublish 实体 + CRUD + 发布 | onto_report_information_publish | /api/display/informationPublish/{id}/publish | ✅ |
| PLT-T6.005 | PushRecord 实体 + CRUD | onto_report_push_record | /api/display/pushRecord/* | ✅ |
| PLT-T6.006 | RecommendationRecord 实体 + CRUD + 推荐 | onto_report_recommendation_record | /api/display/recommendation?userId= | ✅ |
| PLT-T6.007 | SearchIndex 实体 + CRUD + 重建 | onto_report_search_index | /api/display/searchIndex/rebuild | ✅ |
| PLT-T6.008 | ConferenceInfo 实体 + CRUD（SIIS 项目层） | gwy_display_conference_info | /api/display/conferenceInfo/* | ✅ |
| PLT-T6.009 | ColumnManage 实体 + CRUD（SIIS 项目层） | gwy_display_column_manage | /api/display/columnManage/* | ✅ |
| PLT-T6.010 | UserModuleConfig 实体 + CRUD（SIIS 项目层） | gwy_display_user_module_config | /api/display/userModuleConfig/* | ✅ |
| PLT-T6.011 | 报告生成服务 ReportGenerator（LLM 赋能） | — | 7 section 自然语言生成 | ✅ |
| PLT-T6.012 | DisplayController 注册全部实体 | — | DisplayController 组装 | ✅ |

---

# 八、Phase 7 — workspace 域 CRUD（onto_common_* 4 表，8 任务，P0）✅

> 目标：`onto_common_*` 4 表 CRUD + 知识沉淀 + 概览。**2026-10-13 全部完成。**

| 任务ID | 描述 | 实体 | 端点 | 状态 |
|--------|------|------|------|------|
| PLT-T7.001 | DiscoveredEntity 实体 + CRUD | onto_common_discovered_entity | /api/workspace/discoveredEntity/* | ✅ |
| PLT-T7.002 | TenantRelationAsset 实体 + CRUD | onto_common_tenant_relation_asset | /api/workspace/tenantRelationAsset/* | ✅ |
| PLT-T7.003 | PersonalVectorIndex 实体 + CRUD | onto_common_personal_vector_index | /api/workspace/personalVectorIndex/* | ✅ |
| PLT-T7.004 | LlmChatHistory 实体 + CRUD | onto_common_llm_chat_history | /api/workspace/llmChatHistory/* | ✅ |
| PLT-T7.005 | 工作台概览 GET /api/workspace/overview | — | 租户工作台摘要 | ✅ |
| PLT-T7.006 | 知识资产查询 GET /api/workspace/knowledge | — | 沉淀知识资产 | ✅ |
| PLT-T7.007 | 知识沉淀仪表板 GET /api/workspace/knowledge/dashboard | — | 知识增长曲线 | ✅ |
| PLT-T7.008 | 跨主题知识复用建议 | — | 新主题自动建议 | ✅ |

---

# 九、Phase 8 — Auth + 系统管理（onto_sys_user，6 任务，P0）✅

> sys_user + JWT + 角色权限。**2026-10-14 全部完成，65 测试通过。**

| 任务ID | 描述 | 状态 |
|--------|------|------|
| PLT-T8.001 | onto_sys_user 表（+tenant_id +role_level） | ✅ DDL 已有 |
| PLT-T8.002 | 登录 POST /api/auth/login（JWT 签发） | ✅ JwtUtil + BCrypt |
| PLT-T8.003 | 注册 POST /api/auth/register | ✅ 默认访客角色 |
| PLT-T8.004 | 用户信息 + 用户管理 CRUD | ✅ /api/auth/user/* |
| PLT-T8.005 | TenantContext + JWT 过滤器 + tenant_id 隔离 | ✅ JwtAuthenticationFilter |
| PLT-T8.006 | @RequiresRole 注解 + 角色权限校验 | ✅ RoleInterceptor |

---

# 十、Phase 9 — 工作流编排（6 任务，P0）✅

> WorklineOrchestrator 七阶段。**2026-10-14 全部完成。**

| 任务ID | 描述 | 状态 |
|--------|------|------|
| PLT-T9.001 | WorklineOrchestrator 七阶段实现 | ✅ 各阶段有实际DB查询逻辑 |
| PLT-T9.002 | 主题激活 POST /api/workline/topic/{id}/activate | ✅ |
| PLT-T9.003 | 工作流状态/进度查询 | ✅ /status, /progress |
| PLT-T9.004 | 单阶段触发（NLP/建图/分析/报告） | ✅ /phase/{phase} |
| PLT-T9.005 | 自动阶段流转（事件驱动） | ✅ executeUntil + executeAll |
| PLT-T9.006 | 七阶段定义查询 GET /api/workline/stages | ✅ |

---

# 十一、Phase 10 — 互联网爬取引擎（7 任务，P0）✅

> CrawlEngine/调度/限流/多源。**2026-10-14 全部完成。**

| 任务ID | 描述 | 状态 |
|--------|------|------|
| PLT-T10.001 | CrawlEngine 核心（Jsoup + HttpClient） | ✅ Java HttpClient + Jsoup HTML解析 |
| PLT-T10.002 | 多语种源配置 + 关键词驱动 | ✅ lang 参数支持 |
| PLT-T10.003 | 爬虫调度（ONCE/HOURLY/DAILY/WEEKLY） | ✅ CrawlScheduler @Scheduled |
| PLT-T10.004 | 爬虫限流（1 req/sec per source） | ✅ RateLimiter |
| PLT-T10.005 | 爬虫状态查询 | ✅ /api/crawl/task/{id}/status |
| PLT-T10.006 | 爬虫失败处理（错误日志 + 继续） | ✅ error_msg + retry_count |
| PLT-T10.007 | 爬取完成自动入库（gwy_track_article + nlpStatus=PENDING） | ✅ saveArticle 去重 |

---

# 十二、Phase 11 — 多语种 NLP（8 任务，P0）✅

> 四语种策略/图谱归一化。**2026-10-14 全部完成。**

| 任务ID | 描述 | 状态 |
|--------|------|------|
| PLT-T11.001 | ZhNlpStrategy（中文 NLP） | ✅ 词典+正则模式匹配 |
| PLT-T11.002 | EnNlpStrategy（英文 NLP） | ✅ 大写字母+词典 |
| PLT-T11.003 | FrNlpStrategy（法文 NLP） | ✅ 法语变体+词典 |
| PLT-T11.004 | ArNlpStrategy（阿拉伯文 NLP） | ✅ 阿拉伯字符模式 |
| PLT-T11.005 | 多语种本体术语匹配 | ✅ OntologyMatcher i18n+直接 |
| PLT-T11.006 | 多语种情感分析 | ✅ 四语种正面/负面词典 |
| PLT-T11.007 | 跨语种知识图谱归一化（nodeNameZh） | ✅ KgNormalizer i18n翻译 |
| PLT-T11.008 | NLP 完成自动触发图谱构建 | ✅ triggerKgConstruction |

---

# 十三、Phase 12 — 三级知识架构 + 应用层聚合（onto_global_* 5 表，9 任务，P0）✅

> 目标：`onto_global_*` 5 表 + AggregationService 应用层聚合（**替代 v_unified_kg 视图**）+ v_unified_monitor 视图 + 主任角色。**2026-10-14 全部完成，15 测试通过。**

| 任务ID | 描述 | 状态 |
|--------|------|------|
| PLT-T12.001 | onto_global_* 5 表 DDL + 去重索引（discovered_entity / bilateral_relation / kg_node / kg_edge / aggregation_log） | ✅ DDL 已有 |
| PLT-T12.002 | v_unified_monitor 视图（统一监测层） + v_unified_kg 视图保留向后兼容 | ✅ DDL 已有 |
| PLT-T12.003 | AggregationService 应用层聚合服务骨架（替代 v_unified_kg 视图查询） | ✅ 应用层聚合 |
| PLT-T12.004 | 发现实体汇聚实现（去重 + sum mentionCount） | ✅ ON DUPLICATE KEY UPDATE |
| PLT-T12.005 | 双边关系 + 知识图谱汇聚实现 | ✅ country_a/b 映射 + 唯一键 |
| PLT-T12.006 | 定时汇聚调度（每小时） | ✅ AggregationScheduler @Scheduled |
| PLT-T12.007 | 事件触发增量汇聚 | ✅ aggregateIncremental(tenantId) |
| PLT-T12.008 | 主任角色 + 权限实现（role_level + AccessFilter） | ✅ @RequiresRole(0) |
| PLT-T12.009 | 汇聚 API + 手动触发 + 日志查询 | ✅ AggregationController + GlobalKnowledgeController |

---

# 十四、Phase 13 — Plugin Tool（15 工具，14 任务，P0）✅

> 15 个原子工具 + AdminApiClient。**2026-10-14 全部完成，34 测试通过。**

| 任务ID | 描述 | 状态 |
|--------|------|------|
| PLT-T13.001 | AdminApiClient 封装（HTTP + JWT 透传） | ✅ GET/POST/PUT/DELETE + authToken |
| PLT-T13.002 | ontology_query + ontology_search 工具 | ✅ 重构为 BaseOntologyTool |
| PLT-T13.003 | kg_query + kg_search 工具 | ✅ KgQueryTool + KgSearchTool |
| PLT-T13.004 | relation_analysis + alert_query 工具 | ✅ RelationAnalysisTool + AlertQueryTool |
| PLT-T13.005 | topic_create 工具（LLM 赋能） | ✅ TopicCreateTool |
| PLT-T13.006 | crawl_execute 工具 | ✅ CrawlExecuteTool（5 action） |
| PLT-T13.007 | nlp_extract 工具（LLM 赋能） | ✅ NlpExtractTool（6 action） |
| PLT-T13.008 | report_generate 工具（LLM 赋能） | ✅ ReportGenerateTool |
| PLT-T13.009 | ontology_promote + workspace_overview 工具 | ✅ OntologyPromoteTool + WorkspaceOverviewTool |
| PLT-T13.010 | knowledge_aggregate + director_overview 工具 | ✅ KnowledgeAggregateTool + DirectorOverviewTool |
| PLT-T13.011 | DshOntologyPlugin 注册 15 工具 | ✅ tools() 返回 15 工具 |
| PLT-T13.012 | Plugin 单元测试 | ✅ 34 测试（参数化 + 权限） |
| PLT-T13.013 | Plugin 集成测试（Harness 加载） | ✅ ServiceLoader 发现验证 |
| PLT-T13.014 | 主任工具权限校验 | ✅ knowledge_aggregate/director_overview 标记主任 |

---

# 十五、Phase 14 — Skill + Agent（8 任务，P0）✅

> 4 Skill + 3 Agent。**2026-10-14 全部完成，18 测试通过。**

| 任务ID | 描述 | 状态 |
|--------|------|------|
| PLT-T14.001 | hotspot-analysis Skill（SKILL.md） | ✅ 爬取→NLP→图谱→报告四步法 |
| PLT-T14.002 | topic-creation + report-generation Skill | ✅ 主题创建 + 报告生成 |
| PLT-T14.003 | knowledge-extraction Skill | ✅ NLP→本体匹配→晋升→图谱更新 |
| PLT-T14.004 | research-assistant Agent | ✅ role_level=1，13 工具，4 Skill |
| PLT-T14.005 | topic-creator Agent | ✅ role_level=1，5 工具，2 Skill |
| PLT-T14.006 | director-assistant Agent | ✅ role_level=0，15 工具，4 Skill |
| PLT-T14.007 | Skill + Agent 集成测试 | ✅ 18 测试（frontmatter + 工具 + 权限） |
| PLT-T14.008 | 主任决策主线验证 | ✅ 汇聚→概览→视图查询验证 |

---

# 十六、Phase 15 — 集成测试（7 任务，P2）✅

> 端到端 + Agent 对话 + 主任决策。**2026-10-14 全部完成，7 测试通过。**

| 任务ID | 描述 | 状态 |
|--------|------|------|
| PLT-T15.001 | 端到端：中东热点多语种分析 | ✅ 主题→文章→NLP→实体查询 |
| PLT-T15.002 | 端到端：知识沉淀与升级 | ✅ 发现实体→晋升 API |
| PLT-T15.003 | 端到端：多租户隔离 | ✅ 租户1/2 数据隔离验证 |
| PLT-T15.004 | 端到端：AI 对话追问 | ✅ 本体列表→搜索→查询 |
| PLT-T15.005 | 端到端：七模型 YAML 驱动验证 | ✅ 4 分类 CRUD 查询 |
| PLT-T15.006 | 端到端：三级知识架构 + 应用层聚合 | ✅ 汇聚→日志→AggregationService |
| PLT-T15.007 | 端到端：主任决策主线 | ✅ 登录→概览→汇聚→视图 |

---

# 十七、Phase 16 — P0 修复（双库架构对齐，6 任务，P0）✅

> 目标：v0.2.1 双库架构对齐所需的 6 项 P0 修复。**2026-10-18 全部完成。**

| 任务ID | 描述 | 关联 | 状态 |
|--------|------|------|------|
| PLT-T16.001 | 权限 fail-closed：@PublicApi + @RequiresRole + RoleInterceptor，123/123 端点全覆盖（121 @RequiresRole + 2 @PublicApi） | ontology-platform-spec.md | ✅ 123/123 端点全覆盖 |
| PLT-T16.002 | 列名注入防护：WRITABLE_FIELDS 白名单 + sanitizeRows（BaseRepository 写操作字段过滤） | ontology-platform-design.md | ✅ WRITABLE_FIELDS 白名单 |
| PLT-T16.003 | 共享层唯一化：双库分离（ontodb / siisdb）+ v_unified_kg 改 AggregationService 应用层聚合 | ONTO_DB_DESIGN.md | ✅ 双库 + 应用层聚合 |
| PLT-T16.004 | ingestion 降级：onto_ingestion_* 6 表标记为规划中（暂未实施） | ONTO_DB_DESIGN.md | ✅ 标记规划中 |
| PLT-T16.005 | ADR-001 存储范式：路线 A（专表 + onto_ontology_type 元数据注册表）落地 | ADR-001-ontology-storage-paradigm.md | ✅ 路线 A 落地 |
| PLT-T16.006 | API contract：123 端点代码反向生成 contract 文档（OpenAPI 风格） | ontology-platform-design.md | ✅ 123 端点 contract |

---

# 十八、Controller → Phase 映射

| Controller | Phase | 端点数 | 路径前缀 | 数据库 |
|-----------|-------|--------|---------|--------|
| OntologyCrudController | Phase 3 | 17 | /api/ontology | ontodb |
| TrackController | Phase 4 | 11 | /api/track | siisdb |
| CrawlController | Phase 10 | 5 | /api/crawl | siisdb |
| AnalyzeController | Phase 5 | 11 | /api/analyze | siisdb / ontodb |
| NlpController | Phase 11 | 6 | /api/nlp | siisdb |
| DisplayController | Phase 6 | 11 | /api/display | ontodb / siisdb |
| WorkspaceController | Phase 7 | 11 | /api/workspace | ontodb |
| AuthController | Phase 8 | 4 | /api/auth | ontodb |
| SysUserController | Phase 8 | 6 | /api/auth/user | ontodb |
| OntologyController | Phase 3/9 | 7 | /api/ontologies | ontodb |
| WorklineController | Phase 9 | 7 | /api/workline | siisdb |
| AggregationController | Phase 12 | 5 | /api/aggregation | ontodb |
| GlobalKnowledgeController | Phase 12 | 5 | /api/global | ontodb |
| PublicApiController | Phase 16 | 2 | /api/public | ontodb |
| RoleInterceptor（全局） | Phase 8/16 | — | — | ontodb |
| **合计** | — | **123** | — | 双库 |

> **权限分布**：121 @RequiresRole（fail-closed）+ 2 @PublicApi（显式公开），由 RoleInterceptor 统一拦截。

---

# 十九、依赖关系图

```
Phase 1 (数据基座, 双库 ontodb 26 表 + siisdb 36 表 + 2 视图)
  ├──→ Phase 2 (七模型 YAML)
  ├──→ Phase 8 (Auth + onto_sys_user)
  │      └──→ Phase 3 (ontology 域, onto_ontology_* 9 表 + onto_ontology_type 种子)
  │             └──→ Phase 4 (track 域, siisdb)
  │                    └──→ Phase 10 (爬取引擎)
  │                           └──→ Phase 11 (多语种 NLP)
  │                                  └──→ Phase 5 (analyze 域, siisdb / ontodb)
  │                                         └──→ Phase 6 (display 域, onto_report_* + siisdb)
  │                                                └──→ Phase 7 (workspace 域, onto_common_*)
  ├──→ Phase 9 (工作流编排) ←── Phase 3,4,5,6,7
  ├──→ Phase 12 (三级架构 + onto_global_* 5 表 + AggregationService 应用层聚合) ←── Phase 1, 7, 8
  └──→ Phase 13 (15 Tool) ←── Phase 3,4,5,6,7,9,12
        └──→ Phase 14 (4 Skill + 3 Agent) ←── Phase 13, 12
               └──→ Phase 15 (集成测试) ←── ALL
                      └──→ Phase 16 (P0 修复：权限/列名/共享层/ingestion/ADR/API contract) ←── ALL
```

---

# 二十、与 v2.0 对比

| 对比项 | v0.2.1 双库架构（本文件） | v2.0 单库架构 |
|--------|--------------------------|---------------|
| 任务数 | 140 | 134 |
| 数据库架构 | 双库（ontodb 26 表 + siisdb 36 表） | 单库（52 表） |
| 总表数 | 62（26 + 36） | 52 |
| 视图 | 2（v_unified_monitor + v_unified_kg 改应用层聚合） | 2（v_unified_monitor + v_unified_kg 视图） |
| 共享层 | onto_ 前缀，跨项目复用（siis/factory/ecommerce/city-ops） | gwy_ 前缀，单项目 |
| API 端点 | 123（121 @RequiresRole + 2 @PublicApi） | 106 |
| 权限模型 | fail-closed + @PublicApi + @RequiresRole + RoleInterceptor | @RequiresRole |
| 列名注入防护 | WRITABLE_FIELDS 白名单 + sanitizeRows | 无 |
| ontology 域 | onto_ontology_* 9 表 + onto_ontology_type 种子（ADR-001 路线 A） | gwy_ontology_* 13 实体 |
| 三级架构聚合 | AggregationService 应用层聚合 | v_unified_kg 视图 |
| ingestion | onto_ingestion_* 6 表标记规划中 | 已实施 |
| ADR | ADR-001 存储范式（路线 A） | 无 |
| API contract | 123 端点代码反向生成 | 无 |
| Controller | 15 个（含 PublicApiController + RoleInterceptor） | 13 个 |
| Plugin Tool | 15 | 15 |
| Skill / Agent | 4 / 3 | 4 / 3 |
| 测试 | 100（dsh-onto-app 48 + SIIS 52） | 167（app 115 + plugin 52） |
| Phase | 16 个（新增 Phase 16 P0 修复） | 15 个 |

---

# 二十一、附录：Plugin Tool 详细清单方案（21 工具）

> **日期**：2026-10-18 ｜ **状态**：✅ 已实现（Phase 13 完成，15 工具注册）
>
> **背景**：当前 `dsh-java-ontology-plugin` 已实现 15 个工具（ontology_list/ontology_query/ontology_search + 12 新增），回调 app 的 Admin API。实际实现采用语义化工具命名（kg_query/relation_analysis 等）而非附录中的 CRUD 命名（track_article_list 等），工具数 15 而非原规划 21。
>
> **设计原则**：
> 1. **只读优先**：18 个新增工具全部为只读（GET/POST query），写操作工具作为可选后续
> 2. **一工具一端点**：每个工具对应一个 HTTP 回调端点，参数透传
> 3. **命名规范**：`{域}_{动作}`（如 `track_article_list`），与 mall-plugin 风格一致
> 4. **参数 schema**：使用 JSON Schema 描述参数，支持分页/过滤/排序
> 5. **返回格式**：统一 `{code, message, data}` 结构，data 为对象或分页列表

## 21.1 已有工具（3 个，已实现）

| 序号 | 工具名 | HTTP 回调 | 参数 | 说明 |
|------|--------|-----------|------|------|
| 1 | `ontology_list` | GET /api/ontologies | type, page, size | 列出本体实体（country/org/person/...） |
| 2 | `ontology_query` | POST /api/ontologies/query | query, filters, page, size | 综合查询本体实体 |
| 3 | `ontology_search` | GET /api/ontologies/search | keyword, type | 关键词搜索本体 |

## 21.2 新增只读工具（18 个，原规划，实际以语义化命名实现 12 个）

### ontology 域（2 个）

| 序号 | 工具名 | HTTP 回调 | 参数 | 说明 |
|------|--------|-----------|------|------|
| 4 | `ontology_get` | GET /api/ontologies/{type}/{id} | type, id | 获取单个本体实体详情 |
| 5 | `ontology_i18n_list` | GET /api/ontologies/{type}/{id}/i18n | type, id, lang | 列出实体多语种翻译 |

### track 域（4 个）

| 序号 | 工具名 | HTTP 回调 | 参数 | 说明 |
|------|--------|-----------|------|------|
| 6 | `track_article_list` | GET /api/track/gwy_track_article | tenantId, sourceId, status, page, size | 列出监测文章 |
| 7 | `track_article_get` | GET /api/track/gwy_track_article/{id} | id | 获取文章详情 |
| 8 | `track_source_list` | GET /api/track/gwy_track_crawl_source | status, page, size | 列出爬取源 |
| 9 | `track_topic_list` | GET /api/track/gwy_track_topic | tenantId, page, size | 列出监测主题 |

### analyze 域（4 个）

| 序号 | 工具名 | HTTP 回调 | 参数 | 说明 |
|------|--------|-----------|------|------|
| 10 | `analyze_topic_list` | GET /api/analyze/gwy_analyze_track_topic | tenantId, status, page, size | 列出分析主题 |
| 11 | `analyze_topic_get` | GET /api/analyze/gwy_analyze_track_topic/{id} | id | 获取分析主题详情 |
| 12 | `analyze_alert_rule_list` | GET /api/analyze/gwy_analyze_alert_rule | tenantId, enabled, page, size | 列出告警规则 |
| 13 | `analyze_alert_record_list` | GET /api/analyze/gwy_analyze_alert_record | tenantId, ruleId, status, page, size | 列出告警记录 |

### display 域（3 个）

| 序号 | 工具名 | HTTP 回调 | 参数 | 说明 |
|------|--------|-----------|------|------|
| 14 | `display_report_list` | GET /api/display/onto_report_rpt_report | tenantId, status, page, size | 列出报告 |
| 15 | `display_report_get` | GET /api/display/onto_report_rpt_report/{id} | id | 获取报告详情 |
| 16 | `display_alert_list` | GET /api/display/onto_report_alert_record | tenantId, status, page, size | 列出展示告警 |

### workspace 域（3 个）

| 序号 | 工具名 | HTTP 回调 | 参数 | 说明 |
|------|--------|-----------|------|------|
| 17 | `workspace_entity_list` | GET /api/workspace/onto_common_discovered_entity | tenantId, entityType, status, page, size | 列出工作空间发现实体 |
| 18 | `workspace_entity_get` | GET /api/workspace/onto_common_discovered_entity/{id} | id | 获取工作空间实体详情 |
| 19 | `workspace_chat_history` | GET /api/workspace/onto_common_llm_chat_history | tenantId, sessionId, page, size | 列出 LLM 对话历史 |

### global 域（2 个）

| 序号 | 工具名 | HTTP 回调 | 参数 | 说明 |
|------|--------|-----------|------|------|
| 20 | `global_knowledge_search` | GET /api/global/knowledge/search | keyword, type, lang, page, size | 搜索全局知识库（汇聚层，AggregationService） |
| 21 | `director_overview` | GET /api/global/director/overview | date, region | 主任决策概览（仅 SUPER_ADMIN） |

## 21.3 可选写操作工具（5 个，后续评估）

> 写操作工具需额外评估权限校验和审计日志，作为 Phase 13 后续扩展。

| 工具名 | HTTP 回调 | 说明 |
|--------|-----------|------|
| `track_article_create` | POST /api/track/gwy_track_article | 创建监测文章 |
| `analyze_topic_create` | POST /api/analyze/gwy_analyze_track_topic | 创建分析主题 |
| `analyze_alert_rule_create` | POST /api/analyze/gwy_analyze_alert_rule | 创建告警规则 |
| `display_report_generate` | POST /api/display/onto_report_rpt_report/generate | 生成报告（LLM 赋能） |
| `workspace_entity_promote` | POST /api/workspace/onto_common_discovered_entity/promote | 实体升级（discovered → ontology） |

## 21.4 实现计划

| 步骤 | 内容 | 对应任务 |
|------|------|----------|
| 1 | AdminApiClient 封装（HTTP + JWT 透传） | PLT-T13.001 |
| 2 | 实现 18 个只读工具类（每个工具一个 Java 类） | PLT-T13.002~010 扩展 |
| 3 | DshOntologyPlugin 注册 15 工具 | PLT-T13.011 扩展 |
| 4 | Plugin 单元测试（Mock AdminApiClient） | PLT-T13.012 |
| 5 | Plugin 集成测试（Harness 加载 + HTTP 回调） | PLT-T13.013 |
| 6 | 主任工具权限校验（director_overview 仅 SUPER_ADMIN） | PLT-T13.014 |

## 21.5 工具实现模板

```java
@ToolMethod(name = "track_article_list", description = "列出监测文章，支持按租户/来源/状态过滤")
public ToolResult trackArticleList(
    @ToolParam(name = "tenantId", description = "租户ID") Long tenantId,
    @ToolParam(name = "sourceId", description = "爬取来源ID", required = false) Long sourceId,
    @ToolParam(name = "status", description = "文章状态", required = false) String status,
    @ToolParam(name = "page", description = "页码", defaultValue = "1") int page,
    @ToolParam(name = "size", description = "每页条数", defaultValue = "20") int size
) {
    Map<String, Object> params = new HashMap<>();
    params.put("tenantId", tenantId);
    if (sourceId != null) params.put("sourceId", sourceId);
    if (status != null) params.put("status", status);
    params.put("page", page);
    params.put("size", size);
    return adminApiClient.get("/api/track/gwy_track_article", params);
}
```
