# DeepSeek Harness Ontology v0.2.2 开发说明

> 版本：`v0.2.2` | 基线：`v0.2.1` | 文档状态：**P1/P2 代码与文档已实施，运行态验收完成；T12.5 双栈 diff 已执行但未通过（Python 0.3.0 缺 5 个端点）** | 更新日期：2026-11-12
>
> 本说明对应同目录 `spec.md`（需求规格）、`design.md`（技术方案）、`tasks.md`（任务清单）。**T0-T11 与 T12（除 T12.5）已于 2026-11-09 执行完成**（§4.1 记录构建与测试，§4.2-§4.6 记录运行态验证结果）；T12.5 双栈 OpenAPI diff 已于 2026-11-12 执行：Python 0.3.0 缺 5 个端点，未通过，待 Python 补齐后复测并改为“已发布”。
>
> 来源：`static-ontology-implementation-design.md`（v1.0）、`design-review.md`（2026-10-21）、`adr/ADR-001~005`；双栈对齐依据 `ai-pilot/poc/TASK-202611-001/design.md`（v0.3.1，2026-11-08 同步 Java 0.2.2）。动态本体 D1-D4 见 `dynamic-ontology-plan.md`，**不属于本版本**。

---

## **一、版本概述**

| 项目 | 内容 |
|------|------|
| 版本号 | `0.2.2` |
| 基线 | `0.2.1`（平台版本已对齐，静态本体功能尚未落地） |
| 主线 | 静态本体引擎化 + 管理界面可用化 |
| 涉及仓库 | `dsh-java-ontology`（平台）、`dsh-ontolab-siis`（业务）、`ai-pilot`（设计/契约文档，仅文档同步） |
| 新增模块 | 无 |
| 新增第三方依赖 | 无 |
| 新增数据库表 | 无 |
| 新增 API | `/api/model/*` 4 个只读端点；`/api/ontology/discovered` 1 个只读端点；共 5 个，需镜像到 Python 双栈 |
| 主要变更 | YAML 规范化、模型引擎、Schema 校验、TypeCatalog、晋升 9 类、管理台 7 视图、演示数据、工程治理 |
| 明确不做 | 动态本体 D1-D4、MU 14 菜单全量、Neo4j 深化、向量检索、沙盘数字孪生、报告双表阶段 2/3 |

**一句话总结**：v0.2.2 把“七模型 YAML 设计文档”变成“启动即可加载、写入即校验、界面即可管理”的静态本体运行时，并把长期存在的手工映射漂移收敛为可测试的单一事实源。

---

## **二、交付内容（T1-T8 已实施，其余为计划）**

### 2.1 模型层

1. **M1 规范化**：消除裸属性块；8 个本体域聚合根补齐实体头与 `category`；14 类 `category` 与 `onto_ontology_type` 对齐。
2. **模型归位**：基座承载共享本体域（8 类）与系统/元数据实体；项目层承载 track/analyze/display 个人层实体；删除重复 `alias`。
3. **项目清单修复**：`base_manifest` 仅作声明，加载器按 classpath 约定读取基座清单。

### 2.2 引擎层

1. `ModelLoader`：基座 + 项目层清单/YAML 解析；SnakeYAML 安全构造；裸块/重复 alias/未知类型检测。
2. `OntologyRegistry`：`alias` / `category` 双索引；只读；O(1) 查询。
3. `AggregateDescriptor` / `AttributeDescriptor`：不可变描述符，含 `sourceFile` / `sourcePath`。
4. `ModelIntegrityChecker`：重复别名、重复字段、空属性、枚举空值、悬空引用、基线计数。
5. `SchemaValidator`：必填/类型/枚举/未知字段；支持部分更新。
6. `TypeCatalog`：14 类；`dataSource` / `nameColumn` / `promotable` 三维一体。
7. `OntologyEngineConfiguration`：启动即加载；基座失败 fail-fast；项目层可选。

### 2.3 写入校验

1. `WorkspaceCrudService.create/update` 与 `OntologyCrudService.create/update` 服务层挂载。
2. 校验失败返回字段级错误 `{field, rule, expected, actual, message}`。
3. 开关 `dsh.ontology.schema-validation.enabled` 默认开启；可回退。
4. 为保持 141 个存量测试不变，采用可选字段注入；`schemaValidator=null` 时跳过。

### 2.4 类型路由

