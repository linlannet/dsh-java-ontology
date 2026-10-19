package net.linlan.dsh.sys.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 角色权限校验注解
 * <p>标注在 Controller 方法上，要求当前用户角色层级不超过指定值。
 * <ul>
 *   <li>0 = 主任（SUPER_ADMIN）</li>
 *   <li>1 = 研究员（RESEARCHER）</li>
 *   <li>2 = 访客（GUEST）</li>
 * </ul>
 * @author Linlan
 * CreatedAt 2026-10-14
 * @version 1.0
 * @since 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRole {
    /**
     * 允许的最高角色层级（0=主任/1=研究员/2=访客）
     */
    int value();
}
