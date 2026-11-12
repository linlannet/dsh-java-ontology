# DeepSeek Harness Ontology v0.2.2 编码任务清单

> 版本：`v0.2.2` | 基线：`v0.2.1` | 对应需求：`spec.md` | 对应设计：`design.md`
>
> 状态说明：本清单为**执行跟踪计划**。**T0、T1-T10、T12（除 T12.5）、T9 与 T11 已完成（2026-11-09）**；仅剩 T12.5 双栈 diff 待 Python 侧可用时执行。完成后改为 `[x]`，并在 `release-v0.2.2-development-notes.md` 记录实际结果。已实测：平台 16 类 / 110 方法，SIIS 14 类 / 82 方法。
>
> 执行顺序：**T0 → T1 → T2 → T3/T4 → T5 → T6 → T7 → T8 → T9 → T10 → T11 → T12**。T3 与 T4 可在 T2 完成后并行；T7 依赖 T6；T9 可与 T7 并行；T12 依赖 T6/T8，可与 T10/T11 并行。

---

## **任务总览**

| 组 | 名称 | 任务数 | 前置 | 主要仓库 | 风险 |
|----|------|--------|------|---------|------|
| T0 | 工程前置治理 | 5 | 无 | 平台 | 高（构建阻断） |
| T1 | YAML 规范化与归位 | 8 | T0 | 平台 + SIIS | 高（模型完整性） |
| T2 | 模型引擎核心 | 10 | T1 | 平台 | 中 |
| T3 | Schema 校验挂载 | 7 | T2 | 平台 + SIIS | 中 |
| T4 | TypeCatalog 与一致性 | 6 | T1 | 平台 + SIIS | 高（映射漂移） |
| T5 | 晋升与匹配改造 | 7 | T4 | SIIS | 高（写入失败） |
| T6 | 模型元数据 API | 6 | T2 | 平台 | 低 |
| T7 | 管理界面 7 视图 | 12 | T6 | 平台 | 中 |
| T8 | 跨端口与安全 | 4 | T7 | SIIS | 中 |
| T9 | 演示数据 | 5 | T1 | 数据 | 中 |
| T10 | 版本与发布治理 | 5 | T0/T7 | 平台 | 低 |
| T11 | 集成验收与回归 | 7 | 全部 | 双仓库 | 高 |
| T12 | 双栈契约与 ai-pilot 对齐 | 6 | T6/T8 | 平台 + ai-pilot | 中 |

**关键路径**：T0 → T1 → T2 → T4 → T5 → T6 → T7 → T8 → T11 → T12。T3 可在 T2 后并行，T9 可在 T1 后并行，T12 可在 T6/T8 后并行。

**范围外**：动态本体 D1-D4、MU 14 菜单全量、Neo4j 深化、向量检索、沙盘数字孪生、报告双表阶段 2/3；Python v0.3.1 的 M2-M5、OWL2 RL/SHACL、Qdrant/Redis、19 张新增表也不在本清单（见 `spec.md` §7）。

---

## **T0 工程前置治理（P0，必须先完成）**

**目标**：让主仓库恢复可独立构建，统一版本标识，建立测试基线。

- [x] **T0.1 修正根 `pom.xml` 模块声明**
  - 移除 `dsh-ontolab-siis/dsh-ontolab-siis-app` 与 `dsh-ontolab-siis/dsh-ontolab-siis-plugin` 两个失效 `<module>`。
  - 确认 7 个平台模块仍完整：base / flow / qa / viz / metric / app / plugin。
  - 文件：`pom.xml`。
  - 验收：`mvn -o -DskipTests validate` 成功；`mvn -o help:evaluate -Dexpression=project.version` 输出 `0.2.2`。
  - **执行结果**：`mvn -o -DskipTests validate` EXIT=0；`<modules>` 仅保留平台七包，`project.version` = `0.2.2`。

- [x] **T0.2 统一应用版本号**
  - `dsh-onto-app/src/main/resources/application.yml`：`dsh.app.version: 0.2.1` → `0.2.2`。
  - 核对 qa/viz/metric/plugin 的 `application.yml` 是否存在同类残留。
  - 验收：全仓库 `grep -R "0.2.1" --include="application*.yml"` 无业务版本残留（历史文档除外）。
  - **执行结果**：`dsh.app.version` 已改为 `0.2.2`；`dsh-onto-plugin/META-INF/plugin.yaml` 的 `version: 2.0.0` 同步为 `0.2.2`；qa/viz/metric 无 `application.yml`，仅 app 存在该键。

- [x] **T0.3 建立存量测试基线**
  - 平台执行 `mvn -o test`，记录 `Tests run` 总数；预期 74 个 `@Test`（11 个测试文件）。
  - SIIS 执行 `mvn -o test`，记录总数；预期 70 个（11 个测试文件）。
  - 将实际命令与结果写入开发说明，不修改测试以“凑数”。
  - 验收：两侧 `BUILD SUCCESS`；失败用例单独定位，不以跳过方式通过。
  - **执行结果**：平台 `mvn -o clean test` `BUILD SUCCESS`，13 个测试类 / 74 个方法（base 19、app 55）；SIIS `mvn -o test` `BUILD SUCCESS`，11 个测试类 / 67 个方法。修复了 3 个测试源目录与 `package net.linlan.dsh.common` 不一致导致的 “Unresolved compilation problems”，未改动任何断言。

- [x] **T0.4 建立 YAML 备份与快照脚本**
  - 在 `docs/0.2.2/` 下保留原始 YAML 副本（不进入 classpath），用于回滚与 diff。
  - 编写一次性统计脚本（可为 `python`/`awk`），输出实体数/字段数/枚举数；不新增 Maven 插件。
  - 验收：快照数字可复现；备份文件不被 `dsh-onto-base/src/main/resources` 扫描。
  - **执行结果**：新增 `docs/0.2.2/model-snapshot/`：`current/base`、`current/siis` 为 P1 后副本，`original/base`、`original/siis` 取自两仓 `HEAD` 的 P1 前副本；`snapshot.py --check` 输出基座 25/200/16、项目层 27/292/14、合计 52/492/30，与冻结基线一致。目录位于 `docs/`，不在 classpath。

