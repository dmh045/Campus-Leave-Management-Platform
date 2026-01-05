// AuthApiTest.java
package com.example.leavesystem;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.*;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AuthApiTest extends BaseApiIntegrationTest {

    private final String staffLoginType = System.getProperty("test.auth.staff.loginType", "STAFF");
    private final String staffUsername  = System.getProperty("test.auth.staff.username", "T20250034");
    private final String staffPassword  = System.getProperty("test.auth.staff.password", "250034");

    private String staffToken;

    private JsonNode doLogin(String loginType, String username, String password) throws Exception {
        clearAuth(); // 登录不带旧 Authorization
        String body = """
                {"loginType":"%s","username":"%s","password":"%s"}
                """.formatted(loginType, username, password);
        return postJson("/api/auth/login", body);
    }

    @Test
    @Order(1)
    void login_staff_shouldReturnToken() throws Exception {
        JsonNode root = doLogin(staffLoginType, staffUsername, staffPassword);
        assertCodeOk(root);

        staffToken = root.at("/data/token").asText("");
        assertFalse(staffToken.isBlank(), "登录成功但 data.token 为空，resp=" + root);

        setAuth("Authorization", "Bearer " + staffToken);
    }

    @Test
    @Order(2)
    void login_staff_wrongPassword_shouldFail() throws Exception {
        JsonNode root = doLogin(staffLoginType, staffUsername, staffPassword + "_wrong");
        assertTrue(root.has("code"), "响应缺少 code 字段，resp=" + root);
        assertNotEquals(0, root.get("code").asInt(), "密码错误却返回 code=0，resp=" + root);
    }

    @Test
    @Order(3)
    void login_missingFields_shouldFail() throws Exception {
        clearAuth();
        JsonNode root = postJson("/api/auth/login", "{}");
        assertTrue(root.has("code"), "响应缺少 code 字段，resp=" + root);
        assertNotEquals(0, root.get("code").asInt(), "缺字段却返回 code=0，resp=" + root);
    }

    @Test
    @Order(4)
    void logout_shouldSuccess() throws Exception {
        assertNotNull(staffToken, "前置条件失败：staffToken 为空，请先看 login_staff_shouldReturnToken 是否通过");
        JsonNode root = postJson("/api/auth/logout?token=" + staffToken, "{}");
        assertCodeOk(root);
    }

    @Test
    @Order(5)
    void logout_withoutTokenParam_shouldReturn500AndErrorBody() throws Exception {
        clearAuth();

        var result = mockMvc.perform(post("/api/auth/logout"))
                .andReturn();

        int httpStatus = result.getResponse().getStatus();
        assertEquals(500, httpStatus, "你当前后端缺参会返回 500（见日志），若你修成 400 了再改这里断言。");

        String body = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertFalse(body == null || body.isBlank(), "500 但响应体为空");

        JsonNode root = objectMapper.readTree(body);
        assertCode(root, -1);

        String msg = root.path("message").asText("");
        assertTrue(
                msg.contains("Required request parameter") || msg.contains("token"),
                "错误信息不包含 token 缺失提示，resp=" + root
        );
    }
}
