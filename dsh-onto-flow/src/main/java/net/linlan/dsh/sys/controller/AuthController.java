package net.linlan.dsh.sys.controller;

import net.linlan.dsh.common.Result;
import net.linlan.dsh.common.TenantContext;
import net.linlan.dsh.sys.dto.LoginRequest;
import net.linlan.dsh.sys.dto.LoginResponse;
import net.linlan.dsh.sys.dto.RegisterRequest;
import net.linlan.dsh.sys.dto.UserInfoResponse;
import net.linlan.dsh.sys.security.PublicApi;
import net.linlan.dsh.sys.security.RequiresRole;
import net.linlan.dsh.sys.service.SysUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器，提供登录/注册/用户信息接口
 * @author Linlan
 * CreatedAt 2026-10-14
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final SysUserService userService;

    public AuthController(SysUserService userService) {
        this.userService = userService;
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @PublicApi
    public ResponseEntity<Result<LoginResponse>> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = userService.login(request);
            return ResponseEntity.ok(Result.success(response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(Result.badRequest(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.ok(Result.fail(e.getMessage()));
        }
    }

    /**
     * 用户注册（默认角色为访客）
     */
    @PostMapping("/register")
    @PublicApi
    public ResponseEntity<Result<LoginResponse>> register(@RequestBody RegisterRequest request) {
        try {
            LoginResponse response = userService.register(request);
            return ResponseEntity.ok(Result.success(response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(Result.badRequest(e.getMessage()));
        }
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/userinfo")
    @RequiresRole(2)
    public ResponseEntity<Result<UserInfoResponse>> userInfo() {
        try {
            UserInfoResponse info = userService.getCurrentUserInfo();
            return ResponseEntity.ok(Result.success(info));
        } catch (IllegalStateException e) {
            return ResponseEntity.ok(Result.unauthorized(e.getMessage()));
        }
    }

    /**
     * 修改当前用户密码
     */
    @PutMapping("/password")
    @RequiresRole(2)
    public ResponseEntity<Result<Integer>> changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        try {
            Long userId = TenantContext.getUserId();
            if (userId == null) {
                return ResponseEntity.ok(Result.unauthorized("未登录"));
            }
            int rows = userService.changePassword(userId, oldPassword, newPassword, String.valueOf(userId));
            return ResponseEntity.ok(Result.success("修改成功", rows));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(Result.badRequest(e.getMessage()));
        }
    }
}
