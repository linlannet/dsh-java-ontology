package net.linlan.dsh.ontology.entity;

import net.linlan.dsh.common.entity.BaseEntity;

/**
 * 通用本体实体类
 * <p>对应底层共享表 onto_ontology_entity，通过 domain 列支持多业务方向隔离。</p>
 * @author Linlan
 * CreatedAt 2026-10-18
 * @version 1.0
 * @since 0.2.1
 */
public class OntologyEntityGeneric extends BaseEntity {
    /** 业务方向：siis/factory/ecommerce/city-ops */
    private String domain;
    /** 类别：country/organization/person/location/media/book/paper/term */
    private String category;
    /** 实体名称（默认中文） */
    private String entityName;
    /** 主语种：zh/en/fr/ar/ru/es/ja/de */
    private String lang;
    /** 描述 */
    private String description;
    /** 来源 */
    private String source;
    /** Wiki链接 */
    private String wikiUrl;
    /** 状态 */
    private Integer status;

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getEntityName() { return entityName; }
    public void setEntityName(String entityName) { this.entityName = entityName; }
    public String getLang() { return lang; }
    public void setLang(String lang) { this.lang = lang; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getWikiUrl() { return wikiUrl; }
    public void setWikiUrl(String wikiUrl) { this.wikiUrl = wikiUrl; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
