package net.linlan.dsh.onto.model;

import java.util.List;

/**
 * 聚合应用元信息
 * @author Linlan
 * CreatedAt 2026-10-19
 * @version 1.0
 * @since 1.0
 */
public record AppInfo(
        String name,
        String version,
        String description,
        List<String> profiles,
        List<String> modules
) {
}
