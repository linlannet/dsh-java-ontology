-- ================================================================
-- dsh_java_ontodb：单库合并 schema
-- 共 69 张表：33 张 onto_ 共享层表 + 36 张 gwy_ SIIS 业务层表
-- 对应文档：docs/0.2.1/ONTO_DB_DESIGN.md（onto_）+ dsh-ontoext-siis 既有 schema（gwy_）
-- 生成时间：2026-10-18，2026-10-19 合并 gwy_ 业务表
-- 注意：库本身由部署预建（JDBC URL 已指定库名），迁移只负责 schema，
--       不包含 CREATE DATABASE / USE 等 MySQL 管理语句（H2 测试库不兼容）。
-- ================================================================

-- ================================================================
-- 一、认证授权 onto_sys_（1 张）
-- ================================================================

CREATE TABLE IF NOT EXISTS onto_sys_user (
    user_id        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    tenant_id      BIGINT       DEFAULT NULL COMMENT '租户ID（主任可为NULL）',
    role_level     TINYINT      NOT NULL DEFAULT 2 COMMENT '角色层级：0=主任/1=研究员/2=访客',
    user_name      VARCHAR(50)  NOT NULL COMMENT '用户名',
    nick_name      VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    email          VARCHAR(50)  DEFAULT NULL COMMENT '邮箱',
    phonenumber    VARCHAR(50)  DEFAULT NULL COMMENT '手机号',
    password       VARCHAR(200) NOT NULL COMMENT '密码（加密存储）',
    status         VARCHAR(1)   DEFAULT '0' COMMENT '状态：0=正常 1=停用',
    del_flag       VARCHAR(1)   DEFAULT '0' COMMENT '删除标志：0=正常 1=删除',
    login_ip       VARCHAR(50)  DEFAULT NULL COMMENT '最后登录IP',
    login_date     DATETIME     DEFAULT NULL COMMENT '最后登录时间',
    created_by      VARCHAR(50)  DEFAULT NULL COMMENT '创建者',
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by      VARCHAR(50)  DEFAULT NULL COMMENT '更新者',
    updated_at    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark         VARCHAR(800) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_user_name (user_name)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';


-- ================================================================
-- 二、本体元数据 onto_ontology_（8 张，多业务方向 domain 列）
-- ================================================================

CREATE TABLE IF NOT EXISTS onto_ontology_version (
            id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
            domain        VARCHAR(50) NOT NULL DEFAULT 'siis' COMMENT '业务方向: siis/factory/ecommerce/city-ops',
    version_code  VARCHAR(50)  NOT NULL COMMENT '版本号',
    version_name  VARCHAR(100) DEFAULT NULL COMMENT '版本名称',
    entity_count  INT          DEFAULT 0 COMMENT '实体数量',
    source        VARCHAR(200) DEFAULT NULL COMMENT '数据来源',
    status        TINYINT      DEFAULT 0 COMMENT '状态：0=草稿 1=发布 2=归档',
    tenant_id     BIGINT       DEFAULT NULL,
    created_by     VARCHAR(50)  DEFAULT NULL,
    created_at   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by     VARCHAR(50)  DEFAULT NULL,
    updated_at   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag      TINYINT      DEFAULT 0,
    remark        VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='本体版本记录';

CREATE TABLE IF NOT EXISTS onto_ontology_entity (
           id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
           domain         VARCHAR(50) NOT NULL DEFAULT 'siis' COMMENT '业务方向: siis/factory/ecommerce/city-ops',
    category       VARCHAR(50)  NOT NULL COMMENT '类别：country/organization/person/location/media/book/paper/term',
    entity_name    VARCHAR(200) NOT NULL COMMENT '实体名称（中文）',
    lang           VARCHAR(10)  DEFAULT 'zh' COMMENT '主语种：zh/en/fr/ar/ru/es/ja/de',
    description    TEXT         DEFAULT NULL COMMENT '描述',
    source         VARCHAR(200) DEFAULT NULL COMMENT '来源',
    wiki_url       VARCHAR(500) DEFAULT NULL COMMENT 'Wiki链接',
    status         TINYINT      DEFAULT 1 COMMENT '状态',
    tenant_id      BIGINT       DEFAULT NULL,
    created_by      VARCHAR(50)  DEFAULT NULL,
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by      VARCHAR(50)  DEFAULT NULL,
    updated_at    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag       TINYINT      DEFAULT 0,
    remark         VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_category_name (category, entity_name)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='本体实体表（通用）';

CREATE TABLE IF NOT EXISTS onto_ontology_term (
         id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
         domain         VARCHAR(50) NOT NULL DEFAULT 'siis' COMMENT '业务方向: siis/factory/ecommerce/city-ops',
    term_name  VARCHAR(200) NOT NULL COMMENT '术语名（中文）',
    category   VARCHAR(50)  DEFAULT NULL COMMENT '分类',
    definition TEXT         DEFAULT NULL COMMENT '定义',
    source     VARCHAR(200) DEFAULT NULL COMMENT '来源',
    lang       VARCHAR(10)  DEFAULT 'zh' COMMENT '语种',
    status     TINYINT      DEFAULT 1 COMMENT '状态',
    tenant_id  BIGINT       DEFAULT NULL,
    created_by  VARCHAR(50)  DEFAULT NULL,
    created_at DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_by  VARCHAR(50)  DEFAULT NULL,
    updated_at DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag   TINYINT      DEFAULT 0,
    remark     VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='术语库';

CREATE TABLE IF NOT EXISTS onto_ontology_event_type (
               id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
               domain         VARCHAR(50) NOT NULL DEFAULT 'siis' COMMENT '业务方向: siis/factory/ecommerce/city-ops',
    event_type    VARCHAR(100) NOT NULL COMMENT '事件类型（中文）',
    description   TEXT         DEFAULT NULL COMMENT '描述',
    parent_id     BIGINT       DEFAULT NULL COMMENT '父类型ID（树形结构）',
    sort_order    INT          DEFAULT 0 COMMENT '排序',
    status        TINYINT      DEFAULT 1 COMMENT '状态',
    tenant_id     BIGINT       DEFAULT NULL,
    created_by     VARCHAR(50)  DEFAULT NULL,
    created_at   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by     VARCHAR(50)  DEFAULT NULL,
    updated_at   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag      TINYINT      DEFAULT 0,
    remark        VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='事件类型库';

CREATE TABLE IF NOT EXISTS onto_ontology_location (
             id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
             domain         VARCHAR(50) NOT NULL DEFAULT 'siis' COMMENT '业务方向: siis/factory/ecommerce/city-ops',
    name       VARCHAR(200) NOT NULL COMMENT '地名',
    loc_type   VARCHAR(50)  DEFAULT NULL COMMENT '类型：country/city/region/river/mountain',
    parent_id  BIGINT       DEFAULT NULL COMMENT '父级ID',
    country    VARCHAR(100) DEFAULT NULL COMMENT '所属国家',
    capital    VARCHAR(100) DEFAULT NULL COMMENT '首都/首府',
    population VARCHAR(50)  DEFAULT NULL COMMENT '人口',
    area       VARCHAR(50)  DEFAULT NULL COMMENT '面积',
    description TEXT        DEFAULT NULL COMMENT '描述',
    lang       VARCHAR(10)  DEFAULT 'zh' COMMENT '语种',
    status     TINYINT      DEFAULT 1 COMMENT '状态',
    tenant_id  BIGINT       DEFAULT NULL,
    created_by  VARCHAR(50)  DEFAULT NULL,
    created_at DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_by  VARCHAR(50)  DEFAULT NULL,
    updated_at DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag   TINYINT      DEFAULT 0,
    remark     VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='地名信息库';

CREATE TABLE IF NOT EXISTS onto_ontology_i18n (
         id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
         domain         VARCHAR(50) NOT NULL DEFAULT 'siis' COMMENT '业务方向: siis/factory/ecommerce/city-ops',
    entity_id   BIGINT       NOT NULL COMMENT '关联实体ID',
    category    VARCHAR(50)  NOT NULL COMMENT '本体分类：country/organization/person/term/...',
    lang        VARCHAR(10)  NOT NULL COMMENT '语种：en/fr/ar/ru/es/ja/de',
    name        VARCHAR(500) DEFAULT NULL COMMENT '该语种名称',
    description TEXT         DEFAULT NULL COMMENT '该语种描述',
    created_by   VARCHAR(50)  DEFAULT NULL,
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by   VARCHAR(50)  DEFAULT NULL,
    updated_at DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag    TINYINT      DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_entity_category_lang (entity_id, category, lang),
    KEY idx_category_lang (category, lang)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='本体多语种翻译表';

CREATE TABLE onto_ontology_attribute (
id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
entity_id       BIGINT      NOT NULL COMMENT '关联实体ID',
attr_name       VARCHAR(100) NOT NULL COMMENT '属性名',
attr_value      TEXT        COMMENT '属性值',
attr_type       VARCHAR(20) NOT NULL DEFAULT 'STRING' COMMENT '属性类型: STRING/NUMBER/DATE/JSON/BOOLEAN',
domain          VARCHAR(50) NOT NULL DEFAULT 'siis' COMMENT '业务方向',
tenant_id       BIGINT      NULL COMMENT '租户ID(共享层为NULL)',
created_by       VARCHAR(50) NULL COMMENT '创建者',
created_at     DATETIME    NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
updated_by       VARCHAR(50) NULL COMMENT '更新者',
updated_at     DATETIME    NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
del_flag        TINYINT     NOT NULL DEFAULT 0 COMMENT '删除标志(0=正常 1=删除)',
remark          VARCHAR(800) NULL COMMENT '备注',
PRIMARY KEY (id),
INDEX idx_entity (entity_id, domain)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='本体动态属性扩展';

CREATE TABLE onto_ontology_relation (
                                        id                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
                                        subject_entity_id   BIGINT      NOT NULL COMMENT '主体实体ID',
                                        object_entity_id    BIGINT      NOT NULL COMMENT '客体实体ID',
                                        relation_type       VARCHAR(50) NOT NULL COMMENT '关系类型: bilateral/supply/contain/manage',
                                        relation_level      VARCHAR(50) NULL COMMENT '关系级别',
                                        description         TEXT        NULL COMMENT '描述',
                                        domain              VARCHAR(50) NOT NULL DEFAULT 'siis' COMMENT '业务方向',
                                        tenant_id           BIGINT      NULL COMMENT '租户ID(共享层为NULL)',
                                        created_by           VARCHAR(50) NULL COMMENT '创建者',
                                        created_at         DATETIME    NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                        updated_by           VARCHAR(50) NULL COMMENT '更新者',
                                        updated_at         DATETIME    NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                        del_flag            TINYINT     NOT NULL DEFAULT 0 COMMENT '删除标志(0=正常 1=删除)',
                                        remark              VARCHAR(800) NULL COMMENT '备注',
                                        PRIMARY KEY (id),
                                        INDEX idx_subject (subject_entity_id, relation_type),
                                        INDEX idx_object (object_entity_id, relation_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通用本体关系';

CREATE TABLE IF NOT EXISTS onto_ontology_type (
         id                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
         type_code           VARCHAR(64)  NOT NULL COMMENT '类型代码(如country/person/policy)',
    table_name          VARCHAR(128) NOT NULL COMMENT '对应物理表名',
    display_name        VARCHAR(128) NULL COMMENT '展示名称',
    domain              VARCHAR(50)  NOT NULL DEFAULT 'siis' COMMENT '业务方向',
    field_schema        TEXT         NULL COMMENT '字段定义JSON(名称/类型/可写/可搜/展示)',
    display_config      TEXT         NULL COMMENT '展示配置JSON(列表页/详情页)',
    status              TINYINT      NOT NULL DEFAULT 1 COMMENT '状态(0=禁用 1=启用)',
    created_by           VARCHAR(50)  NULL COMMENT '创建者',
    created_at         DATETIME     NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by           VARCHAR(50)  NULL COMMENT '更新者',
    updated_at         DATETIME     NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag            TINYINT      NOT NULL DEFAULT 0 COMMENT '删除标志(0=正常 1=删除)',
    PRIMARY KEY (id),
    UNIQUE KEY uk_type_code (type_code, domain)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='本体类型元数据注册表(ADR-001路线A)';


-- ================================================================
-- 三、知识沉淀 onto_common_（4 张，个人层）
-- ================================================================

CREATE TABLE IF NOT EXISTS onto_common_discovered_entity (
                    id                 BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
                    entity_name        VARCHAR(200) NOT NULL COMMENT '实体名称',
    entity_type        VARCHAR(50)  DEFAULT NULL COMMENT '实体类型',
    source_topic_id    BIGINT       DEFAULT NULL COMMENT '来源主题ID',
    first_seen_time    DATETIME     DEFAULT NULL COMMENT '首次发现时间',
    mention_count      INT          DEFAULT 1 COMMENT '提及次数',
    ontology_entity_id BIGINT       DEFAULT NULL COMMENT '关联本体实体ID（晋升后回填）',
    status             VARCHAR(20)  DEFAULT 'DISCOVERED' COMMENT 'DISCOVERED/PROMOTED',
    tenant_id          BIGINT       NOT NULL COMMENT '租户ID',
    created_by          VARCHAR(50)  DEFAULT NULL,
    created_at        DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(50)  DEFAULT NULL,
    updated_at        DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag           TINYINT      DEFAULT 0,
    remark             VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_tenant_status (tenant_id, status)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='发现实体表';

CREATE TABLE IF NOT EXISTS onto_common_tenant_relation_asset (
                        id                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
                        subject_entity_id BIGINT       NOT NULL COMMENT '主体实体ID',
                        object_entity_id  BIGINT       NOT NULL COMMENT '客体实体ID',
                        relation_type     VARCHAR(50)  DEFAULT NULL COMMENT '关系类型',
    avg_score         FLOAT        DEFAULT NULL COMMENT '平均得分',
    topic_ids         VARCHAR(500) DEFAULT NULL COMMENT '关联主题ID列表',
    last_updated      DATETIME     DEFAULT NULL COMMENT '最后更新时间',
    tenant_id         BIGINT       NOT NULL COMMENT '租户ID',
    created_by         VARCHAR(50)  DEFAULT NULL,
    created_at       DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by         VARCHAR(50)  DEFAULT NULL,
    updated_at       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag          TINYINT      DEFAULT 0,
    remark            VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租户关系资产表';

CREATE TABLE IF NOT EXISTS onto_common_personal_vector_index (
                        id            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
                        entity_id     BIGINT      NOT NULL COMMENT '实体ID',
                        entity_type   VARCHAR(50) DEFAULT NULL COMMENT '实体类型',
    embedding     BLOB        DEFAULT NULL COMMENT '向量嵌入',
    model_version VARCHAR(50) DEFAULT NULL COMMENT '模型版本',
    tenant_id     BIGINT      NOT NULL COMMENT '租户ID',
    created_by     VARCHAR(50) DEFAULT NULL,
    created_at   DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_by     VARCHAR(50) DEFAULT NULL,
    updated_at   DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag      TINYINT     DEFAULT 0,
    remark        VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='个人向量索引表';

CREATE TABLE IF NOT EXISTS onto_common_llm_chat_history (
                   id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
                   agent_name VARCHAR(100) DEFAULT NULL COMMENT 'Agent名称',
    role       VARCHAR(20)  DEFAULT NULL COMMENT '角色：user/assistant/system',
    content    TEXT         DEFAULT NULL COMMENT '内容',
    session_id VARCHAR(100) DEFAULT NULL COMMENT '会话ID',
    tenant_id  BIGINT       NOT NULL COMMENT '租户ID',
    created_by  VARCHAR(50)  DEFAULT NULL,
    created_at DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_by  VARCHAR(50)  DEFAULT NULL,
    updated_at DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag   TINYINT      DEFAULT 0,
    remark     VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='LLM对话历史表';


-- ================================================================
-- 四、全局汇聚 onto_global_（5 张，共享层）
-- ================================================================

CREATE TABLE IF NOT EXISTS onto_global_discovered_entity (
                    id                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
                    entity_name         VARCHAR(200) NOT NULL COMMENT '实体名称',
    entity_type         VARCHAR(50)  DEFAULT NULL COMMENT '实体类型',
    tenant_ids          VARCHAR(1000) DEFAULT NULL COMMENT '来源租户ID列表JSON',
    total_mention_count INT          DEFAULT 0 COMMENT '总提及次数',
    first_seen_time     DATETIME     DEFAULT NULL COMMENT '首次发现时间',
    last_seen_time      DATETIME     DEFAULT NULL COMMENT '最后发现时间',
    tenant_id           BIGINT       DEFAULT NULL,
    created_by           VARCHAR(50)  DEFAULT NULL,
    created_at         DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by           VARCHAR(50)  DEFAULT NULL,
    updated_at         DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag            TINYINT      DEFAULT 0,
    remark              VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_name_type (entity_name, entity_type)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='全局发现实体表';

CREATE TABLE IF NOT EXISTS onto_global_bilateral_relation (
                     id                 BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
                     subject_entity_name VARCHAR(200) NOT NULL COMMENT '主体实体名',
    object_entity_name  VARCHAR(200) NOT NULL COMMENT '客体实体名',
    relation_type      VARCHAR(50)  DEFAULT NULL COMMENT '关系类型',
    tenant_ids         VARCHAR(1000) DEFAULT NULL COMMENT '来源租户ID列表JSON',
    avg_score          FLOAT        DEFAULT NULL COMMENT '平均得分',
    sample_count       INT          DEFAULT 0 COMMENT '样本数',
    tenant_id          BIGINT       DEFAULT NULL,
    created_by          VARCHAR(50)  DEFAULT NULL,
    created_at        DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(50)  DEFAULT NULL,
    updated_at        DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag           TINYINT      DEFAULT 0,
    remark             VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_subj_obj_rel (subject_entity_name, object_entity_name, relation_type)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='全局双边关系表';

CREATE TABLE IF NOT EXISTS onto_global_kg_node (
          id                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
          ontology_entity_id  BIGINT       DEFAULT NULL COMMENT '关联本体实体ID',
          node_name           VARCHAR(200) DEFAULT NULL COMMENT '节点名称',
    node_type           VARCHAR(50)  DEFAULT NULL COMMENT '节点类型',
    tenant_ids          VARCHAR(1000) DEFAULT NULL COMMENT '来源租户ID列表JSON',
    cross_tenant_weight FLOAT        DEFAULT 1.0 COMMENT '跨租户权重',
    tenant_id           BIGINT       DEFAULT NULL,
    created_by           VARCHAR(50)  DEFAULT NULL,
    created_at         DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by           VARCHAR(50)  DEFAULT NULL,
    updated_at         DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag            TINYINT      DEFAULT 0,
    remark              VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_kg_name_type (node_name, node_type)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='全局知识图谱节点表';

CREATE TABLE IF NOT EXISTS onto_global_kg_edge (
          id                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
          source_node_id      VARCHAR(100) NOT NULL COMMENT '源节点ID',
    target_node_id      VARCHAR(100) NOT NULL COMMENT '目标节点ID',
    relation_type       VARCHAR(100) DEFAULT NULL COMMENT '关系类型',
    tenant_ids          VARCHAR(1000) DEFAULT NULL COMMENT '来源租户ID列表JSON',
    cross_tenant_weight FLOAT        DEFAULT 1.0 COMMENT '跨租户权重',
    tenant_id           BIGINT       DEFAULT NULL,
    created_by           VARCHAR(50)  DEFAULT NULL,
    created_at         DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by           VARCHAR(50)  DEFAULT NULL,
    updated_at         DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag            TINYINT      DEFAULT 0,
    remark              VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_edge (source_node_id, target_node_id, relation_type)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='全局知识图谱边表';

CREATE TABLE IF NOT EXISTS onto_global_aggregation_log (
                  id              BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
                  aggregation_type VARCHAR(20) DEFAULT NULL COMMENT '汇聚类型：FULL/INCREMENTAL',
    trigger_type    VARCHAR(20) DEFAULT NULL COMMENT '触发类型：SCHEDULED/EVENT/MANUAL',
    entity_count    INT         DEFAULT 0 COMMENT '实体数量',
    relation_count  INT         DEFAULT 0 COMMENT '关系数量',
    duration_ms     BIGINT      DEFAULT 0 COMMENT '耗时（毫秒）',
    status          VARCHAR(20) DEFAULT 'running' COMMENT 'running/success/failed',
    tenant_id       BIGINT      DEFAULT NULL,
    created_by       VARCHAR(50) DEFAULT NULL,
    created_at     DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_by       VARCHAR(50) DEFAULT NULL,
    updated_at     DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag        TINYINT     DEFAULT 0,
    remark          VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='全局汇聚日志表';


-- ================================================================
-- 五、报告问数分析 onto_report_（7 张）
-- ================================================================

CREATE TABLE IF NOT EXISTS onto_report_template (
           id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
           template_name   VARCHAR(200) NOT NULL COMMENT '模板名称',
    template_type   VARCHAR(50)  DEFAULT NULL COMMENT '模板类型：ANALYSIS/WEEKLY/MONTHLY/CUSTOM',
    description     VARCHAR(800) DEFAULT NULL COMMENT '描述',
    section_config  TEXT         DEFAULT NULL COMMENT '章节配置（JSON）',
    chart_config    TEXT         DEFAULT NULL COMMENT '图表配置（JSON）',
    status          TINYINT      DEFAULT 0 COMMENT '状态：0=草稿 1=发布 2=归档',
    tenant_id       BIGINT       DEFAULT NULL,
    created_by       VARCHAR(50)  DEFAULT NULL,
    created_at     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by       VARCHAR(50)  DEFAULT NULL,
    updated_at     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag        TINYINT      DEFAULT 0,
    remark          VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报告模板表';

CREATE TABLE IF NOT EXISTS onto_report_instance (
           id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
           template_id     BIGINT       DEFAULT NULL COMMENT '模板ID',
           report_name     VARCHAR(200) NOT NULL COMMENT '报告名称',
    report_type     VARCHAR(50)  DEFAULT NULL COMMENT '报告类型',
    data_source     VARCHAR(200) DEFAULT NULL COMMENT '数据来源',
    parameters      TEXT         DEFAULT NULL COMMENT '参数（JSON）',
    status          VARCHAR(20)  DEFAULT 'DRAFT' COMMENT 'DRAFT/GENERATED/PUBLISHED',
    generated_time  DATETIME     DEFAULT NULL COMMENT '生成时间',
    published_time  DATETIME     DEFAULT NULL COMMENT '发布时间',
    legacy_report_id BIGINT      DEFAULT NULL COMMENT 'P2-20：旧表 gwy_display_rpt_report.id 快照引用（迁移后回填）',
    tenant_id       BIGINT       DEFAULT NULL,
    created_by       VARCHAR(50)  DEFAULT NULL,
    created_at     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by       VARCHAR(50)  DEFAULT NULL,
    updated_at     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag        TINYINT      DEFAULT 0,
    remark          VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_legacy (legacy_report_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报告实例表（P2-20 主表，gwy_display_rpt_report 废弃为快照）';

CREATE TABLE IF NOT EXISTS onto_report_section (
          id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
          instance_id     BIGINT       NOT NULL COMMENT '报告实例ID',
          section_index   INT          DEFAULT 0 COMMENT '章节序号',
          section_title   VARCHAR(200) DEFAULT NULL COMMENT '章节标题',
    section_type    VARCHAR(50)  DEFAULT NULL COMMENT '章节类型：TEXT/CHART/TABLE/SUMMARY',
    content         TEXT         DEFAULT NULL COMMENT '章节内容',
    chart_data      TEXT         DEFAULT NULL COMMENT '图表数据（JSON）',
    tenant_id       BIGINT       DEFAULT NULL,
    created_by       VARCHAR(50)  DEFAULT NULL,
    created_at     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by       VARCHAR(50)  DEFAULT NULL,
    updated_at     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag        TINYINT      DEFAULT 0,
    remark          VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_instance (instance_id, section_index)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报告章节表';

CREATE TABLE IF NOT EXISTS onto_report_chart_def (
            id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
            chart_name      VARCHAR(200) NOT NULL COMMENT '图表名称',
    chart_type      VARCHAR(50)  DEFAULT NULL COMMENT '图表类型：BAR/LINE/PIE/TABLE/HEATMAP/SCATTER',
    data_query      TEXT         DEFAULT NULL COMMENT '数据查询SQL',
    x_axis          VARCHAR(100) DEFAULT NULL COMMENT 'X轴字段',
    y_axis          VARCHAR(100) DEFAULT NULL COMMENT 'Y轴字段',
    series_config   TEXT         DEFAULT NULL COMMENT '系列配置（JSON）',
    template_id     BIGINT       DEFAULT NULL COMMENT '关联模板ID',
    tenant_id       BIGINT       DEFAULT NULL,
    created_by       VARCHAR(50)  DEFAULT NULL,
    created_at     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by       VARCHAR(50)  DEFAULT NULL,
    updated_at     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag        TINYINT      DEFAULT 0,
    remark          VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图表定义表';

CREATE TABLE IF NOT EXISTS onto_report_qa_session (
             id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
             session_name    VARCHAR(200) DEFAULT NULL COMMENT '会话名称',
    user_question   TEXT         DEFAULT NULL COMMENT '用户问题',
    context         TEXT         DEFAULT NULL COMMENT '上下文（JSON）',
    status          VARCHAR(20)  DEFAULT 'ACTIVE' COMMENT 'ACTIVE/CLOSED',
    tenant_id       BIGINT       DEFAULT NULL,
    created_by       VARCHAR(50)  DEFAULT NULL,
    created_at     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by       VARCHAR(50)  DEFAULT NULL,
    updated_at     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag        TINYINT      DEFAULT 0,
    remark          VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问数会话表';

CREATE TABLE IF NOT EXISTS onto_report_qa_query (
           id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
           session_id      BIGINT       NOT NULL COMMENT '会话ID',
           query_text      TEXT         NOT NULL COMMENT '查询文本（自然语言）',
           translated_sql  TEXT         DEFAULT NULL COMMENT '翻译后的SQL',
           result_data     TEXT         DEFAULT NULL COMMENT '结果数据（JSON）',
           chart_suggestion VARCHAR(500) DEFAULT NULL COMMENT '图表建议',
    process_status  VARCHAR(20)  DEFAULT 'PENDING' COMMENT 'PENDING/SUCCESS/FAILED',
    tenant_id       BIGINT       DEFAULT NULL,
    created_by       VARCHAR(50)  DEFAULT NULL,
    created_at     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by       VARCHAR(50)  DEFAULT NULL,
    updated_at     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag        TINYINT      DEFAULT 0,
    remark          VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_session (session_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问数查询记录表';

CREATE TABLE IF NOT EXISTS onto_report_analysis_task (
                id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
                task_name       VARCHAR(200) NOT NULL COMMENT '任务名称',
    task_type       VARCHAR(50)  DEFAULT NULL COMMENT '任务类型：TREND/CORRELATION/COMPARISON/PREDICTION',
    data_range      VARCHAR(200) DEFAULT NULL COMMENT '数据范围',
    analysis_config TEXT         DEFAULT NULL COMMENT '分析配置（JSON）',
    result_summary  TEXT         DEFAULT NULL COMMENT '结果摘要',
    status          VARCHAR(20)  DEFAULT 'PENDING' COMMENT 'PENDING/RUNNING/SUCCESS/FAILED',
    start_time      DATETIME     DEFAULT NULL COMMENT '开始时间',
    end_time        DATETIME     DEFAULT NULL COMMENT '结束时间',
    tenant_id       BIGINT       DEFAULT NULL,
    created_by       VARCHAR(50)  DEFAULT NULL,
    created_at     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by       VARCHAR(50)  DEFAULT NULL,
    updated_at     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag        TINYINT      DEFAULT 0,
    remark          VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分析任务表';


-- ================================================================
-- 六.5、审计日志表（onto_sys_audit_log，UPPER_CASE 风格）
-- ================================================================

CREATE TABLE IF NOT EXISTS onto_sys_audit_log (
         log_id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
         user_id         BIGINT       DEFAULT NULL COMMENT '操作用户ID',
         tenant_id       BIGINT       DEFAULT NULL COMMENT '租户ID',
         role_level      TINYINT      DEFAULT NULL COMMENT '用户角色层级',
         action          VARCHAR(50)  NOT NULL COMMENT '操作类型：CREATE/UPDATE/DELETE/LOGIN/LOGOUT/OTHER',
    target_type     VARCHAR(100) DEFAULT NULL COMMENT '操作目标类型（实体名/表名）',
    target_id       VARCHAR(100) DEFAULT NULL COMMENT '操作目标ID',
    method          VARCHAR(200) DEFAULT NULL COMMENT '执行方法（类.方法）',
    request_uri     VARCHAR(500) DEFAULT NULL COMMENT '请求URI',
    request_params  TEXT         DEFAULT NULL COMMENT '请求参数（JSON）',
    result_status   VARCHAR(20)  DEFAULT NULL COMMENT '结果状态：SUCCESS/FAILED',
    error_msg       TEXT         DEFAULT NULL COMMENT '错误信息',
    execution_ms    BIGINT       DEFAULT NULL COMMENT '执行耗时（毫秒）',
    client_ip       VARCHAR(50)  DEFAULT NULL COMMENT '客户端IP',
    created_at     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
    PRIMARY KEY (log_id),
    KEY idx_audit_user_id (user_id),
    KEY idx_audit_action (action),
    KEY idx_audit_created_at (created_at)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计日志表';

-- ================================================================
-- 六.6、角色管理表（onto_sys_role + onto_sys_user_role，UPPER_CASE 风格）
-- ================================================================

CREATE TABLE IF NOT EXISTS onto_sys_role (
    role_id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    role_code       VARCHAR(50)  NOT NULL COMMENT '角色代码',
    role_name       VARCHAR(50)  NOT NULL COMMENT '角色名称',
    role_level      TINYINT      NOT NULL COMMENT '角色层级：0=主任/1=研究员/2=访客',
    description     VARCHAR(200) DEFAULT NULL COMMENT '角色描述',
    status          VARCHAR(1)   DEFAULT '0' COMMENT '状态：0=正常 1=停用',
    del_flag        VARCHAR(1)   DEFAULT '0' COMMENT '删除标志：0=正常 1=删除',
    created_by       VARCHAR(50)  DEFAULT NULL,
    created_at     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by       VARCHAR(50)  DEFAULT NULL,
    updated_at     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    remark          VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (role_id),
    UNIQUE KEY uk_role_code (role_code)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

CREATE TABLE IF NOT EXISTS onto_sys_user_role (
         id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
         user_id         BIGINT       NOT NULL COMMENT '用户ID',
         role_id         BIGINT       NOT NULL COMMENT '角色ID',
         created_by       VARCHAR(50)  DEFAULT NULL,
    created_at     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户-角色关联表';

-- ================================================================
-- 六.7、统一文件表（onto_common_file，lower_case 风格，P1-9）
-- ================================================================

CREATE TABLE IF NOT EXISTS onto_common_file (
       id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
       file_name       VARCHAR(500) NOT NULL COMMENT '原始文件名',
    file_path       VARCHAR(1000) NOT NULL COMMENT '存储路径',
    file_type       VARCHAR(50)  DEFAULT NULL COMMENT '文件分类：media/document/export/attachment',
    file_size       BIGINT       DEFAULT NULL COMMENT '文件大小（字节）',
    mime_type       VARCHAR(100) DEFAULT NULL COMMENT 'MIME类型',
    ref_count       INT          DEFAULT 0 COMMENT '引用计数',
    storage_type    VARCHAR(20)  DEFAULT 'LOCAL' COMMENT '存储类型：LOCAL/OSS/URL',
    tenant_id       BIGINT       DEFAULT NULL COMMENT '租户ID',
    created_by       VARCHAR(50)  DEFAULT NULL,
    created_at     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by       VARCHAR(50)  DEFAULT NULL,
    updated_at     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag        TINYINT      DEFAULT 0,
    remark          VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_file_tenant (tenant_id),
    KEY idx_file_type (file_type)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='统一文件表';

-- ================================================================
-- 六.8、通知订阅表（onto_notify_subscription + onto_notify_message，P1-11）
-- ================================================================

CREATE TABLE IF NOT EXISTS onto_notify_subscription (
               id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
               subscriber_id   BIGINT       NOT NULL COMMENT '订阅人用户ID',
               target_type     VARCHAR(50)  NOT NULL COMMENT '订阅目标类型：alert/topic/report',
    target_id       BIGINT       NOT NULL COMMENT '订阅目标ID',
    channel         VARCHAR(20)  DEFAULT 'email' COMMENT '通知渠道：email/SMS/WEBHOOK/IN_APP',
    enabled         TINYINT      DEFAULT 1 COMMENT '是否启用：0=否 1=是',
    tenant_id       BIGINT       DEFAULT NULL,
    created_by       VARCHAR(50)  DEFAULT NULL,
    created_at     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by       VARCHAR(50)  DEFAULT NULL,
    updated_at     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag        TINYINT      DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_notify_subscriber (subscriber_id),
    KEY idx_notify_target (target_type, target_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知订阅表';

CREATE TABLE IF NOT EXISTS onto_notify_message (
          id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
          subscription_id BIGINT       DEFAULT NULL COMMENT '关联订阅ID',
          recipient_id    BIGINT       NOT NULL COMMENT '收件人用户ID',
          title           VARCHAR(200) NOT NULL COMMENT '消息标题',
    content         TEXT         COMMENT '消息内容',
    channel         VARCHAR(20)  DEFAULT 'email' COMMENT '通知渠道',
    status          VARCHAR(20)  DEFAULT 'PENDING' COMMENT '状态：PENDING/SENT/READ/FAILED',
    sent_time       DATETIME     DEFAULT NULL COMMENT '发送时间',
    tenant_id       BIGINT       DEFAULT NULL,
    created_by       VARCHAR(50)  DEFAULT NULL,
    created_at     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_msg_recipient (recipient_id),
    KEY idx_msg_status (status)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知消息表';

-- ================================================================
-- 七、实体消歧与合并日志（P2-15）
-- ================================================================

CREATE TABLE IF NOT EXISTS onto_common_merge_log (
            id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
            source_entity_id BIGINT      NOT NULL COMMENT '被合并的源实体ID（合并后标记删除）',
            target_entity_id BIGINT      NOT NULL COMMENT '合并目标实体ID（保留）',
            entity_type     VARCHAR(50)  DEFAULT NULL COMMENT '实体类型',
    similarity_score FLOAT       DEFAULT NULL COMMENT '相似度得分（0-1）',
    merge_strategy  VARCHAR(30)  NOT NULL COMMENT '合并策略：AUTO_NAME_MATCH/AUTO_FUZZY/MANUAL',
    operator_id     BIGINT       DEFAULT NULL COMMENT '操作人用户ID（自动合并为NULL）',
    tenant_id       BIGINT       NOT NULL COMMENT '租户ID',
    created_by       VARCHAR(50)  DEFAULT NULL,
    created_at     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by       VARCHAR(50)  DEFAULT NULL,
    updated_at     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag        TINYINT      DEFAULT 0,
    remark          VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_tenant_target (tenant_id, target_entity_id),
    KEY idx_source (source_entity_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='实体合并日志表';

-- ================================================================
-- 八、SIIS 业务层 gwy_ 表（36 张：ontology/track/analyze/display）
-- 摘自 dsh-ontoext-siis-app 的 V1.0.0（dsh_java_ontodb 单库合并 schema，
-- DatabaseSchemaTest 断言 onto_ + gwy_ 全量表共存于同一库）
-- ================================================================

CREATE TABLE IF NOT EXISTS gwy_ontology_country (
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    name       VARCHAR(100) NOT NULL COMMENT '国家名称',
    iso_code   VARCHAR(10)  DEFAULT NULL COMMENT 'ISO 3166代码',
    continent  VARCHAR(50)  DEFAULT NULL COMMENT '所属洲',
    capital    VARCHAR(100) DEFAULT NULL COMMENT '首都',
    language   VARCHAR(100) DEFAULT NULL COMMENT '主要语言',
    currency   VARCHAR(50)  DEFAULT NULL COMMENT '货币',
    population VARCHAR(50)  DEFAULT NULL COMMENT '人口',
    area       VARCHAR(50)  DEFAULT NULL COMMENT '面积',
    status     TINYINT      DEFAULT 1 COMMENT '状态',
    tenant_id  BIGINT       DEFAULT NULL,
    created_by  VARCHAR(50)  DEFAULT NULL,
    created_at DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_by  VARCHAR(50)  DEFAULT NULL,
    updated_at DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag   TINYINT      DEFAULT 0,
    remark     VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_iso_code (iso_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='国家信息库';

CREATE TABLE IF NOT EXISTS gwy_ontology_organization (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(200) NOT NULL COMMENT '机构名称',
    org_type    VARCHAR(50)  DEFAULT NULL COMMENT '类型：government/ngo/company/media/university',
    country     VARCHAR(100) DEFAULT NULL COMMENT '所属国家',
    description TEXT         DEFAULT NULL COMMENT '描述',
    website     VARCHAR(500) DEFAULT NULL COMMENT '官网',
    lang        VARCHAR(10)  DEFAULT 'zh' COMMENT '语种',
    status      TINYINT      DEFAULT 1 COMMENT '状态',
    tenant_id   BIGINT       DEFAULT NULL,
    created_by   VARCHAR(50)  DEFAULT NULL,
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by   VARCHAR(50)  DEFAULT NULL,
    updated_at DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag    TINYINT      DEFAULT 0,
    remark      VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='机构信息库';

CREATE TABLE IF NOT EXISTS gwy_ontology_person (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    name          VARCHAR(200) NOT NULL COMMENT '姓名',
    gender        VARCHAR(10)  DEFAULT NULL COMMENT '性别',
    birth_date    VARCHAR(50)  DEFAULT NULL COMMENT '出生日期',
    country       VARCHAR(100) DEFAULT NULL COMMENT '国籍',
    occupation    VARCHAR(200) DEFAULT NULL COMMENT '职业',
    title         VARCHAR(200) DEFAULT NULL COMMENT '头衔/职务',
    affiliation   VARCHAR(200) DEFAULT NULL COMMENT '所属机构',
    bio           TEXT         DEFAULT NULL COMMENT '简介',
    portrait_url  VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    lang          VARCHAR(10)  DEFAULT 'zh' COMMENT '语种',
    status        TINYINT      DEFAULT 1 COMMENT '状态',
    tenant_id     BIGINT       DEFAULT NULL,
    created_by     VARCHAR(50)  DEFAULT NULL,
    created_at   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by     VARCHAR(50)  DEFAULT NULL,
    updated_at   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag      TINYINT      DEFAULT 0,
    remark        VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='人物信息库';

CREATE TABLE IF NOT EXISTS gwy_ontology_hotspot (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    hotspot_name     VARCHAR(200) NOT NULL COMMENT '热点名称',
    hotspot_type     VARCHAR(50)  DEFAULT NULL COMMENT '类型',
    keywords         TEXT         DEFAULT NULL COMMENT '关键词JSON',
    related_countries VARCHAR(500) DEFAULT NULL COMMENT '相关国家',
    start_time       DATETIME     DEFAULT NULL COMMENT '开始时间',
    end_time         DATETIME     DEFAULT NULL COMMENT '结束时间',
    heat_score       INT          DEFAULT 0 COMMENT '热度分',
    risk_level       VARCHAR(20)  DEFAULT 'low' COMMENT '风险等级：low/medium/high/critical',
    status           TINYINT      DEFAULT 1 COMMENT '状态',
    tenant_id        BIGINT       DEFAULT NULL,
    created_by        VARCHAR(50)  DEFAULT NULL,
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by        VARCHAR(50)  DEFAULT NULL,
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag         TINYINT      DEFAULT 0,
    remark           VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='热点话题库';

CREATE TABLE IF NOT EXISTS gwy_ontology_media (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(200) NOT NULL COMMENT '媒体名称',
    media_type  VARCHAR(50)  DEFAULT NULL COMMENT '类型：newspaper/magazine/tv/radio/web/news_agency',
    country     VARCHAR(100) DEFAULT NULL COMMENT '所属国家',
    language    VARCHAR(20)  DEFAULT NULL COMMENT '语言',
    website     VARCHAR(500) DEFAULT NULL COMMENT '官网',
    audience    VARCHAR(50)  DEFAULT NULL COMMENT '受众规模',
    bias_rating VARCHAR(20)  DEFAULT NULL COMMENT '倾向评级',
    description TEXT         DEFAULT NULL COMMENT '描述',
    lang        VARCHAR(10)  DEFAULT 'zh' COMMENT '语种',
    status      TINYINT      DEFAULT 1 COMMENT '状态',
    tenant_id   BIGINT       DEFAULT NULL,
    created_by   VARCHAR(50)  DEFAULT NULL,
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by   VARCHAR(50)  DEFAULT NULL,
    updated_at DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag    TINYINT      DEFAULT 0,
    remark      VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='媒体资料库';

CREATE TABLE IF NOT EXISTS gwy_ontology_book (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    title        VARCHAR(500) NOT NULL COMMENT '书名',
    author       VARCHAR(200) DEFAULT NULL COMMENT '作者',
    publisher    VARCHAR(200) DEFAULT NULL COMMENT '出版社',
    publish_year VARCHAR(20)  DEFAULT NULL COMMENT '出版年',
    isbn         VARCHAR(50)  DEFAULT NULL COMMENT 'ISBN',
    pages        INT          DEFAULT NULL COMMENT '页数',
    category     VARCHAR(100) DEFAULT NULL COMMENT '分类',
    summary      TEXT         DEFAULT NULL COMMENT '摘要',
    lang         VARCHAR(10)  DEFAULT 'zh' COMMENT '语种',
    status       TINYINT      DEFAULT 1 COMMENT '状态',
    tenant_id    BIGINT       DEFAULT NULL,
    created_by    VARCHAR(50)  DEFAULT NULL,
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by    VARCHAR(50)  DEFAULT NULL,
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag     TINYINT      DEFAULT 0,
    remark       VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='书籍信息库';

CREATE TABLE IF NOT EXISTS gwy_ontology_paper (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    title        VARCHAR(800) NOT NULL COMMENT '论文标题',
    author       VARCHAR(500) DEFAULT NULL COMMENT '作者',
    institution  VARCHAR(300) DEFAULT NULL COMMENT '机构',
    journal      VARCHAR(200) DEFAULT NULL COMMENT '期刊',
    publish_year VARCHAR(20)  DEFAULT NULL COMMENT '发表年份',
    doi          VARCHAR(100) DEFAULT NULL COMMENT 'DOI',
    summary      TEXT         DEFAULT NULL COMMENT '摘要',
    keywords     VARCHAR(500) DEFAULT NULL COMMENT '关键词',
    category     VARCHAR(100) DEFAULT NULL COMMENT '学科分类',
    lang         VARCHAR(10)  DEFAULT 'zh' COMMENT '语种',
    status       TINYINT      DEFAULT 1 COMMENT '状态',
    tenant_id    BIGINT       DEFAULT NULL,
    created_by    VARCHAR(50)  DEFAULT NULL,
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by    VARCHAR(50)  DEFAULT NULL,
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag     TINYINT      DEFAULT 0,
    remark       VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='论文信息库';

CREATE TABLE IF NOT EXISTS gwy_ontology_bilateral_relation (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    country_a     VARCHAR(100) NOT NULL COMMENT '国家A',
    country_b     VARCHAR(100) NOT NULL COMMENT '国家B',
    relation_type VARCHAR(50)  DEFAULT NULL COMMENT '关系类型',
    relation_level VARCHAR(50) DEFAULT NULL COMMENT '关系级别',
    description   TEXT         DEFAULT NULL COMMENT '描述',
    start_year    VARCHAR(20)  DEFAULT NULL COMMENT '起始年份',
    status        TINYINT      DEFAULT 1 COMMENT '状态',
    tenant_id     BIGINT       DEFAULT NULL,
    created_by     VARCHAR(50)  DEFAULT NULL,
    created_at   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by     VARCHAR(50)  DEFAULT NULL,
    updated_at   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag      TINYINT      DEFAULT 0,
    remark        VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='双边关系表';

CREATE TABLE IF NOT EXISTS gwy_track_article (
    id              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    article_hash    VARCHAR(50)   NOT NULL COMMENT '文章哈希（去重）',
    title           VARCHAR(500)  DEFAULT NULL COMMENT '标题',
    url             VARCHAR(1000) DEFAULT NULL COMMENT '原文URL',
    lang            VARCHAR(10)   DEFAULT 'zh' COMMENT '语种',
    published       DATETIME      DEFAULT NULL COMMENT '发布时间',
    source          VARCHAR(200)  DEFAULT NULL COMMENT '来源',
    source_id       BIGINT        DEFAULT NULL COMMENT '采集源ID',
    topic_id        BIGINT        DEFAULT NULL COMMENT '关联主题ID',
    crawl_task_id   BIGINT        DEFAULT NULL COMMENT '采集任务ID',
    sentiment       VARCHAR(20)   DEFAULT NULL COMMENT '情感：positive/negative/neutral',
    sentiment_score FLOAT         DEFAULT NULL COMMENT '情感得分',
    nlp_status      VARCHAR(20)   DEFAULT 'PENDING' COMMENT 'NLP状态：PENDING/COMPLETED',
    content         TEXT          DEFAULT NULL COMMENT '正文摘要',
    tenant_id       BIGINT        NOT NULL COMMENT '租户ID',
    created_by       VARCHAR(50)   DEFAULT NULL,
    created_at     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    updated_by       VARCHAR(50)   DEFAULT NULL,
    updated_at     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag        TINYINT       DEFAULT 0,
    remark          VARCHAR(800)  DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_hash (tenant_id, article_hash),
    KEY idx_tenant_topic_published (tenant_id, topic_id, published)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章表';

CREATE TABLE IF NOT EXISTS gwy_track_crawl_source (
    id               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    source_name      VARCHAR(100)  NOT NULL COMMENT '源名称',
    source_type      VARCHAR(50)   DEFAULT NULL COMMENT '类型：website/rss/api/search_engine/database',
    url              VARCHAR(1000) DEFAULT NULL COMMENT 'URL',
    lang             VARCHAR(10)   DEFAULT 'zh' COMMENT '语种',
    category         VARCHAR(100)  DEFAULT NULL COMMENT '分类：academic/government/media',
    enabled          TINYINT       DEFAULT 1 COMMENT '是否启用',
    interval_minutes INT           DEFAULT 60 COMMENT '采集间隔（分钟）',
    last_crawl_time  DATETIME      DEFAULT NULL COMMENT '上次采集时间',
    last_status      VARCHAR(50)   DEFAULT NULL COMMENT '上次状态',
    last_article_count INT         DEFAULT 0 COMMENT '上次采集数量',
    tenant_id        BIGINT        NOT NULL COMMENT '租户ID',
    created_by        VARCHAR(50)   DEFAULT NULL,
    created_at      DATETIME      DEFAULT CURRENT_TIMESTAMP,
    updated_by        VARCHAR(50)   DEFAULT NULL,
    updated_at      DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag         TINYINT       DEFAULT 0,
    remark           VARCHAR(800)  DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采集源配置表';

CREATE TABLE IF NOT EXISTS gwy_track_crawl_subscription (
    id                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    subscription_name VARCHAR(100) NOT NULL COMMENT '订阅名称',
    source_ids        VARCHAR(500) DEFAULT NULL COMMENT '关联源ID列表',
    keyword_group_id  BIGINT       DEFAULT NULL COMMENT '关键词组ID',
    user_id           BIGINT       DEFAULT NULL COMMENT '订阅用户ID',
    enabled           TINYINT      DEFAULT 1 COMMENT '是否启用',
    interval_minutes  INT          DEFAULT 60 COMMENT '采集间隔',
    start_time        TIME         DEFAULT NULL COMMENT '开始时间',
    end_time          TIME         DEFAULT NULL COMMENT '结束时间',
    tenant_id         BIGINT       NOT NULL COMMENT '租户ID',
    created_by         VARCHAR(50)  DEFAULT NULL,
    created_at       DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by         VARCHAR(50)  DEFAULT NULL,
    updated_at       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag          TINYINT      DEFAULT 0,
    remark            VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采集订阅表';

CREATE TABLE IF NOT EXISTS gwy_track_crawl_task (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    task_name       VARCHAR(200) NOT NULL COMMENT '任务名称',
    source_id       BIGINT       DEFAULT NULL COMMENT '采集源ID',
    subscription_id BIGINT       DEFAULT NULL COMMENT '订阅ID',
    status          VARCHAR(20)  DEFAULT 'pending' COMMENT 'pending/running/success/failed',
    scheduled_time  DATETIME     DEFAULT NULL COMMENT '计划时间',
    crawl_frequency VARCHAR(50)  DEFAULT NULL COMMENT '采集频率',
    start_time      DATETIME     DEFAULT NULL COMMENT '开始时间',
    end_time        DATETIME     DEFAULT NULL COMMENT '结束时间',
    article_count   INT          DEFAULT 0 COMMENT '采集文章数',
    error_msg       TEXT         DEFAULT NULL COMMENT '错误信息',
    retry_count     INT          DEFAULT 0 COMMENT '重试次数',
    tenant_id       BIGINT       NOT NULL COMMENT '租户ID',
    created_by       VARCHAR(50)  DEFAULT NULL,
    created_at     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by       VARCHAR(50)  DEFAULT NULL,
    updated_at     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag        TINYINT      DEFAULT 0,
    remark          VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采集任务记录表';

CREATE TABLE IF NOT EXISTS gwy_track_crawl_keyword_group (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    group_name  VARCHAR(100) NOT NULL COMMENT '关键词组名称',
    keywords_zh TEXT         DEFAULT NULL COMMENT '中文关键词JSON',
    keywords_en TEXT         DEFAULT NULL COMMENT '英文关键词JSON',
    keywords_fr TEXT         DEFAULT NULL COMMENT '法文关键词JSON',
    keywords_ar TEXT         DEFAULT NULL COMMENT '阿拉伯文关键词JSON',
    match_mode  VARCHAR(20)  DEFAULT 'or' COMMENT '匹配模式：and/or',
    lang        VARCHAR(10)  DEFAULT 'zh' COMMENT '语种',
    enabled     TINYINT      DEFAULT 1 COMMENT '是否启用',
    tenant_id   BIGINT       NOT NULL COMMENT '租户ID',
    created_by   VARCHAR(50)  DEFAULT NULL,
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by   VARCHAR(50)  DEFAULT NULL,
    updated_at DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag    TINYINT      DEFAULT 0,
    remark      VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采集关键词组表';

CREATE TABLE IF NOT EXISTS gwy_track_data_import (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    import_name  VARCHAR(200) NOT NULL COMMENT '导入名称',
    import_type  VARCHAR(50)  DEFAULT NULL COMMENT 'csv/excel/json/database/api',
    file_path    VARCHAR(500) DEFAULT NULL COMMENT '文件路径',
    file_size    BIGINT       DEFAULT 0 COMMENT '文件大小（字节）',
    record_count INT          DEFAULT 0 COMMENT '记录数',
    target_table VARCHAR(100) DEFAULT NULL COMMENT '目标表',
    status       VARCHAR(20)  DEFAULT 'pending' COMMENT 'pending/running/success/failed',
    error_rows   INT          DEFAULT 0 COMMENT '错误行数',
    start_time   DATETIME     DEFAULT NULL COMMENT '开始时间',
    end_time     DATETIME     DEFAULT NULL COMMENT '结束时间',
    error_log    TEXT         DEFAULT NULL COMMENT '错误日志',
    tenant_id    BIGINT       NOT NULL COMMENT '租户ID',
    created_by    VARCHAR(50)  DEFAULT NULL,
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by    VARCHAR(50)  DEFAULT NULL,
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag     TINYINT      DEFAULT 0,
    remark       VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据导入记录表';

CREATE TABLE IF NOT EXISTS gwy_track_data_export (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    export_name      VARCHAR(200) NOT NULL COMMENT '导出名称',
    export_type      VARCHAR(50)  DEFAULT NULL COMMENT 'csv/excel/json/pdf',
    source_table     VARCHAR(100) DEFAULT NULL COMMENT '源表',
    filter_conditions TEXT        DEFAULT NULL COMMENT '筛选条件JSON',
    file_path        VARCHAR(500) DEFAULT NULL COMMENT '文件路径',
    file_size        BIGINT       DEFAULT 0 COMMENT '文件大小',
    record_count     INT          DEFAULT 0 COMMENT '导出记录数',
    status           VARCHAR(20)  DEFAULT 'pending' COMMENT 'pending/running/success/failed',
    download_token   VARCHAR(100) DEFAULT NULL COMMENT '下载Token',
    expire_time      DATETIME     DEFAULT NULL COMMENT '过期时间',
    tenant_id        BIGINT       NOT NULL COMMENT '租户ID',
    created_by        VARCHAR(50)  DEFAULT NULL,
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by        VARCHAR(50)  DEFAULT NULL,
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag         TINYINT      DEFAULT 0,
    remark           VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据导出记录表';

CREATE TABLE IF NOT EXISTS gwy_track_kg_node (
    id                 BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    node_id            VARCHAR(100) NOT NULL COMMENT '节点ID',
    node_type          VARCHAR(50)  DEFAULT NULL COMMENT '节点类型',
    node_name          VARCHAR(200) DEFAULT NULL COMMENT '节点名称',
    node_name_zh       VARCHAR(200) DEFAULT NULL COMMENT '节点中文名',
    ontology_entity_id BIGINT       DEFAULT NULL COMMENT '关联本体实体ID',
    properties         JSON         DEFAULT NULL COMMENT '节点属性',
    weight             FLOAT        DEFAULT 1.0 COMMENT '权重',
    source_article_ids TEXT         DEFAULT NULL COMMENT '来源文章ID列表JSON',
    tenant_id          BIGINT       NOT NULL COMMENT '租户ID',
    created_by          VARCHAR(50)  DEFAULT NULL,
    created_at        DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(50)  DEFAULT NULL,
    updated_at        DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag           TINYINT      DEFAULT 0,
    remark             VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_tenant_node (tenant_id, node_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='个人知识图谱节点';

CREATE TABLE IF NOT EXISTS gwy_track_kg_edge (
    id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    source_node_id VARCHAR(100) NOT NULL COMMENT '源节点ID',
    target_node_id VARCHAR(100) NOT NULL COMMENT '目标节点ID',
    relation_type  VARCHAR(100) DEFAULT NULL COMMENT '关系类型',
    relation_name  VARCHAR(200) DEFAULT NULL COMMENT '关系名称',
    properties     JSON         DEFAULT NULL COMMENT '关系属性',
    weight         FLOAT        DEFAULT 1.0 COMMENT '权重',
    article_id     BIGINT       DEFAULT NULL COMMENT '证据文章ID',
    tenant_id      BIGINT       NOT NULL COMMENT '租户ID',
    created_by      VARCHAR(50)  DEFAULT NULL,
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by      VARCHAR(50)  DEFAULT NULL,
    updated_at    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag       TINYINT      DEFAULT 0,
    remark         VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_tenant_edge (tenant_id, source_node_id, target_node_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='个人知识图谱边';

CREATE TABLE IF NOT EXISTS gwy_track_kg_build_log (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    topic_id    BIGINT       DEFAULT NULL COMMENT '主题ID',
    build_status VARCHAR(20) DEFAULT 'running' COMMENT 'running/success/failed',
    node_count  INT          DEFAULT 0 COMMENT '节点数',
    edge_count  INT          DEFAULT 0 COMMENT '边数',
    start_time  DATETIME     DEFAULT NULL COMMENT '开始时间',
    end_time    DATETIME     DEFAULT NULL COMMENT '结束时间',
    error_log   TEXT         DEFAULT NULL COMMENT '错误日志',
    tenant_id   BIGINT       NOT NULL COMMENT '租户ID',
    created_by   VARCHAR(50)  DEFAULT NULL,
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by   VARCHAR(50)  DEFAULT NULL,
    updated_at DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag    TINYINT      DEFAULT 0,
    remark      VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图谱构建日志';

CREATE TABLE IF NOT EXISTS gwy_track_ner_element (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    article_id    BIGINT       NOT NULL COMMENT '文章ID',
    element_when  JSON         DEFAULT NULL COMMENT '时间要素',
    element_where JSON         DEFAULT NULL COMMENT '地点要素',
    element_who   JSON         DEFAULT NULL COMMENT '人物要素',
    element_what  JSON         DEFAULT NULL COMMENT '事件要素',
    element_how   JSON         DEFAULT NULL COMMENT '方式要素',
    element_why   JSON         DEFAULT NULL COMMENT '原因要素',
    tenant_id     BIGINT       NOT NULL COMMENT '租户ID',
    created_by     VARCHAR(50)  DEFAULT NULL,
    created_at   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by     VARCHAR(50)  DEFAULT NULL,
    updated_at   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag      TINYINT      DEFAULT 0,
    remark        VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='NER要素表';

CREATE TABLE IF NOT EXISTS gwy_analyze_track_topic (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    topic_name       VARCHAR(200) NOT NULL COMMENT '主题名称',
    topic_type       VARCHAR(50)  DEFAULT NULL COMMENT '主题类型',
    hotspot_region   VARCHAR(200) DEFAULT NULL COMMENT '关注区域',
    description      TEXT         DEFAULT NULL COMMENT '描述',
    keyword_group_id BIGINT       DEFAULT NULL COMMENT '关键词组ID',
    crawl_source_ids VARCHAR(500) DEFAULT NULL COMMENT '采集源ID列表',
    alert_rule_ids   VARCHAR(500) DEFAULT NULL COMMENT '告警规则ID列表',
    user_id          BIGINT       DEFAULT NULL COMMENT '创建用户ID',
    status           VARCHAR(20)  DEFAULT 'active' COMMENT 'active/paused/closed',
    alert_enabled    TINYINT      DEFAULT 0 COMMENT '是否启用告警',
    tenant_id        BIGINT       NOT NULL COMMENT '租户ID',
    created_by        VARCHAR(50)  DEFAULT NULL,
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by        VARCHAR(50)  DEFAULT NULL,
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag         TINYINT      DEFAULT 0,
    remark           VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_tenant_user (tenant_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='追踪主题表';

CREATE TABLE IF NOT EXISTS gwy_analyze_track_element (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    topic_id     BIGINT       NOT NULL COMMENT '主题ID',
    element_type VARCHAR(50)  DEFAULT NULL COMMENT '要素类型：country/person/org/event/time',
    element_value VARCHAR(200) DEFAULT NULL COMMENT '要素值',
    element_name VARCHAR(200) DEFAULT NULL COMMENT '要素名称',
    weight       INT          DEFAULT 1 COMMENT '权重',
    tenant_id    BIGINT       NOT NULL COMMENT '租户ID',
    created_by    VARCHAR(50)  DEFAULT NULL,
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by    VARCHAR(50)  DEFAULT NULL,
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag     TINYINT      DEFAULT 0,
    remark       VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='主题要素表';

CREATE TABLE IF NOT EXISTS gwy_analyze_nlp_entity (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    article_id  BIGINT       NOT NULL COMMENT '文章ID',
    entity_type VARCHAR(50)  DEFAULT NULL COMMENT '实体类型',
    entity_value VARCHAR(200) DEFAULT NULL COMMENT '实体值',
    entity_name VARCHAR(200) DEFAULT NULL COMMENT '实体名',
    start_pos   INT          DEFAULT NULL COMMENT '起始位置',
    end_pos     INT          DEFAULT NULL COMMENT '结束位置',
    confidence  FLOAT        DEFAULT NULL COMMENT '置信度',
    lang        VARCHAR(10)  DEFAULT 'zh' COMMENT '语种',
    tenant_id   BIGINT       NOT NULL COMMENT '租户ID',
    created_by   VARCHAR(50)  DEFAULT NULL,
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by   VARCHAR(50)  DEFAULT NULL,
    updated_at DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag    TINYINT      DEFAULT 0,
    remark      VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='NLP实体识别结果表';

CREATE TABLE IF NOT EXISTS gwy_analyze_nlp_triple (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    article_id  BIGINT       NOT NULL COMMENT '文章ID',
    subject     VARCHAR(200) DEFAULT NULL COMMENT '主语',
    predicate   VARCHAR(200) DEFAULT NULL COMMENT '谓词',
    obj         VARCHAR(200) DEFAULT NULL COMMENT '宾语',
    subject_type VARCHAR(50) DEFAULT NULL COMMENT '主语类型',
    object_type VARCHAR(50)  DEFAULT NULL COMMENT '宾语类型',
    confidence  FLOAT        DEFAULT NULL COMMENT '置信度',
    lang        VARCHAR(10)  DEFAULT 'zh' COMMENT '语种',
    tenant_id   BIGINT       NOT NULL COMMENT '租户ID',
    created_by   VARCHAR(50)  DEFAULT NULL,
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by   VARCHAR(50)  DEFAULT NULL,
    updated_at DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag    TINYINT      DEFAULT 0,
    remark      VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='NLP三元组抽取表';

CREATE TABLE IF NOT EXISTS gwy_analyze_nlp_sentiment (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    article_id      BIGINT       NOT NULL COMMENT '文章ID',
    sentiment       VARCHAR(20)  DEFAULT NULL COMMENT '情感：positive/negative/neutral',
    score           FLOAT        DEFAULT NULL COMMENT '情感得分',
    confidence      FLOAT        DEFAULT NULL COMMENT '置信度',
    positive_words  TEXT         DEFAULT NULL COMMENT '正面词列表',
    negative_words  TEXT         DEFAULT NULL COMMENT '负面词列表',
    lang            VARCHAR(10)  DEFAULT 'zh' COMMENT '语种',
    tenant_id       BIGINT       NOT NULL COMMENT '租户ID',
    created_by       VARCHAR(50)  DEFAULT NULL,
    created_at     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by       VARCHAR(50)  DEFAULT NULL,
    updated_at     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag        TINYINT      DEFAULT 0,
    remark          VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='NLP情感分析结果表';

CREATE TABLE IF NOT EXISTS gwy_analyze_alert_rule (
    id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    rule_name      VARCHAR(200) NOT NULL COMMENT '规则名称',
    rule_type      VARCHAR(50)  DEFAULT NULL COMMENT '规则类型：keyword/topic/sentiment/event',
    condition_config TEXT       DEFAULT NULL COMMENT '条件配置JSON',
    alert_level    VARCHAR(20)  DEFAULT 'low' COMMENT '告警级别：low/medium/high/critical',
    alert_channel  VARCHAR(50)  DEFAULT NULL COMMENT '告警渠道：email/feishu/sms',
    alert_target   VARCHAR(500) DEFAULT NULL COMMENT '告警目标',
    enabled        TINYINT      DEFAULT 1 COMMENT '是否启用',
    tenant_id      BIGINT       NOT NULL COMMENT '租户ID',
    created_by      VARCHAR(50)  DEFAULT NULL,
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by      VARCHAR(50)  DEFAULT NULL,
    updated_at    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag       TINYINT      DEFAULT 0,
    remark         VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='告警规则表';

CREATE TABLE IF NOT EXISTS gwy_analyze_nlp_version (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    model_name      VARCHAR(100) NOT NULL COMMENT '模型名称',
    model_version   VARCHAR(50)  DEFAULT NULL COMMENT '模型版本',
    model_type      VARCHAR(50)  DEFAULT NULL COMMENT '模型类型：ner/sentiment/ner_multi',
    model_path      VARCHAR(500) DEFAULT NULL COMMENT '模型路径',
    accuracy        FLOAT        DEFAULT NULL COMMENT '准确率',
    precision_val   FLOAT        DEFAULT NULL COMMENT '精确率',
    recall_val      FLOAT        DEFAULT NULL COMMENT '召回率',
    f1_val          FLOAT        DEFAULT NULL COMMENT 'F1分数',
    train_data_count INT         DEFAULT 0 COMMENT '训练数据量',
    deployed        TINYINT      DEFAULT 0 COMMENT '是否已部署',
    deployed_time   DATETIME     DEFAULT NULL COMMENT '部署时间',
    tenant_id       BIGINT       NOT NULL COMMENT '租户ID',
    created_by       VARCHAR(50)  DEFAULT NULL,
    created_at     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by       VARCHAR(50)  DEFAULT NULL,
    updated_at     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag        TINYINT      DEFAULT 0,
    remark          VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='NLP模型版本表';

CREATE TABLE IF NOT EXISTS gwy_display_rpt_template (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    template_name   VARCHAR(200) NOT NULL COMMENT '模板名称',
    template_code   VARCHAR(100) DEFAULT NULL COMMENT '模板编码',
    template_type   VARCHAR(50)  DEFAULT NULL COMMENT 'daily/weekly/monthly/custom',
    template_content TEXT        DEFAULT NULL COMMENT '模板内容',
    default_params  TEXT         DEFAULT NULL COMMENT '默认参数JSON',
    sort            INT          DEFAULT 0 COMMENT '排序',
    status          TINYINT      DEFAULT 1 COMMENT '状态',
    tenant_id       BIGINT       NOT NULL COMMENT '租户ID',
    created_by       VARCHAR(50)  DEFAULT NULL,
    created_at     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by       VARCHAR(50)  DEFAULT NULL,
    updated_at     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag        TINYINT      DEFAULT 0,
    remark          VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报告模板表';

CREATE TABLE IF NOT EXISTS gwy_display_rpt_report (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    report_name      VARCHAR(500) NOT NULL COMMENT '报告名称',
    report_type      VARCHAR(50)  DEFAULT NULL COMMENT '报告类型',
    template_id      BIGINT       DEFAULT NULL COMMENT '模板ID',
    topic_id         BIGINT       DEFAULT NULL COMMENT '主题ID',
    user_id          BIGINT       DEFAULT NULL COMMENT '生成用户ID',
    time_range_start DATETIME     DEFAULT NULL COMMENT '时间范围开始',
    time_range_end   DATETIME     DEFAULT NULL COMMENT '时间范围结束',
    params           TEXT         DEFAULT NULL COMMENT '生成参数JSON',
    content          TEXT         DEFAULT NULL COMMENT '报告内容',
    file_path        VARCHAR(500) DEFAULT NULL COMMENT '文件路径',
    file_size        BIGINT       DEFAULT 0 COMMENT '文件大小',
    status           VARCHAR(20)  DEFAULT 'draft' COMMENT 'draft/published/archived',
    view_count       INT          DEFAULT 0 COMMENT '浏览次数',
    scope            VARCHAR(20)  DEFAULT 'TENANT' COMMENT '可见范围：TENANT/GLOBAL',
    tenant_id        BIGINT       NOT NULL COMMENT '租户ID',
    created_by        VARCHAR(50)  DEFAULT NULL,
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by        VARCHAR(50)  DEFAULT NULL,
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag         TINYINT      DEFAULT 0,
    remark           VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_tenant_scope (tenant_id, scope)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报告记录表';

CREATE TABLE IF NOT EXISTS gwy_display_alert_record (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    rule_id          BIGINT       DEFAULT NULL COMMENT '规则ID',
    rule_name        VARCHAR(200) DEFAULT NULL COMMENT '规则名称',
    alert_level      VARCHAR(20)  DEFAULT NULL COMMENT '告警级别',
    alert_title      VARCHAR(500) DEFAULT NULL COMMENT '告警标题',
    alert_content    TEXT         DEFAULT NULL COMMENT '告警内容',
    trigger_article_id BIGINT     DEFAULT NULL COMMENT '触发文章ID',
    trigger_keywords VARCHAR(500) DEFAULT NULL COMMENT '触发关键词',
    status           VARCHAR(20)  DEFAULT 'unread' COMMENT 'unread/read/resolved',
    resolved_by      VARCHAR(50)  DEFAULT NULL COMMENT '处理人',
    resolved_time    DATETIME     DEFAULT NULL COMMENT '处理时间',
    resolved_comment TEXT         DEFAULT NULL COMMENT '处理备注',
    tenant_id        BIGINT       NOT NULL COMMENT '租户ID',
    created_by        VARCHAR(50)  DEFAULT NULL,
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by        VARCHAR(50)  DEFAULT NULL,
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag         TINYINT      DEFAULT 0,
    remark           VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='告警记录表';

CREATE TABLE IF NOT EXISTS gwy_display_information_publish (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    publish_title VARCHAR(500) NOT NULL COMMENT '发布标题',
    publish_type VARCHAR(50)  DEFAULT NULL COMMENT '发布类型',
    content      TEXT         DEFAULT NULL COMMENT '内容',
    author_id    BIGINT       DEFAULT NULL COMMENT '作者ID',
    status       VARCHAR(20)  DEFAULT 'draft' COMMENT 'draft/published/archived',
    publish_time DATETIME     DEFAULT NULL COMMENT '发布时间',
    category     VARCHAR(100) DEFAULT NULL COMMENT '分类',
    tenant_id    BIGINT       NOT NULL COMMENT '租户ID',
    created_by    VARCHAR(50)  DEFAULT NULL,
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by    VARCHAR(50)  DEFAULT NULL,
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag     TINYINT      DEFAULT 0,
    remark       VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='信息发布表';

CREATE TABLE IF NOT EXISTS gwy_display_push_record (
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    push_type  VARCHAR(20)  DEFAULT NULL COMMENT '推送类型：email/rss/feishu',
    push_target VARCHAR(200) DEFAULT NULL COMMENT '推送目标',
    article_ids TEXT        DEFAULT NULL COMMENT '文章ID列表JSON',
    report_id  BIGINT       DEFAULT NULL COMMENT '关联报告ID',
    subject    VARCHAR(500) DEFAULT NULL COMMENT '标题',
    content    TEXT         DEFAULT NULL COMMENT '内容',
    status     VARCHAR(20)  DEFAULT 'pending' COMMENT 'pending/success/failed',
    send_time  DATETIME     DEFAULT NULL COMMENT '发送时间',
    error_msg  TEXT         DEFAULT NULL COMMENT '错误信息',
    tenant_id  BIGINT       NOT NULL COMMENT '租户ID',
    created_by  VARCHAR(50)  DEFAULT NULL,
    created_at DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_by  VARCHAR(50)  DEFAULT NULL,
    updated_at DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag   TINYINT      DEFAULT 0,
    remark     VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='推送记录表';

CREATE TABLE IF NOT EXISTS gwy_display_recommendation_record (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id       BIGINT       DEFAULT NULL COMMENT '用户ID',
    rec_type      VARCHAR(50)  DEFAULT NULL COMMENT '推荐类型：article/report/topic/entity',
    rec_target_id BIGINT       DEFAULT NULL COMMENT '推荐目标ID',
    rec_title     VARCHAR(500) DEFAULT NULL COMMENT '推荐标题',
    rec_score     FLOAT        DEFAULT NULL COMMENT '推荐得分',
    rec_reason    VARCHAR(500) DEFAULT NULL COMMENT '推荐理由',
    status        VARCHAR(20)  DEFAULT 'unread' COMMENT 'unread/read/adopted/dismissed',
    tenant_id     BIGINT       NOT NULL COMMENT '租户ID',
    created_by     VARCHAR(50)  DEFAULT NULL,
    created_at   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by     VARCHAR(50)  DEFAULT NULL,
    updated_at   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag      TINYINT      DEFAULT 0,
    remark        VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='推荐记录表';

CREATE TABLE IF NOT EXISTS gwy_display_search_index (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    entity_type VARCHAR(50)  DEFAULT NULL COMMENT '实体类型',
    entity_id   BIGINT       DEFAULT NULL COMMENT '实体ID',
    entity_name VARCHAR(500) DEFAULT NULL COMMENT '实体名称',
    keywords    TEXT         DEFAULT NULL COMMENT '关键词',
    lang        VARCHAR(10)  DEFAULT 'zh' COMMENT '语种',
    index_data  JSON         DEFAULT NULL COMMENT '索引数据',
    tenant_id   BIGINT       NOT NULL COMMENT '租户ID',
    created_by   VARCHAR(50)  DEFAULT NULL,
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by   VARCHAR(50)  DEFAULT NULL,
    updated_at DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag    TINYINT      DEFAULT 0,
    remark      VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='搜索索引表';

CREATE TABLE IF NOT EXISTS gwy_display_conference_info (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    conference_name VARCHAR(200) NOT NULL COMMENT '会议名称',
    start_time      DATETIME     DEFAULT NULL COMMENT '开始时间',
    end_time        DATETIME     DEFAULT NULL COMMENT '结束时间',
    location        VARCHAR(200) DEFAULT NULL COMMENT '地点',
    organizer       VARCHAR(200) DEFAULT NULL COMMENT '主办方',
    participants    TEXT         DEFAULT NULL COMMENT '参会人员JSON',
    description     TEXT         DEFAULT NULL COMMENT '描述',
    status          VARCHAR(20)  DEFAULT 'upcoming' COMMENT 'upcoming/ongoing/ended/cancelled',
    tenant_id       BIGINT       NOT NULL COMMENT '租户ID',
    created_by       VARCHAR(50)  DEFAULT NULL,
    created_at     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by       VARCHAR(50)  DEFAULT NULL,
    updated_at     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag        TINYINT      DEFAULT 0,
    remark          VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会议信息表';

CREATE TABLE IF NOT EXISTS gwy_display_column_manage (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    column_name VARCHAR(100) NOT NULL COMMENT '栏目名称',
    column_code VARCHAR(100) DEFAULT NULL COMMENT '栏目编码',
    table_name  VARCHAR(100) DEFAULT NULL COMMENT '关联表名',
    visible     TINYINT      DEFAULT 1 COMMENT '是否可见',
    sort_order  INT          DEFAULT 0 COMMENT '排序',
    config      TEXT         DEFAULT NULL COMMENT '配置JSON',
    tenant_id   BIGINT       NOT NULL COMMENT '租户ID',
    created_by   VARCHAR(50)  DEFAULT NULL,
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by   VARCHAR(50)  DEFAULT NULL,
    updated_at DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag    TINYINT      DEFAULT 0,
    remark      VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='栏目管理表';

CREATE TABLE IF NOT EXISTS gwy_display_user_module_config (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT       NOT NULL COMMENT '用户ID',
    module_code VARCHAR(100) DEFAULT NULL COMMENT '模块编码',
    module_name VARCHAR(100) DEFAULT NULL COMMENT '模块名称',
    config_data TEXT         DEFAULT NULL COMMENT '配置数据JSON',
    is_visible  TINYINT      DEFAULT 1 COMMENT '是否显示',
    sort_order  INT          DEFAULT 0 COMMENT '排序',
    tenant_id   BIGINT       NOT NULL COMMENT '租户ID',
    created_by   VARCHAR(50)  DEFAULT NULL,
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by   VARCHAR(50)  DEFAULT NULL,
    updated_at DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag    TINYINT      DEFAULT 0,
    remark      VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户模块配置表';

-- ================================================================
-- 初始化完成：33 张 onto_ 表 + 36 张 gwy_ 表 = 69 张（种子数据见 V1.0.1）
-- ================================================================
