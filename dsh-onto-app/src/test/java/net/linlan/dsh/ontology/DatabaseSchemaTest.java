package net.linlan.dsh.ontology;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class DatabaseSchemaTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    @Test
    void allTablesExist() throws Exception {
        DatabaseMetaData metaData = dataSource.getConnection().getMetaData();
        List<String> tables = new ArrayList<>();

        try (ResultSet rs = metaData.getTables(null, null, null, new String[]{"TABLE"})) {
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME").toLowerCase());
            }
        }

        String[] expectedTables = {
            "onto_sys_user",
            "onto_ontology_version", "onto_ontology_entity", "onto_ontology_attribute",
            "onto_ontology_term", "onto_ontology_event_type", "onto_ontology_location", "onto_ontology_i18n",
            // 方案 A 后仅保留项目分析所需的 4 张表（V1.0.7 已统一改为 onto_ 前缀）
            "onto_track_article", "onto_track_kg_node", "onto_track_kg_edge", "onto_analyze_track_topic",
            "onto_common_discovered_entity", "onto_common_tenant_relation_asset",
            "onto_common_personal_vector_index", "onto_common_llm_chat_history",
            "onto_global_discovered_entity", "onto_global_bilateral_relation",
            "onto_global_kg_node", "onto_global_kg_edge", "onto_global_aggregation_log",
            // D-R1：R 域平台层补建（V1.0.3）
            "onto_report_alert_record", "onto_report_information_publish",
            "onto_report_push_record", "onto_report_recommendation_record",
            "onto_report_search_index",
            // 平台层指标（dsh-onto-metric 的 V1.0.2）
            "onto_metric_business_object", "onto_metric_process",
            "onto_metric_decision_point", "onto_metric_definition",
            "onto_metric_dependency"
        };

        for (String expected : expectedTables) {
            assertTrue(tables.contains(expected), "Missing table: " + expected);
        }
        assertEquals(31, expectedTables.length, "Expected 31 tables");
    }

    @Test
    void seedUsersExist() {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM onto_sys_user WHERE del_flag = '0'", Integer.class);
        assertNotNull(count);
        assertTrue(count >= 3, "Should have at least 3 seed users");
    }

    @Test
    void ontologyVersionExists() {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM onto_ontology_version WHERE del_flag = 0", Integer.class);
        assertNotNull(count);
        assertTrue(count >= 1, "Should have at least 1 ontology version");
    }
}
