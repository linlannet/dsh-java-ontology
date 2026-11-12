-- =============================================================================
-- V1.0.4 清理本体库中未被使用的 GWY 遗留表（方案 B）
-- -----------------------------------------------------------------------------
-- 背景：
--   V1.0.0 从 SIIS/GWY 业务库整体搬迁了 36 张 gwy_* 表，其中 14 张仍被平台代码引用：
--     · 本体实体 8 张：gwy_ontology_country/organization/person/hotspot/media/book/paper/bilateral_relation
--       （TypeCatalog 的 dataSource/nameColumn；管理台 CRUD、匹配、晋升目标）
--     · 图谱与文章 3 张：gwy_track_kg_node、gwy_track_kg_edge、gwy_track_article
--     · 分析 1 张：gwy_analyze_track_topic
--     · 展示 2 张：gwy_display_alert_record、gwy_display_rpt_report
--   其余 22 张在 Java/XML/YAML/JSON 主代码中零引用，且当前均为 0 行，故删除。
--
-- 约束：
--   1. 只删除 dsh_java_ontodb 内的表；dsh_java_siisdb 的同名表由 SIIS 自行管理，不受影响。
--   2. 被删表无外键引用，也无视图依赖。
--   3. 不修改 V1.0.0（已应用脚本的 checksum 不可变），仅以增量迁移清理。
--   4. 回滚：从 V1.0.0__init_schema.sql 摘取对应 CREATE TABLE 重建；被删表均为 0 行，无数据损失。
-- =============================================================================

-- 分析域（6）
DROP TABLE IF EXISTS gwy_analyze_alert_rule;
DROP TABLE IF EXISTS gwy_analyze_nlp_entity;
DROP TABLE IF EXISTS gwy_analyze_nlp_sentiment;
DROP TABLE IF EXISTS gwy_analyze_nlp_triple;
DROP TABLE IF EXISTS gwy_analyze_nlp_version;
DROP TABLE IF EXISTS gwy_analyze_track_element;

-- 展示域（8）
DROP TABLE IF EXISTS gwy_display_column_manage;
DROP TABLE IF EXISTS gwy_display_conference_info;
DROP TABLE IF EXISTS gwy_display_information_publish;
DROP TABLE IF EXISTS gwy_display_push_record;
DROP TABLE IF EXISTS gwy_display_recommendation_record;
DROP TABLE IF EXISTS gwy_display_rpt_template;
DROP TABLE IF EXISTS gwy_display_search_index;
DROP TABLE IF EXISTS gwy_display_user_module_config;

-- 抓取/轨迹域（8）
DROP TABLE IF EXISTS gwy_track_crawl_keyword_group;
DROP TABLE IF EXISTS gwy_track_crawl_source;
DROP TABLE IF EXISTS gwy_track_crawl_subscription;
DROP TABLE IF EXISTS gwy_track_crawl_task;
DROP TABLE IF EXISTS gwy_track_data_export;
DROP TABLE IF EXISTS gwy_track_data_import;
DROP TABLE IF EXISTS gwy_track_kg_build_log;
DROP TABLE IF EXISTS gwy_track_ner_element;
