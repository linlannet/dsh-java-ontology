package net.linlan.dsh.ontology.engine.loader;

import net.linlan.dsh.ontology.engine.model.AggregateDescriptor;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** 一次加载的完整快照：描述符、清单、原始文档、是否包含项目层。 */
public record EngineSnapshot(
        List<AggregateDescriptor> descriptors,
        List<ModelManifest> manifests,
        List<Map<String, Object>> rawDocuments,
        Set<String> sourceFiles,
        boolean projectLayerPresent) {

    public EngineSnapshot {
        descriptors = List.copyOf(descriptors);
        manifests = List.copyOf(manifests);
        rawDocuments = List.copyOf(rawDocuments);
        sourceFiles = Set.copyOf(sourceFiles);
    }
}
