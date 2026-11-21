package net.linlan.dsh.plat.model;

import java.util.List;

/** 建表执行结果。 */
public record MaterializeResultVO(
        boolean success,
        List<String> createdTables,
        List<String> existingTables,
        String message) {

    public MaterializeResultVO {
        createdTables = createdTables == null ? List.of() : List.copyOf(createdTables);
        existingTables = existingTables == null ? List.of() : List.copyOf(existingTables);
    }
}
