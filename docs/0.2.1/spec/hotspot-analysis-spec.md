# 热点区域关系分析工作主线 — 需求规格

> 本文档以 EARS（Easy Approach to Requirements Syntax）格式定义 dsh-java-ontology 的核心工作主线：**用户创建热点区域关系分析任务 → 系统采集+抽取+建图+分析 → 生成分析报告**。
>
> - **需求编号体系**：`REQ-{阶段字母}{编号}-{优先级}[-{序号}]`，如 `REQ-A1-P0-2`
> - **EARS 类型**：Ubiquitous（普遍型）/ Event-driven（事件驱动型）/ State-driven（状态驱动型）/ Optional（可选型）/ Unwanted（非期望型）
> - **优先级**：P0 工作主线必需 / P1 工作主线增强 / P2 生态扩展
> - **来源**：`source/gwy` 4 大业务域 41 实体的端到端功能编排

---

# 1. 组件定位

## 1.1 核心职责

本组件负责 [编排] [热点区域关系分析的端到端工作流]，实现 [从用户创建分析任务到交付分析报告的全链路自动化]。

## 1.2 工作主线全景

```
用户登录工作台 → 创建主题 → 配置多语种采集 → 互联网爬取 → 多语种NLP → 知识图谱 → 关系分析 → 报告生成
  (J 租户)       (A)          (B+K)            (K)         (C+L)       (D)       (E)        (F)
                                                                              ↓
                                                                    知识沉淀(M) → 反哺本体(G)
```

| 阶段 | 域 | 核心实体 | 输入 | 输出 |
|------|-----|---------|------|------|
| J 工作台 | sys | User, Tenant, Workspace | 用户注册/登录 | 租户上下文（JWT）|
| A 创建任务 | analyze+track | TrackTopic, CrawlKeywordGroup | 用户指定热点+多语种关键词 | 分析任务 |
| B+K 数据采集 | track | CrawlSource, CrawlTask, Article | 采集配置+互联网URL | 多语种文章集 |
| C+L NLP抽取 | analyze | NlpEntity, NlpTriple, NlpSentiment | 文章集（按语种分派）| 实体/三元组/情感 |
| D 知识图谱 | track | KgNode, KgEdge, NerElement | 实体+三元组 | 知识图谱（跨语种归一化）|
| E 关系分析 | analyze | BilateralRelation, AlertRule | 知识图谱+本体 | 双边关系+告警 |
| F 报告生成 | display | RptTemplate, RptReport, AlertRecord | 分析结果 | 多语种分析报告 |
| G 本体支撑 | ontology | Country, Organization, Person, Hotspot, Term ... | 分析全过程 | 共享领域知识底座 |
| M 知识沉淀 | workspace | DiscoveredEntity, TenantRelationAsset | 分析产出 | 租户私有知识资产（可升级到本体）|

## 1.3 职责边界

1. **负责** 内置互联网爬虫（Jsoup + HttpClient），支持多语种源采集（zh/en/fr/ar）。
2. **不负责** NLP 模型训练，仅负责调用 NLP 服务或基于本体术语库的规则匹配，并按语种分派策略。
3. **不负责** 前端可视化渲染，仅通过 API 提供数据供前端消费。
4. **不负责** 若依框架迁移，采用 Spring Boot + JdbcTemplate + MySQL 独立实现。
5. **负责** 工作主线各阶段间的数据流转、状态编排、告警触发、报告装配。
6. **负责** 多租户隔离：本体知识共享，工作台数据按 tenantId 隔离。
7. **负责** 知识沉淀：分析产出的新实体沉淀为租户私有知识，可升级到共享本体。

---

# 2. 领域术语

**热点区域（Hotspot Region）**
: 用户关注的地缘政治热点区域，如"中东""南海""台海""俄乌"，由 TrackTopic 实体承载，关联关键词组和本体知识。

**双边关系（Bilateral Relation）**
: 两个主体（国家/组织/人物）之间的政治/经济/军事/外交关系评估，由 BilateralRelation 实体承载，是关系分析的核心产出。

**知识图谱节点（KgNode）**
: 从文章中抽取的实体在知识图谱中的节点表示，含实体类型、属性、来源文章引用。

**知识图谱边（KgEdge）**
: 两个 KgNode 之间的关系边，含关系类型（盟友/对抗/贸易/制裁/...）、强度、来源。

**追踪主题（TrackTopic）**
: 用户创建的分析任务主题，绑定热点区域、关键词组、采集源、告警规则，是工作主线的起点。

**分析报告（RptReport）**
: 基于报告模板（RptTemplate）装配的最终交付物，含关系分析结论、图表数据、告警摘要、推荐建议。

---

# 3. 需求

## 3.1 阶段 A — 创建分析任务（REQ-A*）

### REQ-A1-P0-1（Ubiquitous）创建追踪主题
The system shall provide an API endpoint `POST /api/analyze/trackTopic` that accepts a TrackTopic with fields {topicName, hotspotRegion, description, keywordGroupId, crawlSourceIds[], alertRuleIds[], status} and persists it to the `gwy_analyze_track_topic` table.

### REQ-A1-P0-2（Ubiquitous）创建关键词组
The system shall provide an API endpoint `POST /api/track/crawlKeywordGroup` that accepts a CrawlKeywordGroup with fields {groupName, keywordsZh[], keywordsEn[], matchMode} and persists it to the `gwy_track_crawl_keyword_group` table.

