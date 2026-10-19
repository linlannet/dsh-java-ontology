# 热点区域关系分析工作主线 — 设计文档

> 本文档将 `hotspot-analysis-spec.md` 的 63 条 EARS 需求翻译为可实现的架构设计，定义七阶段流水线、数据流转、状态机、接口契约和数据库 schema。
>
> - **技术栈**：Java 17 + Spring Boot 3.5.16 + Spring JdbcTemplate + MySQL 8.0 (HikariCP 连接池)
> - **根包**：`net.linlan.dsh.ontology`（app）/ `net.linlan.dsh.ontology.plugin`（plugin）
> - **部署**：ontology-app (:8092) + dsh-java Harness (:8090) 加载 ontology-plugin
> - **参考**：`source/gwy` 4 大业务域 41 实体 + sharptoolbox v9 七模型 YAML 格式

---

# 1. 架构总览

## 1.1 分层架构

```
┌─────────────────────────────────────────────────────────┐
│                  dsh-java-ontology-plugin                │
│         (只读工具：ontology_query / kg_query /            │
│          relation_analysis / report_generate /            │
│          alert_query / ontology_search)                   │
│                    via HTTP → app                         │
└──────────────────────────┬──────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────┐
│                   dsh-java-ontology-app (:8092)          │
│  ┌─────────────────────────────────────────────────────┐ │
│  │              Controller Layer (REST API)             │ │
│  │  OntologyController / TrackController /              │ │
│  │  AnalyzeController / DisplayController               │ │
│  └──────────────────────┬──────────────────────────────┘ │
│  ┌──────────────────────▼──────────────────────────────┐ │
│  │               Service Layer (业务逻辑)               │ │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐    │ │
│  │  │ontology │ │ track   │ │ analyze │ │ display │    │ │
│  │  │ Service │ │ Service │ │ Service │ │ Service │    │ │
│  │  └─────────┘ └─────────┘ └─────────┘ └─────────┘    │ │
│  │           WorklineOrchestrator (跨域编排)             │ │
│  └──────────────────────┬──────────────────────────────┘ │
│  ┌──────────────────────▼──────────────────────────────┐ │
│  │            Repository Layer (数据访问)                │ │
│  │     Spring JdbcTemplate + RowMapper                  │ │
│  └──────────────────────┬──────────────────────────────┘ │
│  ┌──────────────────────▼──────────────────────────────┐ │
│  │              MySQL (ontology_db, 51 tables)           │ │
│  └─────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

## 1.2 包结构

```
net.linlan.dsh.ontology
├── config/           # DataSource, WebConfig, JacksonConfig
├── common/           # BaseEntity, PageResult, Result, BaseController, BaseService
├── ontology/         # 阶段 G — 本体知识支撑
│   ├── controller/
│   ├── service/
│   ├── repository/
│   └── model/        # Country, Organization, Person, Hotspot, Term, EventType, ...
├── track/            # 阶段 B+D — 数据采集+知识图谱
│   ├── controller/
│   ├── service/
│   ├── repository/
│   └── model/        # Article, CrawlTask, CrawlSource, KgNode, KgEdge, NerElement, ...
├── analyze/          # 阶段 A+C+E — 任务创建+NLP抽取+关系分析
│   ├── controller/
│   ├── service/
│   ├── repository/
│   └── model/        # TrackTopic, NlpEntity, NlpTriple, NlpSentiment, BilateralRelation, AlertRule, ...
├── display/          # 阶段 F — 报告生成
│   ├── controller/
│   ├── service/
│   ├── repository/
│   └── model/        # RptTemplate, RptReport, AlertRecord, InformationPublish, ...
└── workline/         # 跨阶段编排
    ├── WorklineOrchestrator.java
    ├── WorklineStage.java (enum)
    └── dto/
```

---

# 2. 七阶段流水线设计

## 2.1 阶段 A — 创建分析任务

### 涉及实体

| 实体 | 表名 | 域 | 关键字段 |
|------|------|-----|---------|
| TrackTopic | gwy_analyze_track_topic | analyze | topicName, hotspotRegion, keywordGroupId, status, articleCount, entityCount, relationCount, reportCount |
| CrawlKeywordGroup | gwy_track_crawl_keyword_group | track | groupName, keywordsZh(JSON), keywordsEn(JSON), matchMode |

### API 端点

| 方法 | 路径 | 说明 | 需求 |
|------|------|------|------|
| POST | /api/track/crawlKeywordGroup | 创建关键词组 | REQ-A1-P0-2 |
| GET | /api/track/crawlKeywordGroup/list | 查询关键词组 | |
| POST | /api/analyze/trackTopic | 创建追踪主题 | REQ-A1-P0-1 |
| PUT | /api/analyze/trackTopic | 更新主题（含状态变更） | REQ-A2-P0-2 |
| GET | /api/analyze/trackTopic/list | 查询主题 | |
| GET | /api/ontology/hotspot | 查询本体热点（供选择） | REQ-A3-P1-2 |

### 状态流转

```
DRAFT ──用户激活──→ ACTIVE ──采集完成──→ ANALYZING ──报告生成──→ COMPLETED
  │                    │
  └──删除──→ DELETED   └──暂停──→ PAUSED ──恢复──→ ACTIVE
