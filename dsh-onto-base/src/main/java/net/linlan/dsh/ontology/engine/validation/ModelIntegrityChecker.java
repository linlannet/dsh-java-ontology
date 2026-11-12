package net.linlan.dsh.ontology.engine.validation;

import net.linlan.dsh.ontology.engine.loader.EngineSnapshot;
import net.linlan.dsh.ontology.engine.model.AggregateDescriptor;
import net.linlan.dsh.ontology.engine.model.AttributeDescriptor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 模型完整性检查：空属性、重复 alias、重复字段、空枚举、悬空引用、基线计数。
 * 基座基线：25 聚合根 / 200 字段 / 16 枚举；含项目层时合计 52 / 492 / 30。
 */
public class ModelIntegrityChecker {

    private static final int BASE_ROOT_COUNT = 25;
    private static final int BASE_ATTRIBUTE_COUNT = 200;
    private static final int BASE_ENUM_COUNT = 16;
    private static final int PROJECT_ROOT_COUNT = 27;
    private static final int PROJECT_ATTRIBUTE_COUNT = 292;
    private static final int PROJECT_ENUM_COUNT = 14;

    public IntegrityReport check(EngineSnapshot snapshot) {
        List<IntegrityIssue> issues = new ArrayList<>();
        Map<String, String> aliasOwners = new LinkedHashMap<>();
        Set<String> ids = new LinkedHashSet<>();
        int attributeCount = 0;
        int enumCount = 0;

        for (AggregateDescriptor descriptor : snapshot.descriptors()) {
            attributeCount += descriptor.totalAttributeCount();
            enumCount += descriptor.totalEnumCount();
            collectIds(descriptor, ids);
            checkAggregate(descriptor, aliasOwners, issues);
        }

        checkDanglingReferences(snapshot.rawDocuments(), ids, snapshot.projectLayerPresent(), issues);
        checkBaseline(snapshot.projectLayerPresent(), snapshot.descriptors().size(),
                attributeCount, enumCount, issues);

        return new IntegrityReport(issues, snapshot.descriptors().size(), attributeCount, enumCount);
    }

    private void checkAggregate(AggregateDescriptor descriptor,
                                Map<String, String> aliasOwners,
                                List<IntegrityIssue> issues) {
        if (descriptor.attributes().isEmpty()) {
            issues.add(new IntegrityIssue(IntegrityIssue.Severity.ERROR, "EMPTY_ATTRIBUTES",
                    "聚合根缺少 attributes: " + descriptor.alias(), descriptor.sourceFile(), descriptor.alias()));
        }
        String previous = aliasOwners.putIfAbsent(descriptor.alias(), descriptor.sourceFile());
        if (previous != null) {
            issues.add(new IntegrityIssue(IntegrityIssue.Severity.ERROR, "DUPLICATE_ALIAS",
                    "重复 alias " + descriptor.alias() + "（" + previous + " / " + descriptor.sourceFile() + "）",
                    descriptor.sourceFile(), descriptor.alias()));
        }
        Set<String> fieldNames = new HashSet<>();
        for (AttributeDescriptor attribute : descriptor.attributes()) {
            if (!fieldNames.add(attribute.name())) {
                issues.add(new IntegrityIssue(IntegrityIssue.Severity.ERROR, "DUPLICATE_FIELD",
                        "重复字段 " + descriptor.alias() + "." + attribute.name(),
                        descriptor.sourceFile(), descriptor.alias()));
            }
            if ("Enum".equals(attribute.type()) && attribute.enumValues().isEmpty()) {
                issues.add(new IntegrityIssue(IntegrityIssue.Severity.ERROR, "EMPTY_ENUM",
                        "枚举字段缺少 enumValues: " + descriptor.alias() + "." + attribute.name(),
                        descriptor.sourceFile(), descriptor.alias()));
            }
        }
        for (AggregateDescriptor child : descriptor.subEntities()) {
            checkAggregate(child, aliasOwners, issues);
        }
    }

    private void collectIds(AggregateDescriptor descriptor, Set<String> ids) {
        if (descriptor.id() != null) {
            ids.add(descriptor.id());
        }
        for (AggregateDescriptor child : descriptor.subEntities()) {
            collectIds(child, ids);
        }
    }

    private void checkDanglingReferences(List<Map<String, Object>> documents,
                                         Set<String> ids,
                                         boolean projectPresent,
                                         List<IntegrityIssue> issues) {
        for (Map<String, Object> document : documents) {
            scanReferences(document, ids, projectPresent, issues);
        }
    }

    @SuppressWarnings("unchecked")
    private void scanReferences(Object node, Set<String> ids, boolean projectPresent,
                                List<IntegrityIssue> issues) {
        if (node instanceof Map<?, ?> map) {
            for (Map.Entry<String, Object> entry : ((Map<String, Object>) map).entrySet()) {
                Object value = entry.getValue();
                if (("ownerEntity".equals(entry.getKey()) || "objectRef".equals(entry.getKey()))
                        && value instanceof String ref && !ref.isBlank() && !ids.contains(ref)) {
                    IntegrityIssue.Severity severity = projectPresent
                            ? IntegrityIssue.Severity.ERROR : IntegrityIssue.Severity.WARNING;
                    issues.add(new IntegrityIssue(severity, "DANGLING_REFERENCE",
                            "悬空引用: " + ref, null, entry.getKey()));
                }
                scanReferences(value, ids, projectPresent, issues);
            }
        } else if (node instanceof List<?> list) {
            for (Object item : list) {
                scanReferences(item, ids, projectPresent, issues);
            }
        }
    }

    private void checkBaseline(boolean projectPresent, int roots, int attributes, int enums,
                               List<IntegrityIssue> issues) {
        int expectedRoots = projectPresent ? BASE_ROOT_COUNT + PROJECT_ROOT_COUNT : BASE_ROOT_COUNT;
        int expectedAttributes = projectPresent
                ? BASE_ATTRIBUTE_COUNT + PROJECT_ATTRIBUTE_COUNT : BASE_ATTRIBUTE_COUNT;
        int expectedEnums = projectPresent ? BASE_ENUM_COUNT + PROJECT_ENUM_COUNT : BASE_ENUM_COUNT;
        if (roots < expectedRoots) {
            issues.add(new IntegrityIssue(IntegrityIssue.Severity.WARNING, "BASELINE_ROOT_DRIFT",
                    "聚合根数下降: " + roots + " < " + expectedRoots, null, "aggregates"));
        }
        if (attributes < expectedAttributes) {
            issues.add(new IntegrityIssue(IntegrityIssue.Severity.WARNING, "BASELINE_FIELD_DRIFT",
                    "字段数下降: " + attributes + " < " + expectedAttributes, null, "attributes"));
        }
        if (enums < expectedEnums) {
            issues.add(new IntegrityIssue(IntegrityIssue.Severity.WARNING, "BASELINE_ENUM_DRIFT",
                    "枚举字段数下降: " + enums + " < " + expectedEnums, null, "enumValues"));
        }
    }
}
