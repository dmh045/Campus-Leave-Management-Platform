package com.example.leavesystem.entity;

import lombok.Data;

@Data
public class StaffRole {

    private Long id;
    private Long staffId;
    private String roleCode;   // admin / counselor / teacher
    private String remark;
}
