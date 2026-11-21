package net.linlan.dsh.ontology.plugin;

import net.linlan.dsh.domain.model.entity.ToolExecutionResult;
import net.linlan.dsh.domain.model.entity.ToolRunContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 建表确认工具（阶段二，写库）：必须在用户明确下达"确认建表"指令后调用。
 * <p>服务端会校验：一次性令牌未过期 + 确认短语正确 + aliases 与预览一致 + 模型指纹未变，
 * 四重校验全部通过才执行 {@code CREATE TABLE IF NOT EXISTS}。</p>
 *
 * @author Linlan
 * CreatedAt 2026-11-18
 * @version 1.0
 */
public class ModelConfirmTool extends BaseOntologyTool {

    /** 与服务端 ModelMaterializeService.CONFIRM_PHRASE 保持一致。 */
    public static final String CONFIRM_PHRASE = "CONFIRM_CREATE_TABLE";

    public ModelConfirmTool(AdminApiClient client) { super(client); }

    @Override public String name() { return "model_confirm"; }

    @Override public String description() {
        return "【建模第二步·真正建表】仅在用户明确说\"确认建表\"后调用。必须传入 model_preview 返回的一次性 token、"
                + "确认短语 CONFIRM_CREATE_TABLE，以及与该次预览一致的 aliases。"
                + "四重校验通过后才在数据库创建表；任何一步不符都不会建表。";
    }

    @Override public Map<String, Object> parameters() {
        return objectSchema()
                .prop("token", stringSchema("必填：model_preview 返回的一次性确认令牌"))
                .prop("confirmPhrase", enumSchema("必填：固定为 CONFIRM_CREATE_TABLE", List.of(CONFIRM_PHRASE)))
                .prop("aliases", objectSchema().build())
                .prop("adminBaseUrl", stringSchema("可选，Admin API 地址"))
                .prop("authToken", stringSchema("可选，JWT token"))
                .required("token", "confirmPhrase")
                .build();
    }

    @Override public boolean isConcurrencySafe(Map<String, Object> args) { return false; }

    @SuppressWarnings("unchecked")
    @Override
    protected CompletableFuture<ToolExecutionResult> run(Map<String, Object> args, ToolRunContext ctx) {
        String token = str(args, "token");
        String phrase = str(args, "confirmPhrase");
        if (token.isEmpty()) {
            return fail("缺少 token；未执行建表", "INVALID_ARGS");
        }
        if (!CONFIRM_PHRASE.equals(phrase)) {
            return fail("确认短语必须为 " + CONFIRM_PHRASE + "；未执行建表", "INVALID_ARGS");
        }
        List<String> aliases = new ArrayList<>();
        Object raw = args.get("aliases");
        if (raw instanceof List<?> list) {
            for (Object item : list) {
                if (item != null) aliases.add(String.valueOf(item));
            }
        }
        if (aliases.isEmpty()) {
            return fail("缺少 aliases（须与预览时一致）；未执行建表", "INVALID_ARGS");
        }
        StringBuilder body = new StringBuilder("{\"token\":\"").append(escapeJson(token))
                .append("\",\"confirmPhrase\":\"").append(CONFIRM_PHRASE).append("\",\"aliases\":[");
        for (int i = 0; i < aliases.size(); i++) {
            if (i > 0) body.append(',');
            body.append('"').append(escapeJson(aliases.get(i))).append('"');
        }
        body.append("]}");
        return okPost("/api/model/materialize/confirm", body.toString(), args);
    }

    private static String escapeJson(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
