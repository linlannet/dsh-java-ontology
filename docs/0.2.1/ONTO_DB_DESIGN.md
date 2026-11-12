# DSH Onto 底层数据库设计文档

> 数据库：`dsh_java_ontodb`（底层共享；早期文档中的 `ontology_db` 为旧占位名）
> 数据库版本：MySQL 8.0
> 文档版本：v0.2.2
> 更新：2026-11-10
> 共 **46 张表**（sys 4 + common 6 + notify 2 + global 5 + report 12 + metric 5 + ontology 8 + 项目分析 4）
> 统一层视图（v_unified_monitor、v_unified_kg）已迁至 `dsh_java_siisdb`，本库不再包含视图

> **说明**：本文档只包含底层（base/flow/report/metric）的表结构，全部采用 `onto_` 前缀。
> SIIS 项目层表（`gwy_` 前缀）见 `dsh-ontolab-siis/docs/DB-DESIGN.md`。
> 方案 A + V1.0.7 后本库业务表全部以 `onto_` 开头：
> 8 类业务实体归并为 `onto_ontology_entity` + `onto_ontology_attribute`（EAV），原 8 张 `gwy_ontology_*` 与 2 张 `gwy_display_*` 已删除；
> 4 张项目分析表由 `gwy_` 改名为 `onto_track_article` / `onto_track_kg_node` / `onto_track_kg_edge` / `onto_analyze_track_topic`；
> `onto_ontology_relation` 无业务引用，已于 V1.0.6 删除。

---

## 命名规范

| 前缀 | 含义 | 所属层 | 模块 | tenant_id | 表数 |
|------|------|--------|------|-----------|------|
| `onto_sys_` | 认证授权 | flow | sys | NULL | 4 |
| `onto_common_` | 知识沉淀（个人层） | flow | common | 必填 | 6 |
| `onto_notify_` | 通知订阅与消息（个人层） | flow | notify | 必填 | 2 |
| `onto_global_` | 全局汇聚（共享层） | flow | global | NULL | 5 |
| `onto_report_` | 报告+问数+分析+发布 | report | report | 必填 | 12 |
| `onto_metric_` | 指标建模与依赖树 | metric | metric | 必填 | 5 |
| `onto_ontology_` | 本体元数据（多业务方向，EAV） | base | ontology | NULL | 8 |
| `onto_track_` | 信息监测（项目分析，个人层） | flow | track | 必填 | 3 |
| `onto_analyze_` | 信息分析主题（个人层） | flow | analyze | 必填 | 1 |

- **已建表**：46 张（sys 4 + common 6 + notify 2 + global 5 + report 12 + metric 5 + ontology 8 + track 3 + analyze 1）
- **设计中**：0 张（原 `onto_ingestion_*` 6 张未落地，按实际库现状不计入）
- **合计**：46 张（与 `docs/dev-ops/dsh_java_ontodb.sql` 一致）

---

## 公共字段

所有表均包含以下公共字段（统一采用小写蛇形命名）：

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT PK AUTO_INCREMENT | 主键 |
| `tenant_id` | BIGINT | 租户ID（共享层为 NULL，个人层必填） |
| `created_by` | VARCHAR(50) | 创建者 |
| `created_at` | DATETIME | 创建时间 |
| `updated_by` | VARCHAR(50) | 更新者 |
| `updated_at` | DATETIME | 更新时间 |
| `del_flag` | TINYINT DEFAULT 0 | 删除标志（0=正常 1=删除） |
| `remark` | VARCHAR(800) | 备注 |

---

## 一、认证授权 onto_sys_（4 张，flow/sys）

### 1. onto_sys_user（用户表）

| 字段 | 类型 | 说明 |
|------|------|------|
| user_id | BIGINT PK | 用户ID |
| tenant_id | BIGINT | 租户ID（主任可为NULL） |
| role_level | TINYINT DEFAULT 2 | 角色层级：0=主任/1=研究员/2=访客 |
| user_name | VARCHAR(50) UNIQUE | 用户名 |
| nick_name | VARCHAR(50) | 昵称 |
| email | VARCHAR(50) | 邮箱 |
| phonenumber | VARCHAR(50) | 手机号 |
| password | VARCHAR(200) | 密码（BCrypt加密） |
| status | VARCHAR(1) DEFAULT '0' | 状态：0=正常 1=停用 |
| login_ip | VARCHAR(50) | 最后登录IP |
| login_date | DATETIME | 最后登录时间 |
| + 公共字段 | | del_flag/created_by/created_at/updated_by/updated_at/remark |

### 2. onto_sys_audit_log（审计日志表）

> P1-7 新增。AOP 切面自动记录所有 @AuditLog 标注的接口调用。

