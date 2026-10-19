package net.linlan.dsh.ontology.entity;

import net.linlan.dsh.common.entity.BaseEntity;

/**
 * 本体版本实体类
 * <p>对应底层共享表 onto_ontology_version，通过 domain 列支持多业务方向隔离。</p>
 * @author Linlan
 * CreatedAt 2026-10-18
 * @version 1.0
 * @since 0.2.1
 */
public class OntologyVersion extends BaseEntity {
    /** 业务方向：siis/factory/ecommerce/city-ops */
    private String domain;
    /** 版本号 */
    private String versionCode;
    /** 版本名称 */
    private String versionName;
    /** 实体数量 */
    private Integer entityCount;
    /** 数据来源 */
    private String source;
    /** 状态：0=草稿 1=发布 2=归档 */
    private Integer status;

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }
    public String getVersionCode() { return versionCode; }
    public void setVersionCode(String versionCode) { this.versionCode = versionCode; }
    public String getVersionName() { return versionName; }
    public void setVersionName(String versionName) { this.versionName = versionName; }
    public Integer getEntityCount() { return entityCount; }
    public void setEntityCount(Integer entityCount) { this.entityCount = entityCount; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
