import request from '@/utils/request';

export interface StudentCheckinRequest {
  studentId: number;
  /** 签到token（来自老师端二维码） */
  token: string;
}

/**
 * 学生扫码/输入口令签到
 * POST /api/attendance/checkin
 */
export const studentCheckin = (payload: StudentCheckinRequest) => {
  return request.post<void>('/api/attendance/checkin', payload);
};
