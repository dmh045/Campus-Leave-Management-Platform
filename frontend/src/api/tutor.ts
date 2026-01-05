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

// === 辅导员：公假页面所需数据接口 ===
export interface CounselorClassDTO {
    classId: number;
    classCode?: string;
    className: string;
}

export interface CounselorStudentDTO {
    studentId: number;
    studentNo: string;
    name: string;
    classId: number;
}

export interface CounselorOfferingDTO {
    offeringId: number;
    courseId: number;
    courseName: string;
    teacherId: number;
    teacherName: string;
    classId: number;
    className: string;
    weekDay: number;        // 1-7 (周一=1)
    sectionStart: number;
    sectionEnd: number;
    classroom?: string;
}

/** 辅导员名下班级 */
export const getCounselorClasses = () => {
    return request.get<CounselorClassDTO[]>('/api/counselor/classes');
};

/** 某班学生名单 */
export const getCounselorClassStudents = (classId: number) => {
    return request.get<CounselorStudentDTO[]>(`/api/counselor/classes/${classId}/students`);
};

/** 某班某学期的开课/课表(Offering) */
export const getCounselorOfferingsByTermClass = (termId: number, classId: number) => {
    return request.get<CounselorOfferingDTO[]>('/api/counselor/offerings/by-term-class', {
        params: { termId, classId }
    });
};
