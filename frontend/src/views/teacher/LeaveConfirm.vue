<template>
  <div class="space-y-6">
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
      <div class="bg-white rounded-xl shadow-sm p-5 border border-gray-100 flex flex-col justify-between h-28 relative overflow-hidden group">
        <div class="absolute right-0 top-0 p-4 opacity-10 group-hover:opacity-20 transition-opacity">
          <AlertCircle class="w-16 h-16 text-orange-500" />
        </div>
        <div class="text-sm font-medium text-gray-500">待确认事项</div>
        <div class="flex items-end gap-2">
          <span class="text-3xl font-bold text-gray-900">{{ pendingCount }}</span>
          <span class="text-xs font-medium text-orange-600 bg-orange-50 px-1.5 py-0.5 rounded mb-1">需紧急处理</span>
        </div>
      </div>

      <div class="bg-white rounded-xl shadow-sm p-5 border border-gray-100 flex flex-col justify-between h-28">
        <div class="text-sm font-medium text-gray-500">本周已确认</div>
        <div class="flex items-end gap-2">
          <span class="text-3xl font-bold text-gray-900">{{ confirmedThisWeek }}</span>
          <span class="text-xs text-green-600 flex items-center mb-1">
            <TrendingUp class="w-3 h-3 mr-0.5" /> {{ confirmedWeekDeltaText }}
          </span>
        </div>
      </div>

      <div class="bg-white rounded-xl shadow-sm p-5 border border-gray-100 flex flex-col justify-between h-28">
        <div class="text-sm font-medium text-gray-500">涉及课程数</div>
        <div class="text-3xl font-bold text-blue-600">{{ courseCount }}</div>
      </div>
    </div>

    <div class="bg-white rounded-lg shadow-sm p-4 flex flex-col sm:flex-row gap-4 justify-between items-center">
      <div class="flex items-center gap-4 w-full sm:w-auto">
        <div class="relative flex-1 sm:w-64">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            type="text"
            v-model="searchText"
            placeholder="搜索学生姓名、学号..."
            class="w-full pl-10 pr-4 py-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm"
          />
        </div>

        <div class="flex items-center gap-2">
          <Filter class="w-4 h-4 text-gray-400" />
          <div class="flex bg-gray-50 rounded-lg p-1">
            <button
              v-for="filter in filters"
              :key="filter.value"
              @click="currentFilter = filter.value"
              class="px-3 py-1.5 text-sm font-medium rounded-md transition-all"
              :class="currentFilter === filter.value ? 'bg-white text-blue-600 shadow-sm' : 'text-gray-600 hover:text-gray-800'"
            >
              {{ filter.label }}
            </button>
          </div>
        </div>
      </div>

      <button
        v-if="selectedIds.length > 0"
        @click="handleBatchConfirm"
        class="flex items-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-sm font-medium shadow-sm transition-all"
      >
        <CheckCircle2 class="w-4 h-4" />
        批量确认 ({{ selectedIds.length }})
      </button>
    </div>

    <div class="bg-white rounded-lg shadow-sm overflow-hidden border border-gray-100">
      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50 border-b border-gray-200">
            <tr>
              <th class="px-4 py-3 text-center w-12">
                <input
                  type="checkbox"
                  :checked="isAllSelected"
                  @change="toggleSelectAll"
                  class="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                />
              </th>
              <th class="px-6 py-3 text-left text-sm font-medium text-gray-500">学生信息</th>
              <th class="px-6 py-3 text-left text-sm font-medium text-gray-500">受影响课程</th>
              <th class="px-6 py-3 text-left text-sm font-medium text-gray-500">请假类型</th>
              <th class="px-6 py-3 text-left text-sm font-medium text-gray-500">状态</th>
              <th class="px-6 py-3 text-center text-sm font-medium text-gray-500">操作</th>
            </tr>
          </thead>

          <tbody class="divide-y divide-gray-100">
            <tr v-for="item in filteredList" :key="item.id" class="hover:bg-gray-50 transition-colors">
              <td class="px-4 py-4 text-center">
                <input
                  type="checkbox"
                  :value="item.id"
                  v-model="selectedIds"
                  :disabled="item.status !== 4"
                  class="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                />
              </td>

              <td class="px-6 py-4">
                <div class="font-medium text-gray-900">{{ item.studentName }}</div>
                <div class="text-xs text-gray-500">{{ item.studentId }}</div>
              </td>

              <td class="px-6 py-4">
                <div class="font-medium text-gray-900">{{ item.className }}</div>
                <div class="text-xs text-gray-500 flex items-center gap-1 mt-1">
                  <span class="inline-flex items-center gap-1 bg-gray-50 px-2 py-0.5 rounded border border-gray-100">
                    <Calendar class="w-3 h-3" /> {{ item.date }} ({{ item.classTime }})
                  </span>
                </div>
              </td>

              <td class="px-6 py-4">
                <span :class="['px-2.5 py-1 rounded text-xs font-medium border', getTypeStyles(item.leaveType)]">
                  {{ formatType(item.leaveType) }}
                </span>
              </td>

              <td class="px-6 py-4">
                <span v-if="item.status === 4" class="inline-flex items-center gap-2 px-2.5 py-1 rounded text-xs font-medium bg-orange-50 text-orange-700 border border-orange-100">
                  <span class="w-1.5 h-1.5 rounded-full bg-orange-500 animate-pulse"></span>
                  待确认
                </span>
                <span v-else class="inline-flex items-center gap-2 px-2.5 py-1 rounded text-xs font-medium bg-green-50 text-green-700 border border-green-100">
                  <Check class="w-3.5 h-3.5" />
                  已确认
                </span>
              </td>

              <td class="px-6 py-4 text-center">
                <button
                    @click="openDetailModal(item)"
                    class="text-blue-600 hover:text-blue-700 hover:bg-blue-50 px-3 py-1.5 rounded text-sm font-medium transition-colors"
                >
                  查看详情
                </button>
                <button
                    v-if="item.status === 4"
                    @click="handleSingleConfirm(item)"
                    class="text-blue-600 hover:text-blue-700 hover:bg-blue-50 px-3 py-1.5 rounded text-sm font-medium transition-colors"
                >
                  确认收到
                </button>
                <span v-else class="text-gray-400 text-sm">--</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="filteredList.length === 0" class="p-12 text-center text-gray-400">
        暂无相关记录
      </div>
    </div>

    <!-- 新增：请假详情弹窗 -->
    <Transition name="fade">
      <div v-if="detailModal.visible" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-black/30 backdrop-blur-sm" @click="closeDetailModal"></div>
        <div class="bg-white rounded-xl shadow-2xl w-full max-w-2xl relative z-10 overflow-hidden transform transition-all scale-100">
          <div class="p-6 border-b border-gray-100 flex justify-between items-center">
            <h3 class="text-lg font-bold text-gray-900">请假详情</h3>
            <button @click="closeDetailModal" class="text-gray-400 hover:text-gray-600"><X class="w-5 h-5"/></button>
          </div>

          <div class="p-6 space-y-6">
            <!-- 学生信息 -->
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">学生姓名</label>
                <div class="text-sm text-gray-900">{{ detailModal.data?.studentName }}</div>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">学号</label>
                <div class="text-sm text-gray-900">{{ detailModal.data?.studentId }}</div>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">课程</label>
                <div class="text-sm text-gray-900">{{ detailModal.data?.className }}</div>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">请假类型</label>
                <div class="text-sm text-gray-900">{{ formatType(detailModal.data?.leaveType || '') }}</div>
              </div>
            </div>

            <!-- 课程时间 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">课程时间</label>
              <div class="text-sm text-gray-900">{{ detailModal.data?.date }} ({{ detailModal.data?.classTime }})</div>
            </div>

            <!-- 请假原因 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">请假原因</label>
              <div class="text-sm text-gray-900 bg-gray-50 rounded-lg p-3">{{ detailModal.data?.reason || '无' }}</div>
            </div>

            <!-- 附件信息 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">证明材料</label>
              <div v-if="detailModal.data?.proofUrl" class="text-sm">
                <div class="flex items-center gap-2">

                  <button
                      @click="downloadFile(detailModal.data.proofUrl, getFileNameFromUrl(detailModal.data.proofUrl))"
                      class="flex items-center gap-1 text-green-600 hover:text-green-700 hover:bg-green-50 px-2 py-1 rounded text-xs transition-colors"
                      title="下载附件"
                  >
                    <Download class="w-3 h-3" />
                    下载
                  </button>
                </div>
                <!-- 调试信息：显示实际的proofUrl值 -->
                <div class="mt-1 text-xs text-gray-400" v-if="showDebugInfo">
                  URL: {{ detailModal.data.proofUrl }}
                </div>
              </div>
              <div v-else class="text-sm text-gray-500">无附件
                <!-- 调试信息：显示为什么没有附件 -->
                <div class="mt-1 text-xs text-gray-400" v-if="showDebugInfo">
                  proofUrl: {{ detailModal.data?.proofUrl || 'null/empty' }}
                </div>
              </div>
            </div>
          </div>

          <div class="p-6 border-t border-gray-100 flex justify-end">
            <button @click="closeDetailModal" class="px-4 py-2 bg-white border border-gray-300 text-gray-700 rounded-lg text-sm font-medium hover:bg-gray-50">
              关闭
            </button>
          </div>
        </div>
      </div>
    </Transition>

    <Transition name="fade">
      <div v-if="confirmModal.visible" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-black/30 backdrop-blur-sm" @click="closeModal"></div>
        <div class="bg-white rounded-xl shadow-2xl w-full max-w-md relative z-10 overflow-hidden transform transition-all scale-100">
          <div class="p-6 text-center">
            <div class="w-12 h-12 bg-blue-100 text-blue-600 rounded-full flex items-center justify-center mx-auto mb-4">
              <CheckCircle2 class="w-6 h-6" />
            </div>
            <h3 class="text-lg font-bold text-gray-900 mb-2">确认考勤变更？</h3>
            <p class="text-sm text-gray-600 leading-relaxed">
              您即将确认
              <span class="font-bold text-gray-900">
                {{ confirmModal.isBatch ? selectedIds.length + ' 条' : confirmModal.data?.studentName }}
              </span>
              的请假信息。<br />
              确认后，系统将自动把该生考勤标记为“请假”。
            </p>
          </div>
          <div class="bg-gray-50 px-6 py-4 flex gap-3">
            <button
              @click="closeModal"
              class="flex-1 px-4 py-2.5 bg-white border border-gray-200 text-gray-700 rounded-lg text-sm font-medium hover:bg-gray-50 transition-colors"
            >
              再想想
            </button>
            <button
              @click="submitConfirm"
              class="flex-1 px-4 py-2.5 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-sm font-medium shadow-sm transition-colors"
            >
              确认知悉
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { Search, AlertCircle, TrendingUp, Calendar, CheckCircle2, Check, Filter , Download , X } from 'lucide-vue-next';
import { ElMessage } from 'element-plus';
import { getTeacherTodos, confirmImpact, type LeaveImpactTask } from '@/api/teacher';

