package net.linlan.dsh.global.service;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 汇聚服务层，提供全量/增量汇聚、日志查询和统一视图查询
 * <p>双数据源：v_unified_kg 改为应用层聚合，个人层查 siisJdbcTemplate，全局层查 ontoJdbcTemplate。</p>
 * @author Linlan
 * CreatedAt 2026-10-14
 * @version 1.0
 * @since 1.0
 */
@Service
public class AggregationService {

    private final KnowledgeAggregator aggregator;
    private final JdbcTemplate jdbcTemplate;
    private final JdbcTemplate ontoJdbcTemplate;

    public AggregationService(KnowledgeAggregator aggregator, JdbcTemplate jdbcTemplate,
                              @Qualifier("ontoJdbcTemplate") ObjectProvider<JdbcTemplate> ontoJdbcTemplateProvider) {
        this.aggregator = aggregator;
        this.jdbcTemplate = jdbcTemplate;
        this.ontoJdbcTemplate = ontoJdbcTemplateProvider.getIfAvailable();
    }

    /**
     * 手动触发全量汇聚
     */
    public KnowledgeAggregator.AggregationStats triggerFullAggregation() {
        return aggregator.aggregateAll("MANUAL");
    }

    /**
     * 定时触发全量汇聚
     */
    public KnowledgeAggregator.AggregationStats scheduledFullAggregation() {
        return aggregator.aggregateAll("SCHEDULED");
    }

    /**
     * 增量汇聚（事件触发）
     */
    public KnowledgeAggregator.AggregationStats triggerIncrementalAggregation(Long tenantId) {
        return aggregator.aggregateIncremental(tenantId, "EVENT");
    }

    /**
     * 手动触发增量汇聚
     */
    public KnowledgeAggregator.AggregationStats triggerManualIncremental(Long tenantId) {
        return aggregator.aggregateIncremental(tenantId, "MANUAL");
    }

    /**
     * 查询汇聚日志
     */
    public List<Map<String, Object>> getAggregationLogs(int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        String sql = "SELECT * FROM onto_global_aggregation_log WHERE del_flag = 0 ORDER BY created_at DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.queryForList(sql, pageSize, offset);
    }

