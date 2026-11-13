# 版本

> 本文件按版本倒序记录 dsh-java-ontology 的变更。
> **变更记录约定**
> - 每个版本标注：版本号、日期、变更类型、影响模块、更新内容
> - 变更类型：`新增` / `变更` / `修复` / `移除` / `文档` / `构建`
> - 影响模块使用 `dsh-onto-*` 模块名；跨模块变更使用 `仓库`
> - 历史条目保留原始事实描述，不回填未经确认的细节
> - AI Agent 修改代码后，应同步更新本文件并引用版本号与影响模块
>
> **AI 检索提示**
> - 查某模块能力从哪个版本引入：在对应版本条目中检索模块名（如 `dsh-onto-metric`、`dsh-onto-qa`）
> - 查契约/版本对齐：检索 `dsh-java-types`、`0.2.2`、`parent`
> - 查文档/拆分遗留：检索 `SIIS`、`gwy_*`、`README`
> - 当前版本以根 `pom.xml` 的 `<version>` 为准；子模块版本应与之保持一致

## 版本索引

| 版本 | 日期 | 类型 | 摘要 |
| --- | --- | --- | --- |
| 0.2.2（文档规划） | 2026-11-10 | 文档 | 身份分层对齐：linlan-emis（BuddyOne）为 IdP，本平台改为 Resource Server |
| 0.2.2 | 2026-11-03 | 变更、构建 | 与 dsh-java 0.2.2 契约对齐，11 个 pom 版本统一 |
| 0.2.2（修订） | 2026-11-10 | 修复、构建、新增、文档 | 根 pom 模块清理、版本元信息统一、测试基线修复、静态本体引擎与管理台落地 |
| 0.2.1（迭代 2） | 2026-10-22 | 新增、变更、文档 | 七包定型、新增 metric、SIIS 文档迁出 |
| 0.2.1（迭代 1） | 2026-10-18 | 新增、构建 | 双模块拆分为六模块、认证授权、报告模块、插件 |
| 0.2.0 | 2026-10-11 | 变更、构建 | 与 dsh-java 0.2.0 对齐、Spring Boot 3.5.16 |
| 0.1.0 | 2026-09-01 | 新增 | 初始化版本 |

## 版本号：0.3.0（文档规划）
- 更新日期：2026-11-10

1. 身份分层对齐：明确 **linlan-emis（BuddyOne）** 为唯一 IdP（Java `backend` + Python `py-api` 双路线），`dsh-java-ontology` 不再新建 `dsh-auth`，改为 OAuth2 Resource Server（`docs/0.2.3`）
2. JWT claims 契约调整为以 `tenant_id` / `role_level` / `user_name` 为准，过渡期先读 snake_case、缺失回退旧 camelCase，P4 收口移除回退（`docs/0.2.3`）
3. 明确 `auth_*` 表与租户/成员模型归属 linlan-emis；本仓库不建认证表，只保留 `tenant_id` 与 `TenantContext`（`docs/0.2.3`）
4. 分期重排为 ontology 侧 P0~P4，与 linlan-emis 发版节奏对齐；新增双路线契约漂移风险 R11（`docs/0.2.3`）
5. 文档口径同步：README / README.en / AGENTS.md / llms.txt 增加分层定位与 0.3.0 规划说明（仓库）

## 版本号：0.2.2
- 更新日期：2026-11-03

1. 内部版本号与 dsh-java 0.2.2 对齐（由 0.2.1 升级）
2. `dsh-onto-base` / `dsh-onto-plugin` / `dsh-ontolab-siis-plugin` 三处 `dsh-java-types` 契约依赖升级 0.2.1 → 0.2.2（保持 `provided` scope）
3. 顶层及全部子模块 parent/自身版本统一至 0.2.2（共 11 个 pom、14 处声明）
4. 修正历史版本位口径：`0.2.1-1` / `0.2.1-2` 的版本位统一为 `0.2.1`（与 pom 声明对齐，迭代序号移入标题备注）

## 版本号：0.2.2（修订）
- 更新日期：2026-11-10

