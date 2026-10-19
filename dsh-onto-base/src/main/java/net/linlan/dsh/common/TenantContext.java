package net.linlan.dsh.common;

public final class TenantContext {

    private static final ThreadLocal<Long> CURRENT_TENANT = new ThreadLocal<>();
    private static final ThreadLocal<Integer> CURRENT_role_level = new ThreadLocal<>();
    private static final ThreadLocal<Long> CURRENT_user_id = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setTenantId(Long tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static Long getTenantId() {
        return CURRENT_TENANT.get();
    }

    public static void setRoleLevel(Integer roleLevel) {
        CURRENT_role_level.set(roleLevel);
    }

    public static Integer getRoleLevel() {
        return CURRENT_role_level.get();
    }

    public static void setUserId(Long userId) {
        CURRENT_user_id.set(userId);
    }

    public static Long getUserId() {
        return CURRENT_user_id.get();
    }

    public static boolean isDirector() {
        Integer level = CURRENT_role_level.get();
        return level != null && level == 0;
    }

    public static boolean isResearcher() {
        Integer level = CURRENT_role_level.get();
        return level != null && level == 1;
    }

    public static boolean isGuest() {
        Integer level = CURRENT_role_level.get();
        return level != null && level == 2;
    }

    public static void clear() {
        CURRENT_TENANT.remove();
        CURRENT_role_level.remove();
        CURRENT_user_id.remove();
    }
}
