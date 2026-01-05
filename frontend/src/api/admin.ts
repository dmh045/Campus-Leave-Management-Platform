// src/api/admin.ts
import request from '@/utils/request';
import * as XLSX from 'xlsx';

export interface Term {
  termId: number;
  termCode: string;
  termName: string;
  startDate: string; // YYYY-MM-DD
  endDate: string;   // YYYY-MM-DD
  isCurrent: boolean;
}

export interface Clazz {
  classId: number;
  classCode: string;
  className: string;
  major: string;
  gradeYear: number;
  counselorId: number | null;
}

export interface Course {
  courseId: number;
  courseCode: string;
  courseName: string;
  credit: number;     // 后端 insert 必填（可传 0）
  totalHours: number; // 后端 insert 必填（可传 0）
}

export interface Offering {
  offeringId: number;
  termId: number;
  courseId: number;
  classId: number;
  teacherId: number;
  weekDay: number;        // 1-7
  sectionStart: number;
  sectionEnd: number;
  classroom?: string | null;
}

export interface SemesterConfig {
  name: string;      // "2025-2026学年 第一学期"
  startDate: string; // YYYY-MM-DD
  totalWeeks: number;
}

export interface ImportResult {
  totalCourses: number;
  totalTeachers: number;
  totalClasses: number;
  warnings: string[];
}

export interface TimetableRow {
  classCode?: string;   // 可选：不填就默认导入到当前选中的班级
  courseCode: string;
  courseName: string;
  teacherId: number;
  weekDay: number;      // 1-7 或 “周一”
  sectionStart: number;
  sectionEnd: number;
  classroom?: string;
}

/** ===================== 后端 CRUD 接口 ===================== **/

export const listTerms = () => request.get<Term[]>('/admin/terms');
export const createTerm = (term: Partial<Term>) => request.post<Term>('/admin/terms', term);
export const updateTerm = (id: number, term: Partial<Term>) => request.put<Term>(`/admin/terms/${id}`, term);
export const openTerm = (id: number) => request.post<Term>(`/admin/terms/${id}/open`);
export const closeTerm = (id: number) => request.post<Term>(`/admin/terms/${id}/close`);

export const listClasses = () => request.get<Clazz[]>('/admin/classes');

export const listCourses = () => request.get<Course[]>('/admin/courses');
export const createCourse = (course: Partial<Course>) => request.post<Course>('/admin/courses', course);

export const listOfferingsByTermClass = (termId: number, classId: number) =>
  request.get<Offering[]>('/admin/offerings/by-term-class', { params: { termId, classId } });

export const createOffering = (offering: Partial<Offering>) =>
  request.post<Offering>('/admin/offerings', offering);

export const deleteOffering = (id: number) =>
  request.delete<void>(`/admin/offerings/${id}`);

/** ===================== Excel 解析（前端本地完成） ===================== **/

function pick(obj: Record<string, any>, keys: string[]) {
  for (const k of keys) {
    if (k in obj && obj[k] !== undefined && obj[k] !== null && String(obj[k]).trim() !== '') return obj[k];
  }
  return undefined;
}

function toInt(v: any) {
  if (v === undefined || v === null || v === '') return NaN;
  const n = Number(String(v).trim());
  return Number.isFinite(n) ? Math.trunc(n) : NaN;
}

function parseWeekDay(v: any): number {
  if (v === undefined || v === null) return NaN;
  if (typeof v === 'number') return v;
  const s = String(v).trim();
  // 允许：1-7 / 周一..周日 / 星期一..星期日
  if (/^\d+$/.test(s)) return Number(s);
  if (s.includes('一')) return 1;
  if (s.includes('二')) return 2;
  if (s.includes('三')) return 3;
  if (s.includes('四')) return 4;
  if (s.includes('五')) return 5;
  if (s.includes('六')) return 6;
  if (s.includes('日') || s.includes('天')) return 7;
  return NaN;
}

/**
 * 解析 Excel 的第一张 Sheet
 * 你需要保证列名大致对应下面这些（允许中文别名）：
 * - 班级代码: classCode / 班级代码 / 班级 / 班级编号   （可选）
 * - 课程代码: courseCode / 课程代码 / 课程编号 / 课程号 （必填）
 * - 课程名称: courseName / 课程名称 / 课程名           （必填）
 * - 教师ID : teacherId / 教师ID / 教师工号 / 任课教师工号（必填）
 * - 星期   : weekDay / 星期 / 周几 / 星期几             （必填）
 * - 开始节 : sectionStart / 开始节次 / 起始节           （必填）
 * - 结束节 : sectionEnd / 结束节次 / 终止节             （必填）
 * - 教室   : classroom / 教室 / 上课地点                （可选）
 */
