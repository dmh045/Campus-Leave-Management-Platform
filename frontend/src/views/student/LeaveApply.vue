<template>
  <div class="max-w-3xl mx-auto space-y-6">
    <div class="bg-blue-50 border border-blue-200 rounded-lg p-4 flex items-start gap-3">
      <Info class="w-5 h-5 text-blue-600 mt-0.5 flex-shrink-0" />
      <p class="text-sm text-blue-800">
        请假提示：病假请上传医院证明，事假请详细说明事由。超过3天的请假需院领导审批。
      </p>
    </div>

    <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
      <div class="px-6 py-4 border-b border-gray-100 bg-gray-50/50">
        <h2 class="text-lg font-bold text-gray-800">
          {{ isEditMode ? '补充并重新提交' : '发起请假申请' }}
        </h2>
      </div>

      <div class="p-6 space-y-6">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">
            <span class="text-red-500">*</span> 请假类型
          </label>
          <div class="flex gap-4">
            <button
                v-for="type in leaveTypes"
                :key="type.value"
                type="button"
                @click="form.type = type.value"
                class="flex-1 px-4 py-3 rounded-lg border-2 transition-all text-sm font-medium"
                :class="
                form.type === type.value
                  ? 'border-blue-500 bg-blue-50 text-blue-700'
                  : 'border-gray-200 hover:border-gray-300 text-gray-600'
              "
            >
              {{ type.label }}
            </button>
          </div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              <span class="text-red-500">*</span> 开始时间
            </label>
            <div class="relative">
              <Calendar class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
              <input
                  type="datetime-local"
                  v-model="form.startTime"
                  class="w-full pl-10 pr-4 py-2.5 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm transition-shadow"
              />
            </div>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              <span class="text-red-500">*</span> 结束时间
            </label>
            <div class="relative">
              <Calendar class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
              <input
                  type="datetime-local"
                  v-model="form.endTime"
                  class="w-full pl-10 pr-4 py-2.5 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm transition-shadow"
              />
            </div>
          </div>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">
            <span class="text-red-500">*</span> 请假原因
          </label>
          <div class="relative">
            <FileText class="absolute left-3 top-3 w-4 h-4 text-gray-400" />
            <textarea
                v-model="form.reason"
                rows="4"
                placeholder="请详细说明请假原因..."
                class="w-full pl-10 pr-4 py-2.5 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm transition-shadow resize-none"
            ></textarea>
          </div>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">证明材料（可选）</label>
          <div
              class="border-2 border-dashed border-gray-200 rounded-lg p-6 hover:border-blue-300 transition-colors cursor-pointer text-center group"
              @click="triggerFileInput"
              @drop.prevent="handleDrop"
              @dragover.prevent
          >
            <input
                ref="fileInput"
                type="file"
                class="hidden"
                accept=".jpg,.jpeg,.png,.pdf,.doc,.docx"
                @change="handleFileSelect"
            />
            <div class="flex flex-col items-center gap-3">
              <div class="p-3 bg-gray-100 rounded-full group-hover:bg-blue-100 transition-colors">
                <Upload class="w-6 h-6 text-gray-400 group-hover:text-blue-500" />
              </div>
              <div>
                <p class="text-sm font-medium text-gray-700">点击上传或拖拽文件到这里</p>
                <p class="text-xs text-gray-500 mt-1">支持 JPG, PNG, PDF, DOC (不超过 5MB)</p>
              </div>
            </div>
          </div>

          <!-- 已上传文件列表 -->
          <div v-if="uploadedFiles.length > 0" class="mt-4 space-y-2">
            <div v-for="file in uploadedFiles" :key="file.name" class="flex items-center justify-between bg-gray-50 rounded-lg p-3">
              <div class="flex items-center gap-3">
                <FileText class="w-4 h-4 text-gray-500" />
                <div>
                  <div v-if="file.status === 'success' && file.url" class="flex items-center gap-2">
                    <button
                        @click="downloadFile(file.url, file.name)"
                        class="text-blue-600 hover:text-blue-700 underline flex items-center gap-1 transition-colors"
                        :title="`下载 ${file.name}`"
                    >
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
                      </svg>
                      {{ file.name }}
                    </button>
                  </div>
                  <p v-else class="text-sm font-medium text-gray-700">{{ file.name }}</p>
                  <p class="text-xs text-gray-500">{{ formatFileSize(file.size) }}</p>
                </div>
              </div>
              <div class="flex items-center gap-2">
                <span v-if="file.status === 'uploading'" class="text-xs text-blue-600 flex items-center gap-1">
                  <svg class="w-3 h-3 animate-spin" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
                  </svg>
                  上传中...
                </span>
                <span v-else-if="file.status === 'success'" class="text-xs text-green-600 flex items-center gap-1">
                  <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
                  </svg>
                  上传成功
                </span>
                <span v-else-if="file.status === 'error'" class="text-xs text-red-600 flex items-center gap-1">
                  <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                  </svg>
                  上传失败
                </span>
                <button
                    @click="removeFile(file)"
                    class="p-1 rounded hover:bg-gray-200 transition-colors"
                    title="删除文件"
                >
                  <X class="w-3 h-3 text-gray-500" />
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="px-6 py-4 border-t border-gray-100 bg-gray-50/50 flex justify-end gap-3">
        <div class="flex gap-2">
          <button
              type="button"
              @click="saveDraft"
              class="px-4 py-2 text-sm font-medium text-blue-600 bg-white border border-blue-200 rounded-lg hover:bg-blue-50 transition-colors flex items-center gap-2"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7H5a2 2 0 00-2 2v9a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-3m-1 4l-3 3m0 0l-3-3m3 3V4"></path>
            </svg>
            保存
          </button>
          <button
              type="button"
              @click="clearDraft"
              class="px-4 py-2 text-sm font-medium text-gray-600 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors flex items-center gap-2"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
            </svg>
            清空
          </button>
        </div>

        <button
            type="button"
            @click="router.push('/leave/list')"
            class="px-6 py-2.5 text-sm font-medium text-gray-600 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
        >
          取消
        </button>

        <button
            type="button"
            @click="submit"
            class="px-6 py-2.5 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-colors"
        >
          {{ isEditMode ? '重新提交' : '提交申请' }}
        </button>
      </div>
    </div>

    <!-- 草稿保存提示 -->
    <div v-if="showDraftSaved" class="fixed top-4 right-4 bg-green-500 text-white px-4 py-2 rounded-lg shadow-lg z-50">
      草稿已保存
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { Calendar, Upload, FileText, Info, X } from 'lucide-vue-next';
import { ElMessage, ElMessageBox } from 'element-plus';
import { createLeave, resubmitLeave, getLeaveDetail, type LeaveSubmitForm } from '@/api/leave';
import { uploadFile } from '@/api/upload';

