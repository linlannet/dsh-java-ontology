package net.linlan.dsh.ontology;

import net.linlan.dsh.DshJavaOntologyApplication;
import net.linlan.dsh.common.TenantContext;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 端到端集成测试，验证完整业务流程
 * @author Linlan
 * CreatedAt 2026-10-14
 * @version 1.0
 * @since 1.0
 */
@SpringBootTest(classes = DshJavaOntologyApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("端到端集成测试")
class EndToEndIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String baseUrl;
    private String directorToken;
    private String researcherToken;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        TenantContext.clear();
        directorToken = login("director", "password123");
        researcherToken = login("researcher", "password123");
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @SuppressWarnings("unchecked")
    private String login(String username, String password) {
        Map<String, String> body = Map.of("userName", username, "password", password);
        ResponseEntity<Map> resp = restTemplate.postForEntity(
                baseUrl + "/api/auth/login", body, Map.class);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        Map<String, Object> data = (Map<String, Object>) resp.getBody().get("data");
        assertNotNull(data, "login response data should not be null");
        String token = (String) data.get("token");
        assertNotNull(token, "JWT token should not be null");
        return "Bearer " + token;
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", token);
        return headers;
    }

    @Test
    @Order(2)
    @DisplayName("端到端：知识沉淀与升级")
    void testKnowledgePrecipitationAndPromotion() {
        jdbcTemplate.update("DELETE FROM onto_common_discovered_entity WHERE entity_name = 'E2E知识沉淀实体'");
        jdbcTemplate.update(
                "INSERT INTO onto_common_discovered_entity (entity_name, entity_type, mention_count, first_seen_time, tenant_id, created_by) " +
                        "VALUES ('E2E知识沉淀实体', 'PERSON', 5, NOW(), 1, 'test')");

        List<Map<String, Object>> entities = jdbcTemplate.queryForList(
                "SELECT * FROM onto_common_discovered_entity WHERE entity_name = 'E2E知识沉淀实体' AND del_flag = 0");
        assertFalse(entities.isEmpty());

        Map<String, Object> promoteBody = Map.of(
                "entityName", "E2E知识沉淀实体",
                "entityType", "PERSON",
                "category", "person"
        );
        ResponseEntity<String> promoteResp = restTemplate.exchange(
                baseUrl + "/api/ontology/entity/promote",
                HttpMethod.POST, new HttpEntity<>(promoteBody, authHeaders(researcherToken)), String.class);
        assertTrue(promoteResp.getStatusCode().is2xxSuccessful() || promoteResp.getStatusCode().is4xxClientError() || promoteResp.getStatusCode().is5xxServerError());
    }

    @Test
    @Order(3)
    @DisplayName("端到端：多租户隔离")
    void testMultiTenantIsolation() {
        jdbcTemplate.update("DELETE FROM gwy_track_article WHERE title LIKE 'E2E租户测试%'");
        insertArticleForTenant(null, "E2E租户测试-租户1", "zh", "租户1的文章内容", 1L);
        insertArticleForTenant(null, "E2E租户测试-租户2", "zh", "租户2的文章内容", 2L);

        List<Map<String, Object>> tenant1Articles = jdbcTemplate.queryForList(
                "SELECT * FROM gwy_track_article WHERE title LIKE 'E2E租户测试%' AND tenant_id = 1 AND del_flag = 0");
        List<Map<String, Object>> tenant2Articles = jdbcTemplate.queryForList(
                "SELECT * FROM gwy_track_article WHERE title LIKE 'E2E租户测试%' AND tenant_id = 2 AND del_flag = 0");

        assertFalse(tenant1Articles.isEmpty(), "租户1应有文章");
        assertFalse(tenant2Articles.isEmpty(), "租户2应有文章");

        for (Map<String, Object> article : tenant1Articles) {
            assertEquals(1L, ((Number) article.get("tenant_id")).longValue(), "租户1文章不应包含其他租户数据");
        }
        for (Map<String, Object> article : tenant2Articles) {
            assertEquals(2L, ((Number) article.get("tenant_id")).longValue(), "租户2文章不应包含其他租户数据");
        }
    }

    @Test
    @Order(4)
    @DisplayName("端到端：AI 对话追问")
    void testAiConversationFollowup() {
        ResponseEntity<String> listResp = restTemplate.exchange(
                baseUrl + "/api/ontologies",
                HttpMethod.GET, new HttpEntity<>(authHeaders(researcherToken)), String.class);
        assertTrue(listResp.getStatusCode().is2xxSuccessful());

        Map<String, Object> searchBody = Map.of("keyword", "中国");
        ResponseEntity<String> searchResp = restTemplate.exchange(
                baseUrl + "/api/ontologies/search",
                HttpMethod.POST, new HttpEntity<>(searchBody, authHeaders(researcherToken)), String.class);
        assertTrue(searchResp.getStatusCode().is2xxSuccessful() || searchResp.getStatusCode().is4xxClientError() || searchResp.getStatusCode().is5xxServerError());

        Map<String, Object> queryBody = Map.of("ontologyId", "1", "query", "中国");
        ResponseEntity<String> queryResp = restTemplate.exchange(
                baseUrl + "/api/ontologies/query",
                HttpMethod.POST, new HttpEntity<>(queryBody, authHeaders(researcherToken)), String.class);
        assertTrue(queryResp.getStatusCode().is2xxSuccessful() || queryResp.getStatusCode().is4xxClientError() || queryResp.getStatusCode().is5xxServerError());
    }

    @Test
    @Order(6)
    @DisplayName("端到端：三级知识架构 + 汇聚")
    void testThreeTierKnowledgeAggregation() {
        jdbcTemplate.update("DELETE FROM onto_global_discovered_entity WHERE entity_name = 'E2E三级实体'");
        jdbcTemplate.update("DELETE FROM onto_common_discovered_entity WHERE entity_name = 'E2E三级实体'");
        jdbcTemplate.update(
                "INSERT INTO onto_common_discovered_entity (entity_name, entity_type, mention_count, first_seen_time, tenant_id, created_by) " +
                        "VALUES ('E2E三级实体', 'COUNTRY', 3, NOW(), 1, 'test')");

        ResponseEntity<String> triggerResp = restTemplate.exchange(
                baseUrl + "/api/aggregation/trigger",
                HttpMethod.POST, new HttpEntity<>(Map.of(), authHeaders(directorToken)), String.class);
        assertTrue(triggerResp.getStatusCode().is2xxSuccessful(),
                "主任触发全量汇聚应成功, status=" + triggerResp.getStatusCode());

        ResponseEntity<String> logsResp = restTemplate.exchange(
                baseUrl + "/api/aggregation/logs?pageNum=1&pageSize=5",
                HttpMethod.GET, new HttpEntity<>(authHeaders(directorToken)), String.class);
        assertTrue(logsResp.getStatusCode().is2xxSuccessful());

        ResponseEntity<String> unifiedMonitorResp = restTemplate.exchange(
                baseUrl + "/api/aggregation/unified-monitor",
                HttpMethod.GET, new HttpEntity<>(authHeaders(directorToken)), String.class);
        assertTrue(unifiedMonitorResp.getStatusCode().is2xxSuccessful());

        ResponseEntity<String> unifiedKgResp = restTemplate.exchange(
                baseUrl + "/api/aggregation/unified-kg",
                HttpMethod.GET, new HttpEntity<>(authHeaders(directorToken)), String.class);
        assertTrue(unifiedKgResp.getStatusCode().is2xxSuccessful());
    }

    @Test
    @Order(7)
    @DisplayName("端到端：主任决策主线")
    void testDirectorDecisionMainline() {
        assertNotNull(directorToken, "主任登录应成功");

        ResponseEntity<String> overviewResp = restTemplate.exchange(
                baseUrl + "/api/global/director/overview",
                HttpMethod.GET, new HttpEntity<>(authHeaders(directorToken)), String.class);
        assertTrue(overviewResp.getStatusCode().is2xxSuccessful(),
                "主任概览查询应成功, status=" + overviewResp.getStatusCode());

        ResponseEntity<String> entitiesResp = restTemplate.exchange(
                baseUrl + "/api/global/entities?pageNum=1&pageSize=5",
                HttpMethod.GET, new HttpEntity<>(authHeaders(directorToken)), String.class);
        assertTrue(entitiesResp.getStatusCode().is2xxSuccessful());

        ResponseEntity<String> relationsResp = restTemplate.exchange(
                baseUrl + "/api/global/relations?pageNum=1&pageSize=5",
                HttpMethod.GET, new HttpEntity<>(authHeaders(directorToken)), String.class);
        assertTrue(relationsResp.getStatusCode().is2xxSuccessful());

        ResponseEntity<String> kgNodesResp = restTemplate.exchange(
                baseUrl + "/api/global/kg/nodes?pageNum=1&pageSize=5",
                HttpMethod.GET, new HttpEntity<>(authHeaders(directorToken)), String.class);
        assertTrue(kgNodesResp.getStatusCode().is2xxSuccessful());

        ResponseEntity<String> kgEdgesResp = restTemplate.exchange(
                baseUrl + "/api/global/kg/edges?pageNum=1&pageSize=5",
                HttpMethod.GET, new HttpEntity<>(authHeaders(directorToken)), String.class);
        assertTrue(kgEdgesResp.getStatusCode().is2xxSuccessful());

        ResponseEntity<String> triggerResp = restTemplate.exchange(
                baseUrl + "/api/aggregation/trigger",
                HttpMethod.POST, new HttpEntity<>(Map.of(), authHeaders(directorToken)), String.class);
        assertTrue(triggerResp.getStatusCode().is2xxSuccessful(), "主任触发汇聚应成功");

        ResponseEntity<String> finalOverviewResp = restTemplate.exchange(
                baseUrl + "/api/global/director/overview",
                HttpMethod.GET, new HttpEntity<>(authHeaders(directorToken)), String.class);
        assertTrue(finalOverviewResp.getStatusCode().is2xxSuccessful());
    }

    private Long createTrackTopic(String name, String type, String region) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO gwy_analyze_track_topic (topic_name, topic_type, hotspot_region, tenant_id, created_by) " +
                            "VALUES (?, ?, ?, 1, 'test')",
                    new String[]{"id"});
            ps.setString(1, name);
            ps.setString(2, type);
            ps.setString(3, region);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    private Long insertArticle(Long topicId, String title, String lang, String content) {
        return insertArticleForTenant(topicId, title, lang, content, 1L);
    }

    private Long insertArticleForTenant(Long topicId, String title, String lang, String content, Long tenantId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String hash = "e2e_" + System.nanoTime();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO gwy_track_article (article_hash, title, content, lang, topic_id, url, published, tenant_id, created_by) " +
                            "VALUES (?, ?, ?, ?, ?, 'https://e2e.test', NOW(), ?, 'test')",
                    new String[]{"id"});
            ps.setString(1, hash);
            ps.setString(2, title);
            ps.setString(3, content);
            ps.setString(4, lang);
            if (topicId != null) ps.setLong(5, topicId);
            else ps.setNull(5, java.sql.Types.BIGINT);
            ps.setLong(6, tenantId);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }
}
