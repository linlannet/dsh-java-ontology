package net.linlan.dsh.qa.repository;

import net.linlan.dsh.common.PageResult;
import net.linlan.dsh.qa.entity.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 报告层仓储类
 * @author Linlan
 * CreatedAt 2026-10-18
 * @version 1.0
 * @since 1.0
 */
@Repository
public class ReportRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReportRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ReportTemplate> findAllTemplates(Long tenantId) {
        String sql = "SELECT * FROM onto_report_template WHERE tenant_id = ? AND del_flag = 0 ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, new ReportTemplateRowMapper(), tenantId);
    }

    public ReportTemplate findTemplateById(Long id, Long tenantId) {
        String sql = "SELECT * FROM onto_report_template WHERE id = ? AND tenant_id = ? AND del_flag = 0";
        return jdbcTemplate.queryForObject(sql, new ReportTemplateRowMapper(), id, tenantId);
    }

    public long insertTemplate(ReportTemplate t) {
        String sql = "INSERT INTO onto_report_template (template_name, template_type, description, section_config, chart_config, status, tenant_id, created_by) VALUES (?,?,?,?,?,?,?,?)";
        return jdbcTemplate.queryForObject(sql + " RETURNING id", Long.class,
                t.getTemplateName(), t.getTemplateType(), t.getDescription(), t.getSectionConfig(), t.getChartConfig(), t.getStatus(), t.getTenantId(), t.getCreatedBy());
    }

    public List<ReportInstance> findAllInstances(Long tenantId) {
        String sql = "SELECT * FROM onto_report_instance WHERE tenant_id = ? AND del_flag = 0 ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, new ReportInstanceRowMapper(), tenantId);
    }

    public ReportInstance findInstanceById(Long id, Long tenantId) {
        String sql = "SELECT * FROM onto_report_instance WHERE id = ? AND tenant_id = ? AND del_flag = 0";
        return jdbcTemplate.queryForObject(sql, new ReportInstanceRowMapper(), id, tenantId);
    }

    public long insertInstance(ReportInstance r) {
        String sql = "INSERT INTO onto_report_instance (template_id, report_name, report_type, data_source, parameters, status, tenant_id, created_by) VALUES (?,?,?,?,?,?,?,?)";
        return jdbcTemplate.queryForObject(sql + " RETURNING id", Long.class,
                r.getTemplateId(), r.getReportName(), r.getReportType(), r.getDataSource(), r.getParameters(), r.getStatus(), r.getTenantId(), r.getCreatedBy());
    }

    public int updateInstanceStatus(Long id, String status, LocalDateTime time) {
        String col = "GENERATED".equals(status) ? "generated_time" : "published_time";
        String sql = "UPDATE onto_report_instance SET status = ?, " + col + " = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        return jdbcTemplate.update(sql, status, Timestamp.valueOf(time), id);
    }

    public List<ReportSection> findSectionsByInstanceId(Long instanceId) {
        String sql = "SELECT * FROM onto_report_section WHERE instance_id = ? AND del_flag = 0 ORDER BY section_index ASC";
        return jdbcTemplate.query(sql, new ReportSectionRowMapper(), instanceId);
    }

    public List<ReportQaSession> findAllQaSessions(Long tenantId) {
        String sql = "SELECT * FROM onto_report_qa_session WHERE tenant_id = ? AND del_flag = 0 ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, new ReportQaSessionRowMapper(), tenantId);
    }

    public long insertQaSession(ReportQaSession s) {
        String sql = "INSERT INTO onto_report_qa_session (session_name, user_question, context, status, tenant_id, created_by) VALUES (?,?,?,?,?,?)";
        return jdbcTemplate.queryForObject(sql + " RETURNING id", Long.class,
                s.getSessionName(), s.getUserQuestion(), s.getContext(), s.getStatus(), s.getTenantId(), s.getCreatedBy());
    }

    public List<ReportQaQuery> findQaQueriesBySessionId(Long sessionId) {
        String sql = "SELECT * FROM onto_report_qa_query WHERE session_id = ? AND del_flag = 0 ORDER BY created_at ASC";
        return jdbcTemplate.query(sql, new ReportQaQueryRowMapper(), sessionId);
    }

    public long insertQaQuery(ReportQaQuery q) {
        String sql = "INSERT INTO onto_report_qa_query (session_id, query_text, translated_sql, result_data, chart_suggestion, process_status, tenant_id, created_by) VALUES (?,?,?,?,?,?,?,?)";
        return jdbcTemplate.queryForObject(sql + " RETURNING id", Long.class,
                q.getSessionId(), q.getQueryText(), q.getTranslatedSql(), q.getResultData(), q.getChartSuggestion(), q.getProcessStatus(), q.getTenantId(), q.getCreatedBy());
    }

    public List<ReportAnalysisTask> findAllAnalysisTasks(Long tenantId) {
        String sql = "SELECT * FROM onto_report_analysis_task WHERE tenant_id = ? AND del_flag = 0 ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, new ReportAnalysisTaskRowMapper(), tenantId);
    }

    public long insertAnalysisTask(ReportAnalysisTask t) {
        String sql = "INSERT INTO onto_report_analysis_task (task_name, task_type, data_range, analysis_config, status, tenant_id, created_by) VALUES (?,?,?,?,?,?,?)";
        return jdbcTemplate.queryForObject(sql + " RETURNING id", Long.class,
                t.getTaskName(), t.getTaskType(), t.getDataRange(), t.getAnalysisConfig(), t.getStatus(), t.getTenantId(), t.getCreatedBy());
    }

    public int updateAnalysisTaskStatus(Long id, String status, String resultSummary, LocalDateTime endTime) {
        String sql = "UPDATE onto_report_analysis_task SET status = ?, result_summary = ?, end_time = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        return jdbcTemplate.update(sql, status, resultSummary, endTime != null ? Timestamp.valueOf(endTime) : null, id);
    }

    public List<ReportChartDef> findChartsByTemplateId(Long templateId) {
        String sql = "SELECT * FROM onto_report_chart_def WHERE template_id = ? AND del_flag = 0 ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, new ReportChartDefRowMapper(), templateId);
    }

    static LocalDateTime toLdt(Timestamp ts) { return ts != null ? ts.toLocalDateTime() : null; }
    static Timestamp toTs(LocalDateTime ldt) { return ldt != null ? Timestamp.valueOf(ldt) : null; }

    static class ReportTemplateRowMapper implements RowMapper<ReportTemplate> {
        @Override
        public ReportTemplate mapRow(ResultSet rs, int rowNum) throws SQLException {
            ReportTemplate e = new ReportTemplate();
            e.setId(rs.getLong("id"));
            e.setTemplateName(rs.getString("template_name"));
            e.setTemplateType(rs.getString("template_type"));
            e.setDescription(rs.getString("description"));
            e.setSectionConfig(rs.getString("section_config"));
            e.setChartConfig(rs.getString("chart_config"));
            e.setStatus(rs.getInt("status"));
            e.setTenantId(rs.getObject("tenant_id", Long.class));
            e.setCreatedBy(rs.getString("created_by"));
            e.setCreatedAt(toLdt(rs.getTimestamp("created_at")));
            e.setUpdatedBy(rs.getString("updated_by"));
            e.setUpdatedAt(toLdt(rs.getTimestamp("updated_at")));
            e.setDelFlag(rs.getInt("del_flag"));
            e.setRemark(rs.getString("remark"));
            return e;
        }
    }

    static class ReportInstanceRowMapper implements RowMapper<ReportInstance> {
        @Override
        public ReportInstance mapRow(ResultSet rs, int rowNum) throws SQLException {
            ReportInstance e = new ReportInstance();
            e.setId(rs.getLong("id"));
            e.setTemplateId(rs.getObject("template_id", Long.class));
            e.setReportName(rs.getString("report_name"));
            e.setReportType(rs.getString("report_type"));
            e.setDataSource(rs.getString("data_source"));
            e.setParameters(rs.getString("parameters"));
            e.setStatus(rs.getString("status"));
            e.setGeneratedTime(toLdt(rs.getTimestamp("generated_time")));
            e.setPublishedTime(toLdt(rs.getTimestamp("published_time")));
            e.setTenantId(rs.getObject("tenant_id", Long.class));
            e.setCreatedBy(rs.getString("created_by"));
            e.setCreatedAt(toLdt(rs.getTimestamp("created_at")));
            e.setUpdatedBy(rs.getString("updated_by"));
            e.setUpdatedAt(toLdt(rs.getTimestamp("updated_at")));
            e.setDelFlag(rs.getInt("del_flag"));
            e.setRemark(rs.getString("remark"));
            return e;
        }
    }

    static class ReportSectionRowMapper implements RowMapper<ReportSection> {
        @Override
        public ReportSection mapRow(ResultSet rs, int rowNum) throws SQLException {
            ReportSection e = new ReportSection();
            e.setId(rs.getLong("id"));
            e.setInstanceId(rs.getObject("instance_id", Long.class));
            e.setSectionIndex(rs.getInt("section_index"));
            e.setSectionTitle(rs.getString("section_title"));
            e.setSectionType(rs.getString("section_type"));
            e.setContent(rs.getString("content"));
            e.setChartData(rs.getString("chart_data"));
            e.setTenantId(rs.getObject("tenant_id", Long.class));
            e.setCreatedBy(rs.getString("created_by"));
            e.setCreatedAt(toLdt(rs.getTimestamp("created_at")));
            e.setDelFlag(rs.getInt("del_flag"));
            return e;
        }
    }

    static class ReportChartDefRowMapper implements RowMapper<ReportChartDef> {
        @Override
        public ReportChartDef mapRow(ResultSet rs, int rowNum) throws SQLException {
            ReportChartDef e = new ReportChartDef();
            e.setId(rs.getLong("id"));
            e.setChartName(rs.getString("chart_name"));
            e.setChartType(rs.getString("chart_type"));
            e.setDataQuery(rs.getString("data_query"));
            e.setXAxis(rs.getString("x_axis"));
            e.setYAxis(rs.getString("y_axis"));
            e.setSeriesConfig(rs.getString("series_config"));
            e.setTemplateId(rs.getObject("template_id", Long.class));
            e.setTenantId(rs.getObject("tenant_id", Long.class));
            e.setCreatedBy(rs.getString("created_by"));
            e.setCreatedAt(toLdt(rs.getTimestamp("created_at")));
            e.setDelFlag(rs.getInt("del_flag"));
            return e;
        }
    }

    static class ReportQaSessionRowMapper implements RowMapper<ReportQaSession> {
        @Override
        public ReportQaSession mapRow(ResultSet rs, int rowNum) throws SQLException {
            ReportQaSession e = new ReportQaSession();
            e.setId(rs.getLong("id"));
            e.setSessionName(rs.getString("session_name"));
            e.setUserQuestion(rs.getString("user_question"));
            e.setContext(rs.getString("context"));
            e.setStatus(rs.getString("status"));
            e.setTenantId(rs.getObject("tenant_id", Long.class));
            e.setCreatedBy(rs.getString("created_by"));
            e.setCreatedAt(toLdt(rs.getTimestamp("created_at")));
            e.setDelFlag(rs.getInt("del_flag"));
            return e;
        }
    }

    static class ReportQaQueryRowMapper implements RowMapper<ReportQaQuery> {
        @Override
        public ReportQaQuery mapRow(ResultSet rs, int rowNum) throws SQLException {
            ReportQaQuery e = new ReportQaQuery();
            e.setId(rs.getLong("id"));
            e.setSessionId(rs.getObject("session_id", Long.class));
            e.setQueryText(rs.getString("query_text"));
            e.setTranslatedSql(rs.getString("translated_sql"));
            e.setResultData(rs.getString("result_data"));
            e.setChartSuggestion(rs.getString("chart_suggestion"));
            e.setProcessStatus(rs.getString("process_status"));
            e.setTenantId(rs.getObject("tenant_id", Long.class));
            e.setCreatedBy(rs.getString("created_by"));
            e.setCreatedAt(toLdt(rs.getTimestamp("created_at")));
            e.setDelFlag(rs.getInt("del_flag"));
            return e;
        }
    }

    static class ReportAnalysisTaskRowMapper implements RowMapper<ReportAnalysisTask> {
        @Override
        public ReportAnalysisTask mapRow(ResultSet rs, int rowNum) throws SQLException {
            ReportAnalysisTask e = new ReportAnalysisTask();
            e.setId(rs.getLong("id"));
            e.setTaskName(rs.getString("task_name"));
            e.setTaskType(rs.getString("task_type"));
            e.setDataRange(rs.getString("data_range"));
            e.setAnalysisConfig(rs.getString("analysis_config"));
            e.setResultSummary(rs.getString("result_summary"));
            e.setStatus(rs.getString("status"));
            e.setStartTime(toLdt(rs.getTimestamp("start_time")));
            e.setEndTime(toLdt(rs.getTimestamp("end_time")));
            e.setTenantId(rs.getObject("tenant_id", Long.class));
            e.setCreatedBy(rs.getString("created_by"));
            e.setCreatedAt(toLdt(rs.getTimestamp("created_at")));
            e.setDelFlag(rs.getInt("del_flag"));
            return e;
        }
    }
}