### REQ-A2-P0-1（State-driven）任务状态初始化
While a TrackTopic is newly created, the system shall set its status to `DRAFT` and initialize `articleCount=0`, `entityCount=0`, `relationCount=0`, `reportCount=0`.

### REQ-A2-P0-2（Event-driven）任务激活触发采集
When a TrackTopic status transitions to `ACTIVE`, the system shall create a CrawlTask linked to the topic's keywordGroupId and crawlSourceIds, and set the CrawlTask status to `PENDING`.

### REQ-A3-P1-1（Optional）关联本体热点
Where the TrackTopic's hotspotRegion matches an existing Hotspot entity in the ontology domain, the system shall link the topic to the Hotspot by storing `hotspotId` in the TrackTopic record.

### REQ-A3-P1-2（Ubiquitous）查询本体热点列表
The system shall provide `GET /api/ontology/hotspot` to return all Hotspot entities with {nameZh, nameEn, region, keyCountries, riskLevel}, enabling the user to select a hotspot when creating a task.

---

## 3.2 阶段 B — 数据采集（REQ-B*）

### REQ-B1-P0-1（Ubiquitous）管理采集源
The system shall provide CRUD endpoints `*/api/track/crawlSource` for CrawlSource with fields {sourceName, sourceUrl, sourceType, language, crawlFrequency, status}.

### REQ-B1-P0-2（Ubiquitous）管理采集订阅
The system shall provide CRUD endpoints `*/api/track/crawlSubscription` for CrawlSubscription with fields {subscriptionName, sourceId, keywordGroupId, subscribeStatus, lastSyncTime}.

### REQ-B1-P0-3（Ubiquitous）管理采集任务
The system shall provide CRUD endpoints `*/api/track/crawlTask` for CrawlTask with fields {taskName, sourceId, keywordGroupId, topicId, taskStatus, scheduledTime, priority}.

### REQ-B2-P0-1（Event-driven）采集任务完成存储文章
When a CrawlTask completes (external crawler callback or manual import), the system shall store the resulting articles in the `gwy_track_article` table with fields {title, content, sourceUrl, language, publishTime, sourceId, topicId, crawlTaskId}.

### REQ-B2-P0-2（Ubiquitous）文章 CRUD
The system shall provide CRUD endpoints `*/api/track/gwy_track_article` for Article, including list with pagination and filtering by {topicId, sourceId, language, publishTimeRange}.

### REQ-B3-P1-1（Ubiquitous）数据导入
The system shall provide `POST /api/track/dataImport` that accepts a batch of articles (JSON/CSV) and links them to a specified topicId, storing an import log in `gwy_track_data_import` table.

### REQ-B3-P1-2（Ubiquitous）数据导出
The system shall provide `POST /api/track/dataExport` that exports articles matching given filters to JSON/CSV, storing an export log in `gwy_track_data_export` table.

### REQ-B4-P1-1（Event-driven）文章入库触发 NLP 抽取
When an Article is stored (via crawl or import), the system shall set `article.nlpStatus=PENDING` to mark it as ready for NLP extraction in stage C.

---

## 3.3 阶段 C — NLP 抽取（REQ-C*）

### REQ-C1-P0-1（Ubiquitous）NLP 实体抽取
The system shall provide `POST /api/analyze/nlpEntity/extract` that accepts an articleId, invokes the NLP service to extract named entities, and stores results in `gwy_analyze_nlp_entity` table with fields {articleId, entityName, entityType, startPos, endPos, confidence}.

### REQ-C1-P0-2（Ubiquitous）NLP 实体 CRUD
The system shall provide CRUD endpoints `*/api/analyze/nlpEntity` for NlpEntity with filtering by {articleId, entityType, entityName}.

### REQ-C2-P0-1（Ubiquitous）NLP 三元组抽取
The system shall provide `POST /api/analyze/nlpTriple/extract` that accepts an articleId, invokes the NLP service to extract subject-predicate-object triples, and stores results in `gwy_analyze_nlp_triple` table with fields {articleId, subject, predicate, object, confidence}.

### REQ-C2-P0-2（Ubiquitous）NLP 三元组 CRUD
The system shall provide CRUD endpoints `*/api/analyze/nlpTriple` for NlpTriple with filtering by {articleId, subject, object}.

### REQ-C3-P0-1（Ubiquitous）情感分析
The system shall provide `POST /api/analyze/nlpSentiment/analyze` that accepts an articleId, invokes the sentiment analysis service, and stores results in `gwy_analyze_nlp_sentiment` table with fields {articleId, overallScore, positiveScore, negativeScore, neutralScore, keyPhrases[]}.

### REQ-C3-P0-2（Ubiquitous）情感分析 CRUD
The system shall provide CRUD endpoints `*/api/analyze/nlpSentiment` for NlpSentiment.

### REQ-C4-P0-1（Event-driven）NLP 完成触发图谱构建
When all three NLP extractions (entity, triple, sentiment) for an Article are complete, the system shall set `article.nlpStatus=COMPLETED` and `article.kgStatus=PENDING` to mark it as ready for knowledge graph construction in stage D.

### REQ-C4-P1-1（Unwanted）NLP 服务不可用
If the NLP service is unavailable or returns an error, the system shall set `article.nlpStatus=FAILED`, store the error message in `article.nlpError`, and not block subsequent articles from processing.

