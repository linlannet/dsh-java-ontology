package net.linlan.dsh.ontology.plugin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * Admin API HTTP 客户端封装，支持 JWT 透传
 * <p>插件通过此客户端回调 app 的 Admin API（端口 8100）</p>
 * @author Linlan
 * CreatedAt 2026-10-14
 * @version 1.0
 * @since 1.0
 */
public class AdminApiClient {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    /**
     * 默认 Admin API 地址
     */
    public static final String DEFAULT_BASE_URL = "http://127.0.0.1:8100";

    /**
     * GET 请求
     */
    public String get(String path, Map<String, Object> args) {
        String adminUrl = baseUrl(args);
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(adminUrl + path)).GET();
        addAuthHeader(builder, args);
        return send(builder.build());
    }

    /**
     * POST 请求（JSON body）
     */
    public String post(String path, String json, Map<String, Object> args) {
        String adminUrl = baseUrl(args);
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(adminUrl + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json));
        addAuthHeader(builder, args);
        return send(builder.build());
    }

    /**
     * PUT 请求（JSON body）
     */
    public String put(String path, String json, Map<String, Object> args) {
        String adminUrl = baseUrl(args);
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(adminUrl + path))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json));
        addAuthHeader(builder, args);
        return send(builder.build());
    }

    /**
     * DELETE 请求
     */
    public String delete(String path, Map<String, Object> args) {
        String adminUrl = baseUrl(args);
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(adminUrl + path)).DELETE();
        addAuthHeader(builder, args);
        return send(builder.build());
    }

    private void addAuthHeader(HttpRequest.Builder builder, Map<String, Object> args) {
        String token = authToken(args);
        if (token != null && !token.isBlank()) {
            builder.header("Authorization", "Bearer " + token);
        }
    }

    private String baseUrl(Map<String, Object> args) {
        Object override = args == null ? null : args.get("adminBaseUrl");
        return override == null || String.valueOf(override).isBlank()
                ? System.getenv().getOrDefault("DSH_ONTOLOGY_ADMIN_URL", DEFAULT_BASE_URL)
                : String.valueOf(override);
    }

    private String authToken(Map<String, Object> args) {
        Object token = args == null ? null : args.get("authToken");
        if (token != null && !String.valueOf(token).isBlank()) {
            return String.valueOf(token);
        }
        return System.getenv().get("DSH_ONTOLOGY_AUTH_TOKEN");
    }

    private String send(HttpRequest request) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                return failJson(response.statusCode(), response.body());
            }
            return response.body();
        } catch (Exception e) {
            return failJson(0, e.getMessage());
        }
    }

    private String failJson(int status, String message) {
        return "{\"error\":true,\"status\":" + status + ",\"message\":\"" + escapeJson(message) + "\"}";
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    /**
     * 构建简单 JSON 键值对
     */
    public static String jsonPair(String key, String value) {
        return "\"" + key + "\":\"" + escapeJsonStatic(value) + "\"";
    }

    /**
     * 构建简单 JSON 对象
     */
    public static String jsonObject(String... pairs) {
        return "{" + String.join(",", pairs) + "}";
    }

    private static String escapeJsonStatic(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}
