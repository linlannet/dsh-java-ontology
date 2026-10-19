


-- ================================================================
-- 七、种子数据
-- ================================================================

-- 1. 用户种子（3 用户：主任/研究员/访客）
INSERT INTO onto_sys_user (tenant_id, role_level, user_name, nick_name, email, password, status, del_flag, created_by)
VALUES (NULL, 0, 'director', '主任', 'director@linlan.net', '$2a$10$K4FvGCaty1h/quHwYIE80e.Im.t2JW8BfI6.YELnzlbwXmSBWrRsG', '0', '0', 'system');
INSERT INTO onto_sys_user (tenant_id, role_level, user_name, nick_name, email, password, status, del_flag, created_by)
VALUES (1, 1, 'researcher', '研究员', 'researcher@linlan.net', '$2a$10$K4FvGCaty1h/quHwYIE80e.Im.t2JW8BfI6.YELnzlbwXmSBWrRsG', '0', '0', 'system');
INSERT INTO onto_sys_user (tenant_id, role_level, user_name, nick_name, email, password, status, del_flag, created_by)
VALUES (1, 2, 'guest', '访客', 'guest@linlan.net', '$2a$10$K4FvGCaty1h/quHwYIE80e.Im.t2JW8BfI6.YELnzlbwXmSBWrRsG', '0', '0', 'system');

-- 2. 本体版本种子
INSERT INTO onto_ontology_version (domain, version_code, version_name, entity_count, source, status, created_by)
VALUES ('siis', 'v2.0.0', '本体知识库 v2.0 初始版本', 0, 'system', 1, 'system');

-- 3. 本体类型注册表种子（14 类型，ADR-001 路线 A）
INSERT INTO onto_ontology_type (type_code, table_name, display_name, domain, status, created_by) VALUES
('country',           'gwy_ontology_country',           '国家',     'siis', 1, 'system'),
('organization',      'gwy_ontology_organization',      '组织机构', 'siis', 1, 'system'),
('person',            'gwy_ontology_person',            '人物',     'siis', 1, 'system'),
('hotspot',           'gwy_ontology_hotspot',           '热点事件', 'siis', 1, 'system'),
('term',              'onto_ontology_term',             '术语',     'siis', 1, 'system'),
('eventType',         'onto_ontology_event_type',       '事件类型', 'siis', 1, 'system'),
('location',          'onto_ontology_location',         '地名',     'siis', 1, 'system'),
('media',             'gwy_ontology_media',             '媒体',     'siis', 1, 'system'),
('book',              'gwy_ontology_book',             '书籍',     'siis', 1, 'system'),
('paper',             'gwy_ontology_paper',             '论文',     'siis', 1, 'system'),
('entity',            'onto_ontology_entity',           '通用实体', 'siis', 1, 'system'),
('version',           'onto_ontology_version',          '本体版本', 'siis', 1, 'system'),
('bilateralRelation', 'gwy_ontology_bilateral_relation','双边关系', 'siis', 1, 'system'),
('i18n',              'onto_ontology_i18n',             '多语种翻译','siis', 1, 'system');

-- 4. 角色种子（3 角色：主任/研究员/访客）
INSERT INTO onto_sys_role (role_code, role_name, role_level, description, created_by) VALUES
('DIRECTOR',  '主任',   0, '全平台最高权限，跨租户管理', 'system'),
('RESEARCHER','研究员', 1, '租户数据读写+共享层只读',     'system'),
('GUEST',     '访客',   2, '共享层只读',                  'system');

-- 5. 用户-角色关联种子
INSERT INTO onto_sys_user_role (user_id, role_id, created_by) VALUES
 (1, 1, 'system'),
 (2, 2, 'system'),
 (3, 3, 'system');

