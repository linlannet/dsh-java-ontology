package net.linlan.dsh.metric.entity;

import net.linlan.dsh.common.entity.BaseEntity;

/**
 * 关键决策点实体（流程中的决策节点）
 * @author Linlan
 * CreatedAt 2026-10-20
 * @version 1.0
 * @since 1.0
 */
public class MetricDecisionPoint extends BaseEntity {
    /** 决策点名称 */
    private String pointName;
    /** 所属流程ID */
    private Long processId;
    /** 节点序号 */
    private Integer nodeIndex;
    /** 决策类型：APPROVE/REJECT/ROUTE/SCORE */
    private String decisionType;
    /** 描述 */
    private String description;
    /** 状态：0=草稿 1=启用 2=停用 */
    private Integer status;

    public String getPointName() { return pointName; }
    public void setPointName(String pointName) { this.pointName = pointName; }
    public Long getProcessId() { return processId; }
    public void setProcessId(Long processId) { this.processId = processId; }
    public Integer getNodeIndex() { return nodeIndex; }
    public void setNodeIndex(Integer nodeIndex) { this.nodeIndex = nodeIndex; }
    public String getDecisionType() { return decisionType; }
    public void setDecisionType(String decisionType) { this.decisionType = decisionType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
