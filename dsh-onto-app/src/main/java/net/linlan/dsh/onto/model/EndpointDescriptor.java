package net.linlan.dsh.onto.model;

/**
 * 入口端点描述（按模块归属分组）
 * @author Linlan
 * CreatedAt 2026-10-19
 * @version 1.0
 * @since 1.0
 */
public record EndpointDescriptor(
        String module,
        String method,
        String path,
        String handler
) {
}
