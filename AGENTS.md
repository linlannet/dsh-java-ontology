# AGENTS.md — dsh-java-ontology

## Project
本体知识工程平台（Java 版）：base + flow + 领域能力包，聚合应用监听 8100，输出 3 个 Harness 工具。版本 0.2.2，与 `dsh-java` 契约对齐。
分层定位：WorkBuddy 对标生态（`dsh-java` / DSH-Desktop）的下游资源服务；统一身份/租户由 **linlan-emis（BuddyOne，Java `backend` + Python `py-api`）** 签发，本平台 0.3.0 起作为 OAuth2 Resource Server（JWKS 验签），不新建 IdP、不做 `dsh-auth`。

## Layout
- `dsh-onto-base/` 静态本体 + 规则 + 通用基础设施 + 插件基类 + 七模型 YAML
- `dsh-onto-flow/` 动态本体、认证授权（JWT/BCrypt/角色；0.3.0 起 JWKS 验签）、工作台、全局知识聚合
- `dsh-onto-qa/` 报告与问数（模板/章节/图表/会话/任务）
- `dsh-onto-viz/` 可视化静态资源
- `dsh-onto-metric/` 指标建模与依赖树（Flyway `V1.0.2`）
- `dsh-onto-app/` 聚合应用（端口 8100，Flyway `V1.0.0~V1.0.1`）
- `dsh-onto-plugin/` 3 个平台工具的插件 JAR
- `agents/` 3 份 Agent 配置；`skills/` 4 份 Skill
- `docs/0.2.1`、`docs/0.2.2`、`docs/contract`、`docs/dev-ops`
- `dsh-ontolab-siis/` 配套 SIIS 工程目录（被 `.gitignore` 忽略，不在本仓库版本控制内）

## Build & Test
- 构建平台七包：`mvn -pl dsh-onto-app -am package -DskipTests`
- 测试：`mvn -pl dsh-onto-base,dsh-onto-app -am test`（16 类 / 110 方法）
- 启动：`java -jar dsh-onto-app/target/dsh-onto-app-0.2.2-exec.jar`
- 开发模式：`mvn spring-boot:run -pl dsh-onto-app`
- 全量 `mvn package` 只构建平台七包；SIIS 使用独立父 pom

## Runtime
- 平台端口 8100；Harness `dsh-java` 8090；配套 SIIS 8101
- 数据库默认 `dsh_java_ontodb`（`DB_HOST` / `DB_PORT` / `DB_NAME`；旧默认名 `ontology_db` 已废弃）
- 身份源：`linlan-emis`（BuddyOne）为唯一 IdP；当前过渡态仍自签 HS256（`JWT_SECRET`，生产必须覆盖），0.3.0 起改验 JWKS（`issuer` / `jwks-uri` / `audience`），claims 以 `tenant_id` / `role_level` / `user_name` 为准并兼容旧 camelCase
- 查询超时 15s、最多 100 条

## Conventions
- 保持模块边界：`base` 不依赖业务包；`flow` 依赖 base；`qa` 依赖 base + flow；`metric` 依赖 base
- 表按前缀归属模块（`onto_sys_` / `onto_common_` / `onto_notify_` / `onto_ontology_` / `onto_report_` / `onto_global_` / `onto_metric_` / `onto_track_` / `onto_analyze_`）
- 新增表必须同步 Flyway 脚本与对应模块
- 接口统一返回框架结果封装；权限使用 `docs/contract/permissions.yaml` 契约
- 插件只做薄封装，业务逻辑留在 Controller/Service
- 版本变更同步 `CHANGELOG.md` 与 README 中的版本号、jar 名

## Do Not
- 不提交 `target/`、`.idea/`、`.DS_Store`
- 不提交数据库密码、JWT 密钥、Harness Token
- 不把业务逻辑写进插件
- 不修改 `V1.0.0` 历史迁移脚本（新增变更用新版本脚本）
- 不擅自修改 `dsh-java-types` 契约版本，需与 `dsh-java` 对齐
- 不声称规划中的能力已交付

## Known Items
- 方案 A + V1.0.7 后本体库业务表全部以 `onto_` 开头（原 4 张项目分析表 `gwy_track_article` / `gwy_track_kg_node` / `gwy_track_kg_edge` / `gwy_analyze_track_topic` 已改名为 `onto_track_article` / `onto_track_kg_node` / `onto_track_kg_edge` / `onto_analyze_track_topic`）；8 类业务实体走 `onto_ontology_entity` + `onto_ontology_attribute`（EAV），历史 8+2 张 `gwy_*` 由 `V1.0.5` 迁移后删除

## Key Docs
- `README.md` / `README.en.md` / `llms.txt`
- `CHANGELOG.md`
- `docs/0.2.1/` / `docs/0.2.2/`
- `docs/0.3.0/auth-tenant-design.md`（身份/租户接入：linlan-emis = IdP，本平台 = RS）
- `docs/contract/permissions.yaml`
- `dsh-ontolab-siis/README.md`