<template>
  <div class="space-y-4">
    <div class="bg-white rounded-lg shadow-sm p-4 space-y-4">
      <div class="flex items-center gap-4">
        <div class="flex-1 relative">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            type="text"
            placeholder="搜索学生姓名或学号..."
            v-model="searchText"
            class="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all text-sm"
          />
        </div>

        <button class="flex items-center gap-2 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors text-sm font-medium text-gray-700">
          <Filter class="w-4 h-4" />
          高级筛选
        </button>
      </div>

      <div class="flex items-center gap-2 border-t border-gray-100 pt-4">
        <span class="text-sm text-gray-600 font-medium mr-2">状态：</span>
        <button
          v-for="tab in statusTabs"
          :key="tab.value"
          @click="currentStatus = tab.value"
          :class="[
            'px-3 py-1.5 rounded-lg text-sm transition-colors font-medium',
            currentStatus === tab.value
              ? 'bg-blue-100 text-blue-700'
              : 'bg-gray-50 text-gray-600 hover:bg-gray-100'
          ]"
        >
          {{ tab.label }}
        </button>
      </div>
    </div>

    <Transition
      enter-active-class="transition ease-out duration-200"
      enter-from-class="opacity-0 -translate-y-2"
      enter-to-class="opacity-100 translate-y-0"
      leave-active-class="transition ease-in duration-150"
      leave-from-class="opacity-100 translate-y-0"
      leave-to-class="opacity-0 -translate-y-2"
    >
      <div v-if="selectedIds.length > 0" class="bg-blue-50 border border-blue-200 rounded-lg p-3 flex items-center justify-between shadow-sm">
        <span class="text-sm font-medium text-blue-800 ml-2">已选择 {{ selectedIds.length }} 条申请</span>
        <div class="flex items-center gap-2">
          <button
            @click="handleBatchAction('approve')"
            class="px-3 py-1.5 bg-green-600 hover:bg-green-700 text-white rounded text-xs font-medium transition-colors"
          >
            批量同意
          </button>
          <button
            @click="handleBatchAction('return')"
            class="px-3 py-1.5 bg-orange-600 hover:bg-orange-700 text-white rounded text-xs font-medium transition-colors"
          >
            批量退回
          </button>
          <button
            @click="handleBatchAction('reject')"
            class="px-3 py-1.5 bg-red-600 hover:bg-red-700 text-white rounded text-xs font-medium transition-colors"
          >
            批量驳回
          </button>
          <button
            @click="selectedIds = []"
            class="px-3 py-1.5 bg-white border border-gray-300 text-gray-600 rounded text-xs font-medium hover:bg-gray-50 transition-colors ml-2"
          >
            取消
          </button>
        </div>
      </div>
    </Transition>

    <div class="bg-white rounded-lg shadow-sm overflow-hidden border border-gray-100">
      <div class="overflow-x-auto">
        <table class="w-full">
          <thead class="bg-gray-50 border-b border-gray-200">
            <tr>
              <th class="px-4 py-3 text-center w-14">
                <input
                  type="checkbox"
                  :checked="isAllSelected"
                  @change="toggleSelectAll"
                  class="w-4 h-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                />
              </th>
              <th class="px-6 py-3 text-left text-sm font-medium text-gray-500">学生信息</th>
              <th class="px-6 py-3 text-left text-sm font-medium text-gray-500">班级</th>
              <th class="px-6 py-3 text-left text-sm font-medium text-gray-500">类型</th>
              <th class="px-6 py-3 text-left text-sm font-medium text-gray-500">详情</th>
              <th class="px-6 py-3 text-left text-sm font-medium text-gray-500">提交时间</th>
              <th class="px-6 py-3 text-center text-sm font-medium text-gray-500">操作</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-100">
            <tr v-for="req in filteredRequests" :key="req.id" class="hover:bg-gray-50/80 transition-colors">
              <td class="px-4 py-4 text-center">
                <input
                  type="checkbox"
                  :value="req.id"
                  v-model="selectedIds"
                  class="w-4 h-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                />
              </td>
              <td class="px-6 py-4">
                <div class="flex items-center gap-3">
                  <div class="w-8 h-8 rounded-full bg-blue-100 text-blue-600 flex items-center justify-center text-xs font-bold">
                    {{ req.studentName.charAt(0) }}
                  </div>
                  <div>
                    <div class="text-sm font-medium text-gray-900">{{ req.studentName }}</div>
                    <div class="text-xs text-gray-500">{{ req.studentId }}</div>
                  </div>
                </div>
              </td>
              <td class="px-6 py-4 text-sm text-gray-600">{{ req.class }}</td>
              <td class="px-6 py-4">
                <span :class="['px-2 py-1 rounded text-xs font-medium', getTypeColor(req.type)]">
                  {{ req.type }}
                </span>
              </td>
              <td class="px-6 py-4">
                <div class="text-sm text-gray-900 font-medium truncate max-w-[200px]">{{ req.reason }}</div>
                <div class="text-xs text-gray-500 mt-0.5">
                  {{ req.startTime.split(' ')[0] }} ~ {{ req.endTime.split(' ')[0] }}
                </div>
              </td>
              <td class="px-6 py-4 text-sm text-gray-500">{{ req.submitTime }}</td>
              <td class="px-6 py-4">
                <div class="flex items-center justify-center gap-1">
                  <button @click="openActionModal('approve', req)" class="p-1.5 text-green-600 hover:bg-green-50 rounded transition-colors" title="同意">
                    <Check class="w-4 h-4" />
                  </button>
                  <button @click="openActionModal('return', req)" class="p-1.5 text-orange-600 hover:bg-orange-50 rounded transition-colors" title="退回">
                    <RotateCcw class="w-4 h-4" />
                  </button>
                  <button @click="openActionModal('reject', req)" class="p-1.5 text-red-600 hover:bg-red-50 rounded transition-colors" title="驳回">
                    <X class="w-4 h-4" />
                  </button>
                  <button
                      @click="openDetailModal(req)"
                      class="text-blue-600 hover:text-blue-700 hover:bg-blue-50 px-3 py-1.5 rounded text-sm font-medium transition-colors"
                  >
                    查看详情
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <div v-if="filteredRequests.length === 0" class="text-center py-12 text-gray-400">
        暂无待办审批
      </div>
    </div>

    <!-- 新增：请假详情弹窗 -->
    <Transition name="fade">
      <div v-if="detailModal.visible" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-black/40 backdrop-blur-sm" @click="closeDetailModal"></div>
        <div class="bg-white rounded-xl shadow-xl w-full max-w-2xl relative z-10 overflow-hidden animate-fade-in">
          <div class="px-6 py-4 border-b border-gray-100 bg-gray-50/50 flex justify-between items-center">
            <h3 class="font-bold text-gray-800">请假申请详情</h3>
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
                <label class="block text-sm font-medium text-gray-700 mb-1">班级</label>
                <div class="text-sm text-gray-900">{{ detailModal.data?.class }}</div>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">请假类型</label>
                <div class="text-sm text-gray-900">{{ detailModal.data?.type }}</div>
              </div>
            </div>

            <!-- 请假时间 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">请假时间</label>
              <div class="text-sm text-gray-900">{{ detailModal.data?.startTime }} - {{ detailModal.data?.endTime }}</div>
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

          <div class="px-6 py-4 bg-gray-50 border-t border-gray-100 flex justify-end">
            <button @click="closeDetailModal" class="px-4 py-2 bg-white border border-gray-300 text-gray-700 rounded-lg text-sm font-medium hover:bg-gray-50">
              关闭
            </button>
          </div>
        </div>
      </div>
    </Transition>

    <Transition name="fade">
      <div v-if="actionModal.visible" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-black/40 backdrop-blur-sm" @click="closeModal"></div>
        <div class="bg-white rounded-xl shadow-xl w-full max-w-md relative z-10 overflow-hidden animate-fade-in">
          <div class="px-6 py-4 border-b border-gray-100 bg-gray-50/50 flex justify-between items-center">
            <h3 class="font-bold text-gray-800">
              {{ getModalTitle(actionModal.type) }}
            </h3>
            <button @click="closeModal" class="text-gray-400 hover:text-gray-600"><X class="w-5 h-5"/></button>
          </div>
          
          <div class="p-6 space-y-4">
            <p class="text-sm text-gray-600">
              您即将对 <span class="font-bold text-gray-900">{{ actionModal.isBatch ? selectedIds.length + ' 个申请' : actionModal.data?.studentName }}</span> 执行操作。
            </p>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1.5">
                {{ actionModal.type === 'approve' ? '审批意见 (选填)' : '原因说明 (必填)' }}
              </label>
              <textarea
                v-model="actionRemark"
                rows="3"
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm resize-none"
                :placeholder="actionModal.type === 'approve' ? '同意，请注意补课...' : '请填写具体原因...'"
              ></textarea>
            </div>
          </div>

          <div class="px-6 py-4 bg-gray-50 border-t border-gray-100 flex justify-end gap-3">
            <button @click="closeModal" class="px-4 py-2 bg-white border border-gray-300 text-gray-700 rounded-lg text-sm font-medium hover:bg-gray-50">
              取消
            </button>
            <button 
              @click="confirmAction"
              :class="['px-4 py-2 text-white rounded-lg text-sm font-medium shadow-sm transition-colors', getActionBtnColor(actionModal.type)]"
            >
              确认提交
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { Search, Filter, Check, RotateCcw, X , Download } from 'lucide-vue-next';
import { ElMessage } from 'element-plus';
import dayjs from 'dayjs';
import { getCounselorLeaves, counselorApproveLeave, counselorBatchApproveLeaves, type CounselorPendingLeaveDTO, type CounselorAction } from '@/api/counselor';


