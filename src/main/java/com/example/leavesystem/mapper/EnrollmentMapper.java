package com.example.leavesystem.mapper;

import com.example.leavesystem.entity.Enrollment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface EnrollmentMapper {

    @Select("SELECT * FROM enrollment WHERE enrollment_id = #{id}")
    Enrollment findById(Long id);

    @Select("""
        SELECT * FROM enrollment
        WHERE student_id = #{studentId}
        """)
    List<Enrollment> findByStudentId(Long studentId);

    @Select("""
        SELECT * FROM enrollment
        WHERE offering_id = #{offeringId}
        """)
    List<Enrollment> findByOfferingId(Long offeringId);

    @Insert("""
        INSERT INTO enrollment
          (student_id, offering_id, status, created_at, updated_at)
        VALUES
          (#{studentId}, #{offeringId}, #{status}, NOW(), NOW())
        """)
    @Options(useGeneratedKeys = true, keyProperty = "enrollmentId")
    int insert(Enrollment enrollment);

    @Update("""
        UPDATE enrollment SET
          status     = #{status},
          updated_at = NOW()
        WHERE enrollment_id = #{enrollmentId}
        """)
    int updateStatus(Enrollment enrollment);

    @Delete("DELETE FROM enrollment WHERE enrollment_id = #{id}")
    int delete(Long id);

    @Select("SELECT * FROM enrollment")
    List<Enrollment> findAll();
}
