package net.linlan.dsh.qa.controller;

import net.linlan.dsh.common.Result;
import net.linlan.dsh.common.TenantContext;
import net.linlan.dsh.qa.entity.*;
import net.linlan.dsh.qa.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import net.linlan.dsh.sys.security.RequiresRole;

/**
 * 报告层控制器：数据分析+报告+问数
 * @author Linlan
 * CreatedAt 2026-10-18
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/report")
public class ReportController {

    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    @GetMapping("/templates")
    @RequiresRole(1)
    public ResponseEntity<Result<List<ReportTemplate>>> listTemplates() {
        return ResponseEntity.ok(Result.success(service.findAllTemplates(TenantContext.getTenantId())));
    }

    @GetMapping("/templates/{id}")
    @RequiresRole(1)
    public ResponseEntity<Result<ReportTemplate>> getTemplate(@PathVariable Long id) {
        return ResponseEntity.ok(Result.success(service.findTemplateById(id, TenantContext.getTenantId())));
    }

    @PostMapping("/templates")
    @RequiresRole(1)
    public ResponseEntity<Result<Long>> createTemplate(@RequestBody ReportTemplate template) {
        template.setTenantId(TenantContext.getTenantId());
        template.setCreatedBy(String.valueOf(TenantContext.getUserId()));
        return ResponseEntity.ok(Result.success(service.createTemplate(template)));
    }

    @GetMapping("/instances")
    @RequiresRole(1)
    public ResponseEntity<Result<List<ReportInstance>>> listInstances() {
        return ResponseEntity.ok(Result.success(service.findAllInstances(TenantContext.getTenantId())));
    }

    @GetMapping("/instances/{id}")
    @RequiresRole(1)
    public ResponseEntity<Result<ReportInstance>> getInstance(@PathVariable Long id) {
        return ResponseEntity.ok(Result.success(service.findInstanceById(id, TenantContext.getTenantId())));
    }

    @PostMapping("/instances")
    @RequiresRole(1)
    public ResponseEntity<Result<Long>> createInstance(@RequestBody ReportInstance instance) {
        instance.setTenantId(TenantContext.getTenantId());
        instance.setCreatedBy(String.valueOf(TenantContext.getUserId()));
        return ResponseEntity.ok(Result.success(service.createInstance(instance)));
    }

    @PostMapping("/instances/{id}/generate")
    @RequiresRole(1)
    public ResponseEntity<Result<Integer>> generateReport(@PathVariable Long id) {
        return ResponseEntity.ok(Result.success(service.generateReport(id)));
    }

    @PostMapping("/instances/{id}/publish")
    @RequiresRole(1)
    public ResponseEntity<Result<Integer>> publishReport(@PathVariable Long id) {
        return ResponseEntity.ok(Result.success(service.publishReport(id)));
    }

    @GetMapping("/instances/{id}/sections")
    @RequiresRole(1)
    public ResponseEntity<Result<List<ReportSection>>> listSections(@PathVariable Long id) {
        return ResponseEntity.ok(Result.success(service.findSectionsByInstanceId(id)));
    }

    @GetMapping("/templates/{id}/charts")
    @RequiresRole(1)
    public ResponseEntity<Result<List<ReportChartDef>>> listCharts(@PathVariable Long id) {
        return ResponseEntity.ok(Result.success(service.findChartsByTemplateId(id)));
    }

    @GetMapping("/qa/sessions")
    @RequiresRole(1)
    public ResponseEntity<Result<List<ReportQaSession>>> listQaSessions() {
        return ResponseEntity.ok(Result.success(service.findAllQaSessions(TenantContext.getTenantId())));
    }

    @PostMapping("/qa/sessions")
    @RequiresRole(1)
    public ResponseEntity<Result<Long>> createQaSession(@RequestBody ReportQaSession session) {
        session.setTenantId(TenantContext.getTenantId());
        session.setCreatedBy(String.valueOf(TenantContext.getUserId()));
        return ResponseEntity.ok(Result.success(service.createQaSession(session)));
    }

    @GetMapping("/qa/sessions/{sessionId}/queries")
    @RequiresRole(1)
    public ResponseEntity<Result<List<ReportQaQuery>>> listQaQueries(@PathVariable Long sessionId) {
        return ResponseEntity.ok(Result.success(service.findQaQueriesBySessionId(sessionId)));
    }

    @PostMapping("/qa/sessions/{sessionId}/queries")
    @RequiresRole(1)
    public ResponseEntity<Result<Long>> createQaQuery(@PathVariable Long sessionId, @RequestBody ReportQaQuery query) {
        query.setSessionId(sessionId);
        query.setTenantId(TenantContext.getTenantId());
        query.setCreatedBy(String.valueOf(TenantContext.getUserId()));
        return ResponseEntity.ok(Result.success(service.createQaQuery(query)));
    }

    @GetMapping("/analysis/tasks")
    @RequiresRole(1)
    public ResponseEntity<Result<List<ReportAnalysisTask>>> listAnalysisTasks() {
        return ResponseEntity.ok(Result.success(service.findAllAnalysisTasks(TenantContext.getTenantId())));
    }

    @PostMapping("/analysis/tasks")
    @RequiresRole(1)
    public ResponseEntity<Result<Long>> createAnalysisTask(@RequestBody ReportAnalysisTask task) {
        task.setTenantId(TenantContext.getTenantId());
        task.setCreatedBy(String.valueOf(TenantContext.getUserId()));
        return ResponseEntity.ok(Result.success(service.createAnalysisTask(task)));
    }

    @PostMapping("/analysis/tasks/{id}/complete")
    @RequiresRole(1)
    public ResponseEntity<Result<Integer>> completeAnalysisTask(@PathVariable Long id, @RequestParam String resultSummary) {
        return ResponseEntity.ok(Result.success(service.completeAnalysisTask(id, resultSummary)));
    }
}