```

### 编排逻辑

`TrackTopicService.activate(topicId)`:
1. 校验 keywordGroupId 和 crawlSourceIds 非空
2. 更新 status = ACTIVE
3. 调用 `CrawlTaskService.createForTopic(topicId)` 创建采集任务
4. 返回任务详情

---

## 2.2 阶段 B — 数据采集

### 涉及实体

| 实体 | 表名 | 域 | 关键字段 |
|------|------|-----|---------|
| CrawlSource | gwy_track_crawl_source | track | sourceName, sourceUrl, sourceType, language, crawlFrequency |
| CrawlSubscription | gwy_track_crawl_subscription | track | subscriptionName, sourceId, keywordGroupId, subscribeStatus |
| CrawlTask | gwy_track_crawl_task | track | taskName, sourceId, keywordGroupId, topicId, taskStatus, scheduledTime |
| Article | gwy_track_article | track | title, content, sourceUrl, language, publishTime, sourceId, topicId, nlpStatus, kgStatus |
| DataImport | gwy_track_data_import | track | importName, topicId, fileType, totalCount, successCount |
| DataExport | gwy_track_data_export | track | exportName, topicId, filters, fileType, totalCount |

### API 端点

| 方法 | 路径 | 说明 | 需求 |
|------|------|------|------|
| CRUD | /api/track/crawlSource | 采集源管理 | REQ-B1-P0-1 |
| CRUD | /api/track/crawlSubscription | 采集订阅管理 | REQ-B1-P0-2 |
| CRUD | /api/track/crawlTask | 采集任务管理 | REQ-B1-P0-3 |
| CRUD | /api/track/gwy_track_article | 文章管理 | REQ-B2-P0-2 |
| POST | /api/track/crawlTask/{id}/complete | 采集完成回调 | REQ-B2-P0-1 |
| POST | /api/track/dataImport | 批量导入文章 | REQ-B3-P1-1 |
| POST | /api/track/dataExport | 批量导出文章 | REQ-B3-P1-2 |

### 编排逻辑

`CrawlTaskService.complete(taskId, articles[])`:
1. 存储文章到 `gwy_track_article` 表，每篇设置 `nlpStatus=PENDING`, `kgStatus=PENDING`
2. 更新 CrawlTask.status = COMPLETED
3. 更新 TrackTopic.articleCount += articles.length
4. 触发阶段 C：对每篇文章调用 `NlpExtractionService.extractForArticle(articleId)`

---

## 2.3 阶段 C — NLP 抽取

### 涉及实体

| 实体 | 表名 | 域 | 关键字段 |
|------|------|-----|---------|
| NlpEntity | gwy_analyze_nlp_entity | analyze | articleId, entityName, entityType, startPos, endPos, confidence |
| NlpTriple | gwy_analyze_nlp_triple | analyze | articleId, subject, predicate, object, confidence |
| NlpSentiment | gwy_analyze_nlp_sentiment | analyze | articleId, overallScore, positiveScore, negativeScore, neutralScore, keyPhrases(JSON) |

### API 端点

| 方法 | 路径 | 说明 | 需求 |
|------|------|------|------|
| POST | /api/analyze/nlpEntity/extract | 实体抽取 | REQ-C1-P0-1 |
| CRUD | /api/analyze/nlpEntity | 实体管理 | REQ-C1-P0-2 |
| POST | /api/analyze/nlpTriple/extract | 三元组抽取 | REQ-C2-P0-1 |
| CRUD | /api/analyze/nlpTriple | 三元组管理 | REQ-C2-P0-2 |
| POST | /api/analyze/nlpSentiment/analyze | 情感分析 | REQ-C3-P0-1 |
| CRUD | /api/analyze/nlpSentiment | 情感管理 | REQ-C3-P0-2 |

### 编排逻辑

`NlpExtractionService.extractForArticle(articleId)`:
1. 加载 Article.content
2. 调用 NLP 服务（外部 HTTP 或内置规则）抽取实体 → 存储 NlpEntity
3. 抽取三元组 → 存储 NlpTriple
4. 情感分析 → 存储 NlpSentiment
5. 更新 Article.nlpStatus = COMPLETED
6. 触发阶段 D：调用 `KgBuildService.buildForArticle(articleId)`

### NLP 服务抽象

```java
public interface NlpServiceClient {
    List<NlpEntity> extractEntities(String text, String language);
    List<NlpTriple> extractTriples(String text, String language);
    NlpSentiment analyzeSentiment(String text, String language);
}
```

实现策略：
- `RuleBasedNlpClient`：基于本体术语库的规则匹配（P0，无需外部服务）
- `HttpNlpClient`：调用外部 NLP 服务（P1，可配置 URL）

---

## 2.4 阶段 D — 知识图谱构建

### 涉及实体

| 实体 | 表名 | 域 | 关键字段 |
|------|------|-----|---------|
| NerElement | gwy_track_ner_element | track | elementName, elementType, standardName, ontologyEntityId, frequency, topicId |
| KgNode | gwy_track_kg_node | track | nodeId, nodeType, nodeName, ontologyEntityId, properties(JSON), topicId |
| KgEdge | gwy_track_kg_edge | track | edgeId, sourceNodeId, targetNodeId, relationType, weight, topicId, evidenceText |
| KgBuildLog | gwy_track_kg_build_log | track | topicId, articleId, nodesAdded, edgesAdded, nodesMerged, buildDuration |

### API 端点

| 方法 | 路径 | 说明 | 需求 |
|------|------|------|------|
| CRUD | /api/track/nerElement | NER 元素管理 | REQ-D1-P0-1 |
| CRUD | /api/track/kgNode | 图谱节点管理 | REQ-D2-P0-1 |
| CRUD | /api/track/kgEdge | 图谱边管理 | REQ-D2-P0-2 |
| GET | /api/track/dashboard/kg | 图谱统计 | REQ-H1-P0-2 |
| GET | /api/track/dashboard/kg/search | 图谱搜索 | REQ-D4-P0-3 |
| GET | /api/track/dashboard/kg/node/{nodeId} | 节点详情 | REQ-D4-P0-1 |
| GET | /api/track/dashboard/kg/node/{nodeId}/neighbors | 邻居查询 | REQ-D4-P0-2 |

### 编排逻辑

`KgBuildService.buildForArticle(articleId)`:
1. 加载文章的所有 NlpEntity → 对每个实体：
   - 查找已有 KgNode（by nodeName + nodeType + topicId），存在则合并（更新 properties、追加 articleId）
   - 不存在则创建新 KgNode
   - 尝试对齐本体：调用 `OntologyMatchService.match(entityName, entityType)` → 设置 ontologyEntityId
2. 加载文章的所有 NlpTriple → 对每个三元组：
   - 查找/创建 source KgNode（subject）
   - 查找/创建 target KgNode（object）
   - 创建 KgEdge（source→target, relationType=predicate, weight=confidence）
3. 更新 NerElement 频率统计
4. 存储 KgBuildLog
5. 更新 Article.kgStatus = COMPLETED
6. 更新 TrackTopic.entityCount
7. 检查是否所有文章已处理完 → 触发阶段 E

---

## 2.5 阶段 E — 关系分析

### 涉及实体

| 实体 | 表名 | 域 | 关键字段 |
|------|------|-----|---------|
| BilateralRelation | gwy_ontology_bilateral_relation | analyze | subjectEntityId, subjectEntityType, objectEntityId, objectEntityType, relationType, relationScore, trend, topicId, evidenceArticleIds(JSON), analysisSummary |
| TrackElement | gwy_analyze_track_element | analyze | elementName, elementType, topicId, ontologyEntityId, mentionCount, firstMentionTime, lastMentionTime |
| AlertRule | gwy_analyze_alert_rule | analyze | ruleName, topicId, ruleType, conditionExpression, threshold, severity, enabled |

### API 端点

| 方法 | 路径 | 说明 | 需求 |
|------|------|------|------|
| CRUD | /api/analyze/bilateralRelation | 双边关系管理 | REQ-E1-P0-1 |
| POST | /api/analyze/bilateralRelation/compute | 触发关系计算 | REQ-E1-P0-2 |
| CRUD | /api/analyze/trackElement | 追踪元素管理 | REQ-E2-P0-1 |
| CRUD | /api/analyze/alertRule | 告警规则管理 | REQ-E3-P0-1 |

### 编排逻辑

`RelationAnalysisService.computeForTopic(topicId)`:
1. 查询 topic 下所有 KgNode（按 nodeType 筛选国家/组织级实体）
2. 对每对实体 (A, B)：
   - 查询 A→B 和 B→A 的所有 KgEdge
   - 聚合 relationType（取众数或加权）
   - 计算 relationScore（边权重均值 × 情感因子）
   - 计算 trend（按文章时间分段比较 score 变化）
   - 收集 evidenceArticleIds
   - 存储/更新 BilateralRelation
3. 更新 TrackElement 统计
4. 检查所有 AlertRule → 对满足条件的创建 AlertRecord（阶段 F）
5. 更新 TrackTopic.relationCount

### 告警触发

`AlertService.evaluate(topicId)`:
1. 查询 topic 的所有 enabled AlertRule
2. 对每条规则，查询匹配的 BilateralRelation / TrackElement
3. 评估 conditionExpression（简单表达式引擎：`relationScore > 0.8 AND relationType = '冲突'`）
4. 满足条件 → 创建 AlertRecord {ruleId, topicId, alertType, severity, triggerValue, alertTime, alertContent}

---

## 2.6 阶段 F — 报告生成

### 涉及实体

| 实体 | 表名 | 域 | 关键字段 |
|------|------|-----|---------|
| RptTemplate | gwy_display_rpt_template | display | templateName, templateType, sections(JSON), chartConfigs(JSON) |
| RptReport | gwy_display_rpt_report | display | reportName, topicId, templateId, status, generatedContent(TEXT), chartData(JSON), summary, conclusion, recommendations(JSON) |
| AlertRecord | gwy_display_alert_record | display | ruleId, topicId, alertType, severity, triggerValue, alertTime, alertContent |
| InformationPublish | gwy_display_information_publish | display | title, content, topicId, reportId, publishStatus |
| PushRecord | gwy_display_push_record | display | pushTarget, pushType, content, reportId, pushStatus |
| RecommendationRecord | gwy_display_recommendation_record | display | topicId, recommendationType, content, score, basis |
| SearchIndex | gwy_display_search_index | display | indexName, entityType, entityId, topicId, keywords, indexedContent |
| ConferenceInfo | gwy_display_conference_info | display | conferenceName, conferenceTime, location, participants, topicId |

### API 端点

| 方法 | 路径 | 说明 | 需求 |
|------|------|------|------|
| CRUD | /api/display/rptTemplate | 报告模板管理 | REQ-F1-P0-1 |
| CRUD | /api/display/rptReport | 报告管理 | REQ-F1-P0-2 |
| POST | /api/display/rptReport/generate | 生成报告 | REQ-F2-P0-1 |
| CRUD | /api/display/alertRecord | 告警记录管理 | REQ-F3-P0-1 |
| CRUD | /api/display/informationPublish | 信息发布 | REQ-F3-P0-2 |
| CRUD | /api/display/pushRecord | 推送记录 | REQ-F4-P1-1 |
| CRUD | /api/display/recommendationRecord | 推荐记录 | REQ-F4-P1-2 |
| CRUD | /api/display/searchIndex | 搜索索引 | REQ-F5-P1-1 |
| CRUD | /api/display/conferenceInfo | 会议信息 | REQ-F6-P1-1 |
| GET | /api/display/dashboard | 展示仪表板 | REQ-H2-P1-1 |

### 报告生成逻辑

`ReportService.generate(topicId, templateId)`:
1. 加载 RptTemplate.sections 配置
2. 按 section 类型查询数据：
   - `OVERVIEW` → TrackTopic 统计（articleCount, entityCount, relationCount）
   - `KEY_ENTITIES` → TrackElement TOP N（by mentionCount）+ 关联本体属性
   - `RELATION_MATRIX` → BilateralRelation 全集
   - `KG_SUMMARY` → KgNode/KgEdge 统计 + 关键路径
   - `SENTIMENT_TREND` → NlpSentiment 按时间聚合
   - `ALERT_SUMMARY` → AlertRecord 全集
   - `RECOMMENDATIONS` → RecommendationRecord 全集
3. 装配 generatedContent（Markdown/HTML）
4. 装配 chartData（JSON，供前端图表渲染）
5. 生成 summary 和 conclusion
6. 存储 RptReport（status=COMPLETED）
7. 更新 TrackTopic.reportCount

---

## 2.7 阶段 G — 本体知识支撑

### 涉及实体（13 个）

| 实体 | 表名 | 关键字段 |
|------|------|---------|
| Country | gwy_ontology_country | countryCode, nameZh, nameEn, region, importance |
| Organization | gwy_ontology_organization | orgCode, nameZh, nameEn, orgType, memberCount |
| Person | gwy_ontology_person | name, nameEn, gender, birthDate, country, occupation |
| Term | gwy_ontology_term | termZh, termEn, category, keywordsZh, keywordsEn |
| EventType | gwy_ontology_event_type | typeCode, nameZh, nameEn, subTypes, keywords |
| Hotspot | gwy_ontology_hotspot | nameZh, nameEn, region, keyCountries, riskLevel |
| Location | gwy_ontology_location | name, nameEn, locType, parentId, country |
| Media | gwy_ontology_media | name, mediaType, country, language, biasRating |
| Book | gwy_ontology_book | title, author, publisher, isbn |
| Paper | gwy_ontology_paper | title, author, institution, journal, doi |
| OntologyEntity | gwy_ontology_entity | category, entityName, entityNameEn, wikiUrl |
| OntologyVersion | gwy_ontology_version | ontologyType, version, recordCount, filePath |
| NlpVersion | gwy_analyze_nlp_version | modelName, modelVersion, modelType, accuracy, f1 |

### API 端点

| 方法 | 路径 | 说明 | 需求 |
|------|------|------|------|
| CRUD | /api/ontology/country | 国家本体 | REQ-G1-P0-1 |
| CRUD | /api/ontology/organization | 组织本体 | REQ-G1-P0-2 |
| CRUD | /api/ontology/person | 人物本体 | REQ-G1-P0-3 |
| CRUD | /api/ontology/hotspot | 热点本体 | REQ-G2-P0-1 |
| CRUD | /api/ontology/term | 术语本体 | REQ-G2-P0-2 |
| CRUD | /api/ontology/eventType | 事件类型本体 | |
| CRUD | /api/ontology/location | 地点本体 | |
| CRUD | /api/ontology/media | 媒体本体 | |
| CRUD | /api/ontology/book | 书籍本体 | |
| CRUD | /api/ontology/paper | 论文本体 | |
| CRUD | /api/ontology/entity | 通用本体实体 | |
| CRUD | /api/ontology/version | 本体版本 | |
| CRUD | /api/ontology/nlpVersion | NLP 版本 | |
| POST | /api/ontology/query | 综合查询 | REQ-G3-P0-1 |
| GET | /api/ontology/search | 关键词搜索 | REQ-G3-P0-2 |

### 本体对齐服务

`OntologyMatchService.match(entityName, entityType)`:
1. 按 entityType 映射到本体表（PERSON→Person, ORG→Organization, GPE/COUNTRY→Country, ...）
2. 精确匹配 name/nameEn/nameZh
3. 模糊匹配 alias/keywords
4. 返回 ontologyEntityId 或 null

---

# 3. 跨阶段编排器

## 3.1 WorklineOrchestrator

```java
@Service
public class WorklineOrchestrator {

