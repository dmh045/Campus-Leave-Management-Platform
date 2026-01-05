<template>
  <Transition
      enter-active-class="transition ease-out duration-200"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition ease-in duration-150"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
  >
    <div v-if="modelValue" class="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div class="absolute inset-0 bg-black/40 backdrop-blur-sm" @click="close"></div>

      <div class="bg-white rounded-xl shadow-2xl w-full max-w-lg flex flex-col relative z-10 overflow-hidden animate-scale-in">
        <div class="px-6 py-4 border-b border-gray-100 flex justify-between items-center bg-gray-50/50">
          <div>
            <h2 class="text-lg font-bold text-gray-800">
              {{ isEditMode ? '补充并重新提交' : '发起请假' }}
            </h2>
            <p class="text-xs text-gray-500 mt-0.5">
              已选中 <span class="text-blue-600 font-bold">{{ courses.length }}</span> 节课程
            </p>
          </div>
          <button @click="close" class="p-2 hover:bg-gray-200 rounded-full transition-colors text-gray-500">
            <X class="w-5 h-5" />
          </button>
        </div>

        <div class="p-6 space-y-5 overflow-y-auto max-h-[70vh]">
          <div class="bg-blue-50/50 border border-blue-100 rounded-xl p-3">
            <h3 class="text-xs font-bold text-blue-800 uppercase tracking-wider mb-2 flex items-center gap-2">
              <BookOpen class="w-3.5 h-3.5" /> 关联课程
            </h3>
            <div class="space-y-2">
              <div
                  v-for="course in courses"
                  :key="course.id"
                  class="flex items-center justify-between bg-white p-2.5 rounded-lg border border-blue-100 shadow-sm"
              >
                <div>
                  <div class="font-bold text-sm text-gray-900">{{ course.courseName }}</div>
                  <div class="text-xs text-gray-500 mt-0.5">周{{ dayMap[course.dayOfWeek] }} · {{ course.teacherName }}</div>
                </div>
                <div class="text-xs font-medium text-blue-600 bg-blue-50 px-2 py-1 rounded">
                  第{{ course.section }}-{{ course.section + course.duration - 1 }}节
                </div>
              </div>
            </div>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">请假类型</label>
            <div class="grid grid-cols-3 gap-3">
              <label
                  v-for="type in leaveTypes"
                  :key="type.value"
                  class="flex flex-col items-center justify-center gap-1 cursor-pointer p-3 border rounded-xl transition-all"
                  :class="form.type === type.value ? 'border-blue-500 bg-blue-50 text-blue-700 ring-1 ring-blue-500' : 'border-gray-200 hover:bg-gray-50 text-gray-600'"
              >
                <input type="radio" :value="type.value" v-model="form.type" class="hidden" />
                <component :is="type.icon" class="w-5 h-5 mb-1" />
                <span class="text-xs font-medium">{{ type.label }}</span>
              </label>
            </div>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">请假事由</label>
            <textarea
                v-model="form.reason"
                rows="3"
                class="w-full p-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none text-sm resize-none"
                placeholder="请输入具体的请假原因..."
            ></textarea>
          </div>

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

          <div v-if="uploadedFiles.length > 0" class="mt-4 space-y-2">
            <div v-for="file in uploadedFiles" :key="file.name" class="flex items-center justify-between bg-gray-50 rounded-lg p-3">
              <div class="flex items-center gap-3">
                <FileText class="w-4 h-4 text-gray-500" />
                <div>
                  <div v-if="file.status === 'success' && file.url" class="flex items-center gap-2">
                    <button
                        @click="downloadFile(file.url!, file.name)"
                        class="text-blue-600 hover:text-blue-700 underline flex items-center gap-1 transition-colors bg-transparent border-none cursor-pointer"
                        :title="`下载 ${file.name}`"
                    >
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                              d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
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
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
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
                <button @click="removeFile(file)" class="p-1 rounded hover:bg-gray-200 transition-colors" title="删除文件">
                  <X class="w-3 h-3 text-gray-500" />
                </button>
              </div>
            </div>
          </div>
        </div>

        <div class="p-4 border-t border-gray-100 bg-gray-50 flex justify-end gap-3">
          <button @click="close"
                  class="px-4 py-2 text-sm font-medium text-gray-600 hover:bg-white hover:shadow-sm rounded-lg transition-all border border-transparent hover:border-gray-200">
            取消
          </button>
          <button @click="submit" :disabled="submitting"
                  class="px-6 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-lg shadow-md shadow-blue-200 transition-all active:scale-95">
            {{ isEditMode ? '重新提交' : '提交申请' }}
          </button>
        </div>

      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue';
import { X, BookOpen, Stethoscope, Briefcase, Users, Upload, FileText } from 'lucide-vue-next';
import { ElMessage } from 'element-plus';
import { uploadFile } from '@/api/upload';
import { createLeave, getLeaveDetail, resubmitLeave, type LeaveSubmitForm } from '@/api/leave';

