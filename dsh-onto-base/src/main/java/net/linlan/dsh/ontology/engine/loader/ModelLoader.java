package net.linlan.dsh.ontology.engine.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.linlan.dsh.ontology.engine.catalog.TypeCatalog;
import net.linlan.dsh.ontology.engine.model.AggregateDescriptor;
import net.linlan.dsh.ontology.engine.model.AttributeDescriptor;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.MarkedYAMLException;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * 读取两层 manifest 与 7+7 个 YAML，产出不可变 EngineSnapshot。
 * 只负责解析与结构转换；校验由 ModelIntegrityChecker 承担。
 */
public class ModelLoader {

    public static final String BASE_MANIFEST = "ontology-models/manifest.json";
    public static final String PROJECT_MANIFEST = "siis-models/manifest.json";

    private final ClassLoader classLoader;
    private final Function<String, InputStream> resourceProvider;
    private final ObjectMapper objectMapper;
    private final TypeCatalog typeCatalog;
    private final Yaml yaml;

    public ModelLoader(ClassLoader classLoader, ObjectMapper objectMapper, TypeCatalog typeCatalog) {
        this(name -> classLoader.getResourceAsStream(name), objectMapper, typeCatalog);
    }

    public ModelLoader(ClassLoader classLoader, ObjectMapper objectMapper) {
        this(classLoader, objectMapper, new TypeCatalog());
    }

    public ModelLoader(Function<String, InputStream> resourceProvider,
                       ObjectMapper objectMapper,
                       TypeCatalog typeCatalog) {
        this.classLoader = null;
        this.resourceProvider = resourceProvider;
        this.objectMapper = objectMapper;
        this.typeCatalog = typeCatalog;
        LoaderOptions options = new LoaderOptions();
        this.yaml = new Yaml(new SafeConstructor(options));
    }

    public EngineSnapshot load() {
        return load(resourceProvider);
    }

    /** 包级可见：允许测试用内存资源构造同一加载流程。 */
    EngineSnapshot load(Function<String, InputStream> resources) {
        List<ModelManifest> manifests = new ArrayList<>();
        List<Map<String, Object>> rawDocuments = new ArrayList<>();
        Set<String> sourceFiles = new LinkedHashSet<>();
        List<AggregateDescriptor> descriptors = new ArrayList<>();
        Map<String, String> aliasToFile = new LinkedHashMap<>();

        ModelManifest base = readManifest(BASE_MANIFEST, resources);
        if (base.baseModels().isEmpty()) {
            throw new ModelLoadException("基座清单 base_models 为空", BASE_MANIFEST, "base_models", -1);
        }
        manifests.add(base);
        loadModels("ontology-models/", base.baseModels(), resources, rawDocuments, sourceFiles, descriptors, aliasToFile);

        boolean projectPresent = resourceExists(PROJECT_MANIFEST, resources);
        if (projectPresent) {
            ModelManifest project = readManifest(PROJECT_MANIFEST, resources);
            List<String> models = stringList(projectDocument(PROJECT_MANIFEST, resources).get("models"));
            if (models.isEmpty()) {
                throw new ModelLoadException("项目清单 models 为空", PROJECT_MANIFEST, "models", -1);
            }
            manifests.add(project);
            loadModels("siis-models/", models, resources, rawDocuments, sourceFiles, descriptors, aliasToFile);
        }

        return new EngineSnapshot(descriptors, manifests, rawDocuments, sourceFiles, projectPresent);
    }

    private void loadModels(String prefix,
                            List<String> models,
                            Function<String, InputStream> resources,
                            List<Map<String, Object>> rawDocuments,
                            Set<String> sourceFiles,
                            List<AggregateDescriptor> descriptors,
                            Map<String, String> aliasToFile) {
        for (String model : models) {
            String file = prefix + model;
            detectBareBlocks(file, resources);
            Map<String, Object> doc = readDocument(file, resources);
            requireHeader(file, doc);
            rawDocuments.add(doc);
            sourceFiles.add(file);
            List<Map<String, Object>> entities = entitiesOf(file, doc);
            for (int i = 0; i < entities.size(); i++) {
                AggregateDescriptor descriptor = toAggregate(entities.get(i), file, "aggregates[" + i + "]");
                registerAlias(descriptor, file, aliasToFile);
                descriptors.add(descriptor);
            }
        }
    }