const searchText = ref('');
const currentFilter = ref<'pending' | 'confirmed' | 'all'>('pending');

const selectedIds = ref<string[]>([]);

// ===== 详情弹窗逻辑 =====
const detailModal = ref({
  visible: false,
  data: null as LeaveImpactTask | null
});
// 调试开关（开发时设为true，生产时设为false）
const showDebugInfo = ref(true);

// ===== 数据持久化逻辑 =====
const STORAGE_KEY = 'teacher_confirmed_records';

// 从本地存储加载已确认记录
const loadConfirmedRecords = (): LeaveImpactTask[] => {
  try {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored) {
      return JSON.parse(stored);
    }
  } catch (error) {
    console.error('加载已确认记录失败:', error);
  }
  return [];
};

// 保存已确认记录到本地存储
const saveConfirmedRecords = (records: LeaveImpactTask[]) => {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(records));
  } catch (error) {
    console.error('保存已确认记录失败:', error);
  }
};

// 清理过期的已确认记录（保留最近30天的记录）
const cleanupExpiredRecords = () => {
  const now = new Date();
  const thirtyDaysAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);

  const filtered = confirmedData.value.filter(record => {
    const recordDate = new Date(record.date);
    return recordDate >= thirtyDaysAgo;
  });

  if (filtered.length !== confirmedData.value.length) {
    confirmedData.value = filtered;
    saveConfirmedRecords(filtered);
  }
};



