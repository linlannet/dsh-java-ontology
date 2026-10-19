package net.linlan.dsh.plat.service;

import net.linlan.dsh.plat.config.OntologyAdminProperties;
import net.linlan.dsh.plat.model.ApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class HarnessAiStreamService {
    private final OntologyAdminProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ExecutorService executor = Executors.newCachedThreadPool(runnable -> {
        Thread thread = new Thread(runnable, "harness-ai-stream");
        thread.setDaemon(true);
        return thread;
    });

    public HarnessAiStreamService(OntologyAdminProperties properties) {
        this.properties = properties;
    }

    public SseEmitter stream(String agentId, String message) {
        SseEmitter emitter = new SseEmitter(300_000L);
        AtomicBoolean closed = new AtomicBoolean(false);
        emitter.onCompletion(() -> closed.set(true));
        emitter.onTimeout(emitter::complete);
        emitter.onError(error -> closed.set(true));
        executor.execute(() -> forward(agentId, message, emitter, closed));
        return emitter;
    }

    private void forward(String agentId, String message, SseEmitter emitter, AtomicBoolean closed) {
        try {
            String body = objectMapper.writeValueAsString(Map.of("agentId", agentId, "message", message));
            HttpRequest request = HttpRequest.newBuilder(URI.create(properties.harness().baseUrl() + "/api/agent/stream"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() / 100 != 2) {
                send(emitter, closed, "error", "{\"message\":\"Harness 返回 " + response.statusCode() + "\"}");
                emitter.complete();
                return;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                String event = "message";
                StringBuilder data = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("event:")) {
                        event = line.substring(6).trim();
                    } else if (line.startsWith("data:")) {
                        data.append(line.substring(5).trim()).append('\n');
                    } else if (line.isEmpty()) {
                        send(emitter, closed, event, data.toString().trim());
                        event = "message";
                        data.setLength(0);
                    }
                }
            }
            emitter.complete();
        } catch (Exception e) {
            try {
                send(emitter, closed, "error", "{\"message\":\"" + e.getMessage().replace("\"", "\\\"") + "\"}");
            } catch (Exception ignored) {
            }
            emitter.complete();
        }
    }

    private void send(SseEmitter emitter, AtomicBoolean closed, String event, String data) {
        if (closed.get() || data == null || data.isBlank()) return;
        try {
            emitter.send(SseEmitter.event().name(event).data(data));
        } catch (Exception e) {
            closed.set(true);
        }
    }
}
