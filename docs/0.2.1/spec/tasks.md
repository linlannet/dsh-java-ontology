# 热点区域关系分析工作主线 — 编码任务清单（tasks.md）

> 本文档基于 `hotspot-analysis-spec.md`（118 条 EARS 需求）与 `hotspot-analysis-design.md`（七阶段流水线）生成，将工作主线拆解为 **131 个原子级编码任务**，按 Phase 1-16 组织。
>
> **v2.0 架构**：dsh-java Harness Agent/Skill/Tool 插件，非传统 CRUD 应用。
>
> - **任务粒度**：每个任务 = 一个可 review 的 PR 单元，0.5-2 天可独立完成、独立提交
> - **技术栈**：Java 17 + Spring Boot 3.5.16 + JdbcTemplate + MySQL + dsh-java Harness Plugin API
> - **根包**：`net.linlan.dsh.ontology`（app）/ `net.linlan.dsh.ontology.plugin`（plugin）
> - **任务编号**：`WL-T{phase}.{序号}`

---

# 一、任务总览

## 1.1 阶段分布

| 阶段 | 优先级 | 任务数 | 目标 | 工作量 |
|------|--------|--------|------|--------|
| Phase 1 — 七模型 YAML 建模 | P0 | 7 | M1-M7+MU YAML + manifest | L |
| Phase 2 — 数据基座 | P0 | 8 | M1→DDL 自动建库 + 通用基类 | M |
| Phase 3 — 阶段 G 本体支撑 | P0 | 10 | 13 实体 CRUD + 综合查询 | L |
| Phase 4 — 阶段 A+B 任务创建+采集 | P0 | 12 | TrackTopic + 关键词组 + 采集 + 文章 | L |
| Phase 5 — 阶段 C NLP 抽取 | P0 | 8 | 实体/三元组/情感抽取 + 编排 | M |
| Phase 6 — 阶段 D 知识图谱 | P0 | 10 | NER + KgNode/KgEdge + 图谱构建 + 查询 | L |
| Phase 7 — 阶段 E 关系分析 | P0 | 8 | 双边关系 + 追踪元素 + 告警 | M |
| Phase 8 — 阶段 F 报告生成 | P0 | 8 | 模板 + 报告 + 告警记录 + 仪表板 | M |
| Phase 9 — 多租户工作台 | P0 | 8 | User/JWT/租户隔离/工作台API | M |
| Phase 10 — 互联网爬取引擎 | P0 | 7 | CrawlEngine/调度/限流/多源 | M |
| Phase 11 — 多语种NLP+知识沉淀 | P0 | 9 | 四语种策略/图谱归一化/沉淀/升级 | L |
| Phase 12 — Plugin Tool 实现 | P0 | 14 | 14 个原子工具 + AdminApiClient | L |
| Phase 13 — Skill + Agent 定义 | P0 | 8 | 4 Skill + 3 Agent + 注册 | M |
| Phase 14 — 三级知识架构+汇聚 | P0 | 9 | 汇聚表/服务/视图/主任角色 | L |
| Phase 15 — 集成测试 | P2 | 7 | 端到端工作主线 + Agent 对话 + 主任决策 | M |
| **合计** | — | **131** | **v2.0 GA（Agent/Skill/YAML + 三级知识架构）** | **8×M + 6×L** |

## 1.2 任务字段说明

- **任务ID**：`WL-T{phase}.{序号}`
- **优先级**：P0（工作主线必需）/ P1（增强）/ P2（可选）
- **模块**：`app` / `plugin`
- **依赖任务**：前置任务ID列表
- **涉及文件**：关键文件路径
- **验收标准**：验证方式

---

# 二、Phase 1 — 数据基座（8 任务，P0）

> 目标：数据库初始化、DataSource 配置、通用基类，为工作主线提供底座。

### WL-T1.001 数据库建表脚本（工作主线核心表）
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：无
- **描述**：编写 `V20261013__workline_schema.sql`，创建工作主线核心表：gwy_analyze_track_topic, gwy_track_crawl_keyword_group, gwy_track_crawl_source, gwy_track_crawl_subscription, gwy_track_crawl_task, gwy_track_article, gwy_track_data_import, gwy_track_data_export, gwy_analyze_nlp_entity, gwy_analyze_nlp_triple, gwy_analyze_nlp_sentiment, gwy_track_ner_element, gwy_track_kg_node, gwy_track_kg_edge, gwy_track_kg_build_log, gwy_ontology_bilateral_relation, gwy_analyze_track_element, gwy_analyze_alert_rule, gwy_display_rpt_template, gwy_display_rpt_report, gwy_display_alert_record, gwy_display_information_publish, gwy_display_push_record, gwy_display_recommendation_record, gwy_display_search_index, gwy_display_conference_info + 13 张本体表 + 10 张三级架构新增表（gwy_common_discovered_entity, gwy_common_tenant_relation_asset, gwy_common_personal_vector_index, gwy_common_llm_chat_history, gwy_global_discovered_entity, gwy_global_bilateral_relation, gwy_global_kg_node, gwy_global_kg_edge, gwy_global_aggregation_log, sys_user 改造）。含主键、索引、公共字段、tenant_id 列。幂等。
- **涉及文件**：`dsh-java-ontology-app/src/main/resources/db/migration/V20261013__workline_schema.sql`
- **验收**：[ ] 全部表建出 [ ] 可重复执行 [ ] 每表含 id/created_at/updated_at/del_flag

### WL-T1.002 DataSource + JdbcTemplate 配置
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T1.001
- **描述**：配置 MySQL DataSource（`spring.datasource.url=jdbc:mysql://localhost:3306/ontology_db`）、HikariCP 连接池、JdbcTemplate Bean、事务管理器。
- **涉及文件**：`config/DataSourceConfig.java`, `application.yml`
- **验收**：[ ] 应用启动无报错 [ ] JdbcTemplate 可查询

### WL-T1.003 通用基类 BaseEntity + PageResult + Result
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：无
- **描述**：BaseEntity（id, createdBy, createdAt, updatedBy, updatedAt, delFlag）、PageResult<T>（分页响应）、Result<T>（统一响应包装 {code, msg, data}）。
- **涉及文件**：`common/BaseEntity.java`, `common/PageResult.java`, `common/Result.java`
- **验收**：[ ] 编译通过 [ ] 单元测试覆盖

### WL-T1.004 通用 BaseController + BaseService 基类
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T1.003
- **描述**：BaseController<T> 提供 CRUD 端点模板（list/get/add/update/delete），BaseService<T> 提供 CRUD 方法模板，BaseRepository<T> 提供 JdbcTemplate 操作模板。
- **涉及文件**：`common/BaseController.java`, `common/BaseService.java`, `common/BaseRepository.java`
- **验收**：[ ] 子类继承后仅需提供 RowMapper + SQL 即可拥有完整 CRUD

