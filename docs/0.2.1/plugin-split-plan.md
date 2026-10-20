# DSH Ontology 插件拆分规划方案

> 版本: v1.0 | 日期: 2026-10-15 | 作者: linlan

## 1. 现状分析

### 1.1 当前架构

```
dsh-java-ontology/                    (根 pom, version 0.2.1)
├── dsh-java-ontology-app/            Spring Boot 单体 (端口 8092)
│   ├── 13 Controller / 9 业务域 / 52 表 / 113 Java 文件
│   └── src/main/java/net/linlan/dsh/{sys,ontology,trace,analyze,display,common,global,workspace,plat}
├── dsh-java-ontology-plugin/         单体插件 JAR
│   ├── 1 Plugin 主类 (DshOntologyPlugin)
│   ├── 15 Tool (全部通过 AdminApiClient HTTP 回调 app:8092)
│   └── 依赖: dsh-java-types (provided)
├── dsh-onto-base/                    空占位
├── dsh-onto-flow/                    空占位
└── dsh-onto-wenshu/                  空占位
```

### 1.2 15 个 Tool 清单

| # | Tool 名称 | HTTP 回调 | 对应 App 域 | 角色 |
|---|-----------|-----------|------------|------|
| 1 | ontology_list | GET /api/ontologies | ontology | 全员 |
| 2 | ontology_query | POST /api/ontologies/query | ontology | 全员 |
| 3 | ontology_search | POST /api/ontologies/search | ontology | 全员 |
| 4 | kg_query | KG 节点查询 | trace | 全员 |
| 5 | kg_search | KG 模糊搜索 | trace | 全员 |
| 6 | relation_analysis | 双边关系查询 | ontology | 全员 |
| 7 | alert_query | 告警记录查询 | display | 全员 |
| 8 | topic_create | 创建监测主题 | analyze | 研究员+ |
| 9 | crawl_execute | 触发爬取 | trace | 研究员+ |
| 10 | nlp_extract | NLP 抽取分析 | analyze | 研究员+ |
| 11 | report_generate | 生成报告 | display | 研究员+ |
| 12 | ontology_promote | 实体晋升 | ontology | 研究员+ |
| 13 | workspace_overview | 工作空间概览 | workspace | 全员 |
| 14 | knowledge_aggregate | 知识汇聚 | global | 主任 |
| 15 | director_overview | 主任决策概览 | global | 主任 |

### 1.3 App 业务域依赖关系

```
                    ┌─────────┐
                    │ common  │  (12 files, 基础层, 无外部依赖)
                    └───┬─────┘
          ┌─────────┬───┼───┬────────┬──────────┬──────────┐
          ▼         ▼   ▼   ▼        ▼          ▼          ▼
       sys(14)  ontology(19) trace(20) analyze(24) display(13) workspace(3)  plat(19)
          │                                               
          └───────────────────────────────────────────────▶ global(10)
```

- `common` 是基础层，所有域都依赖它，它不依赖任何域
- `global` 额外依赖 `sys`（2 处导入，JWT/用户上下文）
- 其余域之间**无直接依赖**，仅通过 `common` 共享

### 1.4 52 张表按前缀分组

| 表前缀 | 数量 | 业务域 |
|--------|------|--------|
| sys_ | 1 | sys |
| gwy_ontology_ | 14 | ontology |
| gwy_track_ | 10 | trace |
| gwy_analyze_ | 6 | analyze |
| gwy_display_ | 10 | display |
| gwy_common_ | 4 | common |
| gwy_global_ | 5 | global |
| (workspace) | 2 | workspace |

---

## 2. 拆分目标

1. **插件层**：将 1 个单体插件 JAR 拆为 3 个独立插件 JAR，各自注册到 Harness
2. **复用空占位模块**：激活 `dsh-onto-base`、`dsh-onto-flow`、`dsh-onto-viz`
3. **App 保持单体**：共享 DB/Auth/Flyway，避免微服务拆分的运维成本
4. **按角色隔离**：普通用户工具 vs 研究员工具 vs 主任工具分属不同插件

---

## 3. 拆分方案

### 3.1 三模块架构

