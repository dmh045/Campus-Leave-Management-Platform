<template>
  <div class="space-y-6">
    <div v-if="!selectedCourse" class="space-y-6">
      <div class="bg-gradient-to-r from-blue-50 to-indigo-50 border border-blue-200 rounded-lg p-6 flex items-start gap-4">
        <div class="p-3 bg-blue-100 rounded-lg"><Info class="w-6 h-6 text-blue-600" /></div>
        <div>
          <h3 class="text-blue-900 mb-2 font-bold">考勤登记说明</h3>
          <p class="text-sm text-blue-800 leading-relaxed">
            点击“发起签到”会生成动态 token，并在弹窗中展示二维码（学生扫码后会自动打开签到页面并携带 token）。
            点击课程卡片进入详细点名界面。
          </p>
        </div>
      </div>

      <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <div class="px-6 py-4 border-b border-gray-100 bg-gray-50/50">
          <h2 class="font-bold text-gray-800 text-lg">今日课程 ({{ selectedDate }})</h2>
        </div>
        <div class="divide-y divide-gray-100">
          <div
            v-for="course in todayCourses"
            :key="course.id"
            class="p-6 hover:bg-gray-50 transition-colors group flex items-center justify-between"
          >
            <div @click="handleCourseSelect(course)" class="flex-1 cursor-pointer">
              <div class="flex items-center gap-3 mb-2">
                <h3 class="text-gray-900 font-bold text-lg group-hover:text-blue-600 transition-colors">{{ course.name }}</h3>
                <span class="px-3 py-1 bg-blue-100 text-blue-700 rounded-full text-xs font-medium">{{ course.class }}</span>
                <span
                  :class="[
                    'px-3 py-1 rounded-full text-xs font-medium',
                    course.status === 'pending' ? 'bg-orange-100 text-orange-700' : 'bg-gray-100 text-gray-600'
                  ]"
                >
                  {{ course.status === 'pending' ? '进行中' : '已结束' }}
                </span>
              </div>
              <div class="flex flex-wrap gap-4 text-sm text-gray-600">
                <div class="flex items-center gap-1"><Clock class="w-4 h-4 text-gray-400" /> {{ course.period }}</div>
                <div class="flex items-center gap-1"><MapPin class="w-4 h-4 text-gray-400" /> {{ course.location }}</div>
                <div class="flex items-center gap-1"><Users class="w-4 h-4 text-gray-400" /> 应到 {{ course.shouldAttendCount }} / 已到 {{ course.checkedInCount }} / 请假 {{ course.leaveCount }}</div>
              </div>
            </div>

            <div class="flex items-center gap-2">
              <button
                v-if="course.status === 'pending'"
                @click.stop="openCheckInModal(course)"
                class="flex items-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-sm font-medium shadow-sm transition-all"
              >
                <QrCode class="w-4 h-4" />
                发起签到
              </button>

              <button
                class="px-3 py-2 rounded-lg border border-gray-200 text-gray-600 hover:bg-gray-50 transition-colors"
                @click="handleCourseSelect(course)"
              >
                <ChevronRight class="w-4 h-4" />
              </button>
            </div>
          </div>

          <div v-if="todayCourses.length === 0" class="p-12 text-center text-gray-400">
            今日暂无签到场次（后端未创建场次时这里会为空）。
          </div>
        </div>
      </div>
    </div>

    <!-- 详情页 -->
    <div v-else class="space-y-6">
      <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <div class="px-6 py-4 border-b border-gray-100 bg-gray-50/50 flex items-center justify-between">
          <div>
            <h2 class="font-bold text-gray-800 text-lg flex items-center gap-2">
              {{ selectedCourse.name }}
              <span class="px-3 py-1 bg-blue-100 text-blue-700 rounded-full text-xs font-medium">{{ selectedCourse.class }}</span>
            </h2>
            <div class="mt-2 flex flex-wrap gap-4 text-sm text-gray-600">
              <div class="flex items-center gap-1"><Calendar class="w-4 h-4 text-gray-400" /> {{ selectedCourse.date }}</div>
              <div class="flex items-center gap-1"><Clock class="w-4 h-4 text-gray-400" /> {{ selectedCourse.period }}</div>
              <div class="flex items-center gap-1"><MapPin class="w-4 h-4 text-gray-400" /> {{ selectedCourse.location }}</div>
            </div>
          </div>
          <button
            @click="refreshDetail"
            class="px-4 py-2 text-sm font-medium bg-white border border-gray-200 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors flex items-center gap-2"
          >
            <RefreshCw class="w-4 h-4" /> 刷新
          </button>


          <button
            @click="selectedCourse = null"
            class="px-4 py-2 text-sm font-medium bg-white border border-gray-200 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors flex items-center gap-2"
          >
            <X class="w-4 h-4" /> 返回
          </button>
        
