package net.linlan.dsh.sys.service;

import net.linlan.dsh.sys.entity.SysRole;
import net.linlan.dsh.sys.repository.SysRoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 角色管理服务
 * @author Linlan
 * CreatedAt 2026-10-18
 * @version 1.0
 * @since 0.2.1
 */
@Service
public class SysRoleService {

    private final SysRoleRepository roleRepository;

    public SysRoleService(SysRoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<SysRole> findAll() {
        return roleRepository.findAll();
    }

    public Optional<SysRole> findById(Long roleId) {
        return roleRepository.findById(roleId);
    }

    public long createRole(SysRole role) {
        if (roleRepository.findByCode(role.getRoleCode()).isPresent()) {
            throw new IllegalArgumentException("角色代码已存在: " + role.getRoleCode());
        }
        return roleRepository.insert(role);
    }

    public int updateRole(SysRole role) {
        return roleRepository.update(role);
    }

    public int deleteRole(Long roleId) {
        return roleRepository.softDelete(roleId);
    }

    public List<SysRole> findRolesByUserId(Long userId) {
        return roleRepository.findRolesByUserId(userId);
    }

    public int assignRole(Long userId, Long roleId, String createdBy) {
        if (roleRepository.findById(roleId).isEmpty()) {
            throw new IllegalArgumentException("角色不存在: " + roleId);
        }
        return roleRepository.assignRoleToUser(userId, roleId, createdBy);
    }

    public int revokeRole(Long userId, Long roleId) {
        return roleRepository.revokeRoleFromUser(userId, roleId);
    }
}