1. 修复根 `pom.xml`：移除 `dsh-ontolab-siis/dsh-ontolab-siis-app` 与 `-plugin` 两个失效 `<module>`，平台七包恢复可独立 `validate` / `package`（仓库）
2. 统一版本元信息：`dsh-onto-app/application.yml` 的 `dsh.app.version` 与 `dsh-onto-plugin/META-INF/plugin.yaml` 的 `version` 由旧值统一为 `0.2.2`（`dsh-onto-app`、`dsh-onto-plugin`）
3. 修复测试基线：`PageResultTest` / `ResultTest` / `TenantContextTest` 源目录与 `package net.linlan.dsh.common` 不一致，已移入 `dsh-onto-base/src/test/java/net/linlan/dsh/common/`（`dsh-onto-base`）
4. 验证：`mvn -o clean test` 全七包 `BUILD SUCCESS`，16 个测试类 / 110 个测试方法全部通过（`dsh-onto-base` 44、`dsh-onto-app` 66）（仓库）
5. dsh-java 0.2.2 兼容回归：`dsh-java-types:0.2.2` SPI（`AbstractHarnessPlugin` / `PluginContext` / `PluginHookResult`）未变，插件 3 工具注册与 Bearer 鉴权无改动，`mvn -o -pl dsh-onto-plugin -am test-compile` 通过（`dsh-onto-plugin`）
6. 文档口径同步：README / README.en / AGENTS.md / llms.txt 更新版本状态与测试规模（仓库）
7. 新增静态本体引擎：`ModelLoader` / `OntologyRegistry` / `ModelIntegrityChecker` / `SchemaValidator` / `TypeCatalog` / `OntologyEngineConfiguration` / `OntologyModelEngine`，启动即加载基座 + 项目层模型，坏模型 fail-fast（`dsh-onto-base`）
8. YAML 规范化与归位：M1 补齐 8 个本体域聚合根与 14 类 `category`，个人层字段迁入 `siis-object.yaml`，删除重复 `alias`；基线冻结为基座 25 根 / 200 字段 / 16 枚举、项目层 27 / 292 / 14（仓库）
9. Schema 校验挂载：`WorkspaceCrudService` / `OntologyCrudService` 写库前校验必填/类型/枚举/未知字段，异常统一映射 HTTP 400 + `List<FieldViolation>`（`dsh-onto-base`、`dsh-onto-flow`）
10. 新增模型元数据 API：`GET /api/model/manifest`、`/aggregates`、`/aggregates/{alias}`、`/integrity`，全部 `@RequiresRole(1)`，响应脱敏（`dsh-onto-flow`）
11. 晋升与匹配补齐至 9 类：按 `TypeCatalog` 选择目标表与名称列（`hotspot_name` / `title` / `name`），未知/不可晋升/双边关系显式拒绝，晋升幂等（SIIS）
12. 管理台 7 视图（登录/总览/实体/搜索/多语种/晋升/AI）：原生 HTML/CSS/JS，无构建步骤与外部依赖；2026-11-09 浏览器实测 7 路由全通、控制台无未捕获错误（`dsh-onto-app`）
13. SIIS 跨端口与安全：新增 CORS 白名单（默认 `127.0.0.1:8100` / `localhost:8100`）并移除平台通配 `corsFilter`；JWT 默认密钥与平台对齐；新增 `/api/ontology/discovered` 只读端点（SIIS）
14. 新增演示数据脚本：`dsh-ontolab-siis/docs/dev-ops/demo/demo_ontology_siis.sql`（8 类 198 行）与 `docs/dev-ops/demo/demo_ontology_onto.sql`（术语 20、地名 15、发现实体 12、i18n 632），均不入 Flyway、`created_by='demo-0.2.2'` 可重灌（仓库）
15. 新增模型快照与统计脚本 `docs/0.2.2/model-snapshot/`（当前/原始 YAML + `snapshot.py`），并建立双栈契约基线 `ai-pilot/contract/openapi-baseline.yaml`（仓库）
16. 修复模型引擎禁用时的启动失败：`ModelController` 改为 `ObjectProvider<OntologyModelEngine>` 注入，`dsh.ontology.model.enabled=false` 时应用正常启动、模型 API 返回 503 `{code,msg,data}`（`dsh-onto-flow`）
17. 修复管理台网络异常提示：`app.js` 请求失败时显示“无法连接服务，请确认平台 8100 / 业务服务 8101 已启动”，替代原生 `Failed to fetch`（`dsh-onto-app`）
18. 运行态验收（2026-11-09）：晋升 9 类端到端（名称列正确、幂等）、管理台 7 路由浏览器冒烟、CORS 预检、停 8101 降级、模型引擎关闭启动、tag `0.2.1` 回滚演练均通过；数据量前后一致（仓库）
19. 插件自包含修复：将仅被 `dsh-onto-plugin` 使用的 `AdminApiClient` / `BaseOntologyTool` 从 `dsh-onto-base` 迁入插件模块，修复独立 Jar 在 Harness 加载时 `NoClassDefFoundError`；桌面 Harness 实测 ACTIVE、3 tools（`dsh-onto-plugin` / `dsh-onto-base`）
20. 修复工作空间租户隔离空结果：`WorkspaceRepository` 的 10 处 `tenant_id = ?` 改为“租户为空时不过滤”，主任（JWT 无 `tenantId`）按跨租户视角查询，写操作缺省租户落 `0`；修复后 `workspace_overview` 对 `director` 返回 12 条发现实体，`researcher`（租户 1）仍仅见 4 条（`dsh-onto-flow`）
21. 插件标识统一：`plugin.yaml` 的 `id`、`DshOntoPlugin.PLUGIN_ID` 与系统提示键由 `dsh-onto` 统一为 `dsh-onto-plugin`，工具前缀变为 `plugin__dsh-onto-plugin__*`；README / README.en 安装命令同步（`dsh-onto-plugin`）
22. 桌面端免登录（临时）：新增 `dsh.ontology.auth.default-director-enabled`（默认 `true`），`JwtAuthenticationFilter` 在无有效 JWT 时按主任身份（`roleLevel=0`、跨租户）放行；管理台 `app.js` 不再强制跳转 `#/login`。测试环境该开关为 `false`，保留 401/403 断言；生产环境应通过 `DSH_AUTH_DEFAULT_DIRECTOR=false` 关闭（`dsh-onto-flow` / `dsh-onto-app`）
23. 清理 GWY 遗留表：新增 `V1.0.4__drop_unused_gwy_tables.sql`，删除本体库中零代码引用的 22 张 `gwy_*` 表（analyze 6、display 8、track 8，均 0 行）；保留被 `TypeCatalog`、`KnowledgeAggregator`、`WorklineOrchestrator`、报表模块引用的 14 张（8 张本体实体表 + 5 张图谱/文章/分析/展示表 + 1 张告警表）。不改动 V1.0.0 的 checksum，SIIS 库同名表不受影响（`dsh-onto-app`）
24. 方案 A（8 类实体归并 EAV）：`TypeCatalog` 中 `country` / `organization` / `person` / `hotspot` / `media` / `book` / `paper` / `bilateralRelation` 统一映射 `onto_ontology_entity`（`DataSource.ONTO`、名称列 `entity_name`）；`OntologyMetaRepository` 增加 EAV 分支（按 `category` 过滤、`onto_ontology_attribute` 行转列、属性 upsert、跨列名兼容 `name`/`hotspot_name`/`title`、`search` 同时匹配实体名与属性值、过滤器走 `EXISTS` 子查询）（`dsh-onto-base`）
25. 方案 A 下游同步：`KnowledgeAggregator` / `WorklineOrchestrator` 的双边关系与热点计数改查 EAV 三表 JOIN，不再引用 `gwy_ontology_*`（`dsh-onto-flow`）
26. 新增 `V1.0.5__merge_gwy_ontology_into_eav.sql`：更新 `onto_ontology_type.table_name`；把 8 张 `gwy_ontology_*` 存量行迁入 EAV（`legacy_ref` 精确关联实体与属性，迁完删列）；`DROP TABLE` 8 张本体实体表 + `gwy_display_rpt_report` + `gwy_display_alert_record`，本体库仅保留 4 张项目分析表（`dsh-onto-app`）
27. 方案 A 运行态验收（2026-11-09）：实库执行 V1.0.5 成功，`dsh_java_ontodb` 48 张表、`gwy_ontology_*` 与两张 `gwy_display_*` 已消失、8 类类型映射指向 `onto_ontology_entity`；分页/搜索/CRUD/汇聚接口正常，全量汇聚产出 28 条双边关系；平台测试 110 方法全绿（仓库）
28. 新增 `V1.0.6__drop_unused_ontology_relation.sql`：全表引用审计确认 `onto_ontology_relation` 无代码 / SQL 引用（`TypeCatalog` 未注册 relation 类型），删除后本体库 47 张表（42 `onto_` + 4 项目分析 `gwy_` + Flyway 历史表）（`dsh-onto-app`）
29. 重新生成 `docs/dev-ops/dsh_java_ontodb.sql`：46 张表（不含 Flyway 历史表）+ 种子数据（用户 3 / 角色 3 / 用户角色 3 / 本体版本 1 / 本体类型 14）；已在临时库实测脚本可完整建库（`docs`）
30. 新增 `V1.0.7__rename_gwy_track_tables_to_onto.sql`：本体库表名统一为 `onto_` 前缀——`gwy_track_article` → `onto_track_article`、`gwy_track_kg_node` → `onto_track_kg_node`、`gwy_track_kg_edge` → `onto_track_kg_edge`、`gwy_analyze_track_topic` → `onto_analyze_track_topic`；同步更新 `KnowledgeAggregator` / `AggregationService` / `WorklineOrchestrator` 与相关测试；本体库 46 张业务表全部以 `onto_` 开头（`dsh-onto-app`、`dsh-onto-flow`）
31. 重新生成 `docs/dev-ops/dsh_java_ontodb.sql`（2026-11-10）：46 张表（全部 `onto_` 前缀）+ 种子数据（用户 3 / 角色 3 / 用户角色 3 / 本体版本 1 / 本体类型 14）；已在临时库 `dsh_java_ontodb_gen` 导入实测，表集合与实库完全一致（`docs`）
32. 设计/需求文档同步（2026-11-10）：`docs/0.2.1/ONTO_DB_DESIGN.md` 更新为 46 表口径（新增 `onto_metric_*` 5、报告扩展 5、项目分析 4，移除 `onto_ontology_relation`，说明视图已迁 SIIS 库）；`docs/0.2.2/spec.md`、`design.md`、`static-ontology-implementation-design.md`、`aggregation-layer-refactor-design.md` 中的 `gwy_ontology_*` 基线改为 EAV 口径（`onto_ontology_entity` + `onto_ontology_attribute`），表基线 38 → 46（`docs`）
33. T12.5 双栈契约 diff 执行（2026-11-12）：导出 Python 0.3.0 双侧 OpenAPI（platform 88 路径 / SIIS 57 路径）与 `ai-pilot/contract/openapi-baseline.yaml` 比对，5 个端点全部缺失且无等价路径（4 个 `/api/model/*` + `/api/ontology/discovered`），diff 不通过；0.2.2 保持“未发布”，待 Python 补齐后复测（`docs/0.2.2`）