// 首先定义接口
interface UploadedFile {
  name: string;
  size: number;
  file: File;
  status: 'pending' | 'uploading' | 'success' | 'error';
  url?: string;
}

interface DraftData {
  form: {
    type: string;
    startTime: string;
    endTime: string;
    reason: string;
    proofUrls: string[];
  };
  uploadedFiles: Array<{
    name: string;
    size: number;
    status: 'pending' | 'uploading' | 'success' | 'error';
    url?: string;
  }>;
  savedAt: string;
}

const fileInput = ref<HTMLInputElement>();
const router = useRouter();
const route = useRoute();
const showDraftSaved = ref(false);

// ✅ 支持 ?editId=xxx（退回后继续编辑）
const editLeaveId = computed<number | null>(() => {
  const q: any = route.query.editId;
  const raw = Array.isArray(q) ? q[0] : q;
  const n = raw ? Number(raw) : NaN;
  return Number.isFinite(n) && n > 0 ? n : null;
});
const isEditMode = computed(() => editLeaveId.value !== null);

const leaveTypes = [
  { label: '病假', value: 'sick' },
  { label: '事假', value: 'personal' },
  { label: '公假', value: 'public' },
];

const form = reactive({
  type: 'sick',
  startTime: '',
  endTime: '',
  reason: '',
  proofUrls: [] as string[] // 存储上传成功的附件URL
});

const uploadedFiles = ref<UploadedFile[]>([]);

// 草稿存储键名（按用户区分；编辑模式单独一份）
const getDraftKey = (): string => {
  const userId = localStorage.getItem('userId') || 'anonymous';
  if (isEditMode.value && editLeaveId.value) {
    return `leave_edit_${editLeaveId.value}_${userId}`;
  }
  return `leave_draft_${userId}`;
};

// 保存草稿
const saveDraft = () => {
  try {
    const draftData: DraftData = {
      form: {
        type: form.type,
        startTime: form.startTime,
        endTime: form.endTime,
        reason: form.reason,
        proofUrls: [...form.proofUrls]
      },
      uploadedFiles: uploadedFiles.value.map(file => ({
        name: file.name,
        size: file.size,
        status: file.status,
        url: file.url
      })),
      savedAt: new Date().toISOString()
    };

    localStorage.setItem(getDraftKey(), JSON.stringify(draftData));

    showDraftSaved.value = true;
    setTimeout(() => {
      showDraftSaved.value = false;
    }, 2000);

    ElMessage.success('草稿保存成功');
  } catch (error) {
    console.error('保存草稿失败:', error);
    ElMessage.error('草稿保存失败');
  }
};

// 清空草稿
const clearDraft = async () => {
  try {
    await ElMessageBox.confirm(
        '确定要清空当前草稿吗？此操作不可恢复。',
        '清空草稿',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
    );

    localStorage.removeItem(getDraftKey());
    resetForm();

    ElMessage.success('草稿已清空');
  } catch (error) {
    // 用户取消操作
  }
};

