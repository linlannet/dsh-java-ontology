package net.linlan.dsh.ontology.engine.registry;

import net.linlan.dsh.ontology.engine.model.AggregateDescriptor;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** alias/category 双索引只读注册表；未知键返回 Optional.empty()。 */
public final class OntologyRegistry {

    private final Map<String, AggregateDescriptor> byAlias;
    private final Map<String, AggregateDescriptor> byCategory;
    private final List<AggregateDescriptor> all;

    private OntologyRegistry(Map<String, AggregateDescriptor> byAlias,
                             Map<String, AggregateDescriptor> byCategory,
                             List<AggregateDescriptor> all) {
        this.byAlias = Map.copyOf(byAlias);
        this.byCategory = Map.copyOf(byCategory);
        this.all = List.copyOf(all);
    }

    public static OntologyRegistry from(Collection<AggregateDescriptor> descriptors) {
        Map<String, AggregateDescriptor> aliases = new LinkedHashMap<>();
        Map<String, AggregateDescriptor> categories = new LinkedHashMap<>();
        for (AggregateDescriptor descriptor : descriptors) {
            aliases.put(descriptor.alias(), descriptor);
            if (descriptor.category() != null) {
                categories.put(descriptor.category(), descriptor);
            }
            registerChildren(descriptor, aliases, categories);
        }
        return new OntologyRegistry(aliases, categories, List.copyOf(descriptors));
    }

    private static void registerChildren(AggregateDescriptor parent,
                                         Map<String, AggregateDescriptor> aliases,
                                         Map<String, AggregateDescriptor> categories) {
        for (AggregateDescriptor child : parent.subEntities()) {
            aliases.put(child.alias(), child);
            if (child.category() != null) {
                categories.put(child.category(), child);
            }
            registerChildren(child, aliases, categories);
        }
    }

    public Optional<AggregateDescriptor> byAlias(String alias) {
        return Optional.ofNullable(aliasIndex().get(alias));
    }

    public Optional<AggregateDescriptor> byCategory(String category) {
        return Optional.ofNullable(categoryIndex().get(category));
    }

    public Set<String> categories() {
        return categoryIndex().keySet();
    }

    public List<AggregateDescriptor> byTag(String tag) {
        return all.stream().filter(d -> d.tags().contains(tag)).toList();
    }

    public List<AggregateDescriptor> all() {
        return all;
    }

    public int size() {
        return all.size();
    }

    private Map<String, AggregateDescriptor> aliasIndex() {
        return byAlias;
    }

    private Map<String, AggregateDescriptor> categoryIndex() {
        return byCategory;
    }
}
