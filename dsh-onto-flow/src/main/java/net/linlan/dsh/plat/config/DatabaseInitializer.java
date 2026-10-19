package net.linlan.dsh.plat.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

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
        try {
            DatabaseMetaData metaData = dataSource.getConnection().getMetaData();
            String dbName = metaData.getDatabaseProductName();
            String dbVersion = metaData.getDatabaseProductVersion();

            List<String> tables = new ArrayList<>();
            try (ResultSet rs = metaData.getTables(null, null, "gwy_%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    tables.add(rs.getString("TABLE_NAME"));
                }
            }
            try (ResultSet rs = metaData.getTables(null, null, "onto_sys_user", new String[]{"TABLE"})) {
                while (rs.next()) {
                    tables.add(rs.getString("TABLE_NAME"));
                }
            }

            List<String> views = new ArrayList<>();
            try (ResultSet rs = metaData.getTables(null, null, "v_unified_%", new String[]{"VIEW"})) {
                while (rs.next()) {
                    views.add(rs.getString("TABLE_NAME"));
                }
            }

            log.info("=== Ontology Database Initialized ===");
            log.info("Database: {} {}", dbName, dbVersion);
            log.info("Tables ({}): {}", tables.size(), tables);
            log.info("Views ({}): {}", views.size(), views);

            if (tables.size() >= 52) {
                log.info("Schema validation PASSED: {} tables found", tables.size());
            } else {
                log.warn("Schema validation WARNING: expected 52 tables, found {}", tables.size());
            }

            Long userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM onto_sys_user WHERE del_flag = '0'", Long.class);
            log.info("Active users: {}", userCount);

        } catch (Exception e) {
            log.error("Failed to log schema info", e);
        }
    }
}
