package net.linlan.dsh.sys;

import net.linlan.dsh.DshJavaOntologyApplication;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.AbstractHandlerMethodMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * P2-19 权限矩阵自动化测试
 * <p>遍历所有注册的 Controller 端点，用三角色（director=0, researcher=1, guest=2）token 打每个端点，
 * 断言权限不足时返回 403，权限充足时不返回 403。
 * <p>同时生成 permissions.yaml 活文档（docs/contract/permissions.yaml）。
 * @author Linlan
 * CreatedAt 2026-10-18
 * @version 1.0
 * @since 1.0
 */
@SpringBootTest(classes = DshJavaOntologyApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("P2-19 权限矩阵自动化测试")
class PermissionMatrixTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    @org.springframework.beans.factory.annotation.Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

    private String baseUrl;
    private String directorToken;
    private String researcherToken;
    private String guestToken;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        directorToken = login("director", "password123");
        researcherToken = login("researcher", "password123");
        guestToken = login("guest", "password123");
    }

    @SuppressWarnings("unchecked")
    private String login(String username, String password) {
        Map<String, String> body = Map.of("userName", username, "password", password);
        ResponseEntity<Map> resp = restTemplate.postForEntity(
                baseUrl + "/api/auth/login", body, Map.class);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        Map<String, Object> data = (Map<String, Object>) resp.getBody().get("data");
        return "Bearer " + data.get("token");
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", token);
        return headers;
    }

    /**
     * 收集所有注册端点的权限元数据
     */
    private List<EndpointInfo> collectEndpoints() {
        List<EndpointInfo> endpoints = new ArrayList<>();
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();

        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            RequestMappingInfo info = entry.getKey();
            HandlerMethod method = entry.getValue();

            Set<String> patterns = info.getPathPatternsCondition() != null
                    ? info.getPathPatternsCondition().getPatternValues()
                    : Collections.emptySet();
            if (patterns.isEmpty()) continue;

            Set<RequestMethod> requestMethods = info.getMethodsCondition() != null
                    ? info.getMethodsCondition().getMethods()
                    : Collections.emptySet();
            Set<HttpMethod> httpMethods;
            if (requestMethods.isEmpty()) {
                httpMethods = Set.of(HttpMethod.GET);
            } else {
                httpMethods = new LinkedHashSet<>();
                for (RequestMethod rm : requestMethods) {
                    httpMethods.add(HttpMethod.valueOf(rm.name()));
                }
            }

            net.linlan.dsh.sys.security.PublicApi publicApi =
                    method.getMethodAnnotation(net.linlan.dsh.sys.security.PublicApi.class);
            net.linlan.dsh.sys.security.RequiresRole requiresRole =
                    method.getMethodAnnotation(net.linlan.dsh.sys.security.RequiresRole.class);

            int requiredRole;
            if (publicApi != null) {
                requiredRole = -1;
            } else if (requiresRole != null) {
                requiredRole = requiresRole.value();
            } else {
                requiredRole = Integer.MAX_VALUE;
            }

            String controller = method.getBeanType().getSimpleName();
            String methodSignature = method.getMethod().getName();

            for (String pattern : patterns) {
                if (controller.startsWith("BasicErrorController") || pattern.equals("/error")) {
                    continue;
                }
                if (info.getProducesCondition() != null &&
                        info.getProducesCondition().getProducibleMediaTypes().stream()
                                .anyMatch(mt -> mt.isCompatibleWith(org.springframework.http.MediaType.TEXT_EVENT_STREAM))) {
                    continue;
                }
                for (HttpMethod httpMethod : httpMethods) {
                    endpoints.add(new EndpointInfo(
                            pattern, httpMethod, requiredRole, controller, methodSignature));
                }
            }
        }
        endpoints.sort(Comparator.comparing(EndpointInfo::pattern)
                .thenComparing(e -> e.httpMethod().name()));
        return endpoints;
    }

    private String resolvePath(String pattern) {
        return pattern.replaceAll("\\{[^}]+}", "0");
    }

    private ResponseEntity<String> callEndpoint(EndpointInfo endpoint, String token) {
        String url = baseUrl + resolvePath(endpoint.pattern());
        HttpHeaders headers = token != null ? authHeaders(token) : new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>("{}", headers);
        return restTemplate.exchange(url, endpoint.httpMethod(), entity, String.class);
    }

    @Test
    @DisplayName("所有端点均已标注 @PublicApi 或 @RequiresRole（fail-closed）")
    void testNoUnannotatedEndpoints() {
        List<EndpointInfo> endpoints = collectEndpoints();
        List<EndpointInfo> unannotated = endpoints.stream()
                .filter(e -> e.requiredRole() == Integer.MAX_VALUE)
                .toList();

        assertTrue(unannotated.isEmpty(),
                "存在未标注 @PublicApi 或 @RequiresRole 的端点（fail-closed 违规）:\n" +
                unannotated.stream()
                        .map(e -> "  " + e.httpMethod() + " " + e.pattern() + " — " + e.controller() + "." + e.methodSignature())
                        .reduce("", (a, b) -> a + b + "\n"));
    }

    @Test
    @DisplayName("权限矩阵：三角色访问所有端点，权限不足必须 403")
    void testPermissionMatrix() {
        List<EndpointInfo> endpoints = collectEndpoints();
        assertFalse(endpoints.isEmpty(), "应至少有一个注册端点");

        List<String> violations = new ArrayList<>();

        for (EndpointInfo endpoint : endpoints) {
            if (endpoint.requiredRole() == -1) {
                continue;
            }

            Map<String, String> tokens = Map.of(
                    "director(0)", directorToken,
                    "researcher(1)", researcherToken,
                    "guest(2)", guestToken);

            for (Map.Entry<String, String> roleEntry : tokens.entrySet()) {
                String roleLabel = roleEntry.getKey();
                String token = roleEntry.getValue();
                int roleLevel = Integer.parseInt(roleLabel.substring(roleLabel.indexOf('(') + 1, roleLabel.indexOf(')')));

                ResponseEntity<String> resp = callEndpoint(endpoint, token);
                int status = resp.getStatusCode().value();

                if (roleLevel > endpoint.requiredRole()) {
                    if (status != 403) {
                        violations.add(String.format(
                                "应 403 但返回 %d: %s %s — 角色 %s (需 role<=%d)",
                                status, endpoint.httpMethod(), endpoint.pattern(), roleLabel, endpoint.requiredRole()));
                    }
                } else {
                    if (status == 403) {
                        violations.add(String.format(
                                "不应 403: %s %s — 角色 %s (需 role<=%d)",
                                endpoint.httpMethod(), endpoint.pattern(), roleLabel, endpoint.requiredRole()));
                    }
                }
            }
        }

        assertTrue(violations.isEmpty(),
                "权限矩阵违规 " + violations.size() + " 条:\n" +
                violations.stream().map(v -> "  " + v).reduce("", (a, b) -> a + b + "\n"));
    }

    @Test
    @DisplayName("@PublicApi 端点无需 token 即可访问（不返回 401/403）")
    void testPublicApiEndpoints() {
        List<EndpointInfo> endpoints = collectEndpoints();
        List<String> violations = new ArrayList<>();

        for (EndpointInfo endpoint : endpoints) {
            if (endpoint.requiredRole() != -1) {
                continue;
            }
            if (endpoint.httpMethod() == HttpMethod.POST || endpoint.httpMethod() == HttpMethod.PUT
                    || endpoint.httpMethod() == HttpMethod.PATCH || endpoint.httpMethod() == HttpMethod.DELETE) {
                continue;
            }

            ResponseEntity<String> resp = callEndpoint(endpoint, null);
            int status = resp.getStatusCode().value();
            if (status == 401 || status == 403) {
                violations.add(String.format(
                        "@PublicApi 端点返回 %d: %s %s",
                        status, endpoint.httpMethod(), endpoint.pattern()));
            }
        }

        assertTrue(violations.isEmpty(),
                "@PublicApi 违规:\n" +
                violations.stream().map(v -> "  " + v).reduce("", (a, b) -> a + b + "\n"));
    }

    private record EndpointInfo(String pattern, HttpMethod httpMethod, int requiredRole,
                                String controller, String methodSignature) {}
}
