package com.example.leavesystem.mapper;

import com.example.leavesystem.entity.LeaveImpact;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface LeaveImpactMapper {

    @Select("SELECT * FROM leave_impact WHERE impact_id = #{id}")
    LeaveImpact findById(Long id);

    @Select("SELECT * FROM leave_impact WHERE leave_id = #{leaveId}")
    List<LeaveImpact> findByLeaveId(Long leaveId);

    @Insert("""
        INSERT INTO leave_impact
          (leave_id, offering_id, course_date,
           section_start, section_end, teacher_id,
           confirm_status, confirm_time, remark)
        VALUES
          (#{leaveId}, #{offeringId}, #{courseDate},
           #{sectionStart}, #{sectionEnd}, #{teacherId},
           #{confirmStatus}, #{confirmTime}, #{remark})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "impactId")
    int insert(LeaveImpact impact);

    @Update("""
        UPDATE leave_impact SET
          confirm_status = #{confirmStatus},
          confirm_time   = #{confirmTime},
          remark         = #{remark}
        WHERE impact_id = #{impactId}
        """)
    int updateConfirm(LeaveImpact impact);

    @Select("""
        SELECT COUNT(1)
        FROM leave_impact
        WHERE leave_id = #{leaveId}
          AND confirm_status = 'PENDING'
        """)
    int countPendingByLeaveId(Long leaveId);

    @Delete("DELETE FROM leave_impact WHERE leave_id = #{leaveId}")
    int deleteByLeaveId(Long leaveId);

    // ===== 新增：考勤模块用，查询某门课某天某节内已审批通过的请假学生ID =====

    /**
     * 查询在指定课程/日期/节次范围内，已审批通过的请假学生ID列表
     * 对应条件：
     *  - leave_impact.offering_id = offeringId
     *  - leave_impact.course_date = courseDate
     *  - 节次区间有交集：impact.section_start <= sectionEnd && impact.section_end >= sectionStart
     *  - leave_request.status = 'APPROVED'
     *  - leave_request.cancelled_at IS NULL
     */
    @Select("""
        SELECT DISTINCT lr.student_id
        FROM leave_impact li
        JOIN leave_request lr ON li.leave_id = lr.leave_id
        WHERE li.offering_id = #{offeringId}
          AND li.course_date = #{courseDate}
          AND li.section_start <= #{sectionEnd}
          AND li.section_end >= #{sectionStart}
          AND lr.status = 'APPROVED'
          AND lr.cancelled_at IS NULL
        """)
    List<Long> listApprovedStudentIds(@Param("offeringId") Long offeringId,
                                      @Param("courseDate") LocalDate courseDate,
                                      @Param("sectionStart") Integer sectionStart,
                                      @Param("sectionEnd") Integer sectionEnd);

    @Update("""
    UPDATE leave_impact
    SET confirm_status = #{newStatus},
        confirm_time   = #{confirmTime}
    WHERE impact_id = #{impactId}
      AND confirm_status = #{oldStatus}
    """)
    int updateConfirmStatusIfCurrent(@Param("impactId") Long impactId,
                                     @Param("oldStatus") String oldStatus,
                                     @Param("newStatus") String newStatus,
                                     @Param("confirmTime") java.time.LocalDateTime confirmTime);

}