```
dsh-java-ontology/
├── dsh-onto-base/                    【共享基础库 JAR】
│   ├── AdminApiClient.java           HTTP 回调客户端
│   ├── BaseOntologyTool.java         工具基类
│   └── 共享 DTO / SchemaBuilder
│
├── dsh-onto-flow/                    【数据流插件】pluginId: dsh-onto-flow
│   ├── DshOntoFlowPlugin.java        Plugin 主类 (注册 9 个 Tool)
│   ├── ontology_list / ontology_query / ontology_search
│   ├── kg_query / kg_search / relation_analysis
│   ├── alert_query / topic_create / crawl_execute
│   └── 依赖: dsh-java-types(provided) + dsh-onto-base
│
├── dsh-onto-wenshu/                  【分析决策插件】pluginId: dsh-onto-wenshu
│   ├── DshOntoWenshuPlugin.java      Plugin 主类 (注册 6 个 Tool)
│   ├── nlp_extract / report_generate / ontology_promote
│   ├── workspace_overview / knowledge_aggregate / director_overview
│   └── 依赖: dsh-java-types(provided) + dsh-onto-base
│
├── dsh-java-ontology-app/            【App 保持不变】
│   └── (单体 Spring Boot, 端口 8092)
│
└── dsh-java-ontology-plugin/         【废弃 → 逐步迁移后删除】
```

### 3.2 模块职责

#### dsh-onto-base（共享基础库）

| 项目 | 内容 |
|------|------|
| packaging | jar |
| 注册 Tool | 0（纯 library） |
| 主要类 | `AdminApiClient`, `BaseOntologyTool`, 共享 Schema 工具 |
| 依赖 | `dsh-java-types` (provided) |
| 被依赖 | dsh-onto-flow, dsh-onto-wenshu |

```xml
<!-- dsh-onto-base/pom.xml -->
<artifactId>dsh-onto-base</artifactId>
<packaging>jar</packaging>
<dependencies>
    <dependency>
        <groupId>net.linlan.dsh</groupId>
        <artifactId>dsh-java-types</artifactId>
        <version>0.2.1</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

#### dsh-onto-flow（数据流插件 — 本体 + 追踪 + 图谱 + 告警）

| 项目 | 内容 |
|------|------|
| packaging | jar |
| pluginId | `dsh-onto-flow` |
| 注册 Tool | 9 个 |
| Tool 列表 | ontology_list, ontology_query, ontology_search, kg_query, kg_search, relation_analysis, alert_query, topic_create, crawl_execute |
| 对应 App 域 | ontology + trace + display(alert) |
| 对应表 | gwy_ontology_* (14) + gwy_track_* (10) + gwy_display_alert_record (1) = 25 表 |
| 角色范围 | 全员 + 研究员 |
| 系统提示词 | 本体查询/图谱检索/追踪爬取能力描述 |

```xml
<!-- dsh-onto-flow/pom.xml -->
<artifactId>dsh-onto-flow</artifactId>
<packaging>jar</packaging>
<dependencies>
    <dependency>
        <groupId>net.linlan.dsh</groupId>
        <artifactId>dsh-java-types</artifactId>
        <version>0.2.1</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>net.linlan.dsh</groupId>
        <artifactId>dsh-onto-base</artifactId>
        <version>0.2.1</version>
    </dependency>
</dependencies>
```

```yaml
# dsh-onto-flow/src/main/resources/META-INF/plugin.yaml
id: dsh-onto-flow
name: DSH OntoFlow Plugin
version: 2.0.0
author: linlan
description: 9 tools for ontology query, KG search, relation analysis, alert, topic, crawl.
entrypoint: net.linlan.dsh.ontology.flow.DshOntoFlowPlugin
```

#### dsh-onto-wenshu（分析决策插件 — NLP + 报告 + 全局 + 主任）

| 项目 | 内容 |
|------|------|
| packaging | jar |
| pluginId | `dsh-onto-viz` |
| 注册 Tool | 6 个 |
| Tool 列表 | nlp_extract, report_generate, ontology_promote, workspace_overview, knowledge_aggregate, director_overview |
| 对应 App 域 | analyze + display(rpt) + global + workspace |
| 对应表 | gwy_analyze_* (6) + gwy_display_rpt_* (2) + gwy_global_* (5) + gwy_common_* (4) = 17 表 |
| 角色范围 | 研究员 + 主任 |
| 系统提示词 | NLP分析/报告生成/知识汇聚/主任决策能力描述 |

```yaml
# dsh-onto-wenshu/src/main/resources/META-INF/plugin.yaml
id: dsh-onto-wenshu
name: DSH OntoWenshu Plugin
version: 2.0.0
author: linlan
description: 6 tools for NLP, report, promotion, workspace, aggregation, director overview.
entrypoint: net.linlan.dsh.ontology.wenshu.DshOntoWenshuPlugin
```

### 3.3 Tool 分配矩阵

| Tool | dsh-onto-flow | dsh-onto-wenshu | 角色 |
|------|:---:|:---:|------|
| ontology_list | ✅ | | 全员 |
| ontology_query | ✅ | | 全员 |
| ontology_search | ✅ | | 全员 |
| kg_query | ✅ | | 全员 |
| kg_search | ✅ | | 全员 |
| relation_analysis | ✅ | | 全员 |
| alert_query | ✅ | | 全员 |
| topic_create | ✅ | | 研究员+ |
| crawl_execute | ✅ | | 研究员+ |
| nlp_extract | | ✅ | 研究员+ |
| report_generate | | ✅ | 研究员+ |
| ontology_promote | | ✅ | 研究员+ |
| workspace_overview | | ✅ | 全员 |
| knowledge_aggregate | | ✅ | 主任 |
| director_overview | | ✅ | 主任 |

### 3.4 根 pom.xml modules 变更

```xml
<modules>
    <!-- 共享基础 -->
    <module>dsh-onto-base</module>
    <!-- 插件 JAR -->
    <module>dsh-onto-flow</module>
    <module>dsh-onto-wenshu</module>
    <!-- App -->
    <module>dsh-java-ontology-app</module>
    <!-- 旧插件（迁移完成后删除） -->
    <!-- <module>dsh-java-ontology-plugin</module> -->
