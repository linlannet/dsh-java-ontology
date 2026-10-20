package net.linlan.dsh.metric.entity;

import net.linlan.dsh.common.entity.BaseEntity;

/**
 * 业务对象实体（被度量的业务实体类型，如"文献""作者""课题"）
 * @author Linlan
 * CreatedAt 2026-10-20
 * @version 1.0
 * @since 1.0
 */
public class MetricBusinessObject extends BaseEntity {
    /** 业务对象名称 */
    private String objectName;
    /** 业务对象编码 */
    private String objectCode;
    /** 对象类型：ENTITY/PROCESS/RESOURCE */
    private String objectType;
    /** 数据源表名 */
    private String sourceTable;
    /** 描述 */
    private String description;
    /** 状态：0=草稿 1=启用 2=停用 */
    private Integer status;

    public String getObjectName() { return objectName; }
    public void setObjectName(String objectName) { this.objectName = objectName; }
    public String getObjectCode() { return objectCode; }
    public void setObjectCode(String objectCode) { this.objectCode = objectCode; }
    public String getObjectType() { return objectType; }
    public void setObjectType(String objectType) { this.objectType = objectType; }
    public String getSourceTable() { return sourceTable; }
    public void setSourceTable(String sourceTable) { this.sourceTable = sourceTable; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
