-- ================================================================
-- dsh_java_ontodb：产品层本体库（平台 + 本体共享层）
-- 内容：46 张表（全部 onto_ 前缀）+ 种子数据（用户/角色/版本/类型）
-- 说明：
--   1) 8 类业务实体统一存于 onto_ontology_entity + onto_ontology_attribute（EAV）
--   2) 原 8 张 gwy_ontology_* 与 2 张 gwy_display_* 已由 V1.0.5 迁移后删除
--   3) 4 张项目分析表已统一为 onto_ 前缀（onto_track_article /
--      onto_track_kg_node / onto_track_kg_edge / onto_analyze_track_topic）
--   4) onto_ontology_relation 为无引用死表，V1.0.6 删除
-- 对应迁移：classpath:db/migration（V1.0.0 ~ V1.0.7）
-- 生成时间：2026-11-10
-- ================================================================

SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS `dsh_java_ontodb` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `dsh_java_ontodb`;

DROP TABLE IF EXISTS `onto_analyze_track_topic`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_analyze_track_topic` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `topic_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主题名称',
  `topic_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '主题类型',
  `hotspot_region` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关注区域',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '描述',
  `keyword_group_id` bigint DEFAULT NULL COMMENT '关键词组ID',
  `crawl_source_ids` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '采集源ID列表',
  `alert_rule_ids` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '告警规则ID列表',
  `user_id` bigint DEFAULT NULL COMMENT '创建用户ID',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'active' COMMENT 'active/paused/closed',
  `alert_enabled` tinyint DEFAULT '0' COMMENT '是否启用告警',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `created_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint DEFAULT '0',
  `remark` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_user` (`tenant_id`,`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='追踪主题表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `onto_common_discovered_entity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_common_discovered_entity` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `entity_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '实体名称',
  `entity_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '实体类型',
  `source_topic_id` bigint DEFAULT NULL COMMENT '来源主题ID',
  `first_seen_time` datetime DEFAULT NULL COMMENT '首次发现时间',
  `mention_count` int DEFAULT '1' COMMENT '提及次数',
  `ontology_entity_id` bigint DEFAULT NULL COMMENT '关联本体实体ID（晋升后回填）',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'DISCOVERED' COMMENT 'DISCOVERED/PROMOTED',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `created_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint DEFAULT '0',
  `remark` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_status` (`tenant_id`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='发现实体表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `onto_common_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_common_file` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `file_name` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '原始文件名',
  `file_path` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '存储路径',
  `file_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文件分类：media/document/export/attachment',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小（字节）',
  `mime_type` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'MIME类型',
  `ref_count` int DEFAULT '0' COMMENT '引用计数',
  `storage_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'LOCAL' COMMENT '存储类型：LOCAL/OSS/URL',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `created_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint DEFAULT '0',
  `remark` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_file_tenant` (`tenant_id`),
  KEY `idx_file_type` (`file_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='统一文件表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `onto_common_llm_chat_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_common_llm_chat_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `agent_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Agent名称',
  `role` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '角色：user/assistant/system',
  `content` text COLLATE utf8mb4_unicode_ci COMMENT '内容',
  `session_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '会话ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `created_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint DEFAULT '0',
  `remark` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='LLM对话历史表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `onto_common_merge_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_common_merge_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `source_entity_id` bigint NOT NULL COMMENT '被合并的源实体ID（合并后标记删除）',
  `target_entity_id` bigint NOT NULL COMMENT '合并目标实体ID（保留）',
  `entity_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '实体类型',
  `similarity_score` float DEFAULT NULL COMMENT '相似度得分（0-1）',
  `merge_strategy` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '合并策略：AUTO_NAME_MATCH/AUTO_FUZZY/MANUAL',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人用户ID（自动合并为NULL）',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `created_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint DEFAULT '0',
  `remark` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_target` (`tenant_id`,`target_entity_id`),
  KEY `idx_source` (`source_entity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='实体合并日志表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `onto_common_personal_vector_index`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_common_personal_vector_index` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `entity_id` bigint NOT NULL COMMENT '实体ID',
  `entity_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '实体类型',
  `embedding` blob COMMENT '向量嵌入',
  `model_version` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '模型版本',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `created_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint DEFAULT '0',
  `remark` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='个人向量索引表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `onto_common_tenant_relation_asset`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_common_tenant_relation_asset` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `subject_entity_id` bigint NOT NULL COMMENT '主体实体ID',
  `object_entity_id` bigint NOT NULL COMMENT '客体实体ID',
  `relation_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关系类型',
  `avg_score` float DEFAULT NULL COMMENT '平均得分',
  `topic_ids` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联主题ID列表',
  `last_updated` datetime DEFAULT NULL COMMENT '最后更新时间',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `created_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint DEFAULT '0',
  `remark` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租户关系资产表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `onto_global_aggregation_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_global_aggregation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `aggregation_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '汇聚类型：FULL/INCREMENTAL',
  `trigger_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '触发类型：SCHEDULED/EVENT/MANUAL',
  `entity_count` int DEFAULT '0' COMMENT '实体数量',
  `relation_count` int DEFAULT '0' COMMENT '关系数量',
  `duration_ms` bigint DEFAULT '0' COMMENT '耗时（毫秒）',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'running' COMMENT 'running/success/failed',
  `tenant_id` bigint DEFAULT NULL,
  `created_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint DEFAULT '0',
  `remark` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='全局汇聚日志表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `onto_global_bilateral_relation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_global_bilateral_relation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `subject_entity_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主体实体名',
  `object_entity_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '客体实体名',
  `relation_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关系类型',
  `tenant_ids` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '来源租户ID列表JSON',
  `avg_score` float DEFAULT NULL COMMENT '平均得分',
  `sample_count` int DEFAULT '0' COMMENT '样本数',
  `tenant_id` bigint DEFAULT NULL,
  `created_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint DEFAULT '0',
  `remark` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_subj_obj_rel` (`subject_entity_name`,`object_entity_name`,`relation_type`)
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='全局双边关系表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `onto_global_discovered_entity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_global_discovered_entity` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `entity_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '实体名称',
  `entity_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '实体类型',
  `tenant_ids` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '来源租户ID列表JSON',
  `total_mention_count` int DEFAULT '0' COMMENT '总提及次数',
  `first_seen_time` datetime DEFAULT NULL COMMENT '首次发现时间',
  `last_seen_time` datetime DEFAULT NULL COMMENT '最后发现时间',
  `tenant_id` bigint DEFAULT NULL,
  `created_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint DEFAULT '0',
  `remark` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name_type` (`entity_name`,`entity_type`)
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='全局发现实体表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `onto_global_kg_edge`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_global_kg_edge` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `source_node_id` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '源节点ID',
  `target_node_id` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '目标节点ID',
  `relation_type` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关系类型',
  `tenant_ids` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '来源租户ID列表JSON',
  `cross_tenant_weight` float DEFAULT '1' COMMENT '跨租户权重',
  `tenant_id` bigint DEFAULT NULL,
  `created_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint DEFAULT '0',
  `remark` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_edge` (`source_node_id`,`target_node_id`,`relation_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='全局知识图谱边表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `onto_global_kg_node`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_global_kg_node` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `ontology_entity_id` bigint DEFAULT NULL COMMENT '关联本体实体ID',
  `node_name` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '节点名称',
  `node_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '节点类型',
  `tenant_ids` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '来源租户ID列表JSON',
  `cross_tenant_weight` float DEFAULT '1' COMMENT '跨租户权重',
  `tenant_id` bigint DEFAULT NULL,
  `created_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint DEFAULT '0',
  `remark` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_kg_name_type` (`node_name`,`node_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='全局知识图谱节点表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `onto_metric_business_object`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_metric_business_object` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `object_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '业务对象名称',
  `object_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '业务对象编码',
  `object_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '对象类型：ENTITY/PROCESS/RESOURCE',
  `source_table` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '数据源表名',
  `description` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
  `status` tinyint DEFAULT '0' COMMENT '状态：0=草稿 1=启用 2=停用',
  `tenant_id` bigint DEFAULT NULL,
  `created_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint DEFAULT '0',
  `remark` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_object_code` (`object_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='业务对象表（被度量的实体类型）';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `onto_metric_decision_point`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_metric_decision_point` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `point_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '决策点名称',
  `process_id` bigint DEFAULT NULL COMMENT '所属流程ID',
  `node_index` int DEFAULT '0' COMMENT '节点序号',
  `decision_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '决策类型：APPROVE/REJECT/ROUTE/SCORE',
  `description` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
  `status` tinyint DEFAULT '0' COMMENT '状态：0=草稿 1=启用 2=停用',
  `tenant_id` bigint DEFAULT NULL,
  `created_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint DEFAULT '0',
  `remark` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_mdp_process` (`process_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关键决策点表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `onto_metric_definition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_metric_definition` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `metric_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '指标名称',
  `metric_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '指标编码',
  `category` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '指标类别：ATOM=原子/DERIVED=派生/COMPOSITE=复合',
  `business_object_id` bigint DEFAULT NULL COMMENT '关联业务对象ID',
  `process_id` bigint DEFAULT NULL COMMENT '关联流程ID',
  `decision_point_id` bigint DEFAULT NULL COMMENT '关联关键决策点ID',
  `formula` text COLLATE utf8mb4_unicode_ci COMMENT '计算公式（派生/复合指标用）',
  `unit` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '计量单位',
  `description` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
  `status` tinyint DEFAULT '0' COMMENT '状态：0=草稿 1=启用 2=停用',
  `tenant_id` bigint DEFAULT NULL,
  `created_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint DEFAULT '0',
  `remark` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_metric_code` (`metric_code`),
  KEY `idx_md_category` (`category`),
  KEY `idx_md_bo` (`business_object_id`),
  KEY `idx_md_process` (`process_id`),
  KEY `idx_md_decision` (`decision_point_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='指标定义表（原子/派生/复合）';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `onto_metric_dependency`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_metric_dependency` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `downstream_metric_id` bigint NOT NULL COMMENT '下游指标ID（依赖方）',
  `upstream_metric_id` bigint NOT NULL COMMENT '上游指标ID（被依赖方）',
  `dependency_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'DIRECT' COMMENT '依赖类型：DIRECT/WEIGHTED/CONDITIONAL',
  `weight` decimal(10,4) DEFAULT '1.0000' COMMENT '权重（加权依赖用）',
  `tenant_id` bigint DEFAULT NULL,
  `created_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint DEFAULT '0',
  `remark` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_mdep_down` (`downstream_metric_id`),
  KEY `idx_mdep_up` (`upstream_metric_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='指标依赖关系表（依赖树边）';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `onto_metric_process`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_metric_process` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `process_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '流程名称',
  `process_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '流程编码',
  `business_object_id` bigint DEFAULT NULL COMMENT '关联业务对象ID',
  `node_config` text COLLATE utf8mb4_unicode_ci COMMENT '节点配置（JSON）',
  `description` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
  `status` tinyint DEFAULT '0' COMMENT '状态：0=草稿 1=启用 2=停用',
  `tenant_id` bigint DEFAULT NULL,
  `created_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint DEFAULT '0',
  `remark` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_process_code` (`process_code`),
  KEY `idx_mp_bo` (`business_object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='业务流程表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `onto_notify_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_notify_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `subscription_id` bigint DEFAULT NULL COMMENT '关联订阅ID',
  `recipient_id` bigint NOT NULL COMMENT '收件人用户ID',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息标题',
  `content` text COLLATE utf8mb4_unicode_ci COMMENT '消息内容',
  `channel` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'email' COMMENT '通知渠道',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING' COMMENT '状态：PENDING/SENT/READ/FAILED',
  `sent_time` datetime DEFAULT NULL COMMENT '发送时间',
  `tenant_id` bigint DEFAULT NULL,
  `created_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_msg_recipient` (`recipient_id`),
  KEY `idx_msg_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知消息表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `onto_notify_subscription`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_notify_subscription` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `subscriber_id` bigint NOT NULL COMMENT '订阅人用户ID',
  `target_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订阅目标类型：alert/topic/report',
  `target_id` bigint NOT NULL COMMENT '订阅目标ID',
  `channel` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'email' COMMENT '通知渠道：email/SMS/WEBHOOK/IN_APP',
  `enabled` tinyint DEFAULT '1' COMMENT '是否启用：0=否 1=是',
  `tenant_id` bigint DEFAULT NULL,
  `created_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_notify_subscriber` (`subscriber_id`),
  KEY `idx_notify_target` (`target_type`,`target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知订阅表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `onto_ontology_attribute`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_ontology_attribute` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `entity_id` bigint NOT NULL COMMENT '关联实体ID',
  `attr_name` varchar(100) NOT NULL COMMENT '属性名',
  `attr_value` text COMMENT '属性值',
  `attr_type` varchar(20) NOT NULL DEFAULT 'STRING' COMMENT '属性类型: STRING/NUMBER/DATE/JSON/BOOLEAN',
  `domain` varchar(50) NOT NULL DEFAULT 'siis' COMMENT '业务方向',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID(共享层为NULL)',
  `created_by` varchar(50) DEFAULT NULL COMMENT '创建者',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(50) DEFAULT NULL COMMENT '更新者',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志(0=正常 1=删除)',
  `remark` varchar(800) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_entity` (`entity_id`,`domain`)
) ENGINE=InnoDB AUTO_INCREMENT=1839 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='本体动态属性扩展';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `onto_ontology_entity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_ontology_entity` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `domain` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'siis' COMMENT '业务方向: siis/factory/ecommerce/city-ops',
  `category` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类别：country/organization/person/location/media/book/paper/term',
  `entity_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '实体名称（中文）',
  `lang` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT 'zh' COMMENT '主语种：zh/en/fr/ar/ru/es/ja/de',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '描述',
  `source` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '来源',
  `wiki_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Wiki链接',
  `status` tinyint DEFAULT '1' COMMENT '状态',
  `tenant_id` bigint DEFAULT NULL,
  `created_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint DEFAULT '0',
  `remark` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_category_name` (`category`,`entity_name`)
) ENGINE=InnoDB AUTO_INCREMENT=412 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='本体实体表（通用）';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `onto_ontology_event_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_ontology_event_type` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `domain` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'siis' COMMENT '业务方向: siis/factory/ecommerce/city-ops',
  `event_type` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '事件类型（中文）',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '描述',
  `parent_id` bigint DEFAULT NULL COMMENT '父类型ID（树形结构）',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态',
  `tenant_id` bigint DEFAULT NULL,
  `created_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint DEFAULT '0',
  `remark` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='事件类型库';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `onto_ontology_i18n`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_ontology_i18n` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `domain` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'siis' COMMENT '业务方向: siis/factory/ecommerce/city-ops',
  `entity_id` bigint NOT NULL COMMENT '关联实体ID',
  `category` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '本体分类：country/organization/person/term/...',
  `lang` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '语种：en/fr/ar/ru/es/ja/de',
  `name` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '该语种名称',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '该语种描述',
  `created_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_entity_category_lang` (`entity_id`,`category`,`lang`),
  KEY `idx_category_lang` (`category`,`lang`)
) ENGINE=InnoDB AUTO_INCREMENT=1028 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='本体多语种翻译表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `onto_ontology_location`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_ontology_location` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `domain` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'siis' COMMENT '业务方向: siis/factory/ecommerce/city-ops',
  `name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '地名',
  `loc_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '类型：country/city/region/river/mountain',
  `parent_id` bigint DEFAULT NULL COMMENT '父级ID',
  `country` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '所属国家',
  `capital` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '首都/首府',
  `population` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '人口',
  `area` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '面积',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '描述',
  `lang` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT 'zh' COMMENT '语种',
  `status` tinyint DEFAULT '1' COMMENT '状态',
  `tenant_id` bigint DEFAULT NULL,
  `created_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint DEFAULT '0',
  `remark` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='地名信息库';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `onto_ontology_term`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_ontology_term` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `domain` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'siis' COMMENT '业务方向: siis/factory/ecommerce/city-ops',
  `term_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '术语名（中文）',
  `category` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分类',
  `definition` text COLLATE utf8mb4_unicode_ci COMMENT '定义',
  `source` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '来源',
  `lang` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT 'zh' COMMENT '语种',
  `status` tinyint DEFAULT '1' COMMENT '状态',
  `tenant_id` bigint DEFAULT NULL,
  `created_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint DEFAULT '0',
  `remark` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='术语库';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `onto_ontology_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_ontology_type` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `type_code` varchar(64) NOT NULL COMMENT '类型代码(如country/person/policy)',
  `table_name` varchar(128) NOT NULL COMMENT '对应物理表名',
  `display_name` varchar(128) DEFAULT NULL COMMENT '展示名称',
  `domain` varchar(50) NOT NULL DEFAULT 'siis' COMMENT '业务方向',
  `field_schema` text COMMENT '字段定义JSON(名称/类型/可写/可搜/展示)',
  `display_config` text COMMENT '展示配置JSON(列表页/详情页)',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0=禁用 1=启用)',
  `created_by` varchar(50) DEFAULT NULL COMMENT '创建者',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(50) DEFAULT NULL COMMENT '更新者',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志(0=正常 1=删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_type_code` (`type_code`,`domain`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='本体类型元数据注册表(ADR-001路线A)';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `onto_ontology_version`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_ontology_version` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `domain` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'siis' COMMENT '业务方向: siis/factory/ecommerce/city-ops',
  `version_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '版本号',
  `version_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '版本名称',
  `entity_count` int DEFAULT '0' COMMENT '实体数量',
  `source` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '数据来源',
  `status` tinyint DEFAULT '0' COMMENT '状态：0=草稿 1=发布 2=归档',
  `tenant_id` bigint DEFAULT NULL,
  `created_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint DEFAULT '0',
  `remark` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='本体版本记录';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `onto_report_alert_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_report_alert_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `topic_id` bigint DEFAULT NULL COMMENT '关联主题',
  `alert_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '告警名称',
  `alert_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '告警类型',
  `alert_level` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'INFO' COMMENT '级别：INFO/WARN/URGENT',
  `content` text COLLATE utf8mb4_unicode_ci COMMENT '告警内容',
  `entity_ids` text COLLATE utf8mb4_unicode_ci COMMENT '关联实体ID（JSON数组）',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'OPEN' COMMENT '状态：OPEN/ACK/CLOSED',
  `triggered_at` datetime DEFAULT NULL COMMENT '触发时间',
  `handled_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '处理人',
  `handled_at` datetime DEFAULT NULL COMMENT '处理时间',
  `created_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint DEFAULT '0',
  `remark` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_ar_tenant_topic` (`tenant_id`,`topic_id`),
  KEY `idx_ar_level_status` (`alert_level`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报告域-告警记录';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `onto_report_analysis_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_report_analysis_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `task_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务名称',
  `task_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '任务类型：TREND/CORRELATION/COMPARISON/PREDICTION',
  `data_range` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '数据范围',
  `analysis_config` text COLLATE utf8mb4_unicode_ci COMMENT '分析配置（JSON）',
  `result_summary` text COLLATE utf8mb4_unicode_ci COMMENT '结果摘要',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING' COMMENT 'PENDING/RUNNING/SUCCESS/FAILED',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `tenant_id` bigint DEFAULT NULL,
  `created_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint DEFAULT '0',
  `remark` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分析任务表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `onto_report_chart_def`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_report_chart_def` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `chart_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '图表名称',
  `chart_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '图表类型：BAR/LINE/PIE/TABLE/HEATMAP/SCATTER',
  `data_query` text COLLATE utf8mb4_unicode_ci COMMENT '数据查询SQL',
  `x_axis` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'X轴字段',
  `y_axis` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Y轴字段',
  `series_config` text COLLATE utf8mb4_unicode_ci COMMENT '系列配置（JSON）',
  `template_id` bigint DEFAULT NULL COMMENT '关联模板ID',
  `tenant_id` bigint DEFAULT NULL,
  `created_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint DEFAULT '0',
  `remark` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图表定义表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `onto_report_information_publish`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_report_information_publish` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `report_id` bigint DEFAULT NULL COMMENT '关联报告实例',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `content` text COLLATE utf8mb4_unicode_ci COMMENT '正文',
  `publish_channel` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '渠道：WEB/MAIL/APP/OTHER',
  `publish_scope` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'TENANT' COMMENT '范围：TENANT/GLOBAL',
  `publish_status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT/PUBLISHED/WITHDRAWN',
  `published_at` datetime DEFAULT NULL COMMENT '发布时间',
  `publisher_id` bigint DEFAULT NULL COMMENT '发布人',
  `created_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint DEFAULT '0',
  `remark` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_ip_tenant_status` (`tenant_id`,`publish_status`),
  KEY `idx_ip_report` (`report_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报告域-信息发布';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `onto_report_instance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_report_instance` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `template_id` bigint DEFAULT NULL COMMENT '模板ID',
  `report_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '报告名称',
  `report_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '报告类型',
  `data_source` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '数据来源',
  `parameters` text COLLATE utf8mb4_unicode_ci COMMENT '参数（JSON）',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'DRAFT' COMMENT 'DRAFT/GENERATED/PUBLISHED',
  `generated_time` datetime DEFAULT NULL COMMENT '生成时间',
  `published_time` datetime DEFAULT NULL COMMENT '发布时间',
  `legacy_report_id` bigint DEFAULT NULL COMMENT 'P2-20：旧表 gwy_display_rpt_report.id 快照引用（迁移后回填）',
  `tenant_id` bigint DEFAULT NULL,
  `created_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint DEFAULT '0',
  `remark` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_legacy` (`legacy_report_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报告实例表（P2-20 主表，gwy_display_rpt_report 废弃为快照）';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `onto_report_push_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_report_push_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `source_type` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '来源类型：REPORT/ALERT',
  `source_id` bigint NOT NULL COMMENT '来源ID',
  `channel` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '渠道：WEB/MAIL/SMS/APP',
  `recipient` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '接收者标识（脱敏存储）',
  `payload` text COLLATE utf8mb4_unicode_ci COMMENT '推送载荷',
  `delivery_status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/SENT/FAILED',
  `error_msg` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '失败原因',
  `sent_at` datetime DEFAULT NULL COMMENT '发送时间',
  `created_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint DEFAULT '0',
  `remark` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_pr_tenant_channel` (`tenant_id`,`channel`),
  KEY `idx_pr_status_time` (`delivery_status`,`created_at`),
  KEY `idx_pr_source` (`source_type`,`source_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报告域-推送记录';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `onto_report_qa_query`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_report_qa_query` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `session_id` bigint NOT NULL COMMENT '会话ID',
  `query_text` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '查询文本（自然语言）',
  `translated_sql` text COLLATE utf8mb4_unicode_ci COMMENT '翻译后的SQL',
  `result_data` text COLLATE utf8mb4_unicode_ci COMMENT '结果数据（JSON）',
  `chart_suggestion` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '图表建议',
  `process_status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING' COMMENT 'PENDING/SUCCESS/FAILED',
  `tenant_id` bigint DEFAULT NULL,
  `created_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint DEFAULT '0',
  `remark` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_session` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问数查询记录表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `onto_report_qa_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_report_qa_session` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `session_name` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '会话名称',
  `user_question` text COLLATE utf8mb4_unicode_ci COMMENT '用户问题',
  `context` text COLLATE utf8mb4_unicode_ci COMMENT '上下文（JSON）',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'ACTIVE' COMMENT 'ACTIVE/CLOSED',
  `tenant_id` bigint DEFAULT NULL,
  `created_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint DEFAULT '0',
  `remark` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问数会话表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `onto_report_recommendation_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_report_recommendation_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `target_type` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '目标类型：REPORT/TOPIC/ONTOLOGY_ENTITY',
  `target_id` bigint NOT NULL COMMENT '目标ID',
  `score` float NOT NULL DEFAULT '0' COMMENT '推荐得分',
  `reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '推荐理由',
  `algorithm` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '算法标识',
  `exposed` tinyint NOT NULL DEFAULT '0' COMMENT '是否已曝光',
  `clicked` tinyint NOT NULL DEFAULT '0' COMMENT '是否已点击',
  `created_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint DEFAULT '0',
  `remark` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_rr_tenant_user` (`tenant_id`,`user_id`),
  KEY `idx_rr_target` (`target_type`,`target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报告域-推荐记录';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `onto_report_search_index`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_report_search_index` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `index_type` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '索引类型：REPORT/ALERT/DOC',
  `target_id` bigint NOT NULL COMMENT '目标ID',
  `keywords` text COLLATE utf8mb4_unicode_ci COMMENT '关键词（分词结果）',
  `content` text COLLATE utf8mb4_unicode_ci COMMENT '索引正文',
  `weight` float NOT NULL DEFAULT '1' COMMENT '权重',
  `index_status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'VALID' COMMENT '状态：VALID/STALE/REBUILDING',
  `rebuilt_at` datetime DEFAULT NULL COMMENT '重建时间',
  `created_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint DEFAULT '0',
  `remark` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_si_index_target` (`index_type`,`target_id`),
  KEY `idx_si_tenant_type` (`tenant_id`,`index_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报告域-搜索索引';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `onto_report_section`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_report_section` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `instance_id` bigint NOT NULL COMMENT '报告实例ID',
  `section_index` int DEFAULT '0' COMMENT '章节序号',
  `section_title` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '章节标题',
  `section_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '章节类型：TEXT/CHART/TABLE/SUMMARY',
  `content` text COLLATE utf8mb4_unicode_ci COMMENT '章节内容',
  `chart_data` text COLLATE utf8mb4_unicode_ci COMMENT '图表数据（JSON）',
  `tenant_id` bigint DEFAULT NULL,
  `created_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint DEFAULT '0',
  `remark` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_instance` (`instance_id`,`section_index`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报告章节表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `onto_report_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_report_template` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `template_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模板名称',
  `template_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '模板类型：ANALYSIS/WEEKLY/MONTHLY/CUSTOM',
  `description` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
  `section_config` text COLLATE utf8mb4_unicode_ci COMMENT '章节配置（JSON）',
  `chart_config` text COLLATE utf8mb4_unicode_ci COMMENT '图表配置（JSON）',
  `status` tinyint DEFAULT '0' COMMENT '状态：0=草稿 1=发布 2=归档',
  `tenant_id` bigint DEFAULT NULL,
  `created_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint DEFAULT '0',
  `remark` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报告模板表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `onto_sys_audit_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_sys_audit_log` (
  `log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` bigint DEFAULT NULL COMMENT '操作用户ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `role_level` tinyint DEFAULT NULL COMMENT '用户角色层级',
  `action` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型：CREATE/UPDATE/DELETE/LOGIN/LOGOUT/OTHER',
  `target_type` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作目标类型（实体名/表名）',
  `target_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作目标ID',
  `method` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '执行方法（类.方法）',
  `request_uri` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '请求URI',
  `request_params` text COLLATE utf8mb4_unicode_ci COMMENT '请求参数（JSON）',
  `result_status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '结果状态：SUCCESS/FAILED',
  `error_msg` text COLLATE utf8mb4_unicode_ci COMMENT '错误信息',
  `execution_ms` bigint DEFAULT NULL COMMENT '执行耗时（毫秒）',
  `client_ip` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '客户端IP',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_audit_user_id` (`user_id`),
  KEY `idx_audit_action` (`action`),
  KEY `idx_audit_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计日志表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `onto_sys_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_sys_role` (
  `role_id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色代码',
  `role_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称',
  `role_level` tinyint NOT NULL COMMENT '角色层级：0=主任/1=研究员/2=访客',
  `description` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '角色描述',
  `status` varchar(1) COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '状态：0=正常 1=停用',
  `del_flag` varchar(1) COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '删除标志：0=正常 1=删除',
  `created_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `onto_sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_sys_user` (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID（主任可为NULL）',
  `role_level` tinyint NOT NULL DEFAULT '2' COMMENT '角色层级：0=主任/1=研究员/2=访客',
  `user_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `nick_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '昵称',
  `email` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  `phonenumber` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号',
  `password` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（加密存储）',
  `status` varchar(1) COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '状态：0=正常 1=停用',
  `del_flag` varchar(1) COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '删除标志：0=正常 1=删除',
  `login_ip` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最后登录IP',
  `login_date` datetime DEFAULT NULL COMMENT '最后登录时间',
  `created_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建者',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '更新者',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_user_name` (`user_name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `onto_sys_user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_sys_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `created_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户-角色关联表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `onto_track_article`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_track_article` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `article_hash` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文章哈希（去重）',
  `title` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标题',
  `url` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '原文URL',
  `lang` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT 'zh' COMMENT '语种',
  `published` datetime DEFAULT NULL COMMENT '发布时间',
  `source` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '来源',
  `source_id` bigint DEFAULT NULL COMMENT '采集源ID',
  `topic_id` bigint DEFAULT NULL COMMENT '关联主题ID',
  `crawl_task_id` bigint DEFAULT NULL COMMENT '采集任务ID',
  `sentiment` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '情感：positive/negative/neutral',
  `sentiment_score` float DEFAULT NULL COMMENT '情感得分',
  `nlp_status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING' COMMENT 'NLP状态：PENDING/COMPLETED',
  `content` text COLLATE utf8mb4_unicode_ci COMMENT '正文摘要',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `created_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint DEFAULT '0',
  `remark` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_hash` (`tenant_id`,`article_hash`),
  KEY `idx_tenant_topic_published` (`tenant_id`,`topic_id`,`published`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `onto_track_kg_edge`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_track_kg_edge` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `source_node_id` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '源节点ID',
  `target_node_id` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '目标节点ID',
  `relation_type` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关系类型',
  `relation_name` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关系名称',
  `properties` json DEFAULT NULL COMMENT '关系属性',
  `weight` float DEFAULT '1' COMMENT '权重',
  `article_id` bigint DEFAULT NULL COMMENT '证据文章ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `created_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint DEFAULT '0',
  `remark` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_edge` (`tenant_id`,`source_node_id`,`target_node_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='个人知识图谱边';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `onto_track_kg_node`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onto_track_kg_node` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `node_id` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '节点ID',
  `node_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '节点类型',
  `node_name` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '节点名称',
  `node_name_zh` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '节点中文名',
  `ontology_entity_id` bigint DEFAULT NULL COMMENT '关联本体实体ID',
  `properties` json DEFAULT NULL COMMENT '节点属性',
  `weight` float DEFAULT '1' COMMENT '权重',
  `source_article_ids` text COLLATE utf8mb4_unicode_ci COMMENT '来源文章ID列表JSON',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `created_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint DEFAULT '0',
  `remark` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_node` (`tenant_id`,`node_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='个人知识图谱节点';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET character_set_client = @saved_cs_client */;

-- ================================================================
-- 种子数据（用户 / 角色 / 用户角色 / 本体版本 / 本体类型）
-- 说明：REPLACE INTO，可重复执行
-- ================================================================
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

LOCK TABLES `onto_sys_user` WRITE;
/*!40000 ALTER TABLE `onto_sys_user` DISABLE KEYS */;
REPLACE INTO `onto_sys_user` VALUES (1,NULL,0,'director','主任','director@linlan.net',NULL,'$2a$10$K4FvGCaty1h/quHwYIE80e.Im.t2JW8BfI6.YELnzlbwXmSBWrRsG','0','0',NULL,NULL,'system','2026-11-09 12:11:46',NULL,'2026-11-09 12:11:46',NULL);
REPLACE INTO `onto_sys_user` VALUES (2,1,1,'researcher','研究员','researcher@linlan.net',NULL,'$2a$10$K4FvGCaty1h/quHwYIE80e.Im.t2JW8BfI6.YELnzlbwXmSBWrRsG','0','0',NULL,NULL,'system','2026-11-09 12:11:46',NULL,'2026-11-09 12:11:46',NULL);
REPLACE INTO `onto_sys_user` VALUES (3,1,2,'guest','访客','guest@linlan.net',NULL,'$2a$10$K4FvGCaty1h/quHwYIE80e.Im.t2JW8BfI6.YELnzlbwXmSBWrRsG','0','0',NULL,NULL,'system','2026-11-09 12:11:46',NULL,'2026-11-09 12:11:46',NULL);
/*!40000 ALTER TABLE `onto_sys_user` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `onto_sys_role` WRITE;
/*!40000 ALTER TABLE `onto_sys_role` DISABLE KEYS */;
REPLACE INTO `onto_sys_role` VALUES (1,'DIRECTOR','主任',0,'全平台最高权限，跨租户管理','0','0','system','2026-11-09 12:11:46',NULL,'2026-11-09 12:11:46',NULL);
REPLACE INTO `onto_sys_role` VALUES (2,'RESEARCHER','研究员',1,'租户数据读写+共享层只读','0','0','system','2026-11-09 12:11:46',NULL,'2026-11-09 12:11:46',NULL);
REPLACE INTO `onto_sys_role` VALUES (3,'GUEST','访客',2,'共享层只读','0','0','system','2026-11-09 12:11:46',NULL,'2026-11-09 12:11:46',NULL);
/*!40000 ALTER TABLE `onto_sys_role` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `onto_sys_user_role` WRITE;
/*!40000 ALTER TABLE `onto_sys_user_role` DISABLE KEYS */;
REPLACE INTO `onto_sys_user_role` VALUES (1,1,1,'system','2026-11-09 12:11:46');
REPLACE INTO `onto_sys_user_role` VALUES (2,2,2,'system','2026-11-09 12:11:46');
REPLACE INTO `onto_sys_user_role` VALUES (3,3,3,'system','2026-11-09 12:11:46');
/*!40000 ALTER TABLE `onto_sys_user_role` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `onto_ontology_version` WRITE;
/*!40000 ALTER TABLE `onto_ontology_version` DISABLE KEYS */;
REPLACE INTO `onto_ontology_version` VALUES (1,'siis','v2.0.0','本体知识库 v2.0 初始版本',0,'system',1,NULL,'system','2026-11-09 12:11:46',NULL,'2026-11-09 12:11:46',0,NULL);
/*!40000 ALTER TABLE `onto_ontology_version` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `onto_ontology_type` WRITE;
/*!40000 ALTER TABLE `onto_ontology_type` DISABLE KEYS */;
REPLACE INTO `onto_ontology_type` VALUES (1,'country','onto_ontology_entity','国家','siis',NULL,NULL,1,'system','2026-11-09 12:11:46',NULL,'2026-11-09 23:44:59',0);
REPLACE INTO `onto_ontology_type` VALUES (2,'organization','onto_ontology_entity','组织机构','siis',NULL,NULL,1,'system','2026-11-09 12:11:46',NULL,'2026-11-09 23:44:59',0);
REPLACE INTO `onto_ontology_type` VALUES (3,'person','onto_ontology_entity','人物','siis',NULL,NULL,1,'system','2026-11-09 12:11:46',NULL,'2026-11-09 23:44:59',0);
REPLACE INTO `onto_ontology_type` VALUES (4,'hotspot','onto_ontology_entity','热点事件','siis',NULL,NULL,1,'system','2026-11-09 12:11:46',NULL,'2026-11-09 23:44:59',0);
REPLACE INTO `onto_ontology_type` VALUES (5,'term','onto_ontology_term','术语','siis',NULL,NULL,1,'system','2026-11-09 12:11:46',NULL,'2026-11-09 12:11:46',0);
REPLACE INTO `onto_ontology_type` VALUES (6,'eventType','onto_ontology_event_type','事件类型','siis',NULL,NULL,1,'system','2026-11-09 12:11:46',NULL,'2026-11-09 12:11:46',0);
REPLACE INTO `onto_ontology_type` VALUES (7,'location','onto_ontology_location','地名','siis',NULL,NULL,1,'system','2026-11-09 12:11:46',NULL,'2026-11-09 12:11:46',0);
REPLACE INTO `onto_ontology_type` VALUES (8,'media','onto_ontology_entity','媒体','siis',NULL,NULL,1,'system','2026-11-09 12:11:46',NULL,'2026-11-09 23:44:59',0);
REPLACE INTO `onto_ontology_type` VALUES (9,'book','onto_ontology_entity','书籍','siis',NULL,NULL,1,'system','2026-11-09 12:11:46',NULL,'2026-11-09 23:44:59',0);
REPLACE INTO `onto_ontology_type` VALUES (10,'paper','onto_ontology_entity','论文','siis',NULL,NULL,1,'system','2026-11-09 12:11:46',NULL,'2026-11-09 23:44:59',0);
REPLACE INTO `onto_ontology_type` VALUES (11,'entity','onto_ontology_entity','通用实体','siis',NULL,NULL,1,'system','2026-11-09 12:11:46',NULL,'2026-11-09 12:11:46',0);
REPLACE INTO `onto_ontology_type` VALUES (12,'version','onto_ontology_version','本体版本','siis',NULL,NULL,1,'system','2026-11-09 12:11:46',NULL,'2026-11-09 12:11:46',0);
REPLACE INTO `onto_ontology_type` VALUES (13,'bilateralRelation','onto_ontology_entity','双边关系','siis',NULL,NULL,1,'system','2026-11-09 12:11:46',NULL,'2026-11-09 23:44:59',0);
REPLACE INTO `onto_ontology_type` VALUES (14,'i18n','onto_ontology_i18n','多语种翻译','siis',NULL,NULL,1,'system','2026-11-09 12:11:46',NULL,'2026-11-09 12:11:46',0);
/*!40000 ALTER TABLE `onto_ontology_type` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

