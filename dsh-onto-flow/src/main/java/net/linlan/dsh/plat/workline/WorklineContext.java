package net.linlan.dsh.plat.workline;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class WorklineContext {

    private final String worklineId;
    private final Long tenantId;
    private final Long userId;
    private final Map<String, Object> attributes = new HashMap<>();
    private WorklinePhase currentPhase;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;

    public WorklineContext(String worklineId, Long tenantId, Long userId) {
        this.worklineId = worklineId;
        this.tenantId = tenantId;
        this.userId = userId;
        this.status = "INITIALIZED";
        this.startTime = LocalDateTime.now();
    }

    public String getWorklineId() {
        return worklineId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public Long getUserId() {
        return userId;
    }

    public WorklinePhase getCurrentPhase() {
        return currentPhase;
    }

    public void setCurrentPhase(WorklinePhase currentPhase) {
        this.currentPhase = currentPhase;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