### WL-T1.005 数据库启动初始化
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T1.002
- **描述**：实现 `DatabaseInitializer` 在应用启动时执行建表脚本（若 Flyway 未引入则手动执行）。
- **涉及文件**：`config/DatabaseInitializer.java`
- **验收**：[ ] 首次启动建表成功 [ ] 二次启动跳过不报错

### WL-T1.006 Jackson + Web 配置
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：无
- **描述**：配置 Jackson（Long→String 序列化避免前端精度丢失）、CORS、统一异常处理。
- **涉及文件**：`config/WebConfig.java`, `config/JacksonConfig.java`, `common/GlobalExceptionHandler.java`
- **验收**：[ ] API 返回 JSON 格式正确 [ ] 异常返回统一包装

### WL-T1.007 WorklineOrchestrator 骨架
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T1.004
- **描述**：创建 WorklineOrchestrator 空壳，定义七阶段编排方法签名（onTopicActivated / onArticleStored / onNlpCompleted / onKgBuildCompleted / onAnalysisCompleted），暂不实现逻辑。
- **涉及文件**：`workline/WorklineOrchestrator.java`, `workline/WorklineStage.java`
- **验收**：[ ] 编译通过

### WL-T1.008 单元测试基座
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T1.002
- **描述**：配置 JUnit 5 + Mockito + H2 内存数据库（MySQL 兼容模式）测试 DataSource，创建 `BaseRepositoryTest` 基类。
- **涉及文件**：`src/test/java/.../common/BaseRepositoryTest.java`
- **验收**：[ ] 测试可运行 [ ] 内存 DB 初始化成功

---

# 三、Phase 2 — 阶段 G 本体支撑（10 任务，P0）

> 目标：13 个本体实体 CRUD + 综合查询 + 搜索，为工作主线提供知识底座。

### WL-T2.001 Country 实体 + CRUD
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T1.004
- **描述**：Country model + repository + service + controller，端点 `*/api/ontology/country`。
- **涉及文件**：`ontology/model/Country.java`, `ontology/repository/CountryRepository.java`, `ontology/service/CountryService.java`, `ontology/controller/OntologyController.java`（追加 country 端点）
- **验收**：[ ] CRUD 全通 [ ] 按 region 过滤正确

### WL-T2.002 Organization 实体 + CRUD
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T1.004
- **描述**：Organization 全链路，端点 `*/api/ontology/organization`。
- **验收**：[ ] CRUD 全通

### WL-T2.003 Person 实体 + CRUD
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T1.004
- **描述**：Person 全链路，端点 `*/api/ontology/person`。
- **验收**：[ ] CRUD 全通

### WL-T2.004 Hotspot 实体 + CRUD
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T1.004
- **描述**：Hotspot 全链路，端点 `*/api/ontology/hotspot`，支持按 region/riskLevel 过滤。
- **验收**：[ ] CRUD 全通 [ ] 阶段 A 可查询热点列表

### WL-T2.005 Term + EventType 实体 + CRUD
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T1.004
- **描述**：Term 和 EventType 全链路。
- **验收**：[ ] CRUD 全通

### WL-T2.006 Location + Media 实体 + CRUD
- **优先级**：P1 ｜ **模块**：app ｜ **依赖**：WL-T1.004
- **描述**：Location 和 Media 全链路。
- **验收**：[ ] CRUD 全通

### WL-T2.007 Book + Paper + OntologyEntity 实体 + CRUD
- **优先级**：P1 ｜ **模块**：app ｜ **依赖**：WL-T1.004
- **描述**：Book, Paper, OntologyEntity 全链路。
- **验收**：[ ] CRUD 全通

### WL-T2.008 OntologyVersion + NlpVersion 实体 + CRUD
- **优先级**：P2 ｜ **模块**：app ｜ **依赖**：WL-T1.004
- **描述**：OntologyVersion, NlpVersion 全链路。
- **验收**：[ ] CRUD 全通

### WL-T2.009 本体综合查询 POST /api/ontology/query
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T2.001~005
- **描述**：接受自然语言查询，跨所有本体类型搜索，返回匹配实体（含类型和相关度）。
- **涉及文件**：`ontology/service/OntologyQueryService.java`
- **验收**：[ ] 查询"中东"返回相关 Country + Hotspot [ ] 查询"以色列"返回 Country + Person

### WL-T2.010 本体搜索 GET /api/ontology/search + 对齐服务
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T2.009
- **描述**：关键词搜索 + `OntologyMatchService.match(entityName, entityType)` 供阶段 D 调用。
- **涉及文件**：`ontology/service/OntologyMatchService.java`
- **验收**：[ ] match("以色列","GPE") 返回 Country.id [ ] match("未知实体","GPE") 返回 null

---

# 四、Phase 3 — 阶段 A+B 任务创建+数据采集（12 任务，P0）

### WL-T3.001 CrawlKeywordGroup 实体 + CRUD
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T1.004
- **描述**：CrawlKeywordGroup 全链路，keywordsZh/keywordsEn 用 JSON 存储。
- **验收**：[ ] CRUD 全通 [ ] JSON 数组正确序列化

### WL-T3.002 CrawlSource 实体 + CRUD
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T1.004
- **描述**：CrawlSource 全链路。
- **验收**：[ ] CRUD 全通

### WL-T3.003 CrawlSubscription 实体 + CRUD
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T3.002
- **描述**：CrawlSubscription 全链路，关联 sourceId 和 keywordGroupId。
- **验收**：[ ] CRUD 全通

### WL-T3.004 CrawlTask 实体 + CRUD
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T3.002
- **描述**：CrawlTask 全链路，关联 topicId/sourceId/keywordGroupId。
- **验收**：[ ] CRUD 全通

### WL-T3.005 Article 实体 + CRUD
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T1.004
- **描述**：Article 全链路，含 nlpStatus/kgStatus/nlpError 字段，支持按 topicId/language/publishTime 过滤。
- **验收**：[ ] CRUD 全通 [ ] 分页正确

### WL-T3.006 DataImport 实体 + CRUD + 批量导入
- **优先级**：P1 ｜ **模块**：app ｜ **依赖**：WL-T3.005
- **描述**：DataImport 全链路 + `POST /api/track/dataImport` 批量导入文章（JSON/CSV），记录导入日志。
- **验收**：[ ] 批量导入 10 篇文章成功 [ ] 导入日志正确

### WL-T3.007 DataExport 实体 + CRUD + 批量导出
- **优先级**：P1 ｜ **模块**：app ｜ **依赖**：WL-T3.005
- **描述**：DataExport 全链路 + `POST /api/track/dataExport` 按条件导出文章。
- **验收**：[ ] 导出 JSON 格式正确

### WL-T3.008 TrackTopic 实体 + CRUD
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T3.001
- **描述**：TrackTopic 全链路，含 status/articleCount/entityCount/relationCount/reportCount 字段。
- **验收**：[ ] CRUD 全通 [ ] 新建时 status=DRAFT