| 字段 | 类型 | 说明 |
|------|------|------|
| log_id | BIGINT PK | 日志ID |
| user_id | BIGINT | 操作用户ID |
| tenant_id | BIGINT | 租户ID |
| role_level | TINYINT | 用户角色层级 |
| action | VARCHAR(50) | 操作类型：CREATE/UPDATE/DELETE/LOGIN/LOGOUT/OTHER |
| target_type | VARCHAR(100) | 操作目标类型（实体名/表名） |
| target_id | VARCHAR(100) | 操作目标ID |
| method | VARCHAR(200) | 执行方法（类.方法） |
| request_uri | VARCHAR(500) | 请求URI |
| request_params | TEXT | 请求参数（JSON） |
| result_status | VARCHAR(20) | 结果状态：SUCCESS/FAILED |
| error_msg | TEXT | 错误信息 |
| execution_ms | BIGINT | 执行耗时（毫秒） |
| client_ip | VARCHAR(50) | 客户端IP |
| created_at | DATETIME | 记录时间 |

### 3. onto_sys_role（角色表）

> P1-8 新增。三级角色管理：主任(0)/研究员(1)/访客(2)。

| 字段 | 类型 | 说明 |
|------|------|------|
| role_id | BIGINT PK | 角色ID |
| role_code | VARCHAR(50) UNIQUE | 角色代码 |
| role_name | VARCHAR(50) | 角色名称 |
| role_level | TINYINT | 角色层级：0=主任/1=研究员/2=访客 |
| description | VARCHAR(200) | 角色描述 |
| status | VARCHAR(1) DEFAULT '0' | 状态：0=正常 1=停用 |
| + 公共字段 | | del_flag/created_by/created_at/updated_by/updated_at/remark |

### 4. onto_sys_user_role（用户-角色关联表）

> P1-8 新增。用户与角色的多对多关联。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| user_id | BIGINT | 用户ID |
| role_id | BIGINT | 角色ID |
| created_by | VARCHAR(50) | 创建者 |
| created_at | DATETIME | 创建时间 |

---

## 二、知识沉淀 onto_common_（5 张，flow/common，个人层）

### 2. onto_common_discovered_entity（发现实体表）

| 字段 | 类型 | 说明 |
|------|------|------|
| entity_name | VARCHAR(200) | 实体名称 |
| entity_type | VARCHAR(50) | 实体类型 |
| source_topic_id | BIGINT | 来源主题ID |
| first_seen_time | DATETIME | 首次发现时间 |
| mention_count | INT DEFAULT 1 | 提及次数 |
| ontology_entity_id | BIGINT | 关联本体实体ID（晋升后回填） |
| status | VARCHAR(20) DEFAULT 'DISCOVERED' | DISCOVERED/PROMOTED |
| + 公共字段 | | tenant_id 必填 |

### 3. onto_common_tenant_relation_asset（租户关系资产表）

| 字段 | 类型 | 说明 |
|------|------|------|
| subject_entity_id | BIGINT | 主体实体ID |
| object_entity_id | BIGINT | 客体实体ID |
| relation_type | VARCHAR(50) | 关系类型 |
| avg_score | FLOAT | 平均得分 |
| topic_ids | VARCHAR(500) | 关联主题ID列表 |
| last_updated | DATETIME | 最后更新时间 |
| + 公共字段 | | tenant_id 必填 |

### 4. onto_common_personal_vector_index（个人向量索引表）

| 字段 | 类型 | 说明 |
|------|------|------|
| entity_id | BIGINT | 实体ID |
| entity_type | VARCHAR(50) | 实体类型 |
| embedding | BLOB | 向量嵌入 |
| model_version | VARCHAR(50) | 模型版本 |
| + 公共字段 | | tenant_id 必填 |

### 5. onto_common_llm_chat_history（LLM对话历史表）

| 字段 | 类型 | 说明 |
|------|------|------|
| agent_name | VARCHAR(100) | Agent名称 |
| role | VARCHAR(20) | 角色：user/assistant/system |
| content | TEXT | 内容 |
| session_id | VARCHAR(100) | 会话ID |
| + 公共字段 | | tenant_id 必填 |

### 6. onto_common_file（统一文件表）`[规划]`

> 统一管理素材、文章附件、报告配图、知识库文档、导出包，支持 hash 去重和引用计数。
> 替代 `gwy_display_media` + `gwy_ontology_media` 双份存储。

