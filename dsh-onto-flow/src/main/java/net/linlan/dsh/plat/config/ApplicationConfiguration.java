package net.linlan.dsh.plat.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OntologyAdminProperties.class)
public class ApplicationConfiguration {
}
