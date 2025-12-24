package com.example.leavesystem.mapper;

import com.example.leavesystem.entity.Staff;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StaffMapper {

    @Select("SELECT * FROM staff WHERE staff_id = #{id}")
    Staff findById(Long id);


    @Select("SELECT * FROM staff")
    List<Staff> findAll();

    @Select("SELECT * FROM staff WHERE staff_no = #{staffNo}")
    Staff selectByStaffNo(String staffNo);

    @Insert("""
        INSERT INTO staff
          (staff_no, name, gender, phone, email, is_active, created_at, updated_at)
        VALUES
          (#{staffNo}, #{name}, #{gender}, #{phone}, #{email}, #{isActive}, NOW(), NOW())
        """)
    @Options(useGeneratedKeys = true, keyProperty = "staffId")
    int insert(Staff staff);

    @Update("""
        UPDATE staff SET
          staff_no = #{staffNo},
          name = #{name},
          gender = #{gender},
          phone = #{phone},
          email = #{email},
          is_active = #{isActive},
          updated_at = NOW()
        WHERE staff_id = #{staffId}
        """)
    int update(Staff staff);

    @Delete("DELETE FROM staff WHERE staff_id = #{id}")
    int delete(Long id);
}
