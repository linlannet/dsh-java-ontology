package net.linlan.dsh.plat.model;

import java.util.List;

/** 建模预览：单张表的完整设计（含可执行 DDL 与说明）。 */
public record TableDesignVO(
        String alias,
        String ownerAlias,
        String tableName,
        String sourceFile,
        String description,
        List<ColumnDesignVO> columns,
        String ddl,
        List<String> indexDdls) {

    public TableDesignVO {
        columns = columns == null ? List.of() : List.copyOf(columns);
        indexDdls = indexDdls == null ? List.of() : List.copyOf(indexDdls);
    }
}