// ====== 与原UI字段保持一致 ======
type UIStatus = 'pending' | 'approved' | 'returned' | 'rejected';

interface UIRequest {
  id: string; // leaveId
  studentName: string;
  studentId: string;
  class: string;
  type: string; // 病假/事假/公假
  startTime: string;
  endTime: string;
  reason: string;
  submitTime: string;
  status: UIStatus;
  proofUrl?: string;
}

const mapLeaveType = (t: string): string => {
  const s = String(t || '').toUpperCase();
  if (s === 'SICK') return '病假';
  if (s === 'PERSONAL') return '事假';
  return '公假';
};

const mapStatus = (s: string): UIStatus => {
  const st = String(s || '').toUpperCase();
  if (st === 'PENDING_COUNSELOR') return 'pending';
  if (st === 'RETURNED') return 'returned';
  if (st === 'REJECTED') return 'rejected';
  // 辅导员通过后会变成 PENDING_TEACHER，后续可能 APPROVED/ENDED/CANCELLED 等，都归为“已通过”
  return 'approved';
};

const mapDtoToUI = (dto: CounselorPendingLeaveDTO): UIRequest => {
  const start = dto.startTime ? dayjs(dto.startTime).format('YYYY-MM-DD HH:mm') : '';
  const end = dto.endTime ? dayjs(dto.endTime).format('YYYY-MM-DD HH:mm') : '';
  const submitRaw = (dto as any).submitTime || (dto as any).createTime || (dto as any).applyTime || dto.startTime;
  const submitTime = submitRaw ? dayjs(submitRaw).format('YYYY-MM-DD HH:mm') : start;

  return {
    id: String(dto.leaveId),
    studentName: String(dto.studentName || ''),
    studentId: String(dto.studentId ?? ''),
    class: String(dto.className || ''),
    type: mapLeaveType(dto.leaveType),
    startTime: start,
    endTime: end,
    submitTime,
    reason: String(dto.reason || ''),
    proofUrl: String(dto.proofUrl || ''),
    status: mapStatus(dto.status) // ✅ 关键
  };
};


