package com.example.leavesystem.mapper;

import com.example.leavesystem.entity.Absence;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AbsenceMapper {

    @Select("SELECT * FROM absence WHERE absence_id = #{id}")
    Absence findById(Long id);

    @Select("""
        SELECT * FROM absence
        WHERE student_id = #{studentId}
        """)
    List<Absence> findByStudentId(Long studentId);

    @Insert("""
        INSERT INTO absence
          (student_id, offering_id, course_date,
           section_start, section_end, source, status,
           makeup_deadline, converted_leave_id,
           created_at, updated_at)
        VALUES
          (#{studentId}, #{offeringId}, #{courseDate},
           #{sectionStart}, #{sectionEnd}, #{source}, #{status},
           #{makeupDeadline}, #{convertedLeaveId},
           NOW(), NOW())
        """)
    @Options(useGeneratedKeys = true, keyProperty = "absenceId")
    int insert(Absence absence);

    @Update("""
        UPDATE absence SET
          status            = #{status},
          makeup_deadline   = #{makeupDeadline},
          converted_leave_id = #{convertedLeaveId},
          updated_at        = NOW()
        WHERE absence_id = #{absenceId}
        """)
    int update(Absence absence);
}
