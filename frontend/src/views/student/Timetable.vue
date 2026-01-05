<template>
  <div class="space-y-4">
    <div class="bg-white rounded-lg shadow-sm p-4 flex items-center justify-between">
      <div class="flex items-center gap-4">
        <button @click="changeWeek(-1)" class="p-2 hover:bg-gray-100 rounded-lg transition-colors">
          <ChevronLeft class="w-5 h-5 text-gray-600" />
        </button>
        <span class="font-medium text-gray-900">第 {{ currentWeek }} 周</span>
        <button @click="changeWeek(1)" class="p-2 hover:bg-gray-100 rounded-lg transition-colors">
          <ChevronRight class="w-5 h-5 text-gray-600" />
        </button>
        <span class="text-gray-500 text-sm">2025-2026学年 第一学期</span>
      </div>

      <div class="flex items-center gap-3">
        <span
            v-if="selectedCells.length > 0"
            class="text-sm text-gray-600 font-medium bg-blue-50 px-2 py-1 rounded text-blue-700"
        >
          已选择 {{ selectedCells.length }} 节课
        </span>
        <button
            @click="handleNewLeave"
            class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg flex items-center gap-2 transition-colors font-medium text-sm shadow-sm"
        >
          <Plus class="w-4 h-4" />
          发起请假
        </button>
      </div>
    </div>

    <div class="bg-white rounded-lg shadow-sm p-4 border border-gray-100">
      <div class="flex items-center gap-6 flex-wrap">
        <span class="text-sm text-gray-500 font-medium">课程状态：</span>
        <div v-for="(label, key) in statusLabels" :key="key" class="flex items-center gap-2">
          <div :class="['w-3 h-3 rounded-full shadow-sm', getLegendColorClass(key)]"></div>
          <span class="text-sm text-gray-600">{{ label }}</span>
        </div>
      </div>
    </div>

    <div class="bg-white rounded-xl shadow-sm overflow-hidden border border-gray-200">
      <div class="overflow-x-auto">
        <table class="w-full border-collapse table-fixed">
          <thead>
          <tr class="bg-gray-50/80 border-b border-gray-200">
            <th class="w-14 py-3 text-center text-xs font-semibold text-gray-500 border-r border-gray-100">
              #
            </th>
            <th class="w-24 py-3 text-center text-xs font-semibold text-gray-500 border-r border-gray-100">
              时间
            </th>
            <th
                v-for="day in weekDayLabels"
                :key="day"
                class="py-3 px-2 text-center text-sm font-semibold text-gray-700 border-r border-gray-100 last:border-r-0 min-w-[130px]"
            >
              {{ day }}
            </th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="sectionNum in 14" :key="sectionNum" class="border-b border-gray-100 last:border-b-0">
            <td
                class="h-16 text-center text-sm font-bold text-gray-400 bg-gray-50/30 border-r border-gray-100"
            >
              {{ sectionNum }}
            </td>

            <td class="h-16 text-center border-r border-gray-100 px-1 py-2">
              <div class="flex flex-col items-center justify-center h-full text-xs text-gray-500 font-mono">
                <span>{{ timeSlots[sectionNum - 1].start }}</span>
                <span class="w-px h-2 bg-gray-300 my-0.5"></span>
                <span>{{ timeSlots[sectionNum - 1].end }}</span>
              </div>
            </td>

            <template v-for="dayIdx in 7" :key="dayIdx">
              <td
                  v-if="checkCellState(dayIdx, sectionNum).isStart"
                  :rowspan="checkCellState(dayIdx, sectionNum).duration"
                  class="p-1 border-r border-gray-100 last:border-r-0 align-top bg-white hover:bg-gray-50/30 transition-colors"
              >
                <div
                    @click="handleCellClick(checkCellState(dayIdx, sectionNum).course!)"
                    :class="[
                      'w-full h-full rounded-lg p-3 cursor-pointer border-l-[5px] shadow-sm hover:shadow-md transition-all duration-200 flex flex-col justify-between',
                      getStatusStyles(checkCellState(dayIdx, sectionNum).course!.status).bg,
                      getStatusStyles(checkCellState(dayIdx, sectionNum).course!.status).border,
                      isSelected(checkCellState(dayIdx, sectionNum).course!)
                        ? 'ring-2 ring-offset-1 ring-blue-500 transform scale-[0.98]'
                        : ''
                    ]"
                >
                  <div>
                    <div class="flex justify-between items-start gap-1 mb-1">
                      <div class="font-bold text-[13px] leading-snug text-gray-800 line-clamp-2">
                        {{ checkCellState(dayIdx, sectionNum).course!.courseName }}
                      </div>
                      <span
                          v-if="checkCellState(dayIdx, sectionNum).course!.status !== 200"
                          :class="[
                            'text-[10px] px-1.5 py-0.5 rounded-full font-medium whitespace-nowrap',
                            getStatusStyles(checkCellState(dayIdx, sectionNum).course!.status).tag
                          ]"
                      >
                          {{ getStatusLabel(checkCellState(dayIdx, sectionNum).course!.status) }}
                        </span>
                    </div>

                    <div class="text-xs text-gray-600 flex items-center gap-1.5">
                      <User class="w-3.5 h-3.5 opacity-70" />
                      {{ checkCellState(dayIdx, sectionNum).course!.teacherName }}
                    </div>
                  </div>

                  <div>
                    <div
                        class="inline-flex items-center gap-1 bg-white/60 px-2 py-1 rounded-md text-[11px] text-gray-600 font-medium shadow-sm border border-black/5"
                    >
                      <MapPin class="w-3 h-3 text-gray-500" />
                      {{ checkCellState(dayIdx, sectionNum).course!.location }}
                    </div>
                  </div>
                </div>
              </td>

              <td
                  v-else-if="!checkCellState(dayIdx, sectionNum).isOccupied"
                  class="h-16 border-r border-gray-100 last:border-r-0 hover:bg-gray-50/50 transition-colors"
              ></td>
            </template>
          </tr>
          </tbody>
        </table>
      </div>
    </div>

    <LeaveDialog
        v-model="showLeaveDialog"
        :courses="selectedCourseObjects"
        :studentId="currentStudentId"
        :termId="currentTermId"
        :courseDate="currentCourseDate"
        :editLeaveId="activeEditLeaveId"
        @submit="handleLeaveSubmitted"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ChevronLeft, ChevronRight, Plus, MapPin, User } from 'lucide-vue-next';
