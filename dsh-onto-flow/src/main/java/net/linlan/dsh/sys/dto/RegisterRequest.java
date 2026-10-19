package net.linlan.dsh.sys.dto;

import java.io.Serializable;

/**
 * 注册请求 DTO
 * @author Linlan
 * CreatedAt 2026-10-14
 * @version 1.0
 * @since 1.0
 */
public class RegisterRequest implements Serializable {
    /** 用户名 */
    private String userName;
    /** 密码 */
    private String password;
    /** 昵称 */
    private String nickName;
    /** 邮箱 */
    private String email;
    /** 手机号 */
    private String phonenumber;
    /** 租户ID（注册时指定，不传则默认为1） */
    private Long tenantId;

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNickName() { return nickName; }
    public void setNickName(String nickName) { this.nickName = nickName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhonenumber() { return phonenumber; }
    public void setPhonenumber(String phonenumber) { this.phonenumber = phonenumber; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
}
