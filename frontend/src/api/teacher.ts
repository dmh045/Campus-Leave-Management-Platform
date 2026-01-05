// src/api/teacher.ts
import request from '@/utils/request';

/**
 * 后端 DTO: /api/leaves/pending/teacher 返回的单条记录
 * 这里按当前后端实现字段命名（TeacherPendingImpactDTO）
 */
export interface TeacherPendingImpactDTO {
  impactId: number;
  leaveId: number;
  studentId: number;
  studentName: string;
  className: string;
  courseId: number;
  courseName: string;
  courseDate: string;      // YYYY-MM-DD
  sectionStart: number;
  sectionEnd: number;
  leaveType: string;       // SICK / PERSONAL / PUBLIC
  reason: string;
  leaveStatus: string; // PENDING_TEACHER / APPROVED / ...
  proofUrl?: string;

}

/**
 * 前端页面（LeaveConfirm.vue）使用的任务结构
 * 注意：页面里用 status=4 表示“待确认”，status=5 表示“已确认”（本地确认后标记）
 */
export interface LeaveImpactTask {
  id: string; // impactId 字符串，用于 v-model 勾选
  impactId: number;
  leaveId: number;

  studentId: string;
  studentName: string;

  // 页面里“受影响课程”列用的是 className，这里填 courseName 以保持模板不改
  className: string;

  courseId: number;
  courseName: string;

  date: string;      // courseDate
  classTime: string; // 第x-y节

  leaveType: 'sick' | 'personal' | 'public';
  reason: string;
  proofUrl?: string;

  status: 4 | 5;

  _raw?: TeacherPendingImpactDTO;
}

/**
 * 导出教师考勤表
 * GET /api/teacher/attendance/export
 */
export const exportTeacherAttendance = (params: {
    courseId?: number;
    classId?: number;
    startDate: string;
    endDate: string;
    fullScore?: number;
    leaveScore?: number;
    absentScore?: number;
}) => {
    return request.get<Blob>('/api/teacher/attendance/export', {
        params,
        responseType: 'blob'
    });
};

/**
 * 获取教师课程列表
 * GET /api/teacher/courses
 */
export const getTeacherCourses = () => {
  return request.get<any[]>('/api/teacher/courses');
};

const pickTeacherId = (teacherId?: number) => {
  if (typeof teacherId === 'number' && !Number.isNaN(teacherId) && teacherId > 0) return teacherId;

  const fromLS = localStorage.getItem('userId') || localStorage.getItem('teacherId');
  const tid = fromLS ? Number(fromLS) : 0;
  return Number.isFinite(tid) && tid > 0 ? tid : 0;
};

const mapLeaveType = (t: any): LeaveImpactTask['leaveType'] => {
  const s = String(t || '').toUpperCase();
  if (s === 'SICK') return 'sick';
  if (s === 'PERSONAL') return 'personal';
  return 'public';
};

const toTask = (dto: any): LeaveImpactTask => {
  const impactId = Number(dto?.impactId);
  const sectionStart = Number(dto?.sectionStart);
  const sectionEnd = Number(dto?.sectionEnd);

  const classTime =
    Number.isFinite(sectionStart) && Number.isFinite(sectionEnd) && sectionStart > 0 && sectionEnd > 0
      ? `第${sectionStart}-${sectionEnd}节`
      : '';

  const task: LeaveImpactTask = {
    id: String(dto?.impactId ?? ''),
    impactId,
    leaveId: Number(dto?.leaveId ?? 0),

    studentId: String(dto?.studentId ?? ''),
    studentName: String(dto?.studentName ?? ''),

    className: String(dto?.courseName ?? dto?.className ?? ''),

    courseId: Number(dto?.courseId ?? 0),
    courseName: String(dto?.courseName ?? ''),

    date: String(dto?.courseDate ?? ''),
    classTime,

    leaveType: mapLeaveType(dto?.leaveType),
    reason: String(dto?.reason ?? ''),
    proofUrl: String((dto as any).proofUrl || ''),

    status: 4,
    _raw: dto,
  };

  return task;
};