1. 晋升从 6 类补齐至 **9 类**。
2. 匹配从 6 类补齐至 **9 类**，并补名称列 `hotspot_name` / `title` / `title`。
3. 晋升插入按类型选择名称列，修复 hotspot/book/paper 必然 SQL 失败的问题。
4. `bilateralRelation` 正确映射到 `gwy_ontology_bilateral_relation`，且不可晋升。
5. 四方一致性测试锁定：TypeCatalog、`onto_ontology_type`、晋升目标、匹配路由。

### 2.5 模型 API

| 端点 | 说明 |
|------|------|
| `GET /api/model/manifest` | 基座/项目版本、模型文件、实体/字段计数 |
| `GET /api/model/aggregates` | 按域分组的聚合根摘要 |
| `GET /api/model/aggregates/{alias}` | 单聚合根完整 Schema |
| `GET /api/model/integrity` | 启动缓存的完整性报告 |

全部 `@RequiresRole(1)`；不返回绝对路径、堆栈、YAML 原文、数据库信息。

### 2.6 管理界面

7 个 hash 路由：

| 路由 | 视图 |
|------|------|
| `#/login` | 登录（JWT 存 localStorage） |
| `#/overview` | 总览（模型树/计数/完整性摘要） |
| `#/entities/{category}` | 实体维护（动态表单 + CRUD） |
| `#/search` | 主表 + i18n 跨语种搜索 |
| `#/i18n/{category}/{id}` | 多语种维护（en/fr/ar） |
| `#/promote` | 晋升工作台（预览/目标选择/结果） |
| `#/ai` | AI 对话（同步 + SSE） |

原生 HTML/CSS/JS，无构建步骤、无前端框架；深色主题对齐 `mu-ui-model.yaml`。

### 2.7 跨端口与安全

1. SIIS 新增白名单 `CorsConfig`，默认仅 `127.0.0.1:8100` / `localhost:8100`。
2. 禁止 `*` 通配；允许 `Authorization` / `Content-Type`；预检缓存 3600 秒。
3. 所有新增端点显式 `@RequiresRole(1)`；SQL 表名/列名仅来自 TypeCatalog 白名单。

### 2.8 演示数据

1. `dsh-ontolab-siis/docs/dev-ops/demo/demo_ontology_siis.sql`：国家 32、组织 60、人物 30、媒体 22、热点 8、双边关系 28、书籍 10、论文 8（共 198 行）；`onto_ontology_version.entity_count` 动态回填。
2. `dsh-java-ontology/docs/dev-ops/demo/demo_ontology_onto.sql`：术语 20、地名 15、发现实体 12、i18n 632（8 类 × 7 语种抽样，`INSERT ... SELECT` 按名称/ISO 定位）。
3. 可重灌、非 Flyway、客观中立；`created_by='demo-0.2.2'` 先删后插；`portrait_url` 与 `bias_rating` 全为 NULL，热点风险仅 low/medium。
4. 已做静态校验：INSERT 列名与两仓 DDL 一致；行数与声明一致。真实导入待 demo 库执行后核对。

### 2.9 工程治理

1. 根 `pom.xml` 移除已迁出的 SIIS 失效模块（原主仓库无法 `mvn validate`，现已恢复）。
2. `dsh.app.version` 修正为 `0.2.2`。
3. `CHANGELOG.md` 增加本版本交付项，非仅版本对齐。

---

## **三、任务映射**

| 交付内容 | 任务组 | 关键验收 |
|---------|--------|---------|
| 工程可构建 | T0 | `mvn -o -DskipTests validate` 成功 |
| YAML 规范化/归位 | T1 | 引擎加载零错误；无重复 alias；快照不下降 |
| 模型引擎 | T2 | 双索引/完整性/错误路径单测通过 |
| Schema 校验 | T3 | 必填/类型/枚举非法被拒；存量测试不变 |
| TypeCatalog | T4 | 四方一致性测试通过 |
| 晋升/匹配 | T5 | 9 类可用；hotspot/book/paper 名称列正确；幂等 |
| 模型 API | T6 | 200/401/403/404 正确；无敏感信息 |
| 管理界面 | T7 | 7 路由全通；401 跳登录 |
| 跨端口 | T8 | 8100 调 8101 成功；非白名单被拒 |
| 演示数据 | T9 | 可重灌；计数一致；中立性抽查通过 |
| 版本治理 | T10 | 版本统一；CHANGELOG 完整 |
| 集成验收 | T11 | 全量回归 + 端到端 + 回滚演练 |

---

## **四、验证计划与已执行结果**

> T0 已于 2026-11-09 执行并回填；§4.2-§4.6 仍为待执行断言。

### 4.1 构建与测试（已执行）

