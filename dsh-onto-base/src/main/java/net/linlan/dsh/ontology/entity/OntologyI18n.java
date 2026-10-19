package net.linlan.dsh.ontology.entity;

import net.linlan.dsh.common.entity.BaseEntity;

/**
 * 本体多语种翻译实体类
 * <p>对应底层共享表 onto_ontology_i18n，存储各本体实体的多语种名称和描述，主表仅存默认中文。</p>
 * @author Linlan
 * CreatedAt 2026-10-18
 * @version 1.0
 * @since 0.2.1
 */
public class OntologyI18n extends BaseEntity {
    /** 业务方向：siis/factory/ecommerce/city-ops */
    private String domain;
    /** 关联实体ID */
    private Long entityId;
    /** 本体分类：country/organization/person/term/... */
    private String category;
    /** 语种：en/fr/ar/ru/es/ja/de */
    private String lang;
    /** 该语种名称 */
    private String name;
    /** 该语种描述 */
    private String description;

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }
    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getLang() { return lang; }
    public void setLang(String lang) { this.lang = lang; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
