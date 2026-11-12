package net.linlan.dsh.ontology.model;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 0.2.2 T1.8 模型基线快照：基座 7 个 YAML 必须可解析，且聚合根/字段/枚举数量不得下降。
 * 快照数字是 T2/T4 的对照基线，后续任何字段丢失都会让本测试失败。
 */
class OntologyYamlBaselineTest {

    /** 基座 m1 聚合根数（含 8 个本体域、6 个本体元数据/系统/沉淀/全局/动态）。 */
    private static final int BASE_M1_ROOT_COUNT = 25;
    /** 基座 m1 字段总数（含 OntologyI18n / AnalyzeTrackElement 等子实体字段）。 */
    private static final int BASE_M1_ATTRIBUTE_COUNT = 200;
    /** 基座 m1 枚举字段数。 */
    private static final int BASE_M1_ENUM_COUNT = 16;
    /** TypeCatalog 14 类本体类型对应的 category。 */
    private static final Set<String> ONTOLOGY_CATEGORIES = new HashSet<>(List.of(
            "country", "organization", "person", "hotspot", "media", "book", "paper", "bilateralRelation",
            "term", "location", "eventType", "entity", "version", "i18n"));

    @Test
    void all_base_models_are_parseable() {
        Map<?, ?> manifest = loadYaml("ontology-models/manifest.json");
        List<String> models = new ArrayList<>();
        Object baseModels = manifest.get("base_models");
        assertNotNull(baseModels, "manifest.base_models 缺失");
        for (Object model : (List<?>) baseModels) {
            models.add(String.valueOf(model));
        }
        assertEquals(7, models.size(), "基座模型清单应为 7 个");
        for (String model : models) {
            Map<?, ?> doc = loadYaml("ontology-models/" + model);
            assertNotNull(doc.get("model_type"), model + " 缺少 model_type");
            assertNotNull(doc.get("version"), model + " 缺少 version");
            assertFalse(doc.isEmpty(), model + " 解析为空");
        }
    }

    @Test
    void m1_baseline_is_frozen() {
        Map<?, ?> doc = loadYaml("ontology-models/m1-object-model.yaml");
        List<Map<?, ?>> aggregates = aggregatesOf(doc);
        assertEquals(BASE_M1_ROOT_COUNT, aggregates.size(), "m1 聚合根数量不得下降");

        int attributes = 0;
        int enumFields = 0;
        int categories = 0;
        for (Map<?, ?> aggregate : aggregates) {
            assertNotNull(aggregate.get("alias"), "存在缺少 alias 的聚合根");
            List<Map<?, ?>> attrs = attributesOf(aggregate);
            assertFalse(attrs.isEmpty(), aggregate.get("alias") + " 缺少 attributes");
            attributes += attrs.size();
            enumFields += countEnum(attrs);
            if (aggregate.get("category") != null) {
                categories++;
            }
            List<Map<?, ?>> children = childrenOf(aggregate);
            for (Map<?, ?> child : children) {
                List<Map<?, ?>> childAttrs = attributesOf(child);
                attributes += childAttrs.size();
                enumFields += countEnum(childAttrs);
                if (child.get("category") != null) {
                    categories++;
                }
            }
        }
        assertEquals(BASE_M1_ATTRIBUTE_COUNT, attributes, "m1 字段总数不得下降");
        assertEquals(BASE_M1_ENUM_COUNT, enumFields, "m1 枚举字段数不得下降");
        assertEquals(14, categories, "14 类本体类型必须全部声明 category");
    }

    @Test
    void category_covers_all_type_catalog_entries() {
        Map<?, ?> doc = loadYaml("ontology-models/m1-object-model.yaml");
        Set<String> actual = new HashSet<>();
        for (Map<?, ?> aggregate : aggregatesOf(doc)) {
            collectCategories(aggregate, actual);
        }
        assertEquals(ONTOLOGY_CATEGORIES, actual, "category 必须与 TypeCatalog 14 类一一对应");
    }

    @Test
    void no_duplicate_alias_in_base_models() {
        Map<?, ?> manifest = loadYaml("ontology-models/manifest.json");
        Set<String> aliases = new HashSet<>();
        for (Object model : (List<?>) manifest.get("base_models")) {
            Map<?, ?> doc = loadYaml("ontology-models/" + model);
            List<Map<?, ?>> aggregates = aggregatesOf(doc);
            for (Map<?, ?> aggregate : aggregates) {
                if (aggregate.get("alias") != null) {
                    assertTrue(aliases.add(String.valueOf(aggregate.get("alias"))),
                            "基座模型出现重复 alias: " + aggregate.get("alias"));
                }
                for (Map<?, ?> child : childrenOf(aggregate)) {
                    if (child.get("alias") != null) {
                        assertTrue(aliases.add(String.valueOf(child.get("alias"))),
                                "基座模型出现重复 alias: " + child.get("alias"));
                    }
                }
            }
        }
    }

    private static void collectCategories(Map<?, ?> aggregate, Set<String> categories) {
        if (aggregate.get("category") != null) {
            categories.add(String.valueOf(aggregate.get("category")));
        }
        for (Map<?, ?> child : childrenOf(aggregate)) {
            if (child.get("category") != null) {
                categories.add(String.valueOf(child.get("category")));
            }
        }
    }

    private static int countEnum(List<Map<?, ?>> attrs) {
        int count = 0;
        for (Map<?, ?> attr : attrs) {
            if ("Enum".equals(attr.get("type"))) {
                count++;
            }
        }
        return count;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<?, ?>> aggregatesOf(Map<?, ?> doc) {
        Object aggregates = doc.get("aggregates");
        return aggregates == null ? List.of() : (List<Map<?, ?>>) aggregates;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<?, ?>> attributesOf(Map<?, ?> node) {
        Object attributes = node.get("attributes");
        return attributes == null ? List.of() : (List<Map<?, ?>>) attributes;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<?, ?>> childrenOf(Map<?, ?> node) {
        Object children = node.get("entities");
        return children == null ? List.of() : (List<Map<?, ?>>) children;
    }

    private static Map<?, ?> loadYaml(String resource) {
        try (InputStream in = OntologyYamlBaselineTest.class.getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(in, "classpath 资源缺失: " + resource);
            return new Yaml().load(in);
        } catch (Exception e) {
            throw new IllegalStateException("解析失败: " + resource, e);
        }
    }
}