---

## 3.4 阶段 D — 知识图谱构建（REQ-D*）

### REQ-D1-P0-1（Ubiquitous）NER 元素管理
The system shall provide CRUD endpoints `*/api/track/nerElement` for NerElement with fields {elementName, elementType, standardName, ontologyEntityId, frequency, topicId}.

### REQ-D1-P0-2（Event-driven）NER 元素对齐本体
When a NerElement is created, the system shall attempt to match it against ontology entities (Country, Organization, Person) by name/alias, and if matched, store the `ontologyEntityId` link.

### REQ-D2-P0-1（Ubiquitous）知识图谱节点 CRUD
The system shall provide CRUD endpoints `*/api/track/kgNode` for KgNode with fields {nodeId, nodeType, nodeName, ontologyEntityId, properties, topicId, articleIds[]}.

### REQ-D2-P0-2（Ubiquitous）知识图谱边 CRUD
The system shall provide CRUD endpoints `*/api/track/kgEdge` for KgEdge with fields {edgeId, sourceNodeId, targetNodeId, relationType, weight, topicId, articleIds[], evidenceText}.

### REQ-D3-P0-1（Event-driven）图谱构建
When an Article's `kgStatus=PENDING`, the system shall:
1. Create or merge KgNode for each NlpEntity (matched by entityType + entityName + topicId)
2. Create KgEdge for each NlpTriple (source→target with predicate as relationType)
3. Set `article.kgStatus=COMPLETED`
4. Log the build in `gwy_track_kg_build_log` table

### REQ-D3-P0-2（Ubiquitous）图谱构建日志
The system shall store a KgBuildLog record for each graph build operation with fields {topicId, articleId, nodesAdded, edgesAdded, nodesMerged, buildDuration, buildTime}.

### REQ-D4-P0-1（Ubiquitous）图谱查询 — 节点详情
The system shall provide `GET /api/track/dashboard/kg/node/{nodeId}` returning the node with all its attributes and linked ontology entity.

### REQ-D4-P0-2（Ubiquitous）图谱查询 — 邻居
The system shall provide `GET /api/track/dashboard/kg/node/{nodeId}/neighbors` returning all adjacent nodes and edges within a specified depth.

### REQ-D4-P0-3（Ubiquitous）图谱查询 — 搜索
The system shall provide `GET /api/track/dashboard/kg/search` that searches KgNode by name/type/topicId and returns matching nodes with their edge counts.

---

## 3.5 阶段 E — 关系分析（REQ-E*）

### REQ-E1-P0-1（Ubiquitous）双边关系 CRUD
The system shall provide CRUD endpoints `*/api/analyze/bilateralRelation` for BilateralRelation with fields {relationId, subjectEntityId, subjectEntityType, objectEntityId, objectEntityType, relationType, relationScore, trend, topicId, evidenceArticleIds[], analysisSummary}.

### REQ-E1-P0-2（Event-driven）双边关系计算
When a TrackTopic's knowledge graph build reaches a threshold (all pending articles processed), the system shall compute BilateralRelation records for all pairs of major entities (countries/organizations) in the topic, deriving:
1. `relationType` from the dominant KgEdge relationType between the pair
2. `relationScore` from the aggregate edge weights and sentiment scores
3. `trend` from temporal analysis of gwy_track_article publish times
4. `evidenceArticleIds` from the supporting articles

### REQ-E2-P0-1（Ubiquitous）追踪元素 CRUD
The system shall provide CRUD endpoints `*/api/analyze/trackElement` for TrackElement with fields {elementName, elementType, topicId, ontologyEntityId, mentionCount, firstMentionTime, lastMentionTime}.

### REQ-E2-P0-2（Event-driven）追踪元素统计
When new articles are added to a topic, the system shall update TrackElement records: increment mentionCount, update lastMentionTime, and create new TrackElement for previously unseen entities.

### REQ-E3-P0-1（Ubiquitous）告警规则 CRUD
The system shall provide CRUD endpoints `*/api/analyze/alertRule` for AlertRule with fields {ruleName, topicId, ruleType, conditionExpression, threshold, severity, enabled}.

### REQ-E3-P0-2（Event-driven）告警触发
When a BilateralRelation or TrackElement update satisfies an enabled AlertRule's condition, the system shall create an AlertRecord in the display domain with fields {ruleId, topicId, alertType, severity, triggerValue, alertTime, alertContent}.

---

## 3.6 阶段 F — 报告生成（REQ-F*）

### REQ-F1-P0-1（Ubiquitous）报告模板 CRUD
The system shall provide CRUD endpoints `*/api/display/rptTemplate` for RptTemplate with fields {templateName, templateType, sections[], chartConfigs[], layout}.

### REQ-F1-P0-2（Ubiquitous）分析报告 CRUD
The system shall provide CRUD endpoints `*/api/display/rptReport` for RptReport with fields {reportName, topicId, templateId, status, generatedContent, chartData, summary, conclusion, recommendations[], generatedTime}.

### REQ-F2-P0-1（Event-driven）报告生成
When a user requests report generation for a topicId (via `POST /api/display/rptReport/generate`), the system shall:
1. Load the specified RptTemplate
2. Query BilateralRelation records for the topic
3. Query KgNode/KgEdge statistics for the topic
4. Query AlertRecord records for the topic
5. Query NlpSentiment aggregate scores for the topic
6. Assemble the report content following the template's sections and chartConfigs
7. Persist the RptReport with status `COMPLETED`