- [x] **T0.5 清理风险与回滚预案**
  - 确认演示数据不与 Flyway 冲突；确认 `dsh.ontology.*` 配置键未被其他模块占用。
  - 记录 v0.2.1 tag / 当前 commit，作为回滚点。
  - 验收：回滚点明确；无未提交的构建配置改动遗漏。
  - **执行结果**：演示脚本落于 `docs/dev-ops/demo/`，不在 `db/migration` 与 `db/siis-migration`，Flyway 不扫描；`dsh.ontology.model` / `dsh.ontology.schema-validation` / `dsh.siis.cors` 键仅由本次特性使用，无其他模块占用。回滚点：平台 tag `0.2.1` + 提交 `c30015b`，SIIS 提交 `ac0f1ea`；预案见 `model-snapshot/README.md`。

**T0 完成标准**：主仓库可 `validate`；两侧测试基线可复现；版本号统一；回滚点已记录。

---

## **T1 YAML 规范化与模型归位**

**目标**：消除 M1 裸属性块与重复 alias，使基座 + 项目层模型可被引擎完整解析，且字段不丢失。

- [x] **T1.1 标记 M1 裸属性块**
  - 定位 `m1-object-model.yaml` 中所有“无实体头的 `- { name: ... }`”块，记录起止行与所属域。
  - 输出清单（文件内注释或评审记录），不直接删除。
  - 验收：清单覆盖所有裸块；无遗漏的 `name/type` 列表项。
  - **执行结果**：已定位 M1 全部裸属性块，重建实体边界；裸块总数归零，无遗漏 `name/type` 列表项。

- [x] **T1.2 补 8 个本体域聚合根头 + `category`**
  - `OntologyCountry` → `category: country`
  - `OntologyOrganization` → `organization`
  - `OntologyPerson` → `person`
  - `OntologyHotspot` → `hotspot`
  - `OntologyMedia` → `media`
  - `OntologyBook` → `book`
  - `OntologyPaper` → `paper`
  - `OntologyBilateralRelation` → `bilateralRelation`
  - 每项补齐 `id/name/alias/aggregateType/lifecycle/tags/attributes`。
  - 验收：8 个实体可被解析；字段数与裸块迁移前一致。
  - **执行结果**：`m1-object-model.yaml` 补齐 OntologyCountry/Organization/Person/Hotspot/Media/Book/Paper/BilateralRelation 8 个聚合根头与 `category`，字段与迁移前一致。

- [x] **T1.3 为既有 6 个本体元数据类型补 `category`**
  - `OntologyTerm` → `term`；`OntologyLocation` → `location`；`OntologyEventType` → `eventType`；`OntologyEntity` → `entity`；`OntologyVersion` → `version`；`OntologyI18n` → `i18n`。
  - 验收：14 类 `category` 与 `onto_ontology_type.type_code` 一一对应。
  - **执行结果**：OntologyTerm/ Location/ EventType/ Entity/ Version/ I18n 均补 `category`；14 类 category 全覆盖。

- [x] **T1.4 迁移个人层裸块到 `siis-object.yaml`**
  - 将 M1 中 track/analyze/display 裸属性块迁入 siis-object 对应聚合根。
  - 每迁移一个域，立即统计字段数，确保无丢失。
  - 验收：个人层聚合根 `attributes` 非空；M1 不再含个人层裸块。
  - **执行结果**：track/analyze/display 字段迁入 `siis-object.yaml`，27 聚合根 / 292 字段 / 14 枚举，M1 不再含个人层裸块。

- [x] **T1.5 删除 siis-object 中 8 个重复本体域聚合根**
  - 删除 country/organization/person/hotspot/media/book/paper/bilateralRelation 的重复定义。
  - 若前端需要项目层引用，改由 `/api/model/aggregates/{alias}` 查询基座。
  - 验收：基座 + 项目层无重复 `alias`；`OntologyRegistry` 可构建。
  - **执行结果**：删除 siis-object 中 8 个重复本体域聚合根；两层加载无重复 alias。

- [x] **T1.6 修复 `siis-models/manifest.json` 的 `base_manifest` 语义**
  - 保留字段但标注“仅声明”；加载器不按文件系统路径解析。
  - 核对 `models[7]` 文件名与磁盘一致。
  - 验收：jar 内加载成功；无 `FileNotFoundException`。
  - **执行结果**：项目层 `base_manifest` 改为 `classpath:ontology-models/manifest.json`；基座 `projects` 置空；jar 内加载正常。

- [x] **T1.7 检查 M2/M3/M5/M6/M7/MU 实体头**
  - 对每个 YAML 的 `entities` 节点做结构检查；缺失 `alias/attributes` 的实体补齐或报告。
  - 不实现行为/规则/流程执行，仅保证可解析。
  - 验收：7 模型全部通过 `ModelIntegrityChecker`。
  - **执行结果**：7 模型均通过 `ModelIntegrityChecker`；基座 25 根 / 200 字段 / 16 枚举，两层 52 / 492 / 30。

- [x] **T1.8 生成基线快照并冻结**
  - 记录基座聚合根数、项目层聚合根数、字段数、枚举字段数。
  - 将快照写入测试常量（T2/T4 使用）。
  - 验收：快照可复现；后续改动导致下降时测试失败。
  - **执行结果**：`OntologyYamlBaselineTest` 冻结基座 25/200/16，`SiisObjectYamlBaselineTest` 冻结项目层 27/292/14。

**T1 完成标准**：基座 + 项目层加载零错误；无重复 alias；14 类 category 齐全；快照冻结。

---

## **T2 模型引擎核心**

**目标**：在 `dsh-onto-base` 实现可装配、可查询、可诊断的模型引擎。

- [x] **T2.1 新建引擎包与目录**
  - `dsh-onto-base/src/main/java/net/linlan/dsh/ontology/engine/`
  - 子包：`loader`、`model`、`registry`、`validation`、`catalog`、`config`。
  - 验收：包结构与 `design.md` §2.2.1 一致；无循环依赖。
  - **执行结果**：`dsh-onto-base/.../ontology/engine/{loader,model,registry,validation,catalog,config}` 已建立，无循环依赖。

- [x] **T2.2 实现 `ModelManifest`**
  - 字段：`modelType`、`version`、`platform`、`baseModels`、`projects`、`name`、`description`。
  - 校验必填与文件列表非空。
  - 验收：清单解析单测通过。
  - **执行结果**：`ModelManifest` record 实现，校验必填与文件列表；由 `ModelLoader` 解析。