    private void requireHeader(String file, Map<String, Object> doc) {
        if (doc == null || doc.get("model_type") == null || doc.get("version") == null) {
            throw new ModelLoadException("YAML 根节点缺少 model_type/version", file, "$", -1);
        }
    }

    private List<Map<String, Object>> entitiesOf(String file, Map<String, Object> doc) {
        Object entities = doc.get("entities");
        if (entities == null) {
            entities = doc.get("aggregates");
        }
        if (entities == null) {
            if ("OBJECT".equals(doc.get("model_type"))) {
                throw new ModelLoadException("对象模型缺少 entities/aggregates 节点", file, "$", -1);
            }
            return List.of();
        }
        if (!(entities instanceof List<?> list)) {
            throw new ModelLoadException("entities 节点必须是列表", file, "entities", -1);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> typed = (Map<String, Object>) map;
                result.add(typed);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private AggregateDescriptor toAggregate(Map<String, Object> node, String file, String path) {
        Object aliasValue = node.get("alias");
        if (aliasValue == null) {
            throw new ModelLoadException("实体缺少 alias", file, path, -1);
        }
        String alias = String.valueOf(aliasValue);
        String category = node.get("category") == null
                ? typeCatalog.typeCodeOfAlias(alias).orElse(null)
                : String.valueOf(node.get("category"));

        List<AttributeDescriptor> attributes = new ArrayList<>();
        Object rawAttributes = node.get("attributes");
        if (rawAttributes instanceof List<?> list) {
            for (int i = 0; i < list.size(); i++) {
                Object item = list.get(i);
                if (!(item instanceof Map<?, ?> map)) {
                    continue;
                }
                attributes.add(toAttribute((Map<String, Object>) map, file,
                        path + ".attributes[" + i + "]"));
            }
        }

        List<AggregateDescriptor> children = new ArrayList<>();
        Object rawChildren = node.get("entities");
        if (rawChildren instanceof List<?> list) {
            for (int i = 0; i < list.size(); i++) {
                Object item = list.get(i);
                if (item instanceof Map<?, ?> map) {
                    children.add(toAggregate((Map<String, Object>) map, file,
                            path + ".entities[" + i + "]"));
                }
            }
        }

        return new AggregateDescriptor(
                stringOrNull(node.get("id")),
                stringOrNull(node.get("name")),
                alias,
                category,
                stringList(node.get("lifecycle")),
                stringList(node.get("tags")),
                attributes,
                children,
                file);
    }

    private AttributeDescriptor toAttribute(Map<String, Object> node, String file, String path) {
        return new AttributeDescriptor(
                String.valueOf(node.get("name")),
                stringOrNull(node.get("label")),
                stringOrNull(node.get("type")),
                Boolean.TRUE.equals(node.get("required")),
                Boolean.TRUE.equals(node.get("unique")),
                stringList(node.get("enumValues")),
                stringOrNull(node.containsKey("defaultValue") ? node.get("defaultValue") : node.get("default")),
                stringOrNull(node.get("description")),
                file + ":" + path);
    }

    private void registerAlias(AggregateDescriptor descriptor, String file, Map<String, String> aliasToFile) {
        String previous = aliasToFile.putIfAbsent(descriptor.alias(), file);
        if (previous != null) {
            throw new ModelLoadException("重复 alias: " + descriptor.alias()
                    + "（已定义于 " + previous + "）", file, descriptor.alias(), -1);
        }
        for (AggregateDescriptor child : descriptor.subEntities()) {
            registerAlias(child, file, aliasToFile);
        }
    }

    private ModelManifest readManifest(String file, Function<String, InputStream> resources) {
        Map<String, Object> doc = projectDocument(file, resources);
        String modelType = stringOrNull(doc.get("model_type"));
        if (modelType == null) {
            throw new ModelLoadException("清单缺少 model_type", file, "$", -1);
        }
        String version = stringOrNull(doc.get("version"));
        if (version == null) {
            throw new ModelLoadException("清单缺少 version", file, "$", -1);
        }
        List<Map<String, Object>> projects = new ArrayList<>();
        Object rawProjects = doc.get("projects");
        if (rawProjects instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> typed = (Map<String, Object>) map;
                    projects.add(typed);
                }
            }
        }
        return new ModelManifest(
                modelType,
                version,
                stringOrNull(doc.get("platform")),
                stringOrNull(doc.get("name")),
                stringOrNull(doc.get("description")),
                stringList(doc.get("base_models")),
                projects,
                stringOrNull(doc.get("base_manifest")),
                file);
    }

