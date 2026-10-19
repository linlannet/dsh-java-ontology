package net.linlan.dsh.ontology.plugin;

import net.linlan.dsh.domain.model.entity.ToolExecutionResult;
import net.linlan.dsh.domain.model.entity.ToolRunContext;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 主任决策概览工具，查询全局知识统计和决策支持数据（仅主任）
 * @author Linlan
 * CreatedAt 2026-10-14
 * @version 1.0
 * @since 1.0
 */
public class DirectorOverviewTool extends BaseOntologyTool {

    public DirectorOverviewTool(AdminApiClient client) { super(client); }

    @Override public String name() { return "director_overview"; }
    @Override public String description() { return "查询主任决策概览，包括全局实体/关系/图谱统计、租户数、文章数、最近汇聚状态。仅主任可用。返回概览 JSON。"; }
    @Override public Map<String, Object> parameters() {
        return objectSchema()
                .prop("detail", enumSchema("查询详情", java.util.List.of("overview", "entities", "relations", "kgNodes", "kgEdges")))
                .prop("pageNum", integerSchema("页码，默认 1（分页查询时使用）"))
                .prop("pageSize", integerSchema("每页条数，默认 20（分页查询时使用）"))
                .prop("adminBaseUrl", stringSchema("可选，Admin API 地址"))
                .prop("authToken", stringSchema("可选，JWT token"))
                .build();
    }
    @Override public boolean isConcurrencySafe(Object args) { return true; }
    @Override
    protected CompletableFuture<ToolExecutionResult> run(Map<String, Object> args, ToolRunContext ctx) {
        String detail = str(args, "detail", "overview");
        int pageNum = intVal(args, "pageNum", 1);
        int pageSize = intVal(args, "pageSize", 20);
        return switch (detail) {
            case "overview" -> okGet("/api/global/director/overview", args);
            case "entities" -> okGet("/api/global/entities?pageNum=" + pageNum + "&pageSize=" + pageSize, args);
            case "relations" -> okGet("/api/global/relations?pageNum=" + pageNum + "&pageSize=" + pageSize, args);
            case "kgNodes" -> okGet("/api/global/kg/nodes?pageNum=" + pageNum + "&pageSize=" + pageSize, args);
            case "kgEdges" -> okGet("/api/global/kg/edges?pageNum=" + pageNum + "&pageSize=" + pageSize, args);
            default -> okGet("/api/global/director/overview", args);
        };
    }
}