- [x] **T2.3 实现 `AggregateDescriptor` / `AttributeDescriptor`**
  - 使用 `record` 或不可变类；字段与 `design.md` §2.2.4 一致。
  - 增加 `sourceFile` / `sourcePath`。
  - 验收：字段完整；不可变（集合 copy）。
  - **执行结果**：`AggregateDescriptor` / `AttributeDescriptor` 为 record，集合 copy，含 `sourceFile` / `sourcePath`。

- [x] **T2.4 实现 `ModelLoader`**
  - 读取基座清单 + 7 YAML；可选读取项目清单 + 7 YAML。
  - 使用 SnakeYAML `SafeConstructor`；禁止任意类反序列化。
  - 裸属性块检测并报告文件 + 行号。
  - 重复 alias 检测并报告双方文件。
  - 验收：单测覆盖成功、缺失、语法错误、重复 alias、裸块。
  - **执行结果**：`ModelLoader` 支持两层清单、SafeConstructor、裸块定位（文件+行号）、重复 alias 双方文件。

- [x] **T2.5 实现 `OntologyRegistry`**
  - `alias` / `category` 双索引；`byTag` 查询；只读集合。
  - 验收：查表 O(1)；未知键返回 `Optional.empty()`。
  - **执行结果**：`OntologyRegistry` alias/category 双索引 + byTag；未知键返回 `Optional.empty()`。

- [x] **T2.6 实现 `ModelIntegrityChecker`**
  - 检查空属性、重复 alias、重复字段、枚举空值、M7/M2 悬空引用、基线计数。
  - 输出 `IntegrityReport`。
  - 验收：每类错误至少 1 个单测；报告字段可序列化。
  - **执行结果**：`ModelIntegrityChecker` 覆盖空属性/重复 alias/重复字段/空枚举/悬空引用/基线计数，输出 `IntegrityReport`。

- [x] **T2.7 实现 `TypeCatalog` 与 `TypeCatalogEntry`**
  - 14 条常量；`require(typeCode)` 抛 `IllegalArgumentException`。
  - 提供 `promotableTypes()`、`nameColumns()`、`dataSource(typeCode)`。
  - 验收：14 条齐全；9 类晋升集合正确；名称列与 DDL 一致。
  - **执行结果**：`TypeCatalog` 14 条常量，`require` / `promotableTypes` / `nameColumns` / `dataSource` 全部实现。

- [x] **T2.8 实现 `OntologyEngineConfiguration`**
  - `@ConditionalOnProperty(dsh.ontology.model.enabled, matchIfMissing=true)`。
  - 启动即加载；失败抛异常终止启动。
  - 记录加载耗时与计数到日志。
  - 验收：正常启动；坏 YAML 启动失败。
  - **执行结果**：`OntologyEngineConfiguration` 启动加载并记录 `files/aggregates/attributes/enums/cost`；SIIS 实测 `52/492/30`。

- [x] **T2.9 实现 `ModelLoadException` / `ModelIntegrityException`**
  - 携带文件、行号、节点路径；`message` 可读。
  - 验收：异常消息不泄露绝对路径（对外 API 场合需转换）。
  - **执行结果**：`ModelLoadException` / `ModelIntegrityException` 携带 file/nodePath/line，消息不含绝对路径。

- [x] **T2.10 编写 `OntologyModelEngineTest`**
  - 覆盖：基座加载、项目层加载、项目缺失、重复 alias、裸块、未知类型、悬空引用。
  - 验收：新增测试 ≥8 个；全绿。
  - **执行结果**：`OntologyModelEngineTest` 11 个用例覆盖成功/缺失/语法错误/重复 alias/裸块/未知类型/悬空引用/Schema。

**T2 完成标准**：引擎 Bean 可装配；注册表双索引可用；完整性报告可输出；单测覆盖错误路径。

---

## **T3 Schema 校验挂载**

**目标**：在写库前执行 YAML 字段校验，且不破坏 144 个存量测试。

- [x] **T3.1 实现 `SchemaValidator`**
  - 必填、类型、枚举、未知字段四类规则；支持 `partial=true`。
  - `enabled=false` 时直接返回空列表。
  - 验收：`SchemaValidatorTest` 覆盖 10+ 场景。
  - **执行结果**：新增 `SchemaValidatorTest` 14 个用例，覆盖必填/空值/自增主键/类型/枚举/未知字段/partial/开关/未知 category；`SchemaValidator` 支持 snake_case 对齐模型字段。

- [x] **T3.2 实现 `FieldViolation` 与 `SchemaValidationException`**
  - 字段：`field/rule/expected/actual/message`。
  - 验收：异常可被全局处理器读取并序列化。
  - **执行结果**：`SchemaValidationException(category, violations)` 与 `FieldViolation` 已具备；异常提供 `category()` 访问器。

- [x] **T3.3 挂载 `WorkspaceCrudService.create/update`**
  - 使用 `@Autowired(required=false) SchemaValidator`；`null` 时跳过。
  - `create` 使用 `partial=false`；`update` 使用 `partial=true`。
  - 验收：既有单测构造方式不变且通过；新增校验用例通过。
  - **执行结果**：`WorkspaceCrudService` 以 `@Autowired(required=false)` 注入校验器，create=`partial:false`、update=`partial:true`；注册表未命中的分类自动跳过，存量测试不变。

- [x] **T3.4 挂载 `OntologyCrudService.create/update`（SIIS）**
  - 同上；确保 `category` 取路径参数。
  - 验收：SIIS 存量测试不变；校验用例通过。
  - **执行结果**：SIIS `OntologyCrudService.create/update` 同样挂载；新增 2 个校验用例（缺必填、非法枚举）通过。

- [x] **T3.5 全局异常映射**
  - `SchemaValidationException` → HTTP 400 + `Result.badRequest`（沿用现有 `Result` 响应体风格）。
  - `data` 放 `List<FieldViolation>`。
  - 验收：API 返回字段列表；前端可渲染。
  - **执行结果**：平台 `ApiExceptionHandler` 与 SIIS `OntologyExceptionHandler` 均映射 HTTP 400，`data` 为 `List<FieldViolation>`。

- [x] **T3.6 配置项落地**
  - `dsh.ontology.schema-validation.enabled=true`
  - `dsh.ontology.model.unknown-field-policy=warn`
  - 验收：开关生效；关闭时仅告警日志。
  - **执行结果**：两应用 `application.yml` 均落地 `dsh.ontology.model.enabled`、`dsh.ontology.model.unknown-field-policy`、`dsh.ontology.schema-validation.enabled`，支持环境变量覆盖。

