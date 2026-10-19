package net.linlan.dsh.common.entity;

import java.time.LocalDateTime;

/**
 * 发现实体类
 * @author Linlan
 * CreatedAt 2026-10-14
 * @version 1.0
 * @since 1.0
 */
public class CommonDiscoveredEntity extends BaseEntity {
    /** 实体名称 */
    private String entityName;
    /** 实体类型 */
    private String entityType;
    /** 来源主题ID */
    private Long sourceTopicId;
    /** 首次发现时间 */
    private LocalDateTime firstSeenTime;
    /** 提及次数 */
    private Integer mentionCount;
    /** 关联本体实体ID（晋升后回填） */
    private Long ontologyEntityId;
    /** 状态：DISCOVERED/PROMOTED */
    private String status;

    public String getEntityName() { return entityName; }
    public void setEntityName(String entityName) { this.entityName = entityName; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public Long getSourceTopicId() { return sourceTopicId; }
    public void setSourceTopicId(Long sourceTopicId) { this.sourceTopicId = sourceTopicId; }
    public LocalDateTime getFirstSeenTime() { return firstSeenTime; }
    public void setFirstSeenTime(LocalDateTime firstSeenTime) { this.firstSeenTime = firstSeenTime; }
    public Integer getMentionCount() { return mentionCount; }
    public void setMentionCount(Integer mentionCount) { this.mentionCount = mentionCount; }
    public Long getOntologyEntityId() { return ontologyEntityId; }
    public void setOntologyEntityId(Long ontologyEntityId) { this.ontologyEntityId = ontologyEntityId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
