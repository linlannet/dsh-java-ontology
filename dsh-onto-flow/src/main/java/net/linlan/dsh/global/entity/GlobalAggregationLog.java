package net.linlan.dsh.global.entity;

import net.linlan.dsh.common.entity.BaseEntity;

/**
 * 全局汇聚日志实体类
 * @author Linlan
 * CreatedAt 2026-10-14
 * @version 1.0
 * @since 1.0
 */
public class GlobalAggregationLog extends BaseEntity {
    /** 汇聚类型：FULL/INCREMENTAL */
    private String aggregationType;
    /** 触发类型：SCHEDULED/EVENT/MANUAL */
    private String triggerType;
    /** 实体数量 */
    private Integer entityCount;
    /** 关系数量 */
    private Integer relationCount;
    /** 耗时（毫秒） */
    private Long durationMs;
    /** 状态：running/success/failed */
    private String status;

    public String getAggregationType() { return aggregationType; }
    public void setAggregationType(String aggregationType) { this.aggregationType = aggregationType; }
    public String getTriggerType() { return triggerType; }
    public void setTriggerType(String triggerType) { this.triggerType = triggerType; }
    public Integer getEntityCount() { return entityCount; }
    public void setEntityCount(Integer entityCount) { this.entityCount = entityCount; }
    public Integer getRelationCount() { return relationCount; }
    public void setRelationCount(Integer relationCount) { this.relationCount = relationCount; }
    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