### REQ-F2-P0-2（Ubiquitous）报告内容结构
The generated report shall contain:
1. **概述**：热点区域、分析时间范围、文章数、实体数
2. **关键实体**：出现频率最高的国家/组织/人物及其本体属性
3. **双边关系矩阵**：所有 BilateralRelation 的关系类型和评分
4. **知识图谱摘要**：节点数、边数、关键关系路径
5. **情感趋势**：按时间维度的情感分数变化
6. **告警摘要**：触发的 AlertRecord 列表
7. **推荐建议**：基于分析结果的 RecommendationRecord

### REQ-F3-P0-1（Ubiquitous）告警记录 CRUD
The system shall provide CRUD endpoints `*/api/display/alertRecord` for AlertRecord with filtering by {topicId, severity, alertTimeRange}.

### REQ-F3-P0-2（Ubiquitous）信息发布
The system shall provide CRUD endpoints `*/api/display/informationPublish` for InformationPublish with fields {title, content, topicId, reportId, publishStatus, publishTime}.

### REQ-F4-P1-1（Ubiquitous）推送记录
The system shall provide CRUD endpoints `*/api/display/pushRecord` for PushRecord with fields {pushTarget, pushType, content, reportId, pushStatus, pushTime}.

### REQ-F4-P1-2（Ubiquitous）推荐记录
The system shall provide CRUD endpoints `*/api/display/recommendationRecord` for RecommendationRecord with fields {topicId, recommendationType, content, score, basis}.

### REQ-F5-P1-1（Ubiquitous）搜索索引
The system shall provide CRUD endpoints `*/api/display/searchIndex` for SearchIndex with fields {indexName, entityType, entityId, topicId, keywords, indexedContent}.

### REQ-F6-P1-1（Optional）会议信息
Where the analysis involves conference/meeting events, the system shall provide CRUD endpoints `*/api/display/conferenceInfo` for ConferenceInfo with fields {conferenceName, conferenceTime, location, participants, topicId, outcomes}.

---

## 3.7 阶段 G — 本体知识支撑（REQ-G*）

### REQ-G1-P0-1（Ubiquitous）国家本体查询
The system shall provide `GET /api/ontology/country` returning Country entities with {countryCode, nameZh, nameEn, region, importance, wikiUrl}, filterable by region and importance.

### REQ-G1-P0-2（Ubiquitous）组织本体查询
The system shall provide `GET /api/ontology/organization` returning Organization entities with {orgCode, nameZh, nameEn, orgType, memberCount, headquarters}.

### REQ-G1-P0-3（Ubiquitous）人物本体查询
The system shall provide `GET /api/ontology/person` returning Person entities with {name, nameEn, gender, birthDate, country, occupation, affiliation}.

### REQ-G2-P0-1（Ubiquitous）热点本体查询
The system shall provide `GET /api/ontology/hotspot` returning Hotspot entities with {nameZh, nameEn, region, keyCountries, riskLevel}, used in stage A for task creation.

### REQ-G2-P0-2（Ubiquitous）术语本体查询
The system shall provide `GET /api/ontology/term` returning Term entities with {termZh, termEn, category, keywordsZh, keywordsEn}, used in NLP extraction for domain-specific entity matching.

### REQ-G3-P0-1（Ubiquitous）本体综合查询
The system shall provide `POST /api/ontology/query` that accepts a natural language query and returns relevant ontology entities across all types (Country, Organization, Person, Term, EventType, Hotspot), serving as the knowledge base for the analysis pipeline.

### REQ-G3-P0-2（Ubiquitous）本体搜索
The system shall provide `GET /api/ontology/search` that performs keyword search across all ontology entities and returns matching records with type and relevance score.

---

## 3.8 跨阶段 — 仪表板与监控（REQ-H*）

### REQ-H1-P0-1（Ubiquitous）追踪仪表板
The system shall provide `GET /api/track/dashboard/monitor` returning a dashboard DTO with {totalArticles, totalEntities, totalRelations, articlesByLanguage, articlesBySource, articlesByTime, topEntities}.

### REQ-H1-P0-2（Ubiquitous）知识图谱仪表板
The system shall provide `GET /api/track/dashboard/kg` returning a KG statistics DTO with {totalNodes, totalEdges, nodesByType, edgesByRelationType, topConnectedNodes}.

### REQ-H2-P1-1（Ubiquitous）展示仪表板
The system shall provide `GET /api/display/dashboard` returning a display dashboard DTO with {totalReports, totalAlerts, alertsBySeverity, recentReports, recentAlerts, publishStats}.

---

## 3.9 跨阶段 — 插件工具（REQ-I*）

### REQ-I1-P0-1（Ubiquitous）插件本体查询工具
The dsh-java-ontology-plugin shall register a tool `ontology_query` that accepts a natural language query and returns ontology entities, callable from the dsh-java Harness runtime.

### REQ-I1-P0-2（Ubiquitous）插件本体搜索工具
The dsh-java-ontology-plugin shall register a tool `ontology_search` that accepts keywords and returns matching ontology entities with relevance scores.

