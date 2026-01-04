<template>
  <div class="max-w-4xl mx-auto space-y-6">
    <div class="bg-gradient-to-r from-blue-50 to-indigo-50 border border-blue-200 rounded-lg p-6 flex items-start gap-4">
      <div class="p-3 bg-blue-100 rounded-lg shrink-0">
        <Info class="w-6 h-6 text-blue-600" />
      </div>
      <div>
        <h3 class="text-blue-900 mb-2 font-bold text-lg">公假申请说明</h3>
        <p class="text-sm text-blue-800 leading-relaxed">
          公假用于学生参加学院组织的活动、竞赛、考试等集体事务。公假由辅导员统一发起，无需学生个人申请。
          发起后将自动通知相关任课教师，并标记为"公假"状态。
        </p>
      </div>
    </div>

    <div class="bg-white rounded-lg shadow-md border border-gray-200 overflow-hidden">
      <div class="px-8 py-6 border-b border-gray-200 bg-gradient-to-r from-gray-50 to-white">
        <h2 class="mb-1 text-xl font-bold text-gray-900">发起公假申请</h2>
        <p class="text-sm text-gray-600">请填写以下信息完成公假申请</p>
      </div>

      <div class="px-8 py-6 space-y-8">
        
        <div>
          <div class="flex items-center gap-3 mb-6 pb-3 border-b border-gray-200">
            <div class="w-1 h-6 bg-blue-600 rounded-full"></div>
            <h3 class="text-gray-900 font-bold">公假基本信息</h3>
          </div>
          <div class="space-y-5">
            <div class="grid grid-cols-2 gap-5">
              <div>
                <label class="block text-sm text-gray-700 mb-2 font-medium"><span class="text-red-500">*</span> 公假名称</label>
                <input type="text" v-model="form.name" placeholder="如：数学建模竞赛培训、校运动会" class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm" />
              </div>
              <div>
                <label class="block text-sm text-gray-700 mb-2 font-medium"><span class="text-red-500">*</span> 公假类型</label>
                <select v-model="form.type" class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-white text-sm">
                  <option value="">请选择公假类型</option>
                  <option value="竞赛">竞赛</option>
                  <option value="活动">活动</option>
                  <option value="考试">考试</option>
                  <option value="实习">实习</option>
                </select>
              </div>
            </div>

            <div class="grid grid-cols-2 gap-5">
              <div>
                <label class="block text-sm text-gray-700 mb-2 font-medium"><span class="text-red-500">*</span> 开始时间</label>
                <div class="relative">
                  <Calendar class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400 pointer-events-none" />
                  <input type="datetime-local" v-model="form.startTime" class="w-full pl-11 pr-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm" />
                </div>
              </div>
              <div>
                <label class="block text-sm text-gray-700 mb-2 font-medium"><span class="text-red-500">*</span> 结束时间</label>
                <div class="relative">
                  <Calendar class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400 pointer-events-none" />
                  <input type="datetime-local" v-model="form.endTime" class="w-full pl-11 pr-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm" />
                </div>
              </div>
            </div>

            <div>
              <label class="block text-sm text-gray-700 mb-2 font-medium"><span class="text-red-500">*</span> 公假说明</label>
              <div class="relative">
                <FileText class="absolute left-3 top-3 w-5 h-5 text-gray-400 pointer-events-none" />
                <textarea v-model="form.description" placeholder="请简要说明公假原因和学校批准情况..." rows="4" class="w-full pl-11 pr-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none text-sm"></textarea>
              </div>
            </div>
          </div>
        </div>

        <div>
          <div class="flex items-center gap-3 mb-6 pb-3 border-b border-gray-200">
            <div class="w-1 h-6 bg-blue-600 rounded-full"></div>
            <h3 class="text-gray-900 font-bold">适用对象</h3>
          </div>
          <div class="space-y-5">
            <div>
              <label class="block text-sm text-gray-700 mb-3 font-medium">选择方式</label>
              <div class="flex items-center gap-4">
                <label :class="['flex items-center gap-2 cursor-pointer px-4 py-3 border-2 rounded-lg transition-all hover:border-blue-300', selectionMode === 'class' ? 'border-blue-500 bg-blue-50' : 'border-gray-200']">
                  <input type="radio" value="class" v-model="selectionMode" class="w-4 h-4 text-blue-600" />
                  <span class="text-sm font-medium">按班级选择</span>
                </label>
                <label :class="['flex items-center gap-2 cursor-pointer px-4 py-3 border-2 rounded-lg transition-all hover:border-blue-300', selectionMode === 'student' ? 'border-blue-500 bg-blue-50' : 'border-gray-200']">
                  <input type="radio" value="student" v-model="selectionMode" class="w-4 h-4 text-blue-600" />
                  <span class="text-sm font-medium">按学生名单</span>
                </label>
              </div>
            </div>

            <div v-if="selectionMode === 'class'">
              <label class="block text-sm text-gray-700 mb-3 font-medium"><span class="text-red-500">*</span> 选择班级</label>
              <div class="grid grid-cols-3 gap-3">
                <label v-for="cls in classes" :key="cls" :class="['flex items-center justify-between px-4 py-3 border-2 rounded-lg cursor-pointer transition-all', selectedClasses.includes(cls) ? 'border-blue-500 bg-blue-50' : 'border-gray-200 hover:border-blue-300']">
                  <span class="text-sm font-medium">{{ cls }}</span>
                  <input type="checkbox" :value="cls" v-model="selectedClasses" class="w-4 h-4 text-blue-600 rounded" />
                </label>
              </div>
            </div>

            <div v-else>
              <label class="block text-sm text-gray-700 mb-3 font-medium"><span class="text-red-500">*</span> 选择学生</label>
              <button class="w-full px-4 py-3 border-2 border-dashed border-gray-300 rounded-lg hover:border-blue-400 transition-colors flex items-center justify-center gap-2 text-gray-600 group">
                <Users class="w-5 h-5 group-hover:text-blue-500" />
                <span class="font-medium group-hover:text-blue-500">点击选择学生（已选 {{ selectedStudents.length }} 人）</span>
              </button>
            </div>
            
            <div v-if="selectedClasses.length > 0 || selectedStudents.length > 0" class="p-4 bg-blue-50 border border-blue-200 rounded-lg flex items-center gap-3">
              <Check class="w-5 h-5 text-blue-600" />
              <div class="flex-1">
                <div class="text-sm text-blue-900 font-medium">
                  {{ selectionMode === 'class' ? `已选择 ${selectedClasses.length} 个班级` : `已选择 ${selectedStudents.length} 名学生` }}
                </div>
                <div class="text-sm text-blue-700 mt-1">共 {{ estimatedCount }} 名学生将收到公假通知</div>
              </div>
            </div>
          </div>
        </div>

        <div>
          <div class="flex items-center gap-3 mb-6 pb-3 border-b border-gray-200">
            <div class="w-1 h-6 bg-blue-600 rounded-full"></div>
            <h3 class="text-gray-900 font-bold">关联课程（可选）</h3>
          </div>
          <div class="space-y-4">
            <div class="p-4 bg-gray-50 border border-gray-200 rounded-lg text-sm text-gray-700">
              选中本次公假涉及的课程，方便任课老师确认考勤。未选择则根据时间自动匹配课程。
            </div>
            <div class="border border-gray-200 rounded-lg overflow-hidden">
              <table class="w-full">
                <thead class="bg-gray-50 border-b border-gray-200">
                  <tr>
                    <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">课程名称</th>
                    <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">任课老师</th>
                    <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">上课时间</th>
                    <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">上课地点</th>
                    <th class="px-4 py-3 text-center text-sm font-medium text-gray-600 w-24">关联</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-gray-200">
                  <tr v-for="course in courses" :key="course.id" :class="['hover:bg-gray-50 transition-colors', course.selected ? 'bg-blue-50/50' : '']">
                    <td class="px-4 py-3 text-sm font-medium text-gray-900">{{ course.name }}</td>
                    <td class="px-4 py-3 text-sm text-gray-600">{{ course.teacher }}</td>
                    <td class="px-4 py-3 text-sm text-gray-600">{{ course.time }}</td>
                    <td class="px-4 py-3 text-sm text-gray-600">{{ course.location }}</td>
                    <td class="px-4 py-3 text-center">
                      <input type="checkbox" v-model="course.selected" class="w-4 h-4 text-blue-600 rounded focus:ring-blue-500" />
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

      </div>

      <div class="px-8 py-6 bg-gray-50 border-t border-gray-200 flex items-center justify-between rounded-b-lg">
        <button class="text-sm text-gray-600 hover:text-gray-900 transition-colors font-medium">← 返回列表</button>
        <div class="flex items-center gap-3">
          <button @click="saveDraft" class="px-6 py-2.5 border border-gray-300 rounded-lg hover:bg-white transition-colors text-sm font-medium text-gray-700">保存草稿</button>
          <button @click="clearDraft" class="px-6 py-2.5 border border-gray-300 rounded-lg hover:bg-white transition-colors text-sm font-medium text-gray-700">清空草稿</button>
          <button @click="handleSubmit" class="px-8 py-2.5 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors shadow-sm flex items-center gap-2 text-sm font-medium">
            <Check class="w-5 h-5" /> 提交公假申请
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { Calendar, Users, FileText, Info, Check } from 'lucide-vue-next';
import { ElMessage ,ElMessageBox} from 'element-plus';

