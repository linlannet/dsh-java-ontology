package net.linlan.dsh.plat.workline;

import net.linlan.dsh.common.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 工作流编排器，负责七阶段流水线的执行与状态管理
 * @author Linlan
 * CreatedAt 2026-10-14
 * @version 2.0
 * @since 1.0
 */
@Component
public class WorklineOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(WorklineOrchestrator.class);

    private final JdbcTemplate jdbcTemplate;
    private final ConcurrentHashMap<String, WorklineContext> contextStore = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<WorklinePhaseResult>> resultStore = new ConcurrentHashMap<>();

    public WorklineOrchestrator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public WorklineContext init(String worklineId, Long tenantId, Long userId) {
        log.info("Initializing workline: {} for tenant: {}, user: {}", worklineId, tenantId, userId);
        WorklineContext context = new WorklineContext(worklineId, tenantId, userId);
        contextStore.put(worklineId, context);
        return context;
    }

    public WorklineContext getContext(String worklineId) {
        return contextStore.get(worklineId);
    }

    public List<WorklinePhaseResult> getResults(String worklineId) {
        return resultStore.get(worklineId);
    }

    public WorklinePhaseResult executePhase(WorklineContext context, WorklinePhase phase) {
        log.info("Executing phase {} ({}) for workline {}", phase.getOrder(), phase.getName(), context.getWorklineId());
        context.setCurrentPhase(phase);
        context.setStatus("RUNNING_" + phase.name());

        LocalDateTime start = LocalDateTime.now();
        try {
            WorklinePhaseResult result = switch (phase) {
                case TOPIC_SETUP -> phase1TopicSetup(context);
                case COLLECTION_MONITORING -> phase2CollectionMonitoring(context);
                case NLP_ANALYSIS -> phase3NlpAnalysis(context);
                case KG_CONSTRUCTION -> phase4KgConstruction(context);
                case HOTSPOT_ANALYSIS -> phase5HotspotAnalysis(context);
                case REPORT_GENERATION -> phase6ReportGeneration(context);
                case PUSH_DISPLAY -> phase7PushDisplay(context);
            };
            log.info("Phase {} completed: success={}, duration={}ms", phase.getOrder(), result.isSuccess(), result.getDurationMs());
            storeResult(context.getWorklineId(), result);
            return result;
        } catch (Exception e) {
            LocalDateTime end = LocalDateTime.now();
            log.error("Phase {} failed", phase.getOrder(), e);
            WorklinePhaseResult failure = WorklinePhaseResult.failure(phase, e.getMessage(), start, end);
            storeResult(context.getWorklineId(), failure);
            return failure;
        }
    }

    public List<WorklinePhaseResult> executeAll(WorklineContext context) {
        context.setStatus("RUNNING");
        return java.util.Arrays.stream(WorklinePhase.values())
                .sorted(java.util.Comparator.comparingInt(WorklinePhase::getOrder))
                .map(phase -> executePhase(context, phase))
                .collect(Collectors.toList());
    }

    public List<WorklinePhaseResult> executeUntil(WorklineContext context, WorklinePhase targetPhase) {
        context.setStatus("RUNNING");
        return java.util.Arrays.stream(WorklinePhase.values())
                .sorted(java.util.Comparator.comparingInt(WorklinePhase::getOrder))
                .filter(p -> p.getOrder() <= targetPhase.getOrder())
                .map(phase -> executePhase(context, phase))
                .collect(Collectors.toList());
    }

    protected WorklinePhaseResult phase1TopicSetup(WorklineContext context) {
        LocalDateTime start = LocalDateTime.now();
        log.info("[Phase 1] Topic Setup for workline: {}", context.getWorklineId());
        Long topicId = context.getAttribute("topicId");
        int topicCount = 0;
        if (topicId != null) {
            topicCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM gwy_analyze_track_topic WHERE id = ? AND del_flag = 0",
                    Integer.class, topicId);
        }
        Map<String, Object> output = Map.of("topicId", topicId != null ? topicId : 0, "topicValid", topicCount > 0);
        LocalDateTime end = LocalDateTime.now();
        return WorklinePhaseResult.success(WorklinePhase.TOPIC_SETUP, "主题设定完成（topicId=" + topicId + "）", output, start, end);
    }

    protected WorklinePhaseResult phase2CollectionMonitoring(WorklineContext context) {
        LocalDateTime start = LocalDateTime.now();
        log.info("[Phase 2] Collection & Monitoring for workline: {}", context.getWorklineId());
        Long tenantId = context.getTenantId();
        Long articleCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM gwy_track_article WHERE tenant_id = ? AND del_flag = 0",
                Long.class, tenantId);
        Map<String, Object> output = Map.of("articleCount", articleCount);
        LocalDateTime end = LocalDateTime.now();
        return WorklinePhaseResult.success(WorklinePhase.COLLECTION_MONITORING, "采集监测完成（文章数=" + articleCount + "）", output, start, end);
    }

    protected WorklinePhaseResult phase3NlpAnalysis(WorklineContext context) {
        LocalDateTime start = LocalDateTime.now();
        log.info("[Phase 3] NLP Analysis for workline: {}", context.getWorklineId());
        Long tenantId = context.getTenantId();
        Long pendingCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM gwy_track_article WHERE tenant_id = ? AND nlp_status = 'PENDING' AND del_flag = 0",
                Long.class, tenantId);
        Map<String, Object> output = Map.of("pendingArticles", pendingCount);
        LocalDateTime end = LocalDateTime.now();
        return WorklinePhaseResult.success(WorklinePhase.NLP_ANALYSIS, "NLP分析完成（待处理=" + pendingCount + "）", output, start, end);
    }

    protected WorklinePhaseResult phase4KgConstruction(WorklineContext context) {
        LocalDateTime start = LocalDateTime.now();
        log.info("[Phase 4] Knowledge Graph Construction for workline: {}", context.getWorklineId());
        Long tenantId = context.getTenantId();
        Long nodeCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM gwy_track_kg_node WHERE tenant_id = ? AND del_flag = 0",
                Long.class, tenantId);
        Long edgeCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM gwy_track_kg_edge WHERE tenant_id = ? AND del_flag = 0",
                Long.class, tenantId);
        Map<String, Object> output = Map.of("kgNodes", nodeCount, "kgEdges", edgeCount);
        LocalDateTime end = LocalDateTime.now();
        return WorklinePhaseResult.success(WorklinePhase.KG_CONSTRUCTION, "知识图谱构建完成（节点=" + nodeCount + ", 边=" + edgeCount + "）", output, start, end);
    }

    protected WorklinePhaseResult phase5HotspotAnalysis(WorklineContext context) {
        LocalDateTime start = LocalDateTime.now();
        log.info("[Phase 5] Hotspot Analysis for workline: {}", context.getWorklineId());
        Long hotspotCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM gwy_ontology_hotspot WHERE del_flag = 0",
                Long.class);
        Map<String, Object> output = Map.of("hotspotCount", hotspotCount);
        LocalDateTime end = LocalDateTime.now();
        return WorklinePhaseResult.success(WorklinePhase.HOTSPOT_ANALYSIS, "热点分析完成（热点数=" + hotspotCount + "）", output, start, end);
    }

    protected WorklinePhaseResult phase6ReportGeneration(WorklineContext context) {
        LocalDateTime start = LocalDateTime.now();
        log.info("[Phase 6] Report Generation for workline: {}", context.getWorklineId());
        Long tenantId = context.getTenantId();
        Long reportCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM onto_report_instance WHERE tenant_id = ? AND del_flag = 0",
                Long.class, tenantId);
        Map<String, Object> output = Map.of("reportCount", reportCount);
        LocalDateTime end = LocalDateTime.now();
        return WorklinePhaseResult.success(WorklinePhase.REPORT_GENERATION, "报告生成完成（报告数=" + reportCount + "）", output, start, end);
    }

    protected WorklinePhaseResult phase7PushDisplay(WorklineContext context) {
        LocalDateTime start = LocalDateTime.now();
        log.info("[Phase 7] Push & Display for workline: {}", context.getWorklineId());
        Long tenantId = context.getTenantId();
        Long pushCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM gwy_display_push_record WHERE tenant_id = ? AND del_flag = 0",
                Long.class, tenantId);
        context.setStatus("COMPLETED");
        context.setEndTime(LocalDateTime.now());
        Map<String, Object> output = Map.of("pushCount", pushCount);
        LocalDateTime end = LocalDateTime.now();
        return WorklinePhaseResult.success(WorklinePhase.PUSH_DISPLAY, "推送展示完成（推送数=" + pushCount + "）", output, start, end);
    }

    private void storeResult(String worklineId, WorklinePhaseResult result) {
        resultStore.compute(worklineId, (k, v) -> {
            if (v == null) {
                List<WorklinePhaseResult> list = new java.util.ArrayList<>();
                list.add(result);
                return list;
            }
            v.add(result);
            return v;
        });
    }
}