### REQ-I2-P0-1（Ubiquitous）插件知识图谱查询工具
The dsh-java-ontology-plugin shall register a tool `kg_query` that accepts a nodeId or entityName and returns the node with its neighbors and edges.

### REQ-I2-P0-2（Ubiquitous）插件关系分析工具
The dsh-java-ontology-plugin shall register a tool `relation_analysis` that accepts a topicId and returns BilateralRelation records with evidence.

### REQ-I3-P0-1（Ubiquitous）插件报告生成工具
The dsh-java-ontology-plugin shall register a tool `report_generate` that accepts a topicId and templateId, triggers report generation, and returns the report summary.

### REQ-I3-P0-2（Ubiquitous）插件告警查询工具
The dsh-java-ontology-plugin shall register a tool `alert_query` that accepts a topicId and returns active AlertRecord records.

---

## 3.10 阶段 J — 多租户工作台（REQ-J*）

### REQ-J1-P0-1（Ubiquitous）用户注册
The system shall provide `POST /api/auth/register` that accepts {username, password, displayName, email} and creates a User record with a unique `tenantId` (UUID), persisting to the `sys_user` table.

### REQ-J1-P0-2（Ubiquitous）用户登录
The system shall provide `POST /api/auth/login` that accepts {username, password} and returns a JWT token containing {userId, tenantId, displayName, exp}.

### REQ-J1-P0-3（State-driven）请求租户识别
While a request carries a valid JWT token, the system shall extract the tenantId from the token and set it as the request's tenant context, used for all subsequent data access.

### REQ-J2-P0-1（Ubiquitous）工作台数据隔离
The system shall enforce that all queries to workline tables (gwy_analyze_track_topic, gwy_track_article, gwy_track_kg_node, gwy_track_kg_edge, gwy_ontology_bilateral_relation, gwy_display_rpt_report, gwy_display_alert_record, ...) automatically filter by the current tenant's tenantId, preventing cross-tenant data access.

### REQ-J2-P0-2（Ubiquitous）工作台概览
The system shall provide `GET /api/workspace/overview` that returns the current tenant's workspace summary: {totalTopics, totalArticles, totalReports, totalAlerts, recentTopics[], recentReports[]}.

### REQ-J2-P0-3（Ubiquitous）工作台主题列表
The system shall provide `GET /api/workspace/topics` that returns all TrackTopic records belonging to the current tenant, with pagination and status filtering.

### REQ-J3-P0-1（Ubiquitous）本体知识共享
The system shall designate ontology domain tables (gwy_ontology_country, gwy_ontology_organization, gwy_ontology_person, gwy_ontology_hotspot, gwy_ontology_term, ...) as **shared public knowledge**, not filtered by tenantId, accessible to all tenants.

### REQ-J3-P0-2（Ubiquitous）租户私有知识
The system shall designate workline tables (gwy_analyze_track_topic, gwy_track_article, gwy_track_kg_node, gwy_track_kg_edge, gwy_ontology_bilateral_relation, ...) as **tenant-private**, each record carrying a `tenant_id` column, filtered by tenantId on every query.

### REQ-J4-P1-1（Ubiquitous）用户模块配置
The system shall provide CRUD endpoints `*/api/display/userModuleConfig` for UserModuleConfig with fields {userId, moduleCode, config(JSON), visible}, enabling per-user workspace customization.

---

## 3.11 阶段 K — 互联网爬取引擎（REQ-K*）

### REQ-K1-P0-1（Ubiquitous）爬虫执行
The system shall provide `POST /api/track/crawlTask/{id}/execute` that starts an asynchronous crawl job for the specified CrawlTask, fetching articles from the configured CrawlSource URLs using HTTP client (Jsoup + HttpClient).

### REQ-K1-P0-2（Event-driven）爬虫完成存储文章
When the crawl job completes, the system shall store all fetched articles in the `gwy_track_article` table with {title, content, sourceUrl, language, publishTime, sourceId, topicId, crawlTaskId, tenantId}, and set each gwy_track_article's `nlpStatus=PENDING`.

### REQ-K2-P0-1（Ubiquitous）多语种源配置
The system shall support CrawlSource with a `language` field accepting values {zh, en, fr, ar}, and the crawler shall set the gwy_track_article's language based on the source's configured language.

### REQ-K2-P0-2（Ubiquitous）关键词驱动爬取
The system shall use the CrawlKeywordGroup's keywords (keywordsZh for zh sources, keywordsEn for en sources, keywordsFr for fr sources, keywordsAr for ar sources) to construct search queries for each language-specific source.

### REQ-K3-P0-1（Ubiquitous）爬虫调度
The system shall support CrawlTask with `scheduledTime` and `crawlFrequency` (ONCE/HOURLY/DAILY/WEEKLY), and a scheduler that triggers crawl execution at the configured intervals.

### REQ-K3-P0-2（State-driven）爬虫限流
While a crawl job is running, the system shall enforce a rate limit (configurable, default 1 request/second per source) to avoid overwhelming source websites.

### REQ-K4-P0-1（Ubiquitous）爬虫状态查询
The system shall provide `GET /api/track/crawlTask/{id}/status` returning the crawl job's current status (PENDING/RUNNING/COMPLETED/FAILED), progress (articlesFetched/totalExpected), and error log.

