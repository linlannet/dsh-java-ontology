package net.linlan.dsh.qa.entity;

import net.linlan.dsh.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 分析任务实体
 * @author Linlan
 * CreatedAt 2026-10-18
 * @version 1.0
 * @since 1.0
 */
public class ReportAnalysisTask extends BaseEntity {
    /** 任务名称 */
    private String taskName;
    /** 任务类型：TREND/CORRELATION/COMPARISON/PREDICTION */
    private String taskType;
    /** 数据范围 */
    private String dataRange;
    /** 分析配置（JSON） */
    private String analysisConfig;
    /** 结果摘要 */
    private String resultSummary;
    /** 状态：PENDING/RUNNING/SUCCESS/FAILED */
    private String status;
    /** 开始时间 */
    private LocalDateTime startTime;
    /** 结束时间 */
    private LocalDateTime endTime;

    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }
    public String getDataRange() { return dataRange; }
    public void setDataRange(String dataRange) { this.dataRange = dataRange; }
    public String getAnalysisConfig() { return analysisConfig; }
    public void setAnalysisConfig(String analysisConfig) { this.analysisConfig = analysisConfig; }
    public String getResultSummary() { return resultSummary; }
    public void setResultSummary(String resultSummary) { this.resultSummary = resultSummary; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}