    // 阶段 A → B：任务激活触发采集
    public void onTopicActivated(Long topicId) {
        crawlTaskService.createForTopic(topicId);
    }

    // 阶段 B → C：文章入库触发 NLP
    public void onArticleStored(Long articleId) {
        nlpExtractionService.extractForArticle(articleId);
    }

    // 阶段 C → D：NLP 完成触发图谱构建
    public void onNlpCompleted(Long articleId) {
        kgBuildService.buildForArticle(articleId);
    }

    // 阶段 D → E：图谱构建完成触发关系分析
    public void onKgBuildCompleted(Long topicId) {
        if (allArticlesProcessed(topicId)) {
            relationAnalysisService.computeForTopic(topicId);
            alertService.evaluate(topicId);
        }
    }

    // 阶段 E → F：关系分析完成可生成报告
    public void onAnalysisCompleted(Long topicId) {
        trackTopicService.updateStatus(topicId, "ANALYZING");
    }
}
```

## 3.2 状态机

```
TrackTopic 状态：
  DRAFT → ACTIVE → ANALYZING → COMPLETED
                    ↑
  Article 状态：    │
  PENDING → NLP_PROCESSING → KG_PROCESSING → COMPLETED
                ↓ (失败)        ↓ (失败)
            NLP_FAILED      KG_FAILED

  CrawlTask 状态：
  PENDING → RUNNING → COMPLETED / FAILED

  RptReport 状态：
  DRAFT → GENERATING → COMPLETED / FAILED