const counselorId = computed(() => {
  const s = localStorage.getItem('userId') || localStorage.getItem('counselorId');
  const v = s ? Number(s) : 0;
  return Number.isFinite(v) && v > 0 ? v : 0;
});

// ===== 数据（由后端提供）=====
const requests = ref<UIRequest[]>([]);

const fetchAll = async () => {
  if (!counselorId.value) {
    requests.value = [];
    return;
  }
  try {
    const list = await getCounselorLeaves(counselorId.value);
    requests.value = Array.isArray(list) ? list.map(mapDtoToUI) : [];
  } catch (e: any) {
    ElMessage.error(e?.message || '获取列表失败');
    requests.value = [];
  }
};

onMounted(() => {
  fetchAll();
});


// ===== 筛选 / 搜索 =====
const searchText = ref('');
const currentStatus = ref<'all' | UIStatus>('all');

const statusTabs: Array<{ label: string; value: 'all' | UIStatus }> = [
  { label: '全部', value: 'all' },
  { label: '待处理', value: 'pending' },
  { label: '已通过', value: 'approved' },
  { label: '已退回', value: 'returned' },
  { label: '已驳回', value: 'rejected' }
];

const filteredRequests = computed(() => {
  return requests.value.filter((req) => {
    if (currentStatus.value !== 'all' && req.status !== currentStatus.value) return false;
    if (searchText.value) {
      return req.studentName.includes(searchText.value) || req.studentId.includes(searchText.value);
    }
    return true;
  });
});

// ===== 多选逻辑 =====
const selectedIds = ref<string[]>([]);

