// src/api/upload.ts
import request from '@/utils/request';

/**
 * 文件上传接口
 * API: POST /api/upload
 */
export const uploadFile = (file: File) => {
    const formData = new FormData();
    formData.append('file', file);

    return request.post('/api/upload', formData, {
        headers: {
            'Content-Type': 'multipart/form-data',
        },
        timeout: 30000, // 文件上传需要更长时间
    });
};

/**
 * 文件上传响应类型
 */
export interface UploadResponse {
    url: string; // 文件访问URL
    filename: string; // 文件名
    size: number; // 文件大小
}