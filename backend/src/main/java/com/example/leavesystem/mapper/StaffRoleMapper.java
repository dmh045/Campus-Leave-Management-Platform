package com.example.leavesystem.mapper;

import com.example.leavesystem.entity.StaffRole;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface StaffRoleMapper {

    @Select("SELECT * FROM staff_role WHERE staff_id = #{staffId}")
    List<StaffRole> findByStaffId(Long staffId);

    @Select("SELECT * FROM staff_role WHERE staff_id = #{staffId}")
    List<StaffRole> selectByStaffId(Long staffId);

    @Insert("""
        INSERT INTO staff_role
          (staff_id, role_code, remark)
        VALUES
          (#{staffId}, #{roleCode}, #{remark})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(StaffRole role);

    @Delete("DELETE FROM staff_role WHERE id = #{id}")
    int deleteById(Long id);

    @Delete("DELETE FROM staff_role WHERE staff_id = #{staffId}")
    int deleteByStaffId(Long staffId);
}
