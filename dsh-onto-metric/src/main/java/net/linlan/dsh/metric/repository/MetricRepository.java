package net.linlan.dsh.metric.repository;

import net.linlan.dsh.metric.entity.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 指标层仓储类
 * @author Linlan
 * CreatedAt 2026-10-20
 * @version 1.0
 * @since 1.0
 */
@Repository
public class MetricRepository {

    private final JdbcTemplate jdbcTemplate;

    public MetricRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<MetricBusinessObject> findAllBusinessObjects(Long tenantId) {
        String sql = "SELECT * FROM onto_metric_business_object WHERE tenant_id = ? AND del_flag = 0 ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, new BusinessObjectRowMapper(), tenantId);
    }

    public MetricBusinessObject findBusinessObjectById(Long id, Long tenantId) {
        String sql = "SELECT * FROM onto_metric_business_object WHERE id = ? AND tenant_id = ? AND del_flag = 0";
        return jdbcTemplate.queryForObject(sql, new BusinessObjectRowMapper(), id, tenantId);
    }

    public long insertBusinessObject(MetricBusinessObject o) {
        String sql = "INSERT INTO onto_metric_business_object (object_name, object_code, object_type, source_table, description, status, tenant_id, created_by) VALUES (?,?,?,?,?,?,?,?)";
        return jdbcTemplate.queryForObject(sql + " RETURNING id", Long.class,
                o.getObjectName(), o.getObjectCode(), o.getObjectType(), o.getSourceTable(), o.getDescription(), o.getStatus(), o.getTenantId(), o.getCreatedBy());
    }

    public List<MetricProcess> findAllProcesses(Long tenantId) {
        String sql = "SELECT * FROM onto_metric_process WHERE tenant_id = ? AND del_flag = 0 ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, new ProcessRowMapper(), tenantId);
    }

    public List<MetricProcess> findProcessesByBusinessObjectId(Long businessObjectId) {
        String sql = "SELECT * FROM onto_metric_process WHERE business_object_id = ? AND del_flag = 0 ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, new ProcessRowMapper(), businessObjectId);
    }

    public long insertProcess(MetricProcess p) {
        String sql = "INSERT INTO onto_metric_process (process_name, process_code, business_object_id, node_config, description, status, tenant_id, created_by) VALUES (?,?,?,?,?,?,?,?)";
        return jdbcTemplate.queryForObject(sql + " RETURNING id", Long.class,
                p.getProcessName(), p.getProcessCode(), p.getBusinessObjectId(), p.getNodeConfig(), p.getDescription(), p.getStatus(), p.getTenantId(), p.getCreatedBy());
    }

    public List<MetricDecisionPoint> findAllDecisionPoints(Long tenantId) {
        String sql = "SELECT * FROM onto_metric_decision_point WHERE tenant_id = ? AND del_flag = 0 ORDER BY node_index ASC";
        return jdbcTemplate.query(sql, new DecisionPointRowMapper(), tenantId);
    }

    public List<MetricDecisionPoint> findDecisionPointsByProcessId(Long processId) {
        String sql = "SELECT * FROM onto_metric_decision_point WHERE process_id = ? AND del_flag = 0 ORDER BY node_index ASC";
        return jdbcTemplate.query(sql, new DecisionPointRowMapper(), processId);
    }

    public long insertDecisionPoint(MetricDecisionPoint d) {
        String sql = "INSERT INTO onto_metric_decision_point (point_name, process_id, node_index, decision_type, description, status, tenant_id, created_by) VALUES (?,?,?,?,?,?,?,?)";
        return jdbcTemplate.queryForObject(sql + " RETURNING id", Long.class,
                d.getPointName(), d.getProcessId(), d.getNodeIndex(), d.getDecisionType(), d.getDescription(), d.getStatus(), d.getTenantId(), d.getCreatedBy());
    }

    public List<MetricDefinition> findAllDefinitions(Long tenantId) {
        String sql = "SELECT * FROM onto_metric_definition WHERE tenant_id = ? AND del_flag = 0 ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, new DefinitionRowMapper(), tenantId);
    }