### WL-T3.009 任务激活编排（A→B）
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T3.008, WL-T3.004, WL-T1.007
- **描述**：实现 `TrackTopicService.activate(topicId)`：校验配置 → 更新 status=ACTIVE → 创建 CrawlTask → 调用 `WorklineOrchestrator.onTopicActivated`。
- **涉及文件**：`analyze/service/TrackTopicService.java`
- **验收**：[ ] 激活后 status=ACTIVE [ ] CrawlTask 自动创建

### WL-T3.010 采集完成回调（B→C 触发）
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T3.004, WL-T3.005, WL-T1.007
- **描述**：实现 `CrawlTaskService.complete(taskId, articles[])`：存储文章（nlpStatus=PENDING）→ 更新任务状态 → 更新 topic.articleCount → 调用 `WorklineOrchestrator.onArticleStored`。
- **验收**：[ ] 文章入库成功 [ ] nlpStatus=PENDING [ ] articleCount 递增

### WL-T3.011 TrackController 仪表板端点
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T3.005
- **描述**：`GET /api/track/dashboard/monitor` 返回监控仪表板 DTO。
- **验收**：[ ] 返回 totalArticles/totalEntities 等统计

### WL-T3.012 阶段 A+B 单元测试
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T3.001~010
- **描述**：为关键词组/采集源/任务/文章/TrackTopic 编写单元测试，覆盖 CRUD + 激活编排 + 采集回调。
- **验收**：[ ] 测试全通过

---

# 五、Phase 4 — 阶段 C NLP 抽取（8 任务，P0）

### WL-T4.001 NlpEntity 实体 + CRUD
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T1.004
- **描述**：NlpEntity 全链路，按 articleId/entityType 过滤。
- **验收**：[ ] CRUD 全通

### WL-T4.002 NlpTriple 实体 + CRUD
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T1.004
- **描述**：NlpTriple 全链路。
- **验收**：[ ] CRUD 全通

### WL-T4.003 NlpSentiment 实体 + CRUD
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T1.004
- **描述**：NlpSentiment 全链路，keyPhrases 用 JSON 存储。
- **验收**：[ ] CRUD 全通

### WL-T4.004 NlpServiceClient 接口 + 规则实现
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T2.010
- **描述**：定义 `NlpServiceClient` 接口 + `RuleBasedNlpClient` 实现（基于本体术语库的规则匹配：用 Term/Country/Organization/Person 的 name 和 keywords 匹配文本中的实体）。
- **涉及文件**：`analyze/service/nlp/NlpServiceClient.java`, `RuleBasedNlpClient.java`
- **验收**：[ ] 对含"以色列"的文本抽取出 GPE 实体 [ ] 对含"拜登"的文本抽取出 PERSON 实体

### WL-T4.005 NLP 抽取编排（C 阶段核心）
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T4.001~004, WL-T1.007
- **描述**：实现 `NlpExtractionService.extractForArticle(articleId)`：加载文章 → 调用 NlpServiceClient 三方法 → 存储结果 → 更新 nlpStatus=COMPLETED → 调用 `WorklineOrchestrator.onNlpCompleted`。
- **涉及文件**：`analyze/service/NlpExtractionService.java`
- **验收**：[ ] 抽取后 nlpStatus=COMPLETED [ ] 实体/三元组/情感均存储

### WL-T4.006 NLP 失败处理
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T4.005
- **描述**：NLP 服务异常时设置 nlpStatus=FAILED + nlpError，不阻塞后续文章。
- **验收**：[ ] 异常时 nlpStatus=FAILED [ ] 其他文章不受影响

### WL-T4.007 HTTP NLP 客户端（可选）
- **优先级**：P1 ｜ **模块**：app ｜ **依赖**：WL-T4.004
- **描述**：`HttpNlpClient` 实现，调用外部 NLP 服务，URL 可配置，通过 `@ConditionalOnProperty` 切换。
- **验收**：[ ] 配置 URL 后使用 HTTP 客户端 [ ] 未配置时使用规则客户端

### WL-T4.008 阶段 C 单元测试
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T4.001~006
- **描述**：NLP 抽取全链路测试，含成功/失败场景。
- **验收**：[ ] 测试全通过

---

# 六、Phase 5 — 阶段 D 知识图谱（10 任务，P0）

### WL-T5.001 NerElement 实体 + CRUD
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T1.004
- **描述**：NerElement 全链路，含 ontologyEntityId 对齐字段。
- **验收**：[ ] CRUD 全通

### WL-T5.002 KgNode 实体 + CRUD
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T1.004
- **描述**：KgNode 全链路，properties/articleIds 用 JSON，按 topicId/nodeType 过滤。
- **验收**：[ ] CRUD 全通

### WL-T5.003 KgEdge 实体 + CRUD
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T5.002
- **描述**：KgEdge 全链路，关联 sourceNodeId/targetNodeId。
- **验收**：[ ] CRUD 全通

### WL-T5.004 KgBuildLog 实体 + CRUD
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T1.004
- **描述**：KgBuildLog 全链路。
- **验收**：[ ] CRUD 全通

### WL-T5.005 图谱构建编排（D 阶段核心）
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T5.001~004, WL-T2.010, WL-T1.007
- **描述**：实现 `KgBuildService.buildForArticle(articleId)`：加载 NlpEntity → 创建/合并 KgNode → 对齐本体 → 加载 NlpTriple → 创建 KgEdge → 更新 NerElement 频率 → 存 KgBuildLog → 更新 kgStatus=COMPLETED → 调用 `WorklineOrchestrator.onKgBuildCompleted`。
- **涉及文件**：`track/service/KgBuildService.java`
- **验收**：[ ] 节点正确创建/合并 [ ] 边正确创建 [ ] ontologyEntityId 对齐成功

### WL-T5.006 图谱查询 — 节点详情
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T5.002
- **描述**：`GET /api/track/dashboard/kg/node/{nodeId}` 返回节点 + 关联本体实体。
- **验收**：[ ] 返回节点属性 + ontologyEntity 信息

### WL-T5.007 图谱查询 — 邻居
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T5.003
- **描述**：`GET /api/track/dashboard/kg/node/{nodeId}/neighbors?depth=1` 返回邻接节点和边。
- **验收**：[ ] depth=1 返回直接邻居 [ ] depth=2 返回二跳邻居

### WL-T5.008 图谱查询 — 搜索
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T5.002
- **描述**：`GET /api/track/dashboard/kg/search` 按 name/type/topicId 搜索，返回节点 + 边数。
- **验收**：[ ] 搜索"以色列"返回匹配节点

### WL-T5.009 图谱统计仪表板
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T5.002, WL-T5.003
- **描述**：`GET /api/track/dashboard/kg` 返回 KG 统计 DTO。
- **验收**：[ ] 返回 totalNodes/totalEdges/nodesByType/edgesByRelationType

### WL-T5.010 阶段 D 单元测试
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T5.001~009
- **描述**：图谱构建 + 查询全链路测试。
- **验收**：[ ] 测试全通过

---

# 七、Phase 6 — 阶段 E 关系分析（8 任务，P0）

