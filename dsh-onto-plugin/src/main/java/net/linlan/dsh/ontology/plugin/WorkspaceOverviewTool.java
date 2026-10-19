package net.linlan.dsh.ontology.plugin;

import net.linlan.dsh.domain.model.entity.ToolExecutionResult;
import net.linlan.dsh.domain.model.entity.ToolRunContext;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 工作空间概览工具，查询研究员工作空间概览数据
 * @author Linlan
 * CreatedAt 2026-10-14
 * @version 1.0
 * @since 1.0
 */
public class WorkspaceOverviewTool extends BaseOntologyTool {

    public WorkspaceOverviewTool(AdminApiClient client) { super(client); }

    @Override public String name() { return "workspace_overview"; }
    @Override public String description() { return "查询研究员工作空间概览数据，包括研究概览、知识沉淀、收藏主题等。返回概览 JSON。"; }
    @Override public Map<String, Object> parameters() {
        return objectSchema()
                .prop("category", stringSchema("可选，分类过滤，如 researchOverview/knowledgePrecipitation"))
                .prop("pageNum", integerSchema("页码，默认 1"))
                .prop("pageSize", integerSchema("每页条数，默认 20"))
                .prop("adminBaseUrl", stringSchema("可选，Admin API 地址"))
                .prop("authToken", stringSchema("可选，JWT token"))
                .build();
    }
    @Override public boolean isConcurrencySafe(Object args) { return true; }
    @Override
    protected CompletableFuture<ToolExecutionResult> run(Map<String, Object> args, ToolRunContext ctx) {
        String category = str(args, "category", "researchOverview");
        int pageNum = intVal(args, "pageNum", 1);
        int pageSize = intVal(args, "pageSize", 20);
        String path = "/api/workspace/" + category + "/page?pageNum=" + pageNum + "&pageSize=" + pageSize;
        return okGet(path, args);
    }
}