- [x] **T3.7 校验日志与隐私**
  - 日志记录 category/字段名/规则，不记录完整 payload 与敏感值。
  - 验收：日志抽查无 JWT/密码/长文本。
  - **执行结果**：异常处理日志仅记录 `category` 与 `field:rule`，不输出 payload/JWT/长文本。

**T3 完成标准**：非法写入被拒绝并返回字段级错误；存量测试全绿；开关可回退。

---

## **T4 TypeCatalog 与一致性**

**目标**：把晋升、匹配、Schema、数据库注册表收敛为同一事实源。

- [x] **T4.1 核对 `onto_ontology_type` 14 条种子**
  - 逐条核对 `type_code/table_name/display_name/domain/status`。
  - 确认 `bilateralRelation` 使用 `gwy_ontology_bilateral_relation`。
  - 验收：14 条与 TypeCatalog 完全对齐。
  - **执行结果**：`TypeCatalogConsistencyTest` 解析 `V1.0.1` 种子，14 条 `type_code/table_name` 与 TypeCatalog 完全一致。

- [x] **T4.2 核对 14 类名称列**
  - 用 DDL 验证：`hotspot_name`、`title`（book/paper）、`name`、`term_name`、`event_type`、`entity_name`、`version_name`。
  - 验收：名称列清单可追溯到 DDL 行号。
  - **执行结果**：测试解析 `V1.0.0` DDL，验证 6 个 ONTO 表名称列（name/term_name/event_type/entity_name/version_name/name）；SIIS 侧由晋升测试覆盖。

- [x] **T4.3 编写 `TypeCatalogConsistencyTest`**
  - 断言 TypeCatalog 与数据库种子一致。
  - 断言 9 类晋升集合 == `promotable=true`。
  - 断言 `nameColumns()` 与 `OntologyMatchService` 一致。
  - 验收：任一映射改动导致测试失败。
  - **执行结果**：`TypeCatalogConsistencyTest` 5 个用例锁定 14 类映射、9 类可晋升、特殊名称列与 alias 映射。

- [x] **T4.4 记录 ADR-001 差异**
  - 明确本期为“常量 + 测试锁定”，全元数据驱动列 0.2.3+。
  - 验收：设计文档与测试注释一致。
  - **执行结果**：设计 §2.4 与 ADR-001 已明确“常量 + 测试锁定”，全元数据驱动列 0.2.3+；测试类注释同步。

- [x] **T4.5 建立映射变更检查清单**
  - 新增类型时必须同时更新：YAML、`onto_ontology_type` 迁移、TypeCatalog、DDL、测试夹具。
  - 验收：清单纳入评审模板（可写入 `tasks.md` 附录）。
  - **执行结果**：检查清单已写入本文档附录 A。

- [x] **T4.6 校验 `bilateralRelation` 边界**
  - 断言其 `promotable=false`、`nameColumn=null`、数据源 SIIS。
  - 验收：晋升服务拒绝该类型并给出可读提示。
  - **执行结果**：测试断言 `bilateralRelation` 为 SIIS、不可晋升、`nameColumn=null`；晋升服务返回“双边关系由汇聚流程维护”。

**T4 完成标准**：四方映射一致；测试可自动发现漂移。

---

## **T5 晋升与匹配改造**

**目标**：9 类可晋升、匹配预览与晋升写入口径一致，修复名称列导致的 SQL 失败。

- [x] **T5.1 改造 `OntologyPromotionService` 使用 TypeCatalog**
  - 删除本地 `TYPE_TO_TABLE` 副本。
  - `require(targetType)` 选择表与数据源。
  - 验收：6 类存量行为不变；9 类可用。
  - **执行结果**：`OntologyPromotionService` 删除 `TYPE_TO_TABLE`，改用 `TypeCatalog.findIgnoreCase/require`。

- [x] **T5.2 实现按名称列插入**
  - `country/organization/person/media/location → name`
  - `hotspot → hotspot_name`
  - `book/paper → title`
  - `term → term_name`
  - 验收：hotspot/book/paper 插入成功，SQL 无未知列错误。
  - **执行结果**：`insertTypedEntity(TypeCatalogEntry, name)` 按 `nameColumn` 插入；hotspot/book/paper 专项测试通过。

- [x] **T5.3 改造 `OntologyMatchService`**
  - `ENTITY_TYPE_TO_TABLE` 6 → 9。
  - `ENTITY_NAME_COLUMNS` 6 → 9（补 hotspot_name/title/title）。
  - 验收：匹配预览对 9 类均可用；大小写归一。
  - **执行结果**：`OntologyMatchService` 删除两张本地映射表，按 TypeCatalog 解析 9 类与名称列，大小写不敏感。

- [x] **T5.4 晋升事务与幂等**
  - 目标表写入 + 发现实体状态更新；跨库采用分事务 + `ontology_entity_id` 幂等。
  - `UPDATE ... WHERE id=? AND status='DISCOVERED'`。
  - 验收：重复调用不产生重复实体；失败可重试。
  - **执行结果**：目标表写入与状态更新使用 `UPDATE ... WHERE id=? AND status='DISCOVERED'`；已晋升记录直接返回，幂等测试通过。

- [x] **T5.5 未知类型处理**
  - 显式 `targetType` 不在 9 类内 → 400；不得静默落通用表。
  - `targetType` 为空且原类型不在 9 类内 → 按 BR-5.4-9 存量兼容落入 `onto_ontology_entity`，并返回 `targetTable`。
  - 验收：非法显式类型返回可读错误；空目标兼容分支有测试覆盖。
  - **执行结果**：显式未知类型抛 400；空目标且原类型非 9 类时落 `onto_ontology_entity`，返回 `targetTable`。

- [x] **T5.6 写 `PromotionRouteConsistencyTest`**
  - 覆盖 9 类、3 个特殊名称列、未知类型、幂等。
  - 验收：新增测试 ≥6 个；全绿。
  - **执行结果**：新增 `OntologyPromotionRouteTest` 8 个用例，覆盖 9 类、特殊名称列、未知类型、不可晋升、双边关系、幂等、通用回落、发现实体只读列表。

- [x] **T5.7 检查 `bilateralRelation` 不被晋升**
  - 明确提示“双边关系由汇聚流程维护”。
  - 验收：调用返回 400 + 说明。
  - **执行结果**：`promote(id, "bilateralRelation")` 抛可读 IllegalArgumentException，并新增测试锁定。

**T5 完成标准**：晋升 9 类可用；匹配与晋升一致；特殊名称列无 SQL 错误；幂等可重试。