// 草稿数据接口
interface DraftData {
  form: {
    name: string;
    type: string;
    startTime: string;
    endTime: string;
    description: string;
  };
  selectionMode: 'class' | 'student';
  selectedClasses: string[];
  selectedStudents: string[];
  courses: Array<{
    id: string;
    name: string;
    teacher: string;
    time: string;
    location: string;
    selected: boolean;
  }>;
}

const form = reactive({
  name: '',
  type: '',
  startTime: '',
  endTime: '',
  description: ''
});

const selectionMode = ref<'class' | 'student'>('class');
const selectedClasses = ref<string[]>([]);
const selectedStudents = ref<string[]>([]);
const classes = ['计算机2101', '计算机2102', '计算机2103'];

const courses = ref([
  { id: '1', name: '高等数学', teacher: '张教授', time: '2024-09-06 08:00-09:40', location: 'A101', selected: false },
  { id: '2', name: '大学英语', teacher: '李老师', time: '2024-09-06 10:00-11:40', location: 'B203', selected: false },
  { id: '3', name: '大学物理', teacher: '陈教授', time: '2024-09-06 14:00-15:40', location: 'A305', selected: false },
]);

const estimatedCount = computed(() => {
  return selectionMode.value === 'class' ? selectedClasses.value.length * 30 : selectedStudents.value.length;
});

