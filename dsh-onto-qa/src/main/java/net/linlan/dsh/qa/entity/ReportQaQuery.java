package net.linlan.dsh.qa.entity;

import net.linlan.dsh.common.entity.BaseEntity;

/**
 * 问数查询记录实体
 * @author Linlan
 * CreatedAt 2026-10-18
 * @version 1.0
 * @since 1.0
 */
public class ReportQaQuery extends BaseEntity {
    /** 会话ID */
    private Long sessionId;
    /** 查询文本（自然语言） */
    private String queryText;
    /** 翻译后的SQL */
    private String translatedSql;
    /** 结果数据（JSON） */
    private String resultData;
    /** 图表建议 */
    private String chartSuggestion;
    /** 处理状态：PENDING/SUCCESS/FAILED */
    private String processStatus;

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public String getQueryText() { return queryText; }
    public void setQueryText(String queryText) { this.queryText = queryText; }
    public String getTranslatedSql() { return translatedSql; }
    public void setTranslatedSql(String translatedSql) { this.translatedSql = translatedSql; }
    public String getResultData() { return resultData; }
    public void setResultData(String resultData) { this.resultData = resultData; }
    public String getChartSuggestion() { return chartSuggestion; }
    public void setChartSuggestion(String chartSuggestion) { this.chartSuggestion = chartSuggestion; }
    public String getProcessStatus() { return processStatus; }
    public void setProcessStatus(String processStatus) { this.processStatus = processStatus; }
}
