package net.linlan.dsh.sys.controller;

import net.linlan.dsh.sys.security.RequiresRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * SSE 流式排障控制器
 * <p>P2-18：提供 Server-Sent Events 端点，实时推送系统状态用于排障。
 * @author Linlan
 * CreatedAt 2026-10-18
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/debug")
public class SseDebugController {

    private static final Logger log = LoggerFactory.getLogger(SseDebugController.class);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @RequiresRole(0)
    public SseEmitter stream() {
        SseEmitter emitter = new SseEmitter(60_000L);

        scheduler.scheduleAtFixedRate(() -> {
            try {
                long heapUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                int threads = Thread.activeCount();
                emitter.send(SseEmitter.event()
                        .name("stats")
                        .data(String.format("{\"heapMB\":%d,\"threads\":%d,\"time\":%d}",
                                heapUsed / 1024 / 1024, threads, System.currentTimeMillis())));
            } catch (Exception e) {
                log.debug("SSE send failed: {}", e.getMessage());
                emitter.completeWithError(e);
            }
        }, 0, 2, TimeUnit.SECONDS);

        emitter.onCompletion(() -> log.info("SSE stream completed"));
        emitter.onTimeout(() -> log.info("SSE stream timeout"));
        return emitter;
    }
}
