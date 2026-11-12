# 全局汇聚分层整改设计（产品层 / 项目层）

> 版本：`0.2.3` 候选 | 基线：`0.2.2` | 类型：分层整改（无表结构变更） | 日期：2026-11-09
>
> 目标：把 `KnowledgeAggregator` 中的项目专属汇聚逻辑从产品层剥离，产品层（`dsh-java-ontology` / `dsh-ontology`）只保留通用能力，项目层（`dsh-ontolab-siis`）通过适配器自行汇聚。

---

## 一、问题定性

现状 `dsh-onto-flow` 的定时聚合（`AggregationScheduler` 每小时一次）在同一个类里混合了两类职责：

| 步骤 | 数据流 | 定性 |
|---|---|---|
| 发现实体汇聚 | `onto_common_discovered_entity` → `onto_global_discovered_entity` | 通用（产品层应保留） |
| 双边关系汇聚 | `onto_ontology_entity`（category=bilateralRelation，EAV）→ `onto_global_bilateral_relation` | **项目专属**（“国家双边关系”是 SIIS 业务概念） |
| 图谱节点汇聚 | `onto_track_kg_node` → `onto_global_kg_node` | **项目专属** |
| 图谱边汇聚 | `onto_track_kg_edge` → `onto_global_kg_edge` | **项目专属** |
| 主任概览文章统计 | `onto_track_article` | **项目专属**（`AggregationService.getDirectorOverview`） |

后果：
1. 产品层进程直接依赖项目表，删/改项目表会连带产品层启动失败；
2. 新产品接入必须改产品层源码；
3. `grep gwy_` 在产品层无法归零，分层边界不可验证。

---

## 二、目标边界

| 层 | 职责 | 允许依赖 |
|---|---|---|
| 产品层 | 汇聚**契约**（接口、注册表、调度、日志、幂等约定）；唯一内置任务：发现实体汇聚；统一层表 `onto_global_*` 的结构与读取 API | 仅 `onto_*` 表 |
| 项目层 | 实现项目专属任务（双边关系、KG 节点/边等），自行调度，写入产品层定义的 `onto_global_*` 表 | 项目表 + 产品契约 jar |

约定：
- `onto_global_*` 表结构归产品层（Flyway 已存在，本次不改）。
- 写入方归项目层：项目层通过自身 `ontoJdbcTemplate` 跨库写 `dsh_java_ontodb`。
- 产品层不认识任何 `gwy_*` 表名。

---

## 三、接口定义

### 3.1 放置位置

新增包 `dsh-onto-base/src/main/java/net/linlan/dsh/ontology/aggregation/`。
放在 `base` 而非 `flow`：项目层 `dsh-ontolab-siis-app` 已依赖 `dsh-onto-base`，实现接口无需引入 `flow` 内部实现。

### 3.2 任务契约

```java
package net.linlan.dsh.ontology.aggregation;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 全局汇聚任务：把个人层/项目层数据合并到统一层（onto_global_*）。
 * 产品层只定义契约与调度，具体任务由所属层实现。
 */
public interface GlobalAggregationTask {

    /** 任务唯一标识，如 discovered-entity、bilateral-relation */
    String code();

    /** 展示名，用于日志与 /api/aggregation/tasks */
    String displayName();

    /** 执行顺序，值小者先执行 */
    default int order() {
        return 100;
    }

    /** 是否启用；默认读取 dsh.ontology.aggregation.tasks.{code}.enabled */
    default boolean enabled() {
        return true;
    }

    /** 执行汇聚，返回受影响行数 */
    AggregationTaskResult run(AggregationContext context);
}
```

```java
package net.linlan.dsh.ontology.aggregation;

import org.springframework.jdbc.core.JdbcTemplate;

/** 汇聚上下文：由调度方注入数据源与触发信息 */
public interface AggregationContext {

    /** 源数据源：个人层/项目层表所在库 */
    JdbcTemplate source();

    /** 统一层数据源：onto_global_* 所在库 */
    JdbcTemplate global();

    /** 租户 ID；null 表示全量汇聚 */
    Long tenantId();

    /** 触发类型：SCHEDULED / MANUAL / EVENT */
    String triggerType();
}
```