| 字段 | 类型 | 说明 |
|------|------|------|
| file_name | VARCHAR(500) | 原始文件名 |
| file_path | VARCHAR(1000) | 存储路径 |
| file_type | VARCHAR(50) | 文件分类：media/document/export/attachment |
| file_size | BIGINT | 文件大小（字节） |
| mime_type | VARCHAR(100) | MIME类型 |
| ref_count | INT DEFAULT 0 | 引用计数 |
| storage_type | VARCHAR(20) DEFAULT 'LOCAL' | 存储类型：LOCAL/OSS/URL |
| + 公共字段 | | tenant_id 必填 |

<details>
<summary>DDL（点击展开）</summary>

</details>

### 7. onto_common_merge_log（实体合并日志表）

> P2-15 新增。记录实体消歧与合并操作，支持手动合并和自动名称匹配合并。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| source_entity_id | BIGINT | 被合并的源实体ID（合并后标记删除） |
| target_entity_id | BIGINT | 合并目标实体ID（保留） |
| entity_type | VARCHAR(50) | 实体类型 |
| similarity_score | FLOAT | 相似度得分（0-1） |
| merge_strategy | VARCHAR(30) | 合并策略：AUTO_NAME_MATCH/AUTO_FUZZY/MANUAL |
| operator_id | BIGINT | 操作人用户ID（自动合并为NULL） |
| + 公共字段 | | tenant_id 必填 |

---

## 二补、通知 onto_notify_（2 张，flow/notify，个人层）

### 8. onto_notify_subscription（通知订阅表）

> P1-11 新增。用户订阅告警/主题/报告的通知，支持多渠道（EMAIL/SMS/WEBHOOK/IN_APP）。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| subscriber_id | BIGINT | 订阅人用户ID |
| target_type | VARCHAR(50) | 订阅目标类型：alert/topic/report |
| target_id | BIGINT | 订阅目标ID |
| channel | VARCHAR(20) DEFAULT 'EMAIL' | 通知渠道：EMAIL/SMS/WEBHOOK/IN_APP |
| enabled | TINYINT DEFAULT 1 | 是否启用：0=否 1=是 |
| + 公共字段 | | tenant_id 必填 |

### 9. onto_notify_message（通知消息表）

> P1-11 新增。通知消息发送记录，关联订阅。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| subscription_id | BIGINT | 关联订阅ID |
| recipient_id | BIGINT | 收件人用户ID |
| title | VARCHAR(200) | 消息标题 |
| content | TEXT | 消息内容 |
| channel | VARCHAR(20) DEFAULT 'EMAIL' | 通知渠道 |
| status | VARCHAR(20) DEFAULT 'PENDING' | 状态：PENDING/SENT/READ/FAILED |
| sent_time | DATETIME | 发送时间 |
| + 公共字段 | | tenant_id 必填 |

---

## 三、全局汇聚 onto_global_（5 张，flow/global，共享层）

### 6. onto_global_discovered_entity（全局发现实体表）

| 字段 | 类型 | 说明 |
|------|------|------|
| entity_name | VARCHAR(200) | 实体名称 |
| entity_type | VARCHAR(50) | 实体类型 |
| tenant_ids | VARCHAR(1000) | 来源租户ID列表JSON |
| total_mention_count | INT DEFAULT 0 | 总提及次数 |
| first_seen_time | DATETIME | 首次发现时间 |
| last_seen_time | DATETIME | 最后发现时间 |
| + 公共字段 | | tenant_id NULL，UNIQUE(entity_name, entity_type) |

### 7. onto_global_bilateral_relation（全局双边关系表）

| 字段 | 类型 | 说明 |
|------|------|------|
| subject_entity_name | VARCHAR(200) | 主体实体名 |
| object_entity_name | VARCHAR(200) | 客体实体名 |
| relation_type | VARCHAR(50) | 关系类型 |
| tenant_ids | VARCHAR(1000) | 来源租户ID列表JSON |
| avg_score | FLOAT | 平均得分 |
| sample_count | INT DEFAULT 0 | 样本数 |
| + 公共字段 | | UNIQUE(subject, object, relation_type) |

### 8. onto_global_kg_node（全局知识图谱节点表）

| 字段 | 类型 | 说明 |
|------|------|------|
| ontology_entity_id | BIGINT | 关联本体实体ID |
| node_name | VARCHAR(200) | 节点名称 |
| node_type | VARCHAR(50) | 节点类型 |
| tenant_ids | VARCHAR(1000) | 来源租户ID列表JSON |
| cross_tenant_weight | FLOAT DEFAULT 1.0 | 跨租户权重 |
| + 公共字段 | | UNIQUE(node_name, node_type) |

### 9. onto_global_kg_edge（全局知识图谱边表）

