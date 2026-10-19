package net.linlan.dsh.plat.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dsh.ontology")
public record OntologyAdminProperties(
        String dataDirectory,
        int queryTimeoutSeconds,
        int maxResults,
        Harness harness
) {
    public record Harness(String baseUrl) {
    }
}
