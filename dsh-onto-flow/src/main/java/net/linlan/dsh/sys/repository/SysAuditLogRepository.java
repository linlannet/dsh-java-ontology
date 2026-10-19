package net.linlan.dsh.sys.repository;

import net.linlan.dsh.sys.entity.SysAuditLog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

/**
 * 审计日志数据访问层
 * @author Linlan
 * CreatedAt 2026-10-18
 * @version 1.0
 * @since 0.2.1
 */
@Repository
public class SysAuditLogRepository {

    private final JdbcTemplate jdbcTemplate;

    public SysAuditLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<SysAuditLog> LOG_ROW_MAPPER = (rs, rowNum) -> {
        SysAuditLog log = new SysAuditLog();
        log.setLogId(rs.getLong("log_id"));
        long userId = rs.getLong("user_id");
        log.setUserId(rs.wasNull() ? null : userId);
        long tenantId = rs.getLong("tenant_id");
        log.setTenantId(rs.wasNull() ? null : tenantId);
        int roleLevel = rs.getInt("role_level");
        log.setRoleLevel(rs.wasNull() ? null : roleLevel);
        log.setAction(rs.getString("action"));
        log.setTargetType(rs.getString("target_type"));
        log.setTargetId(rs.getString("target_id"));
        log.setMethod(rs.getString("method"));
        log.setRequestUri(rs.getString("request_uri"));
        log.setRequestParams(rs.getString("request_params"));
        log.setResultStatus(rs.getString("result_status"));
        log.setErrorMsg(rs.getString("error_msg"));
        long execMs = rs.getLong("execution_ms");
        log.setExecutionMs(rs.wasNull() ? null : execMs);
        log.setClientIp(rs.getString("client_ip"));
        log.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        return log;
    };

    public long insert(SysAuditLog logEntry) {
        String sql = "INSERT INTO onto_sys_audit_log (user_id, tenant_id, role_level, action, target_type, target_id, " +
                "method, request_uri, request_params, result_status, error_msg, execution_ms, client_ip) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"log_id"});
            if (logEntry.getUserId() != null) {
                ps.setLong(1, logEntry.getUserId());
            } else {
                ps.setNull(1, java.sql.Types.BIGINT);
            }
            if (logEntry.getTenantId() != null) {
                ps.setLong(2, logEntry.getTenantId());
            } else {
                ps.setNull(2, java.sql.Types.BIGINT);
            }
            if (logEntry.getRoleLevel() != null) {
                ps.setInt(3, logEntry.getRoleLevel());
            } else {
                ps.setNull(3, java.sql.Types.TINYINT);
            }
            ps.setString(4, logEntry.getAction());
            ps.setString(5, logEntry.getTargetType());
            ps.setString(6, logEntry.getTargetId());
            ps.setString(7, logEntry.getMethod());
            ps.setString(8, logEntry.getRequestUri());
            ps.setString(9, logEntry.getRequestParams());
            ps.setString(10, logEntry.getResultStatus());
            ps.setString(11, logEntry.getErrorMsg());
            if (logEntry.getExecutionMs() != null) {
                ps.setLong(12, logEntry.getExecutionMs());
            } else {
                ps.setNull(12, java.sql.Types.BIGINT);
            }
            ps.setString(13, logEntry.getClientIp());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public List<SysAuditLog> findPage(int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        String sql = "SELECT * FROM onto_sys_audit_log ORDER BY log_id DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, LOG_ROW_MAPPER, pageSize, offset);
    }

    public List<SysAuditLog> findByUserId(Long userId, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        String sql = "SELECT * FROM onto_sys_audit_log WHERE user_id = ? ORDER BY log_id DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, LOG_ROW_MAPPER, userId, pageSize, offset);
    }

    public List<SysAuditLog> findByAction(String action, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        String sql = "SELECT * FROM onto_sys_audit_log WHERE action = ? ORDER BY log_id DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, LOG_ROW_MAPPER, action, pageSize, offset);
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM onto_sys_audit_log";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0;
    }
}
