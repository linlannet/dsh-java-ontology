package net.linlan.dsh.ontology.repository;

import net.linlan.dsh.common.PageResult;
import net.linlan.dsh.common.TenantContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 本体元数据统一CRUD数据访问类
 * <p>负责底层共享层 6 类本体元数据表（term/eventType/location/entity/version/i18n）的通用增删改查。</p>
 * <p>数据源：优先使用 ontoJdbcTemplate（双库模式下指向 dsh_java_ontodb）；
 * 未配置时回退到主 JdbcTemplate（单库模式）。</p>
 * @author Linlan
 * CreatedAt 2026-10-18
 * @version 1.0
 * @since 0.2.1
 */
@Repository
public class OntologyMetaRepository {

    private final JdbcTemplate ontoJdbcTemplate;
    private final JdbcTemplate primaryJdbcTemplate;

    public OntologyMetaRepository(@Qualifier("ontoJdbcTemplate") ObjectProvider<JdbcTemplate> ontoJdbcTemplateProvider,
                                  JdbcTemplate primaryJdbcTemplate) {
        this.ontoJdbcTemplate = ontoJdbcTemplateProvider.getIfAvailable();
        this.primaryJdbcTemplate = primaryJdbcTemplate;
    }

    /** 选择底层本体元数据对应的 JdbcTemplate：优先 onto 数据源，回退主数据源 */
    private JdbcTemplate jdbc() {
        return ontoJdbcTemplate != null ? ontoJdbcTemplate : primaryJdbcTemplate;
    }

    /** 分类名到表名的映射，共 6 类底层本体元数据（含 i18n 翻译表） */
    public static final Map<String, String> META_TABLE_TO_ENTITY = Map.ofEntries(
            Map.entry("term", "onto_ontology_term"),
            Map.entry("eventType", "onto_ontology_event_type"),
            Map.entry("location", "onto_ontology_location"),
            Map.entry("entity", "onto_ontology_entity"),
            Map.entry("version", "onto_ontology_version"),
            Map.entry("i18n", "onto_ontology_i18n")
    );

    /** 判断分类是否属于底层本体元数据 */
    public boolean isMetaCategory(String category) {
        return META_TABLE_TO_ENTITY.containsKey(category);
    }

    /** Get table name by category. 根据分类名获取对应表名.
     * @param category 本体分类名称|term
     * @return 数据库表名
     */
    public String getTableName(String category) {
        String table = META_TABLE_TO_ENTITY.get(category);
        if (table == null) {
            throw new IllegalArgumentException("Unknown ontology meta category: " + category);
        }
        return table;
    }

    /** Find all by category. 根据分类获取所有未删除的实体.
     * @param category 本体分类名称|term
     * @return 实体列表，按id倒序
     */
    public List<Map<String, Object>> findAll(String category) {
        String table = getTableName(category);
        return sanitizeRows(jdbc().queryForList("SELECT * FROM " + table + " WHERE del_flag = 0 ORDER BY id DESC"));
    }

