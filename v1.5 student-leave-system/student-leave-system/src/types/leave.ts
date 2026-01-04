// src/types/leave.ts

// 1. 请假/考勤状态枚举（对应数据库状态值）
export enum LeaveStatus {
  DRAFT = 0,               // 草稿
  PENDING_COUNSELOR = 1,   // 待辅导员审核
  RETURN_SUPPLEMENT = 2,   // 退回补充
  REJECTED = 3,            // 已驳回
  PENDING_TEACHER = 4,     // 待任课确认
  EFFECTIVE = 5,           // 已生效
  CANCELLED = 6,           // 已取消
  ENDED = 7,               // 已结束（超时未修改）
  
  // 特殊状态（用于课表展示逻辑）
  PUBLIC_LEAVE = 100,      // 公假
  ABSENCE = 101,           // 缺勤/待补假
  PRESENT = 200            // 到课
}

// 2. 基础请假单信息接口
export interface LeaveApplication {
  id: string;
  studentId: string;
  studentName: string;
  type: 'sick' | 'personal' | 'public'; // 病假/事假/公假
  reason: string;
  startTime: string; // ISO String
  endTime: string;   // ISO String
  status: LeaveStatus;
  createTime: string;
  attachments?: string[]; // 附件URL
  rejectReason?: string;  // 驳回或退回理由
}

// 3. 课程节次影响接口（用于任课老师确认）
export interface ClassImpact {
  id: string;
  leaveId: string;
  courseName: string;
  teacherId: string;
  teacherName: string;
  classTime: string; // 具体上课时间
  isConfirmed: boolean;
}