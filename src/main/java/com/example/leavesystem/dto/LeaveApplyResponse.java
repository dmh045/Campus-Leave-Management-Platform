package com.example.leavesystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LeaveApplyResponse {
    private Long leaveId;
    private String status;
}