    /** Find page by category. 根据分类分页获取实体列表.
     * @param category 本体分类名称|term
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    public PageResult<Map<String, Object>> findPage(String category, int pageNum, int pageSize) {
        String table = getTableName(category);
        int offset = (pageNum - 1) * pageSize;
        Long total = jdbc().queryForObject("SELECT COUNT(*) FROM " + table + " WHERE del_flag = 0", Long.class);
        if (total == null || total == 0) {
            return PageResult.empty(pageNum, pageSize);
        }
        List<Map<String, Object>> records = sanitizeRows(jdbc().queryForList(
                "SELECT * FROM " + table + " WHERE del_flag = 0 ORDER BY id DESC LIMIT ? OFFSET ?", pageSize, offset));
        return new PageResult<>(records, total, pageNum, pageSize);
    }

    /** Find by id. 根据分类和主键获取实体.
     * @param category 本体分类名称|term
     * @param id 主键ID
     * @return 实体数据；不存在时返回empty
     */
    public Optional<Map<String, Object>> findById(String category, Long id) {
        String table = getTableName(category);
        List<Map<String, Object>> list = sanitizeRows(jdbc().queryForList(
                "SELECT * FROM " + table + " WHERE id = ? AND del_flag = 0", id));
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    /** Insert entity. 新增实体记录.
     * @param category 本体分类名称|term
     * @param data 实体属性键值对
     * @return 新增记录的主键ID
     */
    public Long insert(String category, Map<String, Object> data) {
        String table = getTableName(category);
        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbc())
                .withTableName(table)
                .usingGeneratedKeyColumns("id");
        Map<String, Object> mutableData = new java.util.HashMap<>(data);
        if (!mutableData.containsKey("domain")) {
            mutableData.put("domain", "siis");
        }
        mutableData.put("del_flag", 0);
        mutableData.put("created_at", java.sql.Timestamp.from(java.time.Instant.now()));
        mutableData.put("updated_at", java.sql.Timestamp.from(java.time.Instant.now()));
        if (TenantContext.getUserId() != null) {
            mutableData.put("created_by", String.valueOf(TenantContext.getUserId()));
        }
        return insert.executeAndReturnKey(mutableData).longValue();
    }

