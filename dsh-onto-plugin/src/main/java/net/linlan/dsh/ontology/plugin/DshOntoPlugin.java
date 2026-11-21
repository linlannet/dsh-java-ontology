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
    public static final String PLUGIN_ID = "dsh-onto-plugin";

    private final AdminApiClient client = new AdminApiClient();

    public DshOntoPlugin() {
        super(PLUGIN_ID);
    }

    @Override
    public List<ToolDefinition> tools() {
        return List.of(
                new WorkspaceOverviewTool(client),
                new KnowledgeAggregateTool(client),
                new DirectorOverviewTool(client),
                new ModelPreviewTool(client),
                new ModelConfirmTool(client)
        );
    }

    @Override
    public void configure(PluginContext context) {
        super.configure(context);
        context.registerSystemPrompt("dsh-onto-plugin-capabilities", 20, """
                ## DSH Onto Plugin v0.2.2
                - 统一工作台工具：workspace_overview 查询工作空间概览。
                - 全局汇聚工具：knowledge_aggregate 触发知识汇聚（全量/增量/日志/统一视图）。
                - 主任决策工具：director_overview 查询主任决策概览。
                - 建模两步工具：model_preview（只读，生成表设计 DDL/drawio 图/确认令牌，绝不写库）→ model_confirm（用户明确说"确认建表"后才调用，需 token + 短语 CONFIRM_CREATE_TABLE + 一致的 aliases，校验通过才真正建表）。
                - 建模铁律：未经用户明确"确认建表"指令，禁止调用 model_confirm；必须先 model_preview 并把表设计展示给用户审阅。
                - 主任工具（knowledge_aggregate/director_overview/model_confirm）需要主任角色权限。
                """);
        context.registerHook("PRE_TOOL_USE", (toolName, payloadJson) -> {
            if (toolName != null && toolName.startsWith("plugin__" + PLUGIN_ID + "__")) {
                return PluginHookResult.context("DSH Onto audit: tool call " + toolName);
            }
            return null;
        });
    }
}