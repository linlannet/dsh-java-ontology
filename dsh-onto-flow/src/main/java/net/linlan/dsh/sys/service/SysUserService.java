package net.linlan.dsh.sys.service;

import net.linlan.dsh.common.TenantContext;
import net.linlan.dsh.sys.dto.LoginRequest;
import net.linlan.dsh.sys.dto.LoginResponse;
import net.linlan.dsh.sys.dto.RegisterRequest;
import net.linlan.dsh.sys.dto.UserInfoResponse;
import net.linlan.dsh.sys.entity.SysUser;
import net.linlan.dsh.sys.repository.SysUserRepository;
import net.linlan.dsh.sys.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 用户认证与管理系统服务
 * @author Linlan
 * CreatedAt 2026-10-14
 * @version 1.0
 * @since 1.0
 */
@Service
public class SysUserService {

    private final SysUserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    public SysUserService(SysUserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * 用户登录
     */
    public LoginResponse login(LoginRequest request) {
        Optional<SysUser> userOpt = userRepository.findByUserName(request.getUserName());
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        SysUser user = userOpt.get();
        if ("1".equals(user.getStatus())) {
            throw new IllegalStateException("账号已停用");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        String token = jwtUtil.generateToken(user.getUserId(), user.getUserName(), user.getRoleLevel(), user.getTenantId());
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getUserId());
        response.setUserName(user.getUserName());
        response.setNickName(user.getNickName());
        response.setRoleLevel(user.getRoleLevel());
        response.setTenantId(user.getTenantId());
        return response;
    }

    /**
     * 用户注册（默认角色为访客 roleLevel=2）
     */
    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByUserName(request.getUserName())) {
            throw new IllegalArgumentException("用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUserName(request.getUserName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickName(request.getNickName());
        user.setEmail(request.getEmail());
        user.setPhonenumber(request.getPhonenumber());
        user.setTenantId(request.getTenantId() != null ? request.getTenantId() : 1L);
        user.setRoleLevel(2);
        user.setStatus("0");
        user.setCreatedBy("register");
        long userId = userRepository.insert(user);
        String token = jwtUtil.generateToken(userId, user.getUserName(), user.getRoleLevel(), user.getTenantId());
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(userId);
        response.setUserName(user.getUserName());
        response.setNickName(user.getNickName());
        response.setRoleLevel(user.getRoleLevel());
        response.setTenantId(user.getTenantId());
        return response;
    }

    /**
     * 获取当前登录用户信息
     */
    public UserInfoResponse getCurrentUserInfo() {
        Long userId = TenantContext.getUserId();
        if (userId == null) {
            throw new IllegalStateException("未登录");
        }
        Optional<SysUser> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalStateException("用户不存在");
        }
        return toUserInfo(userOpt.get());
    }

    /**
     * 获取所有用户
     */
    public List<UserInfoResponse> findAll() {
        return userRepository.findAll().stream().map(this::toUserInfo).toList();
    }

    /**
     * 分页获取用户
     */
    public List<UserInfoResponse> findPage(int pageNum, int pageSize) {
        return userRepository.findPage(pageNum, pageSize).stream().map(this::toUserInfo).toList();
    }

    /**
     * 统计用户总数
     */
    public long count() {
        return userRepository.count();
    }

    /**
     * 根据ID获取用户
     */
    public Optional<UserInfoResponse> findById(Long userId) {
        return userRepository.findById(userId).map(this::toUserInfo);
    }

    /**
     * 更新用户信息
     */
    public int updateUser(Long userId, UserInfoResponse info, String updatedBy) {
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setNickName(info.getNickName());
        user.setEmail(info.getEmail());
        user.setPhonenumber(info.getPhonenumber());
        user.setStatus(info.getStatus());
        user.setUpdatedBy(updatedBy);
        user.setRemark(info.getRemark());
        return userRepository.update(user);
    }

    /**
     * 修改密码
     */
    public int changePassword(Long userId, String oldPassword, String newPassword, String updatedBy) {
        Optional<SysUser> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, userOpt.get().getPassword())) {
            throw new IllegalArgumentException("原密码错误");
        }
        return userRepository.updatePassword(userId, passwordEncoder.encode(newPassword), updatedBy);
    }

    /**
     * 软删除用户
     */
    public int softDelete(Long userId) {
        return userRepository.softDelete(userId);
    }

    /**
     * 更新登录信息
     */
    public int updateLoginInfo(Long userId, String loginIp) {
        return userRepository.updateLoginInfo(userId, loginIp);
    }

    private UserInfoResponse toUserInfo(SysUser user) {
        UserInfoResponse info = new UserInfoResponse();
        info.setUserId(user.getUserId());
        info.setTenantId(user.getTenantId());
        info.setRoleLevel(user.getRoleLevel());
        info.setUserName(user.getUserName());
        info.setNickName(user.getNickName());
        info.setEmail(user.getEmail());
        info.setPhonenumber(user.getPhonenumber());
        info.setStatus(user.getStatus());
        info.setLoginIp(user.getLoginIp());
        info.setLoginDate(user.getLoginDate());
        info.setCreatedAt(user.getCreatedAt());
        info.setRemark(user.getRemark());
        return info;
    }
}
