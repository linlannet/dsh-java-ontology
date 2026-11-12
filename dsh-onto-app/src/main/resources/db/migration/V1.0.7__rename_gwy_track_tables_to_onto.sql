-- ============================================================
-- 本体库表名规范化：4 张项目数据表前缀由 gwy_ 调整为 onto_
-- gwy_track_article        → onto_track_article
-- gwy_track_kg_node        → onto_track_kg_node
-- gwy_track_kg_edge        → onto_track_kg_edge
-- gwy_analyze_track_topic  → onto_analyze_track_topic
-- 调整后 dsh_java_ontodb 内业务表全部以 onto_ 开头
-- （SIIS 库 gwy_* 项目表不受影响）
-- ============================================================

ALTER TABLE gwy_track_article RENAME TO onto_track_article;
ALTER TABLE gwy_track_kg_node RENAME TO onto_track_kg_node;
ALTER TABLE gwy_track_kg_edge RENAME TO onto_track_kg_edge;
ALTER TABLE gwy_analyze_track_topic RENAME TO onto_analyze_track_topic;
