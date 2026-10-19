package net.linlan.dsh.plat.service;

import net.linlan.dsh.plat.config.OntologyAdminProperties;
import net.linlan.dsh.plat.model.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class HarnessAiService {
    private final OntologyAdminProperties properties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public HarnessAiService(OntologyAdminProperties properties, RestClient.Builder builder) {
        this.properties = properties;
        this.restClient = builder.baseUrl(properties.harness().baseUrl()).build();
    }

    public String chat(String agentId, String message) {
        Map<String, Object> request = Map.of(
                "agentId", agentId,
                "message", message
        );
        try {
            String response = restClient.post()
                    .uri("/api/agent/message")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(String.class);
            JsonNode root = objectMapper.readTree(response == null ? "{}" : response);
            JsonNode messages = root.path("data").path("messages");
            for (int index = messages.size() - 1; index >= 0; index--) {
                JsonNode item = messages.get(index);
                if ("assistant".equals(item.path("role").asText())) {
                    String content = item.path("content").asText("");
                    if (!content.isBlank()) {
                        return content;
                    }
                }
            }
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Harness 未返回助手内容");
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Harness 对话失败：" + e.getMessage());
        }
    }
}