### REQ-K4-P1-1（Unwanted）爬虫失败处理
If a crawl job encounters a network error or source block, the system shall log the error, mark the failed articles, continue with remaining sources, and not abort the entire task.

---

## 3.12 多语种扩展（REQ-L*）

### REQ-L1-P0-1（Ubiquitous）多语种 NLP 分派
The system shall dispatch NLP extraction to language-specific strategies based on the gwy_track_article's `language` field: `ZhNlpStrategy` for zh, `EnNlpStrategy` for en, `FrNlpStrategy` for fr, `ArNlpStrategy` for ar.

### REQ-L1-P0-2（Ubiquitous）多语种本体术语
The system shall support Term entities with {termZh, termEn, termFr, termAr} and use the appropriate language field for entity matching during NLP extraction.

### REQ-L2-P0-1（Ubiquitous）多语种情感分析
The system shall use language-specific sentiment lexicons for sentiment analysis, supporting positive/negative/neutral scoring for zh, en, fr, ar.

### REQ-L3-P0-1（Ubiquitous）多语种报告生成
The system shall support RptTemplate with a `language` field, and report generation shall produce content in the specified language, using language-appropriate section headers and formatting.

### REQ-L3-P0-2（Ubiquitous）多语种知识图谱
The system shall store KgNode with `nodeName` in the original gwy_track_article language and `nodeNameZh` as a normalized Chinese name (translated if possible), enabling cross-language graph merging by matching ontologyEntityId.

---

## 3.13 知识沉淀（REQ-M*）

### REQ-M1-P0-1（Event-driven）新实体沉淀
When a KgNode is created during analysis and its `ontologyEntityId` is null (no ontology match), the system shall store it as a **tenant-private discovered entity** in the `gwy_common_discovered_entity` table with {tenantId, entityName, entityType, sourceTopicId, firstSeenTime, mentionCount}, accumulating the tenant's private knowledge.

### REQ-M1-P0-2（Event-driven）实体升级到本体
When a discovered entity's mentionCount exceeds a threshold (configurable, default 5), the system shall suggest promoting it to the shared ontology (via `POST /api/ontology/entity/promote`), creating an OntologyEntity record and linking all related KgNodes.

### REQ-M2-P0-1（Ubiquitous）知识资产查询
The system shall provide `GET /api/workspace/knowledge` that returns the tenant's accumulated knowledge assets: {discoveredEntities[], accumulatedRelations[], topicHistory[], entityFrequency[]}.

### REQ-M2-P0-2（Ubiquitous）跨主题知识复用
The system shall, when a user creates a new TrackTopic, automatically suggest relevant discovered entities and accumulated relations from the tenant's previous topics that match the new topic's keywords.

### REQ-M3-P1-1（Event-driven）关系沉淀
When a BilateralRelation is computed, the system shall upsert a `gwy_common_tenant_relation_asset` record {tenantId, subjectEntityId, objectEntityId, relationType, avgScore, topicIds[], lastUpdated}, accumulating the tenant's relationship knowledge across topics.

### REQ-M4-P1-1（Ubiquitous）知识沉淀仪表板
The system shall provide `GET /api/workspace/knowledge/dashboard` returning the tenant's knowledge growth metrics: {totalDiscoveredEntities, totalAccumulatedRelations, entitiesByType, relationsByType, knowledgeGrowthOverTime}.

---

## 3.14 三级知识架构 — 共享层汇聚（REQ-O*）

### REQ-O1-P0-1（Ubiquitous）全局发现实体汇聚表
The system shall maintain a `gwy_global_discovered_entity` table that aggregates discovered entities across all tenants, with fields {entityName, entityType, tenantIds[], totalMentionCount, firstSeenTime, lastSeenTime}, deduplicated by entityName + entityType.

### REQ-O1-P0-2（Ubiquitous）全局双边关系汇聚表
The system shall maintain a `gwy_global_bilateral_relation` table that aggregates bilateral relations across all tenants, with fields {subjectEntityName, objectEntityName, relationType, tenantIds[], avgScore, sampleCount}, deduplicated by subject + object + relationType.

### REQ-O1-P0-3（Ubiquitous）全局知识图谱汇聚表
The system shall maintain `gwy_global_kg_node` and `gwy_global_kg_edge` tables that aggregate knowledge graph nodes and edges across all tenants, merging nodes by ontologyEntityId and summing cross-tenant edge weights.

### REQ-O2-P0-1（Event-driven）定时汇聚
The system shall run a scheduled aggregation job (default: every hour) that collects all tenant-private discovered entities, bilateral relations, and KG nodes/edges, merges them into the corresponding global tables, and logs the operation in `gwy_global_aggregation_log`.

### REQ-O2-P0-2（Event-driven）事件触发增量汇聚
When a tenant completes the analysis pipeline (report generated), the system shall trigger an incremental aggregation that merges only the newly produced entities/relations/graph data into the global tables.

### REQ-O2-P0-3（Ubiquitous）手动汇聚
The system shall provide `POST /api/workspace/knowledge/aggregate` that allows SUPER_ADMIN to manually trigger a full or incremental knowledge aggregation, returning a summary of merged records.

### REQ-O3-P0-1（Ubiquitous）共享层可见性
The system shall allow all RESEARCHER tenants to read the shared-layer tables (ontology_*, *_global) but restrict write access to SUPER_ADMIN only.

