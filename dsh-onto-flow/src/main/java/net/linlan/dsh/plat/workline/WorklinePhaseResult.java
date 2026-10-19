package net.linlan.dsh.plat.workline;

import java.time.LocalDateTime;
import java.util.Map;

public class WorklinePhaseResult {

    private final WorklinePhase phase;
    private final boolean success;
    private final String message;
    private final Map<String, Object> output;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final long durationMs;

    public WorklinePhaseResult(WorklinePhase phase, boolean success, String message,
                               Map<String, Object> output,
                               LocalDateTime startTime, LocalDateTime endTime) {
        this.phase = phase;
        this.success = success;
        this.message = message;
        this.output = output;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMs = java.time.Duration.between(startTime, endTime).toMillis();
    }

    public static WorklinePhaseResult success(WorklinePhase phase, String message,
                                               Map<String, Object> output,
                                               LocalDateTime start, LocalDateTime end) {
        return new WorklinePhaseResult(phase, true, message, output, start, end);
    }

    public static WorklinePhaseResult failure(WorklinePhase phase, String message,
                                              LocalDateTime start, LocalDateTime end) {
        return new WorklinePhaseResult(phase, false, message, null, start, end);
    }

    public WorklinePhase getPhase() {
        return phase;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, Object> getOutput() {
        return output;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public long getDurationMs() {
        return durationMs;
    }
}
