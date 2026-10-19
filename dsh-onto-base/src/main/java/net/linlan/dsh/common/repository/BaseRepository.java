package net.linlan.dsh.common.repository;

import net.linlan.dsh.common.PageResult;
import net.linlan.dsh.common.TenantContext;
import net.linlan.dsh.common.entity.BaseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class BaseRepository<T extends BaseEntity> {

    protected final JdbcTemplate jdbcTemplate;
    protected final String tableName;
    protected final RowMapper<T> rowMapper;
    protected final boolean personalLayer;

    protected BaseRepository(JdbcTemplate jdbcTemplate, String tableName, RowMapper<T> rowMapper, boolean personalLayer) {
        this.jdbcTemplate = jdbcTemplate;
        this.tableName = tableName;
        this.rowMapper = rowMapper;
        this.personalLayer = personalLayer;
    }

    public Optional<T> findById(Long id) {
        String sql = "SELECT * FROM " + tableName + " WHERE id = ? AND del_flag = 0";
        if (personalLayer) {
            Long tenantId = TenantContext.getTenantId();
            if (tenantId == null) {
                return Optional.empty();
            }
            sql += " AND tenant_id = " + tenantId;
        }
        List<T> list = jdbcTemplate.query(sql, rowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<T> findAll() {
        String sql = "SELECT * FROM " + tableName + " WHERE del_flag = 0";
        if (personalLayer) {
            Long tenantId = TenantContext.getTenantId();
            if (tenantId == null) {
                return List.of();
            }
            sql += " AND tenant_id = " + tenantId;
        }
        return jdbcTemplate.query(sql, rowMapper);
    }

    public PageResult<T> findPage(int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        String baseWhere = "WHERE del_flag = 0";
        if (personalLayer) {
            Long tenantId = TenantContext.getTenantId();
            if (tenantId == null) {
                return PageResult.empty(pageNum, pageSize);
            }
            baseWhere += " AND tenant_id = " + tenantId;
        }
        String countSql = "SELECT COUNT(*) FROM " + tableName + " " + baseWhere;
        Long total = jdbcTemplate.queryForObject(countSql, Long.class);
        if (total == null || total == 0) {
            return PageResult.empty(pageNum, pageSize);
        }
        String dataSql = "SELECT * FROM " + tableName + " " + baseWhere + " LIMIT ? OFFSET ?";
        List<T> records = jdbcTemplate.query(dataSql, rowMapper, pageSize, offset);
        return new PageResult<>(records, total, pageNum, pageSize);
    }

    public int softDelete(Long id) {
        String sql = "UPDATE " + tableName + " SET del_flag = 1, updated_at = NOW() WHERE id = ? AND del_flag = 0";
        if (personalLayer) {
            Long tenantId = TenantContext.getTenantId();
            if (tenantId == null) {
                return 0;
            }
            sql += " AND tenant_id = " + tenantId;
        }
        return jdbcTemplate.update(sql, id);
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE del_flag = 0";
        if (personalLayer) {
            Long tenantId = TenantContext.getTenantId();
            if (tenantId == null) {
                return 0;
            }
            sql += " AND tenant_id = " + tenantId;
        }
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0;
    }

    protected SimpleJdbcInsert createInsert() {
        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName(tableName)
                .usingGeneratedKeyColumns("id");
        return insert;
    }

    protected int updatedById(String setClause, Object[] params, Long id) {
        String sql = "UPDATE " + tableName + " SET " + setClause + ", updated_at = NOW() WHERE id = ? AND del_flag = 0";
        Object[] allParams = new Object[params.length + 1];
        System.arraycopy(params, 0, allParams, 0, params.length);
        allParams[params.length] = id;
        if (personalLayer) {
            Long tenantId = TenantContext.getTenantId();
            if (tenantId == null) {
                return 0;
            }
            sql += " AND tenant_id = " + tenantId;
        }
        return jdbcTemplate.update(sql, allParams);
    }

    protected List<T> queryWithTenant(String sql, Object... args) {
        if (personalLayer) {
            Long tenantId = TenantContext.getTenantId();
            if (tenantId == null) {
                return List.of();
            }
            if (!sql.toLowerCase().contains("tenant_id")) {
                sql += " AND tenant_id = " + tenantId;
            }
        }
        return jdbcTemplate.query(sql, rowMapper, args);
    }

    protected String appendTenantFilter(String baseSql) {
        if (personalLayer) {
            Long tenantId = TenantContext.getTenantId();
            if (tenantId != null) {
                String lower = baseSql.toLowerCase();
                if (lower.contains("where")) {
                    return baseSql + " AND tenant_id = " + tenantId;
                } else {
                    return baseSql + " WHERE tenant_id = " + tenantId;
                }
            }
        }
        return baseSql;
    }
}
