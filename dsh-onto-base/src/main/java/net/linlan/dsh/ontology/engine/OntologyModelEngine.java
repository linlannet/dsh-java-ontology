package net.linlan.dsh.ontology.engine;

import net.linlan.dsh.ontology.engine.catalog.TypeCatalog;
import net.linlan.dsh.ontology.engine.loader.EngineSnapshot;
import net.linlan.dsh.ontology.engine.registry.OntologyRegistry;
import net.linlan.dsh.ontology.engine.validation.IntegrityReport;

/** 装配后的只读模型引擎：快照 + 注册表 + 类型目录 + 完整性报告。 */
public class OntologyModelEngine {

    private final EngineSnapshot snapshot;
    private final OntologyRegistry registry;
    private final TypeCatalog typeCatalog;
    private final IntegrityReport integrityReport;

    public OntologyModelEngine(EngineSnapshot snapshot,
                               OntologyRegistry registry,
                               TypeCatalog typeCatalog,
                               IntegrityReport integrityReport) {
        this.snapshot = snapshot;
        this.registry = registry;
        this.typeCatalog = typeCatalog;
        this.integrityReport = integrityReport;
    }

    public EngineSnapshot snapshot() {
        return snapshot;
    }

    public OntologyRegistry registry() {
        return registry;
    }

    public TypeCatalog typeCatalog() {
        return typeCatalog;
    }

    public IntegrityReport integrityReport() {
        return integrityReport;
    }
}
