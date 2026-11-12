package net.linlan.dsh.sys.security;

import net.linlan.dsh.common.TenantContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * JWT 认证过滤器，解析请求头中的令牌并设置 TenantContext
 * @author Linlan
 * CreatedAt 2026-10-14
 * @version 1.0
 * @since 1.0
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final String header;
    private final String prefix;
    private final boolean defaultDirectorEnabled;

    public JwtAuthenticationFilter(
            JwtUtil jwtUtil,
            @Value("${dsh.ontology.jwt.header:Authorization}") String header,
            @Value("${dsh.ontology.jwt.prefix:Bearer }") String prefix,
            @Value("${dsh.ontology.auth.default-director-enabled:false}") boolean defaultDirectorEnabled) {
        this.jwtUtil = jwtUtil;
        this.header = header;
        this.prefix = prefix;
        this.defaultDirectorEnabled = defaultDirectorEnabled;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String token = extractToken(request);
            boolean authenticated = false;
            if (token != null && jwtUtil.validateToken(token)) {
                Claims claims = jwtUtil.parseToken(token);
                TenantContext.setUserId(jwtUtil.getUserId(claims));
                TenantContext.setRoleLevel(jwtUtil.getRoleLevel(claims));
                TenantContext.setTenantId(jwtUtil.getTenantId(claims));
                authenticated = true;
            }
            if (!authenticated && defaultDirectorEnabled) {
                // 桌面端暂无登录界面：未携带有效令牌时默认以主任身份（跨租户）放行，仅用于本地/内网演示
                TenantContext.setUserId(1L);
                TenantContext.setRoleLevel(0);
                TenantContext.setTenantId(null);
            }
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * 从请求头中提取 JWT 令牌
     */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader(header);
        if (authHeader != null && authHeader.startsWith(prefix)) {
            return authHeader.substring(prefix.length());
        }
        return null;
    }
}
