package net.linlan.dsh.sys.controller;

import net.linlan.dsh.common.Result;
import net.linlan.dsh.common.TenantContext;
import net.linlan.dsh.sys.entity.SysRole;
import net.linlan.dsh.sys.security.AuditLog;
import net.linlan.dsh.sys.security.RequiresRole;
import net.linlan.dsh.sys.service.SysRoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器（仅主任可操作）
 * @author Linlan
 * CreatedAt 2026-10-18
 * @version 1.0
 * @since 0.2.1
 */
@RestController
@RequestMapping("/api/auth/role")
public class SysRoleController {

    private final SysRoleService roleService;

    public SysRoleController(SysRoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @RequiresRole(0)
    public ResponseEntity<Result<List<SysRole>>> list() {
        return ResponseEntity.ok(Result.success(roleService.findAll()));
    }

    @GetMapping("/{id}")
    @RequiresRole(0)
    public ResponseEntity<Result<SysRole>> getById(@PathVariable Long id) {
        return roleService.findById(id)
                .map(role -> ResponseEntity.ok(Result.success(role)))
                .orElseGet(() -> ResponseEntity.ok(Result.notFound("角色不存在: " + id)));
    }

    @PostMapping
    @RequiresRole(0)
    @AuditLog(action = "CREATE", targetType = "sys_role")
    public ResponseEntity<Result<Long>> create(@RequestBody SysRole role) {
        role.setCreatedBy(String.valueOf(TenantContext.getUserId()));
        long id = roleService.createRole(role);
        return ResponseEntity.ok(Result.success("创建角色成功", id));
    }

    @PutMapping("/{id}")
    @RequiresRole(0)
    @AuditLog(action = "UPDATE", targetType = "sys_role")
    public ResponseEntity<Result<Integer>> update(@PathVariable Long id, @RequestBody SysRole role) {
        role.setRoleId(id);
        role.setUpdatedBy(String.valueOf(TenantContext.getUserId()));
        int rows = roleService.updateRole(role);
        return ResponseEntity.ok(Result.success("更新" + rows + "条记录", rows));
    }

    @DeleteMapping("/{id}")
    @RequiresRole(0)
    @AuditLog(action = "DELETE", targetType = "sys_role")
    public ResponseEntity<Result<Integer>> delete(@PathVariable Long id) {
        int rows = roleService.deleteRole(id);
        return ResponseEntity.ok(Result.success("删除" + rows + "条记录", rows));
    }

    @GetMapping("/user/{userId}")
    @RequiresRole(0)
    public ResponseEntity<Result<List<SysRole>>> userRoles(@PathVariable Long userId) {
        return ResponseEntity.ok(Result.success(roleService.findRolesByUserId(userId)));
    }

    @PostMapping("/user/{userId}/assign/{roleId}")
    @RequiresRole(0)
    @AuditLog(action = "ASSIGN_ROLE", targetType = "sys_user_role")
    public ResponseEntity<Result<Integer>> assignRole(@PathVariable Long userId, @PathVariable Long roleId) {
        int rows = roleService.assignRole(userId, roleId, String.valueOf(TenantContext.getUserId()));
        return ResponseEntity.ok(Result.success("分配角色成功", rows));
    }

    @DeleteMapping("/user/{userId}/revoke/{roleId}")
    @RequiresRole(0)
    @AuditLog(action = "REVOKE_ROLE", targetType = "sys_user_role")
    public ResponseEntity<Result<Integer>> revokeRole(@PathVariable Long userId, @PathVariable Long roleId) {
        int rows = roleService.revokeRole(userId, roleId);
        return ResponseEntity.ok(Result.success("撤销角色成功", rows));
    }
}
