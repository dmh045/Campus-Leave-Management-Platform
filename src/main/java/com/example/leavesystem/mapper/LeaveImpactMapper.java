package com.example.leavesystem.mapper;

import com.example.leavesystem.entity.LeaveImpact;
import org.apache.ibatis.annotations.*;

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

}
