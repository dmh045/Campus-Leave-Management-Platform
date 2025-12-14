package com.example.leavesystem.service.impl;

import com.example.leavesystem.dto.AbsenceConvertToLeaveRequest;
import com.example.leavesystem.entity.*;
import com.example.leavesystem.mapper.*;
import com.example.leavesystem.service.AbsenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AbsenceServiceImpl implements AbsenceService {

    private final AbsenceMapper absenceMapper;
    private final OfferingMapper offeringMapper;
    private final LeaveRequestMapper leaveRequestMapper;
    private final LeaveImpactMapper leaveImpactMapper;
    private final ApprovalMapper approvalMapper;

    @Override
    @Transactional
    public Long convertToLeave(Long absenceId, AbsenceConvertToLeaveRequest request) {
        Absence absence = absenceMapper.findById(absenceId);
        if (absence == null) {
            throw new IllegalArgumentException("缺勤记录不存在");
        }
        if (!Objects.equals(absence.getStudentId(), request.getStudentId())) {
            throw new IllegalArgumentException("不能操作其他同学的缺勤记录");
        }
        if (!"PENDING_MAKEUP".equals(absence.getStatus())) {
            throw new IllegalStateException("当前状态不允许补假");
        }

        Offering offering = offeringMapper.findById(absence.getOfferingId());
        if (offering == null) {
            throw new IllegalArgumentException("对应开课记录不存在");
        }

        LocalDateTime now = LocalDateTime.now();

        // 1) 新建 leave_request
        LeaveRequest lr = new LeaveRequest();
        lr.setStudentId(absence.getStudentId());
        lr.setTermId(request.getTermId());
        lr.setLeaveType(request.getLeaveType());
        lr.setApplyChannel("BY_COURSE");
        lr.setReason(request.getReason());
        lr.setProofUrl(request.getProofUrl());
        lr.setStartTime(request.getStartTime());
        lr.setEndTime(request.getEndTime());
        lr.setStatus("PENDING_COUNSELOR");
        lr.setCreatedAt(now);
        lr.setUpdatedAt(now);

        leaveRequestMapper.insert(lr); // 要求你原来的 insert 能回填 leaveId

        // 2) 新建 leave_impact（一条，对应该缺勤的节次）
        LeaveImpact impact = new LeaveImpact();
        impact.setLeaveId(lr.getLeaveId());
        impact.setOfferingId(absence.getOfferingId());
        impact.setCourseDate(absence.getCourseDate());
        impact.setSectionStart(absence.getSectionStart());
        impact.setSectionEnd(absence.getSectionEnd());
        impact.setTeacherId(offering.getTeacherId());
        impact.setConfirmStatus("PENDING");
        impact.setConfirmTime(null);
        impact.setRemark(null);

        leaveImpactMapper.insert(impact);

        // 3) 写一条审批流水：学生发起 APPLY
        Approval approval = new Approval();
        approval.setLeaveId(lr.getLeaveId());
        approval.setApproverId(request.getStudentId());
        approval.setApproverRole("STUDENT");
        approval.setAction("APPLY");
        approval.setComment(request.getReason());
        approval.setCreatedAt(now);

        approvalMapper.insert(approval);

        // 4) 并发安全地更新 absence 状态 ＋ 绑定 converted_leave_id
        int rows = absenceMapper.updateStatusAndConvertedIfCurrent(
                absenceId,
                "PENDING_MAKEUP",
                "CONVERTED_TO_LEAVE",
                lr.getLeaveId()
        );
        if (rows == 0) {
            // 有人抢先处理了
            throw new IllegalStateException("该缺勤记录已被处理，请刷新后重试");
        }

        return lr.getLeaveId();
    }

    @Override
    @Transactional
    public void confirmAbsence(Long absenceId, Long operatorId) {
        Absence absence = absenceMapper.findById(absenceId);
        if (absence == null) {
            throw new IllegalArgumentException("缺勤记录不存在");
        }
        if (!"PENDING_MAKEUP".equals(absence.getStatus())) {
            throw new IllegalStateException("当前状态不允许确认缺勤");
        }

        int rows = absenceMapper.updateStatusIfCurrent(
                absenceId,
                "PENDING_MAKEUP",
                "CONFIRMED"
        );
        if (rows == 0) {
            throw new IllegalStateException("该缺勤记录已被处理，请刷新后重试");
        }

        // 这里如果你希望记录“谁确认了缺勤”，可以扩展 absence 表或加一条 approval
        // 暂时保持简单，只改状态。
    }
}
