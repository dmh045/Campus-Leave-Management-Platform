package com.example.leavesystem.mapper;

import com.example.leavesystem.dto.TeacherPendingImpactDTO;
import com.example.leavesystem.dto.TeacherCoursePendingDTO;
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

    /**
     * 任课教师按课程维度聚合的待确认请假列表
     */
    @Select("""
        SELECT
          o.offering_id      AS offeringId,
          c.course_name      AS courseName,
          cls.class_name     AS className,
          o.week_day         AS weekDay,
          o.section_start    AS sectionStart,
          o.section_end      AS sectionEnd,
          o.classroom        AS classroom,
          COUNT(*)           AS pendingCount
        FROM leave_impact li
          JOIN leave_request lr ON li.leave_id   = lr.leave_id
          JOIN offering      o  ON li.offering_id = o.offering_id
          JOIN course        c  ON o.course_id   = c.course_id
          JOIN class         cls ON o.class_id   = cls.class_id
        WHERE li.teacher_id     = #{teacherId}
          AND li.confirm_status = 'PENDING'
          AND lr.status         = 'PENDING_TEACHER'
        GROUP BY
          o.offering_id,
          c.course_name,
          cls.class_name,
          o.week_day,
          o.section_start,
          o.section_end,
          o.classroom
        ORDER BY
          o.week_day,
          o.section_start
        """)
    List<TeacherCoursePendingDTO> listPendingGroupedByCourse(@Param("teacherId") Long teacherId);

}
