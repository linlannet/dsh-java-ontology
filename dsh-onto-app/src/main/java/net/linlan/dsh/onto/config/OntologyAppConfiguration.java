package net.linlan.dsh.onto.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 应用层配置
 * <p>参考 dsh-java-mysql-app 模式：仅负责激活应用级属性绑定。
 * <p>注意：{@code dsh.ontology.*} 业务属性由 dsh-onto-flow 的
 * {@code OntologyAdminProperties} 绑定，应用层使用独立前缀 {@code dsh.app.*}，避免重复注册。
 * 类名使用 OntologyAppConfiguration 以避免与 flow 层
 * {@code net.linlan.dsh.plat.config.ApplicationConfiguration} 的默认 bean 名冲突。</p>
 * @author Linlan
 * CreatedAt 2026-10-19
 * @version 1.0
 * @since 1.0
 */
@Configuration
@EnableConfigurationProperties(OntologyAppProperties.class)
public class OntologyAppConfiguration {
}