### WL-T6.001 BilateralRelation 实体 + CRUD
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T1.004
- **描述**：BilateralRelation 全链路，evidenceArticleIds 用 JSON。
- **验收**：[ ] CRUD 全通

### WL-T6.002 TrackElement 实体 + CRUD
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T1.004
- **描述**：TrackElement 全链路。
- **验收**：[ ] CRUD 全通

### WL-T6.003 AlertRule 实体 + CRUD
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T1.004
- **描述**：AlertRule 全链路，含 conditionExpression/threshold/severity。
- **验收**：[ ] CRUD 全通

### WL-T6.004 双边关系计算（E 阶段核心）
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T6.001, WL-T5.002, WL-T5.003, WL-T1.007
- **描述**：实现 `RelationAnalysisService.computeForTopic(topicId)`：查询国家/组织级 KgNode → 对每对实体聚合 KgEdge → 计算 relationType/relationScore/trend → 存储 BilateralRelation → 更新 TrackElement → 更新 relationCount → 调用 `WorklineOrchestrator.onAnalysisCompleted`。
- **涉及文件**：`analyze/service/RelationAnalysisService.java`
- **验收**：[ ] 正确计算双边关系 [ ] relationScore 在 0-1 范围

### WL-T6.005 追踪元素统计更新
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T6.002
- **描述**：新文章入库时更新 TrackElement（mentionCount/lastMentionTime），未见实体创建新记录。
- **验收**：[ ] mentionCount 正确递增

### WL-T6.006 告警规则评估 + 触发
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T6.003
- **描述**：实现 `AlertService.evaluate(topicId)`：查询 enabled AlertRule → 评估 conditionExpression → 满足条件创建 AlertRecord。
- **涉及文件**：`analyze/service/AlertService.java`, `analyze/service/AlertExpressionEvaluator.java`
- **验收**：[ ] "relationScore > 0.8" 正确评估 [ ] AlertRecord 创建成功

### WL-T6.007 条件表达式引擎
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T6.006
- **描述**：实现简单条件表达式解析器，支持 `field OP value AND/OR field OP value` 格式。
- **验收**：[ ] 支持 > < = >= <= AND OR [ ] 不合法表达式报错

### WL-T6.008 阶段 E 单元测试
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T6.001~007
- **描述**：关系分析 + 告警全链路测试。
- **验收**：[ ] 测试全通过

---

# 八、Phase 7 — 阶段 F 报告生成（8 任务，P0）

### WL-T7.001 RptTemplate 实体 + CRUD
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T1.004
- **描述**：RptTemplate 全链路，sections/chartConfigs 用 JSON。
- **验收**：[ ] CRUD 全通

### WL-T7.002 RptReport 实体 + CRUD
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T1.004
- **描述**：RptReport 全链路，generatedContent/chartData/recommendations 用 JSON/TEXT。
- **验收**：[ ] CRUD 全通

### WL-T7.003 AlertRecord 实体 + CRUD
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T1.004
- **描述**：AlertRecord 全链路，按 topicId/severity/alertTime 过滤。
- **验收**：[ ] CRUD 全通

### WL-T7.004 InformationPublish + PushRecord + RecommendationRecord + SearchIndex + ConferenceInfo 实体 + CRUD
- **优先级**：P1 ｜ **模块**：app ｜ **依赖**：WL-T1.004
- **描述**：display 域剩余 5 个实体全链路。
- **验收**：[ ] CRUD 全通

### WL-T7.005 报告生成服务（F 阶段核心）
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T7.001, WL-T7.002, WL-T6.001, WL-T5.002, WL-T5.003
- **描述**：实现 `ReportService.generate(topicId, templateId)`：加载模板 → 按 section 类型查询数据（OVERVIEW/KEY_ENTITIES/RELATION_MATRIX/KG_SUMMARY/SENTIMENT_TREND/ALERT_SUMMARY/RECOMMENDATIONS）→ 装配 content + chartData → 存储 RptReport → 更新 reportCount。
- **涉及文件**：`display/service/ReportService.java`
- **验收**：[ ] 报告含 7 个 section [ ] generatedContent 非空 [ ] status=COMPLETED

### WL-T7.006 报告生成端点
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T7.005
- **描述**：`POST /api/display/rptReport/generate` 触发报告生成。
- **验收**：[ ] 端点返回报告 ID + 摘要

### WL-T7.007 展示仪表板
- **优先级**：P1 ｜ **模块**：app ｜ **依赖**：WL-T7.002, WL-T7.003
- **描述**：`GET /api/display/dashboard` 返回展示仪表板 DTO。
- **验收**：[ ] 返回 totalReports/totalAlerts/recentReports

### WL-T7.008 阶段 F 单元测试
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T7.001~007
- **描述**：报告生成全链路测试。
- **验收**：[ ] 测试全通过

---

# 九、Phase 8 — 插件工具（6 任务，P0）

### WL-T8.001 AdminApiClient 封装
- **优先级**：P0 ｜ **模块**：plugin ｜ **依赖**：无
- **描述**：封装 HTTP 客户端调用 app REST API，含统一错误处理。
- **涉及文件**：`plugin/client/AdminApiClient.java`
- **验收**：[ ] 可调用 app API [ ] 错误正确包装

### WL-T8.002 ontology_query + ontology_search 工具
- **优先级**：P0 ｜ **模块**：plugin ｜ **依赖**：WL-T8.001
- **描述**：注册 `ontology_query` 和 `ontology_search` 工具到 DshOntologyPlugin。
- **验收**：[ ] 工具可被 Harness 调用 [ ] 返回正确结果

### WL-T8.003 kg_query 工具
- **优先级**：P0 ｜ **模块**：plugin ｜ **依赖**：WL-T8.001
- **描述**：注册 `kg_query` 工具，查询图谱节点 + 邻居。
- **验收**：[ ] 返回节点 + 邻居 + 边

### WL-T8.004 relation_analysis 工具
- **优先级**：P0 ｜ **模块**：plugin ｜ **依赖**：WL-T8.001
- **描述**：注册 `relation_analysis` 工具，查询双边关系。
- **验收**：[ ] 返回 BilateralRelation 列表

### WL-T8.005 report_generate + alert_query 工具
- **优先级**：P0 ｜ **模块**：plugin ｜ **依赖**：WL-T8.001
- **描述**：注册 `report_generate` 和 `alert_query` 工具。
- **验收**：[ ] report_generate 返回报告摘要 [ ] alert_query 返回告警列表

### WL-T8.006 插件注册 + 集成
- **优先级**：P0 ｜ **模块**：plugin ｜ **依赖**：WL-T8.002~005
- **描述**：在 DshOntologyPlugin 中注册全部 6 个工具，确保 Harness 可发现并调用。
- **验收**：[ ] Harness 启动加载插件 [ ] 6 个工具全部注册

---

# 十、Phase 9 — 集成测试（2 任务，P2）

