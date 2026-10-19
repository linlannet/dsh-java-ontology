package net.linlan.dsh.sys.repository;

import net.linlan.dsh.sys.entity.SysUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问层
 * @author Linlan
 * CreatedAt 2026-10-14
 * @version 1.0
 * @since 1.0
 */
@Repository
public class SysUserRepository {

    private final JdbcTemplate jdbcTemplate;

    public SysUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<SysUser> USER_ROW_MAPPER = (rs, rowNum) -> {
        SysUser user = new SysUser();
        user.setUserId(rs.getLong("user_id"));
        long tenantId = rs.getLong("tenant_id");
        user.setTenantId(rs.wasNull() ? null : tenantId);
        user.setRoleLevel(rs.getInt("role_level"));
        user.setUserName(rs.getString("user_name"));
        user.setNickName(rs.getString("nick_name"));
        user.setEmail(rs.getString("email"));
        user.setPhonenumber(rs.getString("phonenumber"));
        user.setPassword(rs.getString("password"));
        user.setStatus(rs.getString("status"));
        user.setDelFlag(rs.getString("del_flag"));
        user.setLoginIp(rs.getString("login_ip"));
        user.setLoginDate(rs.getTimestamp("login_date") != null ? rs.getTimestamp("login_date").toLocalDateTime() : null);
        user.setCreatedBy(rs.getString("created_by"));
        user.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        user.setUpdatedBy(rs.getString("updated_by"));
        user.setUpdatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
        user.setRemark(rs.getString("remark"));
        return user;
    };

    /**
     * 根据用户名查找用户
     */
    public Optional<SysUser> findByUserName(String userName) {
        String sql = "SELECT * FROM onto_sys_user WHERE user_name = ? AND del_flag = '0'";
        List<SysUser> list = jdbcTemplate.query(sql, USER_ROW_MAPPER, userName);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    /**
     * 根据用户ID查找用户
     */
    public Optional<SysUser> findById(Long userId) {
        String sql = "SELECT * FROM onto_sys_user WHERE user_id = ? AND del_flag = '0'";
        List<SysUser> list = jdbcTemplate.query(sql, USER_ROW_MAPPER, userId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    /**
     * 查找所有用户
     */
    public List<SysUser> findAll() {
        String sql = "SELECT * FROM onto_sys_user WHERE del_flag = '0' ORDER BY user_id";
        return jdbcTemplate.query(sql, USER_ROW_MAPPER);
    }

    /**
     * 分页查找用户
     */
    public List<SysUser> findPage(int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        String sql = "SELECT * FROM onto_sys_user WHERE del_flag = '0' ORDER BY user_id LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, USER_ROW_MAPPER, pageSize, offset);
    }

    /**
     * 统计用户总数
     */
    public long count() {
        String sql = "SELECT COUNT(*) FROM onto_sys_user WHERE del_flag = '0'";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0;
    }

    /**
     * 插入用户
     */
    public long insert(SysUser user) {
        String sql = "INSERT INTO onto_sys_user (tenant_id, role_level, user_name, nick_name, email, phonenumber, password, status, del_flag, created_by, remark) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, '0', ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"user_id"});
            if (user.getTenantId() != null) {
                ps.setLong(1, user.getTenantId());
            } else {
                ps.setNull(1, java.sql.Types.BIGINT);
            }
            ps.setInt(2, user.getRoleLevel());
            ps.setString(3, user.getUserName());
            ps.setString(4, user.getNickName());
            ps.setString(5, user.getEmail());
            ps.setString(6, user.getPhonenumber());
            ps.setString(7, user.getPassword());
            ps.setString(8, user.getStatus() != null ? user.getStatus() : "0");
            ps.setString(9, user.getCreatedBy());
            ps.setString(10, user.getRemark());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    /**
     * 更新用户信息
     */
    public int update(SysUser user) {
        String sql = "UPDATE onto_sys_user SET nick_name = ?, email = ?, phonenumber = ?, status = ?, updated_by = ?, updated_at = NOW(), remark = ? " +
                "WHERE user_id = ? AND del_flag = '0'";
        return jdbcTemplate.update(sql,
                user.getNickName(),
                user.getEmail(),
                user.getPhonenumber(),
                user.getStatus(),
                user.getUpdatedBy(),
                user.getRemark(),
                user.getUserId());
    }

    /**
     * 更新密码
     */
    public int updatePassword(Long userId, String newPassword, String updatedBy) {
        String sql = "UPDATE onto_sys_user SET password = ?, updated_by = ?, updated_at = NOW() WHERE user_id = ? AND del_flag = '0'";
        return jdbcTemplate.update(sql, newPassword, updatedBy, userId);
    }

    /**
     * 更新最后登录信息
     */
    public int updateLoginInfo(Long userId, String loginIp) {
        String sql = "UPDATE onto_sys_user SET login_ip = ?, login_date = NOW() WHERE user_id = ?";
        return jdbcTemplate.update(sql, loginIp, userId);
    }

    /**
     * 软删除用户
     */
    public int softDelete(Long userId) {
        String sql = "UPDATE onto_sys_user SET del_flag = '1', updated_at = NOW() WHERE user_id = ? AND del_flag = '0'";
        return jdbcTemplate.update(sql, userId);
    }

    /**
     * 检查用户名是否存在
     */
    public boolean existsByUserName(String userName) {
        String sql = "SELECT COUNT(*) FROM onto_sys_user WHERE user_name = ? AND del_flag = '0'";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, userName);
        return count != null && count > 0;
    }
}
