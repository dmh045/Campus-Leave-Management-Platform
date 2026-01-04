import request from '@/utils/request';

export interface CounselorPendingLeaveDTO {
  leaveId: number;
  studentId: number;
  studentName: string;
  className: string;
  leaveType: string; // SICK / PERSONAL / PUBLIC
  reason: string;
  startTime: string; // ISO
  endTime: string;   // ISO
  status: string;
  proofUrl?: string;
}

export type CounselorAction = 'AGREE' | 'REJECT' | 'RETURN';

export interface CounselorApproveRequest {
  counselorId: number;
  action: CounselorAction;
  comment?: string;
}

export interface CounselorBatchApproveRequest extends CounselorApproveRequest {
  leaveIds: number[];
}

/**
 * 辅导员查看待审批列表
 * GET /api/leaves/pending/counselor?counselorId=xxx
 */
export const getCounselorPendingLeaves = (counselorId: number) => {
  return request.get<CounselorPendingLeaveDTO[]>('/api/leaves/pending/counselor', {
    params: { counselorId }
  });
};

/**
 * 辅导员审批单条请假
 * POST /api/leaves/{id}/counselor-approve
 */
export const counselorApproveLeave = (leaveId: number | string, payload: CounselorApproveRequest) => {
  return request.post<void>(`/api/leaves/${leaveId}/counselor-approve`, payload);
};

/**
 * 辅导员批量审批
 * POST /api/leaves/counselor-approve/batch
 */
export const counselorBatchApproveLeaves = (payload: CounselorBatchApproveRequest) => {
  return request.post<void>('/api/leaves/counselor-approve/batch', payload);
};