import { getStudentTimetable, type CourseCell } from '@/api/course';
import { LeaveStatus } from '@/types/leave';
import { ElMessage } from 'element-plus';
import LeaveDialog from '@/components/student/LeaveDialog.vue';
import dayjs from 'dayjs';
import { getLeaveDetail } from '@/api/leave';

const currentWeek = ref(1);
const courseList = ref<CourseCell[]>([]);
const selectedCells = ref<string[]>([]);
const weekDayLabels = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];

const route = useRoute();
const router = useRouter();

const showLeaveDialog = ref(false);
const activeEditLeaveId = ref<number | undefined>(undefined);

const resubmitIdFromRoute = computed<number | null>(() => {
  const q: any = route.query.resubmitId;
  const raw = Array.isArray(q) ? q[0] : q;
  const n = raw ? Number(raw) : NaN;
  return Number.isFinite(n) && n > 0 ? n : null;
});

const termStartDateStr = computed(() => {
  return localStorage.getItem('termStartDate') || localStorage.getItem('term_start_date') || '2025-09-01';
});

const getWeekStart = (week: number) => {
  const termStart = dayjs(termStartDateStr.value);
  return termStart.add(week - 1, 'week');
};

const calcCourseDate = (dayOfWeek: number) => {
  const weekStart = getWeekStart(currentWeek.value);
  return weekStart.add(dayOfWeek - 1, 'day').format('YYYY-MM-DD');
};

