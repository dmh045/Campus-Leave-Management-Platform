<template>
  <div class="space-y-4">
    <div class="bg-white rounded-lg shadow-sm p-4">
      <div class="flex items-center gap-4">
        <div class="flex-1 relative">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
              type="text"
              placeholder="搜索请假单号或理由..."
              v-model="searchText"
              class="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all text-sm"
          />
        </div>

        <div class="flex items-center gap-2">
          <span class="text-sm text-gray-600 font-medium">状态筛选：</span>
          <div class="flex items-center bg-gray-50 rounded-lg p-1">
            <button
                v-for="option in filterOptions"
                :key="option.value"
                @click="currentFilter = option.value"
                class="px-3 py-1.5 text-sm font-medium rounded-md transition-all"
                :class="currentFilter === option.value ? 'bg-white text-blue-600 shadow-sm' : 'text-gray-600 hover:text-gray-800'"
            >
              {{ option.label }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="bg-white rounded-lg shadow-sm overflow-hidden">
      <div class="p-4 border-b border-gray-100 flex items-center justify-between">
        <div class="flex items-center gap-2">
          <FileText class="w-5 h-5 text-blue-500" />
          <h2 class="text-base font-bold text-gray-800">请假记录</h2>
          <span class="text-xs bg-blue-50 text-blue-600 px-2 py-0.5 rounded-full font-medium">
            {{ filteredRecords.length }} 条
          </span>
        </div>
      </div>

      <div v-if="filteredRecords.length" class="divide-y divide-gray-100">
        <div
            v-for="record in filteredRecords"
            :key="record.id"
            class="p-4 hover:bg-gray-50 transition-colors cursor-pointer"
            @click="selectedRecord = record"
        >
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-3">
              <span class="text-sm font-bold text-gray-800">{{ record.id }}</span>
              <span
                  class="text-xs px-2 py-0.5 rounded-full font-medium"
                  :class="getTypeColor(record.type)"
              >
                {{ record.type }}
              </span>
              <span
                  class="text-xs px-2 py-0.5 rounded-full font-medium flex items-center gap-1"
                  :class="getStatusConfig(record.status).color"
              >
                <component :is="getStatusConfig(record.status).icon" class="w-3 h-3" />
                {{ getStatusConfig(record.status).label }}
              </span>
            </div>

            <div class="flex items-center gap-2">
              <button class="text-blue-600 hover:text-blue-700 transition-colors">
                <Eye class="w-4 h-4" />
              </button>
            </div>
          </div>

          <div class="mt-3 flex items-center justify-between">
            <div class="text-xs text-gray-500">
              <span class="font-medium text-gray-700">请假时间：</span>
              {{ record.startTime }} - {{ record.endTime }}
            </div>
            <div class="text-xs text-gray-400">
              提交于 {{ record.submitTime }}
            </div>
          </div>
        </div>
      </div>

      <div v-else class="text-center py-16 text-gray-400 flex flex-col items-center">
        <div class="bg-gray-50 p-4 rounded-full mb-3">
          <Search class="w-8 h-8 text-gray-300" />
        </div>
        <p>暂无符合条件的请假记录</p>
      </div>
    </div>

    <Transition
        enter-active-class="transition ease-out duration-300"
        enter-from-class="opacity-0 translate-x-full"
        enter-to-class="opacity-100 translate-x-0"
        leave-active-class="transition ease-in duration-200"
        leave-from-class="opacity-100 translate-x-0"
        leave-to-class="opacity-0 translate-x-full"
    >
      <div
          v-if="selectedRecord"
          class="fixed top-0 right-0 h-full w-full sm:w-[520px] bg-white shadow-2xl z-50 overflow-auto animate-slide-in"
      >
        <div class="p-4 border-b border-gray-100 flex items-center justify-between sticky top-0 bg-white z-10">
          <div class="flex items-center gap-2">
            <FileText class="w-5 h-5 text-blue-500" />
            <h3 class="text-base font-bold text-gray-800">请假详情</h3>
          </div>
          <button
              @click="selectedRecord = null"
              class="p-2 rounded-lg hover:bg-gray-100 text-gray-500 hover:text-gray-700 transition-colors"
          >
            <X class="w-5 h-5" />
          </button>
        </div>

        <div class="p-4 space-y-4">
          <div class="bg-gray-50 rounded-lg p-4 space-y-3">
            <div class="flex items-center justify-between">
              <span class="text-sm font-bold text-gray-800">{{ selectedRecord.id }}</span>
              <span
                  class="text-xs px-2 py-0.5 rounded-full font-medium flex items-center gap-1"
                  :class="getStatusConfig(selectedRecord.status).color"
              >
                <component :is="getStatusConfig(selectedRecord.status).icon" class="w-3 h-3" />
                {{ getStatusConfig(selectedRecord.status).label }}
              </span>
            </div>

            <div class="space-y-2 text-sm">
              <div class="flex items-center gap-2">
                <span class="text-gray-500 w-20">请假类型：</span>
                <span class="font-medium">{{ selectedRecord.type }}</span>
              </div>
              <div class="flex items-center gap-2">
                <span class="text-gray-500 w-20">时间范围：</span>
                <span class="font-medium">{{ selectedRecord.startTime }} - {{ selectedRecord.endTime }}</span>
              </div>
              <div class="flex items-start gap-2">
                <span class="text-gray-500 w-20">请假原因：</span>
                <span class="font-medium leading-relaxed">{{ selectedRecord.reason }}</span>
              </div>
            </div>
          </div>

          <div class="bg-white rounded-lg border border-gray-100 overflow-hidden">
            <div class="p-4 border-b border-gray-100">
              <h3 class="text-sm font-bold text-gray-800 flex items-center gap-2">
                <BookOpen class="w-4 h-4 text-blue-500" /> 受影响课程
              </h3>
            </div>
            <div class="p-4">
              <div class="flex flex-wrap gap-2">
                <span
                    v-for="course in selectedRecord.courses"
                    :key="course"
                    class="text-xs bg-blue-50 text-blue-700 px-3 py-1 rounded-full font-medium border border-blue-100"
                >
                  {{ course }}
                </span>
              </div>
            </div>
          </div>

          <div class="bg-white rounded-lg border border-gray-100 overflow-hidden">
            <div class="p-4 border-b border-gray-100">
              <h3 class="text-sm font-bold text-gray-800 flex items-center gap-2">
                <GitCommit class="w-4 h-4 text-purple-500" /> 审批进度
              </h3>
            </div>

            <div class="p-4">
              <div class="relative pl-4 space-y-8 before:absolute before:left-4 before:top-2 before:bottom-2 before:w-0.5 before:bg-gray-100">
                <div v-for="(step, idx) in selectedRecord.timeline" :key="idx" class="relative pl-8">
                  <div
                      :class="[
                      'absolute left-0 top-1 w-2.5 h-2.5 rounded-full border-2 bg-white z-10',
                      step.status === 'completed' ? 'border-green-500 bg-green-500' :
                      step.status === 'current' ? 'border-blue-500 animate-pulse' : 'border-gray-300'
                    ]"
                  ></div>

                  <div :class="step.status === 'pending' ? 'opacity-50' : ''">
                    <div class="flex items-center gap-2">
                      <span class="text-sm font-medium text-gray-900">{{ step.step }}</span>
                      <CheckCircle2 v-if="step.status === 'completed'" class="w-3.5 h-3.5 text-green-500" />
                    </div>
                    <div v-if="step.time" class="text-xs text-gray-400 mt-0.5">{{ step.time }}</div>
                    <div
                        v-if="step.operator"
                        class="text-xs text-gray-500 mt-1 bg-gray-50 inline-block px-2 py-0.5 rounded"
                    >
                      处理人：{{ step.operator }}
                    </div>
                    <div
                        v-if="step.remark"
                        class="mt-2 text-xs bg-red-50 text-red-600 px-3 py-2 rounded border border-red-100"
                    >
                      批注：{{ step.remark }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

        </div>

        <div class="p-4 border-t border-gray-100 bg-gray-50 flex justify-end gap-3">
          <!-- ✅ 退回补充：继续编辑 -->
          <button
              v-if="selectedRecord.status === 'returned'"
              @click="handleEditReturned"
              class="px-4 py-2 text-sm font-medium text-white bg-purple-600 rounded-lg hover:bg-purple-700 transition-colors"
          >
            继续编辑
          </button>

          <button
              @click="selectedRecord = null"
              class="px-4 py-2 text-sm font-medium text-gray-600 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
          >
            关闭
          </button>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import dayjs from 'dayjs';
import {
  Search, Eye, XCircle, Clock, CheckCircle2, AlertCircle, Ban, X,
  FileText, BookOpen, GitCommit
} from 'lucide-vue-next';
import { getMyLeaveList, getLeaveDetail } from '@/api/leave';

const router = useRouter();

// 1. 状态配置 (Figma 风格)
const statusConfig: Record<string, any> = {
  pending: { label: '待审批', color: 'bg-orange-50 text-orange-700 border border-orange-100', icon: Clock },
  approved: { label: '已通过', color: 'bg-green-50 text-green-700 border border-green-100', icon: CheckCircle2 },
  rejected: { label: '已驳回', color: 'bg-red-50 text-red-700 border border-red-100', icon: XCircle },
  returned: { label: '退回补充', color: 'bg-purple-50 text-purple-700 border border-purple-100', icon: AlertCircle },
  cancelled: { label: '已取消', color: 'bg-gray-50 text-gray-600 border border-gray-100', icon: Ban },
};

// 2. 筛选选项
const filterOptions = [
  { label: '全部', value: 'all' },
  { label: '待审批', value: 'pending' },
  { label: '已通过', value: 'approved' },
  { label: '退回', value: 'returned' },
  { label: '已驳回', value: 'rejected' },
];

type TimelineStep = {
  step: string;
  status: 'completed' | 'current' | 'pending';
  time?: string;
  operator?: string;
  remark?: string;
};

type LeaveRecord = {
  leaveId: number;

  id: string;          // 展示用单号
  type: string;        // 病假/事假/公假
  startTime: string;
  endTime: string;
  status: keyof typeof statusConfig; // pending/approved/rejected/returned/cancelled
  submitTime: string;
  reason: string;

  courses: string[];
  timeline: TimelineStep[];

  // ✅ 新增：用于决定“继续编辑”跳转逻辑
  applyChannel?: string;

  _rawStatus?: string;
  _detailLoaded?: boolean;
};

const searchText = ref('');
const currentFilter = ref('all');
const selectedRecord = ref<LeaveRecord | null>(null);

// ✅ 后端真实数据
const records = ref<LeaveRecord[]>([]);

const formatDT = (v: any) => {
  if (!v) return '';
  const d = dayjs(v);
  if (!d.isValid()) return String(v);
  return d.format('YYYY-MM-DD HH:mm');
};

const mapLeaveType = (leaveType: string) => {
  if (leaveType === 'SICK') return '病假';
  if (leaveType === 'PERSONAL') return '事假';
  if (leaveType === 'PUBLIC') return '公假';
  return leaveType || '请假';
};

const mapLeaveStatus = (raw: string): LeaveRecord['status'] => {
  if (!raw) return 'pending';
  const s = String(raw).toUpperCase();

  if (s === 'APPROVED') return 'approved';
  if (s === 'REJECTED') return 'rejected';
  if (s === 'RETURNED') return 'returned';
  if (s === 'CANCELLED' || s === 'CANCELED') return 'cancelled';

  return 'pending';
};

const buildPlaceholderTimeline = (rawStatus?: string, submitTime?: string): TimelineStep[] => {
  const s = (rawStatus || '').toUpperCase();

  const step1: TimelineStep = { step: '提交申请', status: 'completed', time: submitTime || '' };

  const counselorStatus: TimelineStep['status'] =
      (s === 'DRAFT' || s === 'PENDING' || s === 'PENDING_COUNSELOR') ? 'current' : 'completed';

  const teacherStatus: TimelineStep['status'] =
      (s === 'PENDING_TEACHER') ? 'current' :
          (s === 'APPROVED') ? 'completed' : 'pending';

  const effectStatus: TimelineStep['status'] =
      (s === 'APPROVED') ? 'completed' : 'pending';

  return [
    step1,
    { step: '辅导员审批', status: counselorStatus },
    { step: '任课教师确认', status: teacherStatus },
    { step: '请假生效', status: effectStatus },
  ];
};

const mapLeaveRequestToRecord = (item: any): LeaveRecord => {
  const leaveId = Number(item.leaveId);
  const rawStatus = item.status;

  const submitTime = formatDT(item.createdAt);

  return {
    leaveId,
    id: `L${leaveId}`,
    type: mapLeaveType(item.leaveType),
    startTime: formatDT(item.startTime),
    endTime: formatDT(item.endTime),
    status: mapLeaveStatus(rawStatus),
    submitTime,
    reason: item.reason || '',
    courses: [],
    timeline: buildPlaceholderTimeline(rawStatus, submitTime),
    applyChannel: item.applyChannel, // 可能列表不带，后面详情会补
    _rawStatus: rawStatus,
    _detailLoaded: false,
  };
};

const loadMyLeaves = async () => {
  const sidStr = localStorage.getItem('userId') || localStorage.getItem('studentId');
  if (!sidStr) {
    records.value = [];
    return;
  }

  const studentId = Number(sidStr);
  const list = await getMyLeaveList(studentId);

  records.value = Array.isArray(list) ? list.map(mapLeaveRequestToRecord) : [];
};

onMounted(() => {
  loadMyLeaves();
});

// ✅ 退回补充：继续编辑（根据 applyChannel 决定跳转）
const handleEditReturned = async () => {
  if (!selectedRecord.value) return;

  const leaveId = selectedRecord.value.leaveId;

  try {
    let channel = selectedRecord.value.applyChannel;
    if (!channel) {
      const dto: any = await getLeaveDetail(leaveId);
      channel = dto?.applyChannel;
    }

    const ch = String(channel || '').toUpperCase();
    if (ch === 'BY_COURSE') {
      router.push({ path: '/student/timetable', query: { resubmitId: String(leaveId) } });
    } else {
      router.push({ path: '/leave/apply', query: { editId: String(leaveId) } });
    }

    selectedRecord.value = null;
  } catch (e) {
    console.error('继续编辑跳转失败:', e);
    router.push({ path: '/leave/apply', query: { editId: String(leaveId) } });
    selectedRecord.value = null;
  }
};

// 选择记录后自动拉取详情填充 courses/timeline（不改模板点击逻辑）
watch(
    selectedRecord,
    async (val) => {
      if (!val) return;
      if (val._detailLoaded) return;

      try {
        const dto = await getLeaveDetail(val.leaveId);

        const impacts = Array.isArray(dto?.impacts) ? dto.impacts : [];
        const courses: string[] = Array.from(
            new Set(
                impacts
                    .map((x: any) => x?.courseName)
                    .filter((x: any): x is string => typeof x === 'string' && x.trim().length > 0)
            )
        );

        const approvals = Array.isArray(dto?.approvals) ? dto.approvals : [];
        const counselorApproval = approvals.find((a: any) => String(a?.approverRole || '').toUpperCase() === 'COUNSELOR');

        const rawStatus = String(dto?.status || val._rawStatus || '').toUpperCase();

        const step2: TimelineStep = { step: '辅导员审批', status: 'pending' };
        if (counselorApproval) {
          step2.status = 'completed';
          step2.time = formatDT(counselorApproval.createdAt);
          step2.operator = counselorApproval.approverName || '';
          const action = String(counselorApproval.action || '').toUpperCase();
          const actionText = action === 'AGREE' ? '同意' : action === 'REJECT' ? '驳回' : action === 'RETURN' ? '退回补充' : '';
          step2.remark = counselorApproval.comment || actionText || '';
        } else {
          if (rawStatus === 'DRAFT' || rawStatus === 'PENDING' || rawStatus === 'PENDING_COUNSELOR') step2.status = 'current';
          else step2.status = 'completed';
        }

        const step3: TimelineStep = { step: '任课教师确认', status: 'pending' };
        if (impacts.length === 0) {
          step3.status = 'completed';
          step3.remark = '无受影响课程';
        } else {
          const confirmed = impacts.filter((x: any) => String(x?.confirmStatus || '').toUpperCase() === 'CONFIRMED');
          const pending = impacts.filter((x: any) => String(x?.confirmStatus || '').toUpperCase() === 'PENDING');

          if (confirmed.length === impacts.length) {
            step3.status = 'completed';
          } else if (rawStatus === 'PENDING_TEACHER') {
            step3.status = 'current';
          } else if (confirmed.length > 0 && pending.length > 0) {
            step3.status = 'current';
          } else {
            step3.status = 'pending';
          }

          const confirmedTimes = confirmed.map((x: any) => x?.confirmTime).filter(Boolean);
          if (confirmedTimes.length) {
            step3.time = formatDT(confirmedTimes.sort().slice(-1)[0]);
          }

          const teacherNames = Array.from(
              new Set(
                  impacts
                      .map((x: any) => x?.teacherName)
                      .filter((x: any) => typeof x === 'string' && x.trim().length > 0)
              )
          );
          if (teacherNames.length) step3.operator = teacherNames.join('、');
        }

        const step4: TimelineStep = { step: '请假生效', status: 'pending' };
        if (rawStatus === 'APPROVED') {
          step4.status = 'completed';
          step4.time = step3.time || step2.time || '';
        }

        selectedRecord.value = {
          ...val,
          type: mapLeaveType(dto?.leaveType || val.type),
          startTime: formatDT(dto?.startTime || val.startTime),
          endTime: formatDT(dto?.endTime || val.endTime),
          status: mapLeaveStatus(dto?.status || val._rawStatus || val.status),
          reason: dto?.reason ?? val.reason,
          courses,
          timeline: [
            { step: '提交申请', status: 'completed', time: val.submitTime || '' },
            step2,
            step3,
            step4,
          ],
          applyChannel: dto?.applyChannel || val.applyChannel,
          _rawStatus: dto?.status || val._rawStatus,
          _detailLoaded: true,
        };
      } catch (e) {
        selectedRecord.value = { ...val, _detailLoaded: true };
      }
    },
    { deep: false }
);

// 逻辑：筛选 + 搜索
const filteredRecords = computed(() => {
  return records.value.filter(record => {
    if (currentFilter.value !== 'all' && record.status !== currentFilter.value) return false;

    if (searchText.value) {
      const text = searchText.value.toLowerCase();
      return record.id.toLowerCase().includes(text) || record.reason.includes(text);
    }
    return true;
  });
});

const getStatusConfig = (status: string) => {
  return statusConfig[status] || statusConfig['pending'];
};

const getTypeColor = (type: string) => {
  if (type === '病假') return 'bg-red-50 text-red-700 border border-red-100';
  if (type === '事假') return 'bg-yellow-50 text-yellow-700 border border-yellow-100';
  return 'bg-blue-50 text-blue-700 border border-blue-100';
};
</script>

<style scoped>
/* 侧边栏滑入动画 */
@keyframes slide-in {
  from { transform: translateX(100%); }
  to { transform: translateX(0); }
}
.animate-slide-in {
  animation: slide-in 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}
</style>
