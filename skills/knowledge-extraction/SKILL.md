---
name: knowledge-extraction
description: 知识抽取与本体晋升技能，从监测数据中识别新实体和关系，匹配本体库，将高频发现实体晋升为正式本体实体。当用户需要从文章中提取知识、更新本体库、或晋升发现实体时使用。
when_to_use: 当用户要求从文本中提取实体/关系、更新知识图谱、晋升发现实体到本体库、或执行 NLP 分析时使用此技能。触发词包括"知识抽取"、"实体识别"、"本体晋升"、"更新图谱"等。
---

# 知识抽取技能（Knowledge Extraction）

## 适用场景

从监测文章中提取实体、关系和三元组，匹配本体库，晋升高频实体。

## 工作流程

### 第一步：NLP 分析
1. 调用 `nlp_extract` 工具（action=process）对指定文章执行 NLP
2. 调用 `nlp_extract` 工具（action=getEntities）获取识别实体
3. 调用 `nlp_extract` 工具（action=getTriples）获取三元组
4. 调用 `nlp_extract` 工具（action=getSentiment）获取情感分析

### 第二步：本体匹配
1. 对每个识别到的实体，调用 `ontology_search` 工具搜索本体库
2. 判断实体是否已存在于本体库中
3. 记算实体提及频率和跨租户出现次数

### 第三步：实体晋升
1. 对高频且不在本体库中的实体，调用 `ontology_promote` 工具晋升
   - entityName: 实体名称
   - entityType: PERSON/ORGANIZATION/COUNTRY/TERM
   - category: 目标本体分类
2. 晋升后实体获得本体实体 ID，可被后续分析引用

### 第四步：知识图谱更新
1. 调用 `kg_query` 工具查询图谱节点
2. 新实体自动加入知识图谱
3. 三元组自动转化为图谱边

## 注意事项

- NLP 支持中/英/法/阿四语种自动识别
- 实体晋升需满足频率阈值（默认提及次数 ≥ 3）
- 晋升操作不可逆，需确认实体名称和类型正确
- 知识图谱更新后自动触发增量汇聚
