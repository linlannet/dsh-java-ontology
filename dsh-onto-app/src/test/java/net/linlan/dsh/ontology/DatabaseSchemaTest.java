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
            "onto_ontology_version", "onto_ontology_entity", "gwy_ontology_country",
            "gwy_ontology_organization", "gwy_ontology_person", "gwy_ontology_hotspot",
            "onto_ontology_term", "onto_ontology_event_type", "onto_ontology_location",
            "gwy_ontology_media", "gwy_ontology_book", "gwy_ontology_paper",
            "gwy_ontology_bilateral_relation", "onto_ontology_i18n",
            "gwy_track_article", "gwy_track_crawl_source", "gwy_track_crawl_subscription",
            "gwy_track_crawl_task", "gwy_track_crawl_keyword_group", "gwy_track_data_import",
            "gwy_track_data_export", "gwy_track_kg_node", "gwy_track_kg_edge",
            "gwy_track_kg_build_log", "gwy_track_ner_element",
            "gwy_analyze_track_topic", "gwy_analyze_track_element", "gwy_analyze_nlp_entity",
            "gwy_analyze_nlp_triple", "gwy_analyze_nlp_sentiment", "gwy_analyze_alert_rule",
            "gwy_analyze_nlp_version",
            "gwy_display_rpt_template", "gwy_display_rpt_report", "gwy_display_alert_record",
            "gwy_display_information_publish", "gwy_display_push_record",
            "gwy_display_recommendation_record", "gwy_display_search_index",
            "gwy_display_conference_info", "gwy_display_column_manage",
            "gwy_display_user_module_config",
            "onto_common_discovered_entity", "onto_common_tenant_relation_asset",
            "onto_common_personal_vector_index", "onto_common_llm_chat_history",
            "onto_global_discovered_entity", "onto_global_bilateral_relation",
            "onto_global_kg_node", "onto_global_kg_edge", "onto_global_aggregation_log"
        };

        for (String expected : expectedTables) {
            assertTrue(tables.contains(expected), "Missing table: " + expected);
        }
        assertEquals(52, expectedTables.length, "Expected 52 tables");
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
