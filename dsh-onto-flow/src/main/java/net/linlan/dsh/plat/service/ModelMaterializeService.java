package net.linlan.dsh.plat.service;

import net.linlan.dsh.ontology.engine.OntologyModelEngine;
import net.linlan.dsh.ontology.engine.model.AggregateDescriptor;
import net.linlan.dsh.ontology.engine.model.AttributeDescriptor;
import net.linlan.dsh.plat.model.ApiException;
import net.linlan.dsh.plat.model.ColumnDesignVO;
import net.linlan.dsh.plat.model.MaterializeConfirmRequest;
import net.linlan.dsh.plat.model.MaterializePreviewVO;
import net.linlan.dsh.plat.model.MaterializeRequest;
import net.linlan.dsh.plat.model.MaterializeResultVO;
import net.linlan.dsh.plat.model.TableDesignVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模型物化服务（两阶段确认）。
 *
 * <p>{@link #preview(MaterializeRequest)} 只读模型快照，生成表设计 DDL、draw.io ER 图与一次性确认令牌，
 * <b>绝不写库</b>；{@link #confirm(MaterializeConfirmRequest)} 仅在令牌未过期、确认短语正确、
 * aliases 与预览一致、且模型指纹未变时才执行 {@code CREATE TABLE IF NOT EXISTS}。</p>
 *
 * <p>表名前缀固定为 {@code onto_model_}，仅来源于模型 alias 的规范化结果（白名单字符），不接受用户输入直拼。</p>
 */
@Service
public class ModelMaterializeService {

    /** 用户必须显式传此短语，缺一不可。 */
    public static final String CONFIRM_PHRASE = "CONFIRM_CREATE_TABLE";
    private static final String TABLE_PREFIX = "onto_model_";
    private static final long TOKEN_TTL_MS = 10 * 60 * 1000L;
    private static final Logger log = LoggerFactory.getLogger(ModelMaterializeService.class);

    private record PendingPlan(Set<String> aliases, String fingerprint, long expiresAt) {}

    private final OntologyModelEngine engine;
    private final JdbcTemplate jdbcTemplate;
    private final ConcurrentHashMap<String, PendingPlan> pending = new ConcurrentHashMap<>();

    public ModelMaterializeService(ObjectProvider<OntologyModelEngine> engineProvider, JdbcTemplate jdbcTemplate) {
        this.engine = engineProvider.getIfAvailable();
        this.jdbcTemplate = jdbcTemplate;
    }

    // ── 阶段一：预览（只读，不写库） ─────────────────────────────────

    public MaterializePreviewVO preview(MaterializeRequest request) {
        OntologyModelEngine current = requireEngine();
        Set<String> aliases = normalizeAliases(request);
        List<TableDesignVO> tables = buildTables(current, aliases);
        if (tables.isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "未找到指定的模型聚合根：" + aliases);
        }

        String fingerprint = fingerprint(current, tables);
        String token = UUID.randomUUID().toString().replace("-", "");
        pending.put(token, new PendingPlan(Set.copyOf(aliases), fingerprint,
                System.currentTimeMillis() + TOKEN_TTL_MS));
        log.info("模型物化预览已生成：token={}, aliases={}, tables={}, fingerprint={}",
                token, aliases, tables.size(), fingerprint);

        return new MaterializePreviewVO(
                token,
                System.currentTimeMillis() + TOKEN_TTL_MS,
                tables.size(),
                tables,
                buildDrawio(tables),
                "仅预览，未写库。请审阅 DDL 与 drawio 设计；确认无误后，用 token 与确认短语 "
                        + CONFIRM_PHRASE + " 调用 confirm 才会真正建表（令牌 10 分钟内一次性有效）。");
    }

    // ── 阶段二：确认建表（四重校验） ─────────────────────────────────

    public MaterializeResultVO confirm(MaterializeConfirmRequest request) {
        if (request.token() == null || request.token().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "缺少 token；未执行任何建表");
        }
        if (!CONFIRM_PHRASE.equals(request.confirmPhrase())) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "确认短语不正确，必须为 " + CONFIRM_PHRASE + "；未执行任何建表");
        }

        PendingPlan plan = pending.get(request.token());
        if (plan == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "令牌不存在或已使用；未执行任何建表");
        }
        if (System.currentTimeMillis() > plan.expiresAt()) {
            pending.remove(request.token());
            throw new ApiException(HttpStatus.BAD_REQUEST, "令牌已过期，请重新预览；未执行任何建表");
        }
        Set<String> requested = Set.copyOf(request.aliases().stream()
                .map(a -> a == null ? "" : a.trim()).filter(a -> !a.isEmpty()).toList());
        if (!plan.aliases().equals(requested)) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "aliases 与预览不一致；未执行任何建表。预览=" + plan.aliases() + "，本次=" + requested);
        }

        OntologyModelEngine current = requireEngine();
        List<TableDesignVO> tables = buildTables(current, requested);
        if (!plan.fingerprint().equals(fingerprint(current, tables))) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "模型已变更（指纹不匹配），请重新预览；未执行任何建表");
        }

        // 全部校验通过——消费令牌（一次性），开始执行
        pending.remove(request.token());
        List<String> created = new ArrayList<>();
        List<String> existing = new ArrayList<>();
        for (TableDesignVO table : tables) {
            String name = table.tableName();
            if (tableExists(name)) {
                existing.add(name);
                continue;
            }
            try {
                jdbcTemplate.execute(table.ddl());
                for (String indexDdl : table.indexDdls()) {
                    try {
                        jdbcTemplate.execute(indexDdl);
                    } catch (Exception indexError) {
                        log.warn("索引创建失败（不影响表）: {} - {}", indexDdl, indexError.getMessage());
                    }
                }
                created.add(name);
                log.info("模型物化建表成功: {} <- {}", name, table.alias());
            } catch (Exception e) {
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "建表失败：" + name + " - " + e.getMessage());
            }
        }
        return new MaterializeResultVO(true, created, existing,
                "建表完成：新建 " + created.size() + " 张，已存在 " + existing.size() + " 张");
    }

    // ── 内部：别名、表设计、指纹 ────────────────────────────────────

    private OntologyModelEngine requireEngine() {
        if (engine == null) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "模型引擎未启用（dsh.ontology.model.enabled=false）");
        }
        return engine;
    }

    private Set<String> normalizeAliases(MaterializeRequest request) {
        if (request.all()) {
            return Set.of("*");
        }
        Set<String> result = new LinkedHashSet<>();
        for (String alias : request.aliases()) {
            if (alias != null && !alias.trim().isEmpty()) {
                result.add(alias.trim());
            }
        }
        return result;
    }

    private List<TableDesignVO> buildTables(OntologyModelEngine current, Set<String> aliases) {
        List<AggregateDescriptor> roots = new ArrayList<>();
        if (aliases.contains("*")) {
            roots.addAll(current.snapshot().descriptors());
        } else {
            for (String alias : aliases) {
                Optional<AggregateDescriptor> found = current.registry().byAlias(alias);
                if (found.isEmpty()) {
                    throw new ApiException(HttpStatus.NOT_FOUND, "未知聚合根 alias：" + alias);
                }
                roots.add(found.get());
            }
        }

        List<TableDesignVO> tables = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (AggregateDescriptor root : roots) {
            collectTable(root, null, tables, seen);
            for (AggregateDescriptor child : root.subEntities()) {
                collectTable(child, root.alias(), tables, seen);
            }
        }
        tables.sort(Comparator.comparing(TableDesignVO::tableName));
        return tables;
    }

    private void collectTable(AggregateDescriptor aggregate, String ownerAlias,
                              List<TableDesignVO> tables, Set<String> seen) {
        String table = tableName(aggregate.alias());
        if (!seen.add(table)) {
            return;
        }
        List<ColumnDesignVO> columns = new ArrayList<>();
        columns.add(new ColumnDesignVO("id", "主键", "BIGINT", true, false,
                List.of(), null, "自增主键", "generated"));
        for (AttributeDescriptor attribute : aggregate.attributes()) {
            columns.add(new ColumnDesignVO(
                    columnName(attribute.name()),
                    attribute.label(),
                    sqlType(attribute),
                    attribute.required(),
                    attribute.unique(),
                    attribute.enumValues(),
                    attribute.defaultValue(),
                    attribute.description(),
                    attribute.sourcePath()));
        }
        if (ownerAlias != null) {
            columns.add(new ColumnDesignVO("owner_id", "所属实体ID", "BIGINT", false, false,
                    List.of(), null, "所属父实体 " + ownerAlias + " 的主键", "generated"));
        }
        columns.addAll(auditColumns());

        String ddl = buildCreateTableSql(table, aggregate.name(), columns);
        List<String> indexes = buildIndexSql(table, columns);
        tables.add(new TableDesignVO(aggregate.alias(), ownerAlias, table,
                aggregate.sourceFile(), aggregate.id(), columns, ddl, indexes));
    }

    private List<ColumnDesignVO> auditColumns() {
        return List.of(
                new ColumnDesignVO("tenant_id", "租户ID", "BIGINT", false, false,
                        List.of(), null, "多租户隔离；共享层为 NULL", "audit"),
                new ColumnDesignVO("del_flag", "删除标志", "TINYINT", false, false,
                        List.of(), "0", "0=正常 1=删除", "audit"),
                new ColumnDesignVO("created_by", "创建者", "VARCHAR(64)", false, false,
                        List.of(), null, null, "audit"),
                new ColumnDesignVO("created_at", "创建时间", "DATETIME", false, false,
                        List.of(), "CURRENT_TIMESTAMP", null, "audit"),
                new ColumnDesignVO("updated_by", "更新者", "VARCHAR(64)", false, false,
                        List.of(), null, null, "audit"),
                new ColumnDesignVO("updated_at", "更新时间", "DATETIME", false, false,
                        List.of(), "CURRENT_TIMESTAMP", null, "audit"));
    }

    private String buildCreateTableSql(String table, String comment, List<ColumnDesignVO> columns) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS ").append(table).append(" (\n");
        List<String> parts = new ArrayList<>();
        for (ColumnDesignVO column : columns) {
            StringBuilder part = new StringBuilder("  ").append(column.name())
                    .append(' ').append(column.sqlType());
            if (column.required()) {
                part.append(" NOT NULL");
            }
            if (column.defaultValue() != null && !column.defaultValue().isBlank()) {
                part.append(" DEFAULT ").append(defaultLiteral(column));
            }
            String note = column.description();
            if (!column.enumValues().isEmpty()) {
                note = (note == null ? "" : note + "；") + "枚举=" + String.join("/", column.enumValues());
            }
            if (note != null && !note.isBlank()) {
                part.append(" COMMENT '").append(escapeSql(note)).append('\'');
            }
            parts.add(part.toString());
        }
        parts.add("  PRIMARY KEY (id)");
        for (ColumnDesignVO column : columns) {
            if (column.unique() && !"id".equals(column.name())) {
                parts.add("  UNIQUE KEY uk_" + shortName(column.name()) + " (" + column.name() + ")");
            }
        }
        sb.append(String.join(",\n", parts)).append("\n)");
        sb.append(" ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
        sb.append(" COMMENT='").append(escapeSql(comment == null ? table : comment))
                .append("（模型生成）'");
        return sb.toString();
    }

    private String defaultLiteral(ColumnDesignVO column) {
        String value = column.defaultValue();
        if ("CURRENT_TIMESTAMP".equalsIgnoreCase(value)) {
            return "CURRENT_TIMESTAMP";
        }
        if ("BIGINT".equals(column.sqlType()) || "INT".equals(column.sqlType())
                || "TINYINT".equals(column.sqlType()) || "DECIMAL(18,2)".equals(column.sqlType())) {
            return value.matches("-?\\d+(\\.\\d+)?") ? value : "0";
        }
        return "'" + escapeSql(value) + "'";
    }

    private List<String> buildIndexSql(String table, List<ColumnDesignVO> columns) {
        List<String> indexes = new ArrayList<>();
        for (ColumnDesignVO column : columns) {
            if (Set.of("tenant_id", "category", "status", "owner_id").contains(column.name())
                    && !column.unique()) {
                indexes.add("CREATE INDEX idx_" + shortName(table.replace(TABLE_PREFIX, ""))
                        + "_" + shortName(column.name()) + " ON " + table + " (" + column.name() + ")");
            }
        }
        return indexes;
    }

    private String fingerprint(OntologyModelEngine current, List<TableDesignVO> tables) {
        StringBuilder raw = new StringBuilder();
        for (TableDesignVO table : tables) {
            raw.append(table.tableName()).append('|').append(table.ddl()).append('\n');
        }
        raw.append("v=").append(current.snapshot().manifests().stream()
                .map(m -> String.valueOf(m.version())).reduce("", (a, b) -> a + b));
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.substring(0, 16);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    private boolean tableExists(String tableName) {
        try {
            Long count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables "
                            + "WHERE UPPER(table_name) = UPPER(?) AND table_schema = DATABASE()",
                    Long.class, tableName);
            return count != null && count > 0;
        } catch (Exception e) {
            log.debug("表存在性检查失败，按不存在处理: {} - {}", tableName, e.getMessage());
            return false;
        }
    }

    // ── 内部：命名与类型映射 ────────────────────────────────────────

    private static String tableName(String alias) {
        return TABLE_PREFIX + camelToSnake(alias);
    }

    private static String columnName(String name) {
        String column = camelToSnake(name);
        return column.isEmpty() ? "field" : column;
    }

    private static String camelToSnake(String value) {
        String result = value == null ? "" : value.trim()
                .replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                .replaceAll("[^A-Za-z0-9_]", "_")
                .toLowerCase(Locale.ROOT);
        while (result.contains("__")) {
            result = result.replace("__", "_");
        }
        return result.replaceAll("^_+|_+$", "");
    }

    private static String shortName(String value) {
        String result = value.length() > 40 ? value.substring(0, 40) : value;
        return result.replaceAll("[^A-Za-z0-9_]", "_");
    }

    private static String sqlType(AttributeDescriptor attribute) {
        String type = attribute.type() == null ? "" : attribute.type().trim().toLowerCase(Locale.ROOT);
        return switch (type) {
            case "long", "integer", "int" -> "BIGINT";
            case "short", "byte" -> "INT";
            case "boolean", "bool" -> "TINYINT";
            case "double", "float", "number", "decimal", "bigdecimal" -> "DECIMAL(18,2)";
            case "text", "clob" -> "TEXT";
            case "datetime", "timestamp", "localdatetime", "date" -> "DATETIME";
            case "enum" -> "VARCHAR(128)";
            case "json", "object" -> "TEXT";
            default -> "VARCHAR(255)";
        };
    }

    private static String escapeSql(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("'", "''");
    }

    private static String escapeXml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }

    // ── 内部：draw.io ER 图 ─────────────────────────────────────────

    private String buildDrawio(List<TableDesignVO> tables) {
        StringBuilder sb = new StringBuilder();
        sb.append("<mxfile host=\"app.diagrams.net\" version=\"21.0.0\">\n");
        sb.append("  <diagram id=\"onto-model-design\" name=\"本体建模表设计\">\n");
        sb.append("    <mxGraphModel dx=\"1422\" dy=\"820\" grid=\"1\" gridSize=\"10\" guides=\"1\" "
                + "tooltips=\"1\" connect=\"1\" arrows=\"1\" fold=\"1\" page=\"1\" pageScale=\"1\" "
                + "pageWidth=\"1600\" pageHeight=\"1200\" math=\"0\" shadow=\"0\">\n");
        sb.append("      <root>\n");
        sb.append("        <mxCell id=\"0\" />\n");
        sb.append("        <mxCell id=\"1\" parent=\"0\" />\n");

        Map<String, String> cellByAlias = new LinkedHashMap<>();
        int x = 40;
        int y = 40;
        for (TableDesignVO table : tables) {
            String cellId = "tbl_" + shortName(table.tableName());
            cellByAlias.put(table.alias(), cellId);
            StringBuilder label = new StringBuilder();
            label.append("<b>").append(escapeXml(table.tableName())).append("</b>");
            label.append("<br><font color=\"#666\">").append(escapeXml(table.alias()))
                    .append(" / ").append(escapeXml(table.sourceFile())).append("</font>");
            for (ColumnDesignVO column : table.columns()) {
                label.append("<br>").append(escapeXml(column.name()))
                        .append(" : ").append(escapeXml(column.sqlType()));
                if (column.required()) label.append(" *");
                if (column.unique()) label.append(" [UQ]");
                if (!column.enumValues().isEmpty()) {
                    label.append(" {").append(escapeXml(String.join("/", column.enumValues()))).append("}");
                }
            }
            int height = 60 + table.columns().size() * 18;
            sb.append("        <mxCell id=\"").append(cellId).append("\" value=\"")
                    .append(escapeXml(label.toString()))
                    .append("\" style=\"rounded=0;whiteSpace=wrap;html=1;align=left;verticalAlign=top;"
                            + "spacingLeft=8;spacingTop=4;fillColor=#dae8fc;strokeColor=#6c8ebf;\" "
                            + "vertex=\"1\" parent=\"1\">\n");
            sb.append("          <mxGeometry x=\"").append(x).append("\" y=\"").append(y)
                    .append("\" width=\"280\" height=\"").append(height).append("\" as=\"geometry\" />\n");
            sb.append("        </mxCell>\n");
            x += 320;
            if (x > 1300) {
                x = 40;
                y += 460;
            }
        }

        int edge = 0;
        for (TableDesignVO table : tables) {
            String owner = table.ownerAlias();
            if (owner == null) continue;
            String from = cellByAlias.get(owner);
            String to = cellByAlias.get(table.alias());
            if (from == null || to == null) continue;
            sb.append("        <mxCell id=\"edge").append(edge).append("\" value=\"owner\" "
                    + "style=\"edgeStyle=orthogonalEdgeStyle;rounded=0;html=1;endArrow=open;\" "
                    + "edge=\"1\" parent=\"1\" source=\"").append(from)
                    .append("\" target=\"").append(to).append("\">\n");
            sb.append("          <mxGeometry relative=\"1\" as=\"geometry\" />\n");
            sb.append("        </mxCell>\n");
            edge++;
        }

        sb.append("      </root>\n");
        sb.append("    </mxGraphModel>\n");
        sb.append("  </diagram>\n");
        sb.append("</mxfile>\n");
        return sb.toString();
    }
}
