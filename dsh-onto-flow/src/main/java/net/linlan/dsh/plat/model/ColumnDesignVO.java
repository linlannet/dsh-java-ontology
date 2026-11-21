package net.linlan.dsh.plat.model;

import java.util.List;

/** 建模预览：单个字段的表设计。 */
public record ColumnDesignVO(
        String name,
        String label,
        String sqlType,
        boolean required,
        boolean unique,
        List<String> enumValues,
        String defaultValue,
        String description,
        String sourcePath) {

    public ColumnDesignVO {
        enumValues = enumValues == null ? List.of() : List.copyOf(enumValues);
    }
}
