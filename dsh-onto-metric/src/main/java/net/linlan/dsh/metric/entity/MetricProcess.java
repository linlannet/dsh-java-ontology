package net.linlan.dsh.metric.entity;

import net.linlan.dsh.common.entity.BaseEntity;

/**
 * 业务流程实体（业务流转，如"采选→编目→评审→入库"）
 * @author Linlan
 * CreatedAt 2026-10-20
 * @version 1.0
 * @since 1.0
 */
public class MetricProcess extends BaseEntity {
    /** 流程名称 */
    private String processName;
    /** 流程编码 */
    private String processCode;
    /** 关联业务对象ID */
    private Long businessObjectId;
    /** 节点配置（JSON） */
    private String nodeConfig;
    /** 描述 */
    private String description;
    /** 状态：0=草稿 1=启用 2=停用 */
    private Integer status;

    public String getProcessName() { return processName; }
    public void setProcessName(String processName) { this.processName = processName; }
    public String getProcessCode() { return processCode; }
    public void setProcessCode(String processCode) { this.processCode = processCode; }
    public Long getBusinessObjectId() { return businessObjectId; }
    public void setBusinessObjectId(Long businessObjectId) { this.businessObjectId = businessObjectId; }
    public String getNodeConfig() { return nodeConfig; }
    public void setNodeConfig(String nodeConfig) { this.nodeConfig = nodeConfig; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
