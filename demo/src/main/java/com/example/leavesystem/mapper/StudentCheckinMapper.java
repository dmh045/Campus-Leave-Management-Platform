package com.example.leavesystem.mapper;

import com.example.leavesystem.entity.StudentCheckin;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface StudentCheckinMapper {

    /**
     * 插入一条签到记录
     */
    @Insert("""
        INSERT INTO student_checkin
          (session_id, student_id, checkin_time, source)
        VALUES
          (#{sessionId}, #{studentId}, #{checkinTime}, #{source})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "checkinId")
    int insert(StudentCheckin checkin);

    /**
     * 查询某场次的全部签到记录
     */
    @Select("""
        SELECT * FROM student_checkin
        WHERE session_id = #{sessionId}
        """)
    List<StudentCheckin> listBySessionId(Long sessionId);

    /**
     * 查询某场次、某学生是否已经签到（用于防止重复签到）
     */
    @Select("""
        SELECT *
        FROM student_checkin
        WHERE session_id = #{sessionId}
          AND student_id = #{studentId}
        LIMIT 1
        """)
    StudentCheckin findBySessionIdAndStudentId(@Param("sessionId") Long sessionId,
                                               @Param("studentId") Long studentId);

    /**
     * 查询某场次已签到学生ID列表（用于生成缺勤）
     */
    @Select("""
        SELECT student_id
        FROM student_checkin
        WHERE session_id = #{sessionId}
        """)
    List<Long> listStudentIdsBySessionId(Long sessionId);

    /**
     * 统计某场次已签到人数（用于老师端统计）
     */
    @Select("""
        SELECT COUNT(*)
        FROM student_checkin
        WHERE session_id = #{sessionId}
        """)
    int countBySessionId(Long sessionId);
}