const isAllSelected = computed(() => {
  return filteredRequests.value.length > 0 && selectedIds.value.length === filteredRequests.value.length;
});

const toggleSelectAll = () => {
  if (isAllSelected.value) {
    selectedIds.value = [];
  } else {
    selectedIds.value = filteredRequests.value.map((r) => r.id);
  }
};

// ===== 详情弹窗逻辑 =====
const detailModal = ref({
  visible: false,
  data: null as UIRequest | null
});
// 调试开关（开发时设为true，生产时设为false）
const showDebugInfo = ref(true);

const openDetailModal = (data: UIRequest) => {
  detailModal.value = { visible: true, data };
};

const closeDetailModal = () => {
  detailModal.value.visible = false;
};


// ===== 弹窗逻辑 =====
const actionRemark = ref('');

const actionModal = ref({
  visible: false,
  type: 'approve' as 'approve' | 'return' | 'reject',
  isBatch: false,
  data: null as any
});



const openActionModal = (type: 'approve' | 'return' | 'reject', data: any) => {
  actionModal.value = { visible: true, type, isBatch: false, data };
  actionRemark.value = '';
};

const handleBatchAction = (type: 'approve' | 'return' | 'reject') => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择要处理的请假申请');
    return;
  }
  actionModal.value = { visible: true, type, isBatch: true, data: selectedIds.value.slice() };
  actionRemark.value = '';
};

const closeModal = () => {
  actionModal.value.visible = false;
};

const toAction = (type: 'approve' | 'return' | 'reject'): CounselorAction => {
  if (type === 'approve') return 'AGREE';
  if (type === 'return') return 'RETURN';
  return 'REJECT';
};

const confirmAction = async () => {
  if (!counselorId.value) {
    ElMessage.error('未获取到辅导员信息，请重新登录');
    return;
  }

  const action = toAction(actionModal.value.type);

  try {
    if (actionModal.value.isBatch) {
      const ids: number[] = (actionModal.value.data as string[]).map((x) => Number(x)).filter((x) => Number.isFinite(x));
      await counselorBatchApproveLeaves({
        counselorId: counselorId.value,
        action,
        comment: actionRemark.value || '',
        leaveIds: ids
      });

      // 本地更新状态，保持 UI 的状态筛选可用
      const newStatus: UIStatus = actionModal.value.type === 'approve' ? 'approved' : actionModal.value.type === 'return' ? 'returned' : 'rejected';
      requests.value = requests.value.map((r) => (ids.includes(Number(r.id)) ? { ...r, status: newStatus } : r));

      selectedIds.value = [];

      // 批量审批通过时显示通知提示
      if (actionModal.value.type === 'approve') {
        ElMessage.success('批量审批通过，已通知相关任课老师');
      } else {
        ElMessage.success('批量处理完成');
      }
    } else {
      const leaveId = Number(actionModal.value.data?.id);
      await counselorApproveLeave(leaveId, {
        counselorId: counselorId.value,
        action,
        comment: actionRemark.value || ''
      });

      const newStatus: UIStatus = actionModal.value.type === 'approve' ? 'approved' : actionModal.value.type === 'return' ? 'returned' : 'rejected';
      requests.value = requests.value.map((r) => (Number(r.id) === leaveId ? { ...r, status: newStatus } : r));

      // 单个审批通过时显示通知提示
      if (actionModal.value.type === 'approve') {
        ElMessage.success('审批通过，已通知相关任课老师');
      } else {
        ElMessage.success('处理完成');
      }
    }

    closeModal();
  } catch (e: any) {
    ElMessage.error(e?.message || '处理失败');
  }
};

// ===== 工具函数（保留原 UI 色彩逻辑）=====
const getTypeColor = (type: string) => {
  if (type === '病假') return 'bg-red-50 text-red-700 border border-red-100';
  if (type === '事假') return 'bg-yellow-50 text-yellow-700 border border-yellow-100';
  return 'bg-blue-50 text-blue-700 border border-blue-100';
};

const getModalTitle = (type: string) => {
  if (type === 'approve') return '确认同意';
  if (type === 'return') return '确认退回';
  return '确认驳回';
};

const getActionBtnColor = (type: string) => {
  if (type === 'approve') return 'bg-green-600 hover:bg-green-700';
  if (type === 'return') return 'bg-orange-600 hover:bg-orange-700';
  return 'bg-red-600 hover:bg-red-700';
};

// 在辅导员审批方法中添加提示
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
</style>