</div>

        <div class="p-6 space-y-4">
          <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div class="bg-gray-50 rounded-lg p-4">
              <div class="text-xs text-gray-500 mb-1">应到人数</div>
              <div class="text-2xl font-bold text-gray-900">{{ students.length }}</div>
            </div>
            <div class="bg-green-50 rounded-lg p-4">
              <div class="text-xs text-green-700 mb-1">已签到</div>
              <div class="text-2xl font-bold text-green-700">{{ presentCount }}</div>
            </div>
            <div class="bg-orange-50 rounded-lg p-4">
              <div class="text-xs text-orange-700 mb-1">请假</div>
              <div class="text-2xl font-bold text-orange-700">{{ leaveCount }}</div>
            </div>
            <div class="bg-red-50 rounded-lg p-4">
              <div class="text-xs text-red-700 mb-1">缺勤</div>
              <div class="text-2xl font-bold text-red-700">{{ absentCount }}</div>
            </div>
          </div>

          <div class="bg-white rounded-lg border border-gray-100 overflow-hidden">
            <div class="px-6 py-4 border-b border-gray-100 flex items-center justify-between">
              <h3 class="font-bold text-gray-800 flex items-center gap-2">
                <Users class="w-5 h-5 text-blue-500" />
                学生名单
              </h3>
              <span class="text-xs text-gray-500">未签到/未请假的将由后端统计为缺勤</span>
            </div>

            <div class="divide-y divide-gray-100">
              <div v-for="stu in students" :key="stu.id" class="px-6 py-4 flex items-center justify-between">
                <div>
                  <div class="font-medium text-gray-900">{{ stu.name }}</div>
                  <div class="text-xs text-gray-500">{{ stu.studentId }}</div>
                </div>

                <div class="flex items-center gap-2">
                  <span
                    v-if="stu.status === 'present'"
                    class="px-3 py-1 bg-green-50 text-green-700 border border-green-100 rounded-full text-xs font-medium flex items-center gap-1"
                  >
                    <Check class="w-3.5 h-3.5" /> 已签到
                  </span>

                  <span
                    v-else-if="stu.status === 'leave'"
                    class="px-3 py-1 bg-orange-50 text-orange-700 border border-orange-100 rounded-full text-xs font-medium"
                  >
                    请假
                  </span>

                  <span
                    v-else
                    class="px-3 py-1 bg-red-50 text-red-700 border border-red-100 rounded-full text-xs font-medium"
                  >
                    缺勤
                  </span>
                </div>
              </div>

              <div v-if="students.length === 0" class="p-12 text-center text-gray-400">
                暂无学生名单（请确认后端 session detail 是否返回 students）。
              </div>
            </div>
          </div>
        </div>

        <div class="px-6 py-4 border-t border-gray-100 bg-gray-50/50 flex justify-end gap-3">
          <button
            type="button"
            @click="selectedCourse = null"
            class="px-6 py-2.5 text-sm font-medium text-gray-600 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
          >
            取消
          </button>
          <button
            type="button"
            @click="submit"
            class="px-6 py-2.5 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-colors flex items-center gap-2"
          >
            <Send class="w-4 h-4" />
            关闭签到
          </button>
        </div>
      </div>
    </div>

    <!-- 二维码/Token 弹窗 -->
    <Transition name="fade">
      <div v-if="showQrModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-black/30 backdrop-blur-sm" @click="showQrModal = false"></div>
        <div class="bg-white rounded-xl shadow-2xl w-full max-w-md relative z-10 overflow-hidden animate-scale-in">
          <div class="px-6 py-4 border-b border-gray-100 flex items-center justify-between">
            <div class="flex items-center gap-2">
              <QrCode class="w-5 h-5 text-blue-600" />
              <h3 class="font-bold text-gray-900">发起签到 - {{ qrCourseName }}</h3>
            </div>
            <button class="p-2 rounded-lg hover:bg-gray-100 text-gray-500" @click="showQrModal = false">
              <X class="w-5 h-5" />
            </button>
          </div>

          <div class="p-6 space-y-4">
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 items-center">
              <div class="bg-gray-50 border border-gray-100 rounded-lg p-4">
                <div class="text-xs text-gray-500 mb-2">扫码签到</div>
                <div class="bg-white border border-gray-100 rounded-lg p-3 flex items-center justify-center">
                  <canvas ref="qrCanvasRef" width="220" height="220" />
                </div>
              </div>

              <div class="space-y-3">
                <div class="bg-gray-50 border border-gray-100 rounded-lg p-4 text-sm text-gray-700">
                  本次签到 token：<br />
                  <div class="mt-2 font-mono break-all text-gray-900">{{ qrToken || '（暂无 token）' }}</div>
                </div>

                <div class="bg-blue-50 border border-blue-100 rounded-lg p-4 text-sm text-blue-900">
                  签到链接：<br />
                  <div class="mt-2 font-mono break-all text-blue-900">{{ checkinUrl || '（暂无）' }}</div>
                </div>

                <button
                  v-if="checkinUrl"
                  @click="copyLink"
                  class="w-full px-4 py-2 bg-white border border-gray-200 text-gray-700 rounded-lg text-sm font-medium hover:bg-gray-50 transition-colors flex items-center justify-center gap-2"
                >
                  <Copy class="w-4 h-4" /> 复制链接
                </button>
              </div>
            </div>

            <div class="text-xs text-gray-500">
              学生端会使用：`studentId + token` 调用签到接口。token 有有效期，过期需重新发起签到。
            </div>
          </div>

          <div class="bg-gray-50 px-6 py-4 flex gap-3">
            <button
              @click="showQrModal = false"
              class="flex-1 px-4 py-2.5 bg-white border border-gray-200 text-gray-700 rounded-lg text-sm font-medium hover:bg-gray-50 transition-colors"
            >
              关闭
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick, onUnmounted, watch } from 'vue';
import { Info, Calendar, Clock, MapPin, ChevronRight, Users, Check, X, Send, QrCode, Copy, RefreshCw } from 'lucide-vue-next';
import { ElMessage } from 'element-plus';
import dayjs from 'dayjs';
import QRCode from 'qrcode';
import { getTeacherSessions, startCheckInSession, getSessionDetail, closeSession, type AttendanceSessionSummary } from '@/api/teacher';

