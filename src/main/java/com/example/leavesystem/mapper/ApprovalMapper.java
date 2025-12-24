package com.example.leavesystem.mapper;

import com.example.leavesystem.entity.Approval;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

import java.util.List;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ApprovalMapper {

    @Insert("""
        INSERT INTO approval
          (leave_id, approver_id, approver_role, action, comment, created_at)
        VALUES
          (#{leaveId}, #{approverId}, #{approverRole}, #{action}, #{comment}, #{createdAt})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "approvalId")
    int insert(Approval approval);

    @Select("SELECT * FROM approval WHERE leave_id = #{leaveId} ORDER BY created_at ASC")
    List<Approval> findByLeaveId(Long leaveId);
}
