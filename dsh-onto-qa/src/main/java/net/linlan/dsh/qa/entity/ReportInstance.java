package net.linlan.dsh.qa.entity;

import net.linlan.dsh.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 报告实例实体
 * @author Linlan
 * CreatedAt 2026-10-18
 * @version 1.0
 * @since 1.0
 */
public class ReportInstance extends BaseEntity {
    /** 模板ID */
    private Long templateId;
    /** 报告名称 */
    private String reportName;
    /** 报告类型 */
    private String reportType;
    /** 数据来源 */
    private String dataSource;
    /** 参数（JSON） */
    private String parameters;
    /** 状态：DRAFT/GENERATED/PUBLISHED */
    private String status;
    /** 生成时间 */
    private LocalDateTime generatedTime;
    /** 发布时间 */
    private LocalDateTime publishedTime;
    /** 旧报告ID（gwy_display_rpt_report 迁移关联，ADR-004） */
    private Long legacyReportId;

    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }
    public String getReportName() { return reportName; }
    public void setReportName(String reportName) { this.reportName = reportName; }
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }
    public String getParameters() { return parameters; }
    public void setParameters(String parameters) { this.parameters = parameters; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getGeneratedTime() { return generatedTime; }
    public void setGeneratedTime(LocalDateTime generatedTime) { this.generatedTime = generatedTime; }
    public LocalDateTime getPublishedTime() { return publishedTime; }
    public void setPublishedTime(LocalDateTime publishedTime) { this.publishedTime = publishedTime; }
    public Long getLegacyReportId() { return legacyReportId; }
    public void setLegacyReportId(Long legacyReportId) { this.legacyReportId = legacyReportId; }
}
