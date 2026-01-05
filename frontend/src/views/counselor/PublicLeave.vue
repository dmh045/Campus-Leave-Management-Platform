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

        <!-- 公假基本信息 -->
        <div>
          <div class="flex items-center gap-3 mb-6 pb-3 border-b border-gray-200">
            <div class="w-1 h-6 bg-blue-600 rounded-full"></div>
            <h3 class="text-gray-900 font-bold">公假基本信息</h3>
          </div>

          <div class="space-y-5">
            <div class="grid grid-cols-2 gap-5">
              <div>
                <label class="block text-sm text-gray-700 mb-2 font-medium"><span class="text-red-500">*</span> 公假名称</label>
                <input
                    type="text"
                    v-model="form.name"
                    placeholder="如：数学建模竞赛培训、校运动会"
                    class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm"
                />
              </div>

              <div>
                <label class="block text-sm text-gray-700 mb-2 font-medium"><span class="text-red-500">*</span> 公假类型</label>
                <select
                    v-model="form.type"
                    class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-white text-sm"
                >
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
                  <input
                      type="datetime-local"
                      v-model="form.startTime"
                      class="w-full pl-11 pr-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm"
                  />
                </div>
              </div>

              <div>
                <label class="block text-sm text-gray-700 mb-2 font-medium"><span class="text-red-500">*</span> 结束时间</label>
                <div class="relative">
                  <Calendar class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400 pointer-events-none" />
                  <input
                      type="datetime-local"
                      v-model="form.endTime"
                      class="w-full pl-11 pr-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm"
                  />
                </div>
              </div>
            </div>

            <div>
              <label class="block text-sm text-gray-700 mb-2 font-medium"><span class="text-red-500">*</span> 公假说明</label>
              <div class="relative">
                <FileText class="absolute left-3 top-3 w-5 h-5 text-gray-400 pointer-events-none" />
                <textarea
                    v-model="form.description"
                    placeholder="请简要说明公假原因和学校批准情况..."
                    rows="4"
                    class="w-full pl-11 pr-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none text-sm"
                ></textarea>
              </div>
            </div>
          </div>
        </div>

        <!-- 适用对象 -->
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

            <!-- 班级选择 -->
            <div v-if="selectionMode === 'class'">
              <label class="block text-sm text-gray-700 mb-3 font-medium">
                <span class="text-red-500">*</span> 选择班级
              </label>

              <div v-if="loadingClasses" class="text-sm text-gray-500">正在加载班级…</div>

              <div v-else class="grid grid-cols-3 gap-3">
                <label
                    v-for="cls in classes"
                    :key="cls.classId"
                    :class="[
                    'flex items-center justify-between px-4 py-3 border-2 rounded-lg cursor-pointer transition-all',
                    selectedClassIds.includes(cls.classId) ? 'border-blue-500 bg-blue-50' : 'border-gray-200 hover:border-blue-300'
                  ]"
                >
                  <span class="text-sm font-medium">{{ cls.className }}</span>
                  <input type="checkbox" :value="cls.classId" v-model="selectedClassIds" class="w-4 h-4 text-blue-600 rounded" />
                </label>
              </div>

              <div v-if="selectedClassIds.length > 1" class="mt-3 text-xs text-amber-700 bg-amber-50 border border-amber-200 rounded-lg p-3">
                已选择多个班级：不同班级课表可能不同。若不勾选“关联课程”，系统将按时间区间自动匹配各班课表（后端自动匹配）。
              </div>
            </div>

            <!-- 学生名单选择 -->
            <div v-else>
              <label class="block text-sm text-gray-700 mb-3 font-medium"><span class="text-red-500">*</span> 选择学生</label>
              <button
                  @click="openStudentDialog"
                  class="w-full px-4 py-3 border-2 border-dashed border-gray-300 rounded-lg hover:border-blue-400 transition-colors flex items-center justify-center gap-2 text-gray-600 group"
              >
                <Users class="w-5 h-5 group-hover:text-blue-500" />
                <span class="font-medium group-hover:text-blue-500">点击选择学生（已选 {{ selectedStudents.length }} 人）</span>
              </button>

              <div v-if="selectedStudents.length" class="mt-3 text-xs text-gray-600">
                已选：<span class="font-medium">{{ selectedStudents.slice(0, 6).map(s => s.name).join('、') }}</span>
                <span v-if="selectedStudents.length > 6"> …</span>
              </div>
            </div>

            <div v-if="estimatedCount > 0" class="p-4 bg-blue-50 border border-blue-200 rounded-lg flex items-center gap-3">
              <Check class="w-5 h-5 text-blue-600" />
              <div class="flex-1">
                <div class="text-sm text-blue-900 font-medium">
                  {{ selectionMode === 'class' ? `已选择 ${selectedClassIds.length} 个班级` : `已选择 ${selectedStudents.length} 名学生` }}
                </div>
                <div class="text-sm text-blue-700 mt-1">共 {{ estimatedCount }} 名学生将收到公假通知</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 关联课程 -->
        <div>
          <div class="flex items-center gap-3 mb-6 pb-3 border-b border-gray-200">
            <div class="w-1 h-6 bg-blue-600 rounded-full"></div>
            <h3 class="text-gray-900 font-bold">关联课程（可选）</h3>
          </div>

          <div class="space-y-4">
            <div class="p-4 bg-gray-50 border border-gray-200 rounded-lg text-sm text-gray-700">
              这里展示的是数据库里的真实开课信息（Offering）。如果不勾选任何课程，将由后端按时间区间自动匹配受影响课程节次。
            </div>

            <div v-if="!primaryClassId" class="text-sm text-gray-500">
              请选择一个班级（或在“按学生名单”里先选班级）以加载课程关联项。
            </div>

            <div v-else>
              <div class="mb-3 text-sm text-gray-600">
                当前课表来源班级：<span class="font-medium text-gray-900">{{ primaryClassName }}</span>
              </div>

              <div v-if="loadingOfferings" class="text-sm text-gray-500">正在加载课程…</div>

              <div v-else class="border border-gray-200 rounded-lg overflow-hidden">
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
                  <tr
                      v-for="row in offeringRows"
                      :key="row.offeringId"
                      :class="['hover:bg-gray-50 transition-colors', row.selected ? 'bg-blue-50/50' : '']"
                  >
                    <td class="px-4 py-3 text-sm font-medium text-gray-900">{{ row.courseName }}</td>
                    <td class="px-4 py-3 text-sm text-gray-600">{{ row.teacherName }}</td>
                    <td class="px-4 py-3 text-sm text-gray-600">{{ row.timeText }}</td>
                    <td class="px-4 py-3 text-sm text-gray-600">{{ row.classroom || '-' }}</td>
                    <td class="px-4 py-3 text-center">
                      <input type="checkbox" v-model="row.selected" class="w-4 h-4 text-blue-600 rounded focus:ring-blue-500" />
                    </td>
                  </tr>

                  <tr v-if="offeringRows.length === 0">
                    <td colspan="5" class="px-4 py-6 text-center text-sm text-gray-500">
                      该班级在当前学期没有开课数据
                    </td>
                  </tr>
                  </tbody>
                </table>
              </div>

              <div class="mt-3 text-xs text-gray-500">
                提示：勾选课程后，会按公假起止日期展开为具体日期节次写入影响表；不勾选则由后端自动匹配。
              </div>
            </div>
          </div>
        </div>

      </div>

      <div class="px-8 py-6 bg-gray-50 border-t border-gray-200 flex items-center justify-between rounded-b-lg">
        <button @click="openDraftDialog" class="text-sm text-gray-600 hover:text-gray-900 transition-colors font-medium">← 返回列表</button>

        <div class="flex items-center gap-3">
          <button @click="saveDraft" class="px-6 py-2.5 border border-gray-300 rounded-lg hover:bg-white transition-colors text-sm font-medium text-gray-700">
            保存草稿
          </button>
          <button @click="clearDraft" class="px-6 py-2.5 border border-gray-300 rounded-lg hover:bg-white transition-colors text-sm font-medium text-gray-700">
            清空草稿
          </button>
          <button @click="handleSubmit" class="px-8 py-2.5 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors shadow-sm flex items-center gap-2 text-sm font-medium">
            <Check class="w-5 h-5" /> 提交公假申请
          </button>
        </div>
      </div>
    </div>

    <!-- 学生选择弹窗 -->
    <el-dialog v-model="studentDialogVisible" title="选择学生" width="780px" :close-on-click-modal="false">
      <div class="space-y-3">
        <div class="flex items-center gap-3">
          <div class="w-52">
            <el-select v-model="studentDialogClassId" placeholder="选择班级" style="width: 100%" @change="loadStudentsForDialog">
              <el-option v-for="cls in classes" :key="cls.classId" :label="cls.className" :value="cls.classId" />
            </el-select>
          </div>

          <el-input v-model="studentSearch" placeholder="按姓名/学号搜索" clearable />
          <div class="text-sm text-gray-600">已选 {{ selectedStudents.length }} 人</div>
        </div>

        <div class="border rounded-lg overflow-hidden">
          <div v-if="loadingDialogStudents" class="p-4 text-sm text-gray-500">正在加载学生…</div>
          <div v-else class="max-h-96 overflow-auto">
            <table class="w-full">
              <thead class="bg-gray-50 border-b">
              <tr>
                <th class="px-4 py-3 text-left text-sm font-medium text-gray-600 w-20">选择</th>
                <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">学号</th>
                <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">姓名</th>
              </tr>
              </thead>
              <tbody class="divide-y">
              <tr v-for="stu in filteredDialogStudents" :key="stu.studentId" class="hover:bg-gray-50">
                <td class="px-4 py-2">
                  <input type="checkbox" :checked="isStudentSelected(stu.studentId)" @change="toggleStudent(stu)" />
                </td>
                <td class="px-4 py-2 text-sm text-gray-700">{{ stu.studentNo }}</td>
                <td class="px-4 py-2 text-sm text-gray-900 font-medium">{{ stu.name }}</td>
              </tr>

              <tr v-if="filteredDialogStudents.length === 0">
                <td colspan="3" class="px-4 py-6 text-center text-sm text-gray-500">
                  无匹配学生
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="flex justify-between items-center w-full">
          <div class="text-xs text-gray-500">提示：学生名单来自后端真实数据</div>
          <div class="flex items-center gap-2">
            <el-button @click="studentDialogVisible=false">关闭</el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <!-- 草稿列表弹窗（解决“返回列表没反应” + 支持多份草稿） -->
    <el-dialog v-model="draftDialogVisible" title="公假草稿列表" width="820px" :close-on-click-modal="false">
      <div class="space-y-3">
        <div class="text-sm text-gray-600">
          这里展示本地保存的多份草稿（localStorage）。你可以验证“是否能保存多份”。
        </div>

        <div class="border rounded-lg overflow-hidden">
          <table class="w-full">
            <thead class="bg-gray-50 border-b">
            <tr>
              <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">草稿名</th>
              <th class="px-4 py-3 text-left text-sm font-medium text-gray-600 w-56">更新时间</th>
              <th class="px-4 py-3 text-center text-sm font-medium text-gray-600 w-44">操作</th>
            </tr>
            </thead>
            <tbody class="divide-y">
            <tr v-for="d in drafts" :key="d.id" class="hover:bg-gray-50">
              <td class="px-4 py-3 text-sm text-gray-900 font-medium">{{ d.title }}</td>
              <td class="px-4 py-3 text-sm text-gray-600">{{ formatTime(d.updatedAt) }}</td>
              <td class="px-4 py-3 text-center">
                <button class="text-sm text-blue-600 hover:text-blue-700 mr-3" @click="loadDraft(d.id)">加载</button>
                <button class="text-sm text-red-600 hover:text-red-700" @click="deleteDraft(d.id)">删除</button>
              </td>
            </tr>

            <tr v-if="drafts.length === 0">
              <td colspan="3" class="px-4 py-6 text-center text-sm text-gray-500">
                暂无草稿
              </td>
            </tr>
            </tbody>
          </table>
        </div>
      </div>

      <template #footer>
        <div class="flex justify-between items-center w-full">
          <el-button @click="goTodo">返回待办</el-button>
          <div class="flex items-center gap-2">
            <el-button @click="draftDialogVisible=false">关闭</el-button>
          </div>
        </div>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { Calendar, Users, FileText, Info, Check } from 'lucide-vue-next';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  createBatchPublicLeave,
  getCounselorClasses,
  getCounselorClassStudents,
  getCounselorOfferingsByTermClass,
  type CounselorClassDTO,
  type CounselorStudentDTO,
  type CounselorOfferingDTO
} from '@/api/tutor';

