package net.linlan.dsh.plat.service;

import net.linlan.dsh.plat.config.OntologyAdminProperties;
import net.linlan.dsh.plat.model.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OntologyService {
    private final OntologyAdminProperties properties;
    private final Map<String, Map<String, Object>> ontologies = new ConcurrentHashMap<>();

    public OntologyService(OntologyAdminProperties properties) {
        this.properties = properties;
    }

    public List<Map<String, Object>> list() {
        return new ArrayList<>(ontologies.values());
    }

    public Map<String, Object> query(String ontologyId, String query) {
        Map<String, Object> ontology = ontologies.get(ontologyId);
        if (ontology == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "本体库不存在: " + ontologyId);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ontologyId", ontologyId);
        result.put("ontologyName", ontology.get("name"));
        result.put("query", query);
        result.put("concepts", List.of());
        result.put("relations", List.of());
        result.put("message", "查询已接收，本体引擎待接入");
        return result;
    }

    public List<Map<String, Object>> search(String keyword) {
        List<Map<String, Object>> matches = new ArrayList<>();
        for (Map<String, Object> ontology : ontologies.values()) {
            String name = String.valueOf(ontology.getOrDefault("name", ""));
            String description = String.valueOf(ontology.getOrDefault("description", ""));
            if (name.contains(keyword) || description.contains(keyword)) {
                matches.add(ontology);
            }
        }
        return matches;
    }

    public Map<String, Object> save(String id, String name, String description, String source) {
        String ontologyId = id == null || id.isBlank() ? java.util.UUID.randomUUID().toString() : id;
        Map<String, Object> ontology = new LinkedHashMap<>();
        ontology.put("id", ontologyId);
        ontology.put("name", name);
        ontology.put("description", description == null ? "" : description);
        ontology.put("source", source == null ? "" : source);
        ontologies.put(ontologyId, ontology);
        return ontology;
    }

    public void delete(String id) {
        if (ontologies.remove(id) == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "本体库不存在: " + id);
        }
    }
}