/**
 * 任课老师查看待确认请假列表
 * API: GET /api/leaves/pending/teacher?teacherId=xxx
 */
export const getTeacherTodos = async (teacherId?: number): Promise<LeaveImpactTask[]> => {
  const tid = pickTeacherId(teacherId);
  if (!tid) return [];

  const list = await request.get<TeacherPendingImpactDTO[]>('/api/leaves/pending/teacher', {
    params: { teacherId: tid }
  });

  if (!Array.isArray(list)) return [];
  return list.map(toTask);
};

/**
 * 老师确认某一节课的请假（知晓）
 * API: POST /api/leaves/impact/{impactId}/teacher-confirm
 */
export const confirmImpact = (impactId: number | string, teacherId?: number, remark: string = '') => {
  const tid = pickTeacherId(teacherId);
  return request.post(`/api/leaves/impact/${impactId}/teacher-confirm`, {
    teacherId: tid,
    remark
  });
};

// 兼容旧命名（若你其它地方用到了）
export const confirmLeaveImpact = confirmImpact;

/**
 * 老师发起签到
 * API: POST /api/attendance/session/start
 */
export const startCheckInSession = (data: {
  teacherId: number;
  offeringId: number;
  courseDate: string;
  sectionStart: number;
  sectionEnd: number;
  durationMinutes: number;
}) => {
  return request.post('/api/attendance/session/start', data);
};

/**
 * 老师获取自己的签到场次
 * API: GET /api/attendance/teacher/sessions
 */
export const getTeacherSessions = (teacherId: number, startDate: string, endDate: string) => {
  return request.get('/api/attendance/teacher/sessions', {
    params: { teacherId, startDate, endDate }
  });
};

export interface TeacherDayCourseDTO {
    offeringId: number;
    courseId: number;
    courseName: string;
    classId: number;
    className: string;
    weekDay: number;
    sectionStart: number;
    sectionEnd: number;
    classroom?: string;
}

/**
 * 教师某天课表
 * GET /api/timetable/teacher/day?date=YYYY-MM-DD （teacher 角色可不传 teacherId）
 */
export const getTeacherDayTimetable = (date: string, teacherId?: number) => {
    return request.get<TeacherDayCourseDTO[]>('/api/timetable/teacher/day', {
        params: teacherId ? { date, teacherId } : { date },
    });
};

// =========================
// Attendance（签到）相关
// =========================

export interface AttendanceSessionSummary {
  sessionId: number;
  offeringId: number;
  courseId?: number;
  courseName: string;
  classId?: number;
  className: string;
  courseDate: string; // YYYY-MM-DD
  sectionStart: number;
  sectionEnd: number;
  status: string; // OPEN / CLOSED
  allowStartTime?: string;
  allowEndTime?: string;
  shouldAttendCount?: number;
  checkedInCount?: number;
  leaveCount?: number;
}

export interface AttendanceSessionDetail {
  sessionId: number;
  courseName?: string;
  className?: string;
  courseDate?: string;
  sectionStart?: number;
  sectionEnd?: number;
  status?: string;
  allowStartTime?: string;
  allowEndTime?: string;
  students?: Array<{
    studentId: number;
    studentNo?: string;
    studentName: string;
    status: string; // PRESENT / ABSENT / LEAVE
    checkinTime?: string;
    leaveId?: number;
  }>;
}

/**
 * 获取某次签到场次详情
 * GET /api/attendance/session/{sessionId}/detail?teacherId=xxx
 */
export const getSessionDetail = (sessionId: number, teacherId: number) => {
  return request.get<AttendanceSessionDetail>(`/api/attendance/session/${sessionId}/detail`, {
    params: { teacherId },
  });
};

/**
 * 关闭签到场次并生成缺勤
 * POST /api/attendance/session/{sessionId}/close?teacherId=xxx
 */
export const closeSession = (sessionId: number, teacherId: number) => {
  return request.post<void>(`/api/attendance/session/${sessionId}/close`, null, {
    params: { teacherId },
  });
};
