# 动态本体规划：结合实际的效果与使用方法（评审稿 v1.0）

> 目标版本：0.2.3+ ｜ 状态：待评审 ｜ 日期：2026-10-19
> 前置依赖：`static-ontology-implementation-design.md`（0.2.2 静态本体引擎化）
> 理论依据：《一文讲清静态本体与动态本体》四组件框架（状态机/事件流/规则引擎/工作流引擎）
> 演进依据：`ontology-architecture-comparison.md` 路线 E（第二步余项 + 第三步 Action 层）

## 1. 定位与范围

本文档是**规划**，不含实施。回答三个问题：

1. 动态本体在这个项目里**现在是什么状态**（四组件落地度）
2. **做起来之后效果是什么**（红海航运安全监测的端到端场景叙事）
3. **怎么用**（人工操作路径 + Agent 调用路径）与**怎么分阶段落地**（D1-D4）

范围边界：动态本体 = 叠加在静态本体（M1 对象模型）之上的运行时表达维度，即 M2 行为、M3 规则、M6 流程 + lifecycle 状态机。静态本体引擎化见姊妹篇。

## 2. 现状评估：四组件落地度

| 组件 | 设计定义（YAML） | 运行时实现 | 落地度 | 差距 |
|------|-----------------|-----------|--------|------|
| 状态机 | M1 各聚合根 `lifecycle`（如发现实体 `[DISCOVERED, PROMOTED]`）；M6 审批流节点 | `WorklinePhase` 七阶段枚举；`OntologyPromotionService.promote()` 直接 UPDATE status | ★★★☆ 硬编码实现 | 状态迁移规则散落 SQL，无统一校验（非法迁移不可拦截） |
| 事件流 | M2 `syncTriggers` 行为链（Article_Save→Nlp_Process→Alert_Check→Push_Send） | 同步服务调用（无消息队列/CEP） | ★★☆ 简化实现 | 行为链为隐性知识，YAML 不驱动；无异步解耦 |
| 规则引擎 | M3 表达式 DSL（`RULE-PROMOTE-ELIGIBLE`、`RULE-ALERT-MATCH` 等） | 各 Service 内 if-else | ★☆☆ 硬编码 | 规则变更需改代码重发布；YAML `reusedBy` 复用关系未兑现 |
| 工作流 | M6 nodeGraph（汇聚流程/晋升审批） | `WorklineOrchestrator`（flow）+ `AgentDrivenOrchestrator`（siis，gwy_plat_agent_plan 表） | ★★★☆ 硬编码实现 | 流程结构硬编码，nodeGraph 不解释执行；审批流未实装 |

结论：四组件"**功能在、语义断**"——运行时具备等价能力，但与 YAML 定义脱钩，导致规则/流程变更必须走代码发布。动态本体规划的核心就是把这条语义链接回来。

## 3. 目标效果：红海航运安全监测（端到端场景）

以真实监测场景叙述动态本体做起来之后的完整效果。前提：0.2.2 静态本体引擎已上线，本体库含 32 国家/52 组织/34 人物（四语种）。

```text
① 主题设定    研究员（或 topic-creator Agent）创建监测主题"红海航运安全"
              ↓ gwy_analyze_track_topic（keywordsZh/En/Fr/Ar 四语种关键词组）
② 多语种采集  CrawlScheduler 按 intervalMinutes 定时拉取
              ↓ Al Jazeera(阿文)/Reuters(英文)/Jeune Afrique(法文) 文章入 gwy_track_article（articleHash 去重）
③ NLP 抽取    四语种策略（Zh/En/Fr/Ar）执行 NER/三元组/情感
              ↓ 产出 gwy_analyze_nlp_entity / nlp_triple / nlp_sentiment
④ 本体匹配    OntologyMatcher 四级瀑布对齐静态本体
              ↓ 已收录实体（胡塞运动/曼德海峡/EUNAVFOR Atalanta）→ 直接关联 ontology_entity_id
              ↓ 未收录实体 → 写入 onto_common_discovered_entity（DISCOVERED）
⑤ 晋升审批    研究员在晋升工作台发起 → 主任审批（M6 FLOW-PROMOTE-005）
              ↓ Ontology_Promote 行为：匹配则关联，否则按 8 类路由建新实体，状态 DISCOVERED→PROMOTED
⑥ 全局汇聚    AggregationScheduler 定时执行（M6 FLOW-GLOBAL-AGG-004）
              ↓ 多租户发现实体/双边关系/图谱 → onto_global_*（RULE-AGGREGATE-DEDUP 去重）
⑦ 图谱构建    KgNormalizer 归一化 → gwy_track_kg_node/edge；可选 GraphSyncService 同步 Neo4j
              ↓ "谁-袭击-什么船-在哪条航道"多跳关系可在图上一次遍历
⑧ 告警与报告  告警规则（关键词"袭击"+情感 negative+级别 high）触发 gwy_display_alert_record → 推送
              ↓ 周报自动汇总局势变化、关系网络、风险趋势（report-generation Skill）
```

