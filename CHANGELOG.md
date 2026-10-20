# 版本

## 版本号：0.2.1
- 更新日期：2026-10-18

1. 工程由双模块拆分为六模块：`dsh-onto-base`（静态本体定义 + 规则 + 通用基础设施）、`dsh-onto-flow`（认证授权 + 工作台 + 全局知识聚合）、`dsh-onto-qa`（报告）、`dsh-onto-viz`（前端静态资源）、`dsh-onto-app`（聚合应用）、`dsh-onto-plugin`（平台工具插件）
2. 版本号升级至 0.2.1，`dsh-java-types` 契约依赖升级至 0.2.1
3. `flow` 新增认证授权体系（JWT + BCrypt + 主任/研究员/访客三级角色）、统一工作台、全局知识聚合（个人 → 公共 → 全局三级知识架构）
4. 新增 `dsh-onto-qa` 报告模块，报告表收敛（`ADR-004`）
5. Harness 插件：平台插件 3 个工具 + SIIS 插件 12 个工具，共 15 个原子工具注册到 dsh-java
6. SIIS 子项目（`dsh-ontoext-siis`）补齐四域业务（ontology / trace / display / analyze）与 Flyway 迁移脚本 V1.0.0~V1.0.5，应用端口 8101
7. 表命名规范统一为前缀制（`onto_sys_` / `onto_common_` / `onto_global_` / `gwy_*`），共 52 张表 + 2 视图（`v_unified_monitor`、`v_unified_kg`）
8. 新增文档体系：`docs/0.2.1`（平台设计与规格、ONTO_DB_DESIGN、架构对比、插件拆分方案、热点分析 spec 与任务清单）、`docs/adr/ADR-001~005`、`docs/contract/permissions.yaml`、`docs/dev-ops/dsh_java_ontodb.sql`
9. 新增 Agent 配置 3 份（director-assistant / research-assistant / topic-creator）与 Skill 4 份（hotspot-analysis / knowledge-extraction / report-generation / topic-creation）

- **dsh-onto-base**：本体七模型 YAML（m1~m7 + manifest）、通用 `PageRequest`/`Result`/`BaseEntity`/`BaseRepository`、插件基类（`AdminApiClient` + `BaseOntologyTool`）
- **dsh-onto-flow**：JWT 认证与角色拦截、统一工作台（WorkspaceController）、全局知识聚合与知识图谱汇聚
- **dsh-onto-app**：聚合 base + flow + report + visulization，端口 8100
- **dsh-onto-plugin**：输出 3 个平台工具（workspace_overview / knowledge_aggregate / director_overview）
- **dsh-ontoext-siis-app**：四域业务 + 完整 schema，端口 8101
- **dsh-ontoext-siis-plugin**：输出 12 个业务工具（本体查询/图谱/关系分析/预警/话题/爬取/NLP/报告等）


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
