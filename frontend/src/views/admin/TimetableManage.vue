<template>
  <div class="space-y-4">
    <div class="bg-white rounded-lg shadow-sm p-4 border border-gray-200 flex flex-col md:flex-row justify-between items-center gap-4">
      <div>
        <h2 class="text-lg font-bold text-gray-800">学期课表管理</h2>
        <p class="text-xs text-gray-500 mt-1">
          当前状态：
          <span class="text-green-600 font-bold bg-green-50 px-2 py-0.5 rounded">
            {{ statusText }}
          </span>
        </p>
      </div>

      <div class="flex items-center gap-3">
        <div class="flex items-center gap-2 mr-4">
          <span class="text-sm text-gray-600">预览班级：</span>
          <select
            v-model="selectedClassId"
            class="text-sm border-gray-300 rounded-md shadow-sm focus:border-blue-500 focus:ring-blue-500"
          >
            <option v-for="c in classes" :key="c.classId" :value="c.classId">
              {{ c.className || c.classCode }}
            </option>
          </select>
        </div>

        <input
          ref="fileInput"
          type="file"
          class="hidden"
          accept=".xlsx,.xls"
          @change="onFileChange"
        />

        <button
          @click="triggerPickFile"
          class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm font-medium shadow-sm flex items-center gap-2 hover:bg-blue-700"
        >
          <UploadCloud class="w-4 h-4" /> 导入新课表
        </button>
      </div>
    </div>

    <div class="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
      <div class="overflow-auto">
        <table class="min-w-full border-collapse">
          <thead class="bg-gray-50 sticky top-0 z-10">
            <tr>
              <th class="w-14 p-2 text-xs font-medium text-gray-500 border-r border-gray-200">节次</th>
              <th v-for="d in 7" :key="d" class="p-2 text-xs font-medium text-gray-500 border-r border-gray-200">
                周{{ '一二三四五六日'[d-1] }}
              </th>
            </tr>
          </thead>

          <tbody>
            <tr v-for="sectionNum in 12" :key="sectionNum" class="border-t border-gray-100">
              <td class="w-14 p-2 text-[10px] text-gray-500 border-r border-gray-200 align-top">
                <div class="font-bold">{{ sectionNum }}</div>
                <div>{{ timeSlots[sectionNum-1] }}</div>
              </td>

              <template v-for="dayIdx in 7" :key="dayIdx">
                <td
                  v-if="getCell(dayIdx, sectionNum)"
                  :rowspan="getCell(dayIdx, sectionNum)!.duration"
                  class="p-1 border-r border-gray-100 align-top bg-white"
                >
                  <div class="w-full h-full rounded-lg p-2 bg-blue-50 border-l-4 border-blue-500">
                    <div class="font-bold text-xs text-gray-800">
                      {{ getCell(dayIdx, sectionNum)!.name }}
                    </div>
                    <div class="text-[10px] text-gray-500 mt-1">
                      {{ getCell(dayIdx, sectionNum)!.teacher }}
                    </div>
                    <div class="text-[10px] text-gray-500">
                      {{ getCell(dayIdx, sectionNum)!.room }}
                    </div>
                  </div>
                </td>

                <td v-else-if="!isOccupied(dayIdx, sectionNum)" class="h-16 border-r border-gray-100"></td>
              </template>
            </tr>
          </tbody>

        </table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { UploadCloud } from 'lucide-vue-next';
import { ElMessage, ElLoading } from 'element-plus';
import { computed, onMounted, ref, watch } from 'vue';
import {
  listTerms, listClasses, listCourses,
  listOfferingsByTermClass, deleteOffering,
  createCourse, createOffering,
  parseTimetableFile,
  type Clazz, type Term, type TimetableRow
} from '@/api/admin';

const timeSlots = ['08:00','08:50','09:50','10:40','11:30','13:00','13:50','14:50','15:40','16:40','18:30','19:20'];

const fileInput = ref<HTMLInputElement | null>(null);

const terms = ref<Term[]>([]);
const classes = ref<Clazz[]>([]);
const selectedClassId = ref<number | null>(null);

const currentTermId = computed(() => {
  const cur = terms.value.find(t => t.isCurrent) || terms.value[0];
  return cur?.termId ?? null;
});

const courseIdToName = ref(new Map<number, string>());

// 后端 offerings -> 表格 cell
const cells = ref<Array<{ day:number; start:number; duration:number; name:string; teacher:string; room:string }>>([]);

const statusText = computed(() => (cells.value.length > 0 ? '已发布' : '未发布'));

const refreshCourses = async () => {
  const list = await listCourses();
  const m = new Map<number, string>();
  list.forEach(c => m.set(c.courseId, c.courseName));
  courseIdToName.value = m;
};

const refreshOfferings = async () => {
  if (!currentTermId.value || !selectedClassId.value) return;
  const offs = await listOfferingsByTermClass(currentTermId.value, selectedClassId.value);

  // 显示：课程名能映射就用课程名；教师名后端没给，只能先显示教师ID
  cells.value = offs.map(o => ({
    day: o.weekDay,
    start: o.sectionStart,
    duration: o.sectionEnd - o.sectionStart + 1,
    name: courseIdToName.value.get(o.courseId) ?? `课程ID:${o.courseId}`,
    teacher: `教师ID:${o.teacherId}`,
    room: o.classroom || '待定'
  }));
};