```bash
# 平台仓库
cd dsh-java-ontology
mvn -o -DskipTests validate        # EXIT=0
mvn -o clean test                  # BUILD SUCCESS
mvn -o -pl dsh-onto-plugin -am -DskipTests test-compile   # EXIT=0

# SIIS 仓库
cd ../dsh-ontolab-siis
mvn -o test                        # BUILD SUCCESS
```

**实际结果（2026-11-09）**：

| 仓库 | 命令 | 结果 |
|------|------|------|
| 平台 | `mvn -o -DskipTests validate` | EXIT=0；`project.version` = `0.2.2` |
| 平台 | `mvn -o clean test` | BUILD SUCCESS；16 个测试类 / 110 个方法（base 44、app 66），Failures=0、Errors=0 |
| 平台 | `mvn -o -pl dsh-onto-plugin -am -DskipTests test-compile` | EXIT=0；`dsh-java-types:0.2.2` SPI 编译通过 |
| SIIS | `mvn -o test` | BUILD SUCCESS；14 个测试类 / 82 个方法，Failures=0、Errors=0 |

**门槛**（P1 新增用例已计入，偏差已记录）：

1. 平台存量 74 个 `@Test` 为 0.2.1 基线；P1 新增 `SchemaValidatorTest`（14）、`TypeCatalogConsistencyTest`（5）、`ModelControllerTest`（6），并对 YAML 基线与引擎测试扩充，`clean` 后共 16 个测试类 / 110 个方法（base 44、app 66）。
2. SIIS 存量 67 个 `@Test`（11 个类）；P1 新增 `OntologyPromotionRouteTest`（8）、`SiisCorsTest`（2）与 `OntologyServiceTest` 校验用例（2），共 14 个测试类 / 82 个方法。
3. 合计 192 个测试（110 + 82）全部通过；P1 未修改任何既有断言（仅 `AppMetaServiceTest` 的版本断言随 `dsh.app.version` 同步为 `0.2.2`）。

### 4.2 引擎与模型 API

> 单测已执行：`ModelControllerTest` 6 个用例覆盖 200/401/403/404、manifest 计数、aggregates 分组、integrity 空报告与响应脱敏；运行态冒烟已于 2026-11-09 在 8100 完成（见下）。

```bash
curl -s http://127.0.0.1:8100/api/model/manifest
curl -s http://127.0.0.1:8100/api/model/aggregates
curl -s http://127.0.0.1:8100/api/model/aggregates/OntologyCountry
curl -s http://127.0.0.1:8100/api/model/integrity
```

**断言**：

1. 未带 token → 401；访客 token → 403；未知 alias → 404。
2. `integrity` 中 `duplicateAliases=[]`、`danglingRefs=[]`。
3. 响应不含绝对路径、堆栈、连接串、YAML 原文。

### 4.3 晋升端到端

> 单测已执行：`OntologyPromotionRouteTest` 8 个用例覆盖 9 类路由、book/paper 的 `title`、未知类型/不可晋升/双边关系拒绝、幂等与通用表回落；端到端已于 2026-11-09 在真实 MySQL 执行，9 类各晋升 1 条，名称列与幂等均通过（详见 `tasks.md` T11.4）。

1. 取 12 条 `DISCOVERED` 发现实体。
2. 对 9 类各选 1 条执行匹配预览 → 晋升。
3. 校验目标表：hotspot 写 `hotspot_name`，book/paper 写 `title`，country/person 写 `name`。
4. 重复调用同一发现实体：不产生重复实体，状态仍为 `PROMOTED`。

### 4.4 管理台冒烟

> 前端 `node --check` 通过；真实浏览器冒烟已于 2026-11-09 执行：7 路由全通、控制台无未捕获错误、401 自动跳登录（详见 `tasks.md` T7.12/T11.5）。

1. 打开 `http://127.0.0.1:8100/`，未登录 → 跳 `#/login`。
2. 正确账号登录 → `#/overview` 显示模型树与计数。
3. 进入 country 实体页 → 新增/编辑/删除 → 校验错误可见。
4. 搜索英文关键词 → 命中中文实体的 i18n 翻译。
5. 多语种维护 en/fr/ar → 保存后立即可见。
6. 晋升工作台 → 预览 → 晋升 → 结果显示。
7. AI 视图 → 同步问答 + 流式输出。
8. 控制台无未捕获错误；401 自动跳登录。

### 4.5 跨端口与回滚

