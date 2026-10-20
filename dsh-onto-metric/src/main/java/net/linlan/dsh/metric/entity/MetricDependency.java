package net.linlan.dsh.metric.entity;

import net.linlan.dsh.common.entity.BaseEntity;

import java.math.BigDecimal;

/**
 * 指标依赖关系实体（构成指标依赖树的边）
 * @author Linlan
 * CreatedAt 2026-10-20
 * @version 1.0
 * @since 1.0
 */
public class MetricDependency extends BaseEntity {
    /** 下游指标ID（依赖方） */
    private Long downstreamMetricId;
    /** 上游指标ID（被依赖方） */
    private Long upstreamMetricId;
    /** 依赖类型：DIRECT/WEIGHTED/CONDITIONAL */
    private String dependencyType;
    /** 权重（加权依赖用） */
    private BigDecimal weight;

    public Long getDownstreamMetricId() { return downstreamMetricId; }
    public void setDownstreamMetricId(Long downstreamMetricId) { this.downstreamMetricId = downstreamMetricId; }
    public Long getUpstreamMetricId() { return upstreamMetricId; }
    public void setUpstreamMetricId(Long upstreamMetricId) { this.upstreamMetricId = upstreamMetricId; }
    public String getDependencyType() { return dependencyType; }
    public void setDependencyType(String dependencyType) { this.dependencyType = dependencyType; }
    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }
}
