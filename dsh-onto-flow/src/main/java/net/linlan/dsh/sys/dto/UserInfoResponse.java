package net.linlan.dsh.sys.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户信息响应 DTO
 * @author Linlan
 * CreatedAt 2026-10-14
 * @version 1.0
 * @since 1.0
 */
public class UserInfoResponse implements Serializable {
    /** 用户ID */
    private Long userId;
    /** 租户ID */
    private Long tenantId;
    /** 角色层级：0=主任/1=研究员/2=访客 */
    private Integer roleLevel;
    /** 用户名 */
    private String userName;
    /** 昵称 */
    private String nickName;
    /** 邮箱 */
    private String email;
    /** 手机号 */
    private String phonenumber;
    /** 状态：0=正常 1=停用 */
    private String status;
    /** 最后登录IP */
    private String loginIp;
    /** 最后登录时间 */
    private LocalDateTime loginDate;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 备注 */
    private String remark;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Integer getRoleLevel() { return roleLevel; }
    public void setRoleLevel(Integer roleLevel) { this.roleLevel = roleLevel; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getNickName() { return nickName; }
    public void setNickName(String nickName) { this.nickName = nickName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhonenumber() { return phonenumber; }
    public void setPhonenumber(String phonenumber) { this.phonenumber = phonenumber; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getLoginIp() { return loginIp; }
    public void setLoginIp(String loginIp) { this.loginIp = loginIp; }
    public LocalDateTime getLoginDate() { return loginDate; }
    public void setLoginDate(LocalDateTime loginDate) { this.loginDate = loginDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