// 重置表单
const resetForm = () => {
  form.type = 'sick';
  form.startTime = '';
  form.endTime = '';
  form.reason = '';
  form.proofUrls = [];
  uploadedFiles.value = [];
};

// 加载草稿
const loadDraft = () => {
  try {
    const draftDataStr = localStorage.getItem(getDraftKey());
    if (!draftDataStr) return;

    const draftData: DraftData = JSON.parse(draftDataStr);

    form.type = draftData.form.type || 'sick';
    form.startTime = draftData.form.startTime || '';
    form.endTime = draftData.form.endTime || '';
    form.reason = draftData.form.reason || '';
    form.proofUrls = draftData.form.proofUrls || [];

    uploadedFiles.value = draftData.uploadedFiles.map(fileData => ({
      name: fileData.name,
      size: fileData.size,
      file: new File([], fileData.name),
      status: fileData.status,
      url: fileData.url
    }));

    const savedAt = new Date(draftData.savedAt);
    ElMessage.info(`已加载 ${savedAt.toLocaleString()} 保存的草稿`);
  } catch (error) {
    console.error('加载草稿失败:', error);
    localStorage.removeItem(getDraftKey());
  }
};

// ✅ 工具：后端时间 -> datetime-local (YYYY-MM-DDTHH:mm)
const toDateTimeLocal = (v: any) => {
  if (!v) return '';
  const d = new Date(v);
  if (Number.isNaN(d.getTime())) return '';
  const pad = (n: number) => String(n).padStart(2, '0');
  const y = d.getFullYear();
  const m = pad(d.getMonth() + 1);
  const day = pad(d.getDate());
  const hh = pad(d.getHours());
  const mm = pad(d.getMinutes());
  return `${y}-${m}-${day}T${hh}:${mm}`;
};

// ✅ 编辑模式加载旧单（时间/原因/附件）
const loadForEdit = async () => {
  if (!editLeaveId.value) return;
  try {
    const dto: any = await getLeaveDetail(editLeaveId.value);

    const t = String(dto?.leaveType || '').toUpperCase();
    form.type = t === 'SICK' ? 'sick' : t === 'PERSONAL' ? 'personal' : 'public';

    form.startTime = toDateTimeLocal(dto?.startTime);
    form.endTime = toDateTimeLocal(dto?.endTime);
    form.reason = dto?.reason || '';

    const baseUrl = import.meta.env.VITE_API_BASE_URL || '';
    const urls = (dto?.proofUrl ? String(dto.proofUrl) : '')
        .split(',')
        .map((s: string) => s.trim())
        .filter(Boolean)
        .map((u: string) => (u.startsWith('http') ? u : baseUrl + (u.startsWith('/') ? u : '/' + u)));

    form.proofUrls = urls;

    // 用“成功文件”占位，方便下载/删除
    uploadedFiles.value = urls.map((u: string) => {
      const last = (u || '').split('/').pop() || '附件';
      const name = decodeURIComponent(last.split('?')[0]);
      return {
        name,
        size: 0,
        file: new File([], name),
        status: 'success',
        url: u
      } as UploadedFile;
    });

    ElMessage.info('已加载退回的请假单，可修改后重新提交');
  } catch (e) {
    console.error('加载退回单失败:', e);
    ElMessage.error('加载请假单失败，请刷新后重试');
  }
};

// 组件挂载：编辑模式不加载普通草稿，避免覆盖
onMounted(() => {
  if (isEditMode.value) {
    loadForEdit();
  } else {
    loadDraft();
  }
});

const mapLeaveType = (v: string): LeaveSubmitForm['leaveType'] => {
  if (v === 'sick') return 'SICK';
  if (v === 'personal') return 'PERSONAL';
  return 'PUBLIC';
};

// input[type=datetime-local] 常见格式：YYYY-MM-DDTHH:mm 或 YYYY-MM-DDTHH:mm:ss
const normalizeDateTime = (v: string) => {
  if (!v) return v;
  if (v.length > 19) return v.slice(0, 19);
  if (v.length === 16) return v + ':00';
  return v;
};

const triggerFileInput = () => {
  fileInput.value?.click();
};

const handleFileSelect = (event: Event) => {
  const input = event.target as HTMLInputElement;
  if (input.files && input.files.length > 0) {
    processFiles(Array.from(input.files));
    input.value = '';
  }
};

const handleDrop = (event: DragEvent) => {
  if (event.dataTransfer?.files) {
    processFiles(Array.from(event.dataTransfer.files));
  }
};

