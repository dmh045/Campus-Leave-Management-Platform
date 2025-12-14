package com.example.leavesystem.mapper;

import com.example.leavesystem.entity.Student;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface StudentMapper {

    @Select("SELECT * FROM student WHERE student_id = #{id}")
    Student findById(Long id);

    @Select("SELECT * FROM student WHERE student_no = #{studentNo}")
    Student findByStudentNo(String studentNo);

    @Select("SELECT * FROM student")
    List<Student> findAll();

    @Insert("""
        INSERT INTO student
          (student_no, name, gender, class_id, phone, email, status, created_at, updated_at)
        VALUES
          (#{studentNo}, #{name}, #{gender}, #{classId}, #{phone}, #{email},
           #{status}, NOW(), NOW())
        """)
    @Options(useGeneratedKeys = true, keyProperty = "studentId")
    int insert(Student student);

    @Update("""
        UPDATE student SET
          student_no = #{studentNo},
          name       = #{name},
          gender     = #{gender},
          class_id   = #{classId},
          phone      = #{phone},
          email      = #{email},
          status     = #{status},
          updated_at = NOW()
        WHERE student_id = #{studentId}
        """)
    int update(Student student);

    @Delete("DELETE FROM student WHERE student_id = #{id}")
    int delete(Long id);

    @Select("""
    <script>
    SELECT *
    FROM student
    WHERE student_id IN
    <foreach collection="ids" item="id" open="(" separator="," close=")">
      #{id}
    </foreach>
    </script>
    """)
    List<Student> findByIds(@Param("ids") List<Long> ids);

}