| 字段 | 类型 | 说明 |
|------|------|------|
| source_node_id | VARCHAR(100) | 源节点ID |
| target_node_id | VARCHAR(100) | 目标节点ID |
| relation_type | VARCHAR(100) | 关系类型 |
| tenant_ids | VARCHAR(1000) | 来源租户ID列表JSON |
| cross_tenant_weight | FLOAT DEFAULT 1.0 | 跨租户权重 |
| + 公共字段 | | UNIQUE(source, target, relation_type) |

### 10. onto_global_aggregation_log（全局汇聚日志表）

| 字段 | 类型 | 说明 |
|------|------|------|
| aggregation_type | VARCHAR(20) | 汇聚类型：FULL/INCREMENTAL |
| trigger_type | VARCHAR(20) | 触发类型：SCHEDULED/EVENT/MANUAL |
| entity_count | INT DEFAULT 0 | 实体数量 |
| relation_count | INT DEFAULT 0 | 关系数量 |
| duration_ms | BIGINT DEFAULT 0 | 耗时（毫秒） |
| status | VARCHAR(20) DEFAULT 'running' | running/success/failed |
| + 公共字段 | | |

<details>
<summary>DDL（点击展开）</summary>

</details>

---

## 四、通用数据摄取 onto_ingestion_（6 张，flow/ingestion，设计中）

> 通用数据源管理、MCP 协议接入、数据导入/导出。SIIS(trace) 复用此层能力，扩展差异化爬取执行。

### 11. onto_ingestion_source（数据源管理表）

| 字段 | 类型 | 说明 |
|------|------|------|
| source_name | VARCHAR(200) | 数据源名称 |
| source_type | VARCHAR(50) | 类型：WEB/API/DB/FILE/MCP/RSS |
| source_url | VARCHAR(500) | 数据源URL |
| auth_config | TEXT | 认证配置（JSON） |
| schedule_cron | VARCHAR(100) | 定时调度CRON表达式 |
| status | VARCHAR(20) DEFAULT 'ACTIVE' | ACTIVE/PAUSED/ERROR |
| + 公共字段 | | tenant_id 必填 |

### 12. onto_ingestion_mcp_service（MCP 服务表）

| 字段 | 类型 | 说明 |
|------|------|------|
| service_name | VARCHAR(200) | MCP服务名称 |
| service_url | VARCHAR(500) | MCP服务URL |
| protocol_version | VARCHAR(20) | MCP协议版本 |
| capabilities | TEXT | 能力声明（JSON） |
| status | VARCHAR(20) DEFAULT 'ACTIVE' | ACTIVE/PAUSED/ERROR |
| + 公共字段 | | tenant_id NULL（共享） |

### 13. onto_ingestion_mcp_tool（MCP 工具表）

| 字段 | 类型 | 说明 |
|------|------|------|
| service_id | BIGINT | 关联MCP服务ID |
| tool_name | VARCHAR(200) | 工具名称 |
| description | VARCHAR(800) | 工具描述 |
| input_schema | TEXT | 输入参数Schema（JSON） |
| output_schema | TEXT | 输出结果Schema（JSON） |
| + 公共字段 | | |

### 14. onto_ingestion_data_import（数据导入记录表）

| 字段 | 类型 | 说明 |
|------|------|------|
| source_id | BIGINT | 关联数据源ID |
| import_type | VARCHAR(50) | 导入类型：FILE/API/DB/MCP |
| file_path | VARCHAR(500) | 文件路径 |
| format | VARCHAR(20) | 格式：JSON/CSV/XML/EXCEL |
| total_count | INT DEFAULT 0 | 总记录数 |
| success_count | INT DEFAULT 0 | 成功数 |
| fail_count | INT DEFAULT 0 | 失败数 |
| status | VARCHAR(20) DEFAULT 'PENDING' | PENDING/RUNNING/SUCCESS/FAILED |
| + 公共字段 | | tenant_id 必填 |

### 15. onto_ingestion_data_export（数据导出记录表）

| 字段 | 类型 | 说明 |
|------|------|------|
| export_type | VARCHAR(50) | 导出类型：FILE/API/DB |
| query_config | TEXT | 查询配置（JSON） |
| format | VARCHAR(20) | 格式：JSON/CSV/XML/EXCEL |
| file_path | VARCHAR(500) | 导出文件路径 |
| total_count | INT DEFAULT 0 | 总记录数 |
| status | VARCHAR(20) DEFAULT 'PENDING' | PENDING/RUNNING/SUCCESS/FAILED |
| + 公共字段 | | tenant_id 必填 |

### 16. onto_ingestion_record（导入导出通用记录表）

| 字段 | 类型 | 说明 |
|------|------|------|
| ref_type | VARCHAR(20) | 关联类型：IMPORT/EXPORT |
| ref_id | BIGINT | 关联记录ID |
| record_data | TEXT | 记录数据（JSON） |
| error_message | TEXT | 错误信息 |
| + 公共字段 | | tenant_id 必填 |

