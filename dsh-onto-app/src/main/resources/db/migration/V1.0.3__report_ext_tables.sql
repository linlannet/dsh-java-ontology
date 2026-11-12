-- ============================================================
-- D-R1：R 域平台层补建（DB-2 方案 B）
-- 将告警记录 / 信息发布 / 推送记录 / 推荐记录 / 搜索索引
-- 由项目层 gwy_display_* 收敛为平台层 onto_report_*，
-- 与 Python 版 dsh-ontology 保持同名同构。
-- 对应需求：PLT-R2-2 / PLT-R3-1 / PLT-R3-2 / PLT-R4-1 / PLT-R4-2
-- 设计依据：ai-pilot/poc/TASK-202611-001/db-design.md 4.6
-- ============================================================

-- 告警记录（PLT-R2-2）
CREATE TABLE IF NOT EXISTS onto_report_alert_record (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id       BIGINT       NOT NULL COMMENT '租户ID',
    topic_id        BIGINT       DEFAULT NULL COMMENT '关联主题',
    alert_name      VARCHAR(200) NOT NULL COMMENT '告警名称',
    alert_type      VARCHAR(50)  DEFAULT NULL COMMENT '告警类型',
    alert_level     VARCHAR(20)  NOT NULL DEFAULT 'INFO' COMMENT '级别：INFO/WARN/URGENT',
    content         TEXT         DEFAULT NULL COMMENT '告警内容',
    entity_ids      TEXT         DEFAULT NULL COMMENT '关联实体ID（JSON数组）',
    status          VARCHAR(20)  NOT NULL DEFAULT 'OPEN' COMMENT '状态：OPEN/ACK/CLOSED',
    triggered_at    DATETIME     DEFAULT NULL COMMENT '触发时间',
    handled_by      VARCHAR(50)  DEFAULT NULL COMMENT '处理人',
    handled_at      DATETIME     DEFAULT NULL COMMENT '处理时间',
    created_by      VARCHAR(50)  DEFAULT NULL,
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by      VARCHAR(50)  DEFAULT NULL,
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag        TINYINT      DEFAULT 0,
    remark          VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_ar_tenant_topic (tenant_id, topic_id),
    KEY idx_ar_level_status (alert_level, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报告域-告警记录';

-- 信息发布（PLT-R3-1）
CREATE TABLE IF NOT EXISTS onto_report_information_publish (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id       BIGINT       NOT NULL COMMENT '租户ID',
    report_id       BIGINT       DEFAULT NULL COMMENT '关联报告实例',
    title           VARCHAR(200) NOT NULL COMMENT '标题',
    content         TEXT         DEFAULT NULL COMMENT '正文',
    publish_channel VARCHAR(50)  DEFAULT NULL COMMENT '渠道：WEB/MAIL/APP/OTHER',
    publish_scope   VARCHAR(20)  NOT NULL DEFAULT 'TENANT' COMMENT '范围：TENANT/GLOBAL',
    publish_status  VARCHAR(20)  NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT/PUBLISHED/WITHDRAWN',
    published_at    DATETIME     DEFAULT NULL COMMENT '发布时间',
    publisher_id    BIGINT       DEFAULT NULL COMMENT '发布人',
    created_by      VARCHAR(50)  DEFAULT NULL,
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by      VARCHAR(50)  DEFAULT NULL,
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag        TINYINT      DEFAULT 0,
    remark          VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_ip_tenant_status (tenant_id, publish_status),
    KEY idx_ip_report (report_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报告域-信息发布';

-- 推送记录（PLT-R3-2）
CREATE TABLE IF NOT EXISTS onto_report_push_record (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id       BIGINT       NOT NULL COMMENT '租户ID',
    source_type     VARCHAR(30)  NOT NULL COMMENT '来源类型：REPORT/ALERT',
    source_id       BIGINT       NOT NULL COMMENT '来源ID',
    channel         VARCHAR(50)  NOT NULL COMMENT '渠道：WEB/MAIL/SMS/APP',
    recipient       VARCHAR(200) DEFAULT NULL COMMENT '接收者标识（脱敏存储）',
    payload         TEXT         DEFAULT NULL COMMENT '推送载荷',
    delivery_status VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/SENT/FAILED',
    error_msg       VARCHAR(500) DEFAULT NULL COMMENT '失败原因',
    sent_at         DATETIME     DEFAULT NULL COMMENT '发送时间',
    created_by      VARCHAR(50)  DEFAULT NULL,
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by      VARCHAR(50)  DEFAULT NULL,
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag        TINYINT      DEFAULT 0,
    remark          VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_pr_tenant_channel (tenant_id, channel),
    KEY idx_pr_status_time (delivery_status, created_at),
    KEY idx_pr_source (source_type, source_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报告域-推送记录';

-- 推荐记录（PLT-R4-1）
CREATE TABLE IF NOT EXISTS onto_report_recommendation_record (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id       BIGINT       NOT NULL COMMENT '租户ID',
    user_id         BIGINT       NOT NULL COMMENT '用户ID',
    target_type     VARCHAR(30)  NOT NULL COMMENT '目标类型：REPORT/TOPIC/ONTOLOGY_ENTITY',
    target_id       BIGINT       NOT NULL COMMENT '目标ID',
    score           FLOAT        NOT NULL DEFAULT 0 COMMENT '推荐得分',
    reason          VARCHAR(500) DEFAULT NULL COMMENT '推荐理由',
    algorithm       VARCHAR(50)  DEFAULT NULL COMMENT '算法标识',
    exposed         TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已曝光',
    clicked         TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已点击',
    created_by      VARCHAR(50)  DEFAULT NULL,
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by      VARCHAR(50)  DEFAULT NULL,
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag        TINYINT      DEFAULT 0,
    remark          VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_rr_tenant_user (tenant_id, user_id),
    KEY idx_rr_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报告域-推荐记录';

-- 搜索索引（PLT-R4-2）
CREATE TABLE IF NOT EXISTS onto_report_search_index (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id       BIGINT       NOT NULL COMMENT '租户ID',
    index_type      VARCHAR(30)  NOT NULL COMMENT '索引类型：REPORT/ALERT/DOC',
    target_id       BIGINT       NOT NULL COMMENT '目标ID',
    keywords        TEXT         DEFAULT NULL COMMENT '关键词（分词结果）',
    content         TEXT         DEFAULT NULL COMMENT '索引正文',
    weight          FLOAT        NOT NULL DEFAULT 1 COMMENT '权重',
    index_status    VARCHAR(20)  NOT NULL DEFAULT 'VALID' COMMENT '状态：VALID/STALE/REBUILDING',
    rebuilt_at      DATETIME     DEFAULT NULL COMMENT '重建时间',
    created_by      VARCHAR(50)  DEFAULT NULL,
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by      VARCHAR(50)  DEFAULT NULL,
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag        TINYINT      DEFAULT 0,
    remark          VARCHAR(800) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_si_index_target (index_type, target_id),
    KEY idx_si_tenant_type (tenant_id, index_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报告域-搜索索引';
