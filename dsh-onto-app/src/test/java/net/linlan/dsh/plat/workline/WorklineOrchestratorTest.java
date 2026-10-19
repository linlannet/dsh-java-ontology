package net.linlan.dsh.plat.workline;

import net.linlan.dsh.common.TenantContext;
import net.linlan.dsh.DshJavaOntologyApplication;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 工作流编排测试
 * @author Linlan
 * CreatedAt 2026-10-14
 * @version 1.0
 * @since 1.0
 */
@SpringBootTest(classes = DshJavaOntologyApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WorklineOrchestratorTest {

    @Autowired
    private WorklineService worklineService;

    @Autowired
    private WorklineOrchestrator orchestrator;

    private static String worklineId;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(1L);
        TenantContext.setRoleLevel(1);
        TenantContext.setUserId(1L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @Order(1)
    @DisplayName("获取七阶段定义")
    void testGetStages() {
        List<Map<String, Object>> stages = worklineService.getStages();
        assertEquals(7, stages.size());
        assertEquals(1, stages.get(0).get("order"));
        assertEquals("主题设定", stages.get(0).get("name"));
        assertEquals("PUSH_DISPLAY", stages.get(6).get("code"));
    }

    @Test
    @Order(2)
    @DisplayName("激活主题创建工作流")
    void testActivateTopic() {
        WorklineContext context = worklineService.activateTopic(1L);
        assertNotNull(context);
        assertNotNull(context.getWorklineId());
        assertEquals("RUNNING_TOPIC_SETUP", context.getStatus());
        worklineId = context.getWorklineId();
    }

    @Test
    @Order(3)
    @DisplayName("查询工作流状态")
    void testGetStatus() {
        Assumptions.assumeTrue(worklineId != null);
        WorklineContext context = worklineService.getStatus(worklineId);
        assertNotNull(context);
        assertEquals(worklineId, context.getWorklineId());
    }

    @Test
    @Order(4)
    @DisplayName("查询工作流进度")
    void testGetProgress() {
        Assumptions.assumeTrue(worklineId != null);
        List<WorklinePhaseResult> results = worklineService.getProgress(worklineId);
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertTrue(results.get(0).isSuccess());
    }

    @Test
    @Order(5)
    @DisplayName("单阶段触发：NLP_ANALYSIS")
    void testExecuteSinglePhase() {
        Assumptions.assumeTrue(worklineId != null);
        WorklinePhaseResult result = worklineService.executePhase(worklineId, WorklinePhase.NLP_ANALYSIS);
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(WorklinePhase.NLP_ANALYSIS, result.getPhase());
    }

    @Test
    @Order(6)
    @DisplayName("执行到指定阶段：KG_CONSTRUCTION")
    void testExecuteUntil() {
        Assumptions.assumeTrue(worklineId != null);
        List<WorklinePhaseResult> results = worklineService.executeUntil(worklineId, WorklinePhase.KG_CONSTRUCTION);
        assertNotNull(results);
        assertTrue(results.size() <= 4);
    }

    @Test
    @Order(7)
    @DisplayName("执行全部阶段")
    void testExecuteAll() {
        Assumptions.assumeTrue(worklineId != null);
        List<WorklinePhaseResult> results = worklineService.executeAll(worklineId);
        assertNotNull(results);
        assertEquals(7, results.size());
        for (WorklinePhaseResult result : results) {
            assertTrue(result.isSuccess(), "Phase " + result.getPhase() + " should succeed");
        }
    }

    @Test
    @Order(8)
    @DisplayName("WorklinePhase 枚举：fromOrder")
    void testPhaseFromOrder() {
        assertEquals(WorklinePhase.TOPIC_SETUP, WorklinePhase.fromOrder(1));
        assertEquals(WorklinePhase.PUSH_DISPLAY, WorklinePhase.fromOrder(7));
        assertThrows(IllegalArgumentException.class, () -> WorklinePhase.fromOrder(8));
    }

    @Test
    @Order(9)
    @DisplayName("工作流不存在时抛异常")
    void testNonExistentWorkline() {
        assertThrows(IllegalArgumentException.class, () -> worklineService.executeAll("non-existent"));
    }
}