### WL-T9.001 端到端工作主线测试 — 中东热点分析
- **优先级**：P2 ｜ **模块**：app ｜ **依赖**：ALL
- **描述**：编写集成测试覆盖完整工作主线：创建关键词组 → 创建任务 → 激活 → 导入文章 → NLP 抽取 → 图谱构建 → 关系计算 → 告警触发 → 报告生成 → 验证报告内容。
- **涉及文件**：`src/test/java/.../workline/HotspotAnalysisWorklineIT.java`
- **验收**：[ ] 全流程无异常 [ ] 报告含 7 个 section [ ] 双边关系正确计算

### WL-T9.002 端到端工作主线测试 — 俄乌关系追踪
- **优先级**：P2 ｜ **模块**：app ｜ **依赖**：WL-T9.001
- **描述**：第二个场景测试，验证告警规则触发。
- **验收**：[ ] 告警正确触发 [ ] AlertRecord 创建

---

# 十、Phase 10 — 多租户工作台（8 任务，P0）

> 目标：用户注册/登录/JWT、租户上下文、数据隔离、工作台 API。

### WL-T10.001 sys_user 表 + User 实体 + Repository
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T1.002
- **描述**：建 `sys_user` 表（含 tenant_id UUID），User model + repository。
- **涉及文件**：`auth/model/User.java`, `auth/repository/UserRepository.java`
- **验收**：[ ] CRUD 可用 [ ] tenant_id 唯一

### WL-T10.002 JWT 工具 + 认证过滤器
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T10.001
- **描述**：JwtUtil（生成/解析 token，含 userId/tenantId/exp）+ JwtAuthFilter（提取 token → 设置 TenantContext）。
- **涉及文件**：`auth/jwt/JwtUtil.java`, `auth/jwt/JwtAuthFilter.java`
- **验收**：[ ] token 生成/解析正确 [ ] 过滤器设置 TenantContext

### WL-T10.003 注册 + 登录端点
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T10.002
- **描述**：`POST /api/auth/register`（创建 User + 生成 tenantId）+ `POST /api/auth/login`（验证密码 → 返回 JWT）。
- **涉及文件**：`auth/controller/AuthController.java`
- **验收**：[ ] 注册成功返回 tenantId [ ] 登录返回 JWT

### WL-T10.004 TenantContext + BaseRepository 租户隔离
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T10.002
- **描述**：TenantContext（ThreadLocal）+ 修改 BaseRepository，所有查询自动追加 `WHERE tenant_id = ?`，所有写入自动设置 tenant_id。本体 Repository 不隔离。
- **涉及文件**：`common/TenantContext.java`, `common/BaseRepository.java`
- **验收**：[ ] 用户A 查不到用户B 的主题 [ ] 本体数据所有用户可见

### WL-T10.005 工作台概览 API
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T10.004
- **描述**：`GET /api/workspace/overview` 返回当前租户的 {totalTopics, totalArticles, totalReports, totalAlerts, recentTopics, recentReports}。
- **涉及文件**：`workspace/controller/WorkspaceController.java`, `workspace/service/WorkspaceService.java`
- **验收**：[ ] 返回当前租户数据 [ ] 不含其他租户数据

### WL-T10.006 工作台主题列表 API
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T10.005
- **描述**：`GET /api/workspace/topics` 分页返回当前租户的 TrackTopic 列表，支持 status 过滤。
- **验收**：[ ] 分页正确 [ ] 仅返回当前租户主题

### WL-T10.007 UserModuleConfig 实体 + CRUD
- **优先级**：P1 ｜ **模块**：app ｜ **依赖**：WL-T10.004
- **描述**：用户模块配置，按 tenantId 隔离。
- **验收**：[ ] CRUD 全通 [ ] 租户隔离

### WL-T10.008 Phase 10 单元测试
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T10.001~007
- **描述**：注册/登录/隔离/工作台 API 测试，含跨租户隔离验证。
- **验收**：[ ] 测试全通过

---

# 十一、Phase 11 — 互联网爬取引擎（7 任务，P0）

> 目标：内置 Java 爬虫，支持多语种源、关键词驱动搜索、异步调度、限流。

### WL-T11.001 CrawlEngine 核心
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T3.005
- **描述**：CrawlEngine（HttpClient + Jsoup）：加载 CrawlSource + CrawlKeywordGroup → 按 language 选关键词 → 构造搜索 URL → 爬取 → 解析文章列表 → 返回 CrawlResult。
- **涉及文件**：`track/crawl/CrawlEngine.java`
- **验收**：[ ] 能爬取 Google News RSS [ ] 返回文章列表

### WL-T11.002 SearchUrlBuilder 多源支持
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T11.001
- **描述**：实现 GOOGLE_NEWS / BING_NEWS / RSS / CUSTOM 四种 SearchUrlBuilder。
- **涉及文件**：`track/crawl/builder/*.java`
- **验收**：[ ] 四种源 URL 构造正确

### WL-T11.003 爬虫限流
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T11.001
- **描述**：RateLimiter（每源 1 req/sec），CrawlEngine 爬取前 acquire。
- **涉及文件**：`track/crawl/RateLimiter.java`
- **验收**：[ ] 限流生效 [ ] 不超速

### WL-T11.004 异步爬虫调度
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T11.001
- **描述**：CrawlScheduler（@Scheduled 每分钟检查）→ 提交到线程池 → CrawlEngine.execute → 存储文章 → 触发 NLP。
- **涉及文件**：`track/crawl/CrawlScheduler.java`
- **验收**：[ ] 定时触发正确 [ ] 异步执行不阻塞

### WL-T11.005 爬虫执行端点 + 状态查询
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T11.004
- **描述**：`POST /api/track/crawlTask/{id}/execute`（手动触发）+ `GET /api/track/crawlTask/{id}/status`（查询进度）。
- **验收**：[ ] 手动触发成功 [ ] 状态查询返回进度

### WL-T11.006 爬虫失败处理
- **优先级**：P1 ｜ **模块**：app ｜ **依赖**：WL-T11.001
- **描述**：网络错误/源阻塞时记录错误日志，继续其他源，不中断整个任务。
- **验收**：[ ] 单源失败不影响其他源 [ ] 错误日志记录

### WL-T11.007 Phase 11 单元测试
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T11.001~006
- **描述**：爬虫引擎测试（mock HTTP 响应）。
- **验收**：[ ] 测试全通过

---

# 十二、Phase 12 — 多语种 NLP + 知识沉淀（9 任务，P0）

> 目标：多语种 NLP 策略分派、跨语种图谱归一化、知识沉淀与升级。

### WL-T12.001 NlpStrategy 接口 + 语种分派器
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T4.004
- **描述**：NlpStrategy 接口 + NlpStrategyDispatcher（按 article.language 分派，fallback en）。
- **涉及文件**：`analyze/service/nlp/NlpStrategy.java`, `NlpStrategyDispatcher.java`
- **验收**：[ ] zh 文章用 ZhStrategy [ ] 未知语种 fallback en