// 保存草稿到localStorage
const saveDraft = () => {
  const draftData: DraftData = {
    form: { ...form },
    selectionMode: selectionMode.value,
    selectedClasses: [...selectedClasses.value],
    selectedStudents: [...selectedStudents.value],
    courses: courses.value.map(course => ({ ...course }))
  };

  localStorage.setItem('counselor_public_leave_draft', JSON.stringify(draftData));
  ElMessage.success('草稿已保存');
};

// 清空草稿
const clearDraft = async () => {
  try {
    await ElMessageBox.confirm('确定要清空当前草稿吗？清空后无法恢复。', '确认清空', {
      confirmButtonText: '确定清空',
      cancelButtonText: '取消',
      type: 'warning'
    });

    localStorage.removeItem('counselor_public_leave_draft');
    resetForm();
    ElMessage.success('草稿已清空');
  } catch {
    // 用户取消操作
  }
};

// 加载草稿
const loadDraft = () => {
  const draft = localStorage.getItem('counselor_public_leave_draft');
  if (draft) {
    try {
      const draftData: DraftData = JSON.parse(draft);

      // 恢复表单数据
      Object.assign(form, draftData.form);
      selectionMode.value = draftData.selectionMode;
      selectedClasses.value = draftData.selectedClasses;
      selectedStudents.value = draftData.selectedStudents;

      // 恢复课程选择状态
      if (draftData.courses) {
        courses.value.forEach(course => {
          const draftCourse = draftData.courses.find(c => c.id === course.id);
          if (draftCourse) {
            course.selected = draftCourse.selected;
          }
        });
      }

      ElMessage.info('已恢复上次保存的草稿');
    } catch (error) {
      console.error('加载草稿失败:', error);
      localStorage.removeItem('counselor_public_leave_draft');
    }
  }
};

// 重置表单
const resetForm = () => {
  form.name = '';
  form.type = '';
  form.startTime = '';
  form.endTime = '';
  form.description = '';
  selectionMode.value = 'class';
  selectedClasses.value = [];
  selectedStudents.value = [];
  courses.value.forEach(course => course.selected = false);
};

const handleSubmit = () => {
  if (!form.name || !form.type || !form.startTime || !form.endTime) {
    ElMessage.warning('请填写完整的公假信息');
    return;
  }
  // 提交成功后清除草稿
  localStorage.removeItem('counselor_public_leave_draft');
  ElMessage.success(`已为 ${estimatedCount.value} 名学生发起公假`);
};

// 页面加载时自动恢复草稿
onMounted(() => {
  loadDraft();
});

</script>