package com.example.leavesystem.mapper;

import com.example.leavesystem.entity.Course;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CourseMapper {

    @Select("SELECT * FROM course WHERE course_id = #{id}")
    Course findById(Long id);

    @Select("SELECT * FROM course WHERE course_code = #{code}")
    Course findByCode(String code);

    @Select("SELECT * FROM course")
    List<Course> findAll();

    @Insert("""
        INSERT INTO course
          (course_code, course_name, credit, total_hours, created_at, updated_at)
        VALUES
          (#{courseCode}, #{courseName}, #{credit}, #{totalHours}, NOW(), NOW())
        """)
    @Options(useGeneratedKeys = true, keyProperty = "courseId")
    int insert(Course course);

    @Update("""
        UPDATE course SET
          course_code = #{courseCode},
          course_name = #{courseName},
          credit      = #{credit},
          total_hours = #{totalHours},
          updated_at  = NOW()
        WHERE course_id = #{courseId}
        """)
    int update(Course course);

    @Delete("DELETE FROM course WHERE course_id = #{id}")
    int delete(Long id);

    @Select("""
    <script>
    SELECT *
    FROM course
    WHERE course_id IN
    <foreach collection="ids" item="id" open="(" separator="," close=")">
      #{id}
    </foreach>
    </script>
    """)
    List<Course> findByIds(@Param("ids") List<Long> ids);
}