## 版本号：0.2.1（迭代 2）
- 更新日期：2026-10-22

1. 模块结构定型为七包：`dsh-onto-base` / `dsh-onto-flow` / `dsh-onto-qa`（问数，由 `dsh-onto-report` 更名）/ `dsh-onto-viz`（可视化，由 `dsh-onto-visulization` 更名）/ `dsh-onto-metric`（新增指标包）/ `dsh-onto-app` / `dsh-onto-plugin`
2. 新增 `dsh-onto-metric` 指标包：识别业务对象 / 业务流程 / 关键决策点，区分原子 / 派生 / 复合指标，建立指标依赖树
3. **文档按仓库边界拆分**：SIIS 四域业务（ontology / trace / display / analyze）的全部说明迁出至 `dsh-ontolab-siis/README.md`，并新增英文版 `dsh-ontolab-siis/README.en.md`；主仓库 `README.md` / `README.en.md` 只描述平台自身七包，新增「配套工程」小节指向 SIIS
4. 表命名规范按仓库边界拆分：本仓库 `onto_*` 共 38 张表（含 `onto_metric_` 5 张），SIIS `gwy_*` 38 张 + 2 视图；标注 `V1.0.0__init_schema.sql` 中 36 张 `gwy_*` 表为拆分遗留，后续移除
5. 工具统计按仓库边界拆分：本仓库平台插件 3 个原子工具（workspace_overview / knowledge_aggregate / director_overview），SIIS 插件 12 个业务工具
6. 修正测试统计：本仓库 13 个测试类 / 74 个测试方法（`dsh-onto-base` 3 个、`dsh-onto-app` 10 个）
7. `docs/0.2.2` 新增设计评审、静态本体实施方案、动态本体规划与 4 篇方法论文章

