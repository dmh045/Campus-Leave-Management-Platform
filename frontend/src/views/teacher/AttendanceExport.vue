<template>
  <div class="space-y-6">
    <!-- 页面标题和说明 -->
    <div class="bg-gradient-to-r from-blue-50 to-indigo-50 border border-blue-200 rounded-lg p-6 flex items-start gap-4">
      <div class="p-3 bg-blue-100 rounded-lg">
        <Download class="w-6 h-6 text-blue-600" />
      </div>
      <div>
        <h3 class="text-blue-900 mb-2 font-bold">导出考勤表说明</h3>
        <p class="text-sm text-blue-800 leading-relaxed">
          选择课程和时间范围，导出学生考勤记录表格。支持导出为Excel格式，包含学生基本信息和考勤统计数据。
        </p>
      </div>
    </div>

    <!-- 筛选条件 -->
    <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
      <div class="px-6 py-4 border-b border-gray-100 bg-gray-50/50">
        <h2 class="font-bold text-gray-800 text-lg">导出筛选条件</h2>
      </div>
      <div class="p-6 space-y-4">
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <!-- 课程选择 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">选择课程</label>
            <select
                v-model.number="selectedCourseId"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            >
              <option value="">全部课程</option>
              <option v-for="course in uniqueCourses" :key="course.courseId" :value="course.courseId">
                {{ course.courseName }}
              </option>
            </select>
          </div>

          <!-- 班级选择 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">选择班级</label>
            <select
                v-model.number="selectedClassId"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            >
              <option value="">全部班级</option>
              <option v-for="c in classes" :key="c.classId" :value="c.classId">
                {{ c.className }}
              </option>
            </select>
          </div>

          <!-- 开始日期 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">开始日期</label>
            <input
                type="date"
                v-model="startDate"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>

          <!-- 结束日期 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">结束日期</label>
            <input
                type="date"
                v-model="endDate"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>
        </div>

        <!-- 满分 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">满分分值</label>
          <input
              type="number"
              v-model.number="fullScore"
              min="0"
              step="0.5"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="默认10分"
          />
        </div>

        <!-- 请假扣分 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">请假扣分分值</label>
          <input
              type="number"
              v-model.number="leaveScore"
              min="0"
              step="0.5"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="默认0分"
          />
        </div>

        <!-- 旷课扣分 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">旷课扣分分值</label>
          <input
              type="number"
              v-model.number="absentScore"
              min="0"
              step="0.5"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="默认0分"
          />
        </div>

        <!-- 导出类型选择 -->
        <div class="flex items-center gap-4 mb-4">
          <span class="text-sm font-medium text-gray-700">导出类型：</span>
          <el-radio-group v-model="exportType">
            <el-radio label="attendance">考勤表</el-radio>
            <el-radio label="leave">请假表</el-radio>
          </el-radio-group>
        </div>

        <!-- 导出按钮 -->
        <div class="flex justify-end">
          <button
              @click="exportAttendance"
              class="flex items-center gap-2 px-6 py-2.5 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-sm font-medium shadow-sm transition-all disabled:opacity-50 disabled:cursor-not-allowed"
              :disabled="loading"
          >
            <Download class="w-4 h-4" />
            {{ loading ? '导出中...' : '导出考勤表' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 考勤数据预览 -->
    <div v-if="attendanceData.length > 0" class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
      <div class="px-6 py-4 border-b border-gray-100 bg-gray-50/50">
        <h2 class="font-bold text-gray-800 text-lg">考勤数据预览</h2>
      </div>
      <div class="overflow-x-auto">
        <table class="w-full">
          <thead>
          <tr class="bg-gray-50 border-b border-gray-200">
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">学号</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">姓名</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">课程</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">班级</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">日期</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">节次</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">考勤状态</th>
          </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
          <tr v-for="item in attendanceData" :key="item.id">
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ item.studentId }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ item.studentName }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ item.courseName }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ item.className }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ item.date }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ item.classTime }}</td>
            <td class="px-6 py-4 whitespace-nowrap">
                <span
                    :class="[
                    'px-3 py-1 rounded-full text-xs font-medium',
                    item.status === 'present' ? 'bg-green-100 text-green-700' :
                    item.status === 'leave' ? 'bg-orange-100 text-orange-700' :
                    'bg-red-100 text-red-700'
                  ]"
                >
                  {{ item.status === 'present' ? '已签到' : item.status === 'leave' ? '请假' : '缺勤' }}
                </span>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 无数据提示 -->
    <div v-else-if="!loading" class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
      <div class="p-12 text-center text-gray-400">
        <FileText class="w-12 h-12 mx-auto mb-4 text-gray-300" />
        <p>暂无考勤数据，请选择筛选条件后重试</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import { exportClassLeaveDetails } from '@/api/stats';
