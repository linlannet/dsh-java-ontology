---
marp: true
theme: default
size: 16:9
paginate: true
header: 'DSH-JAVA Ontology 本体知识平台'
footer: '2026-10-14 | v2.0 GA · 架构方向增补版'
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

> 本版增补《架构方向》专章：行业判断 → 现状体检 → 路线选型 → 目标架构 → 演进路径

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

<!-- _class: lead -->
<!-- _paginate: false -->

# 架构方向

### 从"AI + 数据库"走向"本体运行时"

<br>

> 行业判断 → 现状体检 → 路线选型 → 目标架构 → 三步演进

---

# 十八、架构方向（一）：行业判断

## 本体，不该只是"描述层"

> 来源：图特摩斯 OntoFlow 公开文章（2026-10）
>
> "很多所谓'本体应用'，最终仍然是：**Agent → Tool → SQL / API → MySQL / Neo4j → 返回结果**。
> 本体在这里更像一个'描述层'——Agent 负责思考，数据库负责存储，本体负责告诉 Agent 数据库里的东西是什么。"

**四点批评：问题出在哪**

| # | 批评点 | 原文关键句 | 本质 |
|---|--------|-----------|------|
| 1 | 链路长 | "每一次行动都要经过 Agent调用→Tool→数据库操作→返回→Agent继续判断" | 调用栈深，每跳都有开销 |
| 2 | 调用多 | "调用多" | 序列化/反序列化次数多，网络往返多 |
| 3 | 上下文重 | "上下文重" | Agent 需维护大量上下文理解库表结构 |
| 4 | 性能受制 | "性能最终受制于外部数据库和 Agent 工具调用" | 本体不参与运行时决策，只是标签 |

**文章主张**：以本体为第一视角，而非以 Agent 为第一视角——**本体自身拥有数据、函数、派生与行动能力**，流式查询本身就是业务逻辑在运行。

---

# 十九、架构方向（一续）：我们的判断

## 接受什么、保留什么

**✅ 我们认为说得对的（4 条）**

| # | 判断 | 对我们的意义 |
|---|------|-------------|
| 1 | "AI + 数据库"模式链路长、调用多 | 与我们的实测一致（见下页） |
| 2 | 本体不应只是"描述层" | 七模型 YAML 目前确实"空转" |
| 3 | 本体应拥有"行动"能力 | 当前 Action 由 Agent 独占，本体不具写能力 |
| 4 | 四步流程框架（接入→处理→建模→发布） | 我们已对齐，方向正确 |

**⚠️ 我们认为过于理想化的（3 条）**

| # | 主张 | 我们的保留意见 |
|---|------|--------------|
| 1 | "本体不依赖 Agent" | Agent 的推理与自然语言交互能力仍不可替代 |
| 2 | "数据自更新自流转" | 依赖自研图引擎（OntoGraph），非通用可行方案 |
| 3 | "平民级" | 原生本体论视角的学习曲线并不低 |

> **结论：不盲从，也不固守。** 先承认问题，再选一条风险可控的路。

---

# 二十、架构方向（二）：现状体检

## 调用链实测 —— 用数据说话

以 `knowledge_aggregate(action=triggerFull)` 为例：

```
[Agent 进程]              [app :8092]                [MySQL]
 1 Agent 决策调用
 2 Tool.run()
 3 BaseOntologyTool.okPost()
 4 AdminApiClient.post()  ── 序列化②（手拼 JSON）──┐
                          ── 网络跳① HTTP POST ───→
                            5 AggregationController
                            6 AggregationService
                            7 KnowledgeAggregator
                            8 jdbcTemplate.update()  ── 序列化③ JDBC ──┐
                                                     ── 网络跳② JDBC ─→ MySQL
                            9 logAggregation()   ── JDBC ─→ MySQL
                           10 返回 AggregationStats ── 序列化④ Jackson ─┘
11 AdminApiClient.send() ←── HTTP 响应 ──
12 BaseOntologyTool.ok() ── 序列化⑤ ToolExecutionResult ──
13 Agent 收到结果
```

