package com.example.leavesystem.mapper;

import com.example.leavesystem.entity.Offering;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OfferingMapper {

    @Select("SELECT * FROM offering WHERE offering_id = #{id}")
    Offering findById(Long id);

    @Select("""
        SELECT * FROM offering
        WHERE term_id = #{termId} AND class_id = #{classId}
        """)
    List<Offering> findByTermAndClass(@Param("termId") Long termId,
                                      @Param("classId") Long classId);

    @Insert("""
        INSERT INTO offering
          (term_id, course_id, class_id, teacher_id,
           week_day, section_start, section_end, classroom,
           created_at, updated_at)
        VALUES
          (#{termId}, #{courseId}, #{classId}, #{teacherId},
           #{weekDay}, #{sectionStart}, #{sectionEnd}, #{classroom},
           NOW(), NOW())
        """)
    @Options(useGeneratedKeys = true, keyProperty = "offeringId")
    int insert(Offering offering);

    @Update("""
        UPDATE offering SET
          term_id       = #{termId},
          course_id     = #{courseId},
          class_id      = #{classId},
          teacher_id    = #{teacherId},
          week_day      = #{weekDay},
          section_start = #{sectionStart},
          section_end   = #{sectionEnd},
          classroom     = #{classroom},
          updated_at    = NOW()
        WHERE offering_id = #{offeringId}
        """)
    int update(Offering offering);

    @Delete("DELETE FROM offering WHERE offering_id = #{id}")
    int delete(Long id);
}