### WL-T12.002 ZhNlpStrategy + EnNlpStrategy
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T12.001
- **描述**：中文策略（用 Term.termZh/Country.nameZh 匹配）+ 英文策略（用 Term.termEn/Country.nameEn 匹配）。
- **验收**：[ ] 中文文本抽取出中文实体 [ ] 英文文本抽取出英文实体

### WL-T12.003 FrNlpStrategy + ArNlpStrategy
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T12.001
- **描述**：法文策略 + 阿拉伯文策略（用 Term.termFr/termAr 匹配）。
- **验收**：[ ] 法文/阿拉伯文抽取正确

### WL-T12.004 多语种情感词典
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T12.001
- **描述**：加载 sentiment_zh/en/fr/ar.json 情感词典，按语种评分。
- **涉及文件**：`src/main/resources/sentiment/sentiment_*.json`
- **验收**：[ ] 四语种情感评分正确

### WL-T12.005 跨语种图谱归一化
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T5.005
- **描述**：KgNode 增加 node_name_zh + language 字段，同 ontologyEntityId 的节点合并。
- **验收**：[ ] 跨语种同实体合并 [ ] 双名称保留

### WL-T12.006 gwy_common_discovered_entity 表 + 沉淀逻辑
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T5.005, WL-T10.004
- **描述**：建 gwy_common_discovered_entity 表（tenant_id 隔离）+ KgNode 创建时若 ontologyEntityId=null 则沉淀到 gwy_common_discovered_entity，mentionCount++。
- **涉及文件**：`workspace/model/DiscoveredEntity.java`, `workspace/service/KnowledgeSedimentationService.java`
- **验收**：[ ] 未匹配实体沉淀 [ ] mentionCount 递增 [ ] 租户隔离

### WL-T12.007 gwy_common_tenant_relation_asset 表 + 关系沉淀
- **优先级**：P1 ｜ **模块**：app ｜ **依赖**：WL-T6.004
- **描述**：建 gwy_common_tenant_relation_asset 表 + BilateralRelation 计算时 upsert 关系资产。
- **验收**：[ ] 关系沉淀正确 [ ] avg_score 更新

### WL-T12.008 实体升级到本体 + 知识复用建议
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T12.006
- **描述**：`POST /api/ontology/entity/promote`（升级 gwy_common_discovered_entity 到 OntologyEntity）+ `GET /api/workspace/knowledge`（知识资产查询）+ 创建新主题时建议复用。
- **涉及文件**：`workspace/service/KnowledgePromotionService.java`, `KnowledgeSuggestionService.java`
- **验收**：[ ] 升级后 KgNode.ontology_entity_id 更新 [ ] 建议复用正确

### WL-T12.009 Phase 12 单元测试
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T12.001~008
- **描述**：多语种 NLP + 知识沉淀全链路测试。
- **验收**：[ ] 测试全通过

---

# 十三、Phase 13 — Plugin Tool 实现（14 任务，P0）

> 目标：实现 14 个原子工具，注册到 Harness ToolRegistry，通过 AdminApiClient 调用 app。

### WL-T13.001 AdminApiClient 封装
- **优先级**：P0 ｜ **模块**：plugin ｜ **依赖**：无
- **描述**：HTTP 客户端调用 app REST API，含 JWT 透传、统一错误处理。
- **涉及文件**：`plugin/client/AdminApiClient.java`
- **验收**：[ ] 可调用 app API [ ] JWT 透传 [ ] 错误包装

### WL-T13.002 ontology_query + ontology_search 工具
- **优先级**：P0 ｜ **模块**：plugin ｜ **依赖**：WL-T13.001
- **描述**：注册 `ontology_query`（自然语言查询本体）和 `ontology_search`（关键词搜索）工具。
- **验收**：[ ] Harness 可发现并调用 [ ] 返回正确结果

### WL-T13.003 kg_query + kg_search 工具
- **优先级**：P0 ｜ **模块**：plugin ｜ **依赖**：WL-T13.001
- **描述**：注册 `kg_query`（节点+邻居）和 `kg_search`（关键词搜节点）工具。
- **验收**：[ ] 返回节点+邻居+边

### WL-T13.004 relation_analysis + alert_query 工具
- **优先级**：P0 ｜ **模块**：plugin ｜ **依赖**：WL-T13.001
- **描述**：注册 `relation_analysis`（双边关系）和 `alert_query`（告警记录）工具。
- **验收**：[ ] 返回 BilateralRelation / AlertRecord

### WL-T13.005 topic_create 工具（LLM 赋能）
- **优先级**：P0 ｜ **模块**：plugin ｜ **依赖**：WL-T13.001
- **描述**：注册 `topic_create` 工具：接收自然语言描述 → 调用 Harness LLM 识别主题+生成多语种关键词 → 调用 app API 创建 TrackTopic。
- **验收**：[ ] LLM 生成关键词 [ ] TrackTopic 创建成功

### WL-T13.006 crawl_execute 工具
- **优先级**：P0 ｜ **模块**：plugin ｜ **依赖**：WL-T13.001
- **描述**：注册 `crawl_execute` 工具：触发 app 异步爬取。
- **验收**：[ ] 爬取启动 [ ] 返回任务状态

### WL-T13.007 nlp_extract 工具（LLM 赋能）
- **优先级**：P0 ｜ **模块**：plugin ｜ **依赖**：WL-T13.001
- **描述**：注册 `nlp_extract` 工具：加载文章 → 调用 Harness LLM 抽取实体/三元组/情感 → 回传 app 存储。
- **验收**：[ ] LLM 抽取正确 [ ] 结果存储成功

### WL-T13.008 report_generate 工具（LLM 赋能）
- **优先级**：P0 ｜ **模块**：plugin ｜ **依赖**：WL-T13.001
- **描述**：注册 `report_generate` 工具：收集分析数据 → 调用 Harness LLM 生成自然语言报告 → 回传 app 存储。
- **验收**：[ ] LLM 生成报告 [ ] 报告含 7 section

### WL-T13.009 ontology_promote + workspace_overview 工具
- **优先级**：P0 ｜ **模块**：plugin ｜ **依赖**：WL-T13.001
- **描述**：注册 `ontology_promote`（实体升级）和 `workspace_overview`（工作台概览）工具。
- **验收**：[ ] 升级成功 [ ] 概览返回正确

### WL-T13.010 knowledge_aggregate + director_overview 工具
- **优先级**：P0 ｜ **模块**：plugin ｜ **依赖**：WL-T13.001
- **描述**：注册 `knowledge_aggregate`（触发知识汇聚：个人→共享→统一）和 `director_overview`（主任跨租户决策仪表板）工具。
- **验收**：[ ] 汇聚触发成功 [ ] 主任仪表板返回跨租户数据

### WL-T13.011 DshOntologyPlugin 注册全部工具
- **优先级**：P0 ｜ **模块**：plugin ｜ **依赖**：WL-T13.002~010
- **描述**：在 DshOntologyPlugin 中注册全部 14 个工具到 PluginContext。
- **涉及文件**：`plugin/DshOntologyPlugin.java`
- **验收**：[ ] Harness 启动加载插件 [ ] 14 工具全部注册

