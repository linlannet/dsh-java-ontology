package net.linlan.dsh.global.entity;

import net.linlan.dsh.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 全局发现实体类
 * @author Linlan
 * CreatedAt 2026-10-14
 * @version 1.0
 * @since 1.0
 */
public class GlobalDiscoveredEntity extends BaseEntity {
    /** 实体名称 */
    private String entityName;
    /** 实体类型 */
    private String entityType;
    /** 来源租户ID列表JSON */
    private String tenantIds;
    /** 总提及次数 */
    private Integer totalMentionCount;
    /** 首次发现时间 */
    private LocalDateTime firstSeenTime;
    /** 最后发现时间 */
    private LocalDateTime lastSeenTime;

    public String getEntityName() { return entityName; }
    public void setEntityName(String entityName) { this.entityName = entityName; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public String getTenantIds() { return tenantIds; }
    public void setTenantIds(String tenantIds) { this.tenantIds = tenantIds; }
    public Integer getTotalMentionCount() { return totalMentionCount; }
    public void setTotalMentionCount(Integer totalMentionCount) { this.totalMentionCount = totalMentionCount; }
    public LocalDateTime getFirstSeenTime() { return firstSeenTime; }
    public void setFirstSeenTime(LocalDateTime firstSeenTime) { this.firstSeenTime = firstSeenTime; }
    public LocalDateTime getLastSeenTime() { return lastSeenTime; }
    public void setLastSeenTime(LocalDateTime lastSeenTime) { this.lastSeenTime = lastSeenTime; }
}