    /** Update entity. 更新实体记录.
     * @param category 本体分类名称|term
     * @param id 主键ID
     * @param data 需要更新的属性键值对
     * @return 受影响的行数
     */
    public int update(String category, Long id, Map<String, Object> data) {
        String table = getTableName(category);
        java.util.Set<String> allowed = WRITABLE_FIELDS.getOrDefault(table, java.util.Set.of());
        StringBuilder setClause = new StringBuilder();
        java.util.List<Object> paramList = new java.util.ArrayList<>();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String col = entry.getKey();
            if (!allowed.contains(col)) {
                continue;
            }
            if (!paramList.isEmpty()) setClause.append(", ");
            setClause.append(col).append(" = ?");
            paramList.add(entry.getValue());
        }
        if (paramList.isEmpty()) {
            return 0;
        }
        setClause.append(", updated_at = NOW()");
        paramList.add(id);
        String sql = "UPDATE " + table + " SET " + setClause + " WHERE id = ? AND del_flag = 0";
        return jdbc().update(sql, paramList.toArray());
    }

    /** Soft delete entity. 软删除实体记录.
     * @param category 本体分类名称|term
     * @param id 主键ID
     * @return 受影响的行数
     */
    public int softDelete(String category, Long id) {
        String table = getTableName(category);
        return jdbc().update("UPDATE " + table + " SET del_flag = 1, updated_at = NOW() WHERE id = ? AND del_flag = 0", id);
    }

    /** Count by category. 统计未删除实体数量.
     * @param category 本体分类名称|term
     * @return 实体总数
     */
    public long count(String category) {
        String table = getTableName(category);
        Long count = jdbc().queryForObject("SELECT COUNT(*) FROM " + table + " WHERE del_flag = 0", Long.class);
        return count != null ? count : 0;
    }

    /** 各表可写字段白名单，update() 时仅允许这些列，防止列名注入 */
    private static final Map<String, java.util.Set<String>> WRITABLE_FIELDS = Map.ofEntries(
            Map.entry("onto_ontology_term", java.util.Set.of("domain", "term_name", "category", "definition", "source", "lang", "status", "tenant_id", "updated_by", "remark")),
            Map.entry("onto_ontology_event_type", java.util.Set.of("domain", "event_type", "description", "parent_id", "sort_order", "status", "tenant_id", "updated_by", "remark")),
            Map.entry("onto_ontology_location", java.util.Set.of("domain", "name", "loc_type", "parent_id", "country", "capital", "population", "area", "description", "lang", "status", "tenant_id", "updated_by", "remark")),
            Map.entry("onto_ontology_entity", java.util.Set.of("domain", "category", "entity_name", "lang", "description", "source", "wiki_url", "status", "tenant_id", "updated_by", "remark")),
            Map.entry("onto_ontology_version", java.util.Set.of("domain", "version_code", "version_name", "entity_count", "source", "status", "tenant_id", "updated_by", "remark")),
            Map.entry("onto_ontology_i18n", java.util.Set.of("domain", "entity_id", "category", "lang", "name", "description", "updated_by"))
    );

    /** 内部列黑名单，查询结果中过滤掉，不对外暴露 */
    private static final java.util.Set<String> INTERNAL_COLUMNS = java.util.Set.of("del_flag", "created_by", "updated_by");

    /** 过滤查询结果中的内部列 */
    private List<Map<String, Object>> sanitizeRows(List<Map<String, Object>> rows) {
        List<Map<String, Object>> result = new java.util.ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            Map<String, Object> filtered = new java.util.LinkedHashMap<>(row);
            INTERNAL_COLUMNS.forEach(filtered::remove);
            result.add(filtered);
        }
        return result;
    }

    /** 各表搜索列映射，用于关键词搜索 */
    private static final Map<String, List<String>> SEARCH_COLUMNS = Map.ofEntries(
            Map.entry("onto_ontology_term", List.of("term_name")),
            Map.entry("onto_ontology_event_type", List.of("event_type")),
            Map.entry("onto_ontology_location", List.of("name")),
            Map.entry("onto_ontology_entity", List.of("entity_name")),
            Map.entry("onto_ontology_version", List.of("version_code", "version_name")),
            Map.entry("onto_ontology_i18n", List.of("name"))
    );

    /** Search by keyword. 关键词搜索底层本体元数据实体.
     * @param keyword 搜索关键词
     * @param category 限定分类，为空则搜索全部元数据分类
     * @param limit 返回条数限制
     * @return 匹配的实体列表
     */
    public List<Map<String, Object>> search(String keyword, String category, int limit) {
        String like = "%" + keyword + "%";
        if (category != null) {
            String table = getTableName(category);
            List<String> columns = SEARCH_COLUMNS.getOrDefault(table, List.of("name"));
            String orClause = columns.stream().map(c -> c + " LIKE ?").collect(java.util.stream.Collectors.joining(" OR "));
            Object[] params = new Object[columns.size() + 1];
            for (int i = 0; i < columns.size(); i++) params[i] = like;
            params[columns.size()] = limit;
            return sanitizeRows(jdbc().queryForList("SELECT * FROM " + table + " WHERE del_flag = 0 AND (" + orClause + ") LIMIT ?", params));
        }
        List<Map<String, Object>> results = new java.util.ArrayList<>();
        for (Map.Entry<String, String> entry : META_TABLE_TO_ENTITY.entrySet()) {
            String cat = entry.getKey();
            String table = entry.getValue();
            List<String> columns = SEARCH_COLUMNS.getOrDefault(table, List.of("name"));
            String orClause = columns.stream().map(c -> c + " LIKE ?").collect(java.util.stream.Collectors.joining(" OR "));
            Object[] params = new Object[columns.size() + 1];
            for (int i = 0; i < columns.size(); i++) params[i] = like;
            params[columns.size()] = limit;
            try {
                List<Map<String, Object>> partial = jdbc().queryForList(
                        "SELECT *, '" + cat + "' AS _category FROM " + table + " WHERE del_flag = 0 AND (" + orClause + ") LIMIT ?", params);
                results.addAll(partial);
            } catch (Exception ignored) {
            }
        }
        return sanitizeRows(results);
    }

    /** Query by filters. 根据过滤条件分页查询实体.
     * @param category 本体分类名称|term
     * @param filters 过滤条件键值对
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 符合条件的实体列表
     */
    public List<Map<String, Object>> queryByCategoryAndFilters(String category, Map<String, Object> filters, int pageNum, int pageSize) {
        String table = getTableName(category);
        java.util.Set<String> allowed = WRITABLE_FIELDS.getOrDefault(table, java.util.Set.of());
        StringBuilder where = new StringBuilder("WHERE del_flag = 0");
        java.util.List<Object> params = new java.util.ArrayList<>();
        for (Map.Entry<String, Object> entry : filters.entrySet()) {
            String col = entry.getKey();
            if (!allowed.contains(col)) {
                continue;
            }
            where.append(" AND ").append(col).append(" = ?");
            params.add(entry.getValue());
        }
        int offset = (pageNum - 1) * pageSize;
        params.add(pageSize);
        params.add(offset);
        return sanitizeRows(jdbc().queryForList("SELECT * FROM " + table + " " + where + " ORDER BY id DESC LIMIT ? OFFSET ?", params.toArray()));
    }

    /** Find i18n translations by entity. 获取实体的所有多语种翻译.
     * @param entityId 实体ID
     * @param category 本体分类名称
     * @return 翻译列表
     */
    public List<Map<String, Object>> findI18n(Long entityId, String category) {
        return sanitizeRows(jdbc().queryForList(
                "SELECT * FROM onto_ontology_i18n WHERE entity_id = ? AND category = ? AND del_flag = 0 ORDER BY lang",
                entityId, category));
    }

    /** Find i18n by lang. 获取实体指定语种的翻译.
     * @param entityId 实体ID
     * @param category 本体分类名称
     * @param lang 语种
     * @return 翻译数据；不存在时返回empty
     */
    public Optional<Map<String, Object>> findI18nByLang(Long entityId, String category, String lang) {
        List<Map<String, Object>> list = sanitizeRows(jdbc().queryForList(
                "SELECT * FROM onto_ontology_i18n WHERE entity_id = ? AND category = ? AND lang = ? AND del_flag = 0",
                entityId, category, lang));
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    /** Upsert i18n translation. 新增或更新多语种翻译.
     * @param entityId 实体ID
     * @param category 本体分类名称
     * @param lang 语种
     * @param name 该语种名称
     * @param description 该语种描述
     * @return 翻译记录ID
     */
    public Long upsertI18n(Long entityId, String category, String lang, String name, String description) {
        var existing = findI18nByLang(entityId, category, lang);
        if (existing.isPresent()) {
            Long id = (Long) existing.get().get("id");
            jdbc().update(
                    "UPDATE onto_ontology_i18n SET name = ?, description = ?, updated_at = NOW() WHERE id = ?",
                    name, description, id);
            return id;
        }
        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbc())
                .withTableName("onto_ontology_i18n")
                .usingGeneratedKeyColumns("id");
        Map<String, Object> data = new java.util.HashMap<>();
        data.put("domain", "siis");
        data.put("entity_id", entityId);
        data.put("category", category);
        data.put("lang", lang);
        data.put("name", name);
        data.put("description", description);
        data.put("del_flag", 0);
        data.put("created_at", java.sql.Timestamp.from(java.time.Instant.now()));
        data.put("updated_at", java.sql.Timestamp.from(java.time.Instant.now()));
        return insert.executeAndReturnKey(data).longValue();
    }

    /** Delete i18n translation. 删除多语种翻译.
     * @param entityId 实体ID
     * @param category 本体分类名称
     * @param lang 语种
     * @return 受影响的行数
     */
    public int deleteI18n(Long entityId, String category, String lang) {
        return jdbc().update(
                "UPDATE onto_ontology_i18n SET del_flag = 1, updated_at = NOW() WHERE entity_id = ? AND category = ? AND lang = ? AND del_flag = 0",
                entityId, category, lang);
    }

    /** Search i18n by keyword. 在翻译表中按关键词搜索指定语种.
     * @param keyword 搜索关键词
     * @param lang 语种，为空则搜索所有语种
     * @param limit 返回条数限制
     * @return 匹配的翻译列表
     */
    public List<Map<String, Object>> searchI18n(String keyword, String lang, int limit) {
        String like = "%" + keyword + "%";
        if (lang != null) {
            return sanitizeRows(jdbc().queryForList(
                    "SELECT * FROM onto_ontology_i18n WHERE del_flag = 0 AND lang = ? AND name LIKE ? LIMIT ?",
                    lang, like, limit));
        }
        return sanitizeRows(jdbc().queryForList(
                "SELECT * FROM onto_ontology_i18n WHERE del_flag = 0 AND name LIKE ? LIMIT ?",
                like, limit));
    }
}
