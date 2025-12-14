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

    // ===== 为考勤子系统新增的 3 个方法 =====

    /**
     * 查询某次开课下，所有“在读（ENROLLED）”学生的ID列表
     */
    @Select("""
        SELECT student_id
        FROM enrollment
        WHERE offering_id = #{offeringId}
          AND status = 'ENROLLED'
        """)
    List<Long> listStudentIdsByOfferingId(Long offeringId);

    /**
     * 统计某门课中，某个学生是否已选课（且为 ENROLLED）
     * 用于学生扫码签到时做合法性校验
     */
    @Select("""
        SELECT COUNT(*)
        FROM enrollment
        WHERE offering_id = #{offeringId}
          AND student_id = #{studentId}
          AND status = 'ENROLLED'
        """)
    int countByOfferingIdAndStudentId(@Param("offeringId") Long offeringId,
                                      @Param("studentId") Long studentId);

    /**
     * 统计某次开课下 “应到人数”（只算 ENROLLED 的）
     */
    @Select("""
        SELECT COUNT(*)
        FROM enrollment
        WHERE offering_id = #{offeringId}
          AND status = 'ENROLLED'
        """)
    int countByOfferingId(Long offeringId);
}
