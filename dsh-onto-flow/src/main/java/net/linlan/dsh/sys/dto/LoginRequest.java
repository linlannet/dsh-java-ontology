package net.linlan.dsh.sys.dto;

import java.io.Serializable;

/**
 * 登录请求 DTO
 * @author Linlan
 * CreatedAt 2026-10-14
 * @version 1.0
 * @since 1.0
 */
public class LoginRequest implements Serializable {
    /** 用户名 */
    private String userName;
    /** 密码 */
    private String password;

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