const selectedCourseObjects = computed(() => {
  return courseList.value
      .filter((c) => selectedCells.value.includes(c.id))
      .map((c: any) => ({
        ...c,
        courseDate: c.courseDate || calcCourseDate(c.dayOfWeek),
      }));
});

const currentStudentId = computed(() => {
  const userId = localStorage.getItem('userId');
  return userId ? Number(userId) : undefined;
});

const currentTermId = computed(() => {
  const termId = localStorage.getItem('termId');
  return termId ? Number(termId) : 2;
});

const currentCourseDate = computed(() => {
  if (selectedCourseObjects.value.length > 0) return selectedCourseObjects.value[0].courseDate;
  return dayjs().format('YYYY-MM-DD');
});

const handleNewLeave = () => {
  if (selectedCells.value.length === 0) {
    ElMessage.warning('请先点击课表选择需要请假的课程！');
    return;
  }
  activeEditLeaveId.value = undefined;
  showLeaveDialog.value = true;
};

const handleLeaveSubmitted = () => {
  selectedCells.value = [];
  activeEditLeaveId.value = undefined;
  fetchTimetable();

  if (resubmitIdFromRoute.value) {
    const q = { ...route.query } as any;
    delete q.resubmitId;
    router.replace({ query: q });
  }
};

watch(showLeaveDialog, (open) => {
  if (!open && resubmitIdFromRoute.value) {
    const q = { ...route.query } as any;
    delete q.resubmitId;
    router.replace({ query: q });
    activeEditLeaveId.value = undefined;
  }
});

const openResubmitIfNeeded = async () => {
  const leaveId = resubmitIdFromRoute.value;
  if (!leaveId) return;

  try {
    const dto: any = await getLeaveDetail(leaveId);
    const impacts = Array.isArray(dto?.impacts) ? dto.impacts : [];
    if (impacts.length === 0) {
      ElMessage.warning('该请假单没有受影响课程，无法按课补充提交');
      return;
    }

    const firstDate = impacts[0].courseDate;
    const termStart = dayjs(termStartDateStr.value).startOf('day');
    const target = dayjs(firstDate).startOf('day');
    const diffDays = target.diff(termStart, 'day');
    const week = Math.floor(diffDays / 7) + 1;
    if (week > 0) currentWeek.value = week;

    await fetchTimetable();

    const ids = new Set(courseList.value.map((c) => c.id));
    const selected = impacts.map((it: any) => String(it.offeringId)).filter((id: string) => ids.has(id));

    if (selected.length === 0) ElMessage.warning('该周课表未找到对应课程，请切换周查看');

    selectedCells.value = Array.from(new Set(selected));
    activeEditLeaveId.value = leaveId;
    showLeaveDialog.value = true;
  } catch (e) {
    console.error('打开退回补充失败:', e);
    ElMessage.error('加载退回请假单失败');
  }
};

const timeSlots = [
  { start: '08:00', end: '08:45' },
  { start: '08:45', end: '09:30' },
  { start: '09:45', end: '10:30' },
  { start: '10:30', end: '11:15' },
  { start: '11:15', end: '12:00' },
  { start: '13:00', end: '13:45' },
  { start: '13:45', end: '14:30' },
  { start: '14:45', end: '15:30' },
  { start: '15:30', end: '16:15' },
  { start: '16:15', end: '17:00' },
  { start: '18:00', end: '18:45' },
  { start: '18:45', end: '19:30' },
  { start: '19:40', end: '20:25' },
  { start: '20:25', end: '21:10' },
];

const checkCellState = (day: number, section: number) => {
  const startCourse = courseList.value.find((c) => c.dayOfWeek === day && c.section === section);
  if (startCourse) return { isStart: true, isOccupied: true, duration: startCourse.duration, course: startCourse };

  const occupiedCourse = courseList.value.find(
      (c) => c.dayOfWeek === day && section > c.section && section < c.section + c.duration
  );
  if (occupiedCourse) return { isStart: false, isOccupied: true, duration: 0 };

  return { isStart: false, isOccupied: false, duration: 0 };
};

