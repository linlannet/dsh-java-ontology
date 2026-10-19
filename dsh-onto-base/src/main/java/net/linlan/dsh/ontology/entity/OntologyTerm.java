package net.linlan.dsh.ontology.entity;

import net.linlan.dsh.common.entity.BaseEntity;

/**
 * 本体术语实体类
 * <p>对应底层共享表 onto_ontology_term，通过 domain 列支持多业务方向隔离。</p>
 * @author Linlan
 * CreatedAt 2026-10-18
 * @version 1.0
 * @since 0.2.1
 */
public class OntologyTerm extends BaseEntity {
    /** 业务方向：siis/factory/ecommerce/city-ops */
    private String domain;
    /** 术语名（默认中文） */
    private String termName;
    /** 分类 */
    private String category;
    /** 定义 */
    private String definition;
    /** 来源 */
    private String source;
    /** 语种 */
    private String lang;
    /** 状态 */
    private Integer status;

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }
    public String getTermName() { return termName; }
    public void setTermName(String termName) { this.termName = termName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDefinition() { return definition; }
    public void setDefinition(String definition) { this.definition = definition; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getLang() { return lang; }
    public void setLang(String lang) { this.lang = lang; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