```java
package net.linlan.dsh.ontology.aggregation;

/** 单任务执行结果 */
public record AggregationTaskResult(
        String code,
        int affectedRows,
        long durationMs,
        boolean success,
        String message) {

    public static AggregationTaskResult ok(String code, int rows, long startMs) {
        return new AggregationTaskResult(code, rows, System.currentTimeMillis() - startMs, true, null);
    }

    public static AggregationTaskResult failed(String code, long startMs, String msg) {
        return new AggregationTaskResult(code, 0, System.currentTimeMillis() - startMs, false, msg);
    }
}
```

### 3.3 注册表与调度

```java
package net.linlan.dsh.ontology.aggregation;

/** 任务注册表：收集容器内全部实现，按 order 排序，过滤未启用项 */
public interface AggregationTaskRegistry {
    java.util.List<GlobalAggregationTask> enabledTasks();
    java.util.List<String> allTaskCodes();
}
```

实现类 `SpringAggregationTaskRegistry`（`flow` 层）：
- 构造注入 `List<GlobalAggregationTask>`，`@Autowired(required = false)`；
- 按 `order()` 升序排序；
- 结合配置 `dsh.ontology.aggregation.tasks.{code}.enabled`（缺省 `true`）过滤；
- 应用到 `AggregationScheduler`：遍历任务执行，单任务失败不影响其他任务。

### 3.4 内置任务（产品层唯一）

```java
@Component
public class DiscoveredEntityAggregationTask implements GlobalAggregationTask {
    @Override public String code() { return "discovered-entity"; }
    @Override public String displayName() { return "发现实体汇聚"; }
    @Override public int order() { return 10; }

    @Override
    public AggregationTaskResult run(AggregationContext ctx) {
        long start = System.currentTimeMillis();
        int rows = ctx.global().update(
            "INSERT INTO onto_global_discovered_entity " +
            "(entity_name, entity_type, tenant_ids, total_mention_count, first_seen_time, last_seen_time, created_by) " +
            "SELECT d.entity_name, d.entity_type, CONCAT('[', GROUP_CONCAT(DISTINCT d.tenant_id), ']'), " +
            "SUM(d.mention_count), MIN(d.first_seen_time), MAX(d.updated_at), 'aggregator' " +
            "FROM onto_common_discovered_entity d WHERE d.del_flag = 0 " +
            "GROUP BY d.entity_name, d.entity_type " +
            "ON DUPLICATE KEY UPDATE tenant_ids = VALUES(tenant_ids), " +
            "total_mention_count = VALUES(total_mention_count), last_seen_time = VALUES(last_seen_time), updated_at = NOW()");
        return AggregationTaskResult.ok(code(), rows, start);
    }
}
```

> 注：产品库中源表与目标表同库，`source()` 与 `global()` 可指向同一 `JdbcTemplate`。

### 3.5 项目层适配器（SIIS 示例）

```java
// dsh-ontolab-siis-app/src/main/java/net/linlan/dsh/graph/aggregation/BilateralRelationAggregationTask.java
@Component
public class BilateralRelationAggregationTask implements GlobalAggregationTask {
    @Override public String code() { return "bilateral-relation"; }
    @Override public String displayName() { return "双边关系汇聚"; }
    @Override public int order() { return 20; }

    @Override
    public AggregationTaskResult run(AggregationContext ctx) {
        long start = System.currentTimeMillis();
        int rows = ctx.global().update(
            "INSERT INTO onto_global_bilateral_relation " +
            "(subject_entity_name, object_entity_name, relation_type, tenant_ids, avg_score, sample_count, created_by) " +
            "SELECT ea.attr_value, eb.attr_value, e.relation_type, CONCAT('[', GROUP_CONCAT(DISTINCT e.tenant_id), ']'), " +
            "1.0, COUNT(*), 'aggregator' FROM onto_ontology_entity e " +
            "LEFT JOIN onto_ontology_attribute ea ON ea.entity_id = e.id AND ea.attr_code = 'country_a' " +
            "LEFT JOIN onto_ontology_attribute eb ON eb.entity_id = e.id AND eb.attr_code = 'country_b' " +
            "WHERE e.category = 'bilateralRelation' AND e.del_flag = 0 " +
            "GROUP BY ea.attr_value, eb.attr_value, e.relation_type " +
            "ON DUPLICATE KEY UPDATE tenant_ids = VALUES(tenant_ids), avg_score = VALUES(avg_score), " +
            "sample_count = VALUES(sample_count), updated_at = NOW()");
        return AggregationTaskResult.ok(code(), rows, start);
    }
}
```

