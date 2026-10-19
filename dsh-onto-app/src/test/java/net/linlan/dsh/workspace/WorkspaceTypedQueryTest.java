package net.linlan.dsh.workspace;

import net.linlan.dsh.ontology.BaseTest;
import net.linlan.dsh.common.entity.CommonDiscoveredEntity;
import net.linlan.dsh.workspace.service.WorkspaceCrudService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WorkspaceTypedQueryTest extends BaseTest {

    @Autowired
    private WorkspaceCrudService workspaceService;

    @Test
    void discoveredEntity_typedQuery_mapsSnakeCaseToCamelCase() {
        workspaceService.create("discoveredEntity", Map.of(
                "entity_name", "类型化测试发现实体",
                "entity_type", "person",
                "mention_count", 5,
                "status", "DISCOVERED"
        ));

        List<CommonDiscoveredEntity> entities = workspaceService.findAllTyped("discoveredEntity");

        CommonDiscoveredEntity entity = entities.stream()
                .filter(e -> "类型化测试发现实体".equals(e.getEntityName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("未找到测试发现实体"));

        assertEquals("person", entity.getEntityType());
        assertEquals(5, entity.getMentionCount());
        assertEquals("DISCOVERED", entity.getStatus());
        assertNotNull(entity.getId());
        assertEquals(1L, entity.getTenantId());
        assertNotNull(entity.getCreatedAt());
        assertEquals(0, entity.getDelFlag());
    }

    @Test
    void llmChatHistory_typedQuery_mapsAllFields() {
        workspaceService.create("llmChatHistory", Map.of(
                "agent_name", "test-agent",
                "role", "user",
                "content", "类型化测试对话内容",
                "session_id", "session-001"
        ));

        List<net.linlan.dsh.common.entity.CommonLlmChatHistory> histories = workspaceService.findAllTyped("llmChatHistory");

        net.linlan.dsh.common.entity.CommonLlmChatHistory history = histories.stream()
                .filter(h -> "test-agent".equals(h.getAgentName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("未找到测试对话记录"));

        assertEquals("user", history.getRole());
        assertEquals("类型化测试对话内容", history.getContent());
        assertEquals("session-001", history.getSessionId());
        assertNotNull(history.getId());
        assertEquals(1L, history.getTenantId());
    }
}