```

---

# 4. 数据库 Schema（工作主线核心表）

> 完整 41 张表见 `02-数据模型设计.md`，此处仅列出工作主线核心表的关键字段。

```sql
-- 阶段 A
CREATE TABLE gwy_analyze_track_topic (
  id BIGINT PRIMARY KEY AUTOINCREMENT,
  topic_name VARCHAR(200) NOT NULL,
  hotspot_region VARCHAR(100),
  keyword_group_id BIGINT,
  status VARCHAR(20) DEFAULT 'DRAFT',
  article_count INT DEFAULT 0,
  entity_count INT DEFAULT 0,
  relation_count INT DEFAULT 0,
  report_count INT DEFAULT 0,
  created_by VARCHAR(64), created_at DATETIME,
  updated_by VARCHAR(64), updated_at DATETIME,
  del_flag CHAR(1) DEFAULT '0'
);

-- 阶段 B
CREATE TABLE gwy_track_article (
  id BIGINT PRIMARY KEY AUTOINCREMENT,
  title VARCHAR(500) NOT NULL,
  content TEXT,
  source_url VARCHAR(500),
  language VARCHAR(10),
  publish_time DATETIME,
  source_id BIGINT,
  topic_id BIGINT,
  crawl_task_id BIGINT,
  nlp_status VARCHAR(20) DEFAULT 'PENDING',
  kg_status VARCHAR(20) DEFAULT 'PENDING',
  nlp_error TEXT,
  ...
);

