package net.linlan.dsh.qa.service;

import net.linlan.dsh.qa.entity.*;
import net.linlan.dsh.qa.repository.ReportRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 报告层服务类
 * @author Linlan
 * CreatedAt 2026-10-18
 * @version 1.0
 * @since 1.0
 */
@Service
public class ReportService {

    private final ReportRepository repository;

    public ReportService(ReportRepository repository) {
        this.repository = repository;
    }

    public List<ReportTemplate> findAllTemplates(Long tenantId) {
        return repository.findAllTemplates(tenantId);
    }

    public ReportTemplate findTemplateById(Long id, Long tenantId) {
        return repository.findTemplateById(id, tenantId);
    }

    public long createTemplate(ReportTemplate template) {
        if (template.getStatus() == null) template.setStatus(0);
        return repository.insertTemplate(template);
    }

    public List<ReportInstance> findAllInstances(Long tenantId) {
        return repository.findAllInstances(tenantId);
    }

    public ReportInstance findInstanceById(Long id, Long tenantId) {
        return repository.findInstanceById(id, tenantId);
    }

    public long createInstance(ReportInstance instance) {
        if (instance.getStatus() == null) instance.setStatus("DRAFT");
        return repository.insertInstance(instance);
    }

    public int generateReport(Long id) {
        return repository.updateInstanceStatus(id, "GENERATED", LocalDateTime.now());
    }

    public int publishReport(Long id) {
        return repository.updateInstanceStatus(id, "PUBLISHED", LocalDateTime.now());
    }

    public List<ReportSection> findSectionsByInstanceId(Long instanceId) {
        return repository.findSectionsByInstanceId(instanceId);
    }

    public List<ReportChartDef> findChartsByTemplateId(Long templateId) {
        return repository.findChartsByTemplateId(templateId);
    }

    public List<ReportQaSession> findAllQaSessions(Long tenantId) {
        return repository.findAllQaSessions(tenantId);
    }

    public long createQaSession(ReportQaSession session) {
        if (session.getStatus() == null) session.setStatus("ACTIVE");
        return repository.insertQaSession(session);
    }

    public List<ReportQaQuery> findQaQueriesBySessionId(Long sessionId) {
        return repository.findQaQueriesBySessionId(sessionId);
    }

    public long createQaQuery(ReportQaQuery query) {
        if (query.getProcessStatus() == null) query.setProcessStatus("PENDING");
        return repository.insertQaQuery(query);
    }

    public List<ReportAnalysisTask> findAllAnalysisTasks(Long tenantId) {
        return repository.findAllAnalysisTasks(tenantId);
    }

    public long createAnalysisTask(ReportAnalysisTask task) {
        if (task.getStatus() == null) task.setStatus("PENDING");
        return repository.insertAnalysisTask(task);
    }

    public int completeAnalysisTask(Long id, String resultSummary) {
        return repository.updateAnalysisTaskStatus(id, "SUCCESS", resultSummary, LocalDateTime.now());
    }
}
