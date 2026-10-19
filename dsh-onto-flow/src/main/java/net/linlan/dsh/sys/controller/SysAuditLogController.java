package net.linlan.dsh.sys.controller;

import net.linlan.dsh.common.Result;
import net.linlan.dsh.sys.entity.SysAuditLog;
import net.linlan.dsh.sys.security.RequiresRole;
import net.linlan.dsh.sys.service.SysAuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 审计日志查询控制器（仅主任可查）
 * @author Linlan
 * CreatedAt 2026-10-18
 * @version 1.0
 * @since 0.2.1
 */
@RestController
@RequestMapping("/api/auth/audit-log")
public class SysAuditLogController {

    private final SysAuditLogService auditLogService;

    public SysAuditLogController(SysAuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping("/page")
    @RequiresRole(0)
    public ResponseEntity<Result<List<SysAuditLog>>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ResponseEntity.ok(Result.success(auditLogService.findPage(pageNum, pageSize)));
    }

    @GetMapping("/user/{userId}")
    @RequiresRole(0)
    public ResponseEntity<Result<List<SysAuditLog>>> byUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ResponseEntity.ok(Result.success(auditLogService.findByUserId(userId, pageNum, pageSize)));
    }

    @GetMapping("/action/{action}")
    @RequiresRole(0)
    public ResponseEntity<Result<List<SysAuditLog>>> byAction(
            @PathVariable String action,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ResponseEntity.ok(Result.success(auditLogService.findByAction(action, pageNum, pageSize)));
    }

    @GetMapping("/count")
    @RequiresRole(0)
    public ResponseEntity<Result<Long>> count() {
        return ResponseEntity.ok(Result.success(auditLogService.count()));
    }
}
