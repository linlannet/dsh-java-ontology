package net.linlan.dsh.sys.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记为公开 API，不进行角色校验。
 * <p>用于登录、注册等无需认证的端点。未标注 @PublicApi 且未标注 @RequiresRole 的端点默认拒绝（fail-closed）。
 * @author Linlan
 * CreatedAt 2026-10-18
 * @version 1.0
 * @since 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PublicApi {
}