`KgNodeAggregationTask`（order 30）、`KgEdgeAggregationTask`（order 40）同构，SQL 取自现有实现。

项目层调度：新增 `SiisAggregationScheduler`，`@Scheduled(cron = "${dsh.siis.aggregation.cron:0 0 * * * ?}")`，构造注入 `siisJdbcTemplate`（`source()`）与 `ontoJdbcTemplate`（`global()`）。

### 3.6 主任概览的指标来源

`getDirectorOverview()` 不再直接读 `gwy_track_article`，改为可选注入：

```java
public interface GlobalMetricProvider {
    /** 项目层补充指标；产品层无实现时返回空 Map */
    java.util.Map<String, Object> extraMetrics();
}
```

- 产品层默认无 Bean → 返回 `{"degraded": true}`，其余 `onto_global_*` 指标正常；
- SIIS 实现该接口，返回 `totalTenants`、`totalArticles`；
- 8100 单进程内无法拿到 8101 的 Bean，故 SIIS 模式改为：由 8101 提供 `GET /api/global/metrics`，8100 通过已有 `admin-api-url` 风格的 HTTP 客户端拉取，超时 1s，失败降级。

### 3.7 配置项

```yaml
dsh:
  ontology:
    aggregation:
      enabled: true              # 总开关
      cron: "0 0 * * * ?"        # 默认每小时
      tasks:
        discovered-entity:
          enabled: true
        # 其他任务由对应层声明
```

```yaml
# dsh-ontolab-siis-app
dsh:
  siis:
    aggregation:
      enabled: true
      cron: "0 5 * * * ?"        # 错开 5 分钟，避免与平台同时写
```

---

## 四、迁移步骤

> 每步可独立构建、启动、验证；任一步失败可回滚该步。

### Step 1：落契约（不改行为）

1. `dsh-onto-base` 新增 `aggregation` 包：`GlobalAggregationTask`、`AggregationContext`、`AggregationTaskResult`、`AggregationTaskRegistry`、`GlobalMetricProvider`。
2. `flow` 新增 `SpringAggregationTaskRegistry`、`SimpleAggregationContext`。
3. 编译 + 全量测试，行为不变。

### Step 2：产品层内建任务

1. 新增 `DiscoveredEntityAggregationTask`，SQL 从 `KnowledgeAggregator.aggregateDiscoveredEntities()` 原样迁移。
2. `AggregationScheduler` 改为遍历 `registry.enabledTasks()`，写一条 `onto_global_aggregation_log`（`aggregation_type=FULL`，`remark` 存各任务 JSON 摘要）。
3. 验证：8100 启动后 `Full aggregation completed` 仍出现，`onto_global_discovered_entity` 行数不变。

### Step 3：SIIS 侧接管项目任务

1. 新增 `BilateralRelationAggregationTask`、`KgNodeAggregationTask`、`KgEdgeAggregationTask`（SQL 原样迁移）。
2. 新增 `SiisAggregationScheduler`，写 SIIS 日志表 `onto_global_aggregation_log`（同为 ontodb，可复用表）。
3. 验证：8101 启动后三张 `onto_global_*` 行数与源表聚合结果一致；连续两次执行行数不变（幂等）。

### Step 4：产品层删除项目 SQL

1. `KnowledgeAggregator` 删除 `aggregateBilateralRelations`、`aggregateKgNodes`、`aggregateKgEdges` 及其 `ForTenant` 私有方法。
2. `AggregationService.getDirectorOverview()` 改用 `GlobalMetricProvider`（缺失时 `degraded=true`）。
3. `AggregationController` 保持 `/trigger`、`/incremental/{tenantId}`、`/logs`、`/unified-monitor`、`/unified-kg` 语义不变；新增 `GET /api/aggregation/tasks` 返回任务 code/名称/顺序/启用状态。
4. 验证：`grep -rn "gwy_" dsh-onto-*/src/main/java` 仅剩 8 张本体实体表与报表/告警相关（见 §六）。

### Step 5：门禁与文档

