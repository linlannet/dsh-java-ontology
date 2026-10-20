package net.linlan.dsh.metric.entity;

import net.linlan.dsh.common.entity.BaseEntity;

/**
 * 指标定义实体（区分原子指标/派生指标/复合指标）
 * @author Linlan
 * CreatedAt 2026-10-20
 * @version 1.0
 * @since 1.0
 */
public class MetricDefinition extends BaseEntity {
    /** 指标名称 */
    private String metricName;
    /** 指标编码 */
    private String metricCode;
    /** 指标类别：ATOM=原子/DERIVED=派生/COMPOSITE=复合 */
    private String category;
    /** 关联业务对象ID */
    private Long businessObjectId;
    /** 关联流程ID */
    private Long processId;
    /** 关联关键决策点ID */
    private Long decisionPointId;
    /** 计算公式（派生/复合指标用） */
    private String formula;
    /** 计量单位 */
    private String unit;
    /** 描述 */
    private String description;
    /** 状态：0=草稿 1=启用 2=停用 */
    private Integer status;

    public String getMetricName() { return metricName; }
    public void setMetricName(String metricName) { this.metricName = metricName; }
    public String getMetricCode() { return metricCode; }
    public void setMetricCode(String metricCode) { this.metricCode = metricCode; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Long getBusinessObjectId() { return businessObjectId; }
    public void setBusinessObjectId(Long businessObjectId) { this.businessObjectId = businessObjectId; }
    public Long getProcessId() { return processId; }
    public void setProcessId(Long processId) { this.processId = processId; }
    public Long getDecisionPointId() { return decisionPointId; }
    public void setDecisionPointId(Long decisionPointId) { this.decisionPointId = decisionPointId; }
    public String getFormula() { return formula; }
    public void setFormula(String formula) { this.formula = formula; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
