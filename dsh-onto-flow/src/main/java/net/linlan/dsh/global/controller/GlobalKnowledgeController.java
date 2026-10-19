package net.linlan.dsh.global.controller;

import net.linlan.dsh.common.Result;
import net.linlan.dsh.global.entity.EntityMergeLog;
import net.linlan.dsh.global.service.AggregationService;
import net.linlan.dsh.global.service.EntityMergeService;
import net.linlan.dsh.sys.security.RequiresRole;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 全局知识查询控制器，提供全局实体/关系/图谱查询和主任决策概览（仅主任）
 * @author Linlan
 * CreatedAt 2026-10-14
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/global")
public class GlobalKnowledgeController {

    private final AggregationService aggregationService;
    private final EntityMergeService entityMergeService;

    public GlobalKnowledgeController(AggregationService aggregationService, EntityMergeService entityMergeService) {
        this.aggregationService = aggregationService;
        this.entityMergeService = entityMergeService;
    }

    /**
     * 全局发现实体列表（仅主任）
     */
    @GetMapping("/entities")
    @RequiresRole(0)
    public ResponseEntity<Result<List<Map<String, Object>>>> getEntities(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ResponseEntity.ok(Result.success(aggregationService.getGlobalEntities(pageNum, pageSize)));
    }

    /**
     * 全局双边关系列表（仅主任）
     */
    @GetMapping("/relations")
    @RequiresRole(0)
    public ResponseEntity<Result<List<Map<String, Object>>>> getRelations(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ResponseEntity.ok(Result.success(aggregationService.getGlobalRelations(pageNum, pageSize)));
    }

    /**
     * 全局知识图谱节点（仅主任）
     */
    @GetMapping("/kg/nodes")
    @RequiresRole(0)
    public ResponseEntity<Result<List<Map<String, Object>>>> getKgNodes(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ResponseEntity.ok(Result.success(aggregationService.getGlobalKgNodes(pageNum, pageSize)));
    }

    /**
     * 全局知识图谱边（仅主任）
     */
    @GetMapping("/kg/edges")
    @RequiresRole(0)
    public ResponseEntity<Result<List<Map<String, Object>>>> getKgEdges(
            @RequestParam(defaultValue = "17") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ResponseEntity.ok(Result.success(aggregationService.getGlobalKgEdges(pageNum, pageSize)));
    }

    /**
     * 主任决策概览（仅主任）
     */
    @GetMapping("/director/overview")
    @RequiresRole(0)
    public ResponseEntity<Result<Map<String, Object>>> getDirectorOverview() {
        return ResponseEntity.ok(Result.success(aggregationService.getDirectorOverview()));
    }

    @PostMapping("/entities/merge")
    @RequiresRole(0)
    public ResponseEntity<Result<Long>> mergeEntities(@RequestBody Map<String, Object> body) {
        Long sourceId = ((Number) body.get("sourceEntityId")).longValue();
        Long targetId = ((Number) body.get("targetEntityId")).longValue();
        String entityType = (String) body.get("entityType");
        Float score = body.get("similarityScore") != null ? ((Number) body.get("similarityScore")).floatValue() : null;
        String strategy = (String) body.getOrDefault("strategy", "MANUAL");
        Long logId = entityMergeService.mergeEntities(sourceId, targetId, entityType, score, strategy);
        return ResponseEntity.ok(Result.success("合并成功", logId));
    }

    @PostMapping("/entities/auto-merge")
    @RequiresRole(0)
    public ResponseEntity<Result<Integer>> autoMerge(@RequestParam Long tenantId) {
        int merged = entityMergeService.autoMergeByNameMatch(tenantId);
        return ResponseEntity.ok(Result.success("自动合并" + merged + "对实体", merged));
    }

    @GetMapping("/merge-logs")
    @RequiresRole(0)
    public ResponseEntity<Result<List<EntityMergeLog>>> getMergeLogs(
            @RequestParam Long tenantId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ResponseEntity.ok(Result.success(entityMergeService.getMergeLogs(tenantId, pageNum, pageSize)));
    }
}