-- 阶段 C
CREATE TABLE gwy_analyze_nlp_entity (
  id BIGINT PRIMARY KEY AUTOINCREMENT,
  article_id BIGINT NOT NULL,
  entity_name VARCHAR(200) NOT NULL,
  entity_type VARCHAR(50) NOT NULL,
  start_pos INT, end_pos INT,
  confidence DECIMAL(5,4),
  ...
);

-- 阶段 D
CREATE TABLE gwy_track_kg_node (
  id BIGINT PRIMARY KEY AUTOINCREMENT,
  node_id VARCHAR(100) NOT NULL,
  node_type VARCHAR(50) NOT NULL,
  node_name VARCHAR(200) NOT NULL,
  ontology_entity_id BIGINT,
  properties TEXT,
  topic_id BIGINT NOT NULL,
  article_ids TEXT,
  ...
);

CREATE TABLE gwy_track_kg_edge (
  id BIGINT PRIMARY KEY AUTOINCREMENT,
  edge_id VARCHAR(100) NOT NULL,
  source_node_id BIGINT NOT NULL,
  target_node_id BIGINT NOT NULL,
  relation_type VARCHAR(100) NOT NULL,
  weight DECIMAL(5,4) DEFAULT 1.0,
  topic_id BIGINT NOT NULL,
  article_ids TEXT,
  evidence_text TEXT,
  ...
);

