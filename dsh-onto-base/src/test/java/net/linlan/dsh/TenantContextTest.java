package net.linlan.dsh.common;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TenantContextTest {

    @AfterEach
    void cleanup() {
        TenantContext.clear();
    }

    @Test
    void setAndGetTenantId() {
        TenantContext.setTenantId(42L);
        assertEquals(42L, TenantContext.getTenantId());
    }

    @Test
    void setAndGetRoleLevel() {
        TenantContext.setRoleLevel(0);
        assertEquals(0, TenantContext.getRoleLevel());
        assertTrue(TenantContext.isDirector());
        assertFalse(TenantContext.isResearcher());
        assertFalse(TenantContext.isGuest());
    }

    @Test
    void isResearcher() {
        TenantContext.setRoleLevel(1);
        assertTrue(TenantContext.isResearcher());
    }

    @Test
    void isGuest() {
        TenantContext.setRoleLevel(2);
        assertTrue(TenantContext.isGuest());
    }

    @Test
    void clear_resetsAll() {
        TenantContext.setTenantId(1L);
        TenantContext.setRoleLevel(1);
        TenantContext.setUserId(1L);
        TenantContext.clear();
        assertNull(TenantContext.getTenantId());
        assertNull(TenantContext.getRoleLevel());
        assertNull(TenantContext.getUserId());
    }
}
