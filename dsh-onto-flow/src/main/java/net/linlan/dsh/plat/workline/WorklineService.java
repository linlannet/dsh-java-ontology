package net.linlan.dsh.plat.workline;

import net.linlan.dsh.common.TenantContext;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 工作流服务层，管理工作流生命周期
 * @author Linlan
 * CreatedAt 2026-10-14
 * @version 1.0
 * @since 1.0
 */
@Service
public class WorklineService {

    private final WorklineOrchestrator orchestrator;

    public WorklineService(WorklineOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    /**
     * 激活主题，启动工作流
     */
    public WorklineContext activateTopic(Long topicId) {
        Long tenantId = TenantContext.getTenantId();
        Long userId = TenantContext.getUserId();
        String worklineId = "wl-" + UUID.randomUUID().toString().substring(0, 8);
        WorklineContext context = orchestrator.init(worklineId, tenantId, userId);
        context.setAttribute("topicId", topicId);
        orchestrator.executePhase(context, WorklinePhase.TOPIC_SETUP);
        return context;
    }

    /**
     * 获取工作流状态
     */
    public WorklineContext getStatus(String worklineId) {
        return orchestrator.getContext(worklineId);
    }

    /**
     * 获取工作流进度（各阶段执行结果）
     */
    public List<WorklinePhaseResult> getProgress(String worklineId) {
        return orchestrator.getResults(worklineId);
    }

    /**
     * 执行单个阶段
     */
    public WorklinePhaseResult executePhase(String worklineId, WorklinePhase phase) {
        WorklineContext context = orchestrator.getContext(worklineId);
        if (context == null) {
            throw new IllegalArgumentException("工作流不存在: " + worklineId);
        }
        return orchestrator.executePhase(context, phase);
    }

    /**
     * 执行到指定阶段
     */
    public List<WorklinePhaseResult> executeUntil(String worklineId, WorklinePhase targetPhase) {
        WorklineContext context = orchestrator.getContext(worklineId);
        if (context == null) {
            throw new IllegalArgumentException("工作流不存在: " + worklineId);
        }
        return orchestrator.executeUntil(context, targetPhase);
    }

    /**
     * 执行全部阶段
     */
    public List<WorklinePhaseResult> executeAll(String worklineId) {
        WorklineContext context = orchestrator.getContext(worklineId);
        if (context == null) {
            throw new IllegalArgumentException("工作流不存在: " + worklineId);
        }
        return orchestrator.executeAll(context);
    }

    /**
     * 获取七阶段定义
     */
    public List<Map<String, Object>> getStages() {
        return Arrays.stream(WorklinePhase.values())
                .map(phase -> Map.<String, Object>of(
                        "order", phase.getOrder(),
                        "name", phase.getName(),
                        "code", phase.name()
                ))
                .collect(Collectors.toList());
    }
}
