package com.example.leavesystem.service.impl;

import com.example.leavesystem.dto.CounselorPendingLeaveDTO;
import com.example.leavesystem.dto.LeaveApplyRequest;
import com.example.leavesystem.dto.LeaveApplyResponse;
import com.example.leavesystem.dto.TeacherPendingImpactDTO;
import com.example.leavesystem.dto.LeaveDetailDTO;
import com.example.leavesystem.dto.ClassLeaveStatsDTO;
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
import com.example.leavesystem.dto.CoursePendingImpactDTO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.ArrayList;


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
        if (request.getApplyChannel() == null || request.getLeaveType() == null) {
            throw new IllegalArgumentException("applyChannel 和 leaveType 不能为空");
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

        // 更新主表状态
        leaveRequestMapper.updateStatusSimple(leaveId, newStatus);

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
     * 学生重新提交方法
     */
    @Override
    @Transactional
    public LeaveApplyResponse resubmitLeave(Long leaveId, LeaveApplyRequest request) {
        // 1. 基本参数校验
        if (request.getStudentId() == null || request.getTermId() == null) {
            throw new IllegalArgumentException("studentId 和 termId 不能为空");
        }
        if (request.getStartTime() == null || request.getEndTime() == null
                || request.getStartTime().isAfter(request.getEndTime())) {
            throw new IllegalArgumentException("请假时间段不合法");
        }
        if (request.getApplyChannel() == null || request.getLeaveType() == null) {
            throw new IllegalArgumentException("applyChannel 和 leaveType 不能为空");
        }

        // 2. 校验请假单存在且状态为 RETURNED
        LeaveRequest leave = leaveRequestMapper.findById(leaveId);
        if (leave == null) {
            throw new IllegalArgumentException("请假单不存在");
        }
        if (!"RETURNED".equals(leave.getStatus())) {
            throw new IllegalStateException("只有被退回的请假单才能重新提交");
        }

        // 3. 校验学生/学期存在
        Student student = studentMapper.findById(request.getStudentId());
        if (student == null) {
            throw new IllegalArgumentException("学生不存在");
        }
        Term term = termMapper.findById(request.getTermId());
        if (term == null) {
            throw new IllegalArgumentException("学期不存在");
        }

        // 4. 更新 leave_request 表
        leave.setLeaveType(request.getLeaveType());
        leave.setApplyChannel(request.getApplyChannel());
        leave.setReason(request.getReason());
        leave.setProofUrl(request.getProofUrl());
        leave.setStartTime(request.getStartTime());
        leave.setEndTime(request.getEndTime());
        leave.setStatus("PENDING_COUNSELOR");
        leaveRequestMapper.updateStatus(leave);

        // 5. 删除旧的影响节次
        leaveImpactMapper.deleteByLeaveId(leaveId);

        // 6. 写入新的影响节次
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

            // 2) 影响节次
            //    - 如果前端传了 impacts：按传入的 impacts 写入（对所有学生相同）
            //    - 如果前端没传 / 传空：按【每个学生所属班级】自动匹配 time 区间内的 offering 节次
            List<PublicLeaveBatchRequest.ImpactItem> impactsToUse = request.getImpacts();
            if (impactsToUse == null || impactsToUse.isEmpty()) {
                impactsToUse = buildAutoImpactsForStudent(request.getTermId(), student.getClassId(), request.getStartTime(), request.getEndTime());
            }

            int inserted = 0;
            if (impactsToUse != null && !impactsToUse.isEmpty()) {
                // 批量拉 offering，避免逐条 findById
                Set<Long> offIds = impactsToUse.stream()
                        .map(PublicLeaveBatchRequest.ImpactItem::getOfferingId)
                        .filter(Objects::nonNull)
                        .collect(java.util.stream.Collectors.toSet());

                Map<Long, Offering> offMap = offIds.isEmpty() ? java.util.Map.of()
                        : offeringMapper.findByIds(new java.util.ArrayList<>(offIds)).stream()
                        .collect(java.util.stream.Collectors.toMap(Offering::getOfferingId, x -> x));

                for (PublicLeaveBatchRequest.ImpactItem item : impactsToUse) {
                    if (item.getOfferingId() == null) continue;
                    Offering offering = offMap.get(item.getOfferingId());
                    if (offering == null) continue;

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
                    inserted++;
                }
            }

// 如果完全没有产生 impacts：这张公假无需老师确认，直接通过
            if (inserted == 0) {
                leaveRequestMapper.updateStatusSimple(leaveId, "APPROVED");
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

    private List<PublicLeaveBatchRequest.ImpactItem> buildAutoImpactsForStudent(
            Long termId, Long classId, LocalDateTime startTime, LocalDateTime endTime
    ) {
        if (termId == null || classId == null || startTime == null || endTime == null) {
            return List.of();
        }

        List<Offering> offs = offeringMapper.findByTermAndClass(termId, classId);
        if (offs == null || offs.isEmpty()) return List.of();

        LocalDate start = startTime.toLocalDate();
        LocalDate end = endTime.toLocalDate();

        List<PublicLeaveBatchRequest.ImpactItem> res = new ArrayList<>();
        for (Offering o : offs) {
            Integer wd = o.getWeekDay();
            if (wd == null || wd < 1 || wd > 7) continue;

            for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
                if (d.getDayOfWeek().getValue() != wd) continue;

                PublicLeaveBatchRequest.ImpactItem item = new PublicLeaveBatchRequest.ImpactItem();
                item.setOfferingId(o.getOfferingId());
                item.setCourseDate(d);
                item.setSectionStart(o.getSectionStart());
                item.setSectionEnd(o.getSectionEnd());
                res.add(item);
            }
        }
        return res;
    }

    /**
     * 老师确认某一节次的请假
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

        impact.setConfirmStatus("CONFIRMED");
        impact.setConfirmTime(LocalDateTime.now());
        impact.setRemark(remark);
        leaveImpactMapper.updateConfirm(impact);

        // 若该请假单所有节次都已确认，则主表状态改为 APPROVED
        int pending = leaveImpactMapper.countPendingByLeaveId(impact.getLeaveId());
        if (pending == 0) {
            leaveRequestMapper.updateStatusSimple(impact.getLeaveId(), "APPROVED");

            Approval approval = new Approval();
            approval.setLeaveId(impact.getLeaveId());
            approval.setApproverId(teacherId);
            approval.setApproverRole("TEACHER");
            approval.setAction("AGREE");
            approval.setComment("所有节次已确认");
            approval.setCreatedAt(LocalDateTime.now());
            approvalMapper.insert(approval);
        }
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
    /**
     * 老师查看自己的待确认节次（按课程分组）
     */
    @Override
    public List<CoursePendingImpactDTO> listPendingByCourseForTeacher(Long teacherId) {
        // 获取按课程分组的原始数据（返回的是 TeacherPendingImpactDTO 列表）
        List<TeacherPendingImpactDTO> rawData = teacherLeaveQueryMapper.findPendingByCourse(teacherId);

        // 按课程ID进行分组处理
        Map<Long, CoursePendingImpactDTO> courseMap = new HashMap<>();

        for (TeacherPendingImpactDTO item : rawData) {
            // 注意：这里需要在 TeacherPendingImpactDTO 中添加 courseId 字段
            // 我们假设已经添加了这个字段
            Long courseId = item.getCourseId();

            if (courseMap.containsKey(courseId)) {
                // 已存在该课程，添加影响节次
                CoursePendingImpactDTO courseDTO = courseMap.get(courseId);
                courseDTO.getImpacts().add(item); // 现在 item 是 TeacherPendingImpactDTO 类型
            } else {
                // 新课程，创建分组对象
                CoursePendingImpactDTO courseDTO = new CoursePendingImpactDTO();
                courseDTO.setCourseId(courseId);
                courseDTO.setCourseName(item.getCourseName());

                // 创建影响节次列表
                List<TeacherPendingImpactDTO> impacts = new ArrayList<>();
                impacts.add(item);
                courseDTO.setImpacts(impacts);

                courseMap.put(courseId, courseDTO);
            }
        }

        // 返回分组后的结果
        return new ArrayList<>(courseMap.values());
    }

    @Override
    public List<CounselorPendingLeaveDTO> listAllForCounselor(Long counselorId) {
        return counselorLeaveQueryMapper.findAllByCounselor(counselorId);
    }

}