> CORS 已单测并完成运行态预检（白名单 200、非白名单 403）；停 8101 后平台仍可用，实体/晋升页显示“无法连接服务，请确认平台 8100 / 业务服务 8101 已启动”；`DSH_ONTOLOGY_MODEL_ENABLED=false` 启动成功、模型 API 返回 503；tag `0.2.1` 回滚 worktree 启动 health 200，数据量前后一致。

1. 停掉 8101：平台视图仍可用；实体/晋升页提示“业务服务未启动”。
2. 非白名单 origin 调 8101 → 浏览器拦截/403。
3. 设 `dsh.ontology.model.enabled=false` → 应用仍可启动（降级模式，不提供模型 API）。
4. 回滚到 v0.2.1 点：代码回退；无 schema 变更；仅需清理 demo 脚本写入的数据。

### 4.6 双栈契约（ai-pilot v0.3.1）

1. `ai-pilot/contract/openapi-baseline.yaml` 包含 5 个新增端点（4 个 `/api/model/*` + `/api/ontology/discovered`）。
2. Java 测试样例与基线一致：`{code, msg, data}`、400/401/403/404、`camelCase`。
3. Python 侧可用时执行 `openapi-diff`；不可用时执行字段/错误码人工 diff。
4. 端口口径：Java 8100、SIIS 8101、Python 8093、Harness 8090；8092 仅外部映射。
5. 表基线差异：Java 38 表 vs 产品层 57 表；19 张新增表属 Python 0.3，不在 0.2.2。
6. **执行结果（2026-11-12，Python 0.3.0）**：已导出双侧 OpenAPI 规范（`dsh-ontology` platform 88 条路径、`dsh-ontots-siis` 57 条路径），按基线逐项 diff，**5 个端点全部缺失且无等价路径**：
   - python-platform：`/api/model/manifest`、`/api/model/aggregates`、`/api/model/aggregates/{alias}`、`/api/model/integrity`
   - python-siis：`/api/ontology/discovered`
   - 因端点缺失，参数、响应包装与错误码的字段级 diff 无法进行；`discovered` 的 snake_case 例外仍保留登记。
   - 结论：**不通过**，需 Python 0.3.1 补齐后复测；补齐前不标记“已发布”。

---

## **五、已知限制与风险**

1. **运行态验收已闭环，但双栈 diff 未通过**：T0-T11 与 T12（除 T12.5）已完成；T11.3-T11.7、T7.12 的运行态冒烟与回滚演练于 2026-11-09 通过；T12.5 已于 2026-11-12 对 Python 0.3.0 执行，5 个端点缺失（4 个 `/api/model/*` + `/api/ontology/discovered`），因此仍不标记“已发布”。
2. **主仓库构建已恢复（2026-11-09）**：T0.1 已移除根 `pom.xml` 的失效 SIIS 模块，平台七包可独立 `validate` / `package`；`dsh.app.version` 与 `plugin.yaml` 版本已统一为 `0.2.2`。
3. **TypeCatalog 仍为代码常量**：新增类型需改 Java；ADR-001 的“全元数据驱动”留待 0.2.3+。
4. **跨库事务不可强一致**：晋升采用“分事务 + 幂等重试”；极端故障下需人工核对 `ontology_entity_id`。
5. **YAML 归位有字段丢失风险**：依赖基线快照与逐块迁移；评审需重点检查 `m1` 与 `siis-object` 的字段总数。
6. **Schema 校验为服务层可选注入**：单测手动构造服务时跳过校验；生产环境由 Spring 保证 Bean 存在。
7. **演示数据仅用于演示**：不代表生产事实；不得导入正式库。
8. **未实现能力**：动态本体 D1-D4、MU 14 菜单全量、Neo4j 深化、向量检索、沙盘数字孪生、报告双表阶段 2/3。
9. **版本口径**：平台与 SIIS 各自独立构建；本文档以平台 0.2.2 为主版本，SIIS 版本需在实施时同步确认。
10. **双栈未完全对齐**：Python v0.3.1 的 M2-M5、OWL2 RL/SHACL、Qdrant/Redis、19 张新增表不在 Java 0.2.2；仅同步 5 个只读端点与 9 类晋升契约。
11. **表基线差异**：Java 侧 38 表，产品层目标 57 表；0.2.2 不新增表，缺口由 Python 0.3 先承接、Java 后续版本追赶。
12. **契约基线已落盘**：`ai-pilot/contract/openapi-baseline.yaml` 已于 2026-11-09 建立，收录 5 个新增端点与统一 Result/错误码；`/api/ontology/discovered` 返回 snake_case 列名，作为已知命名例外登记。

---

## **六、依赖与兼容性**