### WL-T13.012 Plugin 单元测试
- **优先级**：P0 ｜ **模块**：plugin ｜ **依赖**：WL-T13.011
- **描述**：每个工具的单元测试（mock AdminApiClient）。
- **验收**：[ ] 测试全通过

### WL-T13.013 Plugin 集成测试（Harness 加载）
- **优先级**：P0 ｜ **模块**：plugin ｜ **依赖**：WL-T13.012
- **描述**：启动 Harness + 加载 ontology-plugin → 验证工具可发现可调用。
- **验收**：[ ] Harness 启动成功 [ ] 工具调用返回结果

### WL-T13.014 主任工具权限校验
- **优先级**：P0 ｜ **模块**：plugin ｜ **依赖**：WL-T13.010
- **描述**：`director_overview` 和 `knowledge_aggregate` 工具须校验调用者 role_level=0（SUPER_ADMIN），非主任调用返回 403。
- **验收**：[ ] 主任可调用 [ ] 研究员调用返回 403

---

# 十四、Phase 14 — Skill + Agent 定义（8 任务，P0）

> 目标：定义 4 个 Skill（SKILL.md）+ 3 个 Agent（YAML），注册到 Harness。

### WL-T14.001 hotspot-analysis Skill
- **优先级**：P0 ｜ **模块**：plugin ｜ **依赖**：WL-T13.010
- **描述**：编写 `hotspot-analysis/SKILL.md`（七阶段工作流编排）+ 注册到 SkillService。
- **涉及文件**：`plugin/src/main/resources/skills/hotspot-analysis/SKILL.md`
- **验收**：[ ] Skill 可被 Harness 发现 [ ] 工作流编排正确

### WL-T14.002 topic-creation + report-generation Skill
- **优先级**：P0 ｜ **模块**：plugin ｜ **依赖**：WL-T14.001
- **描述**：编写 `topic-creation/SKILL.md` + `report-generation/SKILL.md`。
- **验收**：[ ] 两个 Skill 可被发现

### WL-T14.003 knowledge-extraction Skill
- **优先级**：P0 ｜ **模块**：plugin ｜ **依赖**：WL-T14.002
- **描述**：编写 `knowledge-extraction/SKILL.md`，参考 LLM-knowledgeBase 萃取规范。
- **验收**：[ ] 萃取流程正确

### WL-T14.004 research-assistant Agent
- **优先级**：P0 ｜ **模块**：plugin ｜ **依赖**：WL-T14.001
- **描述**：定义 `research-assistant` Agent（system_prompt + tools 列表 + model 配置），注册到 Agent 运行时。
- **涉及文件**：`plugin/src/main/resources/agents/research-assistant.yaml`
- **验收**：[ ] Agent 可被 Harness 创建 [ ] 对话返回流式结果

### WL-T14.005 topic-creator Agent
- **优先级**：P0 ｜ **模块**：plugin ｜ **依赖**：WL-T14.002
- **描述**：定义 `topic-creator` Agent。
- **验收**：[ ] Agent 可创建主题

### WL-T14.006 director-assistant Agent
- **优先级**：P0 ｜ **模块**：plugin ｜ **依赖**：WL-T13.010
- **描述**：定义 `director-assistant` Agent（system_prompt 定位为研究院主任决策助手 + tools 列表含 director_overview/knowledge_aggregate + model 配置），注册到 Agent 运行时。
- **涉及文件**：`plugin/src/main/resources/agents/director-assistant.yaml`
- **验收**：[ ] Agent 可被 Harness 创建 [ ] 对话返回跨租户数据 [ ] 非主任角色无法使用

### WL-T14.007 Skill + Agent 集成测试
- **优先级**：P0 ｜ **模块**：plugin ｜ **依赖**：WL-T14.004~006
- **描述**：通过 Harness 对话触发 Agent → 验证 Skill 编排 → 验证工具调用（含主任决策主线）。
- **验收**：[ ] 对话触发工作流 [ ] 全流程完成 [ ] 主任决策主线可走通

### WL-T14.008 主任决策主线验证
- **优先级**：P0 ｜ **模块**：plugin ｜ **依赖**：WL-T14.006
- **描述**：主任对话"全平台监测概况" → director-assistant → director_overview → LLM 生成决策摘要 → 追问"生成全平台决策报告" → report_generate(scope=GLOBAL)。
- **验收**：[ ] 主任仪表板返回跨租户数据 [ ] 决策报告生成成功

---

# 十五、Phase 15 — 三级知识架构 + 知识汇聚（9 任务，P0）

> 目标：实现三级知识架构（个人层→共享层→统一层）、知识汇聚机制、主任角色权限，对应 spec REQ-O*/P*/Q*。

### WL-T15.001 汇聚表 DDL + 索引
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T1.001
- **描述**：编写 `V20261014__aggregation_schema.sql`，创建 5 张汇聚表：gwy_global_discovered_entity, gwy_global_bilateral_relation, gwy_global_kg_node, gwy_global_kg_edge, gwy_global_aggregation_log。含去重唯一索引（entityName+entityType / subject+object+relationType / ontologyEntityId）。
- **涉及文件**：`db/migration/V20261014__aggregation_schema.sql`
- **验收**：[ ] 5 表建出 [ ] 去重索引正确 [ ] 幂等

### WL-T15.002 统一层聚合视图
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T15.001
- **描述**：创建 `v_unified_monitor`（跨租户主题监测聚合视图）和 `v_unified_kg`（跨租户知识图谱聚合视图）。
- **涉及文件**：`db/migration/V20261014__aggregation_schema.sql`（追加）
- **验收**：[ ] 视图可查询 [ ] 返回跨租户聚合数据

### WL-T15.003 KnowledgeAggregator 服务骨架
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T15.001
- **描述**：创建 `KnowledgeAggregator` 服务，定义三个汇聚方法：aggregateDiscoveredEntities / aggregateBilateralRelations / aggregateKgGraph，暂不实现逻辑。
- **涉及文件**：`workspace/service/KnowledgeAggregator.java`
- **验收**：[ ] 编译通过

### WL-T15.004 发现实体汇聚实现
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T15.003
- **描述**：实现 `aggregateDiscoveredEntities`：遍历各租户 gwy_common_discovered_entity → 按 entityName+entityType 去重合并 → sum mentionCount → 写入 gwy_global_discovered_entity → 记入 gwy_global_aggregation_log。
- **验收**：[ ] 去重正确 [ ] mentionCount 累加 [ ] gwy_global_aggregation_log 记录

### WL-T15.005 双边关系 + 知识图谱汇聚实现
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T15.004
- **描述**：实现 `aggregateBilateralRelations`（按 subject+object+relationType 去重，avg score）和 `aggregateKgGraph`（按 ontologyEntityId 合并节点，跨租户边权重叠加）。
- **验收**：[ ] 关系去重正确 [ ] 图谱合并正确 [ ] 边权重叠加

