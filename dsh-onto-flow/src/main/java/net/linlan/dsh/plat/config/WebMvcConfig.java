package net.linlan.dsh.plat.config;

import net.linlan.dsh.common.TenantContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TenantContextInterceptor());
    }

    static class TenantContextInterceptor implements HandlerInterceptor {

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            String tenantIdStr = request.getHeader("X-Tenant-Id");
            String roleLevelStr = request.getHeader("X-Role-Level");
            String userIdStr = request.getHeader("X-User-Id");

            if (tenantIdStr != null && !tenantIdStr.isEmpty()) {
                try {
                    TenantContext.setTenantId(Long.parseLong(tenantIdStr));
                } catch (NumberFormatException ignored) {
                }
            }
            if (roleLevelStr != null && !roleLevelStr.isEmpty()) {
                try {
                    TenantContext.setRoleLevel(Integer.parseInt(roleLevelStr));
                } catch (NumberFormatException ignored) {
                }
            }
            if (userIdStr != null && !userIdStr.isEmpty()) {
                try {
                    TenantContext.setUserId(Long.parseLong(userIdStr));
                } catch (NumberFormatException ignored) {
                }
            }
            return true;
        }

        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
            TenantContext.clear();
        }
    }
}
