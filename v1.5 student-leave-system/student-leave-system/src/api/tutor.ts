// src/api/tutor.ts
import request from '@/utils/request';

/**
 * 辅导员查看待审批列表
 * API: GET /api/leaves/pending/counselor
 */
export const getTutorTodos = (counselorId: number) => {
  return request.get('/api/leaves/pending/counselor', {
    params: { counselorId }
  });
};

/**
 * 单个审批
 * API: POST /api/leaves/{id}/counselor-approve
 */
export const auditLeave = (id: number | string, data: { counselorId: number, action: 'AGREE' | 'REJECT' | 'RETURN', comment: string }) => {
  return request.post(`/api/leaves/${id}/counselor-approve`, data);
};

/**
 * 批量审批
 * API: POST /api/leaves/counselor-approve/batch
 */
export const batchAuditLeave = (data: { counselorId: number, action: string, comment: string, leaveIds: number[] }) => {
  return request.post('/api/leaves/counselor-approve/batch', data);
};

/**
 * 批量发起公假
 * API: POST /api/leaves/public/batch
 */
export const createBatchPublicLeave = (data: any) => {
  return request.post('/api/leaves/public/batch', data);
};