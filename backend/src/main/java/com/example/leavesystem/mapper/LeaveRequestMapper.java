package com.example.leavesystem.mapper;

import com.example.leavesystem.entity.LeaveRequest;
import org.apache.ibatis.annotations.*;
import com.example.leavesystem.dto.ClassLeaveStatsDTO;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;


import java.util.List;

@Mapper
public interface LeaveRequestMapper {

    @Select("SELECT * FROM leave_request WHERE leave_id = #{id}")
    LeaveRequest findById(Long id);

    @Select("""
        SELECT * FROM leave_request
        WHERE student_id = #{studentId}
        ORDER BY created_at DESC
        """)
    List<LeaveRequest> findByStudentId(Long studentId);

    @Insert("""
        INSERT INTO leave_request
          (student_id, term_id, leave_type, apply_channel,
           reason, proof_url, start_time, end_time,
           status, created_at, updated_at)
        VALUES
          (#{studentId}, #{termId}, #{leaveType}, #{applyChannel},
           #{reason}, #{proofUrl}, #{startTime}, #{endTime},
           #{status}, NOW(), NOW())
        """)
    @Options(useGeneratedKeys = true, keyProperty = "leaveId")
    int insert(LeaveRequest leave);

    @Update("""
        UPDATE leave_request SET
          status      = #{status},
          rejected_at = #{rejectedAt},
          cancelled_at = #{cancelledAt},
          updated_at  = NOW()
        WHERE leave_id = #{leaveId}
        """)
    int updateStatus(LeaveRequest leave);

    @Update("""
        UPDATE leave_request SET
          status     = #{status},
          updated_at = NOW()
        WHERE leave_id = #{id}
        """)
    int updateStatusSimple(@Param("id") Long id, @Param("status") String status);

    @Select("""
        SELECT
          c.class_id       AS classId,
          c.class_name     AS className,
          COUNT(*)         AS totalLeaves,
          SUM(CASE WHEN lr.leave_type = 'SICK'   THEN 1 ELSE 0 END) AS sickCount,
          SUM(CASE WHEN lr.leave_type = 'AFFAIR' THEN 1 ELSE 0 END) AS affairCount,
          SUM(CASE WHEN lr.leave_type = 'PUBLIC' THEN 1 ELSE 0 END) AS publicCount,
          SUM(CASE WHEN lr.status IN ('PENDING_COUNSELOR','PENDING_TEACHER') THEN 1 ELSE 0 END) AS pendingCount,
          SUM(CASE WHEN lr.status = 'APPROVED' THEN 1 ELSE 0 END) AS approvedCount,
          SUM(CASE WHEN lr.status IN ('REJECTED','RETURNED') THEN 1 ELSE 0 END) AS rejectedCount
        FROM leave_request lr
          JOIN student s ON lr.student_id = s.student_id
          JOIN class   c ON s.class_id    = c.class_id
        WHERE c.class_id = #{classId}
          AND DATE(lr.start_time) BETWEEN #{start} AND #{end}
        GROUP BY c.class_id, c.class_name
        """)
    ClassLeaveStatsDTO queryClassLeaveStats(@Param("classId") Long classId,
                                            @Param("start") LocalDate start,
                                            @Param("end") LocalDate end);

}
