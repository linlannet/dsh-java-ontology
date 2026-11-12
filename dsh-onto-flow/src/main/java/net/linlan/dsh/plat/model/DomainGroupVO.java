package net.linlan.dsh.plat.model;

import java.util.List;

/** 域分组视图：按 tags 聚合的聚合根列表。 */
public record DomainGroupVO(
        String tag,
        int aggregateCount,
        List<AggregateVO> aggregates) {

    public DomainGroupVO {
        aggregates = aggregates == null ? List.of() : List.copyOf(aggregates);
    }
}
