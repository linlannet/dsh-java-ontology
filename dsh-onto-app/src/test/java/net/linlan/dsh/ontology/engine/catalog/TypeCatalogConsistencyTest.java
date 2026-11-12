package net.linlan.dsh.ontology.engine.catalog;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 0.2.2 T4：TypeCatalog 与数据库种子（V1.0.1）及 ONTO DDL（V1.0.0）四方一致性。
 * 任一映射漂移都会使该测试失败。
 */
class TypeCatalogConsistencyTest {

    private static final Pattern SEED_ROW = Pattern.compile(
            "\\('([^']+)',\\s*'([^']+)',\\s*'([^']+)',\\s*'([^']+)',\\s*1,\\s*'system'\\)");

    private static final Set<String> EXPECTED_PROMOTABLE = Set.of(
            "country", "organization", "person", "hotspot", "media", "book", "paper", "term", "location");

    private final TypeCatalog catalog = new TypeCatalog();

    @Test
    void catalog_matches_seed_rows() throws IOException {
        Map<String, String> seedTables = seedTableNames();
        assertEquals(14, seedTables.size(), "onto_ontology_type 种子应为 14 条");
        assertEquals(seedTables.size(), catalog.entries().size(), "TypeCatalog 应为 14 条");
        for (TypeCatalogEntry entry : catalog.entries()) {
            // 方案 A：8 类业务实体统一落 onto_ontology_entity（EAV），种子行中的 gwy_ 物理表名仅作历史记录
            if (net.linlan.dsh.ontology.repository.OntologyMetaRepository.EAV_CATEGORIES.contains(entry.typeCode())) {
                assertEquals("onto_ontology_entity", entry.tableName(),
                        "类型 " + entry.typeCode() + " 应归并到 onto_ontology_entity");
                continue;
            }
            assertEquals(seedTables.get(entry.typeCode()), entry.tableName(),
                    "类型 " + entry.typeCode() + " 表名与种子不一致");
        }
    }

    @Test
    void promotable_types_are_nine() {
        assertEquals(EXPECTED_PROMOTABLE, catalog.promotableTypes(), "可晋升集合必须为 9 类");
        assertEquals(9, catalog.nameColumns().size());
        assertEquals("entity_name", catalog.nameColumns().get("hotspot"));
        assertEquals("entity_name", catalog.nameColumns().get("book"));
        assertEquals("entity_name", catalog.nameColumns().get("paper"));
        assertEquals("term_name", catalog.nameColumns().get("term"));
        assertEquals("entity_name", catalog.nameColumns().get("country"));
    }

    @Test
    void bilateral_relation_is_not_promotable() {
        TypeCatalogEntry entry = catalog.require("bilateralRelation");
        assertEquals(DataSource.ONTO, entry.dataSource());
        assertFalse(entry.promotable());
        assertEquals("entity_name", entry.nameColumn());
    }

    @Test
    void onto_name_columns_exist_in_ddl() throws IOException {
        String ddl = read("db/migration/V1.0.0__init_schema.sql");
        Map<String, String> checks = Map.of(
                "onto_ontology_location", "name",
                "onto_ontology_term", "term_name",
                "onto_ontology_event_type", "event_type",
                "onto_ontology_entity", "entity_name",
                "onto_ontology_version", "version_name",
                "onto_ontology_i18n", "name");
        checks.forEach((table, column) ->
                assertTrue(columnExists(ddl, table, column), table + " 缺少名称列 " + column));
    }

    @Test
    void alias_mapping_covers_fourteen_types() {
        for (TypeCatalogEntry entry : catalog.entries()) {
            String alias = "Ontology" + Character.toUpperCase(entry.typeCode().charAt(0))
                    + entry.typeCode().substring(1);
            if ("eventType".equals(entry.typeCode())) {
                alias = "OntologyEventType";
            }
            String finalAlias = alias;
            assertEquals(entry.typeCode(), catalog.typeCodeOfAlias(finalAlias).orElse(null),
                    "alias 未映射到类型: " + finalAlias);
        }
    }

    private static boolean columnExists(String ddl, String table, String column) {
        int start = ddl.indexOf("CREATE TABLE IF NOT EXISTS " + table + " (");
        assertTrue(start >= 0, "DDL 缺少表 " + table);
        int next = ddl.indexOf("CREATE TABLE", start + 1);
        if (next < 0) {
            next = ddl.length();
        }
        assertTrue(next > start, "DDL 表定义未闭合: " + table);
        String block = ddl.substring(start, next);
        return block.matches("(?s).*\\b" + Pattern.quote(column) + "\\b.*");
    }

    private static Map<String, String> seedTableNames() throws IOException {
        String sql = read("db/migration/V1.0.1__seed_data.sql");
        Map<String, String> rows = new HashMap<>();
        Matcher matcher = SEED_ROW.matcher(sql);
        while (matcher.find()) {
            rows.put(matcher.group(1), matcher.group(2));
        }
        return rows;
    }

    private static String read(String resource) throws IOException {
        try (InputStream in = TypeCatalogConsistencyTest.class.getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(in, "classpath 资源缺失: " + resource);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