interface UploadedFile {
  name: string;
  size: number;
  file: File;
  status: 'pending' | 'uploading' | 'success' | 'error';
  url?: string;
}

type CourseLike = {
  id: string;
  courseName: string;
  teacherName: string;
  dayOfWeek: number;
  section: number;
  duration: number;
  courseDate?: string; // Timetable.vue 会补齐
};

const props = defineProps<{
  modelValue: boolean;
  courses: CourseLike[];
  studentId?: number;
  termId?: number;
  courseDate?: string; // 兼容旧传参（不推荐）
  editLeaveId?: number; // 被退回后重新提交
}>();

const emit = defineEmits(['update:modelValue', 'submit']);

const submitting = ref(false);
const fileInput = ref<HTMLInputElement>();
const uploadedFiles = ref<UploadedFile[]>([]);

const isEditMode = computed(() => typeof props.editLeaveId === 'number' && props.editLeaveId > 0);

const dayMap = ['', '一', '二', '三', '四', '五', '六', '日'];

const leaveTypes = [
  { label: '病假', value: 'sick', icon: Stethoscope },
  { label: '事假', value: 'personal', icon: Briefcase },
  { label: '公假', value: 'public', icon: Users },
];

const form = reactive({
  type: 'sick',
  reason: '',
  proofUrls: [] as string[],
});

const resetForm = () => {
  form.type = 'sick';
  form.reason = '';
  form.proofUrls = [];
  uploadedFiles.value = [];
};

const close = () => emit('update:modelValue', false);

const triggerFileInput = () => fileInput.value?.click();

const handleFileSelect = (event: Event) => {
  const input = event.target as HTMLInputElement;
  if (input.files && input.files.length > 0) {
    processFiles(Array.from(input.files));
    input.value = '';
  }
};

const handleDrop = (event: DragEvent) => {
  if (event.dataTransfer?.files) processFiles(Array.from(event.dataTransfer.files));
};

const processFiles = (files: File[]) => {
  files.forEach((file) => {
    if (file.size > 5 * 1024 * 1024) {
      ElMessage.warning(`文件 ${file.name} 超过5MB限制`);
      return;
    }

    const allowedTypes = [
      'image/jpeg',
      'image/png',
      'application/pdf',
      'application/msword',
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    ];
    if (!allowedTypes.includes(file.type)) {
      ElMessage.warning(`文件 ${file.name} 类型不支持`);
      return;
    }

    const uploadedFile: UploadedFile = { name: file.name, size: file.size, file, status: 'pending' };
    uploadedFiles.value.push(uploadedFile);
    uploadFileToServer(uploadedFile);
  });
};

const uploadFileToServer = async (uploadedFile: UploadedFile) => {
  try {
    uploadedFile.status = 'uploading';
    const result: any = await uploadFile(uploadedFile.file);
    const url = result?.url;
    if (!url) throw new Error(result?.message || '上传成功但未返回url');

    let fileUrl = url;
    if (fileUrl && !fileUrl.startsWith('http')) {
      const baseUrl = import.meta.env.VITE_API_BASE_URL || '';
      fileUrl = baseUrl + (fileUrl.startsWith('/') ? fileUrl : '/' + fileUrl);
    }

    uploadedFile.status = 'success';
    uploadedFile.url = fileUrl;

    if (!form.proofUrls.includes(fileUrl)) form.proofUrls.push(fileUrl);

    ElMessage.success({ message: `文件 ${uploadedFile.name} 上传成功`, duration: 3000, showClose: true });
  } catch (e: any) {
    uploadedFile.status = 'error';
    ElMessage.error({ message: `文件 ${uploadedFile.name} 上传失败: ${e?.message || '未知错误'}`, duration: 3000, showClose: true });
    console.error('文件上传异常:', e);
  }
};

const downloadFile = async (fileUrl: string, fileName: string) => {
  try {
    const token = localStorage.getItem('token');

    const urlParts = fileUrl.split('?')[0].split('/');
    const serverFilename = decodeURIComponent(urlParts[urlParts.length - 1]);

    const baseUrl = import.meta.env.VITE_API_BASE_URL || '';
    const downloadUrl = `${baseUrl}/api/download/${serverFilename}`;

    const response = await fetch(downloadUrl, {
      method: 'GET',
      headers: token ? { Authorization: `Bearer ${token}` } : undefined,
    });

    if (!response.ok) throw new Error(`下载失败: ${response.status} ${response.statusText}`);

    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);

    const link = document.createElement('a');
    link.href = url;
    link.download = fileName || serverFilename || 'attachment';
    link.style.display = 'none';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    window.URL.revokeObjectURL(url);
  } catch (error) {
    console.error('下载文件失败:', error);
    ElMessage.error('文件下载失败，请重新登录或检查网络');
  }
};