1. 新增单测 `AggregationRegistryTest`、`DiscoveredEntityAggregationTaskTest`、`AggregationDegradationTest`（无项目任务时 `success=true`）。
2. SIIS 新增 3 个适配器单测（H2 或 Mock JdbcTemplate）。
3. 更新 `CHANGELOG.md`、`docs/0.2.2/design.md`（汇聚章节）、`ai-pilot/contract/openapi-baseline.yaml`（新增 `/api/aggregation/tasks`，若纳入契约）。
4. 增加 CI 检查脚本：产品层源码禁止出现 `gwy_`（Flyway 与 `docs/` 除外）。

---

## 五、影响面

### 5.1 代码

| 模块 | 变更 | 说明 |
|---|---|---|
| `dsh-onto-base` | 新增 5 个契约类型 | 无外部依赖变化 |
| `dsh-onto-flow` | 新增注册表、上下文、1 个内置任务；改造 `AggregationScheduler` / `AggregationService` / `AggregationController`；`KnowledgeAggregator` 删除 4 个公开方法 + 4 个私有方法 | 对外 API 不变 |
| `dsh-ontolab-siis-app` | 新增 3 个任务 + 1 个调度器 + 1 个 `GlobalMetricProvider` 实现 + 1 个 `/api/global/metrics` | 仅 SIIS 内部 |

### 5.2 接口

| 接口 | 变化 |
|---|---|
| `POST /api/aggregation/trigger` | 返回体追加 `tasks[]` 明细；原字段保留 |
| `POST /api/aggregation/incremental/{tenantId}` | 仅执行支持租户粒度的任务（`discovered-entity`） |
| `GET /api/aggregation/logs` | 不变 |
| `GET /api/aggregation/unified-monitor`、`/unified-kg` | 不变（仍读本库个人表 + 全局表） |
| `GET /api/aggregation/tasks` | 新增 |
| SIIS `GET /api/global/metrics` | 新增（供 8100 拉取） |

### 5.3 数据

- 无表结构变更、无 Flyway 新增（除非决定把 `/tasks` 响应纳入契约文档）。
- 行为变化：8100 不再产出双边关系/KG 全局数据；8101 未启动时，这三张全局表保持上次结果，`lastAggregation` 仍显示平台任务成功。
- 因此需在监控中区分“平台任务失败”和“项目任务未运行”。

### 5.4 测试

| 侧 | 现有 | 新增 |
|---|---|---|
| 平台 | 110 | +5～6（注册表、内置任务、降级、控制器） |
| SIIS | 82 | +4（3 个适配器 + 调度） |

### 5.5 兼容与回滚

- 兼容：所有既有 URL、响应字段、日志表列不变；新增字段只追加。
- 回滚：代码层面回退即恢复旧聚合；因无 DDL 变更，`onto_global_*` 数据不会被破坏。
- 灰度：可用 `dsh.ontology.aggregation.tasks.discovered-entity.enabled=false` 单独关闭任务。

---

## 六、验收标准

1. 产品层源码 `grep -rn "gwy_" dsh-onto-*/src/main/java` 命中仅限：8 张本体实体表（`TypeCatalog`）、`gwy_display_rpt_report`、`gwy_display_alert_record`（报表模块，另案处理）。
2. 8100 独立启动（不启 8101）：日志 `aggregation tasks registered: [discovered-entity]`，`success=true`。
3. 手动触发 `POST /api/aggregation/trigger` 返回 `tasks[]`，各任务 `affectedRows` 与源表聚合值一致。
4. 8101 启动后 5 分钟内，三张 `onto_global_*` 表更新；连续两次执行行数不变（幂等）。
5. 停 8101：8100 概览 `degraded=true`，其余指标正常，不报 500。
6. 平台与 SIIS 全量测试通过，计数不回退。

---

## 七、后续（不在本次范围）

1. `WorklineOrchestrator` 的 `gwy_ontology_hotspot` 计数改用模型注册表统计（产品层应为通用口径）。
2. `dsh-onto-qa` 直接映射 `gwy_display_rpt_report` / `gwy_display_alert_record`：迁至 `onto_report_*` 或定义报表适配器。
3. 统一层写入方式若数据量增长，再从“跨库直写”改为“项目层 API 提供源数据、产品层写入”的单写入方模型。
4. 完成上述后，`dsh_java_ontodb` 的 14 张 `gwy_*` 表可进一步收敛到 8 张本体实体表。