    public List<MetricDefinition> findDefinitionsByCategory(String category, Long tenantId) {
        String sql = "SELECT * FROM onto_metric_definition WHERE category = ? AND tenant_id = ? AND del_flag = 0 ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, new DefinitionRowMapper(), category, tenantId);
    }

    public MetricDefinition findDefinitionById(Long id, Long tenantId) {
        String sql = "SELECT * FROM onto_metric_definition WHERE id = ? AND tenant_id = ? AND del_flag = 0";
        return jdbcTemplate.queryForObject(sql, new DefinitionRowMapper(), id, tenantId);
    }

    public long insertDefinition(MetricDefinition m) {
        String sql = "INSERT INTO onto_metric_definition (metric_name, metric_code, category, business_object_id, process_id, decision_point_id, formula, unit, description, status, tenant_id, created_by) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        return jdbcTemplate.queryForObject(sql + " RETURNING id", Long.class,
                m.getMetricName(), m.getMetricCode(), m.getCategory(), m.getBusinessObjectId(), m.getProcessId(), m.getDecisionPointId(), m.getFormula(), m.getUnit(), m.getDescription(), m.getStatus(), m.getTenantId(), m.getCreatedBy());
    }

    public List<MetricDependency> findAllDependencies(Long tenantId) {
        String sql = "SELECT * FROM onto_metric_dependency WHERE tenant_id = ? AND del_flag = 0";
        return jdbcTemplate.query(sql, new DependencyRowMapper(), tenantId);
    }

    public List<MetricDependency> findDependenciesByDownstreamMetricId(Long metricId) {
        String sql = "SELECT * FROM onto_metric_dependency WHERE downstream_metric_id = ? AND del_flag = 0";
        return jdbcTemplate.query(sql, new DependencyRowMapper(), metricId);
    }

    public long insertDependency(MetricDependency d) {
        String sql = "INSERT INTO onto_metric_dependency (downstream_metric_id, upstream_metric_id, dependency_type, weight, tenant_id, created_by) VALUES (?,?,?,?,?,?)";
        return jdbcTemplate.queryForObject(sql + " RETURNING id", Long.class,
                d.getDownstreamMetricId(), d.getUpstreamMetricId(), d.getDependencyType(), d.getWeight(), d.getTenantId(), d.getCreatedBy());
    }

    static LocalDateTime toLdt(Timestamp ts) { return ts != null ? ts.toLocalDateTime() : null; }
    static Timestamp toTs(LocalDateTime ldt) { return ldt != null ? Timestamp.valueOf(ldt) : null; }

    static class BusinessObjectRowMapper implements RowMapper<MetricBusinessObject> {
        @Override
        public MetricBusinessObject mapRow(ResultSet rs, int rowNum) throws SQLException {
            MetricBusinessObject e = new MetricBusinessObject();
            e.setId(rs.getLong("id"));
            e.setObjectName(rs.getString("object_name"));
            e.setObjectCode(rs.getString("object_code"));
            e.setObjectType(rs.getString("object_type"));
            e.setSourceTable(rs.getString("source_table"));
            e.setDescription(rs.getString("description"));
            e.setStatus(rs.getInt("status"));
            e.setTenantId(rs.getObject("tenant_id", Long.class));
            e.setCreatedBy(rs.getString("created_by"));
            e.setCreatedAt(toLdt(rs.getTimestamp("created_at")));
            e.setUpdatedBy(rs.getString("updated_by"));
            e.setUpdatedAt(toLdt(rs.getTimestamp("updated_at")));
            e.setDelFlag(rs.getInt("del_flag"));
            e.setRemark(rs.getString("remark"));
            return e;
        }
    }

    static class ProcessRowMapper implements RowMapper<MetricProcess> {
        @Override
        public MetricProcess mapRow(ResultSet rs, int rowNum) throws SQLException {
            MetricProcess e = new MetricProcess();
            e.setId(rs.getLong("id"));
            e.setProcessName(rs.getString("process_name"));
            e.setProcessCode(rs.getString("process_code"));
            e.setBusinessObjectId(rs.getObject("business_object_id", Long.class));
            e.setNodeConfig(rs.getString("node_config"));
            e.setDescription(rs.getString("description"));
            e.setStatus(rs.getInt("status"));
            e.setTenantId(rs.getObject("tenant_id", Long.class));
            e.setCreatedBy(rs.getString("created_by"));
            e.setCreatedAt(toLdt(rs.getTimestamp("created_at")));
            e.setUpdatedBy(rs.getString("updated_by"));
            e.setUpdatedAt(toLdt(rs.getTimestamp("updated_at")));
            e.setDelFlag(rs.getInt("del_flag"));
            e.setRemark(rs.getString("remark"));
            return e;
        }
    }

    static class DecisionPointRowMapper implements RowMapper<MetricDecisionPoint> {
        @Override
        public MetricDecisionPoint mapRow(ResultSet rs, int rowNum) throws SQLException {
            MetricDecisionPoint e = new MetricDecisionPoint();
            e.setId(rs.getLong("id"));
            e.setPointName(rs.getString("point_name"));
            e.setProcessId(rs.getObject("process_id", Long.class));
            e.setNodeIndex(rs.getInt("node_index"));
            e.setDecisionType(rs.getString("decision_type"));
            e.setDescription(rs.getString("description"));
            e.setStatus(rs.getInt("status"));
            e.setTenantId(rs.getObject("tenant_id", Long.class));
            e.setCreatedBy(rs.getString("created_by"));
            e.setCreatedAt(toLdt(rs.getTimestamp("created_at")));
            e.setDelFlag(rs.getInt("del_flag"));
            e.setRemark(rs.getString("remark"));
            return e;
        }
    }

    static class DefinitionRowMapper implements RowMapper<MetricDefinition> {
        @Override
        public MetricDefinition mapRow(ResultSet rs, int rowNum) throws SQLException {
            MetricDefinition e = new MetricDefinition();
            e.setId(rs.getLong("id"));
            e.setMetricName(rs.getString("metric_name"));
            e.setMetricCode(rs.getString("metric_code"));
            e.setCategory(rs.getString("category"));
            e.setBusinessObjectId(rs.getObject("business_object_id", Long.class));
            e.setProcessId(rs.getObject("process_id", Long.class));
            e.setDecisionPointId(rs.getObject("decision_point_id", Long.class));
            e.setFormula(rs.getString("formula"));
            e.setUnit(rs.getString("unit"));
            e.setDescription(rs.getString("description"));
            e.setStatus(rs.getInt("status"));
            e.setTenantId(rs.getObject("tenant_id", Long.class));
            e.setCreatedBy(rs.getString("created_by"));
            e.setCreatedAt(toLdt(rs.getTimestamp("created_at")));
            e.setUpdatedBy(rs.getString("updated_by"));
            e.setUpdatedAt(toLdt(rs.getTimestamp("updated_at")));
            e.setDelFlag(rs.getInt("del_flag"));
            e.setRemark(rs.getString("remark"));
            return e;
        }
    }

    static class DependencyRowMapper implements RowMapper<MetricDependency> {
        @Override
        public MetricDependency mapRow(ResultSet rs, int rowNum) throws SQLException {
            MetricDependency e = new MetricDependency();
            e.setId(rs.getLong("id"));
            e.setDownstreamMetricId(rs.getObject("downstream_metric_id", Long.class));
            e.setUpstreamMetricId(rs.getObject("upstream_metric_id", Long.class));
            e.setDependencyType(rs.getString("dependency_type"));
            e.setWeight(rs.getBigDecimal("weight"));
            e.setTenantId(rs.getObject("tenant_id", Long.class));
            e.setCreatedBy(rs.getString("created_by"));
            e.setCreatedAt(toLdt(rs.getTimestamp("created_at")));
            e.setDelFlag(rs.getInt("del_flag"));
            e.setRemark(rs.getString("remark"));
            return e;
        }
    }
}
