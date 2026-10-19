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
     */
    public String getUserName(Claims claims) {
        return claims.get("userName", String.class);
    }

    /**
     * 从令牌中提取角色层级
     */
    public Integer getRoleLevel(Claims claims) {
        return claims.get("roleLevel", Integer.class);
    }

    /**
     * 从令牌中提取租户ID
     */
    public Long getTenantId(Claims claims) {
        Object tid = claims.get("tenantId");
        if (tid == null) {
            return null;
        }
        if (tid instanceof Number) {
            return ((Number) tid).longValue();
        }
        return Long.valueOf(tid.toString());
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
