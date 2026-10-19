package net.linlan.dsh.ontology.entity;

import net.linlan.dsh.common.entity.BaseEntity;

/**
 * 本体事件类型实体类
 * <p>对应底层共享表 onto_ontology_event_type，通过 domain 列支持多业务方向隔离。</p>
 * @author Linlan
 * CreatedAt 2026-10-18
 * @version 1.0
 * @since 0.2.1
 */
public class OntologyEventType extends BaseEntity {
    /** 业务方向：siis/factory/ecommerce/city-ops */
    private String domain;
    /** 事件类型（默认中文） */
    private String eventType;
    /** 描述 */
    private String description;
    /** 父类型ID（树形结构） */
    private Long parentId;
    /** 排序 */
    private Integer sortOrder;
    /** 状态 */
    private Integer status;

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
