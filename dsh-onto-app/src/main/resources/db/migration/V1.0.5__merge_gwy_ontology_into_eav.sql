-- 方案 A：8 类业务实体由物理分表归并到 onto_ontology_entity + onto_ontology_attribute（EAV）。
-- 1) 类型注册表指向统一实体表；
-- 2) 将 ontodb 内历史行（如有）迁入 EAV；
-- 3) 删除 8 张 gwy_ontology_* 与 2 张废弃的 gwy_display_* 表。
-- 说明：产品库中这 10 张表为历史遗留，业务数据实际在 SIIS 库，由 SIIS 侧迁移脚本搬迁。

UPDATE onto_ontology_type
SET table_name = 'onto_ontology_entity', updated_at = NOW()
WHERE type_code IN ('country', 'organization', 'person', 'hotspot', 'media', 'book', 'paper', 'bilateralRelation')
  AND table_name LIKE 'gwy_ontology_%';

-- 临时映射列：把旧表主键映射到新实体行，保证属性迁移可精确关联
ALTER TABLE onto_ontology_entity ADD COLUMN legacy_ref VARCHAR(100) NULL;

-- country
INSERT INTO onto_ontology_entity (domain, category, entity_name, lang, status, tenant_id, created_by, created_at, updated_by, updated_at, del_flag, remark, legacy_ref)
SELECT 'siis', 'country', name, 'zh', status, tenant_id, created_by, created_at, updated_by, updated_at, del_flag, remark, CONCAT('country:', id)
FROM gwy_ontology_country WHERE del_flag = 0;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'iso_code', c.iso_code, 'STRING', 'siis', c.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_country c JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('country:', c.id) WHERE c.iso_code IS NOT NULL;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'continent', c.continent, 'STRING', 'siis', c.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_country c JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('country:', c.id) WHERE c.continent IS NOT NULL;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'capital', c.capital, 'STRING', 'siis', c.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_country c JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('country:', c.id) WHERE c.capital IS NOT NULL;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'language', c.language, 'STRING', 'siis', c.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_country c JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('country:', c.id) WHERE c.language IS NOT NULL;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'currency', c.currency, 'STRING', 'siis', c.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_country c JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('country:', c.id) WHERE c.currency IS NOT NULL;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'population', c.population, 'STRING', 'siis', c.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_country c JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('country:', c.id) WHERE c.population IS NOT NULL;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'area', c.area, 'STRING', 'siis', c.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_country c JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('country:', c.id) WHERE c.area IS NOT NULL;

-- organization
INSERT INTO onto_ontology_entity (domain, category, entity_name, lang, description, status, tenant_id, created_by, created_at, updated_by, updated_at, del_flag, remark, legacy_ref)
SELECT 'siis', 'organization', name, lang, description, status, tenant_id, created_by, created_at, updated_by, updated_at, del_flag, remark, CONCAT('organization:', id)
FROM gwy_ontology_organization WHERE del_flag = 0;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'org_type', o.org_type, 'STRING', 'siis', o.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_organization o JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('organization:', o.id) WHERE o.org_type IS NOT NULL;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'country', o.country, 'STRING', 'siis', o.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_organization o JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('organization:', o.id) WHERE o.country IS NOT NULL;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'website', o.website, 'STRING', 'siis', o.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_organization o JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('organization:', o.id) WHERE o.website IS NOT NULL;

-- person
INSERT INTO onto_ontology_entity (domain, category, entity_name, lang, description, status, tenant_id, created_by, created_at, updated_by, updated_at, del_flag, remark, legacy_ref)
SELECT 'siis', 'person', name, lang, bio, status, tenant_id, created_by, created_at, updated_by, updated_at, del_flag, remark, CONCAT('person:', id)
FROM gwy_ontology_person WHERE del_flag = 0;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'gender', p.gender, 'STRING', 'siis', p.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_person p JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('person:', p.id) WHERE p.gender IS NOT NULL;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'birth_date', p.birth_date, 'STRING', 'siis', p.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_person p JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('person:', p.id) WHERE p.birth_date IS NOT NULL;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'country', p.country, 'STRING', 'siis', p.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_person p JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('person:', p.id) WHERE p.country IS NOT NULL;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'occupation', p.occupation, 'STRING', 'siis', p.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_person p JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('person:', p.id) WHERE p.occupation IS NOT NULL;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'title', p.title, 'STRING', 'siis', p.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_person p JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('person:', p.id) WHERE p.title IS NOT NULL;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'affiliation', p.affiliation, 'STRING', 'siis', p.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_person p JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('person:', p.id) WHERE p.affiliation IS NOT NULL;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'portrait_url', p.portrait_url, 'STRING', 'siis', p.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_person p JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('person:', p.id) WHERE p.portrait_url IS NOT NULL;

