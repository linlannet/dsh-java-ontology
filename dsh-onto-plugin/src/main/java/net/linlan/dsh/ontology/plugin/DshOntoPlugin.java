package net.linlan.dsh.ontology.plugin;

import net.linlan.dsh.domain.model.entity.ToolDefinition;
import net.linlan.dsh.domain.spi.AbstractHarnessPlugin;
import net.linlan.dsh.domain.spi.PluginContext;
import net.linlan.dsh.domain.spi.PluginHookResult;

import java.util.List;

/**
 * DSH 本体平台插件，注册 3 个统一工作台原子工具供 Agent 调用
 * <p>覆盖 workspace（统一工作台）、global（全局汇聚）、director（主任概览）</p>
 * @author Linlan
 * CreatedAt 2026-10-15
 * @version 2.0
 * @since 2.0
 */
public class DshOntoPlugin extends AbstractHarnessPlugin {
    public static final String PLUGIN_ID = "dsh-onto";

    private final AdminApiClient client = new AdminApiClient();

    public DshOntoPlugin() {
        super(PLUGIN_ID);
    }

    @Override
    public List<ToolDefinition> tools() {
        return List.of(
                new WorkspaceOverviewTool(client),
                new KnowledgeAggregateTool(client),
                new DirectorOverviewTool(client)
        );
    }

    @Override
    public void configure(PluginContext context) {
        super.configure(context);
        context.registerSystemPrompt("dsh-onto-capabilities", 20, """
                ## DSH Onto Plugin v2.0
                - 统一工作台工具：workspace_overview 查询工作空间概览。
                - 全局汇聚工具：knowledge_aggregate 触发知识汇聚（全量/增量/日志/统一视图）。
                - 主任决策工具：director_overview 查询主任决策概览。
                - 主任工具（knowledge_aggregate/director_overview）需要主任角色权限。
                """);
        context.registerHook("PRE_TOOL_USE", (toolName, payloadJson) -> {
            if (toolName != null && toolName.startsWith("plugin__" + PLUGIN_ID + "__")) {
                return PluginHookResult.context("DSH Onto audit: tool call " + toolName);
            }
            return null;
        });
    }
}