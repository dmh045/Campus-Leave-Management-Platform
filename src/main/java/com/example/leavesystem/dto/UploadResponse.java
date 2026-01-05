package com.example.leavesystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponse {
    private String url;      // 文件访问URL（给前端保存到 proofUrl）
    private String filename; // 原始文件名
    private long size;       // 文件大小
}
