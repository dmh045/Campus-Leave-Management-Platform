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
          status             = #{status},
          makeup_deadline    = #{makeupDeadline},
          converted_leave_id = #{convertedLeaveId},
          updated_at         = NOW()
        WHERE absence_id = #{absenceId}
        """)
    int update(Absence absence);

    // ========= 批量插入缺勤记录，用于关闭签到场次后一次性写入 =========

    @Insert("""
        <script>
        INSERT INTO absence
          (student_id, offering_id, course_date,
           section_start, section_end, source, status,
           makeup_deadline, converted_leave_id,
           created_at, updated_at)
        VALUES
        <foreach collection="list" item="item" separator=",">
          (#{item.studentId}, #{item.offeringId}, #{item.courseDate},
           #{item.sectionStart}, #{item.sectionEnd}, #{item.source}, #{item.status},
           #{item.makeupDeadline}, #{item.convertedLeaveId},
           NOW(), NOW())
        </foreach>
        </script>
        """)
    int insertBatch(@Param("list") List<Absence> list);

    // ========= 并发安全更新：只有当前状态=oldStatus 才更新成功 =========

    @Update("""
    UPDATE absence
    SET status = #{newStatus},
        updated_at = NOW()
    WHERE absence_id = #{absenceId}
      AND status = #{oldStatus}
    """)
    int updateStatusIfCurrent(@Param("absenceId") Long absenceId,
                              @Param("oldStatus") String oldStatus,
                              @Param("newStatus") String newStatus);

    @Update("""
    UPDATE absence
    SET status = #{newStatus},
        converted_leave_id = #{convertedLeaveId},
        updated_at = NOW()
    WHERE absence_id = #{absenceId}
      AND status = #{oldStatus}
    """)
    int updateStatusAndConvertedIfCurrent(@Param("absenceId") Long absenceId,
                                          @Param("oldStatus") String oldStatus,
                                          @Param("newStatus") String newStatus,
                                          @Param("convertedLeaveId") Long convertedLeaveId);

}