| 项 | 说明 |
|----|------|
| 运行环境 | Java 17、Spring Boot 3.5.16、MySQL 8 / H2 MODE=MySQL |
| 新增依赖 | 无；YAML 解析复用 snakeyaml（Spring Boot 传递） |
| 前端依赖 | 无；原生 HTML/CSS/JS |
| 数据库变更 | 无新增表；无列类型变更；仅演示数据脚本 |
| 配置变更 | 新增 `dsh.ontology.*`、`dsh.siis.cors.allowed-origins` |
| 兼容性 | 既有 API 路径/请求/响应不变；新增字段只追加；`Result<T>` 风格不变 |
| 双栈 | Java 0.2.2 为产品层基线；Python v0.3.1 镜像 5 个新增端点与 9 类晋升契约 |
| 回滚 | 代码回退 + 关闭引擎/校验开关；无数据迁移回滚 |

---

## **七、发布检查清单（实施完成后勾选）**

- [x] 根 `pom.xml` 修正，主仓库 `mvn -o -DskipTests validate` 通过
- [x] 平台 `mvn -o test` 全绿（实际 16 个测试类 / 110 个方法）
- [x] SIIS `mvn -o test` 全绿（实际 14 个测试类 / 82 个方法）
- [x] YAML 基线快照无下降；无重复 alias/悬空引用（单测）
- [x] 晋升 9 类路由与名称列正确（单测 8 用例）
- [x] 模型 API 401/403/404/200 正确；无敏感信息（单测）
- [x] 模型 API 运行态 curl 冒烟通过（200/401/403/404，2026-11-09）
- [x] 管理台 7 路由全通；401 自动跳登录（2026-11-09 浏览器实测）
- [x] SIIS CORS 白名单生效；非白名单被拒（单测 + 运行态预检）
- [x] 演示数据脚本可重灌、静态校验通过；已实际导入两库（198 + 632 行，行数一致）
- [x] 版本号统一 `0.2.2`；CHANGELOG 含交付项
- [x] 5 个新增端点已写入 `ai-pilot/contract/openapi-baseline.yaml`
- [x] ai-pilot `design.md` 已同步 v0.3.1（端点/晋升/端口/契约状态）
- [ ] 双栈字段与错误码人工 diff 通过（Python 可用时执行 `openapi-diff`；discovered 的 snake_case 已登记为例外）；2026-11-12 实测：Python 0.3.0 缺 5 个端点，未通过
- [x] 回滚演练完成（tag `0.2.1` worktree 启动 health 200，数据无损）；回滚点与步骤归档
- [ ] 本说明状态改为“已发布”，并回填实际验证输出（待 Python 补齐 T12.5 的 5 个端点后）

---

## **八、双栈对齐（ai-pilot v0.3.1）**

| 维度 | Java 0.2.2 | Python v0.3.1 |
|------|-----------|---------------|
| 定位 | 产品层 D0：静态本体引擎 + 模型元数据 | 产品层 M1-M4 全量演进 |
| 端口 | 平台 8100；SIIS 8101 | 8093；Harness 8090 |
| 需求基线 | 72 条 PLT 的产品层子集 | 72 条 PLT 全量 |
| 表基线 | 38 表，不新增 | 57 表（38 + 19） |
| 本版本同步 | 5 个只读端点 + 晋升 9 类 + 响应/错误码 | 镜像上述契约；M2-M5/OWL/SHACL/Qdrant 由 Python 承接 |

**同步原则**：

1. Java 为契约基线；Python 以 `ai-pilot/contract/openapi-baseline.yaml` 做 `openapi-diff`。
2. 新增端点必须双方同路径、同 `{code,msg,data}`、同 400/401/403/404 语义。
3. 0.2.2 不引入 springdoc，不新增 Java 依赖；契约由测试样例 + 手写基线片段维护。
4. 端口与表差异必须写入发布说明，不得用“已对齐”模糊表述。

---

## **九、后续版本衔接**

| 阶段 | 内容 | 版本 |
|------|------|------|
| D0 | 静态本体引擎化（本版本） | 0.2.2 |
| D1 | 规则引擎化（M3） | 0.2.3+ |
| D2 | 行为链事件化（M2） | 0.2.3+ |
| D3 | 流程引擎化（M6） | 0.2.3+ |
| D4 | Action 层（Agent 直连） | 0.2.3+ |

本版本为 D1-D4 预留的边界：只读 `OntologyRegistry`、统一 `TypeCatalog`、`/api/model/*` 元数据端点；不提前实装规则解释器、事件总线、流程执行器与 Action 网关。
