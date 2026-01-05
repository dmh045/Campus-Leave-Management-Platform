<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 p-4">
    <div class="max-w-md mx-auto pt-8">
      <!-- 页面标题 -->
      <div class="text-center mb-8">
        <div class="inline-flex p-4 bg-white rounded-2xl shadow-lg mb-4">
          <Smartphone class="w-10 h-10 text-blue-600" />
        </div>
        <h1 class="text-2xl font-bold text-gray-900 mb-2">课堂签到</h1>
        <p class="text-gray-600 text-sm">请在老师规定的时间内完成签到</p>
      </div>

      <template v-if="status !== 'success'">
        <!-- 签到卡片 -->
        <div class="bg-white rounded-2xl shadow-lg p-6 mb-6 border border-gray-100">
        <div class="flex items-center gap-3 mb-6">
          <div class="p-3 bg-blue-100 rounded-xl">
            <QrCode class="w-6 h-6 text-blue-600" />
          </div>
          <div class="flex-1">
            <h2 class="font-bold text-gray-900 text-lg">数据结构与算法</h2>
            <p class="text-gray-500 text-sm">计算机科学与技术 2021级1班</p>
          </div>
        </div>

        <!-- 课程信息 -->
        <div class="space-y-3 mb-6">
          <div class="flex items-center gap-2 text-sm text-gray-600 bg-gray-50 p-3 rounded-xl">
            <Clock class="w-4 h-4" /> 08:00 - 09:40
          </div>
          <div class="flex items-center gap-2 text-sm text-gray-600 bg-gray-50 p-3 rounded-xl">
            <MapPinned class="w-4 h-4" /> 1号教学楼 201教室
          </div>
        </div>

        <!-- 位置校验 -->
        <div class="bg-blue-50 border border-blue-200 rounded-xl p-4 mb-4">
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-2">
              <MapPin class="w-5 h-5 text-blue-600" />
              <span class="font-medium text-blue-900">位置校验</span>
            </div>
            <CheckCircle v-if="locStatus === 'success'" class="w-6 h-6 text-green-500" />
          </div>

          <button 
            v-if="locStatus === 'idle'"
            @click="verifyLocation"
            class="w-full py-3 bg-blue-600 hover:bg-blue-700 text-white rounded-xl mt-3 transition-colors flex items-center justify-center gap-2 shadow-md font-bold"
          >
            <MapPin class="w-5 h-5" /> 开始位置校验
          </button>
          
          <div v-if="locStatus === 'verifying'" class="w-full py-3 bg-blue-100 text-blue-700 rounded-xl mt-3 flex items-center justify-center gap-2 font-medium">
            <Loader class="w-5 h-5 animate-spin" /> 正在获取位置...
          </div>
        </div>

        <!-- 签到按钮 -->
        <button
          @click="doCheckIn"
          :disabled="status === 'verifying'"
          class="w-full py-4 bg-gradient-to-r from-green-600 to-emerald-600 hover:from-green-700 hover:to-emerald-700 text-white rounded-xl transition-all transform hover:scale-[1.02] disabled:opacity-50 disabled:cursor-not-allowed shadow-lg font-bold text-lg flex items-center justify-center gap-2 mt-4"
        >
          <CheckCircle v-if="status !== 'verifying'" class="w-6 h-6" />
          {{ status === 'verifying' ? '正在签到...' : '确认签到' }}
        </button>
      </div>
      </template>

      <template v-else>
      <!-- 成功提示 -->
      <div class="bg-white rounded-2xl shadow-lg p-8 mb-6 border border-green-200 text-center animate-fade-in">
        <div class="inline-flex items-center justify-center w-20 h-20 bg-green-100 rounded-full mb-4 animate-bounce">
          <CheckCircle class="w-10 h-10 text-green-600" />
        </div>
        <h2 class="text-2xl font-bold text-green-700 mb-2">签到成功！</h2>
        <p class="text-gray-600 mb-4">签到时间：{{ checkInTime }}</p>
        <div class="bg-green-50 border border-green-200 rounded-xl p-4">
          <p class="text-green-800 font-medium">祝您学习愉快！</p>
        </div>
      </div>
      </template>

      <!-- 温馨提示 -->
      <div class="bg-white/70 backdrop-blur-sm rounded-xl p-4 text-center">
        <p class="text-xs text-gray-500">
          如遇问题请联系任课教师或教务处<br />
          签到token可通过扫码或由老师提供
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Smartphone, QrCode, Clock, MapPin, CheckCircle, MapPinned, Loader } from 'lucide-vue-next';
import dayjs from 'dayjs';
import { studentCheckin } from '@/api/attendance';

const route = useRoute();

const status = ref<'idle' | 'verifying' | 'success'>('idle');
const locStatus = ref<'idle' | 'verifying' | 'success'>('idle');
const checkInTime = ref('');

// ✅ 支持扫码链接携带 token：/checkin?token=xxxx
const token = ref<string>(typeof route.query.token === 'string' ? route.query.token : '');

const verifyLocation = () => {
  locStatus.value = 'verifying';
  setTimeout(() => {
    locStatus.value = 'success';
  }, 1500);
};

const askTokenIfNeeded = async (): Promise<string | null> => {
  if (token.value && token.value.trim()) return token.value.trim();

  try {
    const { value } = await ElMessageBox.prompt('请输入老师提供的签到 token（或通过扫码自动填充）', '签到口令', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: 'token',
      inputValidator: (v) => (v && String(v).trim().length > 0 ? true : 'token 不能为空'),
    });
    token.value = String(value).trim();
    return token.value;
  } catch {
    return null; // 用户取消
  }
};

const doCheckIn = async () => {
  if (locStatus.value !== 'success') {
    ElMessage.warning('请先完成位置校验');
    return;
  }

  const sidStr = localStorage.getItem('userId') || localStorage.getItem('studentId');
  const studentId = sidStr ? Number(sidStr) : 0;
  if (!studentId) {
    ElMessage.error('未获取到学生信息（userId/studentId），请确认已登录');
    return;
  }

  const tk = await askTokenIfNeeded();
  if (!tk) return;

  status.value = 'verifying';
  try {
    await studentCheckin({ studentId, token: tk });
    status.value = 'success';
    checkInTime.value = dayjs().format('HH:mm:ss');
  } catch (e: any) {
    status.value = 'idle';
    ElMessage.error(e?.message || '签到失败');
  }
};
</script>

<style scoped>
@keyframes fade-in {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
.animate-fade-in { animation: fade-in 0.3s ease-out; }
</style>
