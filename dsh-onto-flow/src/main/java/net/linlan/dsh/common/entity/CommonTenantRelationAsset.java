package net.linlan.dsh.common.entity;

import java.time.LocalDateTime;

/**
 * 租户关系资产实体类
 * @author Linlan
 * CreatedAt 2026-10-14
 * @version 1.0
 * @since 1.0
 */
public class CommonTenantRelationAsset extends BaseEntity {
    /** 主体实体ID */
    private Long subjectEntityId;
    /** 客体实体ID */
    private Long objectEntityId;
    /** 关系类型 */
    private String relationType;
    /** 平均得分 */
    private Float avgScore;
    /** 关联主题ID列表 */
    private String topicIds;
    /** 最后更新时间 */
    private LocalDateTime lastUpdated;

    public Long getSubjectEntityId() { return subjectEntityId; }
    public void setSubjectEntityId(Long subjectEntityId) { this.subjectEntityId = subjectEntityId; }
    public Long getObjectEntityId() { return objectEntityId; }
    public void setObjectEntityId(Long objectEntityId) { this.objectEntityId = objectEntityId; }
    public String getRelationType() { return relationType; }
    public void setRelationType(String relationType) { this.relationType = relationType; }
    public Float getAvgScore() { return avgScore; }
    public void setAvgScore(Float avgScore) { this.avgScore = avgScore; }
    public String getTopicIds() { return topicIds; }
    public void setTopicIds(String topicIds) { this.topicIds = topicIds; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}
