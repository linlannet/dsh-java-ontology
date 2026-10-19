package net.linlan.dsh.sys.config;

import net.linlan.dsh.sys.security.RoleInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置，注册角色权限拦截器
 * @author Linlan
 * CreatedAt 2026-10-14
 * @version 1.0
 * @since 1.0
 */
@Configuration
@EnableAsync
public class SysWebMvcConfig implements WebMvcConfigurer {

    private final RoleInterceptor roleInterceptor;

    public SysWebMvcConfig(RoleInterceptor roleInterceptor) {
        this.roleInterceptor = roleInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(roleInterceptor)
                .addPathPatterns("/api/**");
    }
}
