package net.linlan.dsh.sys.controller;

import net.linlan.dsh.common.Result;
import net.linlan.dsh.common.TenantContext;
import net.linlan.dsh.sys.entity.CommonFile;
import net.linlan.dsh.sys.security.AuditLog;
import net.linlan.dsh.sys.security.RequiresRole;
import net.linlan.dsh.sys.service.CommonFileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 统一文件管理控制器（P1-9）
 * @author Linlan
 * CreatedAt 2026-10-18
 * @version 1.0
 * @since 0.2.1
 */
@RestController
@RequestMapping("/api/file")
public class CommonFileController {

    private final CommonFileService fileService;

    public CommonFileController(CommonFileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/{id}")
    @RequiresRole(2)
    public ResponseEntity<Result<CommonFile>> getById(@PathVariable Long id) {
        return fileService.findById(id)
                .map(f -> ResponseEntity.ok(Result.success(f)))
                .orElseGet(() -> ResponseEntity.ok(Result.notFound("文件不存在: " + id)));
    }

    @GetMapping("/page")
    @RequiresRole(1)
    public ResponseEntity<Result<List<CommonFile>>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        Long tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(Result.success(fileService.findByTenantId(tenantId, pageNum, pageSize)));
    }

    @PostMapping("/{id}/ref/increment")
    @RequiresRole(1)
    @AuditLog(action = "REF_INCREMENT", targetType = "common_file")
    public ResponseEntity<Result<Integer>> incrementRef(@PathVariable Long id) {
        return ResponseEntity.ok(Result.success(fileService.incrementRefCount(id)));
    }

    @PostMapping("/{id}/ref/decrement")
    @RequiresRole(1)
    @AuditLog(action = "REF_DECREMENT", targetType = "common_file")
    public ResponseEntity<Result<Integer>> decrementRef(@PathVariable Long id) {
        return ResponseEntity.ok(Result.success(fileService.decrementRefCount(id)));
    }

    @DeleteMapping("/{id}")
    @RequiresRole(1)
    @AuditLog(action = "DELETE", targetType = "common_file")
    public ResponseEntity<Result<Integer>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(Result.success("删除" + fileService.delete(id) + "条记录", fileService.delete(id)));
    }
}
