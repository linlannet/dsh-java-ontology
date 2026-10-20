package net.linlan.dsh.onto;

import net.linlan.dsh.onto.model.AppInfo;
import net.linlan.dsh.onto.model.EndpointDescriptor;
import net.linlan.dsh.onto.service.AppMetaService;
import net.linlan.dsh.ontology.BaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 应用层元信息服务测试：元信息、端点分组、健康自检
 * @author Linlan
 * CreatedAt 2026-10-19
 * @version 1.0
 * @since 1.0
 */
class AppMetaServiceTest extends BaseTest {

    @Autowired
    private AppMetaService metaService;

    @Test
    void testAppInfo() {
        AppInfo info = metaService.appInfo();
        assertEquals("dsh-onto-app", info.name());
        assertEquals("0.2.1", info.version());
        assertNotNull(info.description());
        assertTrue(info.modules().containsAll(
                List.of("app", "base", "flow", "qa", "viz", "metric")));
    }

    @Test
    void testEndpointsGroupedByModule() {
        Map<String, List<EndpointDescriptor>> grouped = metaService.endpoints();
        for (String module : List.of("app", "base", "flow", "qa", "viz", "metric")) {
            assertTrue(grouped.containsKey(module), "缺少模块分组: " + module);
        }
        assertTrue(grouped.get("app").stream()
                .anyMatch(e -> e.path().equals("/api/app/info")), "app 分组应含 /api/app/info");
        assertTrue(grouped.get("flow").stream()
                .anyMatch(e -> e.path().startsWith("/api/ontologies")), "flow 分组应含 /api/ontologies");
        assertTrue(grouped.get("flow").stream()
                .anyMatch(e -> e.path().startsWith("/api/auth/")), "flow 分组应含 /api/auth/");
        assertTrue(grouped.get("qa").stream()
                .anyMatch(e -> e.path().startsWith("/api/report")), "qa 分组应含 /api/report");
    }

    @Test
    void testHealth() {
        Map<String, Object> health = metaService.health();
        assertTrue(String.valueOf(health.get("database")).startsWith("jdbc:h2"),
                "测试环境数据源应为 H2");
        assertEquals(Boolean.TRUE, health.get("flywaySuccess"), "Flyway 迁移应成功");
        assertNotNull(health.get("flywayVersion"), "应返回最新迁移版本");
    }
}