const openDetailModal = (data: LeaveImpactTask) => {
  detailModal.value = { visible: true, data };
};

const closeDetailModal = () => {
  detailModal.value.visible = false;
};

const pendingData = ref<LeaveImpactTask[]>([]);
// 从本地存储初始化已确认记录
const confirmedData = ref<LeaveImpactTask[]>(loadConfirmedRecords());

const loading = ref(false);

const filters: Array<{ label: string; value: 'pending' | 'confirmed' | 'all' }> = [
{ label: '待确认', value: 'pending' },
  { label: '已确认', value: 'confirmed' },
  { label: '全部', value: 'all' }
];

// 组合成页面表格数据（待确认 + 本地已确认）
const tableData = computed(() => [...pendingData.value, ...confirmedData.value]);

const teacherId = computed(() => {
  const s = localStorage.getItem('userId') || localStorage.getItem('teacherId');
  const v = s ? Number(s) : 0;
  return Number.isFinite(v) && v > 0 ? v : 0;
});

// --- 数据获取 ---
const fetchData = async () => {
  if (!teacherId.value) {
    pendingData.value = [];
    return;
  }

  loading.value = true;
  try {
    const list = await getTeacherTodos(teacherId.value);
    pendingData.value = Array.isArray(list) ? list : [];

    // 清理过期的已确认记录
    cleanupExpiredRecords();

    // 刷新后清理勾选
    selectedIds.value = [];
  } finally {
    loading.value = false;
  }
};