### WL-T15.006 定时汇聚调度
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T15.005
- **描述**：`@Scheduled(fixedRate = 3600000)` 每小时自动触发全量汇聚，记录 gwy_global_aggregation_log。
- **涉及文件**：`workspace/service/AggregationScheduler.java`
- **验收**：[ ] 定时触发 [ ] 汇聚完成写入 log

### WL-T15.007 事件触发增量汇聚
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T15.005
- **描述**：在 WorklineOrchestrator 的 onAnalysisCompleted 阶段触发增量汇聚（仅汇聚本次分析新产出的实体/关系/图谱）。
- **验收**：[ ] 分析完成后自动增量汇聚 [ ] 仅处理新增数据

### WL-T15.008 主任角色 + 权限实现
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T1.001
- **描述**：sys_user 表增加 role_level 列（0=SUPER_ADMIN, 1=RESEARCHER, 2=GUEST）；实现 RoleBasedAccessFilter：SUPER_ADMIN 可跨租户访问，RESEARCHER 仅自己 tenant_id，GUEST 仅共享层只读；统一层视图仅 SUPER_ADMIN 可访问。
- **涉及文件**：`common/RoleBasedAccessFilter.java`, `common/TenantContext.java`（改造）
- **验收**：[ ] 主任可跨租户 [ ] 研究员仅自己 [ ] 访客仅共享只读

### WL-T15.009 汇聚 API + 手动触发
- **优先级**：P0 ｜ **模块**：app ｜ **依赖**：WL-T15.006
- **描述**：`POST /api/workspace/knowledge/aggregate`（SUPER_ADMIN 手动触发全量/增量汇聚）+ `GET /api/workspace/knowledge/aggregate/log`（查看汇聚日志）。
- **验收**：[ ] 手动触发成功 [ ] 日志可查 [ ] 非主任返回 403

---

# 十六、Phase 16 — 集成测试（7 任务，P2）

### WL-T16.001 端到端：中东热点多语种分析
- **优先级**：P2 ｜ **模块**：all ｜ **依赖**：ALL
- **描述**：用户对话"分析中东局势" → Agent 编排 → 4 语种采集 → LLM NLP → 图谱 → 关系 → LLM 报告 → 验证全流程。
- **验收**：[ ] 报告含 7 section [ ] 双边关系正确

### WL-T16.002 端到端：知识沉淀与升级
- **优先级**：P2 ｜ **模块**：all ｜ **依赖**：WL-T16.001
- **描述**：新实体沉淀 → mentionCount 累积 → 升级到本体 → 跨租户可见。
- **验收**：[ ] 沉淀正确 [ ] 升级后共享

### WL-T16.003 端到端：多租户隔离
- **优先级**：P2 ｜ **模块**：all ｜ **依赖**：WL-T16.001
- **描述**：用户A/B 各自创建主题 → 验证数据隔离 → 本体共享。
- **验收**：[ ] A 看不到 B 的数据 [ ] 本体共享

### WL-T16.004 端到端：AI 对话追问
- **优先级**：P2 ｜ **模块**：all ｜ **依赖**：WL-T16.001
- **描述**：报告生成后用户追问"以色列和伊朗的关系" → Agent 调用 relation_analysis → LLM 总结。
- **验收**：[ ] 追问返回正确

### WL-T16.005 端到端：七模型 YAML 驱动验证
- **优先级**：P2 ｜ **模块**：all ｜ **依赖**：WL-T16.001
- **描述**：修改 M1 YAML → 重新生成 DDL → 验证表结构更新。
- **验收**：[ ] YAML 变更驱动 schema 更新

### WL-T16.006 端到端：三级知识架构 + 汇聚
- **优先级**：P2 ｜ **模块**：all ｜ **依赖**：WL-T15.009
- **描述**：租户A/B 各自完成分析 → 触发知识汇聚 → 验证 gwy_global_discovered_entity / gwy_global_bilateral_relation / gwy_global_kg_node 去重合并正确 → 主任通过 director_overview 查看全局数据。
- **验收**：[ ] 汇聚去重正确 [ ] 主任可看全部 [ ] 研究员仅看自己+共享

### WL-T16.007 端到端：主任决策主线
- **优先级**：P2 ｜ **模块**：all ｜ **依赖**：WL-T16.006
- **描述**：主任对话"全平台监测概况" → director-assistant → director_overview → 决策摘要 → 追问"生成全平台中东决策报告" → report_generate(scope=GLOBAL) → 追问"汇聚全平台知识" → knowledge_aggregate。
- **验收**：[ ] 主任仪表板跨租户 [ ] 决策报告生成 [ ] 手动汇聚成功

---

# 十六、依赖关系图

```
Phase 1 (YAML 建模)
  └──→ Phase 2 (M1→DDL 建库, MySQL 51 表)
        ├──→ Phase 9 (多租户工作台)
        │      └──→ Phase 3 (本体支撑 G，共享)
        │             └──→ Phase 4 (任务创建+采集)
        │                    └──→ Phase 10 (互联网爬取)
        │                           └──→ Phase 11 (多语种NLP+知识沉淀)
        │                                  └──→ Phase 5 (知识图谱)
        │                                         └──→ Phase 6 (关系分析)
        │                                                └──→ Phase 7 (报告生成)
        ├──→ Phase 15 (三级架构+汇聚+主任角色) ←── Phase 2, 9
        └──→ Phase 13 (Plugin 14 工具) ←── Phase 3,5,6,7,9,15
              └──→ Phase 14 (Skill + 3 Agent) ←── Phase 13, 15
                     └──→ Phase 16 (集成测试) ←── ALL
```

---

# 十七、与 v1.0 全量任务清单的关系

| 对比项 | v2.0 工作主线（本文件） | v1.0 全量（1.0/05-实现任务清单.md）|
|--------|----------------------|------------------------------|
| 任务数 | 131 | 108 |
| 架构 | dsh-java Agent/Skill/Tool 插件 | 传统若依 CRUD |
| 数据库 | MySQL（51 张表）| MySQL（41 张表）|
| YAML 驱动 | 有（Phase 1 七模型建模）| 无 |
| Agent/Skill | 有（Phase 14, 3 Agent）| 无 |
| LLM 赋能 | 有（Tool 内调 LLM）| 无 |
| 多租户 | 有（Phase 9）| 无 |
| 三级知识架构 | 有（Phase 15, 个人→共享→统一）| 无 |
| 知识汇聚 | 有（Phase 15, 定时+事件+手动）| 无 |
| 主任决策 | 有（Phase 14-15, director-assistant）| 无 |
| 爬虫 | 有（Phase 10）| 无 |
| 多语种 | 有（Phase 11）| 无 |
| 知识沉淀 | 有（Phase 11）| 无 |
| 集成测试 | 端到端+Agent 对话+主任决策（Phase 16）| 无 |
| Phase | 16 个 | 7 个 |