**动态本体做起来之后的增量效果**（对比现状）：

| 环节 | 现状 | 动态本体化后 |
|------|------|-------------|
| ③→④ 匹配规则 | 代码内固定 | 匹配阈值/别名表进 M3 YAML，热加载调优 |
| ⑧ 告警规则 | `AlertService` if-else，改规则=发版 | M3 YAML 定义（keyword/sentiment/event 三型），**改规则零部署** |
| ⑤ 审批流 | 无实装 | M6 nodeGraph 驱动：审批人/流转/驳回路径可配置 |
| ② 采集→③ 分析→⑧ 推送 | 同步调用偶合 | M2 syncTriggers 事件化，环节可插拔（如插入"人工复核"节点） |
| 全局 | 状态/流程/规则不可见 | 引擎暴露 `/api/model/*` 元信息，Agent 与人共享同一份本体语义 |

## 4. 使用方法

### 4.1 人工路径（管理界面，0.2.2 界面 + 动态增强）

**研究员视角（日常）**：
1. 登录 8100 管理台（researcher 账号）→ 主题工作台创建/暂停监测主题
2. 晋升工作台查看 DISCOVERED 实体池 → 逐条"匹配预览"（命中蓝色徽标/未命中灰色）→ 选择目标类型（8 类）→ 提交晋升
3. 本体搜索跨语种核查（如阿拉伯文原名反查中文条目）→ 多语种视图补录 en/fr/ar 翻译
4. 查看 AI 助手（research-assistant）四步分析结果，采纳/驳回

**主任视角（治理）**：
1. 审批中心：待晋升实体批量审批/驳回（D3 实装 M6 审批流后）
2. 统一图谱（v_unified_kg）：个人层+全局层 UNION 查看，按 nodeType/租户过滤
3. 告警规则管理：M3 YAML 化后界面编辑（关键词组/情感阈值/级别/渠道），保存即热加载生效
4. 全局汇聚：手动触发或查看 AggregationScheduler 汇聚日志与统计（REP-GLOBAL-AGG-SUMMARY）

### 4.2 Agent 路径（dsh-java，8090）

安装两个插件后（平台 3 工具 + SIIS 12 工具），Agent 侧用法：

```text
用户（一句话）："分析一下最近红海航运安全局势，重点看胡塞武装对商船的袭击，出一份周报"

director-assistant / research-assistant 拆解为工具调用序列：
  topic_etc          → 查询/创建"红海航运安全"主题（四语种关键词组）
  crawl_execute      → 触发对 Al Jazeera/Reuters 源的定向采集
  nlp_extract        → 对新文章执行 NER/情感（四语种策略自动路由）
  ontology_search    → 核对"胡塞运动/曼德海峡"是否已在本体（多语种模糊搜索）
  kg_query           → 拉取"胡塞运动-袭击-商船"子图（3 跳内关联实体）
  relation_analysis  → 双边关系分析（涉及国家立场矩阵）
  alert_query        → 拉取该主题下 high 级别告警记录
  report_generate    → 汇总生成周报（模板+图表定义）
```

要点：
- **Agent 与人共享同一静态本体**（匹配/晋升都落同一批表），Agent 发现的新知识走发现池→晋升，人工审核把关
- **动态本体化后 Agent 链路缩短**（D4 Action 层）：Agent→Action→Service（3 层）替代 Agent→Tool→HTTP→Controller→Service（5 层）
- 4 个 Skill（hotspot-analysis/knowledge-extraction/report-generation/topic-creation）与 3 个 Agent 配置已就绪（`agents/`、`skills/` 目录），无需改动

### 4.3 动态本体四组件的操作映射（谁改什么、怎么生效）

| 想改什么 | 改哪里 | 生效方式（分阶段后） |
|---------|--------|---------------------|
| 告警/匹配/去重规则 | m3-rule-model.yaml 或界面规则编辑器 | 热加载（D1 后），无需重启 |
| 行为链（采集后联动什么） | m2-behavior-model.yaml syncTriggers | 重启加载（D2 后半程支持热插拔监听器） |
| 审批流/流程编排 | m6-flow-model.yaml nodeGraph | 重启加载（D3 后） |
| 实体状态机（合法迁移集） | M1 lifecycle + 新增 transitions 定义 | 随引擎加载（D3 后统一校验非法迁移） |
| 新增实体类型 | M1 YAML + onto_ontology_type 注册 | 0.2.2 起零 Java 代码（schema 驱动 CRUD） |