// ===== 基础状态 =====
const router = useRouter();

const form = reactive({
  name: '',
  type: '',
  startTime: '',
  endTime: '',
  description: ''
});

const selectionMode = ref<'class' | 'student'>('class');

const classes = ref<CounselorClassDTO[]>([]);
const loadingClasses = ref(false);

const selectedClassIds = ref<number[]>([]);
const classStudentsCache = ref(new Map<number, CounselorStudentDTO[]>());
const classStudentCount = ref(new Map<number, number>());

// ===== 学生选择弹窗 =====
const studentDialogVisible = ref(false);
const studentDialogClassId = ref<number | null>(null);
const dialogStudents = ref<CounselorStudentDTO[]>([]);
const loadingDialogStudents = ref(false);
const studentSearch = ref('');

type PickedStudent = CounselorStudentDTO & { className?: string };
const selectedStudents = ref<PickedStudent[]>([]);

// ===== Offering(课程关联) =====
type OfferingRow = CounselorOfferingDTO & { selected: boolean; timeText: string };
const offeringRows = ref<OfferingRow[]>([]);
const loadingOfferings = ref(false);

// ===== 草稿多份保存 =====
const draftDialogVisible = ref(false);
const DRAFTS_KEY = 'counselor_public_leave_drafts_v1';
const currentDraftId = ref<string | null>(null);

