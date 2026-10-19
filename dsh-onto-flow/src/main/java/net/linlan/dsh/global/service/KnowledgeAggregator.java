package net.linlan.dsh.global.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 知识汇聚器，将各租户个人层知识汇聚到全局统一层
 * <p>三级知识架构：个人层（tenant_id 隔离）→ 共享层（无隔离）→ 统一层（2 视图，仅主任）</p>
 * @author Linlan
 * CreatedAt 2026-10-14
 * @version 1.0
 * @since 1.0
 */
@Component
public class KnowledgeAggregator {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeAggregator.class);

    private final JdbcTemplate jdbcTemplate;

    public KnowledgeAggregator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 全量汇聚：实体 + 关系 + 图谱
     */
    public AggregationStats aggregateAll(String triggerType) {
        long start = System.currentTimeMillis();
        log.info("Starting full aggregation (trigger={})", triggerType);
        AggregationStats stats = new AggregationStats();
        try {
            stats.entityCount = aggregateDiscoveredEntities();
            stats.relationCount = aggregateBilateralRelations();
            aggregateKgNodes();
            aggregateKgEdges();
            stats.durationMs = System.currentTimeMillis() - start;
            stats.success = true;
            logAggregation("FULL", triggerType, stats);
            log.info("Full aggregation completed: {} entities, {} relations in {}ms",
                    stats.entityCount, stats.relationCount, stats.durationMs);
        } catch (Exception e) {
            stats.durationMs = System.currentTimeMillis() - start;
            stats.success = false;
            stats.errorMsg = e.getMessage();
            logAggregation("FULL", triggerType, stats);
            log.error("Full aggregation failed", e);
        }
        return stats;
    }

    /**
     * 增量汇聚：仅汇聚指定租户的新数据
     */
    public AggregationStats aggregateIncremental(Long tenantId, String triggerType) {
        long start = System.currentTimeMillis();
        log.info("Starting incremental aggregation for tenant {} (trigger={})", tenantId, triggerType);
        AggregationStats stats = new AggregationStats();
        try {
            stats.entityCount = aggregateDiscoveredEntitiesForTenant(tenantId);
            stats.relationCount = aggregateBilateralRelationsForTenant(tenantId);
            aggregateKgNodesForTenant(tenantId);
            aggregateKgEdgesForTenant(tenantId);
            stats.durationMs = System.currentTimeMillis() - start;
            stats.success = true;
            logAggregation("INCREMENTAL", triggerType, stats);
            log.info("Incremental aggregation for tenant {} completed: {} entities, {} relations in {}ms",
                    tenantId, stats.entityCount, stats.relationCount, stats.durationMs);
        } catch (Exception e) {
            stats.durationMs = System.currentTimeMillis() - start;
            stats.success = false;
            stats.errorMsg = e.getMessage();
            logAggregation("INCREMENTAL", triggerType, stats);
            log.error("Incremental aggregation for tenant {} failed", tenantId, e);
        }
        return stats;
    }

    /**
     * 汇聚发现实体：从 onto_common_discovered_entity → onto_global_discovered_entity（去重 + sum mentionCount）
     */
    public int aggregateDiscoveredEntities() {
        String sql = "INSERT INTO onto_global_discovered_entity (entity_name, entity_type, tenant_ids, total_mention_count, first_seen_time, last_seen_time, created_by) " +
                "SELECT d.entity_name, d.entity_type, " +
                "CONCAT('[', GROUP_CONCAT(DISTINCT d.tenant_id), ']') AS tenant_ids, " +
                "SUM(d.mention_count) AS total_mention_count, " +
                "MIN(d.first_seen_time) AS first_seen_time, " +
                "MAX(d.updated_at) AS last_seen_time, " +
                "'aggregator' " +
                "FROM onto_common_discovered_entity d " +
                "WHERE d.del_flag = 0 " +
                "GROUP BY d.entity_name, d.entity_type " +
                "ON DUPLICATE KEY UPDATE " +
                "tenant_ids = VALUES(tenant_ids), " +
                "total_mention_count = VALUES(total_mention_count), " +
                "last_seen_time = VALUES(last_seen_time), " +
                "updated_at = NOW()";
        return jdbcTemplate.update(sql);
    }

    /**
     * 汇聚双边关系：从 gwy_ontology_bilateral_relation → onto_global_bilateral_relation
     */
    public int aggregateBilateralRelations() {
        String sql = "INSERT INTO onto_global_bilateral_relation (subject_entity_name, object_entity_name, relation_type, tenant_ids, avg_score, sample_count, created_by) " +
                "SELECT r.country_a, r.country_b, r.relation_type, " +
                "CONCAT('[', GROUP_CONCAT(DISTINCT r.tenant_id), ']') AS tenant_ids, " +
                "1.0 AS avg_score, " +
                "COUNT(*) AS sample_count, " +
                "'aggregator' " +
                "FROM gwy_ontology_bilateral_relation r " +
                "WHERE r.del_flag = 0 " +
                "GROUP BY r.country_a, r.country_b, r.relation_type " +
                "ON DUPLICATE KEY UPDATE " +
                "tenant_ids = VALUES(tenant_ids), " +
                "avg_score = VALUES(avg_score), " +
                "sample_count = VALUES(sample_count), " +
                "updated_at = NOW()";
        return jdbcTemplate.update(sql);
    }

    /**
     * 汇聚知识图谱节点：从 gwy_track_kg_node → onto_global_kg_node
     */
    public int aggregateKgNodes() {
        String sql = "INSERT INTO onto_global_kg_node (node_name, node_type, tenant_ids, cross_tenant_weight, created_by) " +
                "SELECT n.node_name, n.node_type, " +
                "CONCAT('[', GROUP_CONCAT(DISTINCT n.tenant_id), ']') AS tenant_ids, " +
                "COUNT(DISTINCT n.tenant_id) AS cross_tenant_weight, " +
                "'aggregator' " +
                "FROM gwy_track_kg_node n " +
                "WHERE n.del_flag = 0 " +
                "GROUP BY n.node_name, n.node_type " +
                "ON DUPLICATE KEY UPDATE " +
                "tenant_ids = VALUES(tenant_ids), " +
                "cross_tenant_weight = VALUES(cross_tenant_weight), " +
                "updated_at = NOW()";
        return jdbcTemplate.update(sql);
    }

    /**
     * 汇聚知识图谱边：从 gwy_track_kg_edge → onto_global_kg_edge
     */
    public int aggregateKgEdges() {
        String sql = "INSERT INTO onto_global_kg_edge (source_node_id, target_node_id, relation_type, tenant_ids, cross_tenant_weight, created_by) " +
                "SELECT e.source_node_id, e.target_node_id, e.relation_type, " +
                "CONCAT('[', GROUP_CONCAT(DISTINCT e.tenant_id), ']') AS tenant_ids, " +
                "COUNT(DISTINCT e.tenant_id) AS cross_tenant_weight, " +
                "'aggregator' " +
                "FROM gwy_track_kg_edge e " +
                "WHERE e.del_flag = 0 " +
                "GROUP BY e.source_node_id, e.target_node_id, e.relation_type " +
                "ON DUPLICATE KEY UPDATE " +
                "tenant_ids = VALUES(tenant_ids), " +
                "cross_tenant_weight = VALUES(cross_tenant_weight), " +
                "updated_at = NOW()";
        return jdbcTemplate.update(sql);
    }

    private int aggregateDiscoveredEntitiesForTenant(Long tenantId) {
        String sql = "INSERT INTO onto_global_discovered_entity (entity_name, entity_type, tenant_ids, total_mention_count, first_seen_time, last_seen_time, created_by) " +
                "SELECT d.entity_name, d.entity_type, " +
                "CONCAT('[', d.tenant_id, ']'), " +
                "d.mention_count, " +
                "d.first_seen_time, " +
                "d.updated_at, " +
                "'aggregator' " +
                "FROM onto_common_discovered_entity d " +
                "WHERE d.del_flag = 0 AND d.tenant_id = ? " +
                "ON DUPLICATE KEY UPDATE " +
                "total_mention_count = total_mention_count + VALUES(total_mention_count), " +
                "last_seen_time = GREATEST(last_seen_time, VALUES(last_seen_time)), " +
                "updated_at = NOW()";
        return jdbcTemplate.update(sql, tenantId);
    }

    private int aggregateBilateralRelationsForTenant(Long tenantId) {
        String sql = "INSERT INTO onto_global_bilateral_relation (subject_entity_name, object_entity_name, relation_type, tenant_ids, avg_score, sample_count, created_by) " +
                "SELECT r.country_a, r.country_b, r.relation_type, " +
                "CONCAT('[', r.tenant_id, ']'), " +
                "1.0, 1, 'aggregator' " +
                "FROM gwy_ontology_bilateral_relation r " +
                "WHERE r.del_flag = 0 AND r.tenant_id = ? " +
                "ON DUPLICATE KEY UPDATE " +
                "avg_score = (avg_score * sample_count + VALUES(avg_score)) / (sample_count + 1), " +
                "sample_count = sample_count + 1, " +
                "updated_at = NOW()";
        return jdbcTemplate.update(sql, tenantId);
    }

    private int aggregateKgNodesForTenant(Long tenantId) {
        String sql = "INSERT INTO onto_global_kg_node (node_name, node_type, tenant_ids, cross_tenant_weight, created_by) " +
                "SELECT n.node_name, n.node_type, CONCAT('[', n.tenant_id, ']'), 1, 'aggregator' " +
                "FROM gwy_track_kg_node n " +
                "WHERE n.del_flag = 0 AND n.tenant_id = ? " +
                "GROUP BY n.node_name, n.node_type " +
                "ON DUPLICATE KEY UPDATE " +
                "cross_tenant_weight = cross_tenant_weight + 1, " +
                "updated_at = NOW()";
        return jdbcTemplate.update(sql, tenantId);
    }

    private int aggregateKgEdgesForTenant(Long tenantId) {
        String sql = "INSERT INTO onto_global_kg_edge (source_node_id, target_node_id, relation_type, tenant_ids, cross_tenant_weight, created_by) " +
                "SELECT e.source_node_id, e.target_node_id, e.relation_type, CONCAT('[', e.tenant_id, ']'), 1, 'aggregator' " +
                "FROM gwy_track_kg_edge e " +
                "WHERE e.del_flag = 0 AND e.tenant_id = ? " +
                "GROUP BY e.source_node_id, e.target_node_id, e.relation_type " +
                "ON DUPLICATE KEY UPDATE " +
                "cross_tenant_weight = cross_tenant_weight + 1, " +
                "updated_at = NOW()";
        return jdbcTemplate.update(sql, tenantId);
    }

    private void logAggregation(String aggType, String triggerType, AggregationStats stats) {
        try {
            String sql = "INSERT INTO onto_global_aggregation_log (aggregation_type, trigger_type, entity_count, relation_count, duration_ms, status, created_by, remark) " +
                    "VALUES (?, ?, ?, ?, ?, ?, 'aggregator', ?)";
            jdbcTemplate.update(sql, aggType, triggerType, stats.entityCount, stats.relationCount,
                    stats.durationMs, stats.success ? "success" : "failed",
                    stats.errorMsg != null ? stats.errorMsg.substring(0, Math.min(stats.errorMsg.length(), 500)) : null);
        } catch (Exception e) {
            log.warn("Failed to log aggregation: {}", e.getMessage());
        }
    }

    /**
     * 汇聚统计结果
     */
    public static class AggregationStats {
        public int entityCount = 0;
        public int relationCount = 0;
        public long durationMs = 0;
        public boolean success = false;
        public String errorMsg = null;
    }
}