// --- 计算属性 ---
const filteredList = computed(() => {
  return tableData.value.filter(item => {
    // 状态筛选
    if (currentFilter.value === 'pending' && item.status !== 4) return false;
    if (currentFilter.value === 'confirmed' && item.status === 4) return false;

    // 文本搜索
    if (searchText.value) {
      const text = searchText.value.toLowerCase();
      return (
        String(item.studentName || '').toLowerCase().includes(text) ||
        String(item.studentId || '').toLowerCase().includes(text) ||
        String(item.className || '').toLowerCase().includes(text)
      );
    }
    return true;
  });
});

const pendingCount = computed(() => pendingData.value.length);

const courseCount = computed(() => {
  const ids = new Set<number>();
  for (const i of pendingData.value) {
    if (Number.isFinite(i.courseId) && i.courseId > 0) ids.add(i.courseId);
  }
  return ids.size;
});

// 计算本周范围（周一 00:00:00 到下周一 00:00:00）
const weekRange = (d: Date) => {
  const day = (d.getDay() + 6) % 7; // 把周日(0)转成6，周一(1)转成0
  const monday = new Date(d);
  monday.setHours(0, 0, 0, 0);
  monday.setDate(d.getDate() - day);
  const nextMonday = new Date(monday);
  nextMonday.setDate(monday.getDate() + 7);
  return { monday, nextMonday };
};

const inRange = (dateStr: string, start: Date, end: Date) => {
  if (!dateStr) return false;
  const ds = dateStr.slice(0, 10);
  const dt = new Date(ds + 'T00:00:00');
  if (Number.isNaN(dt.getTime())) return false;
  return dt.getTime() >= start.getTime() && dt.getTime() < end.getTime();
};

const confirmedThisWeek = computed(() => {
  const now = new Date();
  const { monday, nextMonday } = weekRange(now);
  return confirmedData.value.filter(i => inRange(i.date, monday, nextMonday)).length;
});

const lastWeekConfirmed = computed(() => {
  const now = new Date();
  const { monday } = weekRange(now);
  const lastMonday = new Date(monday);
  lastMonday.setDate(monday.getDate() - 7);
  return confirmedData.value.filter(i => inRange(i.date, lastMonday, monday)).length;
});

const confirmedWeekDeltaText = computed(() => {
  const delta = confirmedThisWeek.value - lastWeekConfirmed.value;
  return delta >= 0 ? `+${delta}` : String(delta);
});

