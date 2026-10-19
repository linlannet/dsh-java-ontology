package net.linlan.dsh.global.entity;

import net.linlan.dsh.common.entity.BaseEntity;

/**
 * 全局知识图谱边实体类
 * @author Linlan
 * CreatedAt 2026-10-14
 * @version 1.0
 * @since 1.0
 */
public class GlobalKgEdge extends BaseEntity {
    /** 源节点ID */
    private String sourceNodeId;
    /** 目标节点ID */
    private String targetNodeId;
    /** 关系类型 */
    private String relationType;
    /** 来源租户ID列表JSON */
    private String tenantIds;
    /** 跨租户权重 */
    private Float crossTenantWeight;

    public String getSourceNodeId() { return sourceNodeId; }
    public void setSourceNodeId(String sourceNodeId) { this.sourceNodeId = sourceNodeId; }
    public String getTargetNodeId() { return targetNodeId; }
    public void setTargetNodeId(String targetNodeId) { this.targetNodeId = targetNodeId; }
    public String getRelationType() { return relationType; }
    public void setRelationType(String relationType) { this.relationType = relationType; }
    public String getTenantIds() { return tenantIds; }
    public void setTenantIds(String tenantIds) { this.tenantIds = tenantIds; }
    public Float getCrossTenantWeight() { return crossTenantWeight; }
    public void setCrossTenantWeight(Float crossTenantWeight) { this.crossTenantWeight = crossTenantWeight; }
}
