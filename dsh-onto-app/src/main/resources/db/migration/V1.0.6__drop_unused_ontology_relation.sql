-- ============================================================
-- 方案 A 收尾：清理本体库中无代码引用的死表
-- onto_ontology_relation 自 V1.0.0 建表后无任何代码 / SQL 引用
-- （TypeCatalog 未注册 relation 类型，仓库无 CRUD、无查询）；
-- 通用关系能力由 onto_ontology_entity(category=bilateralRelation)
-- + onto_ontology_attribute（EAV）承担，故删除该表。
-- ============================================================

DROP TABLE IF EXISTS onto_ontology_relation;
