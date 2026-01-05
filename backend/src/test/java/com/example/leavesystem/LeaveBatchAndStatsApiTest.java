// LeaveBatchAndStatsApiTest.java  (覆盖 .http 的 19–25，修复 @Order 冲突)
package com.example.leavesystem;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LeaveBatchAndStatsApiTest extends BaseApiIntegrationTest {

    private long leave4Id;
    private long leave5Id;
    private List<Long> publicBatchLeaveIds = new ArrayList<>();

    @Test @Order(19)
    void step19_applyLeave4() throws Exception {
        String body = """
                {
                  "studentId": 1,
                  "termId": 1,
                  "leaveType": "SICK",
                  "applyChannel": "BY_SELF",
                  "reason": "连续感冒请假（第一天）",
                  "proofUrl": null,
                  "startTime": "2024-11-10T08:00:00",
                  "endTime": "2024-11-10T18:00:00",
                  "impacts": [
                    {
                      "offeringId": 1,
                      "courseDate": "2024-11-10",
                      "sectionStart": 1,
                      "sectionEnd": 2
                    }
                  ]
                }
                """;
        JsonNode root = postJson("/api/leaves/apply", body);
        assertCodeOk(root);
        leave4Id = extractDataLong(root, "leaveId");
        Assertions.assertTrue(leave4Id > 0);
    }

    @Test @Order(20)
    void step20_applyLeave5() throws Exception {
        String body = """
                {
                  "studentId": 1,
                  "termId": 1,
                  "leaveType": "SICK",
                  "applyChannel": "BY_SELF",
                  "reason": "连续感冒请假（第二天）",
                  "proofUrl": null,
                  "startTime": "2024-11-11T08:00:00",
                  "endTime": "2024-11-11T18:00:00",
                  "impacts": [
                    {
                      "offeringId": 1,
                      "courseDate": "2024-11-11",
                      "sectionStart": 1,
                      "sectionEnd": 2
                    }
                  ]
                }
                """;
        JsonNode root = postJson("/api/leaves/apply", body);
        assertCodeOk(root);
        leave5Id = extractDataLong(root, "leaveId");
        Assertions.assertTrue(leave5Id > 0);
    }

    @Test @Order(21)
    void step21_counselorBatchAgree_leave4_leave5() throws Exception {
        String body = """
                {
                  "counselorId": 1,
                  "action": "AGREE",
                  "comment": "批量同意两天病假",
                  "leaveIds": [%d, %d]
                }
                """.formatted(leave4Id, leave5Id);

        JsonNode root = postJson("/api/leaves/counselor-approve/batch", body);
        assertCodeOk(root);
    }

    @Test @Order(22)
    void step22_leave4_detail_shouldBePendingTeacher() throws Exception {
        JsonNode root = getJson("/api/leaves/%d/detail".formatted(leave4Id));
        assertCodeOk(root);
        assertStatusEquals(root, "PENDING_TEACHER");
    }

    @Test @Order(23)
    void step23_leave5_detail_shouldBePendingTeacher() throws Exception {
        JsonNode root = getJson("/api/leaves/%d/detail".formatted(leave5Id));
        assertCodeOk(root);
        assertStatusEquals(root, "PENDING_TEACHER");
    }

    @Test @Order(24)
    void step24_publicBatchCreate() throws Exception {
        String body = """
                {
                  "counselorId": 1,
                  "studentIds": [1, 2],
                  "termId": 1,
                  "reason": "学院组织参加省级竞赛（公假）",
                  "startTime": "2024-12-01T08:00:00",
                  "endTime": "2024-12-01T18:00:00",
                  "impacts": [
                    {
                      "offeringId": 1,
                      "courseDate": "2024-12-01",
                      "sectionStart": 1,
                      "sectionEnd": 2
                    }
                  ]
                }
                """;
        JsonNode root = postJson("/api/leaves/public/batch", body);
        assertCodeOk(root);

        JsonNode data = root.get("data");
        Assertions.assertNotNull(data);
        Assertions.assertTrue(data.isArray(), "public/batch 的 data 应是 leaveId 数组。root=" + root);

        publicBatchLeaveIds.clear();
        for (JsonNode n : data) publicBatchLeaveIds.add(n.asLong());
        Assertions.assertTrue(publicBatchLeaveIds.size() >= 2, "至少生成 2 张公假。root=" + root);
    }

    @Test @Order(25)
    void step25_classLeaveStats() throws Exception {
        JsonNode root = getJson("/api/stats/class-leave?classId=1&startDate=2024-10-01&endDate=2024-12-31");
        assertCodeOk(root);
    }

    @Test @Order(26)
    void step26_teacherPendingByCourse() throws Exception {
        JsonNode root = getJson("/api/leaves/pending/teacher/by-course?teacherId=2");
        assertCodeOk(root);
    }
}
