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
          o.week_day      AS weekDay,
          o.section_start AS sectionStart,
          o.section_end   AS sectionEnd,
          o.classroom     AS classroom
        FROM enrollment e
          JOIN offering o ON e.offering_id = o.offering_id
          JOIN course   c ON o.course_id   = c.course_id
          JOIN term     t ON o.term_id     = t.term_id
        WHERE e.student_id = #{studentId}
          AND #{date} BETWEEN t.start_date AND t.end_date
          AND o.week_day = CASE
                              WHEN DAYOFWEEK(#{date}) = 1 THEN 7   -- MySQL: 1=Sunday；我们约定 1=Monday
                              ELSE DAYOFWEEK(#{date}) - 1
                           END
        ORDER BY o.section_start
        """)
    List<StudentDayCourseDTO> findDayCourses(@Param("studentId") Long studentId,
                                             @Param("date") LocalDate date);
}
