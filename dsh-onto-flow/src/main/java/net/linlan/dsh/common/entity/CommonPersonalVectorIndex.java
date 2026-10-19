package net.linlan.dsh.common.entity;

/**
 * 个人向量索引实体类
 * @author Linlan
 * CreatedAt 2026-10-14
 * @version 1.0
 * @since 1.0
 */
public class CommonPersonalVectorIndex extends BaseEntity {
    /** 实体ID */
    private Long entityId;
    /** 实体类型 */
    private String entityType;
    /** 向量嵌入 */
    private byte[] embedding;
    /** 模型版本 */
    private String modelVersion;

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public byte[] getEmbedding() { return embedding; }
    public void setEmbedding(byte[] embedding) { this.embedding = embedding; }
    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }
}
