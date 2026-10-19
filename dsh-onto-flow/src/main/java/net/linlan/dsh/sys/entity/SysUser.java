package net.linlan.dsh.sys.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * @author Linlan
 * CreatedAt 2026-10-14
 * @version 1.0
 * @since 1.0
 */
public class SysUser implements Serializable {
    /** 用户ID */
    private Long userId;
    /** 租户ID（主任可为NULL） */
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
    /** 密码（加密存储） */
    private String password;
    /** 状态：0=正常 1=停用 */
    private String status;
    /** 删除标志：0=正常 1=删除 */
    private String delFlag;
    /** 最后登录IP */
    private String loginIp;
    /** 最后登录时间 */
    private LocalDateTime loginDate;
    /** 创建者 */
    private String createdBy;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新者 */
    private String updatedBy;
    /** 更新时间 */
    private LocalDateTime updatedAt;
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
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getLoginIp() { return loginIp; }
    public void setLoginIp(String loginIp) { this.loginIp = loginIp; }
    public LocalDateTime getLoginDate() { return loginDate; }
    public void setLoginDate(LocalDateTime loginDate) { this.loginDate = loginDate; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