const styleConfig: Record<string, { bg: string; border: string; tag: string }> = {
  green: { bg: 'bg-green-50', border: 'border-green-500', tag: 'bg-green-100 text-green-700' },
  yellow: { bg: 'bg-yellow-50', border: 'border-yellow-500', tag: 'bg-yellow-100 text-yellow-700' },
  orange: { bg: 'bg-orange-50', border: 'border-orange-500', tag: 'bg-orange-100 text-orange-700' },
  blue: { bg: 'bg-blue-50', border: 'border-blue-500', tag: 'bg-blue-100 text-blue-700' },
  red: { bg: 'bg-red-50', border: 'border-red-500', tag: 'bg-red-100 text-red-700' },
  gray: { bg: 'bg-gray-50', border: 'border-gray-400', tag: 'bg-gray-200 text-gray-600' },
};

const getStatusStyles = (status: LeaveStatus) => {
  const keyMap: Record<number, string> = {
    [LeaveStatus.PRESENT]: 'green',
    [LeaveStatus.EFFECTIVE]: 'yellow',
    [LeaveStatus.PENDING_TEACHER]: 'orange',
    [LeaveStatus.PUBLIC_LEAVE]: 'blue',
    [LeaveStatus.ABSENCE]: 'red',
    [LeaveStatus.CANCELLED]: 'gray',
    [LeaveStatus.REJECTED]: 'gray',
  };
  return styleConfig[keyMap[status] || 'green'];
};

const getLegendColorClass = (key: string) => {
  const map: Record<string, string> = {
    attended: 'bg-green-500',
    approved: 'bg-yellow-500',
    pending: 'bg-orange-500',
    public: 'bg-blue-500',
    absent: 'bg-red-500',
    cancelled: 'bg-gray-400',
    rejected: 'bg-purple-500',
  };
  return map[key] || 'bg-gray-300';
};

const statusLabels = {
  attended: '到课',
  approved: '已请假',
  pending: '待确认',
  public: '公假',
  absent: '缺勤/待补假',
  cancelled: '已取消',
  rejected: '已驳回',
};

const getStatusLabel = (status: LeaveStatus) => {
  if (status === LeaveStatus.EFFECTIVE) return '已请假';
  if (status === LeaveStatus.PENDING_TEACHER) return '待确认';
  if (status === LeaveStatus.ABSENCE) return '缺勤';
  if (status === LeaveStatus.PUBLIC_LEAVE) return '公假';
  return '';
};

const fetchTimetable = async () => {
  const userId = localStorage.getItem('userId');

  try {
    const userIdParam = userId ? Number(userId) : undefined;
    courseList.value = await getStudentTimetable(currentWeek.value, { studentId: userIdParam });
  } catch (error: any) {
    console.error('获取课表失败:', error);
    if (error?.response?.data?.message?.includes('缺少参数 studentId')) {
      ElMessage.error('获取课表失败：需要指定学生ID，请重新登录或联系管理员');
    } else if (error?.response?.status === 401) {
      ElMessage.error('登录已过期，请重新登录');
    } else {
      ElMessage.error('获取课表失败，请检查网络连接或联系管理员');
    }
  }
};

const changeWeek = (delta: number) => {
  if (currentWeek.value + delta > 0) {
    currentWeek.value += delta;
    fetchTimetable();
  }
};

const isSelected = (course: CourseCell) => selectedCells.value.includes(course.id);

const handleCellClick = (course: CourseCell) => {
  const id = course.id;
  selectedCells.value.includes(id)
      ? (selectedCells.value = selectedCells.value.filter((cid) => cid !== id))
      : selectedCells.value.push(id);
};

onMounted(fetchTimetable);
onMounted(openResubmitIfNeeded);
</script>
