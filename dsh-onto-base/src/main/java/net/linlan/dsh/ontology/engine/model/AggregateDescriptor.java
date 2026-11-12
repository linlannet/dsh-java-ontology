package net.linlan.dsh.ontology.engine.model;

import java.util.List;
import java.util.Optional;

/** 不可变聚合根描述符；嵌套实体同样以本类型表达。 */
public record AggregateDescriptor(
        String id,
        String name,
        String alias,
        String category,
        List<String> lifecycle,
        List<String> tags,
        List<AttributeDescriptor> attributes,
        List<AggregateDescriptor> subEntities,
        String sourceFile) {

    public AggregateDescriptor {
        lifecycle = lifecycle == null ? List.of() : List.copyOf(lifecycle);
        tags = tags == null ? List.of() : List.copyOf(tags);
        attributes = attributes == null ? List.of() : List.copyOf(attributes);
        subEntities = subEntities == null ? List.of() : List.copyOf(subEntities);
    }

    public Optional<AttributeDescriptor> attribute(String name) {
        return attributes.stream().filter(a -> a.name().equals(name)).findFirst();
    }

    /** 含子实体字段的总数，用于基线统计。 */
    public int totalAttributeCount() {
        return attributes.size() + subEntities.stream().mapToInt(AggregateDescriptor::totalAttributeCount).sum();
    }

    public int totalEnumCount() {
        return enumCount(attributes)
                + subEntities.stream().mapToInt(AggregateDescriptor::totalEnumCount).sum();
    }

    private static int enumCount(List<AttributeDescriptor> attrs) {
        return (int) attrs.stream().filter(a -> "Enum".equals(a.type())).count();
    }
}