<details>
<summary>DDL（点击展开）</summary>

</details>

---

## 五、报告+问数+分析 onto_report_（12 张，report）

> 除下述 7 张基础表外，V1.0.3 报告扩展表 5 张：`onto_report_alert_record`（告警记录）、
> `onto_report_information_publish`（信息发布）、`onto_report_push_record`（推送记录）、
> `onto_report_recommendation_record`（推荐记录）、`onto_report_search_index`（检索索引）。

### 17. onto_report_template（报告模板表）

| 字段 | 类型 | 说明 |
|------|------|------|
| template_name | VARCHAR(200) | 模板名称 |
| template_type | VARCHAR(50) | 类型：ANALYSIS/WEEKLY/MONTHLY/CUSTOM |
| description | VARCHAR(800) | 描述 |
| section_config | TEXT | 章节配置（JSON） |
| chart_config | TEXT | 图表配置（JSON） |
| status | TINYINT DEFAULT 0 | 0=草稿 1=发布 2=归档 |
| + 公共字段 | | |

### 18. onto_report_instance（报告实例表）

| 字段 | 类型 | 说明 |
|------|------|------|
| template_id | BIGINT | 模板ID |
| report_name | VARCHAR(200) | 报告名称 |
| report_type | VARCHAR(50) | 报告类型 |
| data_source | VARCHAR(200) | 数据来源 |
| parameters | TEXT | 参数（JSON） |
| status | VARCHAR(20) DEFAULT 'DRAFT' | DRAFT/GENERATED/PUBLISHED |
| generated_time | DATETIME | 生成时间 |
| published_time | DATETIME | 发布时间 |
| legacy_report_id | BIGINT | 旧报告ID（gwy_display_rpt_report 迁移关联，ADR-004） |
| + 公共字段 | | |

### 19. onto_report_section（报告章节表）

| 字段 | 类型 | 说明 |
|------|------|------|
| instance_id | BIGINT | 报告实例ID |
| section_index | INT DEFAULT 0 | 章节序号 |
| section_title | VARCHAR(200) | 章节标题 |
| section_type | VARCHAR(50) | TEXT/CHART/TABLE/SUMMARY |
| content | TEXT | 章节内容 |
| chart_data | TEXT | 图表数据（JSON） |
| + 公共字段 | | KEY(instance_id, section_index) |

### 20. onto_report_chart_def（图表定义表）

| 字段 | 类型 | 说明 |
|------|------|------|
| chart_name | VARCHAR(200) | 图表名称 |
| chart_type | VARCHAR(50) | BAR/LINE/PIE/TABLE/HEATMAP/SCATTER |
| data_query | TEXT | 数据查询SQL |
| x_axis | VARCHAR(100) | X轴字段 |
| y_axis | VARCHAR(100) | Y轴字段 |
| series_config | TEXT | 系列配置（JSON） |
| template_id | BIGINT | 关联模板ID |
| + 公共字段 | | |

### 21. onto_report_qa_session（问数会话表）

| 字段 | 类型 | 说明 |
|------|------|------|
| session_name | VARCHAR(200) | 会话名称 |
| user_question | TEXT | 用户问题 |
| context | TEXT | 上下文（JSON） |
| status | VARCHAR(20) DEFAULT 'ACTIVE' | ACTIVE/CLOSED |
| + 公共字段 | | |

### 22. onto_report_qa_query（问数查询记录表）

| 字段 | 类型 | 说明 |
|------|------|------|
| session_id | BIGINT | 会话ID |
| query_text | TEXT | 查询文本（自然语言） |
| translated_sql | TEXT | 翻译后的SQL |
| result_data | TEXT | 结果数据（JSON） |
| chart_suggestion | VARCHAR(500) | 图表建议 |
| process_status | VARCHAR(20) DEFAULT 'PENDING' | PENDING/SUCCESS/FAILED |
| + 公共字段 | | KEY(session_id) |

### 23. onto_report_analysis_task（分析任务表）

| 字段 | 类型 | 说明 |
|------|------|------|
| task_name | VARCHAR(200) | 任务名称 |
| task_type | VARCHAR(50) | TREND/CORRELATION/COMPARISON/PREDICTION |
| data_range | VARCHAR(200) | 数据范围 |
| analysis_config | TEXT | 分析配置（JSON） |
| result_summary | TEXT | 结果摘要 |
| status | VARCHAR(20) DEFAULT 'PENDING' | PENDING/RUNNING/SUCCESS/FAILED |
| start_time | DATETIME | 开始时间 |
| end_time | DATETIME | 结束时间 |
| + 公共字段 | | |

