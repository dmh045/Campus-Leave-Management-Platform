package com.example.leavesystem;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public abstract class BaseApiIntegrationTest {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;

    @Autowired(required = false)
    @Nullable
    protected JdbcTemplate jdbcTemplate;

    // ✅ 子类要用就一定要在 Base 里声明
    protected String authHeaderName = System.getProperty("test.auth.header", "Authorization");
    protected String authHeaderValue = System.getProperty("test.auth.value", "");

    protected void setAuth(String headerName, String headerValue) {
        this.authHeaderName = headerName;
        this.authHeaderValue = headerValue;
    }

    protected void clearAuth() {
        this.authHeaderValue = "";
    }

    protected JsonNode getJson(String urlWithQuery) throws Exception {
        return performAndParse(applyAuth(get(urlWithQuery)));
    }

    protected JsonNode postJson(String urlWithQuery, String jsonBody) throws Exception {
        MockHttpServletRequestBuilder req = post(urlWithQuery)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody);
        return performAndParse(applyAuth(req));
    }

    protected JsonNode putJson(String urlWithQuery, String jsonBody) throws Exception {
        MockHttpServletRequestBuilder req = put(urlWithQuery)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody);
        return performAndParse(applyAuth(req));
    }

    protected MockHttpServletRequestBuilder applyAuth(MockHttpServletRequestBuilder req) {
        if (authHeaderName != null && !authHeaderName.isBlank()
                && authHeaderValue != null && !authHeaderValue.isBlank()) {
            req.header(authHeaderName, authHeaderValue);
        }
        return req;
    }

    protected JsonNode performAndParse(MockHttpServletRequestBuilder req) throws Exception {
        MvcResult result = mockMvc.perform(req).andReturn();
        String body = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

        // 不强制 200，留给各 test 自己断言 code
        if (body == null || body.isBlank()) {
            return objectMapper.createObjectNode();
        }
        return objectMapper.readTree(body);
    }

    protected void assertCode(JsonNode root, int expected) {
        Assertions.assertTrue(root.has("code"),
                "响应 JSON 中缺少 code 字段。root=" + root);
        Assertions.assertEquals(expected, root.get("code").asInt(),
                "code 不符合预期。root=" + root);
    }

    protected void assertCodeOk(JsonNode root) {
        assertCode(root, 0);
    }

    protected long extractDataLong(JsonNode root, String preferredField) {
        JsonNode data = root.get("data");
        Assertions.assertNotNull(data, "响应缺少 data。root=" + root);

        if (data.isIntegralNumber()) return data.asLong();

        if (data.isObject()) {
            if (data.has(preferredField) && data.get(preferredField).isIntegralNumber()) {
                return data.get(preferredField).asLong();
            }
            if (data.has("id") && data.get("id").isIntegralNumber()) {
                return data.get("id").asLong();
            }
        }
        throw new AssertionError("无法从 data 提取 long(" + preferredField + ")，root=" + root);
    }

    protected String extractDataText(JsonNode root, String preferredField) {
        JsonNode data = root.get("data");
        Assertions.assertNotNull(data, "响应缺少 data。root=" + root);

        if (data.isTextual()) return data.asText();

        if (data.isObject() && data.has(preferredField) && data.get(preferredField).isTextual()) {
            return data.get(preferredField).asText();
        }
        throw new AssertionError("无法从 data 提取 text(" + preferredField + ")，root=" + root);
    }

    protected long findFirstIdInDataArray(JsonNode root, List<String> candidateFields) {
        JsonNode data = root.get("data");
        Assertions.assertNotNull(data, "响应缺少 data。root=" + root);
        Assertions.assertTrue(data.isArray(), "data 不是数组。root=" + root);

        for (JsonNode item : data) {
            if (!item.isObject()) continue;
            for (String f : candidateFields) {
                if (item.has(f) && item.get(f).isIntegralNumber()) {
                    return item.get(f).asLong();
                }
            }
        }
        throw new AssertionError("data 数组中找不到候选 id 字段 " + candidateFields + "。root=" + root);
    }

    protected Long sqlFindLatestAbsenceIdByStatus(String status) {
        if (jdbcTemplate == null) return null;
        return jdbcTemplate.query(
                "SELECT absence_id FROM absence WHERE status=? ORDER BY absence_id DESC LIMIT 1",
                ps -> ps.setString(1, status),
                rs -> rs.next() ? rs.getLong(1) : null
        );
    }

    protected String sqlGetAbsenceStatus(long absenceId) {
        if (jdbcTemplate == null) return null;
        return jdbcTemplate.query(
                "SELECT status FROM absence WHERE absence_id=?",
                ps -> ps.setLong(1, absenceId),
                rs -> rs.next() ? rs.getString(1) : null
        );
    }

    protected Long sqlGetConvertedLeaveId(long absenceId) {
        if (jdbcTemplate == null) return null;
        return jdbcTemplate.query(
                "SELECT converted_leave_id FROM absence WHERE absence_id=?",
                ps -> ps.setLong(1, absenceId),
                rs -> rs.next() ? (rs.getObject(1) == null ? null : rs.getLong(1)) : null
        );
    }

    protected void assertStatusEquals(JsonNode detailRoot, String expectedStatus) {
        // 约定：detail 接口返回 data.status
        JsonNode statusNode = detailRoot.at("/data/status");
        Assertions.assertFalse(statusNode.isMissingNode(),
                "detail 缺少 data.status。root=" + detailRoot);
        Assertions.assertEquals(expectedStatus, statusNode.asText(),
                "状态不符合预期。root=" + detailRoot);
    }
}
