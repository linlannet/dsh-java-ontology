package net.linlan.dsh.ontology.entity;

import net.linlan.dsh.common.entity.BaseEntity;

/**
 * 地点实体类
 * <p>对应底层共享表 onto_ontology_location，通过 domain 列支持多业务方向隔离。</p>
 * @author Linlan
 * CreatedAt 2026-10-18
 * @version 1.0
 * @since 0.2.1
 */
public class OntologyLocation extends BaseEntity {
    /** 业务方向：siis/factory/ecommerce/city-ops */
    private String domain;
    /** 地名（默认中文） */
    private String name;
    /** 类型：country/city/region/river/mountain */
    private String locType;
    /** 父级ID */
    private Long parentId;
    /** 所属国家 */
    private String country;
    /** 首都/首府 */
    private String capital;
    /** 人口 */
    private String population;
    /** 面积 */
    private String area;
    /** 描述 */
    private String description;
    /** 语种 */
    private String lang;
    /** 状态 */
    private Integer status;

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLocType() { return locType; }
    public void setLocType(String locType) { this.locType = locType; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getCapital() { return capital; }
    public void setCapital(String capital) { this.capital = capital; }
    public String getPopulation() { return population; }
    public void setPopulation(String population) { this.population = population; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLang() { return lang; }
    public void setLang(String lang) { this.lang = lang; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
