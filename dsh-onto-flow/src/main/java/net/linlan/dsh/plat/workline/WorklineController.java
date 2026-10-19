package net.linlan.dsh.plat.workline;

import net.linlan.dsh.common.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import net.linlan.dsh.sys.security.RequiresRole;

/**
 * 工作流控制器
 * @author Linlan
 * CreatedAt 2026-10-14
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/workline")
public class WorklineController {

    private final WorklineService worklineService;

    public WorklineController(WorklineService worklineService) {
        this.worklineService = worklineService;
    }

    /**
     * 激活主题，启动工作流
     */
    @PostMapping("/topic/{topicId}/activate")
    @RequiresRole(1)
    public ResponseEntity<Result<WorklineContext>> activateTopic(@PathVariable Long topicId) {
        WorklineContext context = worklineService.activateTopic(topicId);
        return ResponseEntity.ok(Result.success(context));
    }

    /**
     * 查询工作流状态
     */
    @GetMapping("/{worklineId}/status")
    @RequiresRole(1)
    public ResponseEntity<Result<WorklineContext>> getStatus(@PathVariable String worklineId) {
        WorklineContext context = worklineService.getStatus(worklineId);
        if (context == null) {
            return ResponseEntity.ok(Result.notFound("工作流不存在: " + worklineId));
        }
        return ResponseEntity.ok(Result.success(context));
    }

    /**
     * 查询工作流进度
     */
    @GetMapping("/{worklineId}/progress")
    @RequiresRole(1)
    public ResponseEntity<Result<List<WorklinePhaseResult>>> getProgress(@PathVariable String worklineId) {
        List<WorklinePhaseResult> results = worklineService.getProgress(worklineId);
        return ResponseEntity.ok(Result.success(results));
    }

    /**
     * 单阶段触发
     */
    @PostMapping("/{worklineId}/phase/{phase}")
    @RequiresRole(1)
    public ResponseEntity<Result<WorklinePhaseResult>> executePhase(
            @PathVariable String worklineId,
            @PathVariable String phase) {
        try {
            WorklinePhase worklinePhase = WorklinePhase.valueOf(phase.toUpperCase());
            WorklinePhaseResult result = worklineService.executePhase(worklineId, worklinePhase);
            return ResponseEntity.ok(Result.success(result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(Result.badRequest(e.getMessage()));
        }
    }

    /**
     * 执行到指定阶段
     */
    @PostMapping("/{worklineId}/executeUntil/{phase}")
    @RequiresRole(1)
    public ResponseEntity<Result<List<WorklinePhaseResult>>> executeUntil(
            @PathVariable String worklineId,
            @PathVariable String phase) {
        try {
            WorklinePhase worklinePhase = WorklinePhase.valueOf(phase.toUpperCase());
            List<WorklinePhaseResult> results = worklineService.executeUntil(worklineId, worklinePhase);
            return ResponseEntity.ok(Result.success(results));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(Result.badRequest(e.getMessage()));
        }
    }

    /**
     * 执行全部阶段
     */
    @PostMapping("/{worklineId}/executeAll")
    @RequiresRole(1)
    public ResponseEntity<Result<List<WorklinePhaseResult>>> executeAll(@PathVariable String worklineId) {
        try {
            List<WorklinePhaseResult> results = worklineService.executeAll(worklineId);
            return ResponseEntity.ok(Result.success(results));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(Result.badRequest(e.getMessage()));
        }
    }

    /**
     * 获取七阶段定义
     */
    @GetMapping("/stages")
    @RequiresRole(1)
    public ResponseEntity<Result<List<Map<String, Object>>>> getStages() {
        return ResponseEntity.ok(Result.success(worklineService.getStages()));
    }
}
