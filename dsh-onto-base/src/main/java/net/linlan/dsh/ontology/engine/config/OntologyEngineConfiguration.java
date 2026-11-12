package net.linlan.dsh.ontology.engine.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.linlan.dsh.ontology.engine.OntologyModelEngine;
import net.linlan.dsh.ontology.engine.catalog.TypeCatalog;
import net.linlan.dsh.ontology.engine.loader.EngineSnapshot;
import net.linlan.dsh.ontology.engine.loader.ModelLoader;
import net.linlan.dsh.ontology.engine.registry.OntologyRegistry;
import net.linlan.dsh.ontology.engine.validation.IntegrityReport;
import net.linlan.dsh.ontology.engine.validation.ModelIntegrityChecker;
import net.linlan.dsh.ontology.engine.validation.ModelIntegrityException;
import net.linlan.dsh.ontology.engine.validation.SchemaValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/** 静态本体引擎装配：启动即加载，失败终止启动。 */
@Configuration
@ConditionalOnProperty(name = "dsh.ontology.model.enabled", havingValue = "true", matchIfMissing = true)
public class OntologyEngineConfiguration {

    private static final Logger log = LoggerFactory.getLogger(OntologyEngineConfiguration.class);

    @Bean
    public TypeCatalog typeCatalog() {
        return new TypeCatalog();
    }

    @Bean
    public ModelLoader modelLoader(ObjectMapper objectMapper, TypeCatalog typeCatalog) {
        return new ModelLoader(getClass().getClassLoader(), objectMapper, typeCatalog);
    }

    @Bean
    public OntologyModelEngine ontologyModelEngine(ModelLoader loader, TypeCatalog typeCatalog) {
        long start = System.currentTimeMillis();
        EngineSnapshot snapshot = loader.load();
        IntegrityReport report = new ModelIntegrityChecker().check(snapshot);
        long cost = System.currentTimeMillis() - start;
        log.info("本体模型加载完成：files={}, aggregates={}, attributes={}, enums={}, projectLayer={}, cost={}ms",
                snapshot.sourceFiles().size(), report.aggregateCount(), report.attributeCount(),
                report.enumCount(), snapshot.projectLayerPresent(), cost);
        report.warnings().forEach(issue ->
                log.warn("模型告警 {}: {} [{}]", issue.code(), issue.message(), issue.sourceFile()));
        if (report.hasErrors()) {
            throw new ModelIntegrityException(report);
        }
        return new OntologyModelEngine(snapshot, OntologyRegistry.from(snapshot.descriptors()), typeCatalog, report);
    }

    @Bean
    @Lazy
    public SchemaValidator schemaValidator(OntologyModelEngine engine,
                                           @Value("${dsh.ontology.schema-validation.enabled:true}") boolean enabled,
                                           @Value("${dsh.ontology.model.unknown-field-policy:warn}") String policy) {
        return new SchemaValidator(engine.registry(), enabled, policy);
    }
}
