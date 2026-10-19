---
name: hotspot-analysis
description: 一句话驱动的四步热点分析。用户给出区域+语种+主题+时间范围，自动执行数据接入→数据整理→建模分析→发布本体，输出分析报告。
when_to_use: 当用户要求分析国际热点、监测区域局势、追踪主题舆情时自动触发。
---

# 热点分析技能（四步法）

## 指令分解

从用户指令提取：区域、语种、主题列表、时间范围、分析目标

## 四步流程

### 第一步：数据接入
1. topic_create — 为每个主题创建监测主题
2. kg_search — 查询已有相关文章
3. crawl_execute — 爬取新文章（指定语种）

### 第二步：数据整理
1. nlp_extract(processPending) — 四语种 NLP
2. nlp_extract(getEntities/getTriples/getSentiment) — 获取结果
3. OntologyMatcher 自动匹配本体库
4. KgNormalizer 自动归一化

### 第三步：建模分析
1. kg_query + kg_search — 知识图谱查询
2. relation_analysis — 双边关系分析（关键因素）
3. alert_query — 告警查询
4. report_generate — 生成分析报告

### 第四步：发布本体
1. ontology_promote — 发现实体晋升
2. knowledge_aggregate — 增量汇聚（需主任权限；研究员场景由系统每小时自动执行）
3. 返回：报告 + 关键因素 + 图谱摘要

## 注意事项

- 爬取前确认爬取源已启用且限流配置合理
- NLP 处理支持中/英/法/阿四语种自动识别
- 知识图谱节点需通过本体匹配器匹配到本体库实体
- 报告生成时需关联主题 ID 和文章 ID 列表
- 四步流程按序执行，每步完成后汇报进度
