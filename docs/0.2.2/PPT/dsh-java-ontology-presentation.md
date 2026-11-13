---
marp: true
theme: default
size: 16:9
paginate: true
header: 'DSH-JAVA Ontology 本体知识平台'
footer: '2026-10-14 | v2.0 GA'
style: |
  section {
    background-color: #0f1419;
    color: #e6edf3;
    font-family: "Microsoft YaHei", "PingFang SC", sans-serif;
  }
  h1 { color: #3b82f6; border-bottom: 2px solid #3b82f6; }
  h2 { color: #22c55e; }
  h3 { color: #f97316; }
  table { background-color: #1c2128; }
  th { background-color: #21262d; color: #3b82f6; }
  strong { color: #f97316; }
  blockquote { border-left: 4px solid #3b82f6; background: #161b22; }
---

<!-- _class: lead -->
<!-- _paginate: false -->

# DSH-JAVA Ontology 本体知识平台

### 全球多语种信息监测与决策分析平台 v2.0

**基于 DeepSeek Harness Java**

<br>

> 15 Phase | 134 任务 | 167 测试 | 52 表 + 2 视图 | 15 Tool | 4 Skill | 3 Agent

2026-10-14

---

# 一、DSH-JAVA 主界面与架构

## 宿主架构

```
┌─────────────────────────────────────────┐
│         DeepSeek Harness Java (:8090)    │
│  ┌──────────┐  ┌──────────┐  ┌────────┐ │
│  │ Agent    │  │ Skill    │  │ Tool   │ │
│  │ Engine   │  │ Registry │  │ Registry│ │
│  └──────────┘  └──────────┘  └────────┘ │
│         │                    │           │
│  ┌──────────────────────────────────┐   │
│  │     Plugin Loader (SPI)          │   │
│  └──────────────────────────────────┘   │
└─────────────────────────────────────────┘
```

![bg right:40%](dsh-java-plugins/dsh-java-mysql/docs/assets/images/7、DSH-JAVA内开启新会话.png)

- **Agent 引擎**：DeepSeek LLM 驱动，自然语言对话
- **Skill 编排**：声明式 SKILL.md，自动发现
- **Tool Registry**：插件注册的原子工具
- **Plugin Loader**：ServiceLoader SPI 机制

---

# 二、配置模型

## DeepSeek API + Agent 配置

![bg right:45%](dsh-java-plugins/dsh-java-mysql/docs/assets/images/5、新对话-份许数据库性能状态.png)

```yaml
# application.yml
dsh:
  llm:
    provider: deepseek
    api-key: ${DEEPSEEK_API_KEY}
    model: deepseek-chat
    temperature: 0.7
    max-tokens: 4096
```

- **模型切换**：DeepSeek / 备用模型，YAML 配置
- **Agent 定义**：`agents/*.yaml`（system_prompt + tools + skills）
- **多角色**：research-assistant（研究员）/ director-assistant（主任）
- **参数调优**：temperature / max_tokens / top_p / retry

---

# 三、配置插件

## 插件安装 / 激活 / 管理

![bg right:40%](dsh-java-plugins/dsh-java-mysql/docs/assets/images/6、DSH-JAVA内增加dsh-java-mysql插件.png)

**安装方式**：
1. HTTP API：`POST /api/plugins/install`
2. harness.yml 预置自动加载
3. 手动放置 JAR 到 plugins/ 目录

**生命周期**：
```
load → start → tools() → register → activate
```

**plugin.yaml 清单**：
```yaml
name: dsh-java-ontology
version: 2.0.0
tools: [kg_query, kg_search, ...]
agents: [research-assistant, ...]
skills: [hotspot-analysis, ...]
```

---

# 四、插件激活及查询

## dsh-java-ontology-plugin：15 Tool + 3 Agent + 4 Skill

![bg right:40%](dsh-java-plugins/dsh-java-mysql/docs/assets/images/8、直接通过插件进行查询 .png)

| 类别 | 工具列表 |
|------|---------|
| **本体查询** | kg_query, kg_search, ontology_list, ontology_query, ontology_search |
| **关系分析** | relation_analysis, alert_query |
| **数据采集** | crawl_execute, topic_create, nlp_extract |
| **报告生成** | report_generate, ontology_promote |
| **工作空间** | workspace_overview |
| **全局汇聚** | knowledge_aggregate, director_overview |

> 插件激活后，Agent 自动获得 15 个工具的调用能力

---

# 五、插件结果交互

## 端到端调用链

![bg right:40%](dsh-java-plugins/dsh-java-mysql/docs/assets/images/9、展示基于插件的查询结果.png)

```
用户："查询中美双边关系"
  ↓ Agent 推理
relation_analysis Tool
  ↓ HTTP POST :8092
AggregationController → Service → MySQL
  ↓ 返回 JSON
Agent 格式化 → 自然语言摘要
```

- **结果格式**：JSON + 自然语言摘要
- **审计 Hook**：PRE_TOOL_USE 日志记录
- **JWT 透传**：Bearer Token 鉴权
- **错误处理**：非 2xx 包装为 error JSON

---

# 六、全流程概览

## 四步法：一句话指令 → 自动执行 → 输出报告

![bg right:40%](a4e0be539e504d5059614778693bac5d.png)

```
用户："帮我把最近一年中东地区，中英法、阿拉伯语等
信息收集一下，按照地缘政治、能源、一带一路、以色列
等主题进行分析，找到影响的关键因素，并给出分析报告"

  ↓ Agent 自动分解

① 数据接入 → ② 数据整理 → ③ 建模分析 → ④ 发布本体
```

- **OntoFlow 布局对齐**：左侧 14 导航 + 主画布 4 步流程 + 右侧 AI 面板
- **Agent 驱动**：一句话 → 四步 → 报告

---

# 七、技术亮点

## 七模型 YAML 驱动 + 三级知识架构 + 多语种 NLP

| 维度 | 实现 |
|------|------|
| **七模型 YAML** | M1 对象 / M2 行为 / M3 规则 / M5 主体 / M6 流程 / M7 报告 / MU 界面 |
| **三级架构** | 个人层（tenant 隔离）→ 共享层 → 统一层（2 视图，主任专属） |
| **多语种 NLP** | 中 / 英 / 法 / 阿拉伯语 四语种策略自动识别 |
| **数据规模** | 52 表 + 2 视图 + 106 API 端点 |
| **插件能力** | 15 Tool + 4 Skill + 3 Agent |
| **质量保障** | 167 测试全量通过，v2.0 GA |
| **技术栈** | Java 17 + Spring Boot 3.5.16 + MySQL 8.0 + JdbcTemplate + Flyway |
| **图存储（规划）** | Neo4j 5 社区版，多跳关系查询 O(1) |

---

# 八、数据采集及多源接入

## CrawlEngine：多源采集 + 限流 + 代理轮换 + 定时调度

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│ 结构化库     │     │ 外部 API     │     │ 网页采集     │
│ MySQL/PG    │     │ REST/第三方  │     │ CrawlEngine │
└──────┬──────┘     └──────┬──────┘     └──────┬──────┘
       │                   │                   │
       └───────────┬───────┴───────────────────┘
                   ↓
          ┌────────────────┐
          │ CrawlScheduler │  @Scheduled Cron
          │  限流 + 重试    │  代理轮换
          └────────┬───────┘
                   ↓
          gwy_track_article（文章入库）
```

- **采集源管理**：TrackCrawlSource CRUD（5 端点）
- **任务监控**：TrackCrawlTask 状态机（PENDING→RUNNING→DONE/FAIL）
- **限流配置**：QPS 控制 + 代理池轮换

---

# 九、数据处理及标准化关联

## 四语种 NLP + 本体匹配 + 图谱归一化

```
文章入库
  ↓
① 语种识别（zh/en/fr/ar 自动检测）
  ↓
② 实体抽取（人物/组织/地点/事件）
  ↓
③ 三元组抽取（主-谓-宾）
  ↓
④ 情感分析（positive/negative/neutral + score）
  ↓
⑤ OntologyMatcher → 本体库 i18n 多语种匹配
  ↓
⑥ KgNormalizer → 关系标准化（统一中文锚点）
  ↓
⑦ 时空关联（实体→国家→区域+时间线）[规划]
  ↓
gwy_analyze_nlp_entity / nlp_triple / nlp_sentiment
```

- **四语种策略**：ZhStrategy / EnStrategy / FrStrategy / ArStrategy
- **本体匹配**：i18n 翻译表 + 实体类型分表匹配
- **图谱归一化**：所有语种归一到中文标准名

---

# 十、建模及指标报告

## 知识图谱 + 关系分析 + 关键因素 + 报告生成

```cypher
// 关键因素分析（Neo4j Cypher，规划中）
MATCH (t:Topic {name: '中东局势'})-[:RELATES_TO*1..3]-(e:Entity)
WITH e, count(*) AS influence_score
ORDER BY influence_score DESC LIMIT 10
RETURN e.name, e.type, influence_score
```

| 能力 | 实现 | 工具 |
|------|------|------|
| **知识图谱** | gwy_track_kg_node/edge + Neo4j（规划） | kg_query, kg_search |
| **双边关系** | gwy_ontology_bilateral_relation | relation_analysis |
| **关键因素** | Neo4j 多跳 Cypher（规划） | kg_query(type=keyFactor) |
| **告警引擎** | AnalyzeAlertRule + DisplayAlertRecord | alert_query |
| **报告生成** | DisplayRptReport 7 section 装配 | report_generate |

> 报告含：概览 + 主题分析 + 实体清单 + 关系图谱 + 情感趋势 + 告警汇总 + 结论建议

---

# 十一、本体库发布

## 实体晋升 + 三级汇聚 + 版本管理

```
个人层（tenant 隔离）        共享层（无隔离）         统一层（2 视图）
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│ gwy_track_*     │     │ gwy_common_*    │     │ gwy_global_*    │
│ gwy_ontology_*  │ ──→ │ discovered_*   │ ──→ │ v_unified_*     │
│ gwy_analyze_*   │ 晋升 │ entity/relation│ 汇聚 │ 主任专属视图    │
└─────────────────┘     └─────────────────┘     └─────────────────┘
```

- **实体晋升**：CommonDiscoveredEntity → OntologyEntity（ontology_promote Tool）
- **汇聚策略**：全量 / 增量 / 定时（@Scheduled）/ 事件触发
- **KnowledgeAggregator**：4 表 upsert（ON DUPLICATE KEY UPDATE）
- **版本管理**：Flyway V1.0.0 迁移 + 审计日志
- **能力输出**：15 Tool + 106 API 端点 + 4 Skill

---

# 十二、研究员工作典型场景（一）：指令下达

## 一句话驱动四步流程

![bg right:40%](dsh-java-plugins/dsh-java-mysql/docs/assets/images/1、新对话-列出所有表和行数.png)

**用户在 dsh-java 对话框输入**：

> "帮我把最近一年中东地区，中英法、阿拉伯语等信息
> 收集一下，按照地缘政治、能源、一带一路、以色列
> 等主题进行分析，找到影响的关键因素，并给出分析报告"

**Agent 自动分解**：
- 区域：中东
- 语种：[zh, en, fr, ar]
- 主题：[地缘政治, 能源, 一带一路, 以色列]
- 时间：最近一年
- 目标：关键因素 + 分析报告

---

# 十三、研究员工作典型场景（二）：四步执行

## 自动编排 + 进度汇报 + 结果输出

![bg right:40%](dsh-java-plugins/dsh-java-mysql/docs/assets/images/5、新对话-份许数据库性能状态.png)

| 步骤 | 执行内容 | 工具调用 |
|------|---------|---------|
| **① 数据接入** | 创建 4 主题 + 查已有文章 + 爬取新文章 | topic_create ×4, kg_search, crawl_execute |
| **② 数据整理** | 四语种 NLP + 本体匹配 + 图谱归一化 | nlp_extract, OntologyMatcher, KgNormalizer |
| **③ 建模分析** | 图谱查询 + 关系分析 + 关键因素 + 报告 | kg_query, relation_analysis, report_generate |
| **④ 发布本体** | 实体晋升 + 增量汇聚 + 输出 | ontology_promote, knowledge_aggregate |

> **最终输出**：分析报告 + 关键因素 Top10 + 知识图谱摘要

---

# 十四、主任工作台（一）：全局概览仪表盘

## 跨租户统计 + 决策支持

![bg right:40%](dsh-java-plugins/dsh-java-mysql/docs/assets/images/3、查看SQL监控.png)

- **director_overview Tool**：全局统计概览
  - 实体总数 / 关系总数 / 图谱节点+边数
  - 租户数 / 文章数 / 告警数 / 报告数
  - 最近汇聚状态 + 知识覆盖度

- **统一监测视图**：`v_unified_monitor`
  - 跨租户文章统计 + NLP 处理状态
  - 区域热度排名 + 主题覆盖度

- **角色**：SUPER_ADMIN（role_level=0），可访问所有租户数据

---

# 十五、主任工作台（二）：统一知识图谱

## 全局图谱可视化 + 跨租户汇聚

![bg right:40%](dsh-java-plugins/dsh-java-mysql/docs/assets/images/4、调整界面色系.png)

- **统一知识图谱视图**：`v_unified_kg`（主任专属）
  - 节点类型筛选 / 租户筛选 / 来源层筛选
  - 图谱画布：节点 + 边 + 权重可视化
  - 放大 / 缩小 / 布局调整

- **汇聚操作**：
  - 全量汇聚：重置统一层（低峰期执行）
  - 增量汇聚：追加更新（实时触发）
  - 定时汇聚：每小时自动触发

- **知识覆盖度分析**：跨租户热点趋势 + 实体覆盖度

---

# 十六、主任工作台（三）：决策支持与报告

## 跨租户分析 + 报告审批 + 知识发布

![bg right:40%](a4e0be539e504d5059614778693bac5d.png)

- **决策支持场景**：
  - "本周中东局势有何新变化？" → 全局图谱对比
  - "各研究员知识覆盖度如何？" → 跨租户统计
  - "哪些发现实体需要晋升？" → 晋升候选清单

- **报告审批**：查看研究员提交的报告，审批发布
- **知识发布**：触发全局汇聚，发布统一知识图谱
- **用户管理**：用户/角色/权限配置（JWT + RBAC）

> 主任工作台 = 全局概览 + 统一图谱 + 决策支持 + 知识发布

---

# 十七、本体库知识库及图谱

## 三级架构 + Neo4j 图谱 + 查询能力

```
┌─────────────────────────────────────────────────────┐
│                  查询能力层                          │
│  kg_query | kg_search | relation_analysis           │
│  + path（路径分析）+ keyFactor（关键因素）[规划]     │
├─────────────────────────────────────────────────────┤
│              统一层（2 视图，主任专属）               │
│  v_unified_monitor | v_unified_kg                   │
├─────────────────────────────────────────────────────┤
│              共享层（无 tenant 隔离）                 │
│  gwy_global_discovered_entity | bilateral_relation  │
│  gwy_global_kg_node | kg_edge                       │
├─────────────────────────────────────────────────────┤
│              个人层（tenant 隔离）                    │
│  gwy_track_* | gwy_ontology_* | gwy_analyze_*      │
└─────────────────────────────────────────────────────┘
```

- **Neo4j 图谱**（规划）：Entity 节点 + Relation 边 + Topic 节点，多跳查询 O(1)
- **MySQL → Neo4j 同步**：实时（NLP 完成后）+ 批量（汇聚时）

---

# 十八、演进路线与未来展望

## 三步改造 → 四步工作模式 → OntoFlow 对齐

```
当前 v2.0 GA               改造后 v3.0               远期 v4.0
┌──────────────┐         ┌──────────────┐         ┌──────────────┐
│ MySQL + Agent │  改造1  │ Agent prompt │  改造2  │ Neo4j 图存储 │
│ 167 测试通过  │ ──→    │ 一句话→四步  │ ──→    │ 关键因素分析 │
│ YAML 描述层   │  改造3  │ 动态编排     │  未来  │ 推演沙盘     │
│ MySQL 图谱    │         │ 时空关联     │         │ 孪生应用     │
└──────────────┘         └──────────────┘         └──────────────┘
```

| 阶段 | 内容 | 周期 |
|------|------|------|
| **改造1** | Agent system_prompt 增强 | 2 天 |
| **改造3** | AgentDrivenOrchestrator + 时空关联 | 8 天 |
| **改造2** | Neo4j 图存储层 | 10.5 天 |
| **未来** | 推演沙盘 + 孪生应用 | 待定 |

---

<!-- _class: lead -->
<!-- _paginate: false -->

# 总结

### v2.0 GA 成果

| 维度 | 数值 |
|------|------|
| Phase / 任务 | 15 / 134 全部 ✅ |
| 测试 | 167 通过（app 115 + plugin 52） |
| 数据模型 | 52 表 + 2 视图 + 106 端点 |
| 插件能力 | 15 Tool + 4 Skill + 3 Agent |
| 技术栈 | Java 17 + Spring Boot 3.5.16 + MySQL + Neo4j（规划） |

<br>

> **一句话指令 → 四步自动执行 → 分析报告 + 关键因素 + 知识图谱**

<br>

**DSH-JAVA Ontology Platform v2.0** | 2026-10-14