---

## **T6 模型元数据 API**

**目标**：新增 4 个只读端点，供管理台与运维使用。

- [x] **T6.1 新建 `ModelController`**
  - 路径 `/api/model`；4 个端点；全部 `@RequiresRole(1)`。
  - 验收：`/api/model/manifest` 返回版本与计数。
  - **执行结果**：新增 `ModelController`（`/api/model`），4 端点均 `@RequiresRole(1)`。

- [x] **T6.2 定义 VO/DTO**
  - `ManifestVO`、`DomainGroupVO`、`AggregateVO`、`IntegrityVO`。
  - 不返回绝对路径、堆栈、YAML 原文。
  - 验收：响应字段与 `spec.md` §6.5 一致。
  - **执行结果**：新增 `ManifestVO`/`DomainGroupVO`/`AggregateVO`/`IntegrityVO`，不返回绝对路径、堆栈或 YAML 原文。

- [x] **T6.3 实现 4 端点**
  - `manifest`、`aggregates`、`aggregates/{alias}`、`integrity`。
  - `integrity` 读启动缓存。
  - 验收：未知 alias → 404；未登录 → 401；角色不足 → 403。
  - **执行结果**：manifest/aggregates/aggregates{alias}/integrity 全部实现；未知 alias 返回 404。

- [x] **T6.4 异常与空值处理**
  - 引擎未就绪不应出现（启动失败）；集合为空返回空列表而非 null。
  - 验收：边界用例通过。
  - **执行结果**：引擎启动即构建快照，读取不重复解析；空集合返回 `[]` 而非 null。

- [x] **T6.5 编写 `ModelControllerTest`**
  - 覆盖 200/401/403/404；断言响应不含 `sourceFile` 绝对路径。
  - 验收：测试 ≥4 个；全绿。
  - **执行结果**：`ModelControllerTest` 6 个用例覆盖 200/401/403/404 与响应脱敏，全绿。

- [x] **T6.6 双应用暴露确认**
  - 8100 与 8101 均可访问 `/api/model/*`；内容按各自 classpath 模型。
  - 验收：两侧 curl 返回正常。
  - **执行结果**：控制器位于 `dsh-onto-flow`，8100 与 8101 共享；SIIS 启动加载两层模型后端点计数为 52/492/30。

**T6 完成标准**：4 端点可用；权限正确；响应安全。

---

## **T7 管理界面 7 视图**

**目标**：把 128 行静态壳扩展为可日常使用的管理台。

- [x] **T7.1 重写 `index.html` 骨架**
  - 侧边栏 6 项（总览/实体/搜索/多语种/晋升/AI）+ 登录页；视图容器。
  - 验收：页面可打开；无外部资源依赖。
  - **执行结果**：`index.html` 重写为侧边栏 6 项 + 登录页 + 视图容器，无外部资源。

- [x] **T7.2 重写 `styles.css` 深色主题**
  - 对齐 `mu-ui-model.yaml` `theme.colors`；响应式两列/一列。
  - 验收：视觉一致；窄屏可用。
  - **执行结果**：`styles.css` 深色主题与响应式布局重写。

- [x] **T7.3 实现 `app.js` 路由与 `apiFetch`**
  - hash 路由 7 个；401/403/400/5xx 处理；Bearer 注入。
  - 验收：token 失效自动跳登录。
  - **执行结果**：`app.js` 实现 7 个 hash 路由与统一 `request`，注入 Bearer，处理 401/403/400（字段级）。

- [x] **T7.4 登录视图**
  - `POST /api/auth/login`；保存 token；错误提示。
  - 验收：正确/错误账号均给出明确反馈。
  - **执行结果**：登录视图调用 `/api/auth/login`；失败时展示 `msg` 且不写入 token。

- [x] **T7.5 总览视图**
  - 模型树、计数、完整性摘要。
  - 验收：计数与 DB 一致；点击进入实体页。
  - **执行结果**：总览聚合模型树、计数与完整性摘要，点击聚合根进入实体页。

- [x] **T7.6 实体维护视图**
  - 动态表单（Enum/Text/required）；列表/分页/新增/编辑/删除。
  - 验收：country 可完成 CRUD；校验错误可见。
  - **执行结果**：实体页动态表单（必填/枚举/只读 id）、分页列表、新增/编辑/删除，snake_case 提交。

- [x] **T7.7 搜索视图**
  - 主表 + i18n 跨语种搜索；展示命中语种/列。
  - 验收：英文关键词可命中中文实体翻译。
  - **执行结果**：搜索页并行查询实体与翻译表，展示命中记录。

- [x] **T7.8 多语种视图**
  - en/fr/ar 维护；upsert/delete。
  - 验收：保存后立即可见；唯一键冲突有提示。
  - **执行结果**：多语种页支持加载/upsert/删除 en/fr/ar/zh。

- [x] **T7.9 晋升工作台**
  - `DISCOVERED` 列表、匹配预览、9 类目标选择、晋升结果。
  - 验收：12 条演示发现实体可完成“预览→晋升”。
  - **执行结果**：晋升工作台读取 `DISCOVERED` 列表，支持匹配预览、9 类目标选择与晋升。

- [x] **T7.10 AI 对话视图**
  - 同步 + SSE 双模式；不改 Harness 协议。
  - 验收：普通问答与流式输出均可显示。
  - **执行结果**：AI 视图支持同步与 SSE 流式两种模式，协议未改动。

- [x] **T7.11 新增发现实体只读端点**
  - `GET /api/ontology/discovered?status=DISCOVERED&limit=20`（SIIS，`@RequiresRole(1)`）。
  - 验收：晋升工作台有数据源；不暴露多余字段；作为 0.2.2 第 5 个新增只读端点写入契约基线。
  - **执行结果**：SIIS 新增 `GET /api/ontology/discovered`，仅返回 7 个必要字段；新增服务测试锁定字段裁剪。

- [x] **T7.12 前端冒烟与无障碍基础**
  - **执行结果（2026-11-09 浏览器实测）**：7 路由（`#/login`、`#/overview`、`#/entities/country`、`#/search`、`#/i18n`、`#/promote`、`#/ai`）逐页通过；总览显示模型树与计数，实体页 30 条国家，搜索结果 24 条，晋升页可预览；控制台无未捕获错误；未登录访问自动跳 `#/login`。键盘可操作主要表单（人工抽查）。
  - 7 路由逐页打开；控制台无报错；键盘可操作主要表单。
  - 验收：记录冒烟结果到开发说明。