- **dsh-onto-metric**：指标建模 + Flyway `V1.0.2__metric_tables.sql`（`onto_metric_` 5 张表）
- **README.md / README.en.md**：架构树、运行链路图、快速开始、配置项、模块依赖、安全边界同步去除 SIIS 内容

## 版本号：0.2.1（迭代 1）
- 更新日期：2026-10-18

1. 工程由双模块拆分为六模块：`dsh-onto-base`（静态本体定义 + 规则 + 通用基础设施）、`dsh-onto-flow`（认证授权 + 工作台 + 全局知识聚合）、`dsh-onto-qa`（报告）、`dsh-onto-viz`（前端静态资源）、`dsh-onto-app`（聚合应用）、`dsh-onto-plugin`（平台工具插件）
2. 版本号升级至 0.2.1，`dsh-java-types` 契约依赖升级至 0.2.1
3. `flow` 新增认证授权体系（JWT + BCrypt + 主任/研究员/访客三级角色）、统一工作台、全局知识聚合（个人 → 公共 → 全局三级知识架构）
4. 新增 `dsh-onto-qa` 报告模块，报告表收敛（`ADR-004`）
5. Harness 插件：平台插件 3 个工具（SIIS 插件 12 个工具自 0.2.2 起随 SIIS 独立工程迁出）
6. SIIS 子项目（`dsh-ontoext-siis`，后更名 `dsh-ontolab-siis`）补齐四域业务（ontology / trace / display / analyze）与 Flyway 迁移脚本 V1.0.0~V1.0.5，应用端口 8101
7. 表命名规范统一为前缀制（`onto_sys_` / `onto_common_` / `onto_global_` / `gwy_*`），共 52 张表 + 2 视图（`v_unified_monitor`、`v_unified_kg`）
8. 新增文档体系：`docs/0.2.1`（平台设计与规格、ONTO_DB_DESIGN、架构对比、插件拆分方案、热点分析 spec 与任务清单）、`docs/0.2.2/adr`、`docs/contract/permissions.yaml`、`docs/dev-ops/dsh_java_ontodb.sql`
9. 新增 Agent 配置 3 份（director-assistant / research-assistant / topic-creator）与 Skill 4 份（hotspot-analysis / knowledge-extraction / report-generation / topic-creation）

