package net.linlan.dsh.ontology.plugin;

import net.linlan.dsh.domain.model.entity.ToolExecutionResult;
import net.linlan.dsh.domain.model.entity.ToolRunContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 建模预览工具（阶段一，只读）：读取平台模型快照，生成表设计 DDL 与 draw.io ER 图，
 * 返回一个一次性确认令牌。<b>不写数据库。</b>
 *
 * @author Linlan
 * CreatedAt 2026-11-18
 * @version 1.0
 */
public class ModelPreviewTool extends BaseOntologyTool {

    public ModelPreviewTool(AdminApiClient client) { super(client); }

    @Override public String name() { return "model_preview"; }

    @Override public String description() {
        return "【建模第一步·不写库】根据本体模型生成表设计：返回每张表的 DDL、draw.io ER 图和一次性确认令牌。"
                + "用户审阅设计后，须用确认令牌与确认短语调用 model_confirm 才会真正建表。"
                + "参数 aliases 指定聚合根（如 SysUser），all=true 表示全部模型。";
    }

    @Override public Map<String, Object> parameters() {
        return objectSchema()
                .prop("aliases", objectSchema().build())
                .prop("all", booleanSchema("true 表示覆盖全部模型聚合根；默认 false"))
                .prop("adminBaseUrl", stringSchema("可选，Admin API 地址，默认 http://127.0.0.1:8100"))
                .prop("authToken", stringSchema("可选，JWT token"))
                .build();
    }

    @Override public boolean isConcurrencySafe(Map<String, Object> args) { return true; }

    @SuppressWarnings("unchecked")
    @Override
    protected CompletableFuture<ToolExecutionResult> run(Map<String, Object> args, ToolRunContext ctx) {
        boolean all = bool(args, "all", false);
        List<String> aliases = new ArrayList<>();
        Object raw = args.get("aliases");
        if (raw instanceof List<?> list) {
            for (Object item : list) {
                if (item != null) aliases.add(String.valueOf(item));
            }
        }
        if (!all && aliases.isEmpty()) {
            return fail("请提供 aliases（聚合根列表）或设置 all=true", "INVALID_ARGS");
        }
        StringBuilder body = new StringBuilder("{\"all\":").append(all).append(",\"aliases\":[");
        for (int i = 0; i < aliases.size(); i++) {
            if (i > 0) body.append(',');
            body.append('"').append(escapeJson(aliases.get(i))).append('"');
        }
        body.append("]}");
        return okPost("/api/model/materialize/preview", body.toString(), args);
    }

    private static boolean bool(Map<String, Object> args, String key, boolean defaultValue) {
        Object v = args.get(key);
        if (v == null) return defaultValue;
        if (v instanceof Boolean b) return b;
        return Boolean.parseBoolean(String.valueOf(v));
    }

    private static String escapeJson(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
