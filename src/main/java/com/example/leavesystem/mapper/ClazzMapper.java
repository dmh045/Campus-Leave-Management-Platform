package com.example.leavesystem.mapper;

import com.example.leavesystem.entity.Clazz;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ClazzMapper {

    @Select("SELECT * FROM `class` WHERE class_id = #{id}")
    Clazz findById(Long id);

    @Select("SELECT * FROM `class`")
    List<Clazz> findAll();

    @Insert("""
        INSERT INTO `class`
          (class_code, class_name, major, grade_year, counselor_id, created_at, updated_at)
        VALUES
          (#{classCode}, #{className}, #{major}, #{gradeYear}, #{counselorId}, NOW(), NOW())
        """)
    @Options(useGeneratedKeys = true, keyProperty = "classId")
    int insert(Clazz clazz);

    @Update("""
        UPDATE `class` SET
          class_code   = #{classCode},
          class_name   = #{className},
          major        = #{major},
          grade_year   = #{gradeYear},
          counselor_id = #{counselorId},
          updated_at   = NOW()
        WHERE class_id = #{classId}
        """)
    int update(Clazz clazz);

    @Delete("DELETE FROM `class` WHERE class_id = #{id}")
    int delete(Long id);

    @Select("""
    <script>
    SELECT *
    FROM `class`
    WHERE class_id IN
    <foreach collection="ids" item="id" open="(" separator="," close=")">
      #{id}
    </foreach>
    </script>
    """)
    List<Clazz> findByIds(@Param("ids") List<Long> ids);

}