<details>
<summary>DDL（点击展开）</summary>

</details>

---

## 六、本体元数据 onto_ontology_（8 张，base/ontology，共享层）

> v0.2.1 从 SIIS 项目层迁移至产品层 base 模块，通过 `domain` 列（siis/factory/ecommerce/city-ops）支持多业务方向隔离。
> 6 张迁移表（version/entity/term/event_type/location/i18n）+ 2 张新增表（attribute/type）。
> 原 `onto_ontology_relation` 已于 V1.0.6 删除（无引用）。
> SIIS 项目层 8 张 `gwy_ontology_*` 保留作项目层分析存量源，启动时由 `OntologyEavMigrationRunner` 幂等搬入本层 EAV。
> 方案 A 后所有本体实体统一存 `onto_ontology_entity`（`category` 区分 8 类）+ `onto_ontology_attribute`（EAV）。

### 24. onto_ontology_version（本体版本记录）

| 字段 | 类型 | 说明 |
|------|------|------|
| domain | VARCHAR(50) NOT NULL DEFAULT 'siis' | 业务方向: siis/factory/ecommerce/city-ops |
| id | BIGINT PK AUTO_INCREMENT | 主键 |
| version_code | VARCHAR(50) | 版本号 |
| version_name | VARCHAR(100) | 版本名称 |
| entity_count | INT DEFAULT 0 | 实体数量 |
| source | VARCHAR(200) | 数据来源 |
| status | TINYINT DEFAULT 0 | 状态：0=草稿 1=发布 2=归档 |
| + 公共字段 | | tenant_id NULL |

### 25. onto_ontology_entity（本体实体表，通用）

| 字段 | 类型 | 说明 |
|------|------|------|
| domain | VARCHAR(50) NOT NULL DEFAULT 'siis' | 业务方向 |
| id | BIGINT PK AUTO_INCREMENT | 主键 |
| category | VARCHAR(50) | 类别：country/organization/person/location/media/book/paper/term |
| entity_name | VARCHAR(200) | 实体名称（中文） |
| lang | VARCHAR(10) DEFAULT 'zh' | 主语种：zh/en/fr/ar/ru/es/ja/de |
| description | TEXT | 描述 |
| source | VARCHAR(200) | 来源 |
| wiki_url | VARCHAR(500) | Wiki链接 |
| status | TINYINT DEFAULT 1 | 状态 |
| + 公共字段 | | KEY(category, entity_name) |

### 26. onto_ontology_term（术语库）

| 字段 | 类型 | 说明 |
|------|------|------|
| domain | VARCHAR(50) NOT NULL DEFAULT 'siis' | 业务方向 |
| id | BIGINT PK AUTO_INCREMENT | 主键 |
| term_name | VARCHAR(200) | 术语名（中文） |
| category | VARCHAR(50) | 分类 |
| definition | TEXT | 定义 |
| source | VARCHAR(200) | 来源 |
| lang | VARCHAR(10) DEFAULT 'zh' | 语种 |
| status | TINYINT DEFAULT 1 | 状态 |
| + 公共字段 | | |

### 27. onto_ontology_event_type（事件类型库）

| 字段 | 类型 | 说明 |
|------|------|------|
| domain | VARCHAR(50) NOT NULL DEFAULT 'siis' | 业务方向 |
| id | BIGINT PK AUTO_INCREMENT | 主键 |
| event_type | VARCHAR(100) | 事件类型（中文） |
| description | TEXT | 描述 |
| parent_id | BIGINT | 父类型ID（树形结构） |
| sort_order | INT DEFAULT 0 | 排序 |
| status | TINYINT DEFAULT 1 | 状态 |
| + 公共字段 | | |

### 28. onto_ontology_location（地名信息库）

| 字段 | 类型 | 说明 |
|------|------|------|
| domain | VARCHAR(50) NOT NULL DEFAULT 'siis' | 业务方向 |
| id | BIGINT PK AUTO_INCREMENT | 主键 |
| name | VARCHAR(200) | 地名 |
| loc_type | VARCHAR(50) | 类型：country/city/region/river/mountain |
| parent_id | BIGINT | 父级ID |
| country | VARCHAR(100) | 所属国家 |
| capital | VARCHAR(100) | 首都/首府 |
| population | VARCHAR(50) | 人口 |
| area | VARCHAR(50) | 面积 |
| description | TEXT | 描述 |
| lang | VARCHAR(10) DEFAULT 'zh' | 语种 |
| status | TINYINT DEFAULT 1 | 状态 |
| + 公共字段 | | |

### 29. onto_ontology_i18n（本体多语种表）