</modules>
```

---

## 4. 实施步骤

### 阶段一：创建 dsh-onto-base（共享基础库）

| 步骤 | 操作 |
|------|------|
| 1.1 | 创建 `dsh-onto-base/pom.xml`（jar, 依赖 dsh-java-types provided） |
| 1.2 | 移动 `AdminApiClient.java` → `dsh-onto-base/src/main/java/net/linlan/dsh/ontology/base/` |
| 1.3 | 移动 `BaseOntologyTool.java` → `dsh-onto-base/src/main/java/net/linlan/dsh/ontology/base/` |
| 1.4 | 更新根 pom.xml，加入 `<module>dsh-onto-base</module>` |
| 1.5 | `mvn install -pl dsh-onto-base` 验证编译 |

### 阶段二：创建 dsh-onto-flow（数据流插件）

| 步骤 | 操作 |
|------|------|
| 2.1 | 创建 `dsh-onto-flow/pom.xml`（jar, 依赖 dsh-java-types provided + dsh-onto-base） |
| 2.2 | 创建 `DshOntoFlowPlugin.java`（extends AbstractHarnessPlugin, pluginId="dsh-onto-flow"） |
| 2.3 | 移动 9 个 Tool 类 → `dsh-onto-flow/src/main/java/net/linlan/dsh/ontology/flow/` |
| 2.4 | 修改各 Tool 的 import：`BaseOntologyTool` → `net.linlan.dsh.ontology.base.BaseOntologyTool` |
| 2.5 | 创建 `META-INF/plugin.yaml` 和 `META-INF/services/net.linlan.dsh.domain.spi.JavaHarnessPlugin` |
| 2.6 | 在 `DshOntoFlowPlugin.configure()` 中注册系统提示词（本体/图谱/追踪部分） |
| 2.7 | 更新根 pom.xml，加入 `<module>dsh-onto-flow</module>` |
| 2.8 | `mvn install -pl dsh-onto-flow` 验证编译 |

### 阶段三：创建 dsh-onto-wenshu（分析决策插件）

| 步骤 | 操作 |
|------|------|
| 3.1 | 创建 `dsh-onto-wenshu/pom.xml`（jar, 依赖 dsh-java-types provided + dsh-onto-base） |
| 3.2 | 创建 `DshOntoWenshuPlugin.java`（extends AbstractHarnessPlugin, pluginId="dsh-onto-wenshu"） |
| 3.3 | 移动 6 个 Tool 类 → `dsh-onto-wenshu/src/main/java/net/linlan/dsh/ontology/wenshu/` |
| 3.4 | 修改各 Tool 的 import：`BaseOntologyTool` → `net.linlan.dsh.ontology.base.BaseOntologyTool` |
| 3.5 | 创建 `META-INF/plugin.yaml` 和 `META-INF/services/net.linlan.dsh.domain.spi.JavaHarnessPlugin` |
| 3.6 | 在 `DshOntoWenshuPlugin.configure()` 中注册系统提示词（NLP/报告/主任部分） |
| 3.7 | 更新根 pom.xml，加入 `<module>dsh-onto-wenshu</module>` |
| 3.8 | `mvn install -pl dsh-onto-wenshu` 验证编译 |

### 阶段四：迁移测试 & 废弃旧插件

| 步骤 | 操作 |
|------|------|
| 4.1 | 将 `DshOntologyPluginTest.java` 拆分为 `DshOntoFlowPluginTest` + `DshOntoWenshuPluginTest` |
| 4.2 | 将 `SkillAgentConfigTest.java` 按新插件结构适配 |
| 4.3 | 全量 `mvn test` 验证 |
| 4.4 | 从根 pom.xml 注释掉 `<module>dsh-java-ontology-plugin</module>` |
| 4.5 | 在 dsh-java 宿主中注册新插件路径，验证 Harness 加载两个插件 |
| 4.6 | 端到端验证：Agent 调用 `plugin__dsh-onto-flow__ontology_query` 和 `plugin__dsh-onto-wenshu__director_overview` |

---

## 5. Harness 注册变更

### 5.1 旧配置（单插件）

```yaml
plugins:
  - path: dsh-java-ontology-plugin/target/dsh-java-ontology-plugin-0.2.1.jar