-- hotspot
INSERT INTO onto_ontology_entity (domain, category, entity_name, lang, status, tenant_id, created_by, created_at, updated_by, updated_at, del_flag, remark, legacy_ref)
SELECT 'siis', 'hotspot', hotspot_name, 'zh', status, tenant_id, created_by, created_at, updated_by, updated_at, del_flag, remark, CONCAT('hotspot:', id)
FROM gwy_ontology_hotspot WHERE del_flag = 0;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'hotspot_type', h.hotspot_type, 'STRING', 'siis', h.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_hotspot h JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('hotspot:', h.id) WHERE h.hotspot_type IS NOT NULL;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'keywords', h.keywords, 'STRING', 'siis', h.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_hotspot h JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('hotspot:', h.id) WHERE h.keywords IS NOT NULL;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'related_countries', h.related_countries, 'STRING', 'siis', h.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_hotspot h JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('hotspot:', h.id) WHERE h.related_countries IS NOT NULL;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'start_time', h.start_time, 'STRING', 'siis', h.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_hotspot h JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('hotspot:', h.id) WHERE h.start_time IS NOT NULL;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'end_time', h.end_time, 'STRING', 'siis', h.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_hotspot h JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('hotspot:', h.id) WHERE h.end_time IS NOT NULL;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'heat_score', h.heat_score, 'NUMBER', 'siis', h.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_hotspot h JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('hotspot:', h.id) WHERE h.heat_score IS NOT NULL;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'risk_level', h.risk_level, 'STRING', 'siis', h.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_hotspot h JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('hotspot:', h.id) WHERE h.risk_level IS NOT NULL;

-- media
INSERT INTO onto_ontology_entity (domain, category, entity_name, lang, description, status, tenant_id, created_by, created_at, updated_by, updated_at, del_flag, remark, legacy_ref)
SELECT 'siis', 'media', name, lang, description, status, tenant_id, created_by, created_at, updated_by, updated_at, del_flag, remark, CONCAT('media:', id)
FROM gwy_ontology_media WHERE del_flag = 0;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'media_type', m.media_type, 'STRING', 'siis', m.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_media m JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('media:', m.id) WHERE m.media_type IS NOT NULL;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'country', m.country, 'STRING', 'siis', m.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_media m JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('media:', m.id) WHERE m.country IS NOT NULL;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'language', m.language, 'STRING', 'siis', m.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_media m JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('media:', m.id) WHERE m.language IS NOT NULL;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'website', m.website, 'STRING', 'siis', m.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_media m JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('media:', m.id) WHERE m.website IS NOT NULL;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'audience', m.audience, 'STRING', 'siis', m.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_media m JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('media:', m.id) WHERE m.audience IS NOT NULL;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'bias_rating', m.bias_rating, 'STRING', 'siis', m.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_media m JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('media:', m.id) WHERE m.bias_rating IS NOT NULL;

-- book
INSERT INTO onto_ontology_entity (domain, category, entity_name, lang, description, status, tenant_id, created_by, created_at, updated_by, updated_at, del_flag, remark, legacy_ref)
SELECT 'siis', 'book', title, lang, summary, status, tenant_id, created_by, created_at, updated_by, updated_at, del_flag, remark, CONCAT('book:', id)
FROM gwy_ontology_book WHERE del_flag = 0;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'author', b.author, 'STRING', 'siis', b.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_book b JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('book:', b.id) WHERE b.author IS NOT NULL;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'publisher', b.publisher, 'STRING', 'siis', b.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_book b JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('book:', b.id) WHERE b.publisher IS NOT NULL;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'publish_year', b.publish_year, 'STRING', 'siis', b.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_book b JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('book:', b.id) WHERE b.publish_year IS NOT NULL;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'isbn', b.isbn, 'STRING', 'siis', b.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_book b JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('book:', b.id) WHERE b.isbn IS NOT NULL;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'pages', b.pages, 'NUMBER', 'siis', b.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_book b JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('book:', b.id) WHERE b.pages IS NOT NULL;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'category', b.category, 'STRING', 'siis', b.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_book b JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('book:', b.id) WHERE b.category IS NOT NULL;

