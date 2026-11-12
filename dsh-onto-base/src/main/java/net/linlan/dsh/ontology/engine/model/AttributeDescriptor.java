package net.linlan.dsh.ontology.engine.model;

import java.util.List;

/** 不可变字段描述符，对应 YAML 中 attributes 列表的一项。 */
public record AttributeDescriptor(
        String name,
        String label,
        String type,
        boolean required,
        boolean unique,
        List<String> enumValues,
        String defaultValue,
        String description,
        String sourcePath) {

    public AttributeDescriptor {
        enumValues = enumValues == null ? List.of() : List.copyOf(enumValues);
    }
}
