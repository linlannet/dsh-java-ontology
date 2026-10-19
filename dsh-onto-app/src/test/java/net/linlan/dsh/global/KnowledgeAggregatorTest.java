package net.linlan.dsh.global;

import net.linlan.dsh.common.TenantContext;
import net.linlan.dsh.global.service.AggregationService;
import net.linlan.dsh.global.service.KnowledgeAggregator;
import net.linlan.dsh.DshJavaOntologyApplication;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 三级知识架构汇聚测试
 * @author Linlan
 * CreatedAt 2026-10-14
 * @version 1.0
 * @since 1.0
 */
@SpringBootTest(classes = DshJavaOntologyApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class KnowledgeAggregatorTest {

    @Autowired
    private KnowledgeAggregator aggregator;
    @Autowired
    private AggregationService aggregationService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(1L);
        TenantContext.setRoleLevel(0);
        TenantContext.setUserId(1L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @Order(1)
    @DisplayName("全量汇聚：空源数据时成功")
    void testAggregateAllEmpty() {
        cleanAllSourceData();
        var stats = aggregator.aggregateAll("MANUAL_TEST");
        assertTrue(stats.success);
        assertEquals(0, stats.entityCount);
        assertTrue(stats.durationMs >= 0);
    }

    @Test
    @Order(2)
    @DisplayName("全量汇聚：实体汇聚去重合并")
    void testAggregateDiscoveredEntities() {
        cleanSourceData();
        insertDiscoveredEntity("测试实体A", "PERSON", 3, 1L);
        insertDiscoveredEntity("测试实体A", "PERSON", 5, 2L);
        insertDiscoveredEntity("测试实体B", "ORGANIZATION", 2, 1L);

        int count = aggregator.aggregateDiscoveredEntities();
        assertTrue(count >= 1);

        List<Map<String, Object>> result = jdbcTemplate.queryForList(
                "SELECT * FROM onto_global_discovered_entity WHERE del_flag = 0 ORDER BY total_mention_count DESC");
        assertFalse(result.isEmpty());

        Map<String, Object> entityA = result.stream()
                .filter(r -> "测试实体A".equals(r.get("entity_name")))
                .findFirst().orElse(null);
        assertNotNull(entityA);
        assertEquals(8, ((Number) entityA.get("total_mention_count")).intValue());
    }

    @Test
    @Order(3)
    @DisplayName("全量汇聚：双边关系汇聚")
    void testAggregateBilateralRelations() {
        cleanSourceData();
        insertBilateralRelation("中国", "美国", "贸易", 1L);
        insertBilateralRelation("中国", "美国", "贸易", 2L);
        insertBilateralRelation("中国", "俄罗斯", "能源", 1L);

        int count = aggregator.aggregateBilateralRelations();
        assertTrue(count >= 1);

        List<Map<String, Object>> result = jdbcTemplate.queryForList(
                "SELECT * FROM onto_global_bilateral_relation WHERE del_flag = 0");
        assertFalse(result.isEmpty());

        Map<String, Object> rel = result.stream()
                .filter(r -> "中国".equals(r.get("subject_entity_name"))
                        && "美国".equals(r.get("object_entity_name"))
                        && "贸易".equals(r.get("relation_type")))
                .findFirst().orElse(null);
        assertNotNull(rel);
        assertEquals(2, ((Number) rel.get("sample_count")).intValue());
    }

    @Test
    @Order(4)
    @DisplayName("全量汇聚：知识图谱节点汇聚")
    void testAggregateKgNodes() {
        cleanSourceData();
        insertKgNode("node-1", "测试节点", "PERSON", 1L);
        insertKgNode("node-2", "测试节点", "PERSON", 2L);
        insertKgNode("node-3", "另一节点", "ORGANIZATION", 1L);

        int count = aggregator.aggregateKgNodes();
        assertTrue(count >= 1);

        List<Map<String, Object>> result = jdbcTemplate.queryForList(
                "SELECT * FROM onto_global_kg_node WHERE del_flag = 0");
        assertFalse(result.isEmpty());

        Map<String, Object> node = result.stream()
                .filter(r -> "测试节点".equals(r.get("node_name")))
                .findFirst().orElse(null);
        assertNotNull(node);
        assertEquals(2.0, ((Number) node.get("cross_tenant_weight")).doubleValue(), 0.01);
    }

    @Test
    @Order(5)
    @DisplayName("全量汇聚：知识图谱边汇聚")
    void testAggregateKgEdges() {
        cleanSourceData();
        insertKgEdge("node-1", "node-2", "盟友", 1L);
        insertKgEdge("node-1", "node-2", "盟友", 2L);
        insertKgEdge("node-2", "node-3", "对手", 1L);

        int count = aggregator.aggregateKgEdges();
        assertTrue(count >= 1);

        List<Map<String, Object>> result = jdbcTemplate.queryForList(
                "SELECT * FROM onto_global_kg_edge WHERE del_flag = 0");
        assertFalse(result.isEmpty());

        Map<String, Object> edge = result.stream()
                .filter(r -> "node-1".equals(r.get("source_node_id"))
                        && "node-2".equals(r.get("target_node_id"))
                        && "盟友".equals(r.get("relation_type")))
                .findFirst().orElse(null);
        assertNotNull(edge);
        assertEquals(2.0, ((Number) edge.get("cross_tenant_weight")).doubleValue(), 0.01);
    }

    @Test
    @Order(6)
    @DisplayName("全量汇聚：aggregateAll 综合流程")
    void testAggregateAllFull() {
        cleanSourceData();
        insertDiscoveredEntity("综合实体", "PERSON", 10, 1L);
        insertBilateralRelation("法国", "德国", "合作", 1L);
        insertKgNode("kg-1", "综合节点", "COUNTRY", 1L);
        insertKgEdge("kg-1", "kg-2", "邻接", 1L);

        var stats = aggregator.aggregateAll("MANUAL");
        assertTrue(stats.success);
        assertTrue(stats.entityCount >= 1);
        assertTrue(stats.durationMs >= 0);
    }

    @Test
    @Order(7)
    @DisplayName("增量汇聚：指定租户的新数据")
    void testAggregateIncremental() {
        cleanSourceData();
        insertDiscoveredEntity("增量实体", "PERSON", 5, 1L);
        insertBilateralRelation("日本", "韩国", "贸易", 1L);
        insertKgNode("inc-1", "增量节点", "COUNTRY", 1L);
        insertKgEdge("inc-1", "inc-2", "合作", 1L);

        var stats = aggregator.aggregateIncremental(1L, "EVENT");
        assertTrue(stats.success);
        assertTrue(stats.entityCount >= 1);
    }

    @Test
    @Order(8)
    @DisplayName("汇聚日志：记录查询")
    void testAggregationLogs() {
        aggregator.aggregateAll("MANUAL_LOG_TEST");
        List<Map<String, Object>> logs = aggregationService.getAggregationLogs(1, 10);
        assertFalse(logs.isEmpty());
        Map<String, Object> latest = logs.get(0);
        assertNotNull(latest.get("aggregation_type"));
        assertNotNull(latest.get("status"));
    }

    @Test
    @Order(9)
    @DisplayName("统一/全局视图：统一监测视图查询")
    void testUnifiedMonitor() {
        List<Map<String, Object>> result = aggregationService.getUnifiedMonitor();
        assertNotNull(result);
    }

    @Test
    @Order(9)
    @DisplayName("P1-14：统一监测视图参数化查询（分页/租户过滤/时间窗）")
    void testUnifiedMonitorParameterized() {
        jdbcTemplate.update("DELETE FROM gwy_track_article WHERE article_hash LIKE 'p1mon-%'");
        jdbcTemplate.update("DELETE FROM gwy_analyze_track_topic WHERE topic_name LIKE 'P1MON-%'");

        jdbcTemplate.update(
                "INSERT INTO gwy_analyze_track_topic (topic_name, topic_type, hotspot_region, status, alert_enabled, tenant_id, created_by) " +
                "VALUES ('P1MON-T1', 'EVENT', '区域A', 'active', 0, 1, 'test')");
        Long topic1Id = jdbcTemplate.queryForObject(
                "SELECT id FROM gwy_analyze_track_topic WHERE topic_name = 'P1MON-T1'", Long.class);
        jdbcTemplate.update(
                "INSERT INTO gwy_analyze_track_topic (topic_name, topic_type, hotspot_region, status, alert_enabled, tenant_id, created_by) " +
                "VALUES ('P1MON-T2', 'EVENT', '区域B', 'active', 0, 1, 'test')");
        Long topic2Id = jdbcTemplate.queryForObject(
                "SELECT id FROM gwy_analyze_track_topic WHERE topic_name = 'P1MON-T2'", Long.class);
        jdbcTemplate.update(
                "INSERT INTO gwy_analyze_track_topic (topic_name, topic_type, hotspot_region, status, alert_enabled, tenant_id, created_by) " +
                "VALUES ('P1MON-T3', 'EVENT', '区域C', 'active', 0, 2, 'test')");

        jdbcTemplate.update(
                "INSERT INTO gwy_track_article (article_hash, title, topic_id, published, tenant_id, created_by) " +
                "VALUES ('p1mon-a1', '近期文章1', ?, NOW(), 1, 'test')", topic1Id);
        jdbcTemplate.update(
                "INSERT INTO gwy_track_article (article_hash, title, topic_id, published, tenant_id, created_by) " +
                "VALUES ('p1mon-a2', '旧文章1', ?, '2020-01-01 00:00:00', 1, 'test')", topic1Id);
        jdbcTemplate.update(
                "INSERT INTO gwy_track_article (article_hash, title, topic_id, published, tenant_id, created_by) " +
                "VALUES ('p1mon-a3', '近期文章2', ?, NOW(), 1, 'test')", topic2Id);

        try {
            List<Map<String, Object>> all = aggregationService.getUnifiedMonitor(1, 20, null, 30);
            assertNotNull(all);
            assertTrue(all.size() >= 3, "应至少有 3 条主题记录");

            List<Map<String, Object>> p1 = aggregationService.getUnifiedMonitor(1, 1, null, 30);
            assertEquals(1, p1.size(), "pageSize=1 应返回 1 条");
            List<Map<String, Object>> p2 = aggregationService.getUnifiedMonitor(2, 1, null, 30);
            assertEquals(1, p2.size(), "第 2 页应返回 1 条");
            assertNotEquals(p1.get(0).get("topic_id"), p2.get(0).get("topic_id"), "两页应不同主题");

            List<Map<String, Object>> tenant2 = aggregationService.getUnifiedMonitor(1, 20, 2L, 30);
            assertTrue(tenant2.stream().allMatch(r -> Long.valueOf(2L).equals(r.get("tenant_id"))),
                    "租户过滤应只返回 tenant_id=2");

            Map<String, Object> topic1Row = all.stream()
                    .filter(r -> topic1Id.equals(r.get("topic_id")))
                    .findFirst().orElseThrow();
            assertEquals(2L, ((Number) topic1Row.get("article_count")).longValue(), "topic1 总文章数=2");
            assertEquals(1L, ((Number) topic1Row.get("recent_article_count")).longValue(), "topic1 近 30 天文章数=1");
            assertNotNull(topic1Row.get("latest_article_time"), "latest_article_time 不应为 null");
        } finally {
            jdbcTemplate.update("DELETE FROM gwy_track_article WHERE article_hash LIKE 'p1mon-%'");
            jdbcTemplate.update("DELETE FROM gwy_analyze_track_topic WHERE topic_name LIKE 'P1MON-%'");
        }
    }

    @Test
    @Order(10)
    @DisplayName("统一/全局视图：统一知识图谱视图查询")
    void testUnifiedKg() {
        cleanSourceData();
        insertKgNode("view-node", "视图节点", "PERSON", 1L);
        aggregator.aggregateKgNodes();

        List<Map<String, Object>> result = aggregationService.getUnifiedKg();
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @Order(11)
    @DisplayName("全局查询：实体/关系/图谱分页")
    void testGlobalQueries() {
        cleanSourceData();
        insertDiscoveredEntity("分页实体", "PERSON", 1, 1L);
        insertBilateralRelation("A国", "B国", "关系", 1L);
        insertKgNode("page-1", "分页节点", "COUNTRY", 1L);
        insertKgEdge("page-1", "page-2", "连接", 1L);
        aggregator.aggregateAll("MANUAL");

        List<Map<String, Object>> entities = aggregationService.getGlobalEntities(1, 10);
        assertNotNull(entities);

        List<Map<String, Object>> relations = aggregationService.getGlobalRelations(1, 10);
        assertNotNull(relations);

        List<Map<String, Object>> nodes = aggregationService.getGlobalKgNodes(1, 10);
        assertNotNull(nodes);

        List<Map<String, Object>> edges = aggregationService.getGlobalKgEdges(1, 10);
        assertNotNull(edges);
    }

    @Test
    @Order(12)
    @DisplayName("主任概览：统计数据完整")
    void testDirectorOverview() {
        cleanSourceData();
        insertDiscoveredEntity("概览实体", "PERSON", 1, 1L);
        aggregator.aggregateAll("MANUAL");

        Map<String, Object> overview = aggregationService.getDirectorOverview();
        assertNotNull(overview);
        assertNotNull(overview.get("totalEntities"));
        assertNotNull(overview.get("totalRelations"));
        assertNotNull(overview.get("totalKgNodes"));
        assertNotNull(overview.get("totalKgEdges"));
        assertNotNull(overview.get("lastAggregation"));
    }

    @Test
    @Order(13)
    @DisplayName("增量汇聚：空租户数据时成功且统计为零")
    void testAggregateIncrementalEmpty() {
        cleanSourceData();
        var stats = aggregator.aggregateIncremental(999L, "EVENT");
        assertTrue(stats.success);
        assertEquals(0, stats.entityCount);
    }

    @Test
    @Order(14)
    @DisplayName("汇聚统计：AggregationStats 默认值")
    void testAggregationStatsDefaults() {
        var stats = new KnowledgeAggregator.AggregationStats();
        assertEquals(0, stats.entityCount);
        assertEquals(0, stats.relationCount);
        assertEquals(0, stats.durationMs);
        assertFalse(stats.success);
        assertNull(stats.errorMsg);
    }

    @Test
    @Order(15)
    @DisplayName("服务层：手动/定时/事件触发汇聚")
    void testServiceTriggers() {
        cleanSourceData();
        insertDiscoveredEntity("触发实体", "PERSON", 1, 1L);

        var manual = aggregationService.triggerFullAggregation();
        assertTrue(manual.success);

        var scheduled = aggregationService.scheduledFullAggregation();
        assertTrue(scheduled.success);

        var incremental = aggregationService.triggerIncrementalAggregation(1L);
        assertTrue(incremental.success);

        var manualInc = aggregationService.triggerManualIncremental(1L);
        assertTrue(manualInc.success);
    }

    private void cleanSourceData() {
        jdbcTemplate.update("DELETE FROM onto_global_discovered_entity");
        jdbcTemplate.update("DELETE FROM onto_global_bilateral_relation");
        jdbcTemplate.update("DELETE FROM onto_global_kg_node");
        jdbcTemplate.update("DELETE FROM onto_global_kg_edge");
        jdbcTemplate.update("DELETE FROM onto_global_aggregation_log");
        jdbcTemplate.update("DELETE FROM onto_common_discovered_entity WHERE entity_name LIKE '测试%' OR entity_name LIKE '综合%' OR entity_name LIKE '增量%' OR entity_name LIKE '分页%' OR entity_name LIKE '概览%' OR entity_name LIKE '触发%'");
        jdbcTemplate.update("DELETE FROM gwy_ontology_bilateral_relation WHERE country_a IN ('中国','法国','日本','A国')");
        jdbcTemplate.update("DELETE FROM gwy_track_kg_node WHERE node_id LIKE 'node-%' OR node_id LIKE 'kg-%' OR node_id LIKE 'inc-%' OR node_id LIKE 'page-%' OR node_id LIKE 'view-%'");
        jdbcTemplate.update("DELETE FROM gwy_track_kg_edge WHERE source_node_id LIKE 'node-%' OR source_node_id LIKE 'kg-%' OR source_node_id LIKE 'inc-%' OR source_node_id LIKE 'page-%'");
    }

    private void cleanAllSourceData() {
        jdbcTemplate.update("DELETE FROM onto_global_discovered_entity");
        jdbcTemplate.update("DELETE FROM onto_global_bilateral_relation");
        jdbcTemplate.update("DELETE FROM onto_global_kg_node");
        jdbcTemplate.update("DELETE FROM onto_global_kg_edge");
        jdbcTemplate.update("DELETE FROM onto_global_aggregation_log");
        jdbcTemplate.update("DELETE FROM onto_common_discovered_entity");
        jdbcTemplate.update("DELETE FROM gwy_ontology_bilateral_relation WHERE country_a IN ('中国','法国','日本','A国')");
        jdbcTemplate.update("DELETE FROM gwy_track_kg_node WHERE node_id LIKE 'node-%' OR node_id LIKE 'kg-%' OR node_id LIKE 'inc-%' OR node_id LIKE 'page-%' OR node_id LIKE 'view-%'");
        jdbcTemplate.update("DELETE FROM gwy_track_kg_edge WHERE source_node_id LIKE 'node-%' OR source_node_id LIKE 'kg-%' OR source_node_id LIKE 'inc-%' OR source_node_id LIKE 'page-%'");
    }

    private void insertDiscoveredEntity(String name, String type, int mentionCount, Long tenantId) {
        jdbcTemplate.update(
                "INSERT INTO onto_common_discovered_entity (entity_name, entity_type, mention_count, first_seen_time, tenant_id, created_by) " +
                        "VALUES (?, ?, ?, NOW(), ?, 'test')",
                name, type, mentionCount, tenantId);
    }

    private void insertBilateralRelation(String countryA, String countryB, String relationType, Long tenantId) {
        jdbcTemplate.update(
                "INSERT INTO gwy_ontology_bilateral_relation (country_a, country_b, relation_type, tenant_id, created_by) " +
                        "VALUES (?, ?, ?, ?, 'test')",
                countryA, countryB, relationType, tenantId);
    }

    private void insertKgNode(String nodeId, String nodeName, String nodeType, Long tenantId) {
        jdbcTemplate.update(
                "INSERT INTO gwy_track_kg_node (node_id, node_name, node_type, tenant_id, created_by) " +
                        "VALUES (?, ?, ?, ?, 'test')",
                nodeId, nodeName, nodeType, tenantId);
    }

    private void insertKgEdge(String source, String target, String relationType, Long tenantId) {
        jdbcTemplate.update(
                "INSERT INTO gwy_track_kg_edge (source_node_id, target_node_id, relation_type, tenant_id, created_by) " +
                        "VALUES (?, ?, ?, ?, 'test')",
                source, target, relationType, tenantId);
    }
}
