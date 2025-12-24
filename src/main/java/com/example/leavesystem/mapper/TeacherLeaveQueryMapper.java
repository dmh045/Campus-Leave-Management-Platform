package com.example.leavesystem.mapper;

import com.example.leavesystem.dto.TeacherPendingImpactDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TeacherLeaveQueryMapper {

    @Select("""
        SELECT
          li.impact_id     AS impactId,
          li.leave_id      AS leaveId,
          lr.student_id    AS studentId,
          s.name           AS studentName,
          c.class_name     AS className,
          co.course_name   AS courseName,
          li.course_date   AS courseDate,
          li.section_start AS sectionStart,
          li.section_end   AS sectionEnd,
          lr.leave_type    AS leaveType,
          lr.reason        AS reason,
          lr.status        AS leaveStatus
        FROM leave_impact li
          JOIN leave_request lr ON li.leave_id   = lr.leave_id
          JOIN student s        ON lr.student_id = s.student_id
          JOIN `class` c        ON s.class_id    = c.class_id
          JOIN offering o       ON li.offering_id = o.offering_id
          JOIN course co        ON o.course_id   = co.course_id
        WHERE li.teacher_id = #{teacherId}
          AND li.confirm_status = 'PENDING'
          AND lr.status = 'PENDING_TEACHER'
        ORDER BY li.course_date, li.section_start
        """)
    List<TeacherPendingImpactDTO> findPendingByTeacher(@Param("teacherId") Long teacherId);
}