- **dsh-onto-base**：本体七模型 YAML（m1~m7 + manifest）、通用 `PageRequest`/`Result`/`BaseEntity`/`BaseRepository`、插件基类（`AdminApiClient` + `BaseOntologyTool`）
- **dsh-onto-flow**：JWT 认证与角色拦截、统一工作台（WorkspaceController）、全局知识聚合与知识图谱汇聚
- **dsh-onto-app**：聚合 base + flow + qa + viz（0.2.2 起含 metric），端口 8100
- **dsh-onto-plugin**：输出 3 个平台工具（workspace_overview / knowledge_aggregate / director_overview）


## 版本号：0.2.0
- 更新日期：2026-10-11

1. 版本号与 dsh-java 0.2.0 对齐（由 0.1.0-SNAPSHOT 升级）
2. spring-boot 版本升级 3.3.2 → 3.5.16
3. mybatis-spring-boot 版本升级 3.0.4 → 3.0.5
4. 父 pom 由 dsh-java 拷贝改回 dsh-java-ontology 自身元信息与模块列表
5. scm 与项目 url 统一为 linlannet 仓库子路径
6. 对 dsh-java-types 契约包依赖升级至 0.2.0

- **dsh-java-ontology-app**：版本对齐 0.2.0，spring-boot 3.5.16
- **dsh-java-ontology-plugin**：版本对齐 0.2.0，依赖 dsh-java-types 0.2.0

## 版本号：0.1.0

- 更新日期：2026-09-01
- 初始化版本
