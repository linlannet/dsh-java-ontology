package net.linlan.dsh.sys.repository;

import net.linlan.dsh.sys.entity.SysRole;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

/**
 * 角色数据访问层
 * @author Linlan
 * CreatedAt 2026-10-18
 * @version 1.0
 * @since 0.2.1
 */
@Repository
public class SysRoleRepository {

    private final JdbcTemplate jdbcTemplate;

    public SysRoleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<SysRole> ROLE_ROW_MAPPER = (rs, rowNum) -> {
        SysRole role = new SysRole();
        role.setRoleId(rs.getLong("role_id"));
        role.setRoleCode(rs.getString("role_code"));
        role.setRoleName(rs.getString("role_name"));
        role.setRoleLevel(rs.getInt("role_level"));
        role.setDescription(rs.getString("description"));
        role.setStatus(rs.getString("status"));
        role.setDelFlag(rs.getString("del_flag"));
        role.setCreatedBy(rs.getString("created_by"));
        role.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        role.setUpdatedBy(rs.getString("updated_by"));
        role.setUpdatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
        role.setRemark(rs.getString("remark"));
        return role;
    };

    public List<SysRole> findAll() {
        String sql = "SELECT * FROM onto_sys_role WHERE del_flag = '0' ORDER BY role_level";
        return jdbcTemplate.query(sql, ROLE_ROW_MAPPER);
    }

    public Optional<SysRole> findById(Long roleId) {
        String sql = "SELECT * FROM onto_sys_role WHERE role_id = ? AND del_flag = '0'";
        List<SysRole> list = jdbcTemplate.query(sql, ROLE_ROW_MAPPER, roleId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Optional<SysRole> findByCode(String roleCode) {
        String sql = "SELECT * FROM onto_sys_role WHERE role_code = ? AND del_flag = '0'";
        List<SysRole> list = jdbcTemplate.query(sql, ROLE_ROW_MAPPER, roleCode);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public long insert(SysRole role) {
        String sql = "INSERT INTO onto_sys_role (role_code, role_name, role_level, description, status, del_flag, created_by, remark) " +
                "VALUES (?, ?, ?, ?, ?, '0', ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"role_id"});
            ps.setString(1, role.getRoleCode());
            ps.setString(2, role.getRoleName());
            ps.setInt(3, role.getRoleLevel());
            ps.setString(4, role.getDescription());
            ps.setString(5, role.getStatus() != null ? role.getStatus() : "0");
            ps.setString(6, role.getCreatedBy());
            ps.setString(7, role.getRemark());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public int update(SysRole role) {
        String sql = "UPDATE onto_sys_role SET role_name = ?, role_level = ?, description = ?, status = ?, updated_by = ?, updated_at = NOW(), remark = ? " +
                "WHERE role_id = ? AND del_flag = '0'";
        return jdbcTemplate.update(sql,
                role.getRoleName(), role.getRoleLevel(), role.getDescription(),
                role.getStatus(), role.getUpdatedBy(), role.getRemark(), role.getRoleId());
    }

    public int softDelete(Long roleId) {
        String sql = "UPDATE onto_sys_role SET del_flag = '1', updated_at = NOW() WHERE role_id = ?";
        return jdbcTemplate.update(sql, roleId);
    }

    public List<SysRole> findRolesByUserId(Long userId) {
        String sql = "SELECT r.* FROM onto_sys_role r INNER JOIN onto_sys_user_role ur ON r.role_id = ur.role_id " +
                "WHERE ur.user_id = ? AND r.del_flag = '0' ORDER BY r.role_level";
        return jdbcTemplate.query(sql, ROLE_ROW_MAPPER, userId);
    }

    public int assignRoleToUser(Long userId, Long roleId, String createdBy) {
        String sql = "INSERT INTO onto_sys_user_role (user_id, role_id, created_by) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE created_by = ?";
        return jdbcTemplate.update(sql, userId, roleId, createdBy, createdBy);
    }

    public int revokeRoleFromUser(Long userId, Long roleId) {
        String sql = "DELETE FROM onto_sys_user_role WHERE user_id = ? AND role_id = ?";
        return jdbcTemplate.update(sql, userId, roleId);
    }
}
