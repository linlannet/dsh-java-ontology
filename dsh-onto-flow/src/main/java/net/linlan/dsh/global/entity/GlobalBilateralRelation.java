package net.linlan.dsh.global.entity;

import net.linlan.dsh.common.entity.BaseEntity;

/**
 * 全局双边关系实体类
 * @author Linlan
 * CreatedAt 2026-10-14
 * @version 1.0
 * @since 1.0
 */
public class GlobalBilateralRelation extends BaseEntity {
    /** 主体实体名 */
    private String subjectEntityName;
    /** 客体实体名 */
    private String objectEntityName;
    /** 关系类型 */
    private String relationType;
    /** 来源租户ID列表JSON */
    private String tenantIds;
    /** 平均得分 */
    private Float avgScore;
    /** 样本数 */
    private Integer sampleCount;

    public String getSubjectEntityName() { return subjectEntityName; }
    public void setSubjectEntityName(String subjectEntityName) { this.subjectEntityName = subjectEntityName; }
    public String getObjectEntityName() { return objectEntityName; }
    public void setObjectEntityName(String objectEntityName) { this.objectEntityName = objectEntityName; }
    public String getRelationType() { return relationType; }
    public void setRelationType(String relationType) { this.relationType = relationType; }
    public String getTenantIds() { return tenantIds; }
    public void setTenantIds(String tenantIds) { this.tenantIds = tenantIds; }
    public Float getAvgScore() { return avgScore; }
    public void setAvgScore(Float avgScore) { this.avgScore = avgScore; }
    public Integer getSampleCount() { return sampleCount; }
    public void setSampleCount(Integer sampleCount) { this.sampleCount = sampleCount; }
}
