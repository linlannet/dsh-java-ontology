package net.linlan.dsh.onto.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 应用级配置属性（前缀 dsh.app）
 * <p>与 flow 层的 {@code OntologyAdminProperties}（dsh.ontology.*）职责分离：
 * 本属性仅描述聚合应用自身的元信息，不承载业务配置。</p>
 * @author Linlan
 * CreatedAt 2026-10-19
 * @version 1.0
 * @since 1.0
 */
@ConfigurationProperties(prefix = "dsh.app")
public record OntologyAppProperties(
        String name,
        String version,
        String description
) {
}
