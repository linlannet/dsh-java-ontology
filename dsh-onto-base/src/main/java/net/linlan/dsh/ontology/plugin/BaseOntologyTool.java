package net.linlan.dsh.ontology.plugin;

import net.linlan.dsh.domain.model.entity.AbstractTool;
import net.linlan.dsh.domain.model.entity.ToolExecutionResult;
import net.linlan.dsh.domain.model.entity.ToolRunContext;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 本体插件工具基类，封装 AdminApiClient 和通用辅助方法
 * @author Linlan
 * CreatedAt 2026-10-14
 * @version 1.0
 * @since 1.0
 */
public abstract class BaseOntologyTool extends AbstractTool {

    protected final AdminApiClient client;

    protected BaseOntologyTool(AdminApiClient client) {
        this.client = client;
    }

    protected CompletableFuture<ToolExecutionResult> okGet(String path, Map<String, Object> args) {
        return ok(client.get(path, args));
    }

    protected CompletableFuture<ToolExecutionResult> okPost(String path, String json, Map<String, Object> args) {
        return ok(client.post(path, json, args));
    }

    protected CompletableFuture<ToolExecutionResult> okPut(String path, String json, Map<String, Object> args) {
        return ok(client.put(path, json, args));
    }

    protected CompletableFuture<ToolExecutionResult> okDelete(String path, Map<String, Object> args) {
        return ok(client.delete(path, args));
    }
}