### REQ-O3-P0-2（State-driven）汇聚去重
While aggregating, the system shall deduplicate entities by normalized name (case-insensitive, whitespace-trimmed) + entityType, summing mentionCount and merging tenantId lists, rather than creating duplicate global records.

---

## 3.15 三级知识架构 — 统一层决策（REQ-P*）

### REQ-P1-P0-1（Ubiquitous）统一监测仪表板视图
The system shall provide a `v_unified_monitor` view that aggregates all tenants' gwy_analyze_track_topic data (topicName, articleCount, entityCount, relationCount, reportCount, alertCount) for cross-tenant monitoring.

### REQ-P1-P0-2（Ubiquitous）统一知识图谱视图
The system shall provide a `v_unified_kg` view that aggregates all tenants' gwy_track_kg_node and gwy_track_kg_edge data, enabling cross-tenant knowledge graph queries.

### REQ-P2-P0-1（Ubiquitous）主任决策仪表板工具
The dsh-java-ontology-plugin shall provide a `director_overview` tool that returns the unified-layer dashboard: {totalTenants, totalTopics, totalArticles, totalEntities, totalRelations, totalReports, totalAlerts, tenantActivityRanking[], globalHotspotDistribution[], globalAlertSummary[], globalKgStats}.

### REQ-P2-P0-2（Ubiquitous）主任决策报告
The system shall support `POST /api/display/rptReport/generate` with a `scope=GLOBAL` parameter that generates a cross-tenant decision analysis report, using aggregated global data instead of a single tenant's data.

### REQ-P3-P0-1（State-driven）统一层访问控制
While the requesting user's role_level is 0 (SUPER_ADMIN), the system shall allow access to unified-layer views and cross-tenant aggregation queries; otherwise, access shall be denied with a 403 status.

---

## 3.16 主任决策助手（REQ-Q*）

### REQ-Q1-P0-1（Ubiquitous）主任角色定义
The system shall define a SUPER_ADMIN role with role_level=0, granting cross-tenant visibility to all personal-layer data, shared-layer data, and unified-layer views.

### REQ-Q1-P0-2（Ubiquitous）主任决策 Agent
The dsh-java-ontology-plugin shall register a `director-assistant` Agent with tools {director_overview, knowledge_aggregate, ontology_query, ontology_search, kg_query, kg_search, relation_analysis, report_generate}, whose system_prompt positions it as the research institute director's decision assistant with cross-tenant data access.

### REQ-Q2-P0-1（Ubiquitous）主任工作主线
The system shall support a director workflow: director queries overview → LLM summarizes global status → director asks for decision report → Agent calls report_generate with scope=GLOBAL → LLM generates cross-tenant decision report → director reviews and asks follow-up questions.

### REQ-Q2-P0-2（Ubiquitous）主任知识汇聚触发
The system shall allow the director to trigger knowledge aggregation via natural language (e.g., "汇聚全平台知识"), which the director-assistant Agent interprets and calls the `knowledge_aggregate` tool.

---

## 3.17 非功能性需求（REQ-NF*）

### REQ-NF1-P0-1（Ubiquitous）数据库
The system shall use MySQL as the persistence layer with Spring JdbcTemplate (no JPA/Hibernate), with a database `ontology_db` supporting multi-tenant isolation and three-tier knowledge architecture.

### REQ-NF1-P0-2（Ubiquitous）数据库迁移
The system shall use Flyway or a startup SQL script to initialize all 51 tables idempotently on application startup (41 v1.0 business tables + 10 new tables for three-tier knowledge architecture).

### REQ-NF2-P0-1（Ubiquitous）API 规范
All API endpoints shall be prefixed with `/api/`, return JSON with a unified response wrapper {code, msg, data}, and use standard HTTP status codes.

### REQ-NF3-P0-1（Ubiquitous）插件只读
The dsh-java-ontology-plugin shall only register read-only tools; all write operations (create/update/delete) shall go through the app's REST API.

### REQ-NF4-P0-1（Ubiquitous）端口隔离
The dsh-java-ontology-app shall run on port 8092, separate from the dsh-java Harness on port 8090.

---

# 4. 需求汇总

| 阶段 | 需求数 | P0 | P1 | P2 |
|------|--------|----|----|----|
| A 创建任务 | 5 | 3 | 2 | 0 |
| B 数据采集 | 7 | 4 | 3 | 0 |
| C NLP抽取 | 7 | 6 | 1 | 0 |
| D 知识图谱 | 8 | 8 | 0 | 0 |
| E 关系分析 | 6 | 6 | 0 | 0 |
| F 报告生成 | 9 | 5 | 4 | 0 |
| G 本体支撑 | 7 | 7 | 0 | 0 |
| H 仪表板 | 3 | 2 | 1 | 0 |
| I 插件工具 | 6 | 6 | 0 | 0 |
| J 多租户工作台 | 8 | 7 | 1 | 0 |
| K 互联网爬取 | 7 | 6 | 1 | 0 |
| L 多语种扩展 | 5 | 5 | 0 | 0 |
| M 知识沉淀 | 6 | 4 | 2 | 0 |
| O 共享层汇聚 | 8 | 8 | 0 | 0 |
| P 统一层决策 | 5 | 5 | 0 | 0 |
| Q 主任决策助手 | 4 | 4 | 0 | 0 |
| NF 非功能 | 5 | 5 | 0 | 0 |
| **合计** | **118** | **99** | **18** | **0** |