const selectedDate = ref(dayjs().format('YYYY-MM-DD'));

const selectedCourse = ref<any>(null);
const showQrModal = ref(false);
const qrCourseName = ref('');
const qrToken = ref('');

const qrCanvasRef = ref<HTMLCanvasElement | null>(null);

const sessions = ref<AttendanceSessionSummary[]>([]);
const students = ref<Array<{ id: string; name: string; studentId: string; status: 'present' | 'leave' | 'absent' }>>([]);

const teacherId = computed(() => {
  const s = localStorage.getItem('userId') || localStorage.getItem('teacherId');
  const v = s ? Number(s) : 0;
  return Number.isFinite(v) && v > 0 ? v : 0;
});

const checkinUrl = computed(() => {
  if (!qrToken.value) return '';
  const base = window.location.origin;
  return `${base}/checkin?token=${encodeURIComponent(qrToken.value)}`;
});

const toCourseCard = (s: AttendanceSessionSummary) => {
  return {
    id: String(s.sessionId),
    sessionId: s.sessionId,
    offeringId: s.offeringId,

    name: s.courseName,
    class: s.className,
    date: s.courseDate,

    period: `第${s.sectionStart}-${s.sectionEnd}节`,
    location: '—',

    status: String(s.status || '').toUpperCase() === 'OPEN' ? 'pending' : 'completed',

    sectionStart: s.sectionStart,
    sectionEnd: s.sectionEnd,

    shouldAttendCount: s.shouldAttendCount ?? 0,
    checkedInCount: s.checkedInCount ?? 0,
    leaveCount: s.leaveCount ?? 0,
  };
};

const todayCourses = computed(() => sessions.value.map(toCourseCard));

