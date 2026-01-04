// src/api/stats.ts
import request from '@/utils/request';

export interface ClassLeaveStatsDTO {
  classId: number;
  className: string;
  totalLeaves: number;

  sickCount: number;
  affairCount: number;
  publicCount: number;

  pendingCount: number;
  approvedCount: number;
  rejectedCount: number;
}

/**
 * 班级请假统计（看板）
 * GET /api/stats/class-leave?classId=1&startDate=2024-10-01&endDate=2024-10-31
 */
export const getClassLeaveStats = (params: { classId: number; startDate: string; endDate: string }) => {
  return request.get<ClassLeaveStatsDTO>('/api/stats/class-leave', { params });
};
/**
 * 导出班级请假情况表
 * API: GET /api/stats/class-leave/export
 */
export const exportClassLeaveDetails = (params: { classId: number; startDate: string; endDate: string }) => {
  return request.get('/api/stats/class-leave/export', {
    params,
    responseType: 'blob' // 重要：指定响应类型为blob
  });
};