**T7 完成标准**：7 视图全链路可用；401/403/400 行为正确；无框架依赖。

---

## **T8 跨端口与安全**

**目标**：让 8100 安全调用 8101，不把通配 CORS 扩散到新端口。

- [x] **T8.1 SIIS 新增 `CorsConfig`**
  - 白名单来自 `dsh.siis.cors.allowed-origins`；默认 127.0.0.1/localhost:8100。
  - 允许方法/头/预检缓存见 `design.md` §2.9.1。
  - 验收：8100 调 8101 成功；非白名单被拒。
  - **执行结果**：新增 `SiisCorsConfig`（`WebMvcConfigurer` 白名单）；在 SIIS 内移除平台通配 `corsFilter`，平台配置不变。预检：8100=200，非白名单=403。

- [x] **T8.2 配置项与部署说明**
  - production 示例、禁止 `*`、禁止 `*` + credentials。
  - 验收：配置注释清晰。
  - **执行结果**：`dsh.siis.cors.allowed-origins` 默认仅 127.0.0.1/localhost:8100，注释禁止 `*` 与 credentials 组合。

- [x] **T8.3 JWT 一致性检查**
  - SIIS 与平台 `JWT_SECRET` 约定；错误密钥返回 401。
  - 验收：部署清单记录检查项。
  - **执行结果**：SIIS 默认 JWT secret 已与平台默认值对齐，`JWT_SECRET` 单点覆盖；部署清单记录“两侧必须同密钥”。

- [x] **T8.4 安全自查**
  - 新增端点全部 `@RequiresRole(1)`；无 `@PublicApi` 误标。
  - SQL 表名/列名仅来自白名单；日志无敏感信息。
  - 验收：自查清单完成并记录。
  - **执行结果**：新增端点均 `@RequiresRole(1)`；SQL 表名/列名仅来自 TypeCatalog/静态白名单；校验日志不含敏感值。已自查。

**T8 完成标准**：跨端口可用；CORS 最小化；权限与 SQL 安全达标。

---

## **T9 演示数据**

**目标**：提供可重灌、客观中立的演示数据，使管理台“看起来是活的”。

- [x] **T9.1 编写 `demo_ontology_siis.sql`**
  - 国家 32、组织 ≥50、人物 ≥30、媒体 ≥20、热点 ≥6、双边关系 ≥20、书籍/论文样本。
  - 先清理后插入；`SET NAMES utf8mb4`；头部注明目标库/顺序。
  - 验收：脚本可重复执行。
  - **执行结果**：`dsh-ontolab-siis/docs/dev-ops/demo/demo_ontology_siis.sql`（340 行）：国家 32、组织 60、人物 30、媒体 22、热点 8、双边关系 28、书籍 10、论文 8，共 198 行；`created_by='demo-0.2.2'` 先删后插，可重复执行。

- [x] **T9.2 编写 `demo_ontology_onto.sql`**
  - 术语 20、地名 15、i18n ≥600、发现实体 12、版本计数回填。
  - 依赖 SIIS 自增 ID 的记录，用名称/类型定位，避免硬编码 ID 漂移。
  - 验收：脚本可重复执行；i18n 关联正确。
  - **执行结果**：`docs/dev-ops/demo/demo_ontology_onto.sql`（1060 行）：术语 20、地名 15、发现实体 12（覆盖 9 类）、i18n 632（8 类 × 7 语种，`INSERT ... SELECT` 按名称/ISO 定位，无硬编码自增 ID）。

- [x] **T9.3 中立性审查**
  - `biasRating` 留空；人物仅客观履历；`portraitUrl` 留空；风险描述中性。
  - 验收：逐条抽查；无真实隐私/敏感判断。
  - **执行结果**：人物 30/30 `portrait_url` 为 NULL，媒体 22/22 `bias_rating` 为 NULL；热点 `risk_level` 仅 low/medium，描述为全球议题不归责单一国家。

- [x] **T9.4 计数一致性**
  - `onto_ontology_version.entity_count` 与实体表一致。
  - 验收：总览计数与 SQL 查询一致。
  - **执行结果**：SIIS 脚本用各实体表 `COUNT(*)` 求和动态写入 `entity_count`（当前 170），避免硬编码；总览按发布版本读取。

- [x] **T9.5 导入说明与污染防护**
  - 明确仅 demo 环境；不进入 Flyway；建议先备份。
  - 验收：文件头部警示；发布说明引用。
  - **执行结果**：两文件头部均含目标库、执行顺序、仅 demo、先备份、非 Flyway 警示；发布说明 §2.8 已引用实际行数。静态校验 INSERT 列名与两仓 DDL 完全一致。

**T9 完成标准**：双脚本可重灌；数据规模达标；中立性与计数一致。

---

## **T10 版本与发布治理**

**目标**：版本、文档、变更记录一致，发布说明不夸大。

- [x] **T10.1 更新 `CHANGELOG.md` 0.2.2 分节**
  - 写入：引擎、校验、Model API、管理台、晋升 9 类、演示数据、工程治理。
  - 验收：非“仅版本对齐”。
  - **执行结果**：0.2.2（修订）已扩为 15 条：引擎/YAML 归位/Schema 校验/模型 API/晋升 9 类/管理台/CORS/演示数据/模型快照/契约基线，测试规模与实测一致。

- [x] **T10.2 版本号全量核对**
  - 根 pom、7 模块 pom、application.yml、部署脚本/镜像 tag。
  - 验收：统一 `0.2.2`。
  - **执行结果**：两仓 `pom.xml` 无 `0.2.1` 残留；`dsh.app.version` 与插件 `plugin.yaml` 均为 `0.2.2`；无部署脚本/镜像 tag 需核对；剩余 `@since 0.2.1` 为历史标注。

- [x] **T10.3 文档四件套定稿**
  - `spec.md` / `design.md` / `tasks.md` / `release-v0.2.2-development-notes.md` 互链。
  - 保留来源文档与 ADR，不删除。
  - 验收：链接可跳转；口径一致。
  - **执行结果**：四件套互相引用，来源文档与 ADR 保留；测试规模与 P1 结果口径统一（平台 110、SIIS 82、合计 192）。

- [x] **T10.4 发布检查清单**
  - 构建、测试、演示数据、CORS、版本、回滚点逐项勾选。
  - 验收：清单完成并归档。
  - **执行结果**：发布说明 §七 清单已按实际结果勾选，未完成项（浏览器冒烟、演示数据导入验证、Python diff、回滚演练）保留未勾选。

