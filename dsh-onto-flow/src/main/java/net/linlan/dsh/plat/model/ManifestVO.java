package net.linlan.dsh.plat.model;

import java.util.List;

/** 模型清单视图：版本、文件与计数，不暴露绝对路径。 */
public record ManifestVO(
        String platform,
        String baseVersion,
        String projectVersion,
        List<String> baseModels,
        int projectModelCount,
        int aggregateCount,
        int attributeCount,
        int enumCount,
        boolean projectLayerPresent) {

    public ManifestVO {
        baseModels = baseModels == null ? List.of() : List.copyOf(baseModels);
    }
}
