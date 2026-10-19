package net.linlan.dsh.global.repository;

import net.linlan.dsh.global.entity.EntityMergeLog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

/**
 * 实体合并日志仓库
 * @author Linlan
 * CreatedAt 2026-10-18
 * @version 1.0
 * @since 1.0
 */
@Repository
public class EntityMergeLogRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<EntityMergeLog> ROW_MAPPER = (rs, rowNum) -> {
        EntityMergeLog log = new EntityMergeLog();
        log.setId(rs.getLong("id"));
        log.setSourceEntityId(rs.getLong("source_entity_id"));
        log.setTargetEntityId(rs.getLong("target_entity_id"));
        log.setEntityType(rs.getString("entity_type"));
        log.setSimilarityScore(rs.getObject("similarity_score", Float.class));
        log.setMergeStrategy(rs.getString("merge_strategy"));
        log.setOperatorId(rs.getObject("operator_id", Long.class));
        log.setTenantId(rs.getLong("tenant_id"));
        log.setCreatedBy(rs.getString("created_by"));
        log.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        log.setUpdatedBy(rs.getString("updated_by"));
        log.setUpdatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
        log.setDelFlag(rs.getInt("del_flag"));
        log.setRemark(rs.getString("remark"));
        return log;
    };

    public EntityMergeLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long save(EntityMergeLog log) {
        String sql = "INSERT INTO onto_common_merge_log " +
                "(source_entity_id, target_entity_id, entity_type, similarity_score, " +
                "merge_strategy, operator_id, tenant_id, created_by, remark) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, log.getSourceEntityId());
            ps.setLong(2, log.getTargetEntityId());
            ps.setString(3, log.getEntityType());
            ps.setObject(4, log.getSimilarityScore());
            ps.setString(5, log.getMergeStrategy());
            ps.setObject(6, log.getOperatorId());
            ps.setLong(7, log.getTenantId());
            ps.setString(8, log.getCreatedBy());
            ps.setString(9, log.getRemark());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public List<EntityMergeLog> findByTenantId(Long tenantId, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        String sql = "SELECT * FROM onto_common_merge_log WHERE tenant_id = ? AND del_flag = 0 " +
                "ORDER BY created_at DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, ROW_MAPPER, tenantId, pageSize, offset);
    }

    public List<EntityMergeLog> findByTargetEntityId(Long targetEntityId) {
        return jdbcTemplate.query(
                "SELECT * FROM onto_common_merge_log WHERE target_entity_id = ? AND del_flag = 0 ORDER BY created_at DESC",
                ROW_MAPPER, targetEntityId);
    }

    public long countByTenantId(Long tenantId) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM onto_common_merge_log WHERE tenant_id = ? AND del_flag = 0",
                Long.class, tenantId);
    }
}