| 字段 | 类型 | 说明 |
|------|------|------|
| domain | VARCHAR(50) NOT NULL DEFAULT 'siis' | 业务方向 |
| id | BIGINT PK AUTO_INCREMENT | 主键 |
| entity_id | BIGINT | 关联实体ID |
| category | VARCHAR(50) | 本体分类：country/organization/person/term/... |
| lang | VARCHAR(10) | 语种：en/fr/ar/ru/es/ja/de |
| name | VARCHAR(500) | 该语种名称 |
| description | TEXT | 该语种描述 |
| + 公共字段 | | UNIQUE(entity_id, category, lang) |

### 30. onto_ontology_attribute（本体动态属性扩展表，新增）

> v0.2.1 新增。支持多业务方向自定义字段扩展，无需为每个业务方向单独建表。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK AUTO_INCREMENT | 主键 |
| entity_id | BIGINT | 关联实体ID |
| attr_name | VARCHAR(100) | 属性名 |
| attr_value | TEXT | 属性值 |
| attr_type | VARCHAR(20) DEFAULT 'STRING' | 属性类型: STRING/NUMBER/DATE/JSON/BOOLEAN |
| domain | VARCHAR(50) NOT NULL DEFAULT 'siis' | 业务方向 |
| + 公共字段 | | KEY(entity_id, domain) |

### 31. onto_ontology_relation（已删除）

> V1.0.1 新增，V1.0.6 删除。全仓零引用（双边关系已由 `onto_ontology_entity(category=bilateralRelation)` + 属性 EAV 承载），当前库中不存在该表。

### 32. onto_ontology_type（本体类型元数据注册表，新增）

> v0.2.1 新增（ADR-001 路线 A）。类型元数据驱动 `OntologyRepository.TABLE_TO_ENTITY` 和 `WRITABLE_FIELDS`，新增实体类型无需改 Java 代码。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK AUTO_INCREMENT | 主键 |
| type_code | VARCHAR(64) NOT NULL UNIQUE | 类型代码（如 country/person/policy） |
| table_name | VARCHAR(128) NOT NULL | 对应物理表名 |
| display_name | VARCHAR(128) | 展示名称 |
| domain | VARCHAR(32) NOT NULL DEFAULT 'siis' | 业务方向 |
| field_schema | TEXT | 字段定义 JSON（名称/类型/可写/可搜/展示） |
| display_config | TEXT | 展示配置 JSON（列表页/详情页） |
| status | TINYINT DEFAULT 1 | 状态：0=禁用 1=启用 |
| + 公共字段 | | UNIQUE(type_code, domain) |

<details>
<summary>DDL（点击展开）</summary>

</details>

---

## 六补、指标建模 onto_metric_（5 张，metric）

> Flyway `V1.0.2__metric_tables.sql` 建表，随 `dsh-onto-metric` 模块提供指标建模与依赖树能力。

| # | 表名 | 说明 |
|---|------|------|
| 33 | onto_metric_business_object | 业务对象 |
| 34 | onto_metric_process | 业务流程 |
| 35 | onto_metric_decision_point | 决策点 |
| 36 | onto_metric_definition | 指标定义 |
| 37 | onto_metric_dependency | 指标依赖关系 |

---

## 六补二、项目分析 onto_track_ / onto_analyze_（4 张）

> 原 SIIS 项目层 4 张 `gwy_` 表，V1.0.7 改名为 `onto_` 前缀，随平台库统一管理；
> SIIS 库已由 `V1.0.9__rename_track_tables_to_onto.sql` 同步改名（`v_unified_monitor` / `v_unified_kg` 随之重建），
> 使 `dsh-onto-flow` 的共享代码（`KnowledgeAggregator` / `AggregationService` / `WorklineOrchestrator`）在单库模式下可直接查询；
> 字段明细沿用 SIIS `docs/DB-DESIGN.md` 对应章节（仅表名换前缀）。

| # | 表名 | 原表名 | 说明 |
|---|------|--------|------|
| 38 | onto_track_article | gwy_track_article | 监测文章 |
| 39 | onto_track_kg_node | gwy_track_kg_node | 个人知识图谱节点 |
| 40 | onto_track_kg_edge | gwy_track_kg_edge | 个人知识图谱边 |
| 41 | onto_analyze_track_topic | gwy_analyze_track_topic | 追踪主题 |

---

## 七、统一层视图（2 个）

### v_unified_monitor（跨租户主题监测聚合视图）

> 已迁至 SIIS 库 `dsh_java_siisdb`（V1.0.4 重构），依赖 `gwy_analyze_track_topic` + `gwy_track_article` + `gwy_display_alert_record`。

