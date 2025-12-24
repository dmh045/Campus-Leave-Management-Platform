package com.example.leavesystem.service;

import com.example.leavesystem.dto.CounselorPendingLeaveDTO;
import com.example.leavesystem.dto.LeaveApplyRequest;
import com.example.leavesystem.dto.LeaveApplyResponse;
import com.example.leavesystem.dto.LeaveDetailDTO;
import com.example.leavesystem.dto.TeacherPendingImpactDTO;
import com.example.leavesystem.dto.ClassLeaveStatsDTO;
import com.example.leavesystem.dto.PublicLeaveBatchRequest;
import com.example.leavesystem.entity.LeaveRequest;
import com.example.leavesystem.dto.TeacherCoursePendingDTO;


import java.time.LocalDate;
import java.util.List;

public interface LeaveService {

    // 学生发起请假
    LeaveApplyResponse applyLeave(LeaveApplyRequest request);

    // 学生重新提交被退回的请假单
    LeaveApplyResponse resubmitLeave(Long leaveId, LeaveApplyRequest request);

    // 辅导员查看待审批列表
    List<CounselorPendingLeaveDTO> listPendingForCounselor(Long counselorId);

    // 辅导员单条审批：action = "AGREE" / "REJECT" / "RETURN"
    void counselorApprove(Long leaveId, Long counselorId, String comment, String action);

    // 辅导员批量审批
    void counselorBatchApprove(Long counselorId, String action, String comment, List<Long> leaveIds);

    // 辅导员批量发起公假（返回生成的 leaveId 列表）
    List<Long> createPublicLeaves(PublicLeaveBatchRequest request);

    // 老师查看自己的待确认节次
    List<TeacherPendingImpactDTO> listPendingForTeacher(Long teacherId);

    // 老师确认某一节次的请假
    void teacherConfirmImpact(Long impactId, Long teacherId, String remark);

    // 任课教师端：按课程维度聚合的待确认请假列表
    List<TeacherCoursePendingDTO> listPendingForTeacherByCourse(Long teacherId);

    // 学生查看自己的请假列表
    List<LeaveRequest> listLeavesForStudent(Long studentId);

    // 班级请假统计
    ClassLeaveStatsDTO getClassLeaveStats(Long classId, LocalDate startDate, LocalDate endDate);

    // 请假详情（含节次和审批时间线）
    LeaveDetailDTO getLeaveDetail(Long leaveId);

}
