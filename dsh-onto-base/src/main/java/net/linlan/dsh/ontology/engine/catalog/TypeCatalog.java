package net.linlan.dsh.ontology.engine.catalog;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 14 类本体类型事实源（常量快照），与 onto_ontology_type 种子及实际 DDL 对齐。
 * <p>方案 A：8 类业务实体（country/organization/person/hotspot/media/book/paper/bilateralRelation）
 * 统一收敛到 {@code onto_ontology_entity}，按 {@code category} 区分类型，专有字段落
 * {@code onto_ontology_attribute}（EAV），不再使用 {@code gwy_ontology_*} 物理分表。</p>
 * <p>表名/列名只允许来自本目录白名单，禁止拼接用户输入。</p>
 */
public final class TypeCatalog {

    private static final List<TypeCatalogEntry> ENTRIES = List.of(
            new TypeCatalogEntry("country", "onto_ontology_entity", DataSource.ONTO, "entity_name", true),
            new TypeCatalogEntry("organization", "onto_ontology_entity", DataSource.ONTO, "entity_name", true),
            new TypeCatalogEntry("person", "onto_ontology_entity", DataSource.ONTO, "entity_name", true),
            new TypeCatalogEntry("hotspot", "onto_ontology_entity", DataSource.ONTO, "entity_name", true),
            new TypeCatalogEntry("media", "onto_ontology_entity", DataSource.ONTO, "entity_name", true),
            new TypeCatalogEntry("book", "onto_ontology_entity", DataSource.ONTO, "entity_name", true),
            new TypeCatalogEntry("paper", "onto_ontology_entity", DataSource.ONTO, "entity_name", true),
            new TypeCatalogEntry("bilateralRelation", "onto_ontology_entity", DataSource.ONTO, "entity_name", false),
            new TypeCatalogEntry("location", "onto_ontology_location", DataSource.ONTO, "name", true),
            new TypeCatalogEntry("term", "onto_ontology_term", DataSource.ONTO, "term_name", true),
            new TypeCatalogEntry("eventType", "onto_ontology_event_type", DataSource.ONTO, "event_type", false),
            new TypeCatalogEntry("entity", "onto_ontology_entity", DataSource.ONTO, "entity_name", false),
            new TypeCatalogEntry("version", "onto_ontology_version", DataSource.ONTO, "version_name", false),
            new TypeCatalogEntry("i18n", "onto_ontology_i18n", DataSource.ONTO, "name", false)
    );

    /** 存量 YAML alias → typeCode 兼容映射。 */
    private static final Map<String, String> ALIAS_TO_TYPE = Map.ofEntries(
            Map.entry("OntologyCountry", "country"),
            Map.entry("OntologyOrganization", "organization"),
            Map.entry("OntologyPerson", "person"),
            Map.entry("OntologyHotspot", "hotspot"),
            Map.entry("OntologyMedia", "media"),
            Map.entry("OntologyBook", "book"),
            Map.entry("OntologyPaper", "paper"),
            Map.entry("OntologyBilateralRelation", "bilateralRelation"),
            Map.entry("OntologyLocation", "location"),
            Map.entry("OntologyTerm", "term"),
            Map.entry("OntologyEventType", "eventType"),
            Map.entry("OntologyEntity", "entity"),
            Map.entry("OntologyVersion", "version"),
            Map.entry("OntologyI18n", "i18n")
    );

    private final Map<String, TypeCatalogEntry> byCode;

    public TypeCatalog() {
        Map<String, TypeCatalogEntry> map = new LinkedHashMap<>();
        for (TypeCatalogEntry entry : ENTRIES) {
            map.put(entry.typeCode(), entry);
        }
        this.byCode = Map.copyOf(map);
    }

    public TypeCatalogEntry require(String typeCode) {
        TypeCatalogEntry entry = byCode.get(typeCode);
        if (entry == null) {
            throw new IllegalArgumentException("未知本体类型: " + typeCode);
        }
        return entry;
    }

    public Optional<TypeCatalogEntry> find(String typeCode) {
        return Optional.ofNullable(byCode.get(typeCode));
    }

    /** 大小写不敏感解析；晋升/匹配合并历史小写类型值（如 ORGANIZATION）。 */
    public Optional<TypeCatalogEntry> findIgnoreCase(String typeCode) {
        if (typeCode == null || typeCode.isBlank()) {
            return Optional.empty();
        }
        String normalized = typeCode.trim();
        return ENTRIES.stream()
                .filter(entry -> entry.typeCode().equalsIgnoreCase(normalized))
                .findFirst();
    }

    public List<TypeCatalogEntry> entries() {
        return ENTRIES;
    }

    public Set<String> promotableTypes() {
        return ENTRIES.stream().filter(TypeCatalogEntry::promotable)
                .map(TypeCatalogEntry::typeCode).collect(Collectors.toUnmodifiableSet());
    }

    /** 9 类晋升类型的名称列；不可晋升类型不在结果中。 */
    public Map<String, String> nameColumns() {
        Map<String, String> map = new LinkedHashMap<>();
        for (TypeCatalogEntry entry : ENTRIES) {
            if (entry.promotable()) {
                map.put(entry.typeCode(), entry.nameColumn());
            }
        }
        return Map.copyOf(map);
    }

    public Optional<String> typeCodeOfAlias(String alias) {
        return Optional.ofNullable(ALIAS_TO_TYPE.get(alias));
    }

    public Optional<String> dataSource(String typeCode) {
        return find(typeCode).map(e -> e.dataSource().name());
    }
}
