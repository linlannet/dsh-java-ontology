package net.linlan.dsh.plat.model;

/** 聚合根摘要视图（域分组中的一项）。 */
public record AggregateVO(
        String alias,
        String name,
        String category,
        int attributeCount) {
}
