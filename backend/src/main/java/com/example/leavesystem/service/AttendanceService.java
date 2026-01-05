package com.example.leavesystem.service;

import com.example.leavesystem.dto.*;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {

    /**
     * 老师发起一次签到场次
     */
    AttendanceSessionStartResponse startSession(AttendanceSessionStartRequest request);

    /**
     * 老师关闭本次签到场次，并根据签到情况生成缺勤记录
     */
    void closeSession(Long sessionId, Long teacherId);

    /**
     * 学生扫码 / 输入口令签到
     */
    void checkin(StudentCheckinRequest request);

    /**
     * 老师查看某段时间内自己的签到场次列表
     */
    List<AttendanceSessionSummaryDTO> listSessionsForTeacher(Long teacherId, LocalDate startDate, LocalDate endDate);

    /**
     * 查看某一次签到场次的详细情况
     */
    AttendanceSessionDetailDTO getSessionDetail(Long sessionId, Long teacherId);
}
