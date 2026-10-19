package net.linlan.dsh.workspace.repository;

import net.linlan.dsh.common.PageResult;
import net.linlan.dsh.common.TenantContext;
import net.linlan.dsh.common.entity.CommonDiscoveredEntity;
import net.linlan.dsh.common.entity.CommonTenantRelationAsset;
import net.linlan.dsh.common.entity.CommonPersonalVectorIndex;
import net.linlan.dsh.common.entity.CommonLlmChatHistory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 知识沉淀域统一CRUD数据访问类
 * <p>支持4类个人层实体的通用增删改查操作，所有查询均按租户隔离过滤。</p>
 * @author Linlan
 * CreatedAt 2026-10-13
 * @version 1.0
 * @since 1.0
 */
@Repository
public class WorkspaceRepository {

    private final JdbcTemplate jdbcTemplate;

    public WorkspaceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** 分类名到表名的映射，共4类知识沉淀域实体 */
    public static final Map<String, String> TABLE_TO_ENTITY = Map.ofEntries(
            Map.entry("discoveredEntity", "onto_common_discovered_entity"),
            Map.entry("tenantRelationAsset", "onto_common_tenant_relation_asset"),
            Map.entry("personalVectorIndex", "onto_common_personal_vector_index"),
            Map.entry("llmChatHistory", "onto_common_llm_chat_history")
    );

    /** 分类名到Entity类的映射，用于类型化查询 */
    public static final Map<String, Class<?>> ENTITY_CLASS_MAP = Map.ofEntries(
            Map.entry("discoveredEntity", CommonDiscoveredEntity.class),
            Map.entry("tenantRelationAsset", CommonTenantRelationAsset.class),
            Map.entry("personalVectorIndex", CommonPersonalVectorIndex.class),
            Map.entry("llmChatHistory", CommonLlmChatHistory.class)
    );

    /** 各表搜索列映射，用于关键词搜索 */
    private static final Map<String, List<String>> SEARCH_COLUMNS = Map.ofEntries(
            Map.entry("onto_common_discovered_entity", List.of("entity_name")),
            Map.entry("onto_common_tenant_relation_asset", List.of("relation_type")),
            Map.entry("onto_common_personal_vector_index", List.of()),
            Map.entry("onto_common_llm_chat_history", List.of("agent_name", "content"))
    );

    /** Get table name by category. 根据分类名获取对应表名.
     * @param category 知识沉淀分类名称|discoveredEntity
     * @return 数据库表名
     */
    public String getTableName(String category) {
        String table = TABLE_TO_ENTITY.get(category);
        if (table == null) {
            throw new IllegalArgumentException("Unknown workspace category: " + category);
        }
        return table;
    }

    /** Get entity class by category. 根据分类名获取对应Entity类.
     * @param category 知识沉淀分类名称|discoveredEntity
     * @return Entity类对象
     */
    public Class<?> getEntityClass(String category) {
        Class<?> clazz = ENTITY_CLASS_MAP.get(category);
        if (clazz == null) {
            throw new IllegalArgumentException("Unknown workspace category: " + category);
        }
        return clazz;
    }

    /** Find all typed by category. 根据分类获取当前租户所有未删除的实体（类型化）.
     * @param category 知识沉淀分类名称|discoveredEntity
     * @return 类型化实体列表，按id倒序
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> findAllTyped(String category) {
        String table = getTableName(category);
        Class<T> entityClass = (Class<T>) getEntityClass(category);
        return jdbcTemplate.query(
                "SELECT * FROM " + table + " WHERE del_flag = 0 AND tenant_id = ? ORDER BY id DESC",
                new BeanPropertyRowMapper<>(entityClass), TenantContext.getTenantId());
    }

    /** Find all by category. 根据分类获取当前租户所有未删除的实体.
     * @param category 知识沉淀分类名称|discoveredEntity
     * @return 实体列表，按id倒序
     */
    public List<Map<String, Object>> findAll(String category) {
        String table = getTableName(category);
        return jdbcTemplate.queryForList(
                "SELECT * FROM " + table + " WHERE del_flag = 0 AND tenant_id = ? ORDER BY id DESC",
                TenantContext.getTenantId());
    }

