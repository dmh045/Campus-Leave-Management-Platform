// AbsenceApiTest.java
package com.example.leavesystem;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.*;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AbsenceApiTest extends BaseApiIntegrationTest {

    // 可用 -D 覆盖
    private final long teacherId  = Long.parseLong(System.getProperty("test.teacherId", "2"));
    private final long offeringId = Long.parseLong(System.getProperty("test.offeringId", "1"));
    private final long termId     = Long.parseLong(System.getProperty("test.termId", "1"));

    private long loginStaffId = -1;
    private long presentStudentId = Long.parseLong(System.getProperty("test.presentStudentId", "1"));
    private long absentStudentId  = Long.parseLong(System.getProperty("test.absentStudentId", "2"));

    private long absenceToConvertId;
    private long absenceToConfirmId;

    @BeforeAll
    void loginAndPrepare() throws Exception {
        // 1) 登录拿 token（你给的账号）
        String loginBody = """
            {"loginType":"STAFF","username":"T20250034","password":"250034"}
            """;

        JsonNode loginRoot = postJson("/api/auth/login", loginBody);
        assertCodeOk(loginRoot);

        String token = loginRoot.at("/data/token").asText();
        Assertions.assertFalse(token == null || token.isBlank(), "登录成功但 data.token 为空，resp=" + loginRoot);

        // 2) 让 Base 每次请求自动带 Authorization
        setAuth("Authorization", "Bearer " + token);

        // 3) 尝试从登录返回里取出 staffId/userId（用于 confirm 参数兜底）
        loginStaffId = tryExtractFirstLong(loginRoot.at("/data"), List.of("staffId", "userId", "id"));

        // 4) 如果有 DB，尽量把“要用的学生”换成确实选了这门课的两个人，避免“未选课/不属于本班”等业务拦截
        if (jdbcTemplate != null) {
            long[] pair = tryPickTwoStudentsFromOffering(offeringId);
            if (pair != null) {
                presentStudentId = pair[0];
                absentStudentId = pair[1];
            }

            // 5) 兜底：补齐可能会被当作 approver 的 staff 记录，避免 fk_approval_approver
            ensureStaffRowExists(teacherId, "TEACHER");
            if (loginStaffId > 0) ensureStaffRowExists(loginStaffId, "TEACHER");

            //    如果 student 表里有 counselor_id/ advisor_id 等字段，也顺手补齐（没有该字段则忽略）
            Long counselorId = tryQueryOneLong("SELECT counselor_id FROM student WHERE student_id=?", absentStudentId);
            if (counselorId != null && counselorId > 0) ensureStaffRowExists(counselorId, "COUNSELOR");
        }
    }

    @Test @Order(35)
    void step35_createPendingMakeupAbsence_forConvert() throws Exception {
        long beforeMaxAbsenceId = sqlMaxAbsenceId();

        String courseDate = LocalDate.now().toString();
        JsonNode startRoot = postJson("/api/attendance/session/start", """
                {
                  "teacherId": %d,
                  "offeringId": %d,
                  "courseDate": "%s",
                  "sectionStart": 1,
                  "sectionEnd": 2,
                  "durationMinutes": 1
                }
                """.formatted(teacherId, offeringId, courseDate));
        assertCodeOk(startRoot);

        String token = extractDataText(startRoot, "token");
        String allowStartTime = startRoot.at("/data/allowStartTime").asText(null);

        // 等到允许签到再发请求，避免“窗口未到”
        waitUntilAllowStart(allowStartTime);

        // 只签到一个人，让另一个人变成缺勤（生成 PENDING_MAKEUP）
        JsonNode checkinRoot = checkinWithRetry(presentStudentId, token, allowStartTime);
        assertCodeOk(checkinRoot);

        long sessionId = extractDataLong(startRoot, "sessionId");
        JsonNode closeRoot = postJson("/api/attendance/session/%d/close?teacherId=%d".formatted(sessionId, teacherId), "{}");
        assertCodeOk(closeRoot);

        absenceToConvertId = findNewAbsenceId(beforeMaxAbsenceId, absentStudentId, "PENDING_MAKEUP");
        Assertions.assertTrue(absenceToConvertId > 0,
                "未能在关闭考勤后生成 PENDING_MAKEUP 缺勤记录（可能是：缺勤状态枚举不同 / 该课程不允许补签 / 学生未选课）。");
    }

    @Test @Order(36)
    void step36_convertToLeave_shouldSuccess() throws Exception {
        String today = LocalDate.now().toString();
        String body = """
                {
                  "studentId": %d,
                  "termId": %d,
                  "leaveType": "AFFAIR",
                  "reason": "事后补假：老师已同意，请录入系统",
                  "proofUrl": null,
                  "startTime": "%sT08:00:00",
                  "endTime": "%sT18:00:00"
                }
                """.formatted(absentStudentId, termId, today, today);

        JsonNode root = postJson("/api/absences/%d/convert-to-leave".formatted(absenceToConvertId), body);

        // fk_approval_approver：通常是 staff 表缺少被引用的 approver_id
        // 做一次自动修复后重试（不影响正常情况）
        if (!isCodeOk(root) && isFkApproverError(root) && jdbcTemplate != null) {
            ensureStaffRowExists(teacherId, "TEACHER");
            if (loginStaffId > 0) ensureStaffRowExists(loginStaffId, "TEACHER");
            root = postJson("/api/absences/%d/convert-to-leave".formatted(absenceToConvertId), body);
        }

        assertCodeOk(root);

        if (jdbcTemplate != null) {
            Assertions.assertEquals("CONVERTED_TO_LEAVE", sqlGetAbsenceStatus(absenceToConvertId));
            Assertions.assertNotNull(sqlGetConvertedLeaveId(absenceToConvertId));
        }
    }

    @Test @Order(37)
    void step37_convertToLeave_again_shouldFail() throws Exception {
        String today = LocalDate.now().toString();
        String body = """
                {
                  "studentId": %d,
                  "termId": %d,
                  "leaveType": "AFFAIR",
                  "reason": "重复提交测试",
                  "proofUrl": null,
                  "startTime": "%sT08:00:00",
                  "endTime": "%sT18:00:00"
                }
                """.formatted(absentStudentId, termId, today, today);

        JsonNode root = postJson("/api/absences/%d/convert-to-leave".formatted(absenceToConvertId), body);
        assertCode(root, -1);
    }

    @Test @Order(38)
    void step38_createAnotherPendingMakeupAbsence_forConfirm() throws Exception {
        long beforeMaxAbsenceId = sqlMaxAbsenceId();

        String courseDate = LocalDate.now().plusDays(1).toString();
        JsonNode startRoot = postJson("/api/attendance/session/start", """
                {
                  "teacherId": %d,
                  "offeringId": %d,
                  "courseDate": "%s",
                  "sectionStart": 1,
                  "sectionEnd": 2,
                  "durationMinutes": 1
                }
                """.formatted(teacherId, offeringId, courseDate));
        assertCodeOk(startRoot);

        String token = extractDataText(startRoot, "token");
        String allowStartTime = startRoot.at("/data/allowStartTime").asText(null);
        waitUntilAllowStart(allowStartTime);

        JsonNode checkinRoot = checkinWithRetry(presentStudentId, token, allowStartTime);
        assertCodeOk(checkinRoot);

        long sessionId = extractDataLong(startRoot, "sessionId");
        JsonNode closeRoot = postJson("/api/attendance/session/%d/close?teacherId=%d".formatted(sessionId, teacherId), "{}");
        assertCodeOk(closeRoot);

        absenceToConfirmId = findNewAbsenceId(beforeMaxAbsenceId, absentStudentId, "PENDING_MAKEUP");
        Assertions.assertTrue(absenceToConfirmId > 0, "未能生成 confirm 用的 PENDING_MAKEUP 缺勤记录。");
    }

    @Test @Order(39)
    void step39_confirmAbsence_shouldSuccess() throws Exception {
        long staffIdForConfirm = (loginStaffId > 0 ? loginStaffId : teacherId);

        JsonNode root = postJson("/api/absences/%d/confirm?staffId=%d".formatted(absenceToConfirmId, staffIdForConfirm), "{}");
        assertCodeOk(root);

        if (jdbcTemplate != null) {
            Assertions.assertEquals("CONFIRMED", sqlGetAbsenceStatus(absenceToConfirmId));
        }
    }

    @Test @Order(40)
    void step40_confirmAbsence_again_shouldFail() throws Exception {
        long staffIdForConfirm = (loginStaffId > 0 ? loginStaffId : teacherId);

        JsonNode root = postJson("/api/absences/%d/confirm?staffId=%d".formatted(absenceToConfirmId, staffIdForConfirm), "{}");
        assertCode(root, -1);

        if (jdbcTemplate != null) {
            Assertions.assertEquals("CONFIRMED", sqlGetAbsenceStatus(absenceToConfirmId));
        }
    }

    // ---------------- helpers ----------------

    private JsonNode checkinWithRetry(long studentId, String token, String allowStartTime) throws Exception {
        JsonNode last = null;
        for (int i = 0; i < 4; i++) {
            last = postJson("/api/attendance/checkin", """
                    {"studentId": %d, "token": "%s"}
                    """.formatted(studentId, token));

            if (isCodeOk(last)) return last;

            String msg = last.path("msg").asText("");
            if (msg.isBlank()) msg = last.path("message").asText("");

            // 只对“窗口问题”做重试，其它业务错误直接返回给断言
            if (msg.contains("签到时间窗口") || msg.contains("不在签到时间窗口内")) {
                waitUntilAllowStart(allowStartTime);
                Thread.sleep(200);
                continue;
            }
            return last;
        }
        return last;
    }

    private boolean isCodeOk(JsonNode root) {
        return root.has("code") && root.get("code").asInt() == 0;
    }

    private boolean isFkApproverError(JsonNode root) {
        String msg = root.path("msg").asText("");
        if (msg.isBlank()) msg = root.path("message").asText("");
        if (msg.isBlank()) msg = root.path("data").path("message").asText("");
        return msg.contains("fk_approval_approver") || msg.contains("fk_approval") || msg.contains("approval");
    }

    private long tryExtractFirstLong(JsonNode obj, List<String> fields) {
        if (obj == null || obj.isMissingNode()) return -1;
        for (String f : fields) {
            JsonNode n = obj.get(f);
            if (n != null && n.isIntegralNumber()) return n.asLong();
        }
        return -1;
    }

    private void waitUntilAllowStart(String allowStartTime) {
        if (allowStartTime == null || allowStartTime.isBlank()) return;

        LocalDateTime t;
        try {
            t = LocalDateTime.parse(allowStartTime);
        } catch (Exception ignore) {
            return;
        }

        long ms = Duration.between(LocalDateTime.now(), t).toMillis();
        if (ms > 0) {
            try {
                Thread.sleep(ms + 250); // buffer，减少边界抖动
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private long sqlMaxAbsenceId() {
        if (jdbcTemplate == null) return 0;
        return jdbcTemplate.query(
                "SELECT COALESCE(MAX(absence_id),0) FROM absence",
                rs -> rs.next() ? rs.getLong(1) : 0L
        );
    }

    private long findNewAbsenceId(long afterAbsenceId, long studentId, String status) {
        if (jdbcTemplate == null) {
            Long id = sqlFindLatestAbsenceIdByStatus(status);
            return id == null ? 0 : id;
        }

        return jdbcTemplate.query(
                "SELECT absence_id FROM absence WHERE absence_id>? AND student_id=? AND status=? ORDER BY absence_id DESC LIMIT 1",
                ps -> {
                    ps.setLong(1, afterAbsenceId);
                    ps.setLong(2, studentId);
                    ps.setString(3, status);
                },
                rs -> rs.next() ? rs.getLong(1) : 0L
        );
    }

    private long[] tryPickTwoStudentsFromOffering(long offeringId) {
        try {
            List<Long> ids = jdbcTemplate.query(
                    "SELECT student_id FROM enrollment WHERE offering_id=? LIMIT 2",
                    ps -> ps.setLong(1, offeringId),
                    (rs, rowNum) -> rs.getLong(1)
            );
            if (ids != null && ids.size() >= 2) return new long[]{ids.get(0), ids.get(1)};
        } catch (Exception ignore) { }
        return null;
    }

    private Long tryQueryOneLong(String sql, Object... args) {
        if (jdbcTemplate == null) return null;
        try {
            return jdbcTemplate.query(sql, ps -> {
                for (int i = 0; i < args.length; i++) ps.setObject(i + 1, args[i]);
            }, rs -> rs.next() ? (rs.getObject(1) == null ? null : rs.getLong(1)) : null);
        } catch (Exception ignore) {
            return null;
        }
    }

    /**
     * 如果 staff 表里没有某个 staff_id，就动态补一条最小可插入记录。
     * 目的是避免 approval.approver_id 外键约束失败（你日志里的 fk_approval_approver）。
     */
    private void ensureStaffRowExists(long staffId, String roleGuess) {
        if (jdbcTemplate == null) return;

        Integer cnt = jdbcTemplate.query(
                "SELECT COUNT(*) FROM staff WHERE staff_id=?",
                ps -> ps.setLong(1, staffId),
                rs -> rs.next() ? rs.getInt(1) : 0
        );
        if (cnt != null && cnt > 0) return;

        Map<String, RefFk> fkMap = loadStaffFkMap();

        List<Map<String, Object>> cols = jdbcTemplate.queryForList(
                """
                SELECT column_name, is_nullable, column_default, extra, data_type, column_type, column_key
                FROM information_schema.columns
                WHERE table_schema = DATABASE() AND table_name = 'staff'
                ORDER BY ordinal_position
                """
        );

        List<String> insertCols = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        for (Map<String, Object> c : cols) {
            String name = String.valueOf(c.get("column_name"));
            String isNullable = String.valueOf(c.get("is_nullable"));
            Object colDefault = c.get("column_default");
            String extra = String.valueOf(c.get("extra"));
            String dataType = String.valueOf(c.get("data_type"));
            String colType = String.valueOf(c.get("column_type"));
            String colKey = String.valueOf(c.get("column_key"));

            boolean autoInc = extra != null && extra.toLowerCase(Locale.ROOT).contains("auto_increment");
            boolean mustProvide = "NO".equalsIgnoreCase(isNullable) && colDefault == null && !autoInc;

            // 主键（或 staff_id）一定要提供
            if ("staff_id".equalsIgnoreCase(name) || "PRI".equalsIgnoreCase(colKey)) {
                insertCols.add(name);
                values.add(staffId);
                continue;
            }

            if (!mustProvide) continue;

            insertCols.add(name);

            // 如果该列是外键，优先拿被引用表里真实存在的一条 id
            RefFk ref = fkMap.get(name.toLowerCase(Locale.ROOT));
            if (ref != null) {
                Long refVal = tryQueryOneLong("SELECT " + ref.refColumn + " FROM " + ref.refTable + " LIMIT 1");
                if (refVal != null) {
                    values.add(refVal);
                    continue;
                }
            }

            values.add(guessValueForStaffColumn(name, dataType, colType, roleGuess, staffId));
        }

        if (insertCols.isEmpty()) {
            jdbcTemplate.update("INSERT INTO staff(staff_id) VALUES(?)", staffId);
            return;
        }

        String placeholders = String.join(",", Collections.nCopies(insertCols.size(), "?"));
        String sql = "INSERT INTO staff(" + String.join(",", insertCols) + ") VALUES(" + placeholders + ")";
        jdbcTemplate.update(sql, values.toArray());
    }

    private Map<String, RefFk> loadStaffFkMap() {
        Map<String, RefFk> map = new HashMap<>();
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    """
                    SELECT column_name, referenced_table_name, referenced_column_name
                    FROM information_schema.key_column_usage
                    WHERE table_schema = DATABASE()
                      AND table_name = 'staff'
                      AND referenced_table_name IS NOT NULL
                    """
            );
            for (Map<String, Object> r : rows) {
                String col = String.valueOf(r.get("column_name")).toLowerCase(Locale.ROOT);
                String rt = String.valueOf(r.get("referenced_table_name"));
                String rc = String.valueOf(r.get("referenced_column_name"));
                map.put(col, new RefFk(rt, rc));
            }
        } catch (Exception ignore) { }
        return map;
    }

    private static class RefFk {
        final String refTable;
        final String refColumn;
        RefFk(String refTable, String refColumn) {
            this.refTable = refTable;
            this.refColumn = refColumn;
        }
    }

    private Object guessValueForStaffColumn(String colName, String dataType, String colType, String roleGuess, long staffId) {
        String n = colName.toLowerCase(Locale.ROOT);

        if (n.contains("no") || n.contains("code") || n.contains("number")
                || n.contains("username") || n.contains("account")) {
            return "AUTO" + staffId;
        }
        if (n.contains("name")) {
            return "AutoStaff" + staffId;
        }
        if (n.contains("phone") || n.contains("mobile")) {
            return "1380000" + String.format("%04d", staffId % 10000);
        }
        if (n.contains("email")) {
            return "auto" + staffId + "@example.com";
        }
        if (n.contains("role") || n.contains("type")) {
            if (colType != null && colType.toLowerCase(Locale.ROOT).startsWith("enum(")) {
                List<String> enums = parseEnumValues(colType);
                if (enums.contains(roleGuess)) return roleGuess;
                if (!enums.isEmpty()) return enums.get(0);
            }
            return roleGuess;
        }
        if (n.contains("password") || n.contains("pwd")) {
            return "test123456";
        }

        String t = (dataType == null ? "" : dataType.toLowerCase(Locale.ROOT));

        // enum 但不是 role/type 的情况：取第一个值兜底
        if ("enum".equals(t) && colType != null) {
            List<String> enums = parseEnumValues(colType);
            return enums.isEmpty() ? "AUTO" : enums.get(0);
        }

        if (t.contains("int") || t.equals("decimal") || t.equals("double") || t.equals("float")) return 0;
        if (t.equals("bit") || t.equals("boolean")) return 0;

        if (t.equals("date")) return Date.valueOf(LocalDate.now());
        if (t.equals("time")) return Time.valueOf(LocalDateTime.now().toLocalTime());
        if (t.contains("datetime") || t.contains("timestamp")) return Timestamp.valueOf(LocalDateTime.now());
        if (t.contains("char") || t.contains("text")) return "AUTO";

        return "AUTO";
    }

    private List<String> parseEnumValues(String colType) {
        List<String> out = new ArrayList<>();
        if (colType == null) return out;
        Matcher m = Pattern.compile("'([^']*)'").matcher(colType);
        while (m.find()) out.add(m.group(1));
        return out;
    }
}
