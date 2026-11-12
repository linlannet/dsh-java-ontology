package net.linlan.dsh.ontology.engine.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.linlan.dsh.ontology.engine.OntologyModelEngine;
import net.linlan.dsh.ontology.engine.catalog.TypeCatalog;
import net.linlan.dsh.ontology.engine.registry.OntologyRegistry;
import net.linlan.dsh.ontology.engine.validation.FieldViolation;
import net.linlan.dsh.ontology.engine.validation.IntegrityIssue;
import net.linlan.dsh.ontology.engine.validation.IntegrityReport;
import net.linlan.dsh.ontology.engine.validation.ModelIntegrityChecker;
import net.linlan.dsh.ontology.engine.validation.SchemaValidator;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OntologyModelEngineTest {

    private static final String BASE_MANIFEST_JSON = """
            {"model_type":"MANIFEST","version":"3.0","platform":"ontology",
             "base_models":["m1.yaml"],"projects":[]}
            """;
    private static final String PROJECT_MANIFEST_JSON = """
            {"model_type":"MANIFEST","version":"3.0","project":"siis",
             "base_manifest":"classpath:ontology-models/manifest.json",
             "models":["p1.yaml"]}
            """;
    private static final String SIMPLE_MODEL = """
            model_type: OBJECT
            version: "3.0"
            aggregates:
              - id: AGG-X
                alias: OntologyCountry
                lifecycle: [有效]
                tags: [本体域]
                attributes:
                  - { name: id, label: 主键, type: Long, required: true }
                  - { name: name, label: 名称, type: String, required: true }
            """;
    private static final String PROJECT_MODEL = """
            model_type: OBJECT
            version: "3.0"
            aggregates:
              - id: AGG-Y
                alias: OntologyBook
                attributes:
                  - { name: title, label: 书名, type: String, required: true }
            """;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void loads_real_base_models() {
        ModelLoader loader = new ModelLoader(getClass().getClassLoader(), objectMapper);
        EngineSnapshot snapshot = loader.load();

        assertFalse(snapshot.projectLayerPresent());
        assertEquals(25, snapshot.descriptors().size());
        assertEquals(7, snapshot.sourceFiles().size());
        OntologyRegistry registry = OntologyRegistry.from(snapshot.descriptors());
        assertEquals(14, registry.categories().size());
        assertTrue(registry.byCategory("country").isPresent());
        assertTrue(registry.byAlias("OntologyI18n").isPresent());
        assertTrue(registry.byTag("本体域").size() >= 9);

        IntegrityReport report = new ModelIntegrityChecker().check(snapshot);
        assertFalse(report.hasErrors(), () -> "不应有错误: " + report.errors());
        assertEquals(200, report.attributeCount());
        assertEquals(16, report.enumCount());
    }

    @Test
    void loads_project_layer_when_present() {
        ModelLoader loader = loaderWith(Map.of(
                ModelLoader.BASE_MANIFEST, BASE_MANIFEST_JSON,
                "ontology-models/m1.yaml", SIMPLE_MODEL,
                ModelLoader.PROJECT_MANIFEST, PROJECT_MANIFEST_JSON,
                "siis-models/p1.yaml", PROJECT_MODEL));
        EngineSnapshot snapshot = loader.load();

        assertTrue(snapshot.projectLayerPresent());
        assertEquals(2, snapshot.descriptors().size());
        OntologyRegistry registry = OntologyRegistry.from(snapshot.descriptors());
        assertEquals("country", registry.byAlias("OntologyCountry").orElseThrow().category());
        assertEquals("book", registry.byAlias("OntologyBook").orElseThrow().category());
    }

    @Test
    void missing_base_manifest_fails() {
        ModelLoader loader = loaderWith(Map.of());
        ModelLoadException exception = assertThrows(ModelLoadException.class, loader::load);
        assertTrue(exception.getMessage().contains("manifest"));
    }

    @Test
    void syntax_error_reports_file_and_line() {
        ModelLoader loader = loaderWith(Map.of(
                ModelLoader.BASE_MANIFEST, BASE_MANIFEST_JSON,
                "ontology-models/m1.yaml", "model_type: OBJECT\nversion: \"3.0\"\naggregates: [\n  - {"));

        ModelLoadException exception = assertThrows(ModelLoadException.class, loader::load);
        assertEquals("ontology-models/m1.yaml", exception.file());
        assertTrue(exception.line() > 0);
    }

    @Test
    void duplicate_alias_fails() {
        ModelLoader loader = loaderWith(Map.of(
                ModelLoader.BASE_MANIFEST, """
                        {"model_type":"MANIFEST","version":"3.0","base_models":["a.yaml","b.yaml"],"projects":[]}
                        """,
                "ontology-models/a.yaml", SIMPLE_MODEL,
                "ontology-models/b.yaml", SIMPLE_MODEL));

        ModelLoadException exception = assertThrows(ModelLoadException.class, loader::load);
        assertTrue(exception.getMessage().contains("重复 alias"));
    }

    @Test
    void bare_attribute_block_fails() {
        ModelLoader loader = loaderWith(Map.of(
                ModelLoader.BASE_MANIFEST, BASE_MANIFEST_JSON,
                "ontology-models/m1.yaml", """
                        model_type: OBJECT
                        version: "3.0"
                        aggregates:
                          - id: AGG-X
                            alias: OntologyCountry
                          - { name: id, type: Long, required: true }
                        """));

        ModelLoadException exception = assertThrows(ModelLoadException.class, loader::load);
        assertTrue(exception.getMessage().contains("裸属性块"));
        assertEquals("ontology-models/m1.yaml", exception.file());
    }

    @Test
    void unknown_type_has_null_category() {
        ModelLoader loader = loaderWith(Map.of(
                ModelLoader.BASE_MANIFEST, BASE_MANIFEST_JSON,
                "ontology-models/m1.yaml", """
                        model_type: OBJECT
                        version: "3.0"
                        aggregates:
                          - id: AGG-Z
                            alias: UnknownThing
                            attributes:
                              - { name: id, type: Long, required: true }
                        """));

        EngineSnapshot snapshot = loader.load();
        assertNull(snapshot.descriptors().get(0).category());
    }

    @Test
    void dangling_reference_is_error_when_project_present_and_warning_for_base_only() {
        AggregateDescriptorFactory factory = new AggregateDescriptorFactory();
        EngineSnapshot baseOnly = new EngineSnapshot(
                List.of(factory.descriptor("AGG-X")),
                List.of(),
                List.of(Map.of("behaviors", List.of(Map.of("ownerEntity", "MISSING-ID"))),
                        Map.of("records", List.of(Map.of("objectRef", "MISSING-ID"))),
                        Map.of("records", List.of())),
                java.util.Set.of("m2.yaml"),
                false);
        IntegrityReport baseReport = new ModelIntegrityChecker().check(baseOnly);
        assertFalse(baseReport.hasErrors());
        assertTrue(baseReport.issues().stream().anyMatch(i -> "DANGLING_REFERENCE".equals(i.code())));

        EngineSnapshot withProject = new EngineSnapshot(
                List.of(factory.descriptor("AGG-X")),
                List.of(),
                List.of(Map.of("behaviors", List.of(Map.of("ownerEntity", "MISSING-ID"))),
                        Map.of("records", List.of(Map.of("objectRef", "MISSING-ID"))),
                        Map.of("records", List.of())),
                java.util.Set.of("m2.yaml"),
                true);
        IntegrityReport projectReport = new ModelIntegrityChecker().check(withProject);
        assertTrue(projectReport.hasErrors());
        assertEquals(IntegrityIssue.Severity.ERROR, projectReport.errors().get(0).severity());
    }

    @Test
    void registry_returns_empty_for_unknown_keys() {
        ModelLoader loader = new ModelLoader(getClass().getClassLoader(), objectMapper);
        OntologyRegistry registry = OntologyRegistry.from(loader.load().descriptors());
        assertEquals(Optional.empty(), registry.byAlias("Nope"));
        assertEquals(Optional.empty(), registry.byCategory("nope"));
    }

    @Test
    void schema_validator_covers_required_type_enum_and_partial() {
        OntologyModelEngine engine = newEngine();
        SchemaValidator validator = new SchemaValidator(engine.registry(), true, "warn");

        List<FieldViolation> missing = validator.validate("country", Map.of(), false);
        assertTrue(missing.stream().anyMatch(v -> "required".equals(v.rule())));

        List<FieldViolation> wrongType = validator.validate("country",
                Map.of("id", "abc", "name", "中国"), false);
        assertTrue(wrongType.stream().anyMatch(v -> "type".equals(v.rule())));

        List<FieldViolation> enumViolation = validator.validate("hotspot",
                Map.of("id", 1L, "hotspotName", "热点", "riskLevel", "severe"), false);
        assertTrue(enumViolation.stream().anyMatch(v -> "enum".equals(v.rule())));

        List<FieldViolation> partial = validator.validate("country", Map.of("id", 1L), true);
        assertTrue(partial.isEmpty(), () -> "partial 不应校验未提交字段: " + partial);
    }

    @Test
    void schema_validator_honors_switch_and_unknown_policy() {
        OntologyModelEngine engine = newEngine();
        SchemaValidator disabled = new SchemaValidator(engine.registry(), false, "reject");
        assertTrue(disabled.validate("country", Map.of(), false).isEmpty());

        SchemaValidator reject = new SchemaValidator(engine.registry(), true, "reject");
        assertTrue(reject.validate("country", Map.of("id", 1L, "name", "中国", "hack", "x"), false)
                .stream().anyMatch(v -> "unknown".equals(v.rule())));
    }

    private OntologyModelEngine newEngine() {
        try {
            ModelLoader loader = new ModelLoader(getClass().getClassLoader(), objectMapper);
            EngineSnapshot snapshot = loader.load();
            return new OntologyModelEngine(snapshot, OntologyRegistry.from(snapshot.descriptors()),
                    new TypeCatalog(), new ModelIntegrityChecker().check(snapshot));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private ModelLoader loaderWith(Map<String, String> resources) {
        Function<String, InputStream> provider = name -> {
            String content = resources.get(name);
            if (content == null) {
                return null;
            }
            return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        };
        return new ModelLoader(provider, objectMapper, new TypeCatalog());
    }

    private static final class AggregateDescriptorFactory {
        net.linlan.dsh.ontology.engine.model.AggregateDescriptor descriptor(String id) {
            return new net.linlan.dsh.ontology.engine.model.AggregateDescriptor(
                    id, "测试", "TestEntity", null, List.of(), List.of(),
                    List.of(new net.linlan.dsh.ontology.engine.model.AttributeDescriptor(
                            "id", "主键", "Long", true, false, List.of(), null, null, "test")),
                    List.of(), "test.yaml");
        }
    }
}