- [x] **T10.5 已知限制披露**
  - 未实现：动态本体 D1-D4、MU 全量、Neo4j 深化、向量检索。
  - 验收：与 `spec.md` §7 一致。
  - **执行结果**：发布说明 §5 第 8/10/11 条与 `spec.md` §7 一致。

**T10 完成标准**：版本统一；变更记录完整；发布说明诚实。

---

## **T11 集成验收与回归**

**目标**：端到端验证四件套声明的所有能力，并锁定回归。

- [x] **T11.1 主仓库构建与全量测试**
  - `mvn -o -DskipTests validate`；`mvn -o test`。
  - 验收：BUILD SUCCESS；存量 74 + 新增全绿。
  - **执行结果**：`mvn -o -f pom.xml clean test` → `BUILD SUCCESS`，16 个测试类 / 110 个方法（base 44、app 66），Failures=0、Errors=0。

- [x] **T11.2 SIIS 构建与全量测试**
  - 独立仓库 `mvn -o test`。
  - 验收：存量 70 + 新增全绿。
  - **执行结果**：`mvn -o -f pom.xml test` → `BUILD SUCCESS`，14 个测试类 / 82 个方法，Failures=0、Errors=0。

- [x] **T11.3 引擎与模型 API 冒烟**
  - `/api/model/manifest`、`aggregates`、`aggregates/{alias}`、`integrity`。
  - 验收：200/401/403/404 正确；报告无重复 alias/悬空引用。
  - **执行结果（2026-11-09 运行态）**：平台 8100 实测 `manifest`/`aggregates`/`aggregates/OntologyCountry`/`integrity` 均 200；无 token 401、guest 403、未知 alias 404。基座单层加载 integrity 为 `duplicateAliases=0`、`danglingRefs=2`（跨层引用告警，`hasErrors=false`）；SIIS 双层加载为 `duplicateAliases=0`、`danglingRefs=0`。

- [x] **T11.4 晋升端到端**
  - 12 条发现实体：匹配预览 → 晋升 9 类样本 → 校验目标表/状态。
  - 验收：hotspot/book/paper 写入正确名称列；重复调用幂等。
  - **执行结果（2026-11-09 真实 MySQL）**：12 条发现实体覆盖 9 类，各晋升 1 条：country `name`、organization `name`、person `name`、hotspot `hotspot_name`、media `name`、book/paper `title`、term `term_name`、location `name` 均写入正确列；9 条状态变为 `PROMOTED`；重复晋升返回 `ALREADY_EXISTS` 且目标表仅 1 行，幂等通过。晋升前匹配预览返回 404（目标实体不存在，符合预期）。

- [x] **T11.5 管理台端到端**
  - 登录 → 总览 → 实体 CRUD → 搜索 → i18n → 晋升 → AI。
  - 验收：7 路由全通；401 自动跳登录；无控制台报错。
  - **执行结果（2026-11-09 浏览器实测）**：7 路由全部 PASS；总览模型树/计数、country 列表 30 条、搜索 24 条、i18n 表单、晋升预览与 AI 对话页均正常；控制台无未捕获错误；退出后访问受限页自动回 `#/login`。

- [x] **T11.6 跨端口与降级**
  - 8100 调 8101；停 8101 后平台视图仍可用并提示。
  - 验收：CORS 正常；降级提示可见。
  - **执行结果（2026-11-09 运行态）**：白名单 origin `http://127.0.0.1:8100` 预检 200 且带 `Access-Control-Allow-Origin`，`http://evil.example` 403；停 8101 后 8100 与总览仍可用，实体/晋升页不白屏并显示“无法连接服务，请确认平台 8100 / 业务服务 8101 已启动”；控制台无未捕获崩溃。为此补充 `app.js` 网络异常提示（原为原生 `Failed to fetch`）。

- [x] **T11.7 回归与回滚演练**
  - 关闭 `dsh.ontology.model.enabled` 验证应用仍可启动（可选降级模式）。
  - 记录回滚步骤并演练一次。
  - 验收：回滚路径可执行；数据无损。
  - **执行结果（2026-11-09）**：`DSH_ONTOLOGY_MODEL_ENABLED=false` 在 8102 启动成功、health 200，模型 API 返回 503 `{code,msg,data}`（为此将 `ModelController` 改为 `ObjectProvider` 注入，避免引擎缺失导致启动失败）；回滚演练用 tag `0.2.1` 创建临时 worktree（补 T0.1 根 pom 模块修正 + `SPRING_DATASOURCE_URL` 用 `UTF-8`），8102 启动 health 200；演练前后数据量一致（country 33、person 31、term 21），无 schema 变更、无数据丢失。SIIS 回滚点为 `ac0f1ea`，回滚步骤已归档，未重复演练。

**T11 完成标准**：所有验收项通过；回归绿；回滚可执行。

---

## **T12 双栈契约与 ai-pilot 对齐**

**目标**：把 0.2.2 新增能力同步到产品层双栈基线，避免 Java/Python 契约漂移；不引入 springdoc，不扩大 Java 实现范围。

- [x] **T12.1 建立契约基线文件**
  - 创建 `ai-pilot/contract/openapi-baseline.yaml`（当前目录不存在）。
  - 至少写入 0.2.2 的 5 个新增只读端点：`/api/model/manifest`、`/api/model/aggregates`、`/api/model/aggregates/{alias}`、`/api/model/integrity`、`/api/ontology/discovered`。
  - 已有端点可按 Java 现有路径逐步补录，不在本期一次性全量导出。
  - 验收：基线文件可被 OpenAPI 解析器读取；5 个 path 齐全。
  - **执行结果**：`ai-pilot/contract/openapi-baseline.yaml` 已创建（OpenAPI 3.0.3），PyYAML 解析通过，5 个 path 与 `components.schemas`（Result/4 个 VO/IntegrityIssue/FieldViolation/DiscoveredRow）齐全。

- [x] **T12.2 固化响应与错误码契约**
  - 统一 `{code, msg, data}`；400/401/403/404 语义与 Python 版一致。
  - 分页结构、字段命名（camelCase）保持一致。
  - 验收：`ModelControllerTest` 与 `DiscoveredControllerTest` 的样例与基线一致。
  - **执行结果**：`ModelControllerTest` 6 用例覆盖包装与 400/401/403/404，与基线一致；`/api/ontology/discovered` 实际返回数据库列名（snake_case），已在 baseline 中登记为已知命名例外，未新增 `DiscoveredControllerTest`（避免重复 Spring 上下文，改由 `OntologyPromotionRouteTest` 断言字段裁剪）。

