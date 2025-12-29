// LeaveWorkflowApiTest.java
package com.example.leavesystem;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LeaveWorkflowApiTest extends BaseApiIntegrationTest {

    private long leave1Id; // 主线假条 #1
    private long leave2Id; // REJECT 假条 #2
    private long leave3Id; // RETURN 假条 #3
    private long impactIdForLeave1; // 老师待确认 impactId（动态抓）

    @Test @Order(0)
    void step0_healthCheck() throws Exception {
        JsonNode root = getJson("/api/leaves/my?studentId=1");
        assertCodeOk(root);
    }

    @Test @Order(1)
    void step1_applyLeave1() throws Exception {
        String body = """
                {
                  "studentId": 1,
                  "termId": 1,
                  "leaveType": "SICK",
                  "applyChannel": "BY_COURSE",
                  "reason": "感冒发烧，需要到医院就诊",
                  "proofUrl": null,
                  "startTime": "2024-10-10T08:00:00",
                  "endTime": "2024-10-10T12:00:00",
                  "impacts": [
                    {
                      "offeringId": 1,
                      "courseDate": "2024-10-10",
                      "sectionStart": 1,
                      "sectionEnd": 2
                    }
                  ]
                }
                """;
        JsonNode root = postJson("/api/leaves/apply", body);
        assertCodeOk(root);
        leave1Id = extractDataLong(root, "leaveId");
        Assertions.assertTrue(leave1Id > 0);
    }

    @Test @Order(2)
    void step2_counselorPendingList() throws Exception {
        JsonNode root = getJson("/api/leaves/pending/counselor?counselorId=1");
        assertCodeOk(root);
    }

    @Test @Order(3)
    void step3_counselorReturnLeave1() throws Exception {
        String body = """
                {
                  "counselorId": 1,
                  "action": "RETURN",
                  "comment": "请补充医院就诊证明"
                }
                """;
        JsonNode root = postJson("/api/leaves/%d/counselor-approve".formatted(leave1Id), body);
        assertCodeOk(root);
    }

    @Test @Order(4)
    void step4_resubmitLeave1() throws Exception {
        String body = """
                {
                  "studentId": 1,
                  "termId": 1,
                  "leaveType": "SICK",
                  "applyChannel": "BY_COURSE",
                  "reason": "已补充医院就诊证明，病假申请",
                  "proofUrl": "http://example.com/proof_new.jpg",
                  "startTime": "2024-10-10T08:00:00",
                  "endTime": "2024-10-10T12:00:00",
                  "impacts": [
                    {
                      "offeringId": 1,
                      "courseDate": "2024-10-10",
                      "sectionStart": 1,
                      "sectionEnd": 2
                    }
                  ]
                }
                """;
        JsonNode root = putJson("/api/leaves/%d/resubmit".formatted(leave1Id), body);
        assertCodeOk(root);
    }

    @Test @Order(5)
    void step5_counselorPendingListAgain() throws Exception {
        JsonNode root = getJson("/api/leaves/pending/counselor?counselorId=1");
        assertCodeOk(root);
    }

    @Test @Order(6)
    void step6_leave1Detail_shouldHaveTimeline() throws Exception {
        JsonNode root = getJson("/api/leaves/%d/detail".formatted(leave1Id));
        assertCodeOk(root);
    }

    @Test @Order(7)
    void step7_counselorAgreeLeave1() throws Exception {
        String body = """
                {
                  "counselorId": 1,
                  "action": "AGREE",
                  "comment": "同意请假，注意身体"
                }
                """;
        JsonNode root = postJson("/api/leaves/%d/counselor-approve".formatted(leave1Id), body);
        assertCodeOk(root);
    }

    @Test @Order(8)
    void step8_teacherPendingImpacts_extractImpactId() throws Exception {
        JsonNode root = getJson("/api/leaves/pending/teacher?teacherId=2");
        assertCodeOk(root);

        impactIdForLeave1 = findFirstIdInDataArray(root, java.util.List.of("impactId", "id"));
        Assertions.assertTrue(impactIdForLeave1 > 0);
    }

    @Test @Order(9)
    void step9_teacherConfirmImpact() throws Exception {
        String body = """
                {
                  "teacherId": 2,
                  "remark": "已收到学生请假说明，确认缺勤"
                }
                """;
        JsonNode root = postJson("/api/leaves/impact/%d/teacher-confirm".formatted(impactIdForLeave1), body);
        assertCodeOk(root);
    }

    @Test @Order(10)
    void step10_studentMyLeaves_shouldBeApproved() throws Exception {
        JsonNode root = getJson("/api/leaves/my?studentId=1");
        assertCodeOk(root);
    }

    @Test @Order(11)
    void step11_studentTimetableDay() throws Exception {
        JsonNode root = getJson("/api/timetable/student/day?studentId=1&date=2024-10-10");
        assertCodeOk(root);
    }

    @Test @Order(12)
    void step12_leave1Detail_shouldBeApproved() throws Exception {
        JsonNode root = getJson("/api/leaves/%d/detail".formatted(leave1Id));
        assertCodeOk(root);
        assertStatusEquals(root, "APPROVED");
    }

    @Test @Order(13)
    void step13_applyLeave2_forRejectBranch() throws Exception {
        String body = """
                {
                  "studentId": 1,
                  "termId": 1,
                  "leaveType": "AFFAIR",
                  "applyChannel": "BY_SELF",
                  "reason": "临时想回家休息一天",
                  "proofUrl": null,
                  "startTime": "2024-10-15T08:00:00",
                  "endTime": "2024-10-15T18:00:00",
                  "impacts": [
                    {
                      "offeringId": 1,
                      "courseDate": "2024-10-15",
                      "sectionStart": 1,
                      "sectionEnd": 2
                    }
                  ]
                }
                """;
        JsonNode root = postJson("/api/leaves/apply", body);
        assertCodeOk(root);
        leave2Id = extractDataLong(root, "leaveId");
        Assertions.assertTrue(leave2Id > 0);
    }

    @Test @Order(14)
    void step14_counselorRejectLeave2() throws Exception {
        String body = """
                {
                  "counselorId": 1,
                  "action": "REJECT",
                  "comment": "理由不充分，驳回"
                }
                """;
        JsonNode root = postJson("/api/leaves/%d/counselor-approve".formatted(leave2Id), body);
        assertCodeOk(root);
    }

    @Test @Order(15)
    void step15_leave2Detail_shouldBeRejected() throws Exception {
        JsonNode root = getJson("/api/leaves/%d/detail".formatted(leave2Id));
        assertCodeOk(root);
        assertStatusEquals(root, "REJECTED");
    }

    @Test @Order(16)
    void step16_applyLeave3_forReturnNoResubmit() throws Exception {
        String body = """
                {
                  "studentId": 1,
                  "termId": 1,
                  "leaveType": "PUBLIC",
                  "applyChannel": "BY_SELF",
                  "reason": "参加校级竞赛，请假一天",
                  "proofUrl": null,
                  "startTime": "2024-11-01T08:00:00",
                  "endTime": "2024-11-01T18:00:00",
                  "impacts": [
                    {
                      "offeringId": 1,
                      "courseDate": "2024-11-01",
                      "sectionStart": 1,
                      "sectionEnd": 2
                    }
                  ]
                }
                """;
        JsonNode root = postJson("/api/leaves/apply", body);
        assertCodeOk(root);
        leave3Id = extractDataLong(root, "leaveId");
        Assertions.assertTrue(leave3Id > 0);
    }

    @Test @Order(17)
    void step17_counselorReturnLeave3_noResubmit() throws Exception {
        String body = """
                {
                  "counselorId": 1,
                  "action": "RETURN",
                  "comment": "请补充竞赛通知截图后再提交"
                }
                """;
        JsonNode root = postJson("/api/leaves/%d/counselor-approve".formatted(leave3Id), body);
        assertCodeOk(root);
    }

    @Test @Order(18)
    void step18_leave3Detail_shouldBeReturned() throws Exception {
        JsonNode root = getJson("/api/leaves/%d/detail".formatted(leave3Id));
        assertCodeOk(root);
        assertStatusEquals(root, "RETURNED");
    }
}