export const parseTimetableFile = async (
  file: File
): Promise<{ result: ImportResult; rows: TimetableRow[] }> => {
  const warnings: string[] = [];

  if (!file.name.endsWith('.xlsx') && !file.name.endsWith('.xls')) {
    throw new Error('仅支持 Excel 文件 (.xlsx, .xls)');
  }

  const buf = await file.arrayBuffer();
  const wb = XLSX.read(buf, { type: 'array' });
  const sheetName = wb.SheetNames?.[0];
  if (!sheetName) throw new Error('Excel 中找不到 Sheet');

  const sheet = wb.Sheets[sheetName];
  const raw = XLSX.utils.sheet_to_json<Record<string, any>>(sheet, { defval: '' });

  const rows: TimetableRow[] = [];

  raw.forEach((r, idx) => {
    const line = idx + 2; // 近似行号（header 算 1 行）
    const classCode = pick(r, ['classCode', '班级代码', '班级', '班级编号']);
    const courseCode = pick(r, ['courseCode', '课程代码', '课程编号', '课程号']);
    const courseName = pick(r, ['courseName', '课程名称', '课程名']);
    const teacherIdRaw = pick(r, ['teacherId', '教师ID', '教师工号', '任课教师工号']);
    const weekDayRaw = pick(r, ['weekDay', '星期', '周几', '星期几']);
    const sectionStartRaw = pick(r, ['sectionStart', '开始节次', '起始节', '开始节']);
    const sectionEndRaw = pick(r, ['sectionEnd', '结束节次', '终止节', '结束节']);
    const classroom = pick(r, ['classroom', '教室', '上课地点']);

    if (!courseCode) warnings.push(`行号 ${line}: 缺少课程代码 courseCode`);
    if (!courseName) warnings.push(`行号 ${line}: 缺少课程名称 courseName`);

    const teacherId = toInt(teacherIdRaw);
    if (!Number.isFinite(teacherId)) warnings.push(`行号 ${line}: 教师ID 无法解析为数字（teacherId=${teacherIdRaw}）`);

    const weekDay = parseWeekDay(weekDayRaw);
    if (!(weekDay >= 1 && weekDay <= 7)) warnings.push(`行号 ${line}: 星期无效（weekDay=${weekDayRaw}）`);

    const sectionStart = toInt(sectionStartRaw);
    const sectionEnd = toInt(sectionEndRaw);
    if (!(sectionStart >= 1)) warnings.push(`行号 ${line}: 开始节次无效（sectionStart=${sectionStartRaw}）`);
    if (!(sectionEnd >= sectionStart)) warnings.push(`行号 ${line}: 结束节次无效（sectionEnd=${sectionEndRaw}）`);

    if (!courseCode || !courseName || !Number.isFinite(teacherId) || !(weekDay >= 1 && weekDay <= 7)) return;
    if (!Number.isFinite(sectionStart) || !Number.isFinite(sectionEnd) || sectionEnd < sectionStart) return;

    rows.push({
      classCode: classCode ? String(classCode).trim() : undefined,
      courseCode: String(courseCode).trim(),
      courseName: String(courseName).trim(),
      teacherId,
      weekDay,
      sectionStart,
      sectionEnd,
      classroom: classroom ? String(classroom).trim() : undefined
    });
  });

  const uniqCourse = new Set(rows.map(r => r.courseCode)).size;
  const uniqTeacher = new Set(rows.map(r => r.teacherId)).size;
  const uniqClass = new Set(rows.map(r => r.classCode || '__NO_CLASS__')).size - (rows.some(r => !r.classCode) ? 1 : 0);

  return {
    result: {
      totalCourses: uniqCourse,
      totalTeachers: uniqTeacher,
      totalClasses: uniqClass,
      warnings
    },
    rows
  };
};

/**
 * “发布学期数据”的真做法：创建/更新 term，然后 open，并把其他 isCurrent 的 term close 掉
 * （后端 openTerm 不会自动把其他学期关掉，所以前端要补这一刀）
 */
export const publishSemesterData = async (config: SemesterConfig): Promise<void> => {
  const terms = await listTerms();

  const genTermCode = (name: string) => {
    const m = name.match(/(\d{4})\s*-\s*(\d{4})/);
    const n = name.match(/(第一|第1|1)\s*学期/) ? '1' : (name.match(/(第二|第2|2)\s*学期/) ? '2' : '1');
    if (m) return `${m[1]}-${m[2]}-${n}`;
    return `TERM-${Date.now()}`;
  };

  const start = config.startDate;
  const end = (() => {
    const d = new Date(start);
    d.setDate(d.getDate() + config.totalWeeks * 7 - 1);
    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
  })();

  // 尝试按 termCode 找同一个学期（没有就创建）
  const termCode = genTermCode(config.name);
  const existed = terms.find(t => t.termCode === termCode);

  const payload: Partial<Term> = {
    termCode,
    termName: config.name,
    startDate: start,
    endDate: end
  };

  const saved = existed
    ? await updateTerm(existed.termId, payload)
    : await createTerm(payload);

  // 先把其他 current 的关掉，再把新的打开
  const currentOthers = (await listTerms()).filter(t => t.isCurrent && t.termId !== saved.termId);
  for (const t of currentOthers) {
    await closeTerm(t.termId);
  }
  await openTerm(saved.termId);
};
