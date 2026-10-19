package net.linlan.dsh.global.entity;

import net.linlan.dsh.common.entity.BaseEntity;

/**
 * 全局知识图谱节点实体类
 * @author Linlan
 * CreatedAt 2026-10-14
 * @version 1.0
 * @since 1.0
 */
public class GlobalKgNode extends BaseEntity {
    /** 关联本体实体ID */
    private Long ontologyEntityId;
    /** 节点名称 */
    private String nodeName;
    /** 节点类型 */
    private String nodeType;
    /** 来源租户ID列表JSON */
    private String tenantIds;
    /** 跨租户权重 */
    private Float crossTenantWeight;

    public Long getOntologyEntityId() { return ontologyEntityId; }
    public void setOntologyEntityId(Long ontologyEntityId) { this.ontologyEntityId = ontologyEntityId; }
    public String getNodeName() { return nodeName; }
    public void setNodeName(String nodeName) { this.nodeName = nodeName; }
    public String getNodeType() { return nodeType; }
    public void setNodeType(String nodeType) { this.nodeType = nodeType; }
    public String getTenantIds() { return tenantIds; }
    public void setTenantIds(String tenantIds) { this.tenantIds = tenantIds; }
    public Float getCrossTenantWeight() { return crossTenantWeight; }
    public void setCrossTenantWeight(Float crossTenantWeight) { this.crossTenantWeight = crossTenantWeight; }
}