| 维度 | 实测值 | 对应批评点 |
|------|--------|-----------|
| Java 调用栈层数 | **7–8 层** | 链路长 |
| 序列化 / 反序列化 | **5 次 / 3 次** | 调用多 |
| 网络跳数 | **2 跳**（HTTP + JDBC） | 链路长 |
| 进程边界 | **3 个**（Agent / app / MySQL） | 性能受制 |
| NLP 往返 | **5 + 2N + 2M 次**（一篇文章典型 **75 次**） | 调用多 |
| 本体参与运行时决策 | **❌ 否**（YAML 仅描述层） | 上下文重 |

---

# 二十一、架构方向（二续）：本体"空转"与工程短板

## 七模型 YAML：是"设计文档"，不是"运行时引擎"

| 模型 | 设计了什么 | 运行时实际由谁执行 | 状态 |
|------|-----------|------------------|------|
| **M1 对象** | 52 个实体定义 | 仅用于 DDL 生成 | ⚠️ 半消费 |
| **M2 行为** | CRUD 操作 | BaseController 硬编码 | ❌ 空转 |
| **M3 规则** | 告警规则 | AlertService 硬编码 if-else | ❌ 空转 |
| **M5 主体** | 三角色 | @RequiresRole 注解硬编码 | ❌ 空转 |
| **M6 流程** | 七阶段流 | WorklineOrchestrator 硬编码 | ❌ 空转 |
| **M7 报告** | 报告模板 | ReportService 硬编码 | ❌ 空转 |
| **MU 界面** | 界面布局 | 未被消费（前端未实现） | ❌ 未实现 |

**工程短板（AdminApiClient）**

| 问题 | 现状 | 风险 |
|------|------|------|
| 读超时 | 未设置（仅 connectTimeout=5s） | 大查询时无限等待 |
| 重试 | 0 次 | 网络抖动直接失败 |
| JSON 序列化 | 手拼字符串（无 Jackson/Gson） | 转义不完整、嵌套不支持 |
| 连接池 | 无（每次新建 HttpClient） | 高并发时端口耗尽 |

> **一句话：链路长、往返多、本体空转——三个问题都能用架构手段收敛。**

---

# 二十二、架构方向（三）：路线选型

## 五条主流路线：本体到底站在什么位置

| 路线 | 代表 | 核心理念 | 本体定位 |
|------|------|---------|---------|
| **A 图数据库原生** | Neo4j / TypeDB / Dgraph | 关系是一等公民，图遍历原生 | Schema 描述层 |
| **B 语义网 / OWL** | Jena / Stardog / GraphDB | 知识 = 三元组，推理 = 逻辑闭包 | 推理基础 |
| **C Palantir Ontology** | Foundry / Gotham | 对象 + 关系 + **Action**，本体即应用 | 对象+关系+Action |
| **D OntoFlow 原生本体** | OntoGraph / OntoFlow | 本体自持数据、函数、派生、行动 | **自运行实体** |
| **E 混合模式** ⭐ | MySQL + Neo4j + 本体层 | 事务归事务、关系归关系、本体做运行时抽象 | 运行时抽象层 |

**横向对比**

| 维度 | A 图库 | B OWL | C Palantir | D OntoFlow | **E 混合** |
|------|-------|-------|-----------|-----------|----------|
| Agent 依赖 | 仍需 | 仍需 | 可选 | 不依赖 | 可选 |
| 链路长度 | 2 层 | 2 层 | 1–2 层 | **1 层** | 2–3 层 |
| 推理能力 | ❌ | ✅ OWL | ✅ 规则 | ✅ 派生传播 | ✅ 可插拔 |
| Action 支持 | ❌ | ❌ | ✅ Action Type | ✅ 行动空间 | ✅ 可定义 |
| 事务 ACID | ✅ | ⚠️ 弱 | ✅ | ✅ | ✅ MySQL |
| 生态成熟度 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐⭐ |
| 开源 / 成本 | ✅ 社区版 | ✅ Jena | ❌ 百万美元级 | ⚠️ 闭源 | ✅ 全开源 |
| 与现有架构兼容 | 中 | 低 | 低 | 低 | **高** |