-- paper
INSERT INTO onto_ontology_entity (domain, category, entity_name, lang, description, status, tenant_id, created_by, created_at, updated_by, updated_at, del_flag, remark, legacy_ref)
SELECT 'siis', 'paper', title, lang, summary, status, tenant_id, created_by, created_at, updated_by, updated_at, del_flag, remark, CONCAT('paper:', id)
FROM gwy_ontology_paper WHERE del_flag = 0;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'author', p.author, 'STRING', 'siis', p.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_paper p JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('paper:', p.id) WHERE p.author IS NOT NULL;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'institution', p.institution, 'STRING', 'siis', p.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_paper p JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('paper:', p.id) WHERE p.institution IS NOT NULL;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'journal', p.journal, 'STRING', 'siis', p.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_paper p JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('paper:', p.id) WHERE p.journal IS NOT NULL;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'publish_year', p.publish_year, 'STRING', 'siis', p.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_paper p JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('paper:', p.id) WHERE p.publish_year IS NOT NULL;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'doi', p.doi, 'STRING', 'siis', p.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_paper p JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('paper:', p.id) WHERE p.doi IS NOT NULL;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'keywords', p.keywords, 'STRING', 'siis', p.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_paper p JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('paper:', p.id) WHERE p.keywords IS NOT NULL;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'category', p.category, 'STRING', 'siis', p.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_paper p JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('paper:', p.id) WHERE p.category IS NOT NULL;

-- bilateralRelation
INSERT INTO onto_ontology_entity (domain, category, entity_name, lang, description, status, tenant_id, created_by, created_at, updated_by, updated_at, del_flag, remark, legacy_ref)
SELECT 'siis', 'bilateralRelation', CONCAT(country_a, ' - ', country_b), 'zh', description, status, tenant_id, created_by, created_at, updated_by, updated_at, del_flag, remark, CONCAT('bilateralRelation:', id)
FROM gwy_ontology_bilateral_relation WHERE del_flag = 0;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'country_a', r.country_a, 'STRING', 'siis', r.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_bilateral_relation r JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('bilateralRelation:', r.id);
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'country_b', r.country_b, 'STRING', 'siis', r.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_bilateral_relation r JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('bilateralRelation:', r.id);
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'relation_type', r.relation_type, 'STRING', 'siis', r.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_bilateral_relation r JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('bilateralRelation:', r.id) WHERE r.relation_type IS NOT NULL;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'relation_level', r.relation_level, 'STRING', 'siis', r.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_bilateral_relation r JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('bilateralRelation:', r.id) WHERE r.relation_level IS NOT NULL;
INSERT INTO onto_ontology_attribute (entity_id, attr_name, attr_value, attr_type, domain, tenant_id, created_at, updated_at, del_flag)
SELECT e.id, 'start_year', r.start_year, 'STRING', 'siis', r.tenant_id, NOW(), NOW(), 0
FROM gwy_ontology_bilateral_relation r JOIN onto_ontology_entity e ON e.legacy_ref = CONCAT('bilateralRelation:', r.id) WHERE r.start_year IS NOT NULL;

ALTER TABLE onto_ontology_entity DROP COLUMN legacy_ref;

-- 删除历史物理分表与废弃报表/告警表
DROP TABLE IF EXISTS gwy_ontology_country;
DROP TABLE IF EXISTS gwy_ontology_organization;
DROP TABLE IF EXISTS gwy_ontology_person;
DROP TABLE IF EXISTS gwy_ontology_hotspot;
DROP TABLE IF EXISTS gwy_ontology_media;
DROP TABLE IF EXISTS gwy_ontology_book;
DROP TABLE IF EXISTS gwy_ontology_paper;
DROP TABLE IF EXISTS gwy_ontology_bilateral_relation;
DROP TABLE IF EXISTS gwy_display_rpt_report;
DROP TABLE IF EXISTS gwy_display_alert_record;
