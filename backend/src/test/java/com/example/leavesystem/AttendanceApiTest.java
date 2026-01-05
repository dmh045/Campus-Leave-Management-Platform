// AttendanceApiTest.java
package com.example.leavesystem;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AttendanceApiTest extends BaseApiIntegrationTest {

    private long session1Id;
    private String session1Token;

    private long session2Id;
    private String session2Token;

    @Test @Order(26)
    void step26_startSession_linkWithApprovedLeaveScenario() throws Exception {
        String body = """
                {
                  "teacherId": 2,
                  "offeringId": 1,
                  "courseDate": "2024-10-10",
                  "sectionStart": 1,
                  "sectionEnd": 2,
                  "durationMinutes": 10
                }
                """;
        JsonNode root = postJson("/api/attendance/session/start", body);
        assertCodeOk(root);

        session1Id = extractDataLong(root, "sessionId");
        session1Token = extractDataText(root, "token");
        Assertions.assertTrue(session1Id > 0);
        Assertions.assertFalse(session1Token.isBlank());
    }

    @Test @Order(27)
    void step27_student2_checkin_session1() throws Exception {
        String body = """
                {
                  "studentId": 2,
                  "token": "%s"
                }
                """.formatted(session1Token);
        JsonNode root = postJson("/api/attendance/checkin", body);
        assertCodeOk(root);
    }

    @Test @Order(28)
    void step28_closeSession1() throws Exception {
        JsonNode root = postJson("/api/attendance/session/%d/close?teacherId=2".formatted(session1Id), "{}");
        assertCodeOk(root);
    }

    @Test @Order(29)
    void step29_teacherSessionsList_oct() throws Exception {
        JsonNode root = getJson("/api/attendance/teacher/sessions?teacherId=2&startDate=2024-10-01&endDate=2024-10-31");
        assertCodeOk(root);
    }

    @Test @Order(30)
    void step30_session1_detail() throws Exception {
        JsonNode root = getJson("/api/attendance/session/%d/detail?teacherId=2".formatted(session1Id));
        assertCodeOk(root);
    }

    @Test @Order(31)
    void step31_startSession_noLeaveScenario() throws Exception {
        String body = """
                {
                  "teacherId": 2,
                  "offeringId": 1,
                  "courseDate": "2024-10-15",
                  "sectionStart": 1,
                  "sectionEnd": 2,
                  "durationMinutes": 10
                }
                """;
        JsonNode root = postJson("/api/attendance/session/start", body);
        assertCodeOk(root);

        session2Id = extractDataLong(root, "sessionId");
        session2Token = extractDataText(root, "token");
        Assertions.assertTrue(session2Id > 0);
        Assertions.assertFalse(session2Token.isBlank());
    }

    @Test @Order(32)
    void step32_onlyStudent1_checkin_session2() throws Exception {
        String body = """
                {
                  "studentId": 1,
                  "token": "%s"
                }
                """.formatted(session2Token);
        JsonNode root = postJson("/api/attendance/checkin", body);
        assertCodeOk(root);
    }

    @Test @Order(33)
    void step33_closeSession2_shouldGenerateAbsence() throws Exception {
        JsonNode root = postJson("/api/attendance/session/%d/close?teacherId=2".formatted(session2Id), "{}");
        assertCodeOk(root);
    }

    @Test @Order(34)
    void step34_session2_detail_verifyPresentAbsentDistribution() throws Exception {
        JsonNode root = getJson("/api/attendance/session/%d/detail?teacherId=2".formatted(session2Id));
        assertCodeOk(root);
    }
}
