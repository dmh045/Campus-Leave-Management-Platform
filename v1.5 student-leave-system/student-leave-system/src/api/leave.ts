// src/api/leave.ts
import request from '@/utils/request';

// --- 1. 定义与后端接口匹配的参数类型 ---

/**
 * 对应 OpenAPI: ImpactItem (受影响的课程节次)
 */
export interface ImpactItem {
  offeringId: number;
  courseDate: string; // "YYYY-MM-DD"
  sectionStart: number;
  sectionEnd: number;
}

/**
 * 对应 OpenAPI: LeaveApplyRequest (发起请假参数)
 */
export interface LeaveSubmitForm {
  studentId: number;
  termId: number; // 暂时可以写死，或者从 Store 取
  leaveType: 'SICK' | 'PERSONAL' | 'PUBLIC'; // 后端要求大写
  applyChannel: 'BY_COURSE' | 'BY_TIME';
  reason: string;
  proofUrl?: string; // 附件 URL
  startTime: string; // "YYYY-MM-DD HH:mm:ss"
  endTime: string;
  impacts?: ImpactItem[]; // 如果是按课程请假，必填
}

// --- 2. 真实 API 方法 ---

/**
 * 学生发起请假
 * API: POST /api/leaves/apply
 */
export const createLeave = (data: LeaveSubmitForm) => {
  return request.post('/api/leaves/apply', data);
};

/**
 * 获取我的请假列表
 * API: GET /api/leaves/my
 * 注意：后端接口目前只支持按 studentId 查全部，暂无分页参数
 */
export const getMyLeaveList = (studentId: number) => {
  return request.get('/api/leaves/my', {
    params: { studentId }
  });
};

/**
 * 获取请假详情（包含审批流）
 * API: GET /api/leaves/{id}/detail
 */
export const getLeaveDetail = (id: string | number) => {
  return request.get(`/api/leaves/${id}/detail`);
};

/**
 * 重新提交（被退回后）
 * API: PUT /api/leaves/{id}/resubmit
 */
export const resubmitLeave = (id: string | number, data: LeaveSubmitForm) => {
  return request.put(`/api/leaves/${id}/resubmit`, data);
};