---

# 5. 工作主线验证场景

## 场景 1：用户A 中东热点多语种分析（完整工作主线）

1. 用户A 注册并登录系统 → 获得 JWT（含 tenantId=T-A）
2. 用户A 进入工作台 → `GET /api/workspace/overview` 查看已有主题/报告
3. 用户A 查询本体热点 → `GET /api/ontology/hotspot` → 选择"中东"（本体共享，所有租户可见）
4. 用户A 创建关键词组 → `POST /api/track/crawlKeywordGroup`：
   - keywordsZh: ["中东","以色列","巴勒斯坦","伊朗"]
   - keywordsEn: ["Middle East","Israel","Palestine","Iran"]
   - keywordsFr: ["Moyen-Orient","Israël","Palestine","Iran"]
   - keywordsAr: ["الشرق الأوسط","إسرائيل","فلسطين","إيران"]
5. 用户A 配置 4 个采集源（zh/en/fr/ar 各一）→ `POST /api/track/crawlSource`
6. 用户A 创建主题 → `POST /api/analyze/trackTopic` {topicName: "中东局势分析", hotspotRegion: "中东", keywordGroupId, crawlSourceIds[4], status: "ACTIVE"}（自动绑定 tenantId=T-A）
7. 系统自动创建 4 个 CrawlTask（每个语种一个）→ 异步执行互联网爬取
8. 爬取完成 → 4 语种文章入库（article.tenantId=T-A，nlpStatus=PENDING）
9. 系统对每篇文章按 language 分派 NLP 策略（ZhNlpStrategy/EnNlpStrategy/FrNlpStrategy/ArNlpStrategy）
10. NLP 完成 → 构建知识图谱（KgNode 含 nodeName 原文 + nodeNameZh 中文归一化，ontologyEntityId 对齐本体）
11. 未匹配实体 → 沉淀到 gwy_common_discovered_entity（tenantId=T-A）
12. 图谱完成 → 计算双边关系（以色列-巴勒斯坦: 冲突 0.9；伊朗-以色列: 对抗 0.8；...）
13. 关系沉淀到 gwy_common_tenant_relation_asset（跨主题复用知识）
14. 告警规则评估 → 满足条件创建 AlertRecord
15. 用户A 生成报告 → `POST /api/display/rptReport/generate`（可指定 language=zh 生成中文报告）
16. 报告含 7 个 section，全部数据属于 tenantId=T-A

## 场景 2：用户B 俄乌追踪 + 知识复用

1. 用户B 登录（tenantId=T-B）→ 工作台
2. 用户B 创建"俄乌关系追踪"主题，关键词含俄/乌/北约
3. 系统建议复用知识 → `GET /api/workspace/knowledge` 发现用户B 之前积累的"俄罗斯"实体
4. 系统爬取→抽取→建图→分析（用户B 私有，用户A 看不到）
5. 用户B 配置告警规则 {ruleName: "冲突升级", condition: "relationScore > 0.85 AND relationType = 冲突"}
6. 新文章入库 → 关系更新 → 满足告警 → AlertRecord
7. 用户B 生成周报 → 报告存入 gwy_display_rpt_report（tenantId=T-B）

## 场景 3：知识沉淀与升级

1. 用户A 在多次分析中反复遇到"哈马斯"实体（mentionCount > 5）
2. 系统建议升级 → `POST /api/ontology/entity/promote` → "哈马斯"从 gwy_common_discovered_entity 升级为 OntologyEntity（共享本体）
3. 后续所有租户的 NLP 抽取都能匹配到"哈马斯"本体实体
4. 用户A 查看知识沉淀仪表板 → `GET /api/workspace/knowledge/dashboard` → 看到知识增长曲线

## 场景 4：主任全平台决策（三级知识架构）

1. 研究院主任登录（role=SUPER_ADMIN, role_level=0）
2. 主任与 director-assistant Agent 对话："给我看全平台本周监测概况"
3. Agent 调用 director_overview 工具 → 返回跨租户聚合数据：
   - 全平台 5 个租户、23 个主题、1,847 篇文章、326 个实体、89 组双边关系
   - 租户活跃度排行：T-A > T-C > T-B > T-E > T-D
   - 全局热点分布：中东 40%、俄乌 25%、南海 20%、台海 15%
   - 全局告警 12 条（3 条 P0）
4. Agent LLM 生成决策摘要 → 流式返回
5. 主任追问："生成全平台中东局势决策报告"
6. Agent 调用 report_generate（scope=GLOBAL）→ 跨租户聚合中东相关数据 → LLM 生成决策报告
7. 主任追问："汇聚全平台知识"
8. Agent 调用 knowledge_aggregate → 触发全量汇聚：
   - 各租户 gwy_common_discovered_entity → gwy_global_discovered_entity（去重合并，共 326 个全局实体）
   - 各租户 gwy_ontology_bilateral_relation → gwy_global_bilateral_relation（去重合并，共 89 组全局关系）
   - 各租户 gwy_track_kg_node/gwy_track_kg_edge → gwy_global_kg_node/gwy_global_kg_edge（按 ontologyEntityId 合并）
   - 记入 gwy_global_aggregation_log
9. 主任查看汇聚后的全局知识图谱 → kg_query（scope=GLOBAL）
