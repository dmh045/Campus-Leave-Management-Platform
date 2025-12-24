package com.example.leavesystem.service.impl;

import com.example.leavesystem.dto.CounselorPendingLeaveDTO;
import com.example.leavesystem.dto.LeaveApplyRequest;
import com.example.leavesystem.dto.LeaveApplyResponse;
import com.example.leavesystem.dto.TeacherPendingImpactDTO;
import com.example.leavesystem.dto.LeaveDetailDTO;
import com.example.leavesystem.dto.ClassLeaveStatsDTO;
import com.example.leavesystem.dto.TeacherCoursePendingDTO;
import com.example.leavesystem.dto.PublicLeaveBatchRequest;
import com.example.leavesystem.entity.Approval;
import com.example.leavesystem.entity.Clazz;
import com.example.leavesystem.entity.Course;
import com.example.leavesystem.entity.LeaveImpact;
import com.example.leavesystem.entity.LeaveRequest;
import com.example.leavesystem.entity.Offering;
import com.example.leavesystem.entity.Staff;
import com.example.leavesystem.entity.Student;
import com.example.leavesystem.entity.Term;
import com.example.leavesystem.mapper.*;
import com.example.leavesystem.service.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveServiceImpl implements LeaveService {

    private final StudentMapper studentMapper;
    private final TermMapper termMapper;
    private final OfferingMapper offeringMapper;
    private final LeaveRequestMapper leaveRequestMapper;
    private final LeaveImpactMapper leaveImpactMapper;
    private final CounselorLeaveQueryMapper counselorLeaveQueryMapper;
    private final ApprovalMapper approvalMapper;
    private final TeacherLeaveQueryMapper teacherLeaveQueryMapper;
    private final ClazzMapper clazzMapper;
    private final StaffMapper staffMapper;
    private final CourseMapper courseMapper;

    /**
     * 学生发起请假
     */
    @Override
    @Transactional
    public LeaveApplyResponse applyLeave(LeaveApplyRequest request) {
        // 1. 基本参数校验
        if (request.getStudentId() == null || request.getTermId() == null) {
            throw new IllegalArgumentException("studentId 和 termId 不能为空");
        }
        if (request.getStartTime() == null || request.getEndTime() == null
                || request.getStartTime().isAfter(request.getEndTime())) {
            throw new IllegalArgumentException("请假时间段不合法");
        }

        // 2. 校验学生/学期存在
        Student student = studentMapper.findById(request.getStudentId());
        if (student == null) {
            throw new IllegalArgumentException("学生不存在");
        }
        Term term = termMapper.findById(request.getTermId());
        if (term == null) {
            throw new IllegalArgumentException("学期不存在");
        }

        // 3. 写入 leave_request
        LeaveRequest leave = new LeaveRequest();
        leave.setStudentId(request.getStudentId());
        leave.setTermId(request.getTermId());
        leave.setLeaveType(request.getLeaveType());
        leave.setApplyChannel(request.getApplyChannel());
        leave.setReason(request.getReason());
        leave.setProofUrl(request.getProofUrl());
        leave.setStartTime(request.getStartTime());
        leave.setEndTime(request.getEndTime());
        // 初始状态：待辅导员审批
        leave.setStatus("PENDING_COUNSELOR");

        leaveRequestMapper.insert(leave);
        Long leaveId = leave.getLeaveId();

        // 4. 写入 leave_impact
        if (request.getImpacts() != null) {
            for (LeaveApplyRequest.ImpactItem item : request.getImpacts()) {
                if (item.getOfferingId() == null) {
                    continue;
                }

                Offering offering = offeringMapper.findById(item.getOfferingId());
                if (offering == null) {
                    throw new IllegalArgumentException("开课记录不存在，id=" + item.getOfferingId());
                }

                LeaveImpact impact = new LeaveImpact();
                impact.setLeaveId(leaveId);
                impact.setOfferingId(item.getOfferingId());
                impact.setCourseDate(item.getCourseDate());
                impact.setSectionStart(item.getSectionStart());
                impact.setSectionEnd(item.getSectionEnd());
                impact.setTeacherId(offering.getTeacherId());
                impact.setConfirmStatus("PENDING");
                impact.setConfirmTime(null);

                leaveImpactMapper.insert(impact);
            }
        }

        return new LeaveApplyResponse(leaveId, leave.getStatus());
    }

    /**
     * 辅导员待审批列表
     */
    @Override
    public List<CounselorPendingLeaveDTO> listPendingForCounselor(Long counselorId) {
        return counselorLeaveQueryMapper.findPendingByCounselor(counselorId);
    }

    /**
     * 辅导员单条审批：AGREE / REJECT / RETURN
     * 加入并发安全：只有当前状态仍为 PENDING_COUNSELOR 时才更新成功
     */
    @Override
    @Transactional
    public void counselorApprove(Long leaveId, Long counselorId, String comment, String action) {
        LeaveRequest leave = leaveRequestMapper.findById(leaveId);
        if (leave == null) {
            throw new IllegalArgumentException("请假单不存在");
        }
        if (!"PENDING_COUNSELOR".equals(leave.getStatus())) {
            throw new IllegalStateException("当前状态不允许辅导员审批");
        }

        String normalized = action == null ? "" : action.toUpperCase();
        String newStatus;
        switch (normalized) {
            case "AGREE":
                newStatus = "PENDING_TEACHER";
                break;
            case "REJECT":
                newStatus = "REJECTED";
                break;
            case "RETURN":
                newStatus = "RETURNED";
                break;
            default:
                throw new IllegalArgumentException("未知的审批动作: " + action);
        }

        // 并发安全更新：只有当前还是 PENDING_COUNSELOR 才会真正更新
        int rows = leaveRequestMapper.updateStatusIfCurrent(
                leaveId,
                "PENDING_COUNSELOR",
                newStatus
        );
        if (rows == 0) {
            // 说明已经被别的并发请求处理过
            throw new IllegalStateException("该请假单已被其他人处理，请刷新后重试");
        }

        // 写审批记录
        Approval approval = new Approval();
        approval.setLeaveId(leaveId);
        approval.setApproverId(counselorId);
        approval.setApproverRole("COUNSELOR");
        approval.setAction(normalized);
        approval.setComment(comment);
        approval.setCreatedAt(LocalDateTime.now());
        approvalMapper.insert(approval);
    }

    /**
     * 学生重新提交被退回的请假单
     */
    @Override
    @Transactional
    public LeaveApplyResponse resubmitLeave(Long leaveId, LeaveApplyRequest request) {
        // 0. 基本校验
        if (leaveId == null) {
            throw new IllegalArgumentException("leaveId 不能为空");
        }
        if (request.getStudentId() == null || request.getTermId() == null) {
            throw new IllegalArgumentException("studentId 和 termId 不能为空");
        }
        if (request.getStartTime() == null || request.getEndTime() == null
                || request.getStartTime().isAfter(request.getEndTime())) {
            throw new IllegalArgumentException("请假时间段不合法");
        }

        // 1. 查原始请假单
        LeaveRequest leave = leaveRequestMapper.findById(leaveId);
        if (leave == null) {
            throw new IllegalArgumentException("请假单不存在");
        }

        // 1.1 只能本人修改
        if (!request.getStudentId().equals(leave.getStudentId())) {
            throw new IllegalStateException("无权修改他人的请假单");
        }

        // 1.2 只能在 RETURNED 状态下重新提交
        if (!"RETURNED".equals(leave.getStatus())) {
            throw new IllegalStateException("当前状态不允许重新提交（仅 RETURNED 可编辑）");
        }

        // 2. 校验学生 / 学期存在（跟 applyLeave 同样逻辑）
        Student student = studentMapper.findById(request.getStudentId());
        if (student == null) {
            throw new IllegalArgumentException("学生不存在");
        }
        Term term = termMapper.findById(request.getTermId());
        if (term == null) {
            throw new IllegalArgumentException("学期不存在");
        }

        // 3. 更新主表内容
        leave.setTermId(request.getTermId());
        leave.setLeaveType(request.getLeaveType());
        // 保持申请渠道：如果前端带 applyChannel 就用新的，否则保留原值
        if (request.getApplyChannel() != null) {
            leave.setApplyChannel(request.getApplyChannel());
        }
        leave.setReason(request.getReason());
        leave.setProofUrl(request.getProofUrl());
        leave.setStartTime(request.getStartTime());
        leave.setEndTime(request.getEndTime());
        // 重新提交后，状态回到辅导员待审批
        leave.setStatus("PENDING_COUNSELOR");

        leaveRequestMapper.updateForStudentEdit(leave);

        // 4. 清空旧的受影响节次，按新选择重新生成
        leaveImpactMapper.deleteByLeaveId(leaveId);

        if (request.getImpacts() != null) {
            for (LeaveApplyRequest.ImpactItem item : request.getImpacts()) {
                if (item.getOfferingId() == null) {
                    continue;
                }
                Offering offering = offeringMapper.findById(item.getOfferingId());
                if (offering == null) {
                    throw new IllegalArgumentException("开课记录不存在，id=" + item.getOfferingId());
                }

                LeaveImpact impact = new LeaveImpact();
                impact.setLeaveId(leaveId);
                impact.setOfferingId(item.getOfferingId());
                impact.setCourseDate(item.getCourseDate());
                impact.setSectionStart(item.getSectionStart());
                impact.setSectionEnd(item.getSectionEnd());
                impact.setTeacherId(offering.getTeacherId());
                impact.setConfirmStatus("PENDING");
                impact.setConfirmTime(null);
                impact.setRemark(null);

                leaveImpactMapper.insert(impact);
            }
        }

        // 5. 返回结果（还是原来的 leaveId，只是状态变了）
        return new LeaveApplyResponse(leaveId, leave.getStatus());
    }

    /**
     * 辅导员批量审批
     */
    @Override
    @Transactional
    public void counselorBatchApprove(Long counselorId, String action, String comment, List<Long> leaveIds) {
        if (leaveIds == null || leaveIds.isEmpty()) {
            return;
        }
        for (Long id : leaveIds) {
            counselorApprove(id, counselorId, comment, action);
        }
    }

    /**
     * 辅导员批量发起公假
     */
    @Override
    @Transactional
    public List<Long> createPublicLeaves(PublicLeaveBatchRequest request) {
        if (request.getStudentIds() == null || request.getStudentIds().isEmpty()) {
            throw new IllegalArgumentException("studentIds 不能为空");
        }
        if (request.getTermId() == null) {
            throw new IllegalArgumentException("termId 不能为空");
        }

        // 校验学期
        Term term = termMapper.findById(request.getTermId());
        if (term == null) {
            throw new IllegalArgumentException("学期不存在");
        }

        List<Long> resultIds = new ArrayList<>();

        for (Long studentId : request.getStudentIds()) {
            Student student = studentMapper.findById(studentId);
            if (student == null) {
                throw new IllegalArgumentException("学生不存在: " + studentId);
            }

            // 1) 主表写入
            LeaveRequest leave = new LeaveRequest();
            leave.setStudentId(studentId);
            leave.setTermId(request.getTermId());
            leave.setLeaveType("PUBLIC");                 // 公假
            leave.setApplyChannel("COUNSELOR_PUBLIC");    // 由辅导员发起
            leave.setReason(request.getReason());
            leave.setProofUrl(null);
            leave.setStartTime(request.getStartTime());
            leave.setEndTime(request.getEndTime());
            // 直接进入老师确认环节
            leave.setStatus("PENDING_TEACHER");

            leaveRequestMapper.insert(leave);
            Long leaveId = leave.getLeaveId();
            resultIds.add(leaveId);

            // 2) 影响节次（对所有学生相同）
            if (request.getImpacts() != null) {
                for (PublicLeaveBatchRequest.ImpactItem item : request.getImpacts()) {
                    if (item.getOfferingId() == null) {
                        continue;
                    }

                    Offering offering = offeringMapper.findById(item.getOfferingId());
                    if (offering == null) {
                        throw new IllegalArgumentException("开课记录不存在: " + item.getOfferingId());
                    }

                    LeaveImpact impact = new LeaveImpact();
                    impact.setLeaveId(leaveId);
                    impact.setOfferingId(item.getOfferingId());
                    impact.setCourseDate(item.getCourseDate());
                    impact.setSectionStart(item.getSectionStart());
                    impact.setSectionEnd(item.getSectionEnd());
                    impact.setTeacherId(offering.getTeacherId());
                    impact.setConfirmStatus("PENDING");
                    impact.setConfirmTime(null);
                    impact.setRemark(null);

                    leaveImpactMapper.insert(impact);
                }
            }

            // 3) 写辅导员“同意”记录（视为已同意）
            Approval approval = new Approval();
            approval.setLeaveId(leaveId);
            approval.setApproverId(request.getCounselorId());
            approval.setApproverRole("COUNSELOR");
            approval.setAction("AGREE");
            approval.setComment("公假由辅导员发起: " + request.getReason());
            approval.setCreatedAt(LocalDateTime.now());
            approvalMapper.insert(approval);
        }

        return resultIds;
    }

    /**
     * 老师查看自己的待确认节次
     */
    @Override
    public List<TeacherPendingImpactDTO> listPendingForTeacher(Long teacherId) {
        return teacherLeaveQueryMapper.findPendingByTeacher(teacherId);
    }

    /**
     * 老师确认某一节次的请假
     * 加入并发安全：只有当前 confirm_status 仍为 PENDING 时才更新成功，
     * 且最后一个节次确认时，主单从 PENDING_TEACHER → APPROVED 也使用乐观锁。
     */
    @Override
    @Transactional
    public void teacherConfirmImpact(Long impactId, Long teacherId, String remark) {
        LeaveImpact impact = leaveImpactMapper.findById(impactId);
        if (impact == null) {
            throw new IllegalArgumentException("影响记录不存在");
        }
        if (!teacherId.equals(impact.getTeacherId())) {
            throw new IllegalStateException("无权确认其他老师的课程");
        }
        if ("CONFIRMED".equals(impact.getConfirmStatus())) {
            // 已确认就不重复处理
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        // 并发安全更新确认状态（仅当当前仍为 PENDING 时成功）
        int rows = leaveImpactMapper.updateConfirmStatusIfCurrent(
                impactId,
                "PENDING",
                "CONFIRMED",
                now
        );
        if (rows == 0) {
            throw new IllegalStateException("该节次请假已被其他老师或系统处理，请刷新后重试");
        }

        // 如果需要记录 remark，可以再调用一次通用的 updateConfirm 来写入备注
        impact.setConfirmStatus("CONFIRMED");
        impact.setConfirmTime(now);
        impact.setRemark(remark);
        leaveImpactMapper.updateConfirm(impact);

        // 若该请假单所有节次都已确认，则主表状态改为 APPROVED
        int pending = leaveImpactMapper.countPendingByLeaveId(impact.getLeaveId());
        if (pending == 0) {
            // 使用乐观锁从 PENDING_TEACHER → APPROVED
            int updated = leaveRequestMapper.updateStatusIfCurrent(
                    impact.getLeaveId(),
                    "PENDING_TEACHER",
                    "APPROVED"
            );
            if (updated > 0) {
                // 只有真正从 PENDING_TEACHER 变为 APPROVED 的那一次写审批记录
                Approval approval = new Approval();
                approval.setLeaveId(impact.getLeaveId());
                approval.setApproverId(teacherId);
                approval.setApproverRole("TEACHER");
                approval.setAction("AGREE");
                approval.setComment("所有节次已确认");
                approval.setCreatedAt(now);
                approvalMapper.insert(approval);
            }
        }
    }

    @Override
    public List<TeacherCoursePendingDTO> listPendingForTeacherByCourse(Long teacherId) {
        if (teacherId == null) {
            throw new IllegalArgumentException("teacherId 不能为空");
        }
        return teacherLeaveQueryMapper.listPendingGroupedByCourse(teacherId);
    }

    /**
     * 学生查看自己的请假列表
     */
    @Override
    public List<LeaveRequest> listLeavesForStudent(Long studentId) {
        return leaveRequestMapper.findByStudentId(studentId);
    }

    /**
     * 班级请假统计
     */
    @Override
    public ClassLeaveStatsDTO getClassLeaveStats(Long classId, LocalDate startDate, LocalDate endDate) {
        if (classId == null || startDate == null || endDate == null) {
            throw new IllegalArgumentException("classId / startDate / endDate 不能为空");
        }

        ClassLeaveStatsDTO stats = leaveRequestMapper.queryClassLeaveStats(classId, startDate, endDate);
        if (stats == null) {
            Clazz clazz = clazzMapper.findById(classId);
            stats = new ClassLeaveStatsDTO();
            stats.setClassId(classId);
            stats.setClassName(clazz != null ? clazz.getClassName() : null);
        }
        return stats;
    }

    /**
     * 请假详情 + 时间线
     */
    @Override
    public LeaveDetailDTO getLeaveDetail(Long leaveId) {
        // 1. 主表信息
        LeaveRequest leave = leaveRequestMapper.findById(leaveId);
        if (leave == null) {
            throw new IllegalArgumentException("请假单不存在");
        }

        LeaveDetailDTO dto = new LeaveDetailDTO();
        dto.setLeaveId(leave.getLeaveId());
        dto.setStudentId(leave.getStudentId());
        dto.setLeaveType(leave.getLeaveType());
        dto.setApplyChannel(leave.getApplyChannel());
        dto.setReason(leave.getReason());
        dto.setProofUrl(leave.getProofUrl());
        dto.setStartTime(leave.getStartTime());
        dto.setEndTime(leave.getEndTime());
        dto.setStatus(leave.getStatus());

        // 学生姓名 & 班级名称
        String studentName = null;
        String className = null;
        if (leave.getStudentId() != null) {
            Student stu = studentMapper.findById(leave.getStudentId());
            if (stu != null) {
                studentName = stu.getName();
                if (stu.getClassId() != null) {
                    Clazz clazz = clazzMapper.findById(stu.getClassId());
                    if (clazz != null) {
                        className = clazz.getClassName();
                    }
                }
            }
        }
        dto.setStudentName(studentName);
        dto.setClassName(className);

        // 2. 影响节次列表
        List<LeaveImpact> impacts = leaveImpactMapper.findByLeaveId(leaveId);
        List<LeaveDetailDTO.ImpactItem> impactItems = impacts.stream().map(imp -> {
            LeaveDetailDTO.ImpactItem item = new LeaveDetailDTO.ImpactItem();
            item.setImpactId(imp.getImpactId());
            item.setOfferingId(imp.getOfferingId());
            item.setCourseDate(imp.getCourseDate());
            item.setSectionStart(imp.getSectionStart());
            item.setSectionEnd(imp.getSectionEnd());
            item.setConfirmStatus(imp.getConfirmStatus());
            item.setConfirmTime(imp.getConfirmTime());
            item.setRemark(imp.getRemark());

            // 课程名 & 老师名
            String courseName = null;
            String teacherName = null;
            if (imp.getOfferingId() != null) {
                Offering off = offeringMapper.findById(imp.getOfferingId());
                if (off != null) {
                    if (off.getCourseId() != null) {
                        Course course = courseMapper.findById(off.getCourseId());
                        if (course != null) {
                            courseName = course.getCourseName();
                        }
                    }
                    if (off.getTeacherId() != null) {
                        Staff teacher = staffMapper.findById(off.getTeacherId());
                        if (teacher != null) {
                            teacherName = teacher.getName();
                        }
                    }
                }
            }
            item.setCourseName(courseName);
            item.setTeacherName(teacherName);

            return item;
        }).collect(Collectors.toList());
        dto.setImpacts(impactItems);

        // 3. 审批记录时间线
        List<Approval> approvals = approvalMapper.findByLeaveId(leaveId);
        List<LeaveDetailDTO.ApprovalItem> approvalItems = approvals.stream().map(a -> {
            LeaveDetailDTO.ApprovalItem item = new LeaveDetailDTO.ApprovalItem();
            item.setApprovalId(a.getApprovalId());
            item.setApproverId(a.getApproverId());
            item.setApproverRole(a.getApproverRole());
            item.setAction(a.getAction());
            item.setComment(a.getComment());
            item.setCreatedAt(a.getCreatedAt());

            String approverName = null;
            if (a.getApproverId() != null) {
                Staff s = staffMapper.findById(a.getApproverId());
                if (s != null) {
                    approverName = s.getName();
                }
            }
            item.setApproverName(approverName);
            return item;
        }).collect(Collectors.toList());
        dto.setApprovals(approvalItems);

        return dto;
    }
}
