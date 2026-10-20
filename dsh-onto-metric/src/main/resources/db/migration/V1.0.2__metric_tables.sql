-- ============================================================
-- dsh-onto-metric 指标体系建表脚本
-- 业务对象 / 业务流程 / 关键决策点 / 指标定义(原子·派生·复合) / 指标依赖
-- ============================================================

CREATE TABLE IF NOT EXISTS onto_metric_business_object (
           id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
           object_name     VARCHAR(200) NOT NULL COMMENT '业务对象名称',
           object_code     VARCHAR(64)  NOT NULL COMMENT '业务对象编码',
           object_type     VARCHAR(32)  DEFAULT NULL COMMENT '对象类型：ENTITY/PROCESS/RESOURCE',
           source_table    VARCHAR(128) DEFAULT NULL COMMENT '数据源表名',
           description     VARCHAR(800) DEFAULT NULL COMMENT '描述',
           status          TINYINT      DEFAULT 0 COMMENT '状态：0=草稿 1=启用 2=停用',
           tenant_id       BIGINT       DEFAULT NULL,
           created_by      VARCHAR(50)  DEFAULT NULL,
           created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
           updated_by      VARCHAR(50)  DEFAULT NULL,
           updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
           del_flag        TINYINT      DEFAULT 0,
           remark          VARCHAR(800) DEFAULT NULL,
           PRIMARY KEY (id),
           CONSTRAINT uk_object_code UNIQUE (object_code)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='业务对象表（被度量的实体类型）';

CREATE TABLE IF NOT EXISTS onto_metric_process (
           id                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
           process_name      VARCHAR(200) NOT NULL COMMENT '流程名称',
           process_code      VARCHAR(64)  NOT NULL COMMENT '流程编码',
           business_object_id BIGINT      DEFAULT NULL COMMENT '关联业务对象ID',
           node_config       TEXT         DEFAULT NULL COMMENT '节点配置（JSON）',
           description       VARCHAR(800) DEFAULT NULL COMMENT '描述',
           status            TINYINT      DEFAULT 0 COMMENT '状态：0=草稿 1=启用 2=停用',
           tenant_id         BIGINT       DEFAULT NULL,
           created_by        VARCHAR(50)  DEFAULT NULL,
           created_at        DATETIME     DEFAULT CURRENT_TIMESTAMP,
           updated_by        VARCHAR(50)  DEFAULT NULL,
           updated_at        DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
           del_flag          TINYINT      DEFAULT 0,
           remark            VARCHAR(800) DEFAULT NULL,
           PRIMARY KEY (id),
           CONSTRAINT uk_process_code UNIQUE (process_code),
           KEY idx_mp_bo (business_object_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='业务流程表';

CREATE TABLE IF NOT EXISTS onto_metric_decision_point (
           id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
           point_name      VARCHAR(200) NOT NULL COMMENT '决策点名称',
           process_id      BIGINT       DEFAULT NULL COMMENT '所属流程ID',
           node_index      INT          DEFAULT 0 COMMENT '节点序号',
           decision_type   VARCHAR(32)  DEFAULT NULL COMMENT '决策类型：APPROVE/REJECT/ROUTE/SCORE',
           description     VARCHAR(800) DEFAULT NULL COMMENT '描述',
           status          TINYINT      DEFAULT 0 COMMENT '状态：0=草稿 1=启用 2=停用',
           tenant_id       BIGINT       DEFAULT NULL,
           created_by      VARCHAR(50)  DEFAULT NULL,
           created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
           updated_by      VARCHAR(50)  DEFAULT NULL,
           updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
           del_flag        TINYINT      DEFAULT 0,
           remark          VARCHAR(800) DEFAULT NULL,
           PRIMARY KEY (id),
           KEY idx_mdp_process (process_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关键决策点表';

CREATE TABLE IF NOT EXISTS onto_metric_definition (
           id                 BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
           metric_name        VARCHAR(200) NOT NULL COMMENT '指标名称',
           metric_code        VARCHAR(64)  NOT NULL COMMENT '指标编码',
           category           VARCHAR(16)  NOT NULL COMMENT '指标类别：ATOM=原子/DERIVED=派生/COMPOSITE=复合',
           business_object_id BIGINT       DEFAULT NULL COMMENT '关联业务对象ID',
           process_id         BIGINT       DEFAULT NULL COMMENT '关联流程ID',
           decision_point_id  BIGINT       DEFAULT NULL COMMENT '关联关键决策点ID',
           formula            TEXT         DEFAULT NULL COMMENT '计算公式（派生/复合指标用）',
           unit               VARCHAR(32)  DEFAULT NULL COMMENT '计量单位',
           description        VARCHAR(800) DEFAULT NULL COMMENT '描述',
           status             TINYINT      DEFAULT 0 COMMENT '状态：0=草稿 1=启用 2=停用',
           tenant_id          BIGINT       DEFAULT NULL,
           created_by         VARCHAR(50)  DEFAULT NULL,
           created_at         DATETIME     DEFAULT CURRENT_TIMESTAMP,
           updated_by         VARCHAR(50)  DEFAULT NULL,
           updated_at         DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
           del_flag           TINYINT      DEFAULT 0,
           remark             VARCHAR(800) DEFAULT NULL,
           PRIMARY KEY (id),
           CONSTRAINT uk_metric_code UNIQUE (metric_code),
           KEY idx_md_category (category),
           KEY idx_md_bo (business_object_id),
           KEY idx_md_process (process_id),
           KEY idx_md_decision (decision_point_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='指标定义表（原子/派生/复合）';

CREATE TABLE IF NOT EXISTS onto_metric_dependency (
           id                   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
           downstream_metric_id BIGINT       NOT NULL COMMENT '下游指标ID（依赖方）',
           upstream_metric_id   BIGINT       NOT NULL COMMENT '上游指标ID（被依赖方）',
           dependency_type      VARCHAR(32)  DEFAULT 'DIRECT' COMMENT '依赖类型：DIRECT/WEIGHTED/CONDITIONAL',
           weight               DECIMAL(10,4) DEFAULT 1.0000 COMMENT '权重（加权依赖用）',
           tenant_id            BIGINT       DEFAULT NULL,
           created_by           VARCHAR(50)  DEFAULT NULL,
           created_at           DATETIME     DEFAULT CURRENT_TIMESTAMP,
           updated_by           VARCHAR(50)  DEFAULT NULL,
           updated_at           DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
           del_flag             TINYINT      DEFAULT 0,
           remark               VARCHAR(800) DEFAULT NULL,
           PRIMARY KEY (id),
           KEY idx_mdep_down (downstream_metric_id),
           KEY idx_mdep_up (upstream_metric_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='指标依赖关系表（依赖树边）';