type DraftItem = {
  id: string;
  title: string;
  updatedAt: number;
  payload: any;
};
const drafts = ref<DraftItem[]>([]);

// ===== 计算：估算人数 =====
const estimatedCount = computed(() => {
  if (selectionMode.value === 'student') return selectedStudents.value.length;
  let sum = 0;
  for (const cid of selectedClassIds.value) {
    sum += classStudentCount.value.get(cid) ?? 0;
  }
  return sum;
});

// 课表加载的“主班级”：
// - 按班级：只选了 1 个班，拿这个
// - 按学生：弹窗里当前班级
const primaryClassId = computed(() => {
  if (selectionMode.value === 'class') {
    if (selectedClassIds.value.length === 1) return selectedClassIds.value[0];
    return null;
  }
  return studentDialogClassId.value;
});

const primaryClassName = computed(() => {
  const id = primaryClassId.value;
  if (!id) return '';
  return classes.value.find(c => c.classId === id)?.className || '';
});

const termId = computed(() => {
  const s = localStorage.getItem('termId') || '1';
  const n = Number(s);
  return Number.isFinite(n) ? n : 1;
});

// ===== 工具函数 =====
function toIsoLocalDateTime(v: string) {
  // datetime-local: "YYYY-MM-DDTHH:mm" 或 "YYYY-MM-DDTHH:mm:ss"
  if (!v) return v;
  return v.length === 16 ? `${v}:00` : v;
}

