package net.linlan.dsh.ontology;

import net.linlan.dsh.common.TenantContext;
import net.linlan.dsh.DshJavaOntologyApplication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = DshJavaOntologyApplication.class)
@ActiveProfiles("test")
public abstract class BaseTest {

    @BeforeEach
    void setUpTenantContext() {
        TenantContext.setTenantId(1L);
        TenantContext.setRoleLevel(1);
        TenantContext.setUserId(1L);
    }

    @AfterEach
    void clearTenantContext() {
        TenantContext.clear();
    }
}