const processFiles = (files: File[]) => {
  files.forEach(file => {
    if (file.size > 5 * 1024 * 1024) {
      ElMessage.warning(`文件 ${file.name} 超过5MB限制`);
      return;
    }

    const allowedTypes = ['image/jpeg', 'image/png', 'application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'];
    if (!allowedTypes.includes(file.type)) {
      ElMessage.warning(`文件 ${file.name} 类型不支持`);
      return;
    }

    const uploadedFile: UploadedFile = {
      name: file.name,
      size: file.size,
      file: file,
      status: 'pending'
    };

    uploadedFiles.value.push(uploadedFile);
    uploadFileToServer(uploadedFile);
  });
};

const uploadFileToServer = async (uploadedFile: UploadedFile) => {
  try {
    uploadedFile.status = 'uploading';
    const result = await uploadFile(uploadedFile.file);

    if (result && (result as any).url) {
      uploadedFile.status = 'success';

      let fileUrl = (result as any).url;
      if (fileUrl && !fileUrl.startsWith('http')) {
        const baseUrl = import.meta.env.VITE_API_BASE_URL || '';
        fileUrl = baseUrl + (fileUrl.startsWith('/') ? fileUrl : '/' + fileUrl);
      }
      uploadedFile.url = fileUrl;

      if (!form.proofUrls.includes(fileUrl)) {
        form.proofUrls.push(fileUrl);
      }

      ElMessage.success({
        message: `文件 ${uploadedFile.name} 上传成功`,
        duration: 3000,
        showClose: true
      });
    } else {
      uploadedFile.status = 'error';

      let errorMessage = '文件上传失败';
      if (result && (result as any).message) {
        errorMessage = (result as any).message;
      }

      ElMessage.error(`文件 ${uploadedFile.name} 上传失败: ${errorMessage}`);
      console.error('文件上传失败详情:', {
        response: result,
        file: uploadedFile.name
      });
    }
  } catch (error: any) {
    uploadedFile.status = 'error';

    let errorMessage = '文件上传失败';
    if (error?.response?.status === 413) {
      errorMessage = '文件太大，请选择小于5MB的文件';
    } else if (error?.response?.status === 415) {
      errorMessage = '不支持的文件类型';
    } else if (error?.message) {
      errorMessage = error.message;
    }

    ElMessage.error({
      message: `文件 ${uploadedFile.name} 上传失败: ${errorMessage}`,
      duration: 3000,
      showClose: true
    });
    console.error('文件上传异常:', error);
  }
};

const downloadFile = async (fileUrl: string, fileName: string) => {
  try {
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

const removeFile = (fileToRemove: UploadedFile) => {
  const index = uploadedFiles.value.findIndex(file => file === fileToRemove);
  if (index !== -1) {
    uploadedFiles.value.splice(index, 1);

    if (fileToRemove.url) {
      const urlIndex = form.proofUrls.indexOf(fileToRemove.url);
      if (urlIndex !== -1) {
        form.proofUrls.splice(urlIndex, 1);
      }
    }
  }
};

const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

const submit = async () => {
  if (!form.startTime || !form.endTime || !form.reason) {
    ElMessage.warning('请填写完整的请假信息');
    return;
  }

  const uploadingFiles = uploadedFiles.value.filter(file => file.status === 'uploading');
  if (uploadingFiles.length > 0) {
    ElMessage.warning('请等待文件上传完成');
    return;
  }

  const start = new Date(form.startTime);
  const end = new Date(form.endTime);
  if (Number.isNaN(start.getTime()) || Number.isNaN(end.getTime()) || start.getTime() > end.getTime()) {
    ElMessage.warning('请假时间段不合法');
    return;
  }

  const studentIdStr = localStorage.getItem('userId') || localStorage.getItem('studentId');
  if (!studentIdStr) {
    ElMessage.error('未获取到登录信息（userId/studentId），请确认已登录并携带 token');
    return;
  }
  const termIdStr = localStorage.getItem('termId') || '1';

  const payload: LeaveSubmitForm = {
    studentId: Number(studentIdStr),
    termId: Number(termIdStr),
    leaveType: mapLeaveType(form.type),
    applyChannel: 'BY_TIME',
    reason: form.reason,
    startTime: normalizeDateTime(form.startTime),
    endTime: normalizeDateTime(form.endTime),
    proofUrl: form.proofUrls.length > 0 ? form.proofUrls.join(',') : undefined
  };

  try {
    // ✅ 编辑模式走 resubmit，否则走 create
    if (isEditMode.value && editLeaveId.value) {
      await resubmitLeave(editLeaveId.value, payload);
      ElMessage.success('重新提交成功！');
    } else {
      await createLeave(payload);
      ElMessage.success('申请提交成功！');
    }

    // 成功后清理对应草稿
    localStorage.removeItem(getDraftKey());

    router.push('/leave/list');
  } catch (error) {
    ElMessage.error('申请提交失败，请重试');
    console.error('请假申请失败:', error);
  }
};
</script>
