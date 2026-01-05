package com.example.leavesystem.mapper;

import com.example.leavesystem.entity.AttendanceSession;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AttendanceSessionMapper {

    /**
     * 新建签到场次
     */
    @Insert("""
        INSERT INTO attendance_session
          (teacher_id, offering_id, course_date,
           section_start, section_end,
           token, token_expire_time,
           allow_start_time, allow_end_time,
           status, created_at, updated_at)
        VALUES
          (#{teacherId}, #{offeringId}, #{courseDate},
           #{sectionStart}, #{sectionEnd},
           #{token}, #{tokenExpireTime},
           #{allowStartTime}, #{allowEndTime},
           #{status}, #{createdAt}, #{updatedAt})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "sessionId")
    int insert(AttendanceSession session);

    /**
     * 按主键查询
     */
    @Select("SELECT * FROM attendance_session WHERE session_id = #{sessionId}")
    AttendanceSession findById(Long sessionId);

    /**
     * 如果你项目里有地方单独更新 token，可以保留这个方法
     */
    @Update("""
        UPDATE attendance_session
        SET token = #{token},
            token_expire_time = #{tokenExpireTime},
            updated_at = NOW()
        WHERE session_id = #{sessionId}
        """)
    int updateToken(AttendanceSession session);

    /**
     * 更新状态（用于关闭场次），和 AttendanceServiceImpl 里的调用保持一致：
     * attendanceSessionMapper.updateStatus(sessionId, "CLOSED", now);
     */
    @Update("""
        UPDATE attendance_session
        SET status = #{status},
            updated_at = #{updatedAt}
        WHERE session_id = #{sessionId}
        """)
    int updateStatus(@Param("sessionId") Long sessionId,
                     @Param("status") String status,
                     @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 学生签到时，通过 token 找到当前仍然有效且 OPEN 的签到场次
     */
    @Select("""
        SELECT *
        FROM attendance_session
        WHERE token = #{token}
          AND status = 'OPEN'
          AND token_expire_time >= #{now}
        LIMIT 1
        """)
    AttendanceSession findOpenByToken(@Param("token") String token,
                                      @Param("now") LocalDateTime now);

    /**
     * 老师查看一段时间内的所有签到场次列表
     */
    @Select("""
        SELECT *
        FROM attendance_session
        WHERE teacher_id = #{teacherId}
          AND course_date BETWEEN #{startDate} AND #{endDate}
        ORDER BY course_date DESC, section_start ASC
        """)
    List<AttendanceSession> listByTeacherAndDateRange(@Param("teacherId") Long teacherId,
                                                      @Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);
}