const removeFile = (fileToRemove: UploadedFile) => {
  const index = uploadedFiles.value.findIndex((file) => file === fileToRemove);
  if (index !== -1) {
    uploadedFiles.value.splice(index, 1);
    if (fileToRemove.url) {
      const urlIndex = form.proofUrls.indexOf(fileToRemove.url);
      if (urlIndex !== -1) form.proofUrls.splice(urlIndex, 1);
    }
  }
};

const formatFileSize = (bytes: number): string => {
  if (!bytes) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

const resolveStudentId = () => {
  if (typeof props.studentId === 'number') return props.studentId;
  const v = localStorage.getItem('userId') || localStorage.getItem('studentId');
  if (v && !Number.isNaN(Number(v))) return Number(v);
  return null;
};

const resolveTermId = () => {
  if (typeof props.termId === 'number') return props.termId;
  const v = localStorage.getItem('termId');
  if (v && !Number.isNaN(Number(v))) return Number(v);
  return null;
};

const leaveTypeMap: Record<string, LeaveSubmitForm['leaveType']> = {
  sick: 'SICK',
  personal: 'PERSONAL',
  public: 'PUBLIC',
};

const loadEditDetail = async () => {
  if (!isEditMode.value || !props.editLeaveId) return;

  try {
    const dto: any = await getLeaveDetail(props.editLeaveId);

    const t = String(dto?.leaveType || '').toUpperCase();
    form.type = t === 'SICK' ? 'sick' : t === 'PERSONAL' ? 'personal' : 'public';
    form.reason = dto?.reason || '';

    const urls = (dto?.proofUrl ? String(dto.proofUrl) : '')
        .split(',')
        .map((s: string) => s.trim())
        .filter(Boolean);

    form.proofUrls = urls;
    uploadedFiles.value = urls.map((u: string) => {
      const name = (() => {
        try {
          const seg = u.split('?')[0].split('/').filter(Boolean);
          return decodeURIComponent(seg[seg.length - 1] || '附件');
        } catch {
          return '附件';
        }
      })();
      return { name, size: 0, file: new File([], name), status: 'success', url: u } as UploadedFile;
    });
  } catch (e) {
    console.error('加载请假单详情失败:', e);
    ElMessage.error('加载退回请假单失败');
  }
};

watch(
    () => props.modelValue,
    (open) => {
      if (!open) return;
      if (!isEditMode.value) resetForm();
      else loadEditDetail();
    }
);

const submit = async () => {
  if (!form.reason.trim()) {
    ElMessage.warning('请填写请假事由');
    return;
  }
  if (!props.courses || props.courses.length === 0) {
    ElMessage.warning('请先选择需要请假的课程');
    return;
  }
  if (uploadedFiles.value.some((f) => f.status === 'uploading')) {
    ElMessage.warning('请等待文件上传完成');
    return;
  }

  const studentId = resolveStudentId();
  const termId = resolveTermId();
  if (!studentId) {
    ElMessage.error('缺少 studentId：请重新登录');
    return;
  }
  if (!termId) {
    ElMessage.error('缺少 termId：请重新登录');
    return;
  }

  const impacts = props.courses.map((c) => {
    const cd = c.courseDate || props.courseDate;
    if (!cd) throw new Error('缺少 courseDate：请让 Timetable.vue 给 courses 补齐 courseDate');
    return {
      offeringId: Number(c.id),
      courseDate: cd,
      sectionStart: c.section,
      sectionEnd: c.section + c.duration - 1,
    };
  });

  const dates = impacts.map((it) => it.courseDate).sort();
  const startDate = dates[0];
  const endDate = dates[dates.length - 1];

  const payload: LeaveSubmitForm = {
    studentId,
    termId,
    leaveType: leaveTypeMap[form.type] ?? 'SICK',
    applyChannel: 'BY_COURSE',
    reason: form.reason,
    proofUrl: form.proofUrls.length ? form.proofUrls.join(',') : undefined,
    startTime: `${startDate}T00:00:00`,
    endTime: `${endDate}T23:59:59`,
    impacts,
  };

  try {
    submitting.value = true;

    if (isEditMode.value && props.editLeaveId) {
      await resubmitLeave(props.editLeaveId, payload);
      ElMessage.success('重新提交成功！');
    } else {
      await createLeave(payload);
      ElMessage.success('请假申请已提交！');
    }

    emit('submit', payload);
    close();
  } catch (e: any) {
    console.error('提交失败:', e);
    ElMessage.error(e?.message ?? '提交失败');
  } finally {
    submitting.value = false;
  }
};
</script>

<style scoped>
.animate-scale-in {
  animation: scale-in 0.2s cubic-bezier(0.16, 1, 0.3, 1);
}
@keyframes scale-in {
  from { opacity: 0; transform: scale(0.95); }
  to { opacity: 1; transform: scale(1); }
}
</style>