-- 阶段 E
CREATE TABLE gwy_ontology_bilateral_relation (
  id BIGINT PRIMARY KEY AUTOINCREMENT,
  subject_entity_id BIGINT NOT NULL,
  subject_entity_type VARCHAR(50) NOT NULL,
  object_entity_id BIGINT NOT NULL,
  object_entity_type VARCHAR(50) NOT NULL,
  relation_type VARCHAR(50) NOT NULL,
  relation_score DECIMAL(5,4),
  trend VARCHAR(20),
  topic_id BIGINT NOT NULL,
  evidence_article_ids TEXT,
  analysis_summary TEXT,
  ...
);

-- 阶段 F
CREATE TABLE gwy_display_rpt_report (
  id BIGINT PRIMARY KEY AUTOINCREMENT,
  report_name VARCHAR(200) NOT NULL,
  topic_id BIGINT NOT NULL,
  template_id BIGINT,
  status VARCHAR(20) DEFAULT 'DRAFT',
  generated_content TEXT,
  chart_data TEXT,
  summary TEXT,
  conclusion TEXT,
  recommendations TEXT,
  generated_time DATETIME,
  ...
);
```

---

# 5. 插件工具设计

dsh-java-ontology-plugin 向 Harness 注册 6 个只读工具：

| 工具名 | 输入 | 输出 | 对应需求 |
|--------|------|------|---------|
| ontology_query | {query: string} | 本体实体列表 | REQ-I1-P0-1 |
| ontology_search | {keywords: string} | 匹配实体+相关度 | REQ-I1-P0-2 |
| kg_query | {nodeId 或 entityName, depth?: int} | 节点+邻居+边 | REQ-I2-P0-1 |
| relation_analysis | {topicId} | BilateralRelation 列表 | REQ-I2-P0-2 |
| report_generate | {topicId, templateId} | 报告摘要 | REQ-I3-P0-1 |
| alert_query | {topicId} | AlertRecord 列表 | REQ-I3-P0-2 |

所有工具通过 `AdminApiClient`（HTTP）调用 app 的 REST API，插件本身不直接访问数据库。

---

# 6. 多租户工作台架构（阶段 J）

## 6.1 数据模型

```sql
CREATE TABLE sys_user (
  id BIGINT PRIMARY KEY AUTOINCREMENT,
  username VARCHAR(64) UNIQUE NOT NULL,
  password_hash VARCHAR(256) NOT NULL,
  display_name VARCHAR(100),
  email VARCHAR(200),
  tenant_id VARCHAR(36) NOT NULL,  -- UUID
  status VARCHAR(20) DEFAULT 'ACTIVE',
  created_at DATETIME,
  updated_at DATETIME
);
```

所有工作主线表追加 `tenant_id VARCHAR(36)` 列，本体表（ontology_*）不追加。

## 6.2 租户上下文

```java
@Component
public class TenantContext {
    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    public static void setTenantId(String tenantId) { CURRENT_TENANT.set(tenantId); }
    public static String getTenantId() { return CURRENT_TENANT.get(); }
    public static void clear() { CURRENT_TENANT.remove(); }
}
```

## 6.3 JWT 认证

```java
@Component
public class JwtAuthFilter implements OncePerRequestFilter {
    // 从 Authorization header 提取 JWT → 解析 tenantId → TenantContext.setTenantId()
    // 白名单：/api/auth/register, /api/auth/login
}
```

## 6.4 数据隔离机制

**BaseRepository** 的所有查询自动追加 `WHERE tenant_id = ?`：

```java
public abstract class BaseRepository<T extends BaseEntity> {
    protected String currentTenantId() {
        return TenantContext.getTenantId();
    }
    // list/get/insert/update/delete 全部自动注入 tenantId
}
```

**OntologyRepository** 不继承 BaseRepository 的租户过滤（本体共享）。

## 6.5 工作台 API

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/register | 用户注册 |
| POST | /api/auth/login | 用户登录 → JWT |
| GET | /api/workspace/overview | 工作台概览 |
| GET | /api/workspace/topics | 我的主题列表 |
| GET | /api/workspace/knowledge | 知识资产 |
| GET | /api/workspace/knowledge/dashboard | 知识沉淀仪表板 |

---

# 7. 互联网爬取引擎（阶段 K）

## 7.1 架构

```java
@Service
public class CrawlEngine {
    private final HttpClient httpClient;  // Java 11+ HttpClient
    private final RateLimiter rateLimiter; // 1 req/sec per source

