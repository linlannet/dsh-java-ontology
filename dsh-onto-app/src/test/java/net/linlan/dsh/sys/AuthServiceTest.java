package net.linlan.dsh.sys;

import net.linlan.dsh.DshJavaOntologyApplication;
import net.linlan.dsh.sys.dto.LoginRequest;
import net.linlan.dsh.sys.dto.LoginResponse;
import net.linlan.dsh.sys.dto.RegisterRequest;
import net.linlan.dsh.sys.dto.UserInfoResponse;
import net.linlan.dsh.sys.security.JwtUtil;
import net.linlan.dsh.sys.service.SysUserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.jsonwebtoken.Claims;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 认证服务测试
 * @author Linlan
 * CreatedAt 2026-10-14
 * @version 1.0
 * @since 1.0
 */
@SpringBootTest(classes = DshJavaOntologyApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthServiceTest {

    @Autowired
    private SysUserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    @Order(1)
    @DisplayName("主任登录成功")
    void testDirectorLogin() {
        LoginRequest request = new LoginRequest();
        request.setUserName("director");
        request.setPassword("password123");
        LoginResponse response = userService.login(request);
        assertNotNull(response.getToken());
        assertEquals("director", response.getUserName());
        assertEquals(0, response.getRoleLevel());
        assertNull(response.getTenantId());
    }

    @Test
    @Order(2)
    @DisplayName("研究员登录成功")
    void testResearcherLogin() {
        LoginRequest request = new LoginRequest();
        request.setUserName("researcher");
        request.setPassword("password123");
        LoginResponse response = userService.login(request);
        assertNotNull(response.getToken());
        assertEquals("researcher", response.getUserName());
        assertEquals(1, response.getRoleLevel());
        assertEquals(1L, response.getTenantId());
    }

    @Test
    @Order(3)
    @DisplayName("访客登录成功")
    void testGuestLogin() {
        LoginRequest request = new LoginRequest();
        request.setUserName("guest");
        request.setPassword("password123");
        LoginResponse response = userService.login(request);
        assertNotNull(response.getToken());
        assertEquals("guest", response.getUserName());
        assertEquals(2, response.getRoleLevel());
    }

    @Test
    @Order(4)
    @DisplayName("登录失败：错误密码")
    void testLoginWrongPassword() {
        LoginRequest request = new LoginRequest();
        request.setUserName("director");
        request.setPassword("wrongpassword");
        assertThrows(IllegalArgumentException.class, () -> userService.login(request));
    }

    @Test
    @Order(5)
    @DisplayName("登录失败：用户不存在")
    void testLoginUserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setUserName("nonexistent");
        request.setPassword("password123");
        assertThrows(IllegalArgumentException.class, () -> userService.login(request));
    }

    @Test
    @Order(6)
    @DisplayName("注册成功")
    void testRegisterSuccess() {
        RegisterRequest request = new RegisterRequest();
        request.setUserName("testuser_" + System.currentTimeMillis());
        request.setPassword("testpassword");
        request.setNickName("测试用户");
        request.setEmail("test@linlan.net");
        request.setTenantId(1L);
        LoginResponse response = userService.register(request);
        assertNotNull(response.getToken());
        assertEquals(2, response.getRoleLevel());
        assertEquals(1L, response.getTenantId());
    }

    @Test
    @Order(7)
    @DisplayName("注册失败：用户名已存在")
    void testRegisterDuplicate() {
        RegisterRequest request = new RegisterRequest();
        request.setUserName("director");
        request.setPassword("testpassword");
        request.setNickName("重复用户");
        assertThrows(IllegalArgumentException.class, () -> userService.register(request));
    }

    @Test
    @Order(8)
    @DisplayName("JWT 签发与解析")
    void testJwtGenerateAndParse() {
        String token = jwtUtil.generateToken(100L, "testuser", 1, 1L);
        assertTrue(jwtUtil.validateToken(token));
        Claims claims = jwtUtil.parseToken(token);
        assertEquals(100L, jwtUtil.getUserId(claims));
        assertEquals("testuser", jwtUtil.getUserName(claims));
        assertEquals(1, jwtUtil.getRoleLevel(claims));
        assertEquals(1L, jwtUtil.getTenantId(claims));
    }

    @Test
    @Order(9)
    @DisplayName("JWT 签发：主任 tenantId 为 null")
    void testJwtDirectorNullTenant() {
        String token = jwtUtil.generateToken(1L, "director", 0, null);
        assertTrue(jwtUtil.validateToken(token));
        Claims claims = jwtUtil.parseToken(token);
        assertEquals(1L, jwtUtil.getUserId(claims));
        assertEquals(0, jwtUtil.getRoleLevel(claims));
        assertNull(jwtUtil.getTenantId(claims));
    }

    @Test
    @Order(10)
    @DisplayName("JWT 验证：无效令牌")
    void testJwtInvalidToken() {
        assertFalse(jwtUtil.validateToken("invalid.token.here"));
        assertFalse(jwtUtil.validateToken(""));
    }

    @Test
    @Order(11)
    @DisplayName("获取所有用户")
    void testFindAllUsers() {
        var users = userService.findAll();
        assertNotNull(users);
        assertTrue(users.size() >= 3);
    }

    @Test
    @Order(12)
    @DisplayName("根据ID查找用户")
    void testFindById() {
        var users = userService.findAll();
        if (!users.isEmpty()) {
            UserInfoResponse first = users.get(0);
            var found = userService.findById(first.getUserId());
            assertTrue(found.isPresent());
            assertEquals(first.getUserName(), found.get().getUserName());
        }
    }

    @Test
    @Order(13)
    @DisplayName("修改密码")
    void testChangePassword() {
        RegisterRequest regReq = new RegisterRequest();
        regReq.setUserName("pwdtest_" + System.currentTimeMillis());
        regReq.setPassword("oldpassword");
        regReq.setNickName("密码测试");
        regReq.setTenantId(1L);
        LoginResponse regResp = userService.register(regReq);

        int rows = userService.changePassword(regResp.getUserId(), "oldpassword", "newpassword", String.valueOf(regResp.getUserId()));
        assertEquals(1, rows);

        LoginRequest loginOld = new LoginRequest();
        loginOld.setUserName(regReq.getUserName());
        loginOld.setPassword("oldpassword");
        assertThrows(IllegalArgumentException.class, () -> userService.login(loginOld));

        LoginRequest loginNew = new LoginRequest();
        loginNew.setUserName(regReq.getUserName());
        loginNew.setPassword("newpassword");
        LoginResponse loginResp = userService.login(loginNew);
        assertNotNull(loginResp.getToken());
    }

    @Test
    @Order(14)
    @DisplayName("软删除用户")
    void testSoftDelete() {
        RegisterRequest regReq = new RegisterRequest();
        regReq.setUserName("deltest_" + System.currentTimeMillis());
        regReq.setPassword("testpassword");
        regReq.setNickName("删除测试");
        regReq.setTenantId(1L);
        LoginResponse regResp = userService.register(regReq);

        int rows = userService.softDelete(regResp.getUserId());
        assertEquals(1, rows);

        var found = userService.findById(regResp.getUserId());
        assertTrue(found.isEmpty());
    }
}