    /** Find page by category. 根据分类分页获取当前租户实体列表.
     * @param category 知识沉淀分类名称|discoveredEntity
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    public PageResult<Map<String, Object>> findPage(String category, int pageNum, int pageSize) {
        String table = getTableName(category);
        int offset = (pageNum - 1) * pageSize;
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + table + " WHERE del_flag = 0 AND tenant_id = ?",
                Long.class, TenantContext.getTenantId());
        if (total == null || total == 0) {
            return PageResult.empty(pageNum, pageSize);
        }
        List<Map<String, Object>> records = jdbcTemplate.queryForList(
                "SELECT * FROM " + table + " WHERE del_flag = 0 AND tenant_id = ? ORDER BY id DESC LIMIT ? OFFSET ?",
                TenantContext.getTenantId(), pageSize, offset);
        return new PageResult<>(records, total, pageNum, pageSize);
    }

    /** Find by id. 根据分类和主键获取当前租户实体.
     * @param category 知识沉淀分类名称|discoveredEntity
     * @param id 主键ID
     * @return 实体数据；不存在时返回empty
     */
    public Optional<Map<String, Object>> findById(String category, Long id) {
        String table = getTableName(category);
        List<Map<String, Object>> list = jdbcTemplate.queryForList(
                "SELECT * FROM " + table + " WHERE id = ? AND del_flag = 0 AND tenant_id = ?",
                id, TenantContext.getTenantId());
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    /** Insert entity. 新增当前租户实体记录.
     * @param category 知识沉淀分类名称|discoveredEntity
     * @param data 实体属性键值对
     * @return 新增记录的主键ID
     */
    public Long insert(String category, Map<String, Object> data) {
        String table = getTableName(category);
        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName(table)
                .usingGeneratedKeyColumns("id");
        Map<String, Object> mutableData = new HashMap<>(data);
        mutableData.put("tenant_id", TenantContext.getTenantId());
        mutableData.put("del_flag", 0);
        mutableData.put("created_at", java.sql.Timestamp.from(java.time.Instant.now()));
        mutableData.put("updated_at", java.sql.Timestamp.from(java.time.Instant.now()));
        if (TenantContext.getUserId() != null) {
            mutableData.put("created_by", String.valueOf(TenantContext.getUserId()));
        }
        return insert.executeAndReturnKey(mutableData).longValue();
    }

    /** Update entity. 更新当前租户实体记录.
     * @param category 知识沉淀分类名称|discoveredEntity
     * @param id 主键ID
     * @param data 需要更新的属性键值对
     * @return 受影响的行数
     */
    public int update(String category, Long id, Map<String, Object> data) {
        String table = getTableName(category);
        StringBuilder setClause = new StringBuilder();
        List<Object> params = new ArrayList<>();
        int i = 0;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (i > 0) setClause.append(", ");
            setClause.append(entry.getKey()).append(" = ?");
            params.add(entry.getValue());
            i++;
        }
        setClause.append(", updated_at = NOW()");
        params.add(id);
        params.add(TenantContext.getTenantId());
        String sql = "UPDATE " + table + " SET " + setClause + " WHERE id = ? AND del_flag = 0 AND tenant_id = ?";
        return jdbcTemplate.update(sql, params.toArray());
    }

    /** Soft delete entity. 软删除当前租户实体记录.
     * @param category 知识沉淀分类名称|discoveredEntity
     * @param id 主键ID
     * @return 受影响的行数
     */
    public int softDelete(String category, Long id) {
        String table = getTableName(category);
        return jdbcTemplate.update(
                "UPDATE " + table + " SET del_flag = 1, updated_at = NOW() WHERE id = ? AND del_flag = 0 AND tenant_id = ?",
                id, TenantContext.getTenantId());
    }

    /** Count by category. 统计当前租户未删除实体数量.
     * @param category 知识沉淀分类名称|discoveredEntity
     * @return 实体总数
     */
    public long count(String category) {
        String table = getTableName(category);
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + table + " WHERE del_flag = 0 AND tenant_id = ?",
                Long.class, TenantContext.getTenantId());
        return count != null ? count : 0;
    }

    /** Search by keyword. 关键词搜索当前租户知识沉淀实体.
     * <p>指定分类时仅搜索该表；未指定时遍历所有表搜索。无搜索列的表将被跳过。</p>
     * @param keyword 搜索关键词
     * @param category 限定分类，为空则搜索全部
     * @param limit 返回条数限制
     * @return 匹配的实体列表
     */
    public List<Map<String, Object>> search(String keyword, String category, int limit) {
        String like = "%" + keyword + "%";
        if (category != null) {
            String table = getTableName(category);
            List<String> columns = SEARCH_COLUMNS.getOrDefault(table, List.of());
            if (columns.isEmpty()) {
                return List.of();
            }
            String orClause = columns.stream().map(c -> c + " LIKE ?").collect(Collectors.joining(" OR "));
            List<Object> params = new ArrayList<>();
            params.add(TenantContext.getTenantId());
            for (int i = 0; i < columns.size(); i++) params.add(like);
            params.add(limit);
            return jdbcTemplate.queryForList(
                    "SELECT * FROM " + table + " WHERE del_flag = 0 AND tenant_id = ? AND (" + orClause + ") LIMIT ?",
                    params.toArray());
        }
        List<Map<String, Object>> results = new ArrayList<>();
        for (String cat : TABLE_TO_ENTITY.keySet()) {
            String table = TABLE_TO_ENTITY.get(cat);
            List<String> columns = SEARCH_COLUMNS.getOrDefault(table, List.of());
            if (columns.isEmpty()) {
                continue;
            }
            String orClause = columns.stream().map(c -> c + " LIKE ?").collect(Collectors.joining(" OR "));
            List<Object> params = new ArrayList<>();
            params.add(TenantContext.getTenantId());
            for (int i = 0; i < columns.size(); i++) params.add(like);
            params.add(limit);
            try {
                List<Map<String, Object>> partial = jdbcTemplate.queryForList(
                        "SELECT *, '" + cat + "' AS _category FROM " + table + " WHERE del_flag = 0 AND tenant_id = ? AND (" + orClause + ") LIMIT ?",
                        params.toArray());
                results.addAll(partial);
            } catch (Exception ignored) {
            }
        }
        return results;
    }

    /** Query by filters. 根据过滤条件分页查询当前租户实体.
     * @param category 知识沉淀分类名称|discoveredEntity
     * @param filters 过滤条件键值对
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 符合条件的实体列表
     */
    public List<Map<String, Object>> queryByCategoryAndFilters(String category, Map<String, Object> filters, int pageNum, int pageSize) {
        String table = getTableName(category);
        StringBuilder where = new StringBuilder("WHERE del_flag = 0 AND tenant_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(TenantContext.getTenantId());
        for (Map.Entry<String, Object> entry : filters.entrySet()) {
            where.append(" AND ").append(entry.getKey()).append(" = ?");
            params.add(entry.getValue());
        }
        int offset = (pageNum - 1) * pageSize;
        params.add(pageSize);
        params.add(offset);
        return jdbcTemplate.queryForList("SELECT * FROM " + table + " " + where + " ORDER BY id DESC LIMIT ? OFFSET ?", params.toArray());
    }
}