onMounted(async () => {
  try {
    terms.value = await listTerms();
    classes.value = await listClasses();

    if (!selectedClassId.value && classes.value.length > 0) {
      selectedClassId.value = classes.value[0].classId;
    }

    await refreshCourses();
    await refreshOfferings();
  } catch (e: any) {
    ElMessage.error(e?.message || '初始化失败，请检查是否以管理员身份登录、以及后端是否可达');
  }
});

watch([currentTermId, selectedClassId], async () => {
  try {
    await refreshOfferings();
  } catch {}
});

/** ===== 表格渲染逻辑（与你原来的 mockData 逻辑一致，只换数据源） ===== */
const getCell = (day: number, section: number) => {
  return cells.value.find(c => c.day === day && c.start === section) || null;
};

const isOccupied = (day: number, section: number) => {
  return cells.value.some(c => c.day === day && section > c.start && section < c.start + c.duration);
};

/** ===== 导入逻辑：选 Excel -> 解析 -> 写入后端 offerings ===== */
const triggerPickFile = () => {
  if (!currentTermId.value) return ElMessage.error('没有可用学期，请先到“系统设置”保存一个学期并设为当前');
  if (!selectedClassId.value) return ElMessage.error('请先选择班级');
  fileInput.value?.click();
};

const onFileChange = async (ev: Event) => {
  const input = ev.target as HTMLInputElement;
  const file = input.files?.[0];
  input.value = ''; // 允许重复选择同一文件
  if (!file) return;

  if (!currentTermId.value || !selectedClassId.value) return;

  const loading = ElLoading.service({
    lock: true,
    text: '正在解析并导入课表...',
    background: 'rgba(0, 0, 0, 0.7)',
  });

  try {
    // 1) 解析 Excel
    const { result, rows } = await parseTimetableFile(file);
    if (result.warnings.length) {
      // 不拦截导入，但提醒你 Excel 哪些行有问题
      ElMessage.warning(`解析警告（前3条）：${result.warnings.slice(0,3).join('；')}`);
    }
    if (rows.length === 0) {
      ElMessage.error('没有解析出任何有效行，请检查 Excel 列名/数据');
      return;
    }

    // 2) 准备 classCode -> classId（如果 Excel 没写 classCode，就默认导入到当前选中班级）
    const classCodeToId = new Map<string, number>();
    classes.value.forEach(c => classCodeToId.set(c.classCode, c.classId));

    const targetClassId = selectedClassId.value;
    const normalized: Array<TimetableRow & { classId: number }> = [];
    for (const r of rows) {
      let classId = targetClassId;
      if (r.classCode) {
        const mapped = classCodeToId.get(r.classCode);
        if (!mapped) continue; // Excel 写了 classCode 但后端不存在该班级：直接跳过
        classId = mapped;
      }
      normalized.push({ ...r, classId });
    }

    if (normalized.length === 0) {
      ElMessage.error('Excel 里的班级代码在后端都匹配不上（或都被过滤掉了）');
      return;
    }

    // 3) 确保课程存在：不存在就创建（credit/totalHours 传 0）
    const courses = await listCourses();
    const courseCodeToId = new Map<string, number>();
    courses.forEach(c => courseCodeToId.set(c.courseCode, c.courseId));

    const uniqCourse = Array.from(new Set(normalized.map(r => r.courseCode)));
    for (const code of uniqCourse) {
      if (courseCodeToId.has(code)) continue;
      const sample = normalized.find(r => r.courseCode === code)!;
      const created = await createCourse({
        courseCode: sample.courseCode,
        courseName: sample.courseName,
        credit: 0,
        totalHours: 0
      });
      courseCodeToId.set(created.courseCode, created.courseId);
    }

    // 4) 为避免重复：先把该 学期+班级 现有 offerings 删掉，再导入新的
    // 注意：如果你们数据库里已经有选课(enrollment)引用了 offering，删除可能失败
    const existing = await listOfferingsByTermClass(currentTermId.value, targetClassId);
    for (const o of existing) {
      await deleteOffering(o.offeringId);
    }

    // 5) 写入 offerings
    for (const r of normalized) {
      const courseId = courseCodeToId.get(r.courseCode);
      if (!courseId) continue;

      await createOffering({
        termId: currentTermId.value,
        classId: r.classId,
        courseId,
        teacherId: r.teacherId,
        weekDay: r.weekDay,
        sectionStart: r.sectionStart,
        sectionEnd: r.sectionEnd,
        classroom: r.classroom || null
      });
    }

    // 6) 刷新显示
    await refreshCourses();
    await refreshOfferings();

    ElMessage.success(`导入成功：课程${result.totalCourses}门，教师${result.totalTeachers}人，有效记录${normalized.length}条`);
  } catch (e: any) {
    ElMessage.error(e?.message || '导入失败');
  } finally {
    loading.close();
  }
};
</script>
