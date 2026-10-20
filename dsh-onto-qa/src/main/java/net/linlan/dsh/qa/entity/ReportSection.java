package net.linlan.dsh.qa.entity;

import net.linlan.dsh.common.entity.BaseEntity;

/**
 * 报告章节实体
 * @author Linlan
 * CreatedAt 2026-10-18
 * @version 1.0
 * @since 1.0
 */
public class ReportSection extends BaseEntity {
    /** 报告实例ID */
    private Long instanceId;
    /** 章节序号 */
    private Integer sectionIndex;
    /** 章节标题 */
    private String sectionTitle;
    /** 章节类型：TEXT/CHART/TABLE/SUMMARY */
    private String sectionType;
    /** 章节内容 */
    private String content;
    /** 图表数据（JSON） */
    private String chartData;

    public Long getInstanceId() { return instanceId; }
    public void setInstanceId(Long instanceId) { this.instanceId = instanceId; }
    public Integer getSectionIndex() { return sectionIndex; }
    public void setSectionIndex(Integer sectionIndex) { this.sectionIndex = sectionIndex; }
    public String getSectionTitle() { return sectionTitle; }
    public void setSectionTitle(String sectionTitle) { this.sectionTitle = sectionTitle; }
    public String getSectionType() { return sectionType; }
    public void setSectionType(String sectionType) { this.sectionType = sectionType; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getChartData() { return chartData; }
    public void setChartData(String chartData) { this.chartData = chartData; }
}