    /**
     * 查询统一监测视图（参数化内联查询，支持分页/租户过滤/时间窗）
     * <p>P1-14：原 {@code SELECT * FROM v_unified_monitor} 无 LIMIT 且全量计数，
     * 数据量增长后主任首页 P95 无法满足 &lt; 3s。改为应用层参数化查询：
     * <ul>
     *   <li>分页：LIMIT/OFFSET，默认 pageSize=20</li>
     *   <li>租户过滤：可选 tenantId，null 表示全租户</li>
     *   <li>时间窗：recent_article_count 统计近 N 天文章数，cutoff 由 Java 计算保证 H2/MySQL 兼容</li>
     * </ul>
     * @param pageNum 页码（1-based）
     * @param pageSize 每页条数
     * @param tenantId 租户 ID，null 表示全部租户
     * @param timeWindowDays 时间窗天数（用于 recent_article_count），默认 30
     * @return 监测记录列表
     */
    public List<Map<String, Object>> getUnifiedMonitor(int pageNum, int pageSize, Long tenantId, int timeWindowDays) {
        int offset = (pageNum - 1) * pageSize;
        LocalDate cutoffDate = LocalDate.now().minusDays(Math.max(timeWindowDays, 0));

        StringBuilder sql = new StringBuilder(
                "SELECT t.tenant_id, t.id AS topic_id, t.topic_name, t.topic_type, " +
                "t.hotspot_region, t.status AS topic_status, t.alert_enabled, " +
                "COUNT(DISTINCT a.id) AS article_count, " +
                "COUNT(DISTINCT CASE WHEN a.published >= ? THEN a.id END) AS recent_article_count, " +
                "COUNT(DISTINCT ar.id) AS alert_count, " +
                "MAX(a.published) AS latest_article_time " +
                "FROM gwy_analyze_track_topic t " +
                "LEFT JOIN gwy_track_article a ON a.topic_id = t.id AND a.tenant_id = t.tenant_id AND a.del_flag = 0 " +
                "LEFT JOIN gwy_display_alert_record ar ON ar.rule_id IN " +
                "(SELECT id FROM gwy_analyze_alert_rule WHERE FIND_IN_SET(id, t.alert_rule_ids) > 0) " +
                "WHERE t.del_flag = 0");

        List<Object> params = new ArrayList<>();
        params.add(cutoffDate);

        if (tenantId != null) {
            sql.append(" AND t.tenant_id = ?");
            params.add(tenantId);
        }

        sql.append(" GROUP BY t.tenant_id, t.id, t.topic_name, t.topic_type, t.hotspot_region, t.status, t.alert_enabled");
        sql.append(" ORDER BY t.id");
        sql.append(" LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add(offset);

        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    /**
     * 查询统一监测视图（默认分页：第 1 页，每页 20 条，全租户，时间窗 30 天）
     */
    public List<Map<String, Object>> getUnifiedMonitor() {
        return getUnifiedMonitor(1, 20, null, 30);
    }

    /**
     * 查询统一知识图谱（应用层聚合，不依赖跨库视图）
     * <p>个人层查 siisJdbcTemplate.gwy_track_kg_node，全局层查 ontoJdbcTemplate.onto_global_kg_node。</p>
     */
    public List<Map<String, Object>> getUnifiedKg() {
        JdbcTemplate ontoJdbc = (ontoJdbcTemplate != null) ? ontoJdbcTemplate : jdbcTemplate;
        List<Map<String, Object>> result = new ArrayList<>();
        result.addAll(jdbcTemplate.queryForList(
                "SELECT 'PERSONAL' AS source_layer, tenant_id, node_id, node_type, node_name, weight " +
                "FROM gwy_track_kg_node WHERE del_flag = 0"));
        result.addAll(ontoJdbc.queryForList(
                "SELECT 'GLOBAL' AS source_layer, NULL AS tenant_id, CONCAT('G-', id) AS node_id, node_type, node_name, cross_tenant_weight AS weight " +
                "FROM onto_global_kg_node WHERE del_flag = 0"));
        return result;
    }

    /**
     * 查询全局发现实体
     */
    public List<Map<String, Object>> getGlobalEntities(int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return jdbcTemplate.queryForList(
                "SELECT * FROM onto_global_discovered_entity WHERE del_flag = 0 ORDER BY total_mention_count DESC LIMIT ? OFFSET ?",
                pageSize, offset);
    }

    /**
     * 查询全局双边关系
     */
    public List<Map<String, Object>> getGlobalRelations(int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return jdbcTemplate.queryForList(
                "SELECT * FROM onto_global_bilateral_relation WHERE del_flag = 0 ORDER BY sample_count DESC LIMIT ? OFFSET ?",
                pageSize, offset);
    }

    /**
     * 查询全局知识图谱节点
     */
    public List<Map<String, Object>> getGlobalKgNodes(int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return jdbcTemplate.queryForList(
                "SELECT * FROM onto_global_kg_node WHERE del_flag = 0 ORDER BY cross_tenant_weight DESC LIMIT ? OFFSET ?",
                pageSize, offset);
    }

    /**
     * 查询全局知识图谱边
     */
    public List<Map<String, Object>> getGlobalKgEdges(int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return jdbcTemplate.queryForList(
                "SELECT * FROM onto_global_kg_edge WHERE del_flag = 0 ORDER BY cross_tenant_weight DESC LIMIT ? OFFSET ?",
                pageSize, offset);
    }

    /**
     * 主任决策概览
     */
    public Map<String, Object> getDirectorOverview() {
        Map<String, Object> overview = new java.util.HashMap<>();
        overview.put("totalEntities", jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM onto_global_discovered_entity WHERE del_flag = 0", Long.class));
        overview.put("totalRelations", jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM onto_global_bilateral_relation WHERE del_flag = 0", Long.class));
        overview.put("totalKgNodes", jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM onto_global_kg_node WHERE del_flag = 0", Long.class));
        overview.put("totalKgEdges", jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM onto_global_kg_edge WHERE del_flag = 0", Long.class));
        overview.put("totalTenants", jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT tenant_id) FROM gwy_track_article WHERE del_flag = 0", Long.class));
        overview.put("totalArticles", jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM gwy_track_article WHERE del_flag = 0", Long.class));
        overview.put("lastAggregation", getLastAggregationLog());
        return overview;
    }

    private Map<String, Object> getLastAggregationLog() {
        List<Map<String, Object>> logs = jdbcTemplate.queryForList(
                "SELECT * FROM onto_global_aggregation_log WHERE del_flag = 0 ORDER BY created_at DESC LIMIT 1");
        return logs.isEmpty() ? Map.of("status", "none") : logs.get(0);
    }
}