function weekdayText(weekDay: number) {
  const map = ['一', '二', '三', '四', '五', '六', '日'];
  const idx = Math.max(1, Math.min(7, weekDay)) - 1;
  return `周${map[idx]}`;
}

function formatYMD(d: Date) {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}

function formatTime(ts: number) {
  const d = new Date(ts);
  return `${formatYMD(d)} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
}

function getAllDrafts(): DraftItem[] {
  try {
    const raw = localStorage.getItem(DRAFTS_KEY);
    if (!raw) return [];
    const arr = JSON.parse(raw);
    return Array.isArray(arr) ? arr : [];
  } catch {
    return [];
  }
}

function saveAllDrafts(arr: DraftItem[]) {
  localStorage.setItem(DRAFTS_KEY, JSON.stringify(arr));
}

// ===== 后端数据加载 =====
async function loadClasses() {
  loadingClasses.value = true;
  try {
    const list = await getCounselorClasses();
    classes.value = list || [];
    // 默认选第一个班（仅为了 UI 体验）
    if (classes.value.length && selectedClassIds.value.length === 0) {
      selectedClassIds.value = [classes.value[0].classId];
    }
    // 学生弹窗默认班级
    if (classes.value.length && studentDialogClassId.value == null) {
      studentDialogClassId.value = classes.value[0].classId;
    }
  } finally {
    loadingClasses.value = false;
  }
}

async function ensureStudentsLoaded(classId: number) {
  if (classStudentsCache.value.has(classId)) return;
  const list = await getCounselorClassStudents(classId);
  classStudentsCache.value.set(classId, list || []);
  classStudentCount.value.set(classId, (list || []).length);
}

async function loadOfferingsForPrimaryClass() {
  const cid = primaryClassId.value;
  if (!cid) {
    offeringRows.value = [];
    return;
  }
  loadingOfferings.value = true;
  try {
    const list = await getCounselorOfferingsByTermClass(termId.value, cid);
    offeringRows.value = (list || []).map((o) => ({
      ...o,
      selected: false,
      timeText: `${weekdayText(o.weekDay)} 第${o.sectionStart}-${o.sectionEnd}节`
    }));
  } finally {
    loadingOfferings.value = false;
  }
}

// ===== 学生弹窗逻辑 =====
async function openStudentDialog() {
  if (!classes.value.length) {
    ElMessage.warning('当前没有可用班级数据');
    return;
  }
  studentDialogVisible.value = true;
  if (studentDialogClassId.value == null) {
    studentDialogClassId.value = classes.value[0].classId;
  }
  await loadStudentsForDialog();
}

async function loadStudentsForDialog() {
  const cid = studentDialogClassId.value;
  if (!cid) return;
  loadingDialogStudents.value = true;
  try {
    const list = await getCounselorClassStudents(cid);
    dialogStudents.value = list || [];
  } finally {
    loadingDialogStudents.value = false;
  }
}

const filteredDialogStudents = computed(() => {
  const key = studentSearch.value.trim();
  if (!key) return dialogStudents.value;
  return dialogStudents.value.filter(s =>
      (s.name || '').includes(key) || (s.studentNo || '').includes(key)
  );
});

function isStudentSelected(studentId: number) {
  return selectedStudents.value.some(s => s.studentId === studentId);
}

function toggleStudent(stu: CounselorStudentDTO) {
  const idx = selectedStudents.value.findIndex(s => s.studentId === stu.studentId);
  if (idx >= 0) {
    selectedStudents.value.splice(idx, 1);
  } else {
    const className = classes.value.find(c => c.classId === stu.classId)?.className;
    selectedStudents.value.push({ ...stu, className });
  }
}

// ===== 草稿：多份保存/列表/加载/删除 =====
function refreshDrafts() {
  drafts.value = getAllDrafts().sort((a, b) => b.updatedAt - a.updatedAt);
}

function buildDraftPayload() {
  return {
    form: { ...form },
    selectionMode: selectionMode.value,
    selectedClassIds: [...selectedClassIds.value],
    selectedStudents: [...selectedStudents.value],
    offeringRows: offeringRows.value.map(r => ({ offeringId: r.offeringId, selected: r.selected }))
  };
}

async function saveDraft() {
  const title = `${form.name || '未命名公假'}（${new Date().toLocaleString()}）`;
  const payload = buildDraftPayload();
  const now = Date.now();

  const arr = getAllDrafts();
  if (currentDraftId.value) {
    const i = arr.findIndex(d => d.id === currentDraftId.value);
    if (i >= 0) {
      arr[i] = { ...arr[i], title, updatedAt: now, payload };
    } else {
      arr.push({ id: currentDraftId.value, title, updatedAt: now, payload });
    }
  } else {
    const id = `draft_${now}_${Math.random().toString(16).slice(2)}`;
    currentDraftId.value = id;
    arr.push({ id, title, updatedAt: now, payload });
  }

  saveAllDrafts(arr);
  refreshDrafts();
  ElMessage.success('草稿已保存（支持多份）');
}

async function clearDraft() {
  try {
    await ElMessageBox.confirm('确定要清空当前编辑内容吗？（不会删除草稿列表里的其他草稿）', '确认清空', {
      confirmButtonText: '确定清空',
      cancelButtonText: '取消',
      type: 'warning'
    });
  } catch {
    return;
  }

  form.name = '';
  form.type = '';
  form.startTime = '';
  form.endTime = '';
  form.description = '';

  selectionMode.value = 'class';
  selectedClassIds.value = [];
  selectedStudents.value = [];
  offeringRows.value.forEach(r => r.selected = false);

  currentDraftId.value = null;
  ElMessage.success('已清空当前内容');
}

function openDraftDialog() {
  refreshDrafts();
  draftDialogVisible.value = true;
}

async function loadDraft(id: string) {
  const arr = getAllDrafts();
  const d = arr.find(x => x.id === id);
  if (!d) return;

  const p = d.payload || {};
  currentDraftId.value = d.id;

  Object.assign(form, p.form || {});
  selectionMode.value = p.selectionMode || 'class';
  selectedClassIds.value = Array.isArray(p.selectedClassIds) ? p.selectedClassIds : [];
  selectedStudents.value = Array.isArray(p.selectedStudents) ? p.selectedStudents : [];

  // 先确保班级学生数量能正确显示
  for (const cid of selectedClassIds.value) {
    await ensureStudentsLoaded(cid);
  }

  // 重新加载当前班级的课程列表，再恢复勾选状态
  if (classes.value.length) {
    if (selectionMode.value === 'student') {
      // 学生模式：弹窗班级切到草稿里第一位学生的班级或默认
      const first = selectedStudents.value[0];
      studentDialogClassId.value = first?.classId ?? classes.value[0].classId;
    }
  }
  await loadOfferingsForPrimaryClass();

  const selMap = new Map<number, boolean>();
  (p.offeringRows || []).forEach((x: any) => selMap.set(Number(x.offeringId), !!x.selected));
  offeringRows.value.forEach(r => r.selected = selMap.get(r.offeringId) || false);

  ElMessage.success('草稿已加载');
  draftDialogVisible.value = false;
}

async function deleteDraft(id: string) {
  const arr = getAllDrafts().filter(d => d.id !== id);
  saveAllDrafts(arr);
  refreshDrafts();
  if (currentDraftId.value === id) currentDraftId.value = null;
  ElMessage.success('已删除草稿');
}

function goTodo() {
  draftDialogVisible.value = false;
  router.push('/tutor/todos');
}

// ===== 提交：真实调用后端 /api/leaves/public/batch =====
function validateForm() {
  if (!form.name || !form.type || !form.startTime || !form.endTime || !form.description) {
    ElMessage.warning('请填写完整的公假信息');
    return false;
  }
  if (selectionMode.value === 'class') {
    if (!selectedClassIds.value.length) {
      ElMessage.warning('请选择至少一个班级');
      return false;
    }
  } else {
    if (!selectedStudents.value.length) {
      ElMessage.warning('请选择至少一名学生');
      return false;
    }
  }
  return true;
}

function expandImpactsFromSelectedOfferings(rows: OfferingRow[], start: string, end: string) {
  const picked = rows.filter(r => r.selected);
  if (!picked.length) return null; // 交给后端自动匹配

  const s = new Date(toIsoLocalDateTime(start));
  const e = new Date(toIsoLocalDateTime(end));
  if (Number.isNaN(s.getTime()) || Number.isNaN(e.getTime())) return null;

  const startDate = new Date(s.getFullYear(), s.getMonth(), s.getDate());
  const endDate = new Date(e.getFullYear(), e.getMonth(), e.getDate());

  const res: Array<{ offeringId: number; courseDate: string; sectionStart: number; sectionEnd: number }> = [];
  const seen = new Set<string>();

  for (let d = new Date(startDate); d.getTime() <= endDate.getTime(); d.setDate(d.getDate() + 1)) {
    // JS: 0=周日..6=周六 => 转成 1..7 且周一=1
    const js = d.getDay(); // 0..6
    const weekDay = js === 0 ? 7 : js; // 1..7, 周日=7
    const ymd = formatYMD(d);

    for (const off of picked) {
      if (off.weekDay !== weekDay) continue;
      const key = `${off.offeringId}_${ymd}`;
      if (seen.has(key)) continue;
      seen.add(key);
      res.push({
        offeringId: off.offeringId,
        courseDate: ymd,
        sectionStart: off.sectionStart,
        sectionEnd: off.sectionEnd
      });
    }
  }
  return res;
}

async function resolveStudentIds(): Promise<number[]> {
  if (selectionMode.value === 'student') {
    return selectedStudents.value.map(s => s.studentId);
  }

  // 班级模式：把选中班级的学生合并
  const ids: number[] = [];
  for (const cid of selectedClassIds.value) {
    await ensureStudentsLoaded(cid);
    const list = classStudentsCache.value.get(cid) || [];
    for (const s of list) ids.push(s.studentId);
  }
  // 去重
  return Array.from(new Set(ids));
}

async function handleSubmit() {
  if (!validateForm()) return;

  const studentIds = await resolveStudentIds();
  if (!studentIds.length) {
    ElMessage.error('没有解析到学生列表');
    return;
  }

  const impacts = expandImpactsFromSelectedOfferings(offeringRows.value, form.startTime, form.endTime);

  const reason = `【${form.type}】${form.name}：${form.description}`;

  await createBatchPublicLeave({
    // counselorId 可不传，后端会从 token 取当前辅导员
    studentIds,
    termId: termId.value,
    reason,
    startTime: toIsoLocalDateTime(form.startTime),
    endTime: toIsoLocalDateTime(form.endTime),
    impacts: impacts ?? null
  });

  ElMessage.success(`公假已提交：${studentIds.length} 名学生`);
}

// ===== 生命周期/监听 =====
watch(selectedClassIds, async (n) => {
  if (selectionMode.value !== 'class') return;
  for (const cid of n) {
    await ensureStudentsLoaded(cid);
  }
}, { deep: true });

watch([primaryClassId, termId], async () => {
  await loadOfferingsForPrimaryClass();
});

watch(selectionMode, async () => {
  // 切换模式时也刷新课程列表（以 primaryClassId 为准）
  await loadOfferingsForPrimaryClass();
});

onMounted(async () => {
  await loadClasses();
  refreshDrafts();

  // 班级模式默认选中班级后，补齐人数统计
  for (const cid of selectedClassIds.value) {
    await ensureStudentsLoaded(cid);
  }
  await loadOfferingsForPrimaryClass();
});
</script>
