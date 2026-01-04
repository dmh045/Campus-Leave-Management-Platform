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
        <h2 class="text-lg font-bold text-gray-800">发起请假申请</h2>

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
                  <!-- 优化文件链接显示，添加下载图标和更好的视觉反馈 -->
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
          提交申请
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
import { reactive,ref, onMounted} from 'vue';
import { useRouter } from 'vue-router';
import { Calendar, Upload, FileText, Info, X } from 'lucide-vue-next';
import { ElMessage,  ElMessageBox } from 'element-plus';
import { createLeave, type LeaveSubmitForm } from '@/api/leave';
import { uploadFile} from '@/api/upload';

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
const showDraftSaved = ref(false);

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

// 草稿存储键名（按用户区分）
const getDraftKey = (): string => {
  const userId = localStorage.getItem('userId') || 'anonymous';
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

    // 显示保存成功提示
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

    // 恢复表单数据
    form.type = draftData.form.type || 'sick';
    form.startTime = draftData.form.startTime || '';
    form.endTime = draftData.form.endTime || '';
    form.reason = draftData.form.reason || '';
    form.proofUrls = draftData.form.proofUrls || [];

    // 恢复文件列表（注意：File对象无法序列化，所以只恢复基本信息）
    uploadedFiles.value = draftData.uploadedFiles.map(fileData => ({
      name: fileData.name,
      size: fileData.size,
      file: new File([], fileData.name), // 创建空的File对象
      status: fileData.status,
      url: fileData.url
    }));

    // 显示草稿加载提示
    const savedAt = new Date(draftData.savedAt);
    ElMessage.info(`已加载 ${savedAt.toLocaleString()} 保存的草稿`);
  } catch (error) {
    console.error('加载草稿失败:', error);
    // 如果草稿数据损坏，清除它
    localStorage.removeItem(getDraftKey());
  }
};

// // 自动保存草稿（定时器）
// let autoSaveTimer: ReturnType<typeof setTimeout> | null = null;
// const startAutoSave = () => {
//   // 清除之前的定时器
//   if (autoSaveTimer) {
//     clearTimeout(autoSaveTimer);
//   }
//
//   // 设置新的定时器（5秒后自动保存）
//   autoSaveTimer = setTimeout(() => {
//     // 只有在有内容时才自动保存
//     if (form.startTime || form.endTime || form.reason || uploadedFiles.value.length > 0) {
//       saveDraft();
//     }
//   }, 5000);
// };


// 组件挂载时加载草稿
onMounted(() => {
  loadDraft();
});

const mapLeaveType = (v: string): LeaveSubmitForm['leaveType'] => {
  if (v === 'sick') return 'SICK';
  if (v === 'personal') return 'PERSONAL';
  return 'PUBLIC';
};

// input[type=datetime-local] 常见格式：YYYY-MM-DDTHH:mm 或 YYYY-MM-DDTHH:mm:ss
const normalizeDateTime = (v: string) => {
  if (!v) return v;
  // 去掉毫秒
  if (v.length > 19) return v.slice(0, 19);
  // 补秒
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
    input.value = ''; // 重置input
  }
};

const handleDrop = (event: DragEvent) => {
  if (event.dataTransfer?.files) {
    processFiles(Array.from(event.dataTransfer.files));
  }
};

const processFiles = (files: File[]) => {
  files.forEach(file => {
    // 检查文件大小（5MB限制）
    if (file.size > 5 * 1024 * 1024) {
      ElMessage.warning(`文件 ${file.name} 超过5MB限制`);
      return;
    }

    // 检查文件类型
    const allowedTypes = ['image/jpeg', 'image/png', 'application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'];
    if (!allowedTypes.includes(file.type)) {
      ElMessage.warning(`文件 ${file.name} 类型不支持`);
      return;
    }

    // 添加到上传列表
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

    // 直接调用uploadFile，不需要response.data，因为request拦截器已经解包了
    const result = await uploadFile(uploadedFile.file);

    // 检查上传响应数据 - 现在result就是UploadResponse对象或错误信息
    if (result && result.url) {
      // 上传成功：result是UploadResponse对象
      uploadedFile.status = 'success';

      // 确保URL格式正确，如果URL不是完整路径，添加基础URL
      let fileUrl = result.url;
      if (fileUrl && !fileUrl.startsWith('http')) {
        // 如果后端返回的是相对路径，添加基础URL
        const baseUrl = import.meta.env.VITE_API_BASE_URL || '';
        fileUrl = baseUrl + (fileUrl.startsWith('/') ? fileUrl : '/' + fileUrl);
      }
      uploadedFile.url = fileUrl;

      // 添加到proofUrls数组
      if (!form.proofUrls.includes(fileUrl)) {
        form.proofUrls.push(fileUrl);
      }

      // 优化成功提示，显示更长时间并可关闭
      ElMessage.success({
        message: `文件 ${uploadedFile.name} 上传成功`,
        duration: 3000,
        showClose: true
      });
    } else {
      // 处理错误情况：result可能是错误信息对象
      uploadedFile.status = 'error';

      let errorMessage = '文件上传失败';
      if (result && result.message) {
        errorMessage = result.message;
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
    // 从文件URL中提取文件名
    const urlParts = fileUrl.split('/');
    const serverFilename = urlParts[urlParts.length - 1];

    // 构建下载URL（使用后端下载接口）
    const downloadUrl = `${import.meta.env.VITE_API_BASE_URL || ''}/api/download/${serverFilename}`;

    // 使用fetch API下载，携带认证信息
    const response = await fetch(downloadUrl, {
      method: 'GET',
      credentials: 'include' // 包含认证信息
    });

    if (!response.ok) {
      throw new Error(`下载失败: ${response.status} ${response.statusText}`);
    }

    // 获取文件blob
    const blob = await response.blob();

    // 创建下载链接
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = fileName;
    link.style.display = 'none';

    // 触发下载
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    // 释放URL对象
    window.URL.revokeObjectURL(url);

  } catch (error) {
    console.error('下载文件失败:', error);
    ElMessage.error('文件下载失败，请检查网络连接或重新登录');
  }
};

const removeFile = (fileToRemove: UploadedFile) => {
  const index = uploadedFiles.value.findIndex(file => file === fileToRemove);
  if (index !== -1) {
    uploadedFiles.value.splice(index, 1);

    // 从proofUrls中移除对应的URL
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

  // 检查是否有文件正在上传
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
    proofUrl: form.proofUrls.length > 0 ? form.proofUrls.join(',') : undefined // 多个URL用逗号分隔

  };
  try {
    await createLeave(payload);
    ElMessage.success('申请提交成功！');
    router.push('/leave/list');
  } catch (error) {
    ElMessage.error('申请提交失败，请重试');
    console.error('请假申请失败:', error);
  }

  await createLeave(payload);
  // 提交成功后清除草稿
  localStorage.removeItem(getDraftKey());
  ElMessage.success('申请提交成功！');
  router.push('/leave/list');
};
</script>
