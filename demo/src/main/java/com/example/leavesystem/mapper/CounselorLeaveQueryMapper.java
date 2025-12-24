package com.example.leavesystem.mapper;

import com.example.leavesystem.dto.CounselorPendingLeaveDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CounselorLeaveQueryMapper {

    @Select("""
        SELECT
          lr.leave_id    AS leaveId,
          lr.student_id  AS studentId,
          s.name         AS studentName,
          c.class_name   AS className,
          lr.leave_type  AS leaveType,
          lr.reason      AS reason,
          lr.start_time  AS startTime,
          lr.end_time    AS endTime,
          lr.status      AS status
        FROM leave_request lr
          JOIN student s ON lr.student_id = s.student_id
          JOIN `class` c ON s.class_id    = c.class_id
        WHERE lr.status = 'PENDING_COUNSELOR'
          AND c.counselor_id = #{counselorId}
        ORDER BY lr.created_at DESC
        """)
    List<CounselorPendingLeaveDTO> findPendingByCounselor(@Param("counselorId") Long counselorId);
}