---

# 二十三、架构方向（三续）：依据与案例

## 实测验证 + 案例背书

**① 批评点在我们项目中全部得到验证**

| 批评点 | 我们的实测 | 依据 |
|--------|-----------|------|
| 链路长 | 7–8 层调用栈 | 调用链分析 |
| 调用多 | 5+2N+2M 次 JDBC | NlpService 逐条写入 |
| 上下文重 | YAML 不参与运行时决策 | 七模型"空转"分析 |
| 性能受制 | 75 次 JDBC 往返 / 篇文章 | N=20, M=15 典型场景 |

**② 案例背书**

| 案例 | 技术 | 给我们什么启示 |
|------|------|--------------|
| **Cisco** 网络拓扑分析 | Neo4j | 查询 20 分钟 → **2 秒**，图库对关系查询的优势可量化 |
| **LinkedIn** Economic Graph | Neo4j | 5 亿+ 节点图谱遍历，可扩展性已被验证 |
| **GitHub / Airbnb / 沃尔玛 / Zillow** | MySQL + Neo4j | **混合模式在头部公司生产环境长期验证** —— 与路线 E 一致 |
| **AstraZeneca** 药物图谱 | Stardog | 证明 OWL 推理价值，但推理闭包代价高（NP-hard） |
| **Palantir** 战场态势 | Ontology | Action Type 作为一等公民 —— 与我们第三步对齐 |
| **OntoFlow** 物流履约 | OntoGraph | 原生本体自运行 —— 我们的**远期目标**，非近期路径 |

**技术选型依据**

| 选型 | 依据 |
|------|------|
| Neo4j（非 TigerGraph / ArangoDB） | 社区版免费、Spring Data Neo4j 成熟、市占 70%+、Java 生态最佳 |
| 保留 MySQL（非全量迁图库） | 事务数据（文章/告警/报告）需 ACID，图库事务能力弱 |
| YAML 驱动（非 OWL / RDF） | 已有七模型基础，学习曲线低，团队已熟悉 |
| 渐进式（非推翻重做） | 167 测试 + 52 表 + 15 工具的资产保留，风险可控 |

---

# 二十四、架构方向（四）：目标架构

## 混合模式三层 —— 各归其位

```
                    ┌─────────────────────┐
                    │   本体运行时层       │
                    │   (Ontology Engine)  │
                    │   YAML 驱动 + 推理   │
                    └──────┬──────┬───────┘
                           │      │
              ┌────────────┴──┐ ┌─┴──────────────┐
              │  图存储层      │ │  关系存储层     │
              │  (Neo4j)      │ │  (MySQL)       │
              │  实体+关系    │ │  事务数据      │
              │  图谱遍历     │ │  CRUD          │
              └───────────────┘ └────────────────┘
```

| 层 | 技术 | 职责 |
|----|------|------|
| **本体运行时层** | 自研（YAML 驱动） | 实体 / 关系 / Action 定义，查询路由，推理 |
| **图存储层** | Neo4j / Apache AGE | 实体 + 关系存储，图谱遍历，多跳查询 O(1) |
| **关系存储层** | MySQL 8.0 | 事务数据，CRUD，全文检索（配合 ES） |

**与现有资产的映射**

| 现有资产 | 去向 |
|---------|------|
| `gwy_track_kg_node/edge`、`gwy_global_kg_node/edge`（4 表） | → **迁移** 至 Neo4j |
| `gwy_track_article / topic`、告警、报告（事务数据） | → **保留** MySQL |
| 七模型 YAML（M1–M7+MU） | → **升级** 为运行时本体注册表 |
| 15 Tool + 106 API 端点 + 167 测试 | → **保留**，逐步由 Action 包装 |

> **核心原则**：保留 MySQL 事务优势 + 引入图引擎关系优势 + 激活本体运行时层。

---

# 二十五、架构方向（五）：三步演进路径

## 渐进式演进，不做推翻重做

**第一步｜引入图存储层（解决关系查询性能）**