### v_unified_kg（跨租户知识图谱聚合视图）

> 已迁至 SIIS 库 `dsh_java_siisdb`；跨个人层 `gwy_track_kg_node` 与共享层 `onto_global_kg_node`。
> 平台侧对应能力改由 `AggregationService#getUnifiedKg` 应用层聚合实现。

---

## 表清单汇总

| # | 表名 | 所属层 | 模块 | 状态 |
|---|------|--------|------|------|
| 1 | onto_sys_user | flow | sys | ✅ 已建 |
| 2 | onto_sys_role | flow | sys | ✅ 已建 |
| 3 | onto_sys_user_role | flow | sys | ✅ 已建 |
| 4 | onto_sys_audit_log | flow | sys | ✅ 已建 |
| 5 | onto_common_discovered_entity | flow | common | ✅ 已建 |
| 6 | onto_common_tenant_relation_asset | flow | common | ✅ 已建 |
| 7 | onto_common_personal_vector_index | flow | common | ✅ 已建 |
| 8 | onto_common_llm_chat_history | flow | common | ✅ 已建 |
| 9 | onto_common_file | flow | common | ✅ 已建 |
| 10 | onto_common_merge_log | flow | common | ✅ 已建 |
| 11 | onto_notify_subscription | flow | notify | ✅ 已建 |
| 12 | onto_notify_message | flow | notify | ✅ 已建 |
| 13 | onto_global_discovered_entity | flow | global | ✅ 已建 |
| 14 | onto_global_bilateral_relation | flow | global | ✅ 已建 |
| 15 | onto_global_kg_node | flow | global | ✅ 已建 |
| 16 | onto_global_kg_edge | flow | global | ✅ 已建 |
| 17 | onto_global_aggregation_log | flow | global | ✅ 已建 |
| 18 | onto_report_template | report | report | ✅ 已建 |
| 19 | onto_report_instance | report | report | ✅ 已建 |
| 20 | onto_report_section | report | report | ✅ 已建 |
| 21 | onto_report_chart_def | report | report | ✅ 已建 |
| 22 | onto_report_qa_session | report | report | ✅ 已建 |
| 23 | onto_report_qa_query | report | report | ✅ 已建 |
| 24 | onto_report_analysis_task | report | report | ✅ 已建 |
| 25 | onto_report_alert_record | report | report | ✅ 已建（V1.0.3） |
| 26 | onto_report_information_publish | report | report | ✅ 已建（V1.0.3） |
| 27 | onto_report_push_record | report | report | ✅ 已建（V1.0.3） |
| 28 | onto_report_recommendation_record | report | report | ✅ 已建（V1.0.3） |
| 29 | onto_report_search_index | report | report | ✅ 已建（V1.0.3） |
| 30 | onto_metric_business_object | metric | metric | ✅ 已建（V1.0.2） |
| 31 | onto_metric_process | metric | metric | ✅ 已建（V1.0.2） |
| 32 | onto_metric_decision_point | metric | metric | ✅ 已建（V1.0.2） |
| 33 | onto_metric_definition | metric | metric | ✅ 已建（V1.0.2） |
| 34 | onto_metric_dependency | metric | metric | ✅ 已建（V1.0.2） |
| 35 | onto_ontology_version | base | ontology | ✅ 已建（迁移） |
| 36 | onto_ontology_entity | base | ontology | ✅ 已建（迁移，EAV） |
| 37 | onto_ontology_term | base | ontology | ✅ 已建（迁移） |
| 38 | onto_ontology_event_type | base | ontology | ✅ 已建（迁移） |
| 39 | onto_ontology_location | base | ontology | ✅ 已建（迁移） |
| 40 | onto_ontology_i18n | base | ontology | ✅ 已建（迁移） |
| 41 | onto_ontology_attribute | base | ontology | ✅ 已建（EAV） |
| 42 | onto_ontology_type | base | ontology | ✅ 已建（ADR-001） |
| 43 | onto_track_article | flow | track | ✅ 已建（V1.0.7 改名） |
| 44 | onto_track_kg_node | flow | track | ✅ 已建（V1.0.7 改名） |
| 45 | onto_track_kg_edge | flow | track | ✅ 已建（V1.0.7 改名） |
| 46 | onto_analyze_track_topic | flow | analyze | ✅ 已建（V1.0.7 改名） |

**合计：46 张表**（全部已建，全部 `onto_` 前缀）
> 已删除：`onto_ontology_relation`（V1.0.6）、8 张 `gwy_ontology_*` 与 2 张 `gwy_display_*`（V1.0.5 迁移后删除）。
> 设计稿未落地：`onto_ingestion_*` 6 张（不计入实际表数）。
