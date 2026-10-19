package net.linlan.dsh.global.controller;

import net.linlan.dsh.common.Result;
import net.linlan.dsh.global.service.AggregationService;
import net.linlan.dsh.global.service.KnowledgeAggregator;
import net.linlan.dsh.sys.security.RequiresRole;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 汇聚控制器，提供手动触发、日志查询和统一视图查询（仅主任）
 * @author Linlan
 * CreatedAt 2026-10-14
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/aggregation")
public class AggregationController {

    private final AggregationService aggregationService;

    public AggregationController(AggregationService aggregationService) {
        this.aggregationService = aggregationService;
    }

    /**
     * 手动触发全量汇聚（仅主任）
     */
    @PostMapping("/trigger")
    @RequiresRole(0)
    public ResponseEntity<Result<KnowledgeAggregator.AggregationStats>> triggerFull() {
        var stats = aggregationService.triggerFullAggregation();
        return ResponseEntity.ok(Result.success(stats));
    }

    /**
     * 手动触发增量汇聚（仅主任）
     */
    @PostMapping("/incremental/{tenantId}")
    @RequiresRole(0)
    public ResponseEntity<Result<KnowledgeAggregator.AggregationStats>> triggerIncremental(@PathVariable Long tenantId) {
        var stats = aggregationService.triggerManualIncremental(tenantId);
        return ResponseEntity.ok(Result.success(stats));
    }

    /**
     * 查询汇聚日志（仅主任）
     */
    @GetMapping("/logs")
    @RequiresRole(0)
    public ResponseEntity<Result<List<Map<String, Object>>>> getLogs(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ResponseEntity.ok(Result.success(aggregationService.getAggregationLogs(pageNum, pageSize)));
    }

    /**
     * 查询统一监测视图（仅主任，支持分页/租户过滤/时间窗）
     * <p>P1-14：原无分页全量查询，改为参数化查询保证主任首页 P95 &lt; 3s。</p>
     * @param pageNum 页码（默认 1）
     * @param pageSize 每页条数（默认 20）
     * @param tenantId 租户 ID（可选，null 表示全租户）
     * @param timeWindowDays 时间窗天数（默认 30，用于 recent_article_count）
     */
    @GetMapping("/unified-monitor")
    @RequiresRole(0)
    public ResponseEntity<Result<List<Map<String, Object>>>> getUnifiedMonitor(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) Long tenantId,
            @RequestParam(defaultValue = "30") int timeWindowDays) {
        return ResponseEntity.ok(Result.success(
                aggregationService.getUnifiedMonitor(pageNum, pageSize, tenantId, timeWindowDays)));
    }

    /**
     * 查询统一知识图谱视图（仅主任）
     */
    @GetMapping("/unified-kg")
    @RequiresRole(0)
    public ResponseEntity<Result<List<Map<String, Object>>>> getUnifiedKg() {
        return ResponseEntity.ok(Result.success(aggregationService.getUnifiedKg()));
    }
}
