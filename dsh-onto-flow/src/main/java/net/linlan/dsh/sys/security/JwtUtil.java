package net.linlan.dsh.sys.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类，负责令牌签发与解析
 * @author Linlan
 * CreatedAt 2026-10-14
 * @version 1.0
 * @since 1.0
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMs;

    public JwtUtil(
            @Value("${dsh.ontology.jwt.secret}") String secret,
            @Value("${dsh.ontology.jwt.expiration-ms:86400000}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * 签发 JWT 令牌
     * @param userId 用户ID
     * @param userName 用户名
     * @param roleLevel 角色层级（0=主任/1=研究员/2=访客）
     * @param tenantId 租户ID（主任可为null）
     * @return JWT 令牌字符串
     */
    public String generateToken(Long userId, String userName, Integer roleLevel, Long tenantId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("userName", userName)
                .claim("roleLevel", roleLevel)
                .claim("tenantId", tenantId)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    /**
     * 解析 JWT 令牌
     * @param token JWT 令牌
     * @return Claims 声明
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从令牌中提取用户ID
     */
    public Long getUserId(Claims claims) {
        return Long.valueOf(claims.getSubject());
    }

    /**
     * 从令牌中提取用户名
     * 过渡双读：优先 snake_case（linlan-emis 标准契约），缺失时回退旧 camelCase
     */
    public String getUserName(Claims claims) {
        String userName = claims.get("user_name", String.class);
        return userName != null ? userName : claims.get("userName", String.class);
    }

    /**
     * 从令牌中提取角色层级
     * 过渡双读：优先 role_level，缺失时回退 roleLevel
     */
    public Integer getRoleLevel(Claims claims) {
        return toInteger(claims.get("role_level"), claims.get("roleLevel"));
    }

    /**
     * 从令牌中提取租户ID
     * 过渡双读：优先 tenant_id，缺失时回退 tenantId
     */
    public Long getTenantId(Claims claims) {
        return toLong(claims.get("tenant_id"), claims.get("tenantId"));
    }

    private static Integer toInteger(Object primary, Object fallback) {
        Object value = primary != null ? primary : fallback;
        if (value == null) {
            return null;
        }
        return value instanceof Number number ? number.intValue() : Integer.valueOf(value.toString());
    }

    private static Long toLong(Object primary, Object fallback) {
        Object value = primary != null ? primary : fallback;
        if (value == null) {
            return null;
        }
        return value instanceof Number number ? number.longValue() : Long.valueOf(value.toString());
    }

    /**
     * 验证令牌是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