## 5. 分阶段落地路线

| 阶段 | 内容 | 核心交付 | 效果承诺 | 预估 |
|------|------|---------|---------|------|
| **D0（前置）** | 静态本体引擎化 | 见姊妹篇 0.2.2 方案 | M1 参与运行时；表单/校验 schema 驱动 | 15-20 人日 |
| **D1 规则引擎化** | M3 → 运行时规则引擎：`RuleEngine.evaluate(ruleId, ctx)`；先接管 `AlertService` 的 keyword/sentiment/event 三型判断（RULE-ALERT-MATCH），再接管晋升资格/去重校验；提供规则热加载（WatchService 监听 YAML 变更）+ 规则测试沙箱 API | 告警规则、匹配规则**改 YAML 即生效**；规则执行留审计（规则版本+命中记录） | 4-5 人日 |
| **D2 行为链事件化** | M2 syncTriggers → Spring `ApplicationEventPublisher`：Article_Save/Nlp_Process/Alert_Check/Push_Send 定义为领域事件；`BehaviorRegistry` 按 YAML 注册监听器；保留同步兜底模式（配置切换） | 环节解耦可插拔（如插入人工复核节点不改采集代码）；行为调用链可观测（事件轨迹表或日志） | 3-4 人日 |
| **D3 流程引擎化** | M6 nodeGraph → 轻量解释器：`FlowEngine.run(flowId, ctx)` 按节点图调度（system_task 调行为、user_task/approval_task 生成待办）；接管 Workline 七阶段与晋升审批流（FLOW-PROMOTE-005 实装）；lifecycle 迁移合法性校验 | **审批流实装**（研究员提交→主任审批→晋升/驳回）；流程结构可配置 | 4-6 人日 |
| **D4 Action 层** | 路线 E 第三步：M2 行为升级为 Action 定义（权限/审计/版本/回滚参数化）；`ActionEngine` 直接暴露给 Agent（替代部分 HTTP Tool）；定时器/事件可触发 Action | Agent 链路 5→3 层；本体不依赖 Agent 也能执行写操作（对齐 Palantir Action Type） | 3-4 人日 |

依赖关系：D1/D2/D3 均依赖 D0（引擎与 Registry）；D1 与 D2 可并行；D4 依赖 D1-D3。

## 6. 效果度量（验收 KPI）

| 指标 | 现状基线 | 目标 |
|------|---------|------|
| 新增实体类型的代码量 | Controller/Service/测试改动 | **0 行 Java**（YAML+注册表） |
| 告警规则变更生效时间 | 发版（小时~天级） | **热加载 ≤10s**（D1） |
| 审批流实装 | 无 | 晋升全流程线上化，审批记录可溯（D3） |
| Agent 写操作链路层数 | 5 层 | 3 层（D4） |
| 规则/流程定义与运行时一致性 | 无（YAML 空转） | IntegrityChecker + 一致性测试全覆盖，漂移即报警 |

## 7. 风险与依赖

| 风险 | 应对 |
|------|------|
| D1 规则表达式能力边界（M3 DSL 非 JS/SQL） | 第一期只覆盖已定义的 12 条规则语义，解释器白盒实现；复杂规则仍留 Java 并在 YAML 标注 `implementedBy` |
| D2 事件化改变时序语义（原同步改为异步可能影响测试断言） | 配置开关默认保持同步模式，逐域灰度切换 |
| D3 流程引擎自研范围蔓延（对标 Camunda/Temporal 陷阱） | 严格限定 nodeGraph 已有的节点类型（start/end/system_task/user_task/approval_task），不引入 BPMN |
| 与 dsh-java Harness 的 Tool 契约演进耦合（D4） | Action 先以"Tool 适配层"形态暴露，不改 dsh-java-types 契约 |
| 热加载引发的状态不一致 | 热加载仅限规则（D1），行为/流程变更仍走重启；规则带版本号+审计 |

## 8. 评审清单

1. 场景叙事（红海航运安全）是否符合预期演示主线？是否需要补一条"东南亚"对照线？
2. D1-D4 排序与并行度（D1/D2 并行）是否认可？
3. KPI 六项是否需要增删（如是否要加"告警延迟"性能指标）？
4. D2 事件化默认保持同步模式（灰度切换）是否过于保守？
5. D4 是否保持"不改 dsh-java-types 契约"的边界（还是推动契约升级）？