import { Download, FileText } from 'lucide-vue-next';
import { getTeacherCourses, exportTeacherAttendance } from '@/api/teacher';

// 数据状态
const loading = ref(false);
const courses = ref<any[]>([]);
const attendanceData = ref<any[]>([]);
const exportType = ref<'attendance' | 'leave'>('attendance');

// 筛选条件（关键：ID 用 number）
const selectedCourseId = ref<number | ''>('');
const selectedClassId = ref<number | ''>('');
const startDate = ref('');
const endDate = ref('');
const fullScore = ref<number | null>(10);
const leaveScore = ref<number | null>(null);
const absentScore = ref<number | null>(null);

// 计算属性：提取唯一课程列表
const uniqueCourses = computed(() => {
  const courseMap = new Map<number, any>();
  courses.value.forEach((course) => {
    const cid = Number(course.courseId);
    if (cid && !courseMap.has(cid)) {
      courseMap.set(cid, course);
    }
  });
  return Array.from(courseMap.values());
});

// 计算属性：根据选中课程过滤班级列表（返回 {classId,className}）
const classes = computed(() => {
  const m = new Map<number, string>();

  courses.value.forEach((course) => {
    const courseId = Number(course.courseId);
    const classId = Number(course.classId);
    const className = String(course.className || '');

    if (!classId || !className) return;

    if (selectedCourseId.value !== '' && courseId !== selectedCourseId.value) return;

    m.set(classId, className);
  });

  return Array.from(m, ([classId, className]) => ({ classId, className }));
});

const selectedClassName = computed(() => {
  if (selectedClassId.value === '') return '';
  return classes.value.find((x) => x.classId === selectedClassId.value)?.className || '';
});

// 计算本学期开始日期
const getSemesterStartDate = () => {
  const now = new Date();
  const currentMonth = now.getMonth() + 1;
  const currentYear = now.getFullYear();

  if (currentMonth >= 9 && currentMonth <= 12) {
    return `${currentYear}-09-01`;
  } else {
    return `${currentYear - 1}-09-01`;
  }
};

// 计算当前日期
const getCurrentDate = () => {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

// 监听课程选择变化，重置班级选择
watch(selectedCourseId, () => {
  selectedClassId.value = '';
});

// 初始化页面
onMounted(() => {
  startDate.value = getSemesterStartDate();
  endDate.value = getCurrentDate();
  loadCourses();
});

// 加载教师课程
const loadCourses = async () => {
  try {
    const response = await getTeacherCourses();
    courses.value = response || [];
  } catch (error) {
    console.error('加载课程失败:', error);
  }
};

// 导出
const exportAttendance = async () => {
  if (!startDate.value || !endDate.value) {
    alert('请选择完整的时间范围');
    return;
  }

  // 请假表一般必须选班级（避免传空/NaN）
  if (exportType.value === 'leave' && selectedClassId.value === '') {
    alert('请先选择班级');
    return;
  }

  try {
    loading.value = true;

    if (exportType.value === 'attendance') {
      const response = await exportTeacherAttendance({
        courseId: selectedCourseId.value === '' ? undefined : selectedCourseId.value,
        classId: selectedClassId.value === '' ? undefined : selectedClassId.value,
        startDate: startDate.value,
        endDate: endDate.value,
        fullScore: fullScore.value ?? undefined,
        leaveScore: leaveScore.value ?? undefined,
        absentScore: absentScore.value ?? undefined
      });

      const blob = new Blob([response], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;

      const fileName = selectedClassId.value !== ''
          ? `考勤统计表_${selectedClassName.value}_${startDate.value}_${endDate.value}.xlsx`
          : `考勤统计表_${startDate.value}_${endDate.value}.xlsx`;

      link.download = fileName;
      link.click();
      window.URL.revokeObjectURL(url);
      alert('考勤表导出成功');
    } else {
      const response = await exportClassLeaveDetails({
        classId: selectedClassId.value as number,
        startDate: startDate.value,
        endDate: endDate.value
      });

      const blob = new Blob([response], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;

      const fileName = `班级请假表_${selectedClassName.value}_${startDate.value}_${endDate.value}.xlsx`;

      link.download = fileName;
      link.click();
      window.URL.revokeObjectURL(url);
      alert('班级请假表导出成功');
    }
  } catch (error) {
    console.error('导出失败:', error);
    alert('导出失败，请重试');
  } finally {
    loading.value = false;
  }
};
</script>
