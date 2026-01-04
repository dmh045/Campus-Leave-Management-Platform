// src/api/course.ts
import request from '@/utils/request';
import dayjs from 'dayjs';

export interface CourseCell {
  // === 后端字段（StudentDayCourseDTO / StudentDayCourseMapper） ===
  offeringId: number;
  courseId: number;
  courseName: string;
  weekDay: number; // 1-7
  sectionStart: number;
  sectionEnd: number;
  classroom: string;

  // === 前端课表组件兼容字段（Timetable.vue 在用）===
  id: string;
  teacherName: string;
  location: string;
  dayOfWeek: number;
  section: number;
  duration: number;
  status: number;
}

/**
 * 获取某一天课表
 * API: GET /api/timetable/student/day
 *
 * 注意：后端 studentId 是可选的（学生角色会强制以 token 为准），所以这里也做成可选参数。
 */
export const getDayTimetable = (date: string, studentId?: number) => {
  const params: Record<string, any> = { date };
  if (typeof studentId === 'number') params.studentId = studentId;

  return request.get<any[]>('/api/timetable/student/day', { params });
};

/**
 * 🔥 聚合接口：获取整周课表
 * 前端传 week (第几周)，这里自动算出这周7天的日期，并发请求后端
 */
export const getStudentTimetable = async (week: number, options?: { studentId?: number; termStartDate?: string }) => {
  // 1) 学期开始日期：优先 options，其次 localStorage，最后给一个可用默认值
  const termStartDateStr =
      options?.termStartDate ||
      localStorage.getItem('termStartDate') ||
      localStorage.getItem('term_start_date') ||
      '2025-09-01';

  const termStartDate = dayjs(termStartDateStr);

  // 2) 直接获取学生ID：优先 options，其次 localStorage（同时尝试userId和studentId）
  const sidFromStorage = localStorage.getItem('userId') || localStorage.getItem('studentId');
  const sid = options?.studentId ?? (sidFromStorage ? Number(sidFromStorage) : undefined);

  // 3) 计算目标周的周一到周日
  const currentWeekStart = termStartDate.add(week - 1, 'week');

  const promises: Array<Promise<any[]>> = [];
  for (let i = 0; i < 7; i++) {
    const dateStr = currentWeekStart.add(i, 'day').format('YYYY-MM-DD');
    promises.push(getDayTimetable(dateStr, sid));
  }

  // 4) 等待所有请求完成
  const results = await Promise.all(promises);

  // 5) 拍平数据并适配前端格式
  const allCourses = results.flatMap((arr) => (Array.isArray(arr) ? arr : []));

  return allCourses.map((item: any) => ({
    // 后端字段
    offeringId: item.offeringId,
    courseId: item.courseId,
    courseName: item.courseName,
    weekDay: item.weekDay,
    sectionStart: item.sectionStart,
    sectionEnd: item.sectionEnd,
    classroom: item.classroom,

    // 前端字段
    id: String(item.offeringId),
    teacherName: item.teacherName || '未知',
    location: item.classroom || '',
    dayOfWeek: item.weekDay,
    section: item.sectionStart,
    duration: (item.sectionEnd - item.sectionStart) + 1,
    status: 200,
  })) as CourseCell[];
};