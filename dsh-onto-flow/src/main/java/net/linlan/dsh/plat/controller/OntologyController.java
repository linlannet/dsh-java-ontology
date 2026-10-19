package net.linlan.dsh.plat.controller;

import net.linlan.dsh.plat.model.ApiException;
import net.linlan.dsh.plat.model.ErrorResponse;
import net.linlan.dsh.plat.model.OntologyQueryRequest;
import net.linlan.dsh.plat.service.HarnessAiService;
import net.linlan.dsh.plat.service.HarnessAiStreamService;
import net.linlan.dsh.plat.service.OntologyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import net.linlan.dsh.sys.security.RequiresRole;

@RestController
@RequestMapping("/api/ontologies")
public class OntologyController {
    private final OntologyService ontologyService;
    private final HarnessAiService harnessAiService;
    private final HarnessAiStreamService harnessAiStreamService;

    public OntologyController(OntologyService ontologyService,
                              HarnessAiService harnessAiService,
                              HarnessAiStreamService harnessAiStreamService) {
        this.ontologyService = ontologyService;
        this.harnessAiService = harnessAiService;
        this.harnessAiStreamService = harnessAiStreamService;
    }

    @GetMapping
    @RequiresRole(1)
    public List<Map<String, Object>> list() {
        return ontologyService.list();
    }

    @PostMapping
    @RequiresRole(1)
    public Map<String, Object> save(@RequestBody Map<String, String> request) {
        String id = request.getOrDefault("id", "");
        String name = request.getOrDefault("name", "");
        String description = request.getOrDefault("description", "");
        String source = request.getOrDefault("source", "");
        if (name.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "name 不能为空");
        }
        return ontologyService.save(id.isBlank() ? null : id, name, description, source);
    }

    @DeleteMapping("/{id}")
    @RequiresRole(1)
    public void delete(@PathVariable("id") String id) {
        ontologyService.delete(id);
    }

    @PostMapping("/query")
    @RequiresRole(1)
    public Map<String, Object> query(@Valid @RequestBody OntologyQueryRequest request) {
        return ontologyService.query(request.ontologyId(), request.query());
    }

    @PostMapping("/search")
    @RequiresRole(1)
    public List<Map<String, Object>> search(@RequestBody Map<String, String> request) {
        String keyword = request.getOrDefault("keyword", "");
        if (keyword.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "keyword 不能为空");
        }
        return ontologyService.search(keyword);
    }

    @PostMapping("/ai")
    @RequiresRole(1)
    public Map<String, Object> ai(@RequestBody Map<String, String> request) {
        String agentId = request.getOrDefault("agentId", "");
        String message = request.getOrDefault("message", "");
        String ontologyId = request.getOrDefault("ontologyId", "");
        if (agentId.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "agentId 不能为空");
        }
        if (message.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "message 不能为空");
        }
        return Map.of("answer", harnessAiService.chat(agentId, buildPrompt(ontologyId, message)));
    }

    @PostMapping(value = "/ai/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @RequiresRole(1)
    public SseEmitter stream(@RequestBody Map<String, String> request) {
        String agentId = request.getOrDefault("agentId", "");
        String message = request.getOrDefault("message", "");
        String ontologyId = request.getOrDefault("ontologyId", "");
        if (agentId.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "agentId 不能为空");
        }
        if (message.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "message 不能为空");
        }
        return harnessAiStreamService.stream(agentId, buildPrompt(ontologyId, message));
    }

    private String buildPrompt(String ontologyId, String message) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("当前环境：\n");
        if (!ontologyId.isBlank()) {
            prompt.append("- 本体库 ID：").append(ontologyId).append("\n");
        }
        prompt.append("- 你可以使用以下只读本体检索工具获取真实数据（回答涉及概念定义、层级关系、实体属性的问题时，必须先调用工具查证，禁止凭空猜测）：\n");
        prompt.append("  - plugin__dsh-java-ontology-plugin__ontology_list()：列出可用本体库\n");
        prompt.append("  - plugin__dsh-java-ontology-plugin__ontology_query(ontologyId, query)：查询概念与关系\n");
        prompt.append("  - plugin__dsh-java-ontology-plugin__ontology_search(keyword)：模糊搜索概念\n");
        prompt.append("\n用户需求：").append(message);
        prompt.append("\n\n回答要求：\n");
        prompt.append("- 使用中文，Markdown 格式，结论先行。\n");
        prompt.append("- 引用工具查询结果时给出关键结论，不要原样转储大段 JSON。\n");
        return prompt.toString();
    }
}
