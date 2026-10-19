package net.linlan.dsh.sys.controller;

import net.linlan.dsh.common.Result;
import net.linlan.dsh.sys.dto.UserInfoResponse;
import net.linlan.dsh.sys.security.AuditLog;
import net.linlan.dsh.sys.security.RequiresRole;
import net.linlan.dsh.sys.service.SysUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器，提供用户 CRUD 接口（仅主任可操作）
 * @author Linlan
 * CreatedAt 2026-10-14
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/auth/user")
public class SysUserController {

    private final SysUserService userService;

    public SysUserController(SysUserService userService) {
        this.userService = userService;
    }

    /**
     * 获取所有用户（仅主任）
     */
    @GetMapping
    @RequiresRole(0)
    public ResponseEntity<Result<List<UserInfoResponse>>> list() {
        return ResponseEntity.ok(Result.success(userService.findAll()));
    }

    /**
     * 分页获取用户（仅主任）
     */
    @GetMapping("/page")
    @RequiresRole(0)
    public ResponseEntity<Result<List<UserInfoResponse>>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(Result.success(userService.findPage(pageNum, pageSize)));
    }

    /**
     * 统计用户总数（仅主任）
     */
    @GetMapping("/count")
    @RequiresRole(0)
    public ResponseEntity<Result<Long>> count() {
        return ResponseEntity.ok(Result.success(userService.count()));
    }

    /**
     * 获取用户详情（研究员及以上）
     */
    @GetMapping("/{id}")
    @RequiresRole(1)
    public ResponseEntity<Result<UserInfoResponse>> getById(@PathVariable Long id) {
        return userService.findById(id)
                .map(info -> ResponseEntity.ok(Result.success(info)))
                .orElseGet(() -> ResponseEntity.ok(Result.notFound("用户不存在: " + id)));
    }

    /**
     * 更新用户信息（仅主任）
     */
    @PutMapping("/{id}")
    @RequiresRole(0)
    @AuditLog(action = "UPDATE", targetType = "sys_user")
    public ResponseEntity<Result<Integer>> update(
            @PathVariable Long id,
            @RequestBody UserInfoResponse info) {
        int rows = userService.updateUser(id, info, String.valueOf(net.linlan.dsh.common.TenantContext.getUserId()));
        return ResponseEntity.ok(Result.success("更新" + rows + "条记录", rows));
    }

    /**
     * 删除用户（仅主任）
     */
    @DeleteMapping("/{id}")
    @RequiresRole(0)
    @AuditLog(action = "DELETE", targetType = "sys_user")
    public ResponseEntity<Result<Integer>> delete(@PathVariable Long id) {
        int rows = userService.softDelete(id);
        return ResponseEntity.ok(Result.success("删除" + rows + "条记录", rows));
    }
}