const presentCount = computed(() => students.value.filter(s => s.status === 'present').length);
const leaveCount = computed(() => students.value.filter(s => s.status === 'leave').length);
const absentCount = computed(() => students.value.filter(s => s.status === 'absent').length);

const fetchTodaySessions = async () => {
  if (!teacherId.value) {
    sessions.value = [];
    return;
  }
  const list = await getTeacherSessions(teacherId.value, selectedDate.value, selectedDate.value);
  sessions.value = Array.isArray(list) ? list : [];
};

const handleCourseSelect = async (course: any) => {
  selectedCourse.value = course;
  students.value = [];

  if (!teacherId.value) return;
  try {
    const detail = await getSessionDetail(course.sessionId, teacherId.value);
    const list = Array.isArray(detail?.students) ? detail.students : [];

    students.value = list.map((x: any) => {
      const raw = String(x?.status || '').toUpperCase();
      const status = raw === 'PRESENT' ? 'present' : raw === 'LEAVE' ? 'leave' : 'absent';
      return {
        id: String(x?.studentId ?? x?.studentNo ?? ''),
        name: String(x?.studentName ?? ''),
        studentId: String(x?.studentNo ?? x?.studentId ?? ''),
        status,
      };
    });
  } catch (e: any) {
    ElMessage.error(e?.message || '获取签到详情失败');
  }
};


const refreshDetail = async () => {
  if (!selectedCourse.value) return;
  await handleCourseSelect(selectedCourse.value);
};

let pollTimer: number | null = null;

const startPollingIfNeeded = () => {
  stopPolling();
  // 仅在详情页打开且场次仍为进行中时轮询
  if (!selectedCourse.value || selectedCourse.value.status !== 'pending') return;

  pollTimer = window.setInterval(() => {
    // 避免并发：用当前选中的 course 刷新
    refreshDetail();
  }, 5000);
};

const stopPolling = () => {
  if (pollTimer) {
    window.clearInterval(pollTimer);
    pollTimer = null;
  }
};


const renderQr = async () => {
  await nextTick();
  if (!qrCanvasRef.value || !checkinUrl.value) return;

  try {
    await QRCode.toCanvas(qrCanvasRef.value, checkinUrl.value, {
      width: 220,
      margin: 1,
      errorCorrectionLevel: 'M',
    });
  } catch (e: any) {
    ElMessage.error(e?.message || '二维码生成失败');
  }
};

const openCheckInModal = async (course: any) => {
  if (!teacherId.value) {
    ElMessage.error('未获取到教师信息，请重新登录');
    return;
  }

  try {
    const resp = await startCheckInSession({
      teacherId: teacherId.value,
      offeringId: course.offeringId,
      courseDate: course.date,
      sectionStart: course.sectionStart,
      sectionEnd: course.sectionEnd,
      durationMinutes: 10,
    });

    qrCourseName.value = course.name;
    qrToken.value = resp?.token || '';
    showQrModal.value = true;

    await renderQr();
    await fetchTodaySessions();
  } catch (e: any) {
    ElMessage.error(e?.message || '发起签到失败');
  }
};

const copyLink = async () => {
  if (!checkinUrl.value) return;
  try {
    await navigator.clipboard.writeText(checkinUrl.value);
    ElMessage.success('已复制');
  } catch {
    ElMessage.warning('复制失败，请手动复制');
  }
};

const submit = async () => {
  if (!selectedCourse.value) return;
  if (!teacherId.value) {
    ElMessage.error('未获取到教师信息，请重新登录');
    return;
  }

  try {
    await closeSession(selectedCourse.value.sessionId, teacherId.value);
    ElMessage.success('签到已关闭');
    selectedCourse.value = null;
    await fetchTodaySessions();
  } catch (e: any) {
    ElMessage.error(e?.message || '关闭签到失败');
  }
};

onMounted(() => {
  fetchTodaySessions();
});

watch(selectedCourse, () => {
  startPollingIfNeeded();
});

onUnmounted(() => {
  stopPolling();
});

</script>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
.animate-scale-in { animation: scale-in 0.2s cubic-bezier(0.16, 1, 0.3, 1); }
@keyframes scale-in { from { opacity: 0; transform: scale(0.95); } to { opacity: 1; transform: scale(1); } }
</style>