- 做法：Docker 部署 Neo4j 社区版 → 4 张 kg 表迁移 → Spring Data Neo4j 替代 JdbcTemplate 图谱操作
- 收益：多跳查询由 O(n) 多表 JOIN → **O(1) 邻居查找**；"3 跳内关联实体"由秒级 → 毫秒级
- 工作量：**2–3 人周**

**第二步｜本体运行时层（解决本体空转）**

- 做法：YAML 加载 → 运行时本体注册表；M1 驱动动态 CRUD、M2 驱动 API 路由、M3 驱动规则引擎（替代 AlertService if-else）、M6 驱动动态编排
- 收益：新增实体只写 YAML 不写 Java；规则变更只改配置不重新部署；**本体真正参与运行时决策**
- 工作量：**4–6 人周**

**第三步｜Action 层（解决本体无行动）**

- 做法：YAML 定义 Action（如 `promote_entity`、`generate_report`）→ Action 引擎解析并调用 Service；带权限 / 审计 / 版本 / 回滚
- 收益：调用链由 `Agent→Tool→HTTP→Controller→Service`（5 层）收敛为 `Agent→Action→Service`（**3 层**）；Action 可被定时器 / 事件触发，**本体不再依赖 Agent 才能工作**
- 工作量：**3–4 人周**

**不建议的做法**

| 不建议 | 原因 |
|--------|------|
| ❌ 推翻重做用 OntoGraph | 闭源、生态小、代价太大 |
| ❌ 全量迁移到 Neo4j | 事务数据（订单 / 告警）不适合图数据库 |
| ❌ 引入 OWL 推理机 | 学习曲线高、推理性能 NP-hard、过度设计 |
| ❌ 购买 Palantir | 年费百万美元级、封闭生态 |
| ❌ 保持现状不演进 | 本体空转问题会随数据量增长而放大 |

---

# 二十六、演进路线与未来展望

## 短期改造 → 架构演进 → 远期形态

**短期（应用层改造，天级）**

| 改造 | 内容 | 周期 |
|------|------|------|
| 改造 1 | Agent system_prompt 增强 | 2 天 |
| 改造 3 | AgentDrivenOrchestrator + 时空关联 | 8 天 |
| 改造 2 | Neo4j 图存储层接入（对应架构第一步） | 10.5 天 |

**中长期（架构方向，周级）**

```
当前 v2.0 GA            架构第一步            架构第二步            远期 v4.0
┌──────────────┐      ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│ MySQL + Agent │ ──→ │ Neo4j 图存储  │ ─→ │ 本体运行时层  │ ─→ │ Action 层     │
│ 167 测试通过  │      │ 多跳 O(1)     │    │ YAML 驱动     │    │ 推演沙盘      │
│ YAML 描述层   │      │ 关系查询提速  │    │ 参与运行时决策│    │ 孪生应用      │
└──────────────┘      └──────────────┘    └──────────────┘    └──────────────┘
```

| 阶段 | 内容 | 周期 | 优先级 |
|------|------|------|--------|
| 当前 | v2.0 GA（167 测试通过） | ✅ 已完成 | — |
| 第一步 | 引入 Neo4j 图存储层 | 2–3 周 | P1 |
| 第二步 | 本体运行时层（YAML 驱动） | 4–6 周 | P1 |
| 第三步 | Action 层 + 链路优化 | 3–4 周 | P2 |
| 远期 | 推演沙盘 + 孪生应用 | 待定 | P3 |

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

### 架构方向

| 判断 | 结论 |
|------|------|
| 现状定位 | "AI + 数据库"模式：链路 7–8 层 / 本体 YAML 空转 |
| 路线选型 | **E 混合模式**：MySQL 事务 + Neo4j 关系 + 本体运行时层 |
| 演进方式 | 三步渐进（Neo4j → 本体运行时 → Action），不做推翻重做 |

<br>

> **一句话指令 → 四步自动执行 → 分析报告 + 关键因素 + 知识图谱**
>
> **架构方向：保留成熟度，补上先进性——让本体从"设计文档"变成"运行时引擎"。**

<br>

**DSH-JAVA Ontology Platform v2.0** | 2026-10-14
