package net.linlan.dsh.qa.entity;

import net.linlan.dsh.common.entity.BaseEntity;

/**
 * 图表定义实体
 * @author Linlan
 * CreatedAt 2026-10-18
 * @version 1.0
 * @since 1.0
 */
public class ReportChartDef extends BaseEntity {
    /** 图表名称 */
    private String chartName;
    /** 图表类型：BAR/LINE/PIE/TABLE/HEATMAP/SCATTER */
    private String chartType;
    /** 数据查询SQL */
    private String dataQuery;
    /** X轴字段 */
    private String xAxis;
    /** Y轴字段 */
    private String yAxis;
    /** 系列配置（JSON） */
    private String seriesConfig;
    /** 关联模板ID */
    private Long templateId;

    public String getChartName() { return chartName; }
    public void setChartName(String chartName) { this.chartName = chartName; }
    public String getChartType() { return chartType; }
    public void setChartType(String chartType) { this.chartType = chartType; }
    public String getDataQuery() { return dataQuery; }
    public void setDataQuery(String dataQuery) { this.dataQuery = dataQuery; }
    public String getXAxis() { return xAxis; }
    public void setXAxis(String xAxis) { this.xAxis = xAxis; }
    public String getYAxis() { return yAxis; }
    public void setYAxis(String yAxis) { this.yAxis = yAxis; }
    public String getSeriesConfig() { return seriesConfig; }
    public void setSeriesConfig(String seriesConfig) { this.seriesConfig = seriesConfig; }
    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }
}
