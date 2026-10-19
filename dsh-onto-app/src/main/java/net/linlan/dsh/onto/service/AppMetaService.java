package net.linlan.dsh.onto.service;

import net.linlan.dsh.onto.config.OntologyAppProperties;
import net.linlan.dsh.onto.model.AppInfo;
import net.linlan.dsh.onto.model.EndpointDescriptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 聚合应用元信息服务
 * <p>从 Spring MVC 映射注册表提取全部入口端点，并按模块归属（app/base/flow/report）分组；
 * 数据库与 Flyway 迁移状态用于应用健康自检。</p>
 * <p>本服务是应用层专属能力，不与 base/flow/report/visulization 任何业务端点重叠。</p>
 * @author Linlan
 * CreatedAt 2026-10-19
 * @version 1.0
 * @since 1.0
 */
@Service
public class AppMetaService {

    private static final List<String> MODULES = List.of("app", "base", "flow", "report", "visulization");

    private final OntologyAppProperties properties;
    private final Environment environment;
    private final JdbcTemplate jdbcTemplate;
    private final RequestMappingHandlerMapping handlerMapping;

    public AppMetaService(OntologyAppProperties properties,
                          Environment environment,
                          JdbcTemplate jdbcTemplate,
                          @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping handlerMapping) {
        this.properties = properties;
        this.environment = environment;
        this.jdbcTemplate = jdbcTemplate;
        this.handlerMapping = handlerMapping;
    }

    /** 应用元信息：名称/版本/描述/profile/聚合模块 */
    public AppInfo appInfo() {
        return new AppInfo(
                properties.name(),
                properties.version(),
                properties.description(),
                List.of(environment.getActiveProfiles()),
                MODULES);
    }

    /** 全部入口端点清单，按模块分组（module → 端点列表） */
    public Map<String, List<EndpointDescriptor>> endpoints() {
        Map<String, List<EndpointDescriptor>> grouped = new LinkedHashMap<>();
        for (String module : MODULES) {
            grouped.put(module, new ArrayList<>());
        }
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMapping.getHandlerMethods().entrySet()) {
            RequestMappingInfo info = entry.getKey();
            HandlerMethod method = entry.getValue();
            String module = moduleOf(method.getBeanType());
            String handler = method.getBeanType().getSimpleName() + "#" + method.getMethod().getName();
            for (String path : info.getDirectPaths()) {
                for (String httpMethod : info.getMethodsCondition().getMethods().stream()
                        .map(Enum::name).sorted().toList()) {
                    grouped.get(module).add(new EndpointDescriptor(module, httpMethod, path, handler));
                }
            }
        }
        grouped.values().forEach(list -> list.sort(Comparator.comparing(EndpointDescriptor::path)
                .thenComparing(EndpointDescriptor::method)));
        return grouped;
    }

    /** 应用健康自检：数据源 URL 与 Flyway 迁移状态 */
    public Map<String, Object> health() {
        Map<String, Object> health = new LinkedHashMap<>();
        try (var connection = jdbcTemplate.getDataSource() != null
                ? jdbcTemplate.getDataSource().getConnection() : null) {
            if (connection != null) {
                health.put("database", connection.getMetaData().getURL());
                health.put("databaseProduct", connection.getMetaData().getDatabaseProductName());
            }
        } catch (Exception e) {
            health.put("databaseError", e.getMessage());
        }
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT version, description, success FROM sys_flyway_schema_history ORDER BY installed_rank DESC LIMIT 1");
            if (!rows.isEmpty()) {
                Map<String, Object> latest = rows.get(0);
                health.put("flywayVersion", latest.get("version"));
                health.put("flywayDescription", latest.get("description"));
                health.put("flywaySuccess", latest.get("success"));
            }
        } catch (Exception e) {
            health.put("flywayError", e.getMessage());
        }
        return health;
    }

    /** 按处理器包名归属模块：onto→app，report→report，plat/sys/global/workspace→flow，其余→base */
    private String moduleOf(Class<?> beanType) {
        String packageName = beanType.getPackage() != null ? beanType.getPackageName() : "";
        if (packageName.startsWith("net.linlan.dsh.onto.")) {
            return "app";
        }
        if (packageName.startsWith("net.linlan.dsh.report.")) {
            return "report";
        }
        if (packageName.startsWith("net.linlan.dsh.global.")
                || packageName.startsWith("net.linlan.dsh.plat.")
                || packageName.startsWith("net.linlan.dsh.sys.")
                || packageName.startsWith("net.linlan.dsh.workspace.")) {
            return "flow";
        }
        return "base";
    }
}
