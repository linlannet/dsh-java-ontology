package net.linlan.dsh.plat.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class DatabaseInitializer {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public DatabaseInitializer(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logSchemaInfo() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String dbName = metaData.getDatabaseProductName();
            String dbVersion = metaData.getDatabaseProductVersion();
            // H2 等驱动必须显式传入 catalog/schema，否则 getTables 返回空集
            String catalog = connection.getCatalog();
            String schema = connection.getSchema();

            List<String> tables = listObjects(metaData, catalog, schema, "TABLE", "onto_", "gwy_");
            List<String> views = listObjects(metaData, catalog, schema, "VIEW", "v_unified_");

            log.info("=== Ontology Database Initialized ===");
            log.info("Database: {} {}", dbName, dbVersion);
            log.info("Tables ({}): {}", tables.size(), tables);
            log.info("Views ({}): {}", views.size(), views);

            if (tables.size() >= 40) {
                log.info("Schema validation PASSED: {} tables found", tables.size());
            } else {
                log.warn("Schema validation WARNING: expected at least 40 onto_/gwy_ tables, found {}", tables.size());
            }

            Long userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM onto_sys_user WHERE del_flag = '0'", Long.class);
            log.info("Active users: {}", userCount);

        } catch (Exception e) {
            log.error("Failed to log schema info", e);
        }
    }

    /**
     * 按类型列出名称匹配指定前缀的对象。
     * <p>H2 在 schema 为空时不返回任何结果，因此首次为空时回退到不限 catalog/schema 重查；
     * H2 默认把未加引号的标识符存为大写，前缀匹配统一转小写。</p>
     */
    private List<String> listObjects(DatabaseMetaData metaData, String catalog, String schema,
                                     String type, String... prefixes) throws SQLException {
        List<String> names = collectObjects(metaData, catalog, schema, type, prefixes);
        if (names.isEmpty() && (catalog != null || schema != null)) {
            names = collectObjects(metaData, null, null, type, prefixes);
        }
        return names;
    }

    private List<String> collectObjects(DatabaseMetaData metaData, String catalog, String schema,
                                        String type, String... prefixes) throws SQLException {
        List<String> names = new ArrayList<>();
        try (ResultSet rs = metaData.getTables(catalog, schema, "%", new String[]{type})) {
            while (rs.next()) {
                String objectName = rs.getString("TABLE_NAME");
                if (objectName == null) {
                    continue;
                }
                String normalized = objectName.toLowerCase(Locale.ROOT);
                for (String prefix : prefixes) {
                    if (normalized.startsWith(prefix)) {
                        names.add(objectName);
                        break;
                    }
                }
            }
        }
        return names;
    }
}
