package net.linlan.dsh.report.entity;

import net.linlan.dsh.common.entity.BaseEntity;

/**
 * 报告模板实体
 * @author Linlan
 * CreatedAt 2026-10-18
 * @version 1.0
 * @since 1.0
 */
public class ReportTemplate extends BaseEntity {
    /** 模板名称 */
    private String templateName;
    /** 模板类型：ANALYSIS/WEEKLY/MONTHLY/CUSTOM */
    private String templateType;
    /** 描述 */
    private String description;
    /** 章节配置（JSON） */
    private String sectionConfig;
    /** 图表配置（JSON） */
    private String chartConfig;
    /** 状态：0=草稿 1=发布 2=归档 */
    private Integer status;

    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }
    public String getTemplateType() { return templateType; }
    public void setTemplateType(String templateType) { this.templateType = templateType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSectionConfig() { return sectionConfig; }
    public void setSectionConfig(String sectionConfig) { this.sectionConfig = sectionConfig; }
    public String getChartConfig() { return chartConfig; }
    public void setChartConfig(String chartConfig) { this.chartConfig = chartConfig; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
