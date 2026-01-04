<template>
  <div class="max-w-2xl mx-auto space-y-6">
    <div class="bg-white rounded-lg shadow-sm p-6 border border-gray-200">
      <h2 class="text-lg font-bold text-gray-800 mb-4 border-b pb-2">学期设置</h2>

      <div class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">当前学期名称</label>
          <input
            type="text"
            v-model="form.name"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
          />
        </div>

        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">开学日期</label>
            <input
              type="date"
              v-model="form.startDate"
              class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">总周数</label>
            <input
              type="number"
              v-model.number="form.totalWeeks"
              class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
              min="1"
            />
          </div>
        </div>

        <div class="pt-2">
          <button
            @click="onSave"
            :disabled="saving"
            class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700 disabled:opacity-60"
          >
            保存设置
          </button>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { listTerms, publishSemesterData } from '@/api/admin';

const saving = ref(false);

const form = reactive({
  name: '2025-2026学年 第一学期',
  startDate: '2025-09-01',
  totalWeeks: 20
});

onMounted(async () => {
  try {
    const terms = await listTerms();
    const current = terms.find(t => t.isCurrent) || terms[0];
    if (current) {
      form.name = current.termName || form.name;
      form.startDate = current.startDate || form.startDate;

      // 用 start/end 反推周数（四舍五入到整周，至少 1）
      const s = new Date(current.startDate);
      const e = new Date(current.endDate);
      const days = Math.max(1, Math.round((e.getTime() - s.getTime()) / 86400000) + 1);
      form.totalWeeks = Math.max(1, Math.round(days / 7));
    }
  } catch (e) {
    // 不阻塞页面
  }
});

const onSave = async () => {
  if (!form.name.trim()) return ElMessage.error('学期名称不能为空');
  if (!form.startDate) return ElMessage.error('开学日期不能为空');
  if (!(form.totalWeeks >= 1)) return ElMessage.error('总周数必须 >= 1');

  saving.value = true;
  try {
    await publishSemesterData({
      name: form.name.trim(),
      startDate: form.startDate,
      totalWeeks: form.totalWeeks
    });
    ElMessage.success('保存成功（已设置为当前学期）');
  } finally {
    saving.value = false;
  }
};
</script>
