package net.linlan.dsh.ontology.engine.loader;

import java.util.List;
import java.util.Map;

/** 模型清单（基座或项目层）。 */
public record ModelManifest(
        String modelType,
        String version,
        String platform,
        String name,
        String description,
        List<String> baseModels,
        List<Map<String, Object>> projects,
        String baseManifest,
        String sourceFile) {

    public ModelManifest {
        baseModels = baseModels == null ? List.of() : List.copyOf(baseModels);
        projects = projects == null ? List.of() : List.copyOf(projects);
    }
}