- [x] **T12.3 同步 ai-pilot 设计文档**
  - `ai-pilot/poc/TASK-202611-001/design.md` 增加 Java 0.2.2 的 5 端点、晋升 9 类、端口修正（8100/8101）与契约基线状态。
  - 更新版本号为 `v0.3.1`。
  - 验收：设计文档与 `spec.md` §0 / `design.md` §1.6 口径一致。
  - **执行结果**：design.md 已为 v0.3.1（2026-11-08 同步 5 端点与端口）；2026-11-09 补正契约基线状态为“已落盘”，并更新 §9.5 与 R-002 说明。

- [x] **T12.4 修正端口与部署口径**
  - Java 平台 8100、SIIS 8101、Python 8093、Harness 8090。
  - 8092 仅作为外部映射端口，不写默认配置；同步修正 `01-平台层需求规格.md` / `03-非功能需求与验收.md` 中 PLT-N2-P0-2 的端口描述。
  - 验收：设计文档、需求文本、发布说明、部署说明四处一致。
  - **执行结果**：`00-需求总览.md`、`01-平台层需求规格.md`（PLT-N2-P0-2 与 Q-7）、`03-非功能需求与验收.md` 均已改为 8100/8101；发布说明 §4.6/§5 与 design.md D-007 一致。

- [ ] **T12.5 双栈契约 diff（可选/联调时执行）**
  - Python 仓可用时，以基线执行 `openapi-diff`；不可用时执行字段/错误码人工比对清单。
  - 验收：无 breaking change；差异项记录并评审。
  - **执行结果（2026-11-12，Python 0.3.0 实测）**：Python 仓已可用，导出双侧 OpenAPI（platform 88 路径 / SIIS 57 路径）与基线逐项 diff，**5 个端点全部缺失且无等价路径**：
    - `python-platform`（`dsh-ontology`）：`/api/model/manifest`、`/api/model/aggregates`、`/api/model/aggregates/{alias}`、`/api/model/integrity`
    - `python-siis`（`dsh-ontots-siis`）：`/api/ontology/discovered`
    - 因端点缺失，参数/响应包装/错误码的字段级 diff 无法进行；`discovered` 的 snake_case 例外仍保留登记。
  - **结论**：**不通过**，需 Python 0.3.1 侧补齐 5 个端点后复测；在补齐前 0.2.2 不标记“已发布”。

- [x] **T12.6 表基线差异登记**
  - 记录 Java 38 表 vs 产品层 57 表；19 张新增表属 Python 0.3，不纳入 Java 0.2.2。
  - 验收：发布说明“已知限制”章节包含该差异。
  - **执行结果**：发布说明 §5 第 11 条已登记 38 vs 57 与 19 张新增表归属。

**T12 完成标准**：5 个新端点已进入契约基线；ai-pilot 设计同步；端口与表基线差异有明确登记。

---

## **附录 A：新增/修改文件清单（按仓库）**

### 平台仓库 `dsh-java-ontology`

| 文件 | 类型 |
|------|------|
| `pom.xml` | 修改 |
| `dsh-onto-base/src/main/resources/ontology-models/m1-object-model.yaml` | 修改 |
| `dsh-onto-base/.../ontology/engine/**` | 新增 |
| `dsh-onto-base/.../common/exception/SchemaValidationException.java` | 新增 |
| `dsh-onto-flow/.../plat/controller/ModelController.java` | 新增 |
| `dsh-onto-flow/.../plat/model/{ManifestVO,DomainGroupVO,AggregateVO,IntegrityVO}.java` | 新增 |
| `dsh-onto-flow/.../plat/controller/ApiExceptionHandler.java` | 修改（SchemaValidationException → 400） |
| `dsh-onto-flow/.../workspace/service/WorkspaceCrudService.java` | 修改 |
| `dsh-onto-app/src/main/resources/static/index.html` | 重写 |
| `dsh-onto-app/src/main/resources/static/styles.css` | 重写 |
| `dsh-onto-app/src/main/resources/static/app.js` | 重写 |
| `dsh-onto-app/src/main/resources/application.yml` | 修改 |
| `docs/0.2.2/spec.md` / `design.md` / `tasks.md` / `release-v0.2.2-development-notes.md` | 新增 |
| `docs/dev-ops/demo/demo_ontology_onto.sql` | 新增 |
| `CHANGELOG.md` | 修改 |

### SIIS 仓库 `dsh-ontolab-siis`

| 文件 | 类型 |
|------|------|
| `dsh-ontolab-siis-app/src/main/resources/siis-models/siis-object.yaml` | 修改 |
| `.../ontology/service/OntologyPromotionService.java` | 修改 |
| `.../ontology/service/OntologyMatchService.java` | 修改 |
| `.../ontology/service/OntologyCrudService.java` | 修改 |
| `.../controller/OntologyExceptionHandler.java` | 新增（SchemaValidationException → 400） |
| `.../config/SiisCorsConfig.java` | 新增（白名单 + 移除平台通配 CorsFilter） |
| `.../controller/OntologyCrudController.java` | 修改（新增 discovered 只读端点） |
| `docs/dev-ops/demo/demo_ontology_siis.sql` | 新增 |

---

### 跨栈文档与契约（`ai-pilot`）

| 文件 | 类型 |
|------|------|
| `ai-pilot/poc/TASK-202611-001/design.md` | 修改（v0.3.1：0.2.2 端点/晋升/端口/契约状态） |
| `ai-pilot/contract/openapi-baseline.yaml` | 新增（5 个 0.2.2 端点契约片段） |

---

## **附录 B：映射变更检查清单（新增类型时必填）**

1. YAML 聚合根（含 `category`、`attributes`、`lifecycle`、`tags`）。
2. `onto_ontology_type` 迁移/种子行（`type_code`、`table_name`、`domain`）。
3. `TypeCatalog` 常量（`dataSource`、`nameColumn`、`promotable`）。
4. 物理表 DDL（名称列必须与 `nameColumn` 一致）。
5. `TypeCatalogConsistencyTest` 期望更新。
6. 演示数据与前端路由（如需展示）。

> 未完成 1-5 任一环节，不得合入。该清单用于防止“晋升 6 类”式漂移再次发生。
