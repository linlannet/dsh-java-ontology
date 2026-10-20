# 0.2.2 设计文档评审报告

> 评审对象：`static-ontology-implementation-design.md`（v1.0）、`dynamic-ontology-plan.md`（v1.0）
> 评审日期：2026-10-21 ｜ 评审人：CodeArts ｜ 结论：**有条件通过**（修正 4 处事实错误后可进入实施）

## 1. 总体结论

两份文档质量高，架构合理、目标清晰、风险识别到位，**批准作为 0.2.2 实施依据**。需先修正 4 处事实/数量错误（见 §3）。模块重命名（report→qa / visulization→viz，2026-10-20）对两份文档**无影响**——文档引用的路径均在 base/flow/app/siis，未涉及重命名模块。

## 2. 事实核对（带证据）

| # | 文档声明 | 实际 | 结论 |
|---|---------|------|------|
| F1 | P2: static/ index.html 42 行 + app.js 68 行 = 128 行 | 42 + styles.css 18 + app.js 68 = 128 行 | ✅ |
| F2 | P3: `onto_ontology_version.entity_count=0` | 种子 `entity_count=0` | ✅ |
| F3 | P3: V1.0.1 种子"仅 24 行" | 实际 48 行 / 7 条 INSERT | ⚠️ 行数不准（语义对：种子稀少） |
| F4 | P4: `OntologyPromotionService.java:47-54` TYPE_TO_TABLE 6 类 | `:47` 确有 `TYPE_TO_TABLE`，6 类（country/organization/person/location/media/term） | ✅ |
| F5 | P4: M1 定义 8 类 | M1 有 19 个 alias；**本体域聚合根 8 个**（Country/Org/Person/Hotspot/Media/Book/Paper/BilateralRelation） | ✅ "8 类"指本体域聚合根 |
| F6 | onto_ontology_type 种子 14 类型 | 14 类型（country/organization/person/hotspot/term/eventType/location/media/book/paper/entity/version/bilateralRelation/i18n） | ✅ |
| F7 | 2.1: 115 个存量测试 | 实际 **144 个 @Test**（26 文件） | ❌ 过时（文档写于 10-19，重命名+metric 后增长） |
| F8 | 4.4: TYPE_TO_TABLE"从 6 类扩到 8 类" | 列了 **9 类**（country/organization/person/hotspot/media/book/paper/location/term） | ❌ 数量矛盾（6+3 新增=9） |
| F9 | 4.4 仅提 PromotionService.TYPE_TO_TABLE | `OntologyMatchService.java:52` 另有 `ENTITY_TYPE_TO_TABLE`（6 类，同内容） | ⚠️ 遗漏：补齐须同步两处 |
| F10 | 2.1 可复用资产（10 个类） | 全部存在：OntologyMetaRepository/OntologyMatchService/OntologyCrudController(18 端点)/GenericCrudController/OntologyController/HarnessAiService/HarnessAiStreamService/WorklineOrchestrator/AgentDrivenOrchestrator/RoleInterceptor | ✅ |
| F11 | dynamic §2 四组件落地度 | WorklineOrchestrator(flow)/AgentDrivenOrchestrator(siis) 均存在 | ✅ |

## 3. 需修正的问题

### 3.1 【数量错误】static §1.2 G3 + §4.4 "8 类" → 应为"9 类"

**证据**：文档 4.4 列了 9 行（country/organization/person/hotspot/media/book/paper/location/term），6 现有 + 3 新增（hotspot/book/paper）= 9。§1.2 G3 目标行同源写"补齐至 8 类"。

**修正**：§1.2 G3 `补齐至 8 类` → `补齐至 9 类`；§4.4 `从 6 类扩到 8 类` → `从 6 类扩到 9 类`；P4"M1 定义 8 类"保留（指本体域聚合根，与晋升目标数不同概念）。

### 3.2 【遗漏】static §4.4 须同步补齐 OntologyMatchService.ENTITY_TYPE_TO_TABLE

**证据**：`OntologyMatchService.java:52` 的 `ENTITY_TYPE_TO_TABLE` 与 `OntologyPromotionService.java:47` 的 `TYPE_TO_TABLE` 内容完全相同（6 类），是**重复代码**。若仅补 PromotionService，匹配引擎仍漏 hotspot/book/paper，晋升工作台"匹配预览"对这三类永远 miss。

**修正**：4.4 补一行"同步 `OntologyMatchService.ENTITY_TYPE_TO_TABLE`（与 PromotionService 同份 6 类，一并扩到 9 类）"；建议顺带抽取为共享常量 `TypeCatalog.promotionTargets()`（与 4.2 TypeCatalog 统一），消除重复。

### 3.3 【过时】static §2.1/§7 "115 个存量测试" → 144

**证据**：`find ... @Test | wc -l` = 144（26 文件）。文档写于重命名前 10-19，现 10-21 已 +metric 模块 +重命名后测试。

**修正**：`115` → `144`（§2.1 表"测试基线"行、§7"回归门槛"行、§6 S5 验收"115 存量测试"三处）。

## 4. 决策点裁决建议

### static 文档 RP-1 ~ RP-6

| 编号 | 建议 | 理由 |
|------|------|------|
| RP-1 | ✅ 已解决（独立仓库 dsh-ontolab-siis） | 文档已标注 |
| RP-2 | **采纳推荐**：siis-app CORS 白名单 | 开发期最轻量；Nginx 反代留部署期 |
| RP-3 | **采纳推荐**：独立 SQL 双脚本（docs/dev-ops/demo/） | i18n 跨库依赖自增 ID，Flyway 顺序不可控；可重灌 |
| RP-4 | **采纳推荐**：修复时同步核对 + IntegrityChecker 兜底 | m7 objectRef/mu screenRef 悬空风险必须拦 |
| RP-5 | **采纳推荐**：默认开启 + 配置开关 + 未知字段仅告警 | 兼顾安全与存量兼容 |
| RP-6 | **采纳推荐**：以 DDL 为准 | DDL 是运行时真相，m1 可能滞后 |

### dynamic 文档评审清单（§8）

1. **红海航运场景作为演示主线合理**；东南亚对照线非必需——演示数据已含东南亚 8 国，场景叙事聚焦一条更清晰。
2. **D1-D4 排序与并行度认可**：D1（规则）/D2（事件）并行合理，D3（流程）依赖 D0 引擎，D4（Action）依赖 D1-D3。
3. **KPI 六项足够**：不建议加"告警延迟"性能指标——D1 热加载 ≤10s 已覆盖时效，性能基线宜在实施后量化。
4. **D2 默认保持同步模式不保守**：事件化改变时序语义风险高，灰度切换是正确的谨慎策略。
5. **D4 保持"不改 dsh-java-types 契约"**：Action 以 Tool 适配层暴露，契约演进另立 ADR，避免耦合。

## 5. 实施准入清单

- [x] 架构归属（引擎进 base）符合分层
- [x] 7 视图范围合理（图谱可视化列二期）
- [x] 演示数据规模与语种满足演示诉求
- [x] RP-1~RP-6 决策点已裁决
- [x] dynamic D1-D4 路线与 KPI 认可
- [x] **修正 §3.1/§3.2/§3.3 共 4 处后，文档定稿可进入 S1 实施**（已于 2026-10-21 修正，状态保持待评审待用户定稿）

## 6. 后续

1. 修正 static 文档 4 处错误（§3.1 含 G3+§4.4 / §3.2 / §3.3）
2. 文档状态 `待评审` → `已评审·已定稿`
3. 按 S1→S5 实施（15-20 人日），每阶段完成后 `mvn test` 全绿门槛