    private Map<String, Object> projectDocument(String file, Function<String, InputStream> resources) {
        try (InputStream in = resources.apply(file)) {
            if (in == null) {
                throw new ModelLoadException("classpath 资源缺失", file, "$", -1);
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> doc = objectMapper.readValue(in, Map.class);
            return doc;
        } catch (ModelLoadException e) {
            throw e;
        } catch (Exception e) {
            throw new ModelLoadException("清单解析失败: " + e.getMessage(), file, "$", -1, e);
        }
    }

    private Map<String, Object> readDocument(String file, Function<String, InputStream> resources) {
        try (InputStream in = resources.apply(file)) {
            if (in == null) {
                throw new ModelLoadException("classpath 资源缺失", file, "$", -1);
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> doc = yaml.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            if (doc == null) {
                throw new ModelLoadException("YAML 解析结果为空", file, "$", -1);
            }
            return doc;
        } catch (MarkedYAMLException e) {
            int line = e.getProblemMark() == null ? -1 : e.getProblemMark().getLine() + 1;
            throw new ModelLoadException("YAML 语法错误: " + e.getProblem(),
                    file, "$", line, e);
        } catch (ModelLoadException e) {
            throw e;
        } catch (Exception e) {
            throw new ModelLoadException("YAML 解析失败: " + e.getMessage(), file, "$", -1, e);
        }
    }

    private void detectBareBlocks(String file, Function<String, InputStream> resources) {
        try (InputStream in = resources.apply(file)) {
            if (in == null) {
                throw new ModelLoadException("classpath 资源缺失", file, "$", -1);
            }
            Node root = yaml.compose(new InputStreamReader(in, StandardCharsets.UTF_8));
            if (!(root instanceof MappingNode mapping)) {
                return;
            }
            for (NodeTuple tuple : mapping.getValue()) {
                if (!(tuple.getKeyNode() instanceof ScalarNode key)
                        || !(tuple.getValueNode() instanceof SequenceNode sequence)) {
                    continue;
                }
                for (Node item : sequence.getValue()) {
                    if (!(item instanceof MappingNode node)) {
                        continue;
                    }
                    Map<String, Boolean> keys = new LinkedHashMap<>();
                    for (NodeTuple nodeTuple : node.getValue()) {
                        if (nodeTuple.getKeyNode() instanceof ScalarNode scalar) {
                            keys.put(scalar.getValue(), Boolean.TRUE);
                        }
                    }
                    if (!keys.containsKey("alias") && keys.containsKey("name") && keys.containsKey("type")) {
                        throw new ModelLoadException("裸属性块：缺少父实体 alias",
                                file, key.getValue(), item.getStartMark().getLine() + 1);
                    }
                }
            }
        } catch (MarkedYAMLException e) {
            int line = e.getProblemMark() == null ? -1 : e.getProblemMark().getLine() + 1;
            throw new ModelLoadException("YAML 语法错误: " + e.getProblem(),
                    file, "$", line, e);
        } catch (ModelLoadException e) {
            throw e;
        } catch (Exception e) {
            throw new ModelLoadException("YAML 结构检查失败: " + e.getMessage(), file, "$", -1, e);
        }
    }

    private boolean resourceExists(String file, Function<String, InputStream> resources) {
        try (InputStream in = resources.apply(file)) {
            return in != null;
        } catch (Exception e) {
            return false;
        }
    }

    private static List<String> stringList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (Object item : list) {
            if (item != null) {
                result.add(String.valueOf(item));
            }
        }
        return result;
    }

    private static String stringOrNull(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