```

### 5.2 新配置（双插件）

```yaml
plugins:
  - path: dsh-onto-flow/target/dsh-onto-flow-0.2.1.jar
  - path: dsh-onto-wenshu/target/dsh-onto-wenshu-0.2.1.jar
```

### 5.3 Tool 命名变更

| 旧名称 | 新名称 |
|--------|--------|
| plugin__dsh-java-ontology-plugin__ontology_query | plugin__dsh-onto-flow__ontology_query |
| plugin__dsh-java-ontology-plugin__crawl_execute | plugin__dsh-onto-flow__crawl_execute |
| plugin__dsh-java-ontology-plugin__nlp_extract | plugin__dsh-onto-wenshu__nlp_extract |
| plugin__dsh-java-ontology-plugin__director_overview | plugin__dsh-onto-wenshu__director_overview |
| ... | ... |

> ⚠️ Agent 系统提示词中引用的工具名需同步更新。

---

## 6. 系统提示词拆分

### 6.1 dsh-onto-flow 提示词

```
## DSH OntoFlow Plugin v2.0
- 本体库工具用于查询知识图谱、概念层级和实体关系。
- ontology_list 列出本体库；ontology_query 查询概念；ontology_search 模糊搜索。
- kg_query/kg_search 查询知识图谱节点和边。
- relation_analysis 查询双边关系；alert_query 查询告警记录。
- topic_create 创建监测主题；crawl_execute 触发爬取。
- 涉及概念定义、层级关系、实体属性等问题前，必须先用 ontology_query 查证真实结果，禁止凭空猜测。
```

### 6.2 dsh-onto-wenshu 提示词

```
## DSH OntoWenshu Plugin v2.0
- nlp_extract 执行 NLP 分析；report_generate 生成报告。
- ontology_promote 晋升实体；workspace_overview 查询工作空间。
- knowledge_aggregate 触发知识汇聚；director_overview 查询主任决策概览。
- 主任工具（knowledge_aggregate/director_overview）需要主任角色权限。
```

---

## 7. 后续演进方向（可选）

| 方向 | 说明 | 优先级 |
|------|------|--------|
| App 模块化 | 将 app 内 9 个业务包拆为 Maven 多模块，编译期强制依赖隔离 | 低 |
| App 微服务化 | 按域拆为独立 Spring Boot 服务，需引入服务发现 + 分布式事务 | 低 |
| 插件进一步细分 | 若 flow 插件 Tool 持续增长，可再拆为 dsh-onto-ontology + dsh-onto-trace | 中 |
| 共享 DB 拆分 | 按域拆库，每插件对应独立 schema | 低 |
| 插件热加载 | 利用 dsh-java 隔离 ClassLoader，实现插件不停机更新 | 中 |

---

## 8. 风险与缓解

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| Tool 命名变更导致 Agent 提示词失效 | Agent 调用失败 | 保留旧插件 JAR 过渡期并行运行，逐步迁移提示词 |
| dsh-onto-base 被 ClassLoader 隔离后类不共享 | NoClassDefFoundError | base JAR 以 provided scope 打入各插件 fat-jar，不依赖宿主共享 |
| 两个插件同时回调 app 导致并发压力 | app 性能下降 | app 已有连接池，15 个 Tool 量级无风险 |
| 测试覆盖断裂 | 回归遗漏 | 阶段四全量 mvn test + 端到端验证 |
