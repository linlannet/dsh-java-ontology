package net.linlan.dsh.ontology.plugin;

import net.linlan.dsh.domain.model.entity.ToolExecutionResult;
import net.linlan.dsh.domain.model.entity.ToolRunContext;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 知识汇聚工具，触发全量或增量知识汇聚（仅主任）
 * @author Linlan
 * CreatedAt 2026-10-14
 * @version 1.0
 * @since 1.0
 */
public class KnowledgeAggregateTool extends BaseOntologyTool {

    public KnowledgeAggregateTool(AdminApiClient client) { super(client); }

    @Override public String name() { return "knowledge_aggregate"; }
    @Override public String description() { return "触发知识汇聚（全量或增量），将各租户个人层知识汇聚到全局统一层。仅主任可用。返回汇聚统计 JSON。"; }
    @Override public Map<String, Object> parameters() {
        return objectSchema()
                .prop("action", enumSchema("操作类型", java.util.List.of("triggerFull", "triggerIncremental", "logs", "unifiedMonitor", "unifiedKg")))
                .prop("tenantId", stringSchema("租户 ID（triggerIncremental 时必填）"))
                .prop("pageNum", integerSchema("页码，默认 1（logs 时使用）"))
                .prop("pageSize", integerSchema("每页条数，默认 20（logs 时使用）"))
                .prop("adminBaseUrl", stringSchema("可选，Admin API 地址"))
                .prop("authToken", stringSchema("可选，JWT token"))
                .required("action")
                .build();
    }
    @Override public boolean isConcurrencySafe(Object args) { return false; }
    @Override
    protected CompletableFuture<ToolExecutionResult> run(Map<String, Object> args, ToolRunContext ctx) {
        String action = str(args, "action");
        return switch (action) {
            case "triggerFull" -> okPost("/api/aggregation/trigger", "{}", args);
            case "triggerIncremental" -> okPost("/api/aggregation/incremental/" + str(args, "tenantId"), "{}", args);
            case "logs" -> okGet("/api/aggregation/logs?pageNum=" + intVal(args, "pageNum", 1) + "&pageSize=" + intVal(args, "pageSize", 20), args);
            case "unifiedMonitor" -> okGet("/api/aggregation/unified-monitor", args);
            case "unifiedKg" -> okGet("/api/aggregation/unified-kg", args);
            default -> fail("unknown action: " + action, "INVALID_ARGS");
        };
    }
}
