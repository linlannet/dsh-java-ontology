package net.linlan.dsh.sys.dto;

import java.io.Serializable;

/**
 * 登录响应 DTO
 * @author Linlan
 * CreatedAt 2026-10-14
 * @version 1.0
 * @since 1.0
 */
public class LoginResponse implements Serializable {
    /** JWT 令牌 */
    private String token;
    /** 用户ID */
    private Long userId;
    /** 用户名 */
    private String userName;
    /** 昵称 */
    private String nickName;
    /** 角色层级：0=主任/1=研究员/2=访客 */
    private Integer roleLevel;
    /** 租户ID */
    private Long tenantId;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getNickName() { return nickName; }
    public void setNickName(String nickName) { this.nickName = nickName; }
    public Integer getRoleLevel() { return roleLevel; }
    public void setRoleLevel(Integer roleLevel) { this.roleLevel = roleLevel; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
}