    public CrawlResult execute(CrawlTask task) {
        // 1. 加载 CrawlSource + CrawlKeywordGroup
        // 2. 按 source.language 选择关键词（keywordsZh/En/Fr/Ar）
        // 3. 构造搜索 URL（source-specific SearchUrlBuilder）
        // 4. 限流爬取 → Jsoup.parse(html) → 提取文章列表
        // 5. 对每篇文章提取 title/content/publishTime
        // 6. 返回 CrawlResult {articles[], sourceId, language}
    }
}
```

## 7.2 搜索 URL 构造

每 种 CrawlSource.sourceType 对应一个 `SearchUrlBuilder`：

| sourceType | 搜索引擎 | URL 模板 |
|-----------|---------|---------|
| GOOGLE_NEWS | Google News | `https://news.google.com/search?q={keywords}&hl={lang}` |
| BING_NEWS | Bing News | `https://www.bing.com/news/search?q={keywords}&setlang={lang}` |
| RSS | RSS Feed | 直接解析 RSS XML |
| CUSTOM | 自定义 | `sourceUrl + keywords` |

## 7.3 异步调度

```java
@Service
public class CrawlScheduler {
    // @Scheduled(fixedDelay = 60000) 每分钟检查待执行 CrawlTask
    // 对 scheduledTime <= now 且 status=PENDING 的任务：
    //   → 提交到 ThreadPoolExecutor
    //   → CrawlEngine.execute(task)
    //   → ArticleService.storeArticles(result, task.topicId, task.tenantId)
    //   → WorklineOrchestrator.onArticleStored() per gwy_track_article
}
```

## 7.4 多语种源配置示例

```
CrawlSource 1: {sourceName: "新华社中文", language: "zh", sourceType: "RSS", sourceUrl: "http://..."}
CrawlSource 2: {sourceName: "Reuters EN", language: "en", sourceType: "GOOGLE_NEWS"}
CrawlSource 3: {sourceName: "AFP FR", language: "fr", sourceType: "BING_NEWS"}
CrawlSource 4: {sourceName: "Al Jazeera AR", language: "ar", sourceType: "RSS", sourceUrl: "http://..."}
```

---

# 8. 多语种 NLP 策略（阶段 L）

## 8.1 语种分派

```java
@Service
public class NlpStrategyDispatcher {
    private final Map<String, NlpStrategy> strategies;  // {zh, en, fr, ar}

    public NlpResult extract(String text, String language) {
        NlpStrategy strategy = strategies.get(language);
        if (strategy == null) strategy = strategies.get("en"); // fallback
        return strategy.extract(text);
    }
}

public interface NlpStrategy {
    List<NlpEntity> extractEntities(String text);
    List<NlpTriple> extractTriples(String text);
    NlpSentiment analyzeSentiment(String text);
}
```

## 8.2 规则匹配策略（按语种）

每种语种的 `RuleBasedNlpStrategy` 使用对应语种的本体术语：

| 语种 | 实体匹配源 | 情感词典 |
|------|-----------|---------|
| zh | Term.termZh, Country.nameZh, Person.name | sentiment_zh.json |
| en | Term.termEn, Country.nameEn, Person.nameEn | sentiment_en.json |
| fr | Term.termFr, Country.nameFr, Person.nameEn | sentiment_fr.json |
| ar | Term.termAr, Country.nameAr, Person.nameEn | sentiment_ar.json |

## 8.3 跨语种图谱归一化

KgNode 存储双名称：

```sql
CREATE TABLE gwy_track_kg_node (
  ...
  node_name VARCHAR(200) NOT NULL,      -- 原文语种名称
  node_name_zh VARCHAR(200),            -- 中文归一化名称
  language VARCHAR(10),                 -- 原文语种
  ontology_entity_id BIGINT,            -- 对齐到本体（跨语种锚点）
  ...
);
```

