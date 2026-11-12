package net.linlan.dsh.plat.controller;

import net.linlan.dsh.DshJavaOntologyApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 0.2.2 T6.5：模型 API 200/401/403/404 与响应脱敏。 */
@SpringBootTest(classes = DshJavaOntologyApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ModelControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl;
    private String researcherToken;
    private String guestToken;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        researcherToken = login("researcher", "password123");
        guestToken = login("guest", "password123");
    }

    @SuppressWarnings("unchecked")
    private String login(String username, String password) {
        Map<String, String> body = Map.of("userName", username, "password", password);
        ResponseEntity<Map> response = restTemplate.postForEntity(baseUrl + "/api/auth/login", body, Map.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        return "Bearer " + data.get("token");
    }

    private HttpHeaders headers(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            headers.set("Authorization", token);
        }
        return headers;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> data(ResponseEntity<Map> response) {
        return (Map<String, Object>) response.getBody().get("data");
    }

    @Test
    @SuppressWarnings("unchecked")
    void manifest_returns_counts_without_absolute_paths() {
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/api/model/manifest", HttpMethod.GET,
                new HttpEntity<>(headers(researcherToken)), Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> data = data(response);
        assertEquals(25, data.get("aggregateCount"));
        assertEquals(200, data.get("attributeCount"));
        assertEquals(16, data.get("enumCount"));
        assertEquals(false, data.get("projectLayerPresent"));
        assertFalse(String.valueOf(response.getBody()).contains("/Users/"));
        assertFalse(String.valueOf(response.getBody()).contains("file:"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void aggregates_are_grouped_and_contain_country() {
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/api/model/aggregates", HttpMethod.GET,
                new HttpEntity<>(headers(researcherToken)), Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<Map<String, Object>> groups = (List<Map<String, Object>>) response.getBody().get("data");
        assertFalse(groups.isEmpty(), "域分组不应为空");
        assertTrue(groups.stream().anyMatch(group -> group.get("tag") != null));
        assertTrue(groups.stream()
                        .flatMap(group -> ((List<Map<String, Object>>) group.get("aggregates")).stream())
                        .anyMatch(item -> "OntologyCountry".equals(item.get("alias"))),
                "本体域分组应包含 OntologyCountry");
    }

    @Test
    @SuppressWarnings("unchecked")
    void aggregate_detail_returns_schema() {
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/api/model/aggregates/OntologyCountry", HttpMethod.GET,
                new HttpEntity<>(headers(researcherToken)), Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> data = data(response);
        assertEquals("country", data.get("category"));
        assertNotNull(data.get("attributes"));
        assertFalse(((List<?>) data.get("attributes")).isEmpty());
        assertFalse(String.valueOf(response.getBody()).contains("/Users/"));
    }

    @Test
    void unknown_alias_returns_404() {
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/api/model/aggregates/NotAnAggregate", HttpMethod.GET,
                new HttpEntity<>(headers(researcherToken)), Map.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().get("code"));
    }

    @Test
    void missing_token_returns_401() {
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/model/manifest", HttpMethod.GET,
                new HttpEntity<>(headers(null)), String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void guest_role_returns_403() {
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/model/manifest", HttpMethod.GET,
                new HttpEntity<>(headers(guestToken)), String.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
}
