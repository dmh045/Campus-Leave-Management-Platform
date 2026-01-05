package com.example.leavesystem.service;

import com.example.leavesystem.dto.AbsenceConvertToLeaveRequest;

public interface AbsenceService {

    /**
     * 将一条缺勤记录转为新的请假单，返回新生成的 leaveId
     */
    Long convertToLeave(Long absenceId, AbsenceConvertToLeaveRequest request);

    /**
     * 确认缺勤（不再补假）
     */
    void confirmAbsence(Long absenceId, Long operatorId);
}
