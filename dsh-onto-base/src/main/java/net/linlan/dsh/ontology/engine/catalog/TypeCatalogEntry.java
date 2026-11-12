package net.linlan.dsh.ontology.engine.catalog;

/** 单个本体类型的事实：表名、数据源、名称列、是否可晋升。 */
public record TypeCatalogEntry(
        String typeCode,
        String tableName,
        DataSource dataSource,
        String nameColumn,
        boolean promotable) {
}
