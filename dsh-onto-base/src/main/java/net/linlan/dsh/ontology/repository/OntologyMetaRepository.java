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

    /**
     * 方案 A：8 类业务实体统一收敛到 onto_ontology_entity + onto_ontology_attribute（EAV）。
     * 与 {@link net.linlan.dsh.ontology.engine.catalog.TypeCatalog} 保持一致。
     */
    public static final java.util.Set<String> EAV_CATEGORIES = java.util.Set.of(
            "country", "organization", "person", "hotspot", "media", "book", "paper", "bilateralRelation");

    private static final String EAV_TABLE = "onto_ontology_entity";
    private static final String EAV_ATTR_TABLE = "onto_ontology_attribute";

    /** onto_ontology_entity 的固定列，其余字段写入属性表 */
    private static final java.util.Set<String> EAV_BASE_FIELDS = java.util.Set.of(
            "domain", "category", "entity_name", "lang", "description", "source",
            "wiki_url", "status", "tenant_id", "remark");

    /** 判断分类是否属于底层本体元数据 */
    public boolean isMetaCategory(String category) {
        return META_TABLE_TO_ENTITY.containsKey(category);
    }

    /** 判断分类是否属于方案 A 的 EAV 业务实体 */
    public boolean isEavCategory(String category) {
        return EAV_CATEGORIES.contains(category);
    }

    /** 查询发现实体（晋升工作台数据源，仅返回必要字段）。 */
    public List<Map<String, Object>> findDiscovered(String status, int limit) {
        String normalizedStatus = status == null || status.isBlank() ? "DISCOVERED" : status.trim().toUpperCase();
        int capped = Math.max(1, Math.min(limit, 100));
        return jdbc().queryForList(
                "SELECT id, entity_name, entity_type, source_topic_id, first_seen_time, mention_count, status "
                        + "FROM onto_common_discovered_entity WHERE del_flag = 0 AND status = ? "
                        + "ORDER BY mention_count DESC, id DESC LIMIT ?",
                normalizedStatus, capped);
    }

    /** Get table name by category. 根据分类名获取对应表名.
     * @param category 本体分类名称|term
     * @return 数据库表名
     */
    public String getTableName(String category) {
        if (isEavCategory(category)) {
            return EAV_TABLE;
        }
        String table = META_TABLE_TO_ENTITY.get(category);
        if (table == null) {
            throw new IllegalArgumentException("Unknown ontology meta category: " + category);
        }
        return table;
    }

    /** EAV 实体读出的名称字段：热点用 hotspot_name，书/论文用 title，其余用 name。 */
    private static String eavNameField(String category) {
        return switch (category) {
            case "hotspot" -> "hotspot_name";
            case "book", "paper" -> "title";
            default -> "name";
        };
    }

    /** 将 name / hotspot_name / title 统一收敛为 entity_name。 */
    private static String resolveEntityName(String category, Map<String, Object> data) {
        Object value = data.get("entity_name");
        if (value == null || String.valueOf(value).isBlank()) {
            value = data.get(eavNameField(category));
        }
        if (value == null || String.valueOf(value).isBlank()) {
            value = data.get("name");
        }
        if (value == null || String.valueOf(value).isBlank()) {
            value = data.get("title");
        }
        if (value == null || String.valueOf(value).isBlank()) {
            value = data.get("hotspot_name");
        }
        if ((value == null || String.valueOf(value).isBlank()) && "bilateralRelation".equals(category)) {
            Object a = data.get("country_a");
            Object b = data.get("country_b");
            if (a != null && b != null) {
                return a + " \u2194 " + b;
            }
        }
        return value == null ? null : String.valueOf(value);
    }

    /** Find all by category. 根据分类获取所有未删除的实体.
     * @param category 本体分类名称|term
     * @return 实体列表，按id倒序
     */
    public List<Map<String, Object>> findAll(String category) {
        if (isEavCategory(category)) {
            List<Map<String, Object>> rows = jdbc().queryForList(
                    "SELECT * FROM " + EAV_TABLE + " WHERE category = ? AND del_flag = 0 ORDER BY id DESC", category);
            attachAttributes(rows, category);
            return sanitizeRows(rows);
        }
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
        int offset = (pageNum - 1) * pageSize;
        if (isEavCategory(category)) {
            Long total = jdbc().queryForObject(
                    "SELECT COUNT(*) FROM " + EAV_TABLE + " WHERE category = ? AND del_flag = 0", Long.class, category);
            if (total == null || total == 0) {
                return PageResult.empty(pageNum, pageSize);
            }
            List<Map<String, Object>> records = jdbc().queryForList(
                    "SELECT * FROM " + EAV_TABLE + " WHERE category = ? AND del_flag = 0 ORDER BY id DESC LIMIT ? OFFSET ?",
                    category, pageSize, offset);
            attachAttributes(records, category);
            return new PageResult<>(sanitizeRows(records), total, pageNum, pageSize);
        }
        String table = getTableName(category);
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
        if (isEavCategory(category)) {
            List<Map<String, Object>> rows = jdbc().queryForList(
                    "SELECT * FROM " + EAV_TABLE + " WHERE id = ? AND category = ? AND del_flag = 0", id, category);
            if (rows.isEmpty()) {
                return Optional.empty();
            }
            attachAttributes(rows, category);
            return Optional.of(sanitizeRows(rows).get(0));
        }
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
        if (isEavCategory(category)) {
            return insertEav(category, data);
        }
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
        if (isEavCategory(category)) {
            return updateEav(category, id, data);
        }
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
        if (isEavCategory(category)) {
            jdbc().update("UPDATE " + EAV_ATTR_TABLE + " SET del_flag = 1, updated_at = NOW() WHERE entity_id = ? AND del_flag = 0", id);
            return jdbc().update("UPDATE " + EAV_TABLE + " SET del_flag = 1, updated_at = NOW() WHERE id = ? AND category = ? AND del_flag = 0", id, category);
        }
        String table = getTableName(category);
        return jdbc().update("UPDATE " + table + " SET del_flag = 1, updated_at = NOW() WHERE id = ? AND del_flag = 0", id);
    }

    /** Count by category. 统计未删除实体数量.
     * @param category 本体分类名称|term
     * @return 实体总数
     */
    public long count(String category) {
        if (isEavCategory(category)) {
            Long eavCount = jdbc().queryForObject(
                    "SELECT COUNT(*) FROM " + EAV_TABLE + " WHERE category = ? AND del_flag = 0", Long.class, category);
            return eavCount != null ? eavCount : 0;
        }
        String table = getTableName(category);
        Long count = jdbc().queryForObject("SELECT COUNT(*) FROM " + table + " WHERE del_flag = 0", Long.class);
        return count != null ? count : 0;
    }

    /** 方案 A：写入 onto_ontology_entity + onto_ontology_attribute。 */
    private Long insertEav(String category, Map<String, Object> data) {
        java.sql.Timestamp now = java.sql.Timestamp.from(java.time.Instant.now());
        Map<String, Object> base = new java.util.HashMap<>();
        base.put("domain", data.getOrDefault("domain", "siis"));
        base.put("category", category);
        String name = resolveEntityName(category, data);
        if (name != null) {
            base.put("entity_name", name);
        }
        if (data.get("lang") != null) {
            base.put("lang", data.get("lang"));
        }
        for (String field : new String[]{"description", "source", "wiki_url", "status", "tenant_id", "remark"}) {
            if (data.get(field) != null) {
                base.put(field, data.get(field));
            }
        }
        base.put("del_flag", 0);
        base.put("created_at", now);
        base.put("updated_at", now);
        if (TenantContext.getUserId() != null) {
            base.put("created_by", String.valueOf(TenantContext.getUserId()));
        }
        Long entityId = new SimpleJdbcInsert(jdbc())
                .withTableName(EAV_TABLE)
                .usingGeneratedKeyColumns("id")
                .executeAndReturnKey(base).longValue();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (isEavBaseKey(entry.getKey()) || entry.getValue() == null) {
                continue;
            }
            insertAttribute(entityId, category, entry.getKey(), entry.getValue(), data);
        }
        return entityId;
    }

    /** 方案 A：更新实体固定列与属性行。 */
    private int updateEav(String category, Long id, Map<String, Object> data) {
        List<Map<String, Object>> existing = jdbc().queryForList(
                "SELECT id FROM " + EAV_TABLE + " WHERE id = ? AND category = ? AND del_flag = 0", id, category);
        if (existing.isEmpty()) {
            return 0;
        }
        StringBuilder setClause = new StringBuilder();
        java.util.List<Object> paramList = new java.util.ArrayList<>();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String col = mapEavBaseColumn(entry.getKey());
            if (col == null) {
                continue;
            }
            if (!setClause.isEmpty()) {
                setClause.append(", ");
            }
            setClause.append(col).append(" = ?");
            paramList.add(entry.getValue());
        }
        int affected = 0;
        if (!setClause.isEmpty()) {
            setClause.append(", updated_at = NOW()");
            paramList.add(id);
            affected = jdbc().update("UPDATE " + EAV_TABLE + " SET " + setClause + " WHERE id = ? AND del_flag = 0",
                    paramList.toArray());
        }
        boolean attrChanged = false;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (isEavBaseKey(entry.getKey()) || entry.getValue() == null) {
                continue;
            }
            upsertAttribute(id, category, entry.getKey(), entry.getValue(), data);
            attrChanged = true;
        }
        return affected > 0 ? affected : (attrChanged ? 1 : 0);
    }

    /** name / hotspot_name / title 与实体固定列的映射；其余字段视为 EAV 属性。 */
    private String mapEavBaseColumn(String key) {
        if ("name".equals(key) || "hotspot_name".equals(key) || "title".equals(key)) {
            return "entity_name";
        }
        if (EAV_BASE_FIELDS.contains(key) && !"category".equals(key) && !"domain".equals(key)) {
            return key;
        }
        return null;
    }

    private boolean isEavBaseKey(String key) {
        return "id".equals(key) || "name".equals(key) || "hotspot_name".equals(key) || "title".equals(key)
                || EAV_BASE_FIELDS.contains(key);
    }

    private void insertAttribute(Long entityId, String category, String attrName, Object attrValue, Map<String, Object> data) {
        Map<String, Object> attr = buildAttribute(entityId, category, attrName, attrValue, data);
        new SimpleJdbcInsert(jdbc())
                .withTableName(EAV_ATTR_TABLE)
                .usingGeneratedKeyColumns("id")
                .execute(attr);
    }

    private void upsertAttribute(Long entityId, String category, String attrName, Object attrValue, Map<String, Object> data) {
        List<Long> ids = jdbc().queryForList(
                "SELECT id FROM " + EAV_ATTR_TABLE + " WHERE entity_id = ? AND attr_name = ? AND del_flag = 0",
                Long.class, entityId, attrName);
        if (!ids.isEmpty()) {
            jdbc().update("UPDATE " + EAV_ATTR_TABLE + " SET attr_value = ?, attr_type = ?, updated_at = NOW() WHERE id = ?",
                    String.valueOf(attrValue), attrType(attrValue), ids.get(0));
            return;
        }
        insertAttribute(entityId, category, attrName, attrValue, data);
    }

    private Map<String, Object> buildAttribute(Long entityId, String category, String attrName, Object attrValue, Map<String, Object> data) {
        java.sql.Timestamp now = java.sql.Timestamp.from(java.time.Instant.now());
        Map<String, Object> attr = new java.util.HashMap<>();
        attr.put("entity_id", entityId);
        attr.put("attr_name", attrName);
        attr.put("attr_value", String.valueOf(attrValue));
        attr.put("attr_type", attrType(attrValue));
        attr.put("domain", data.getOrDefault("domain", "siis"));
        if (data.get("tenant_id") != null) {
            attr.put("tenant_id", data.get("tenant_id"));
        }
        attr.put("del_flag", 0);
        attr.put("created_at", now);
        attr.put("updated_at", now);
        if (TenantContext.getUserId() != null) {
            attr.put("created_by", String.valueOf(TenantContext.getUserId()));
        }
        return attr;
    }

    private static String attrType(Object value) {
        return value instanceof Number ? "NUMBER" : "STRING";
    }

    /** 为 EAV 行批量附加属性列，并按分类补回语义名称字段。 */
    private void attachAttributes(List<Map<String, Object>> rows, String category) {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        java.util.List<Long> ids = new java.util.ArrayList<>();
        for (Map<String, Object> row : rows) {
            Object id = row.get("id");
            if (id instanceof Number) {
                ids.add(((Number) id).longValue());
            }
        }
        java.util.Map<Long, java.util.Map<String, Object>> attributes = new java.util.HashMap<>();
        if (!ids.isEmpty()) {
            String placeholders = String.join(",", java.util.Collections.nCopies(ids.size(), "?"));
            List<Map<String, Object>> attrRows = jdbc().queryForList(
                    "SELECT entity_id, attr_name, attr_value, attr_type FROM " + EAV_ATTR_TABLE
                            + " WHERE del_flag = 0 AND entity_id IN (" + placeholders + ")", ids.toArray());
            for (Map<String, Object> attrRow : attrRows) {
                Object entityId = attrRow.get("entity_id");
                if (!(entityId instanceof Number)) {
                    continue;
                }
                Object value = "NUMBER".equals(attrRow.get("attr_type"))
                        ? toNumber(attrRow.get("attr_value")) : attrRow.get("attr_value");
                attributes.computeIfAbsent(((Number) entityId).longValue(), key -> new java.util.LinkedHashMap<>())
                        .put(String.valueOf(attrRow.get("attr_name")), value);
            }
        }
        String nameField = eavNameField(category);
        for (Map<String, Object> row : rows) {
            Object id = row.get("id");
            if (id instanceof Number) {
                java.util.Map<String, Object> attrs = attributes.get(((Number) id).longValue());
                if (attrs != null) {
                    row.putAll(attrs);
                }
            }
            Object name = row.get("entity_name");
            if (name != null) {
                row.put(nameField, name);
            }
            row.put("_category", category);
        }
    }

    private static Object toNumber(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value);
        try {
            return Long.valueOf(text);
        } catch (NumberFormatException ignored) {
            try {
                return Double.valueOf(text);
            } catch (NumberFormatException ignoredToo) {
                return value;
            }
        }
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
            if (isEavCategory(category)) {
                return searchEav(keyword, category, limit);
            }
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
        for (String eavCategory : EAV_CATEGORIES) {
            try {
                results.addAll(searchEav(keyword, eavCategory, limit));
            } catch (Exception ignored) {
            }
        }
        return sanitizeRows(results);
    }

    /** EAV 实体关键词搜索：匹配实体名称与属性值。 */
    private List<Map<String, Object>> searchEav(String keyword, String category, int limit) {
        String like = "%" + keyword + "%";
        List<Map<String, Object>> rows = jdbc().queryForList(
                "SELECT e.* FROM " + EAV_TABLE + " e WHERE e.category = ? AND e.del_flag = 0 AND (e.entity_name LIKE ?"
                        + " OR EXISTS (SELECT 1 FROM " + EAV_ATTR_TABLE + " a WHERE a.entity_id = e.id"
                        + " AND a.del_flag = 0 AND a.attr_value LIKE ?)) ORDER BY e.id DESC LIMIT ?",
                category, like, like, limit);
        attachAttributes(rows, category);
        return sanitizeRows(rows);
    }

    /** Query by filters. 根据过滤条件分页查询实体.
     * @param category 本体分类名称|term
     * @param filters 过滤条件键值对
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 符合条件的实体列表
     */
    public List<Map<String, Object>> queryByCategoryAndFilters(String category, Map<String, Object> filters, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        if (isEavCategory(category)) {
            StringBuilder where = new StringBuilder("WHERE e.category = ? AND e.del_flag = 0");
            java.util.List<Object> params = new java.util.ArrayList<>();
            params.add(category);
            for (Map.Entry<String, Object> entry : filters.entrySet()) {
                String key = entry.getKey();
                String baseColumn = mapEavBaseColumn(key);
                if (baseColumn != null) {
                    where.append(" AND e.").append(baseColumn).append(" = ?");
                    params.add(entry.getValue());
                } else if (!"category".equals(key) && !"id".equals(key)) {
                    where.append(" AND EXISTS (SELECT 1 FROM ").append(EAV_ATTR_TABLE)
                            .append(" a WHERE a.entity_id = e.id AND a.del_flag = 0 AND a.attr_name = ? AND a.attr_value = ?)");
                    params.add(key);
                    params.add(String.valueOf(entry.getValue()));
                }
            }
            params.add(pageSize);
            params.add(offset);
            List<Map<String, Object>> rows = jdbc().queryForList(
                    "SELECT e.* FROM " + EAV_TABLE + " e " + where + " ORDER BY e.id DESC LIMIT ? OFFSET ?", params.toArray());
            attachAttributes(rows, category);
            return sanitizeRows(rows);
        }
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
