package com.example.leavesystem.mapper;

import com.example.leavesystem.dto.StudentDayCourseDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface TimetableMapper {

    @Select("""
    SELECT
      o.offering_id   AS offeringId,
      c.course_id     AS courseId,
      c.course_name   AS courseName,
      s.name          AS teacherName,
      o.week_day      AS weekDay,
      o.section_start AS sectionStart,
      o.section_end   AS sectionEnd,
      o.classroom     AS classroom,

      CASE
        WHEN MAX(CASE WHEN lr.leave_id IS NOT NULL
                       AND lr.leave_type = 'PUBLIC'
                       AND lr.status NOT IN ('REJECTED', 'CANCELLED') THEN 1 ELSE 0 END) = 1 THEN 100
        WHEN MAX(CASE WHEN lr.leave_id IS NOT NULL
                       AND lr.status = 'APPROVED' THEN 1 ELSE 0 END) = 1 THEN 5
        WHEN MAX(CASE WHEN lr.leave_id IS NOT NULL
                       AND lr.status IN ('PENDING_COUNSELOR','PENDING_TEACHER','RETURNED') THEN 1 ELSE 0 END) = 1 THEN 4
        WHEN MAX(CASE WHEN lr.leave_id IS NOT NULL
                       AND lr.status = 'REJECTED' THEN 1 ELSE 0 END) = 1 THEN 3
        WHEN MAX(CASE WHEN lr.leave_id IS NOT NULL
                       AND lr.status = 'CANCELLED' THEN 1 ELSE 0 END) = 1 THEN 6
        WHEN MAX(CASE WHEN lr.leave_id IS NOT NULL
                       AND lr.status = 'ENDED' THEN 1 ELSE 0 END) = 1 THEN 7
        ELSE 200
      END AS status

    FROM enrollment e
      JOIN offering o ON e.offering_id = o.offering_id
      JOIN course   c ON o.course_id   = c.course_id
      JOIN term     t ON o.term_id     = t.term_id
      LEFT JOIN staff s ON s.staff_id  = o.teacher_id
      LEFT JOIN leave_impact li ON li.offering_id = o.offering_id
                           AND li.course_date = #{date}
                           AND li.section_start <= o.section_end
                           AND li.section_end   >= o.section_start
      LEFT JOIN leave_request lr ON lr.leave_id   = li.leave_id
                               AND lr.student_id = e.student_id
    WHERE e.student_id = #{studentId}
      AND #{date} BETWEEN t.start_date AND t.end_date
      AND o.week_day = CASE
                          WHEN DAYOFWEEK(#{date}) = 1 THEN 7
                          ELSE DAYOFWEEK(#{date}) - 1
                       END
    GROUP BY
      o.offering_id, c.course_id, c.course_name, s.name,
      o.week_day, o.section_start, o.section_end, o.classroom
    ORDER BY o.section_start
    """)
    List<StudentDayCourseDTO> findDayCourses(@Param("studentId") Long studentId,
                                             @Param("date") LocalDate date);

    @Select("""
    SELECT
      o.offering_id   AS offeringId,
      c.course_id     AS courseId,
      c.course_name   AS courseName,
      cl.class_id     AS classId,
      cl.class_name   AS className,
      o.week_day      AS weekDay,
      o.section_start AS sectionStart,
      o.section_end   AS sectionEnd,
      o.classroom     AS classroom
    FROM offering o
      JOIN course c ON o.course_id = c.course_id
      JOIN `class` cl ON o.class_id = cl.class_id
      JOIN term t ON o.term_id = t.term_id
    WHERE o.teacher_id = #{teacherId}
      AND #{date} BETWEEN t.start_date AND t.end_date
      AND o.week_day = CASE
                          WHEN DAYOFWEEK(#{date}) = 1 THEN 7
                          ELSE DAYOFWEEK(#{date}) - 1
                       END
    ORDER BY o.section_start
    """)
    List<com.example.leavesystem.dto.TeacherDayCourseDTO> findTeacherDayCourses(
            @Param("teacherId") Long teacherId,
            @Param("date") java.time.LocalDate date
    );

}
