package net.linlan.dsh.global.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体合并日志
 * <p>P2-15：记录实体消歧合并操作（source → target）。
 * @author Linlan
 * CreatedAt 2026-10-18
 * @version 1.0
 * @since 1.0
 */
public class EntityMergeLog implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long sourceEntityId;
    private Long targetEntityId;
    private String entityType;
    private Float similarityScore;
    private String mergeStrategy;
    private Long operatorId;
    private Long tenantId;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private Integer delFlag;
    private String remark;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSourceEntityId() { return sourceEntityId; }
    public void setSourceEntityId(Long sourceEntityId) { this.sourceEntityId = sourceEntityId; }

    public Long getTargetEntityId() { return targetEntityId; }
    public void setTargetEntityId(Long targetEntityId) { this.targetEntityId = targetEntityId; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public Float getSimilarityScore() { return similarityScore; }
    public void setSimilarityScore(Float similarityScore) { this.similarityScore = similarityScore; }

    public String getMergeStrategy() { return mergeStrategy; }
    public void setMergeStrategy(String mergeStrategy) { this.mergeStrategy = mergeStrategy; }

    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Integer getDelFlag() { return delFlag; }
    public void setDelFlag(Integer delFlag) { this.delFlag = delFlag; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
