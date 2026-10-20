package net.linlan.dsh.metric.controller;

import net.linlan.dsh.common.Result;
import net.linlan.dsh.common.TenantContext;
import net.linlan.dsh.metric.entity.*;
import net.linlan.dsh.metric.service.MetricService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import net.linlan.dsh.sys.security.RequiresRole;

/**
 * 指标层控制器：业务对象/流程/决策点 + 指标定义 + 依赖树
 * @author Linlan
 * CreatedAt 2026-10-20
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/metric")
public class MetricController {

    private final MetricService service;

    public MetricController(MetricService service) {
        this.service = service;
    }

    @GetMapping("/business-objects")
    @RequiresRole(1)
    public ResponseEntity<Result<List<MetricBusinessObject>>> listBusinessObjects() {
        return ResponseEntity.ok(Result.success(service.findAllBusinessObjects(TenantContext.getTenantId())));
    }

    @PostMapping("/business-objects")
    @RequiresRole(1)
    public ResponseEntity<Result<Long>> createBusinessObject(@RequestBody MetricBusinessObject o) {
        o.setTenantId(TenantContext.getTenantId());
        o.setCreatedBy(String.valueOf(TenantContext.getUserId()));
        return ResponseEntity.ok(Result.success(service.createBusinessObject(o)));
    }

    @GetMapping("/processes")
    @RequiresRole(1)
    public ResponseEntity<Result<List<MetricProcess>>> listProcesses() {
        return ResponseEntity.ok(Result.success(service.findAllProcesses(TenantContext.getTenantId())));
    }

    @GetMapping("/business-objects/{boId}/processes")
    @RequiresRole(1)
    public ResponseEntity<Result<List<MetricProcess>>> listProcessesByBo(@PathVariable Long boId) {
        return ResponseEntity.ok(Result.success(service.findProcessesByBusinessObjectId(boId)));
    }

    @PostMapping("/processes")
    @RequiresRole(1)
    public ResponseEntity<Result<Long>> createProcess(@RequestBody MetricProcess p) {
        p.setTenantId(TenantContext.getTenantId());
        p.setCreatedBy(String.valueOf(TenantContext.getUserId()));
        return ResponseEntity.ok(Result.success(service.createProcess(p)));
    }

    @GetMapping("/decision-points")
    @RequiresRole(1)
    public ResponseEntity<Result<List<MetricDecisionPoint>>> listDecisionPoints() {
        return ResponseEntity.ok(Result.success(service.findAllDecisionPoints(TenantContext.getTenantId())));
    }

    @GetMapping("/processes/{processId}/decision-points")
    @RequiresRole(1)
    public ResponseEntity<Result<List<MetricDecisionPoint>>> listDecisionPointsByProcess(@PathVariable Long processId) {
        return ResponseEntity.ok(Result.success(service.findDecisionPointsByProcessId(processId)));
    }

    @PostMapping("/decision-points")
    @RequiresRole(1)
    public ResponseEntity<Result<Long>> createDecisionPoint(@RequestBody MetricDecisionPoint d) {
        d.setTenantId(TenantContext.getTenantId());
        d.setCreatedBy(String.valueOf(TenantContext.getUserId()));
        return ResponseEntity.ok(Result.success(service.createDecisionPoint(d)));
    }

    @GetMapping("/definitions")
    @RequiresRole(1)
    public ResponseEntity<Result<List<MetricDefinition>>> listDefinitions(@RequestParam(required = false) String category) {
        Long tenantId = TenantContext.getTenantId();
        if (category != null && !category.isEmpty()) {
            return ResponseEntity.ok(Result.success(service.findDefinitionsByCategory(category, tenantId)));
        }
        return ResponseEntity.ok(Result.success(service.findAllDefinitions(tenantId)));
    }

    @GetMapping("/definitions/{id}")
    @RequiresRole(1)
    public ResponseEntity<Result<MetricDefinition>> getDefinition(@PathVariable Long id) {
        return ResponseEntity.ok(Result.success(service.findDefinitionById(id, TenantContext.getTenantId())));
    }

    @PostMapping("/definitions")
    @RequiresRole(1)
    public ResponseEntity<Result<Long>> createDefinition(@RequestBody MetricDefinition m) {
        m.setTenantId(TenantContext.getTenantId());
        m.setCreatedBy(String.valueOf(TenantContext.getUserId()));
        return ResponseEntity.ok(Result.success(service.createDefinition(m)));
    }

    @GetMapping("/dependencies")
    @RequiresRole(1)
    public ResponseEntity<Result<List<MetricDependency>>> listDependencies() {
        return ResponseEntity.ok(Result.success(service.findAllDependencies(TenantContext.getTenantId())));
    }

    @PostMapping("/dependencies")
    @RequiresRole(1)
    public ResponseEntity<Result<Long>> createDependency(@RequestBody MetricDependency d) {
        d.setTenantId(TenantContext.getTenantId());
        d.setCreatedBy(String.valueOf(TenantContext.getUserId()));
        return ResponseEntity.ok(Result.success(service.createDependency(d)));
    }

    @GetMapping("/definitions/{id}/dependency-tree")
    @RequiresRole(1)
    public ResponseEntity<Result<MetricService.MetricTreeNode>> getDependencyTree(@PathVariable Long id) {
        return ResponseEntity.ok(Result.success(service.buildDependencyTree(id, TenantContext.getTenantId())));
    }
}