跨语种合并逻辑：两个 KgNode 若 `ontology_entity_id` 相同则视为同一实体，合并节点（保留双名称）。

---

# 9. 知识沉淀机制（阶段 M）

## 9.1 数据模型

```sql
CREATE TABLE gwy_common_discovered_entity (
  id BIGINT PRIMARY KEY AUTOINCREMENT,
  tenant_id VARCHAR(36) NOT NULL,
  entity_name VARCHAR(200) NOT NULL,
  entity_type VARCHAR(50) NOT NULL,
  source_topic_id BIGINT,
  first_seen_time DATETIME,
  mention_count INT DEFAULT 1,
  promoted_to_ontology BIGINT,  -- 升级后的 gwy_ontology_entity.id
  ...
);

CREATE TABLE gwy_common_tenant_relation_asset (
  id BIGINT PRIMARY KEY AUTOINCREMENT,
  tenant_id VARCHAR(36) NOT NULL,
  subject_entity_id BIGINT NOT NULL,
  object_entity_id BIGINT NOT NULL,
  relation_type VARCHAR(50) NOT NULL,
  avg_score DECIMAL(5,4),
  topic_ids TEXT,  -- JSON array
  occurrence_count INT DEFAULT 1,
  last_updated DATETIME,
  ...
);
```

## 9.2 沉淀流程

```
KgNode 创建（ontologyEntityId=null）
  → 查找 gwy_common_discovered_entity（by tenantId + entityName + entityType）
  → 存在：mention_count++，更新 last_seen_time
  → 不存在：创建新 gwy_common_discovered_entity

BilateralRelation 计算
  → 查找 gwy_common_tenant_relation_asset（by tenantId + subjectEntityId + objectEntityId）
  → 存在：更新 avg_score，追加 topicId
  → 不存在：创建新 gwy_common_tenant_relation_asset
```

## 9.3 实体升级到本体

```java
@Service
public class KnowledgePromotionService {
    public OntologyEntity promote(Long discoveredEntityId) {
        // 1. 加载 gwy_common_discovered_entity
        // 2. 创建 gwy_ontology_entity（共享本体）
        // 3. 更新所有相关 KgNode.ontology_entity_id
        // 4. 更新 gwy_common_discovered_entity.promoted_to_ontology
        // 5. 返回 OntologyEntity
    }

    public List<DiscoveredEntity> getPromotionCandidates(String tenantId) {
        // 返回 mention_count >= threshold 且 promoted_to_ontology IS NULL 的实体
    }
}
```

## 9.4 跨主题知识复用

用户创建新 TrackTopic 时，系统自动建议：

```java
@Service
public class KnowledgeSuggestionService {
    public KnowledgeSuggestion suggest(String tenantId, List<String> keywords) {
        // 1. 从 gwy_common_discovered_entity 匹配关键词 → 建议相关实体
        // 2. 从 gwy_common_tenant_relation_asset 匹配 → 建议已知关系
        // 3. 返回 {suggestedEntities, suggestedRelations}
    }
}
```

---

# 10. 与七模型 YAML 的对接

工作主线七阶段可映射到 sharptoolbox v9 七模型 YAML：

| 工作主线阶段 | v9 模型 | YAML 文件 |
|-------------|---------|-----------|
| J 工作台 | M5 主体（RBAC + 租户）| m5-actor-model.yaml |
| A 创建任务 | M2 行为（COMMAND: createTopic）| m2-behavior-model.yaml |
| B+K 数据采集 | M1 对象（Article 聚合根）+ M2 行为（crawl）| m1/m2 |
| C+L NLP 抽取 | M3 规则（VALIDATION + DERIVATION，多语种）| m3-rule-model.yaml |
| D 知识图谱 | M1 对象（KgNode/KgEdge 聚合根）| m1-object-model.yaml |
| E 关系分析 | M3 规则（CALCULATION + RISK）| m3-rule-model.yaml |
| F 报告生成 | M7 查询报表（多语种）| m7-report-model.yaml |
| G 本体支撑 | M1 对象（本体聚合根，共享）| m1-object-model.yaml |
| M 知识沉淀 | M2 行为（syncTriggers: 沉淀+升级）| m2-behavior-model.yaml |
| 跨阶段 | M2 行为（syncTriggers）| m2-behavior-model.yaml |
| 权限 | M5 主体（租户隔离）| m5-actor-model.yaml |
| 审批 | M6 流程 | m6-flow-model.yaml |
| 界面 | MU UI（工作台界面）| mu-ui-model.yaml |

> 可选：在 `src/main/resources/ontology-models/` 下为工作主线建一套 7 文件 + manifest，用 YAML 驱动 schema 生成和校验。P2 优先级，不阻塞工作主线实现。