// 勾选逻辑只对“待确认”生效
const selectableList = computed(() => filteredList.value.filter(i => i.status === 4));

const isAllSelected = computed(() => {
  return selectableList.value.length > 0 && selectedIds.value.length === selectableList.value.length;
});

const toggleSelectAll = () => {
  if (isAllSelected.value) {
    selectedIds.value = [];
  } else {
    selectedIds.value = selectableList.value.map(i => i.id);
  }
};

// 过滤器变化时，清理不在可选列表内的勾选
watch([currentFilter, filteredList], () => {
  const allow = new Set(selectableList.value.map(i => i.id));
  selectedIds.value = selectedIds.value.filter(id => allow.has(id));
});

// --- 弹窗 ---
const confirmModal = ref<{
  visible: boolean;
  isBatch: boolean;
  data: LeaveImpactTask | null;
}>({
  visible: false,
  isBatch: false,
  data: null
});

const handleSingleConfirm = (item: LeaveImpactTask) => {
  confirmModal.value = { visible: true, isBatch: false, data: item };
};

const handleBatchConfirm = () => {
  confirmModal.value = { visible: true, isBatch: true, data: null };
};

const closeModal = () => {
  confirmModal.value.visible = false;
};

const submitConfirm = async () => {
  const ids = confirmModal.value.isBatch ? selectedIds.value : [confirmModal.value.data!.id];

  if (!teacherId.value) {
    ElMessage.error('未获取到教师信息，请重新登录');
    closeModal();
    return;
  }

  loading.value = true;
  try {
    for (const id of ids) {
      await confirmImpact(id, teacherId.value);

      // 从待确认移入已确认（本地）
      const idx = pendingData.value.findIndex(i => i.id === id);
      if (idx >= 0) {
        const item = pendingData.value.splice(idx, 1)[0];
        confirmedData.value.unshift({ ...item, status: 5 });
      }
    }

    // 保存已确认记录到本地存储
    saveConfirmedRecords(confirmedData.value);

    ElMessage.success('操作成功，考勤状态已更新');
    selectedIds.value = [];
    closeModal();
  } finally {
    loading.value = false;
  }
};

// --- 样式工具 ---
const getTypeStyles = (type: string) => {
  const t = String(type || '').toLowerCase();
  if (t === 'sick') return 'bg-red-50 text-red-700 border-red-100';
  if (t === 'personal') return 'bg-orange-50 text-orange-700 border-orange-100';
  return 'bg-blue-50 text-blue-700 border-blue-100';
};

const formatType = (type: string) => {
  const t = String(type || '').toLowerCase();
  const map: Record<string, string> = { sick: '病假', personal: '事假', public: '公假' };
  return map[t] || type;
};

onMounted(fetchData);

// ===== 附件下载功能 =====
const downloadFile = async (fileUrl: string, fileName: string) => {
  try {
    // 取出服务端文件名（去掉 ? 后面的参数）
    const last = (fileUrl || '').split('/').pop() || '';
    const serverFilename = decodeURIComponent(last.split('?')[0]);

    const downloadUrl = `${import.meta.env.VITE_API_BASE_URL || ''}/api/download/${serverFilename}`;

    const token = localStorage.getItem('token');
    const response = await fetch(downloadUrl, {
      method: 'GET',
      headers: token ? { Authorization: `Bearer ${token}` } : undefined
    });

    if (!response.ok) {
      throw new Error(`下载失败: ${response.status} ${response.statusText}`);
    }

    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);

    const link = document.createElement('a');
    link.href = url;
    link.download = fileName || 'attachment';
    link.style.display = 'none';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    window.URL.revokeObjectURL(url);
    ElMessage.success('文件下载成功');
  } catch (error) {
    console.error('下载文件失败:', error);
    ElMessage.error('文件下载失败，请检查网络连接或重新登录');
  }
};


// 提取文件名（从URL中提取或使用默认名称）
const getFileNameFromUrl = (url: string): string => {
  const urlParts = url.split('/');
  const filename = urlParts[urlParts.length - 1];
  // 如果URL中没有文件名，使用默认名称
  if (!filename || filename.includes('?')) {
    return 'attachment.pdf';
  }
  return decodeURIComponent(filename);
};

</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
