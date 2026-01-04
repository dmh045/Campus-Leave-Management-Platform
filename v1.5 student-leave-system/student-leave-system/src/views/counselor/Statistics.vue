<template>
  <div class="space-y-6">
    <div class="flex items-start justify-between">
      <div>
        <h2 class="mb-2 text-xl font-bold text-gray-900">班级统计</h2>
        <p class="text-sm text-gray-600">查看所管班级的请假类型分布与周趋势</p>
      </div>

      <div class="flex items-center gap-3">
        <div class="flex items-center gap-2">
          <Users class="w-4 h-4 text-gray-500" />
          <select
            v-model="selectedClass"
            class="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white"
          >
            <option value="计算机2101">计算机2101</option>
            <option value="计算机2102">计算机2102</option>
            <option value="计算机2103">计算机2103</option>
            <option value="all">全部班级</option>
          </select>
        </div>

        <div class="flex items-center gap-2">
          <Calendar class="w-4 h-4 text-gray-500" />
          <div class="flex items-center bg-gray-100 rounded-lg p-1">
            <button
              v-for="option in timeOptions"
              :key="option.value"
              @click="timeRange = option.value"
              :class="[
                'px-3 py-1 rounded-md text-sm transition-colors',
                timeRange === option.value
                  ? 'bg-white text-blue-600 shadow-sm'
                  : 'text-gray-600 hover:text-gray-900'
              ]"
            >
              {{ option.label }}
            </button>
          </div>
        </div>

        <button class="flex items-center gap-2 px-3 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors text-sm font-medium">
          <Filter class="w-4 h-4" />
          类型筛选
        </button>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <div class="lg:col-span-2 space-y-6">
        <div class="bg-white rounded-lg shadow-sm p-6 border border-gray-100">
          <div class="mb-6">
            <h3 class="mb-1 text-lg font-bold text-gray-900">请假类型分布</h3>
            <p class="text-sm text-gray-500">当前筛选条件下，不同类型请假占比</p>
          </div>

          <div ref="pieChartRef" class="w-full h-[280px]"></div>

          <div class="mt-6 flex items-center justify-center gap-8">
            <div v-for="(item, index) in leaveTypeData" :key="index" class="flex items-center gap-2">
              <div class="w-3 h-3 rounded-full" :style="{ backgroundColor: item.color }"></div>
              <span class="text-sm text-gray-700">{{ item.name }}</span>
              <span class="text-sm text-gray-500">{{ item.value }} 次</span>
            </div>
          </div>
          <div class="mt-4 text-center text-xs text-gray-400">数据截止时间：{{ currentTime }}</div>
        </div>

        <div class="bg-white rounded-lg shadow-sm p-6 border border-gray-100">
          <div class="flex items-center justify-between mb-6">
            <div>
              <h3 class="mb-1 text-lg font-bold text-gray-900">近一周请假趋势</h3>
              <p class="text-sm text-gray-500">按日期统计请假人次变化</p>
            </div>
            <div class="flex items-center bg-gray-100 rounded-lg p-1">
              <button
                v-for="period in trendPeriods"
                :key="period"
                @click="trendPeriod = period"
                :class="[
                  'px-3 py-1 rounded-md text-sm transition-colors',
                  trendPeriod === period
                    ? 'bg-white text-blue-600 shadow-sm'
                    : 'text-gray-600 hover:text-gray-900'
                ]"
              >
                {{ period === '7days' ? '近7天' : '近30天' }}
              </button>
            </div>
          </div>
          <div ref="barChartRef" class="w-full h-[320px]"></div>
        </div>
      </div>

      <div class="space-y-6">
        <div class="space-y-3">
          <div v-for="(stat, index) in summaryStats" :key="index" 
            class="bg-white rounded-lg shadow-sm p-4 border border-gray-100 hover:border-blue-200 transition-colors"
          >
            <div class="flex items-center justify-between">
              <div class="flex-1">
                <div class="text-sm text-gray-600 mb-2">{{ stat.label }}</div>
                <div class="flex items-end gap-3">
                  <div class="text-2xl font-bold text-gray-900">{{ stat.value }}</div>
                  <div :class="['flex items-center gap-1 text-sm mb-1 font-medium', stat.trend === 'up' ? 'text-red-600' : 'text-green-600']">
                     <component :is="stat.trend === 'up' ? TrendingUp : TrendingDown" class="w-4 h-4" />
                     <span>{{ stat.change }}</span>
                  </div>
                </div>
              </div>
              <div :class="['p-3 rounded-lg', stat.bgClass]">
                <component :is="stat.icon" class="w-5 h-5" :class="stat.textClass" />
              </div>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-lg shadow-sm overflow-hidden border border-gray-100">
          <div class="px-6 py-4 border-b border-gray-200 bg-gradient-to-r from-orange-50 to-red-50">
            <div class="flex items-center gap-2">
              <AlertTriangle class="w-5 h-5 text-orange-600" />
              <h3 class="text-orange-900 font-bold">异常提醒</h3>
            </div>
            <p class="text-sm text-orange-700 mt-1">需要关注的异常情况</p>
          </div>

          <div class="divide-y divide-gray-100">
            <div v-for="alert in anomalyAlerts" :key="alert.id" :class="['p-4 border-l-4 hover:bg-gray-50/50 transition-colors', getAlertStyle(alert.type).border]">
              <div class="flex items-start justify-between gap-3">
                <div class="flex-1 min-w-0">
                  <div class="flex items-center gap-2 mb-2">
                    <span :class="['px-2 py-0.5 rounded text-xs font-medium', getAlertStyle(alert.type).tag]">
                      {{ alert.category }}
                    </span>
                    <span class="text-xs text-gray-500">{{ alert.time }}</span>
                  </div>
                  <p class="text-sm text-gray-800 leading-relaxed">{{ alert.message }}</p>
                </div>
                <button class="flex items-center gap-1 text-sm text-blue-600 hover:text-blue-700 flex-shrink-0 font-medium" @click="handleAlertClick(alert)">
                  <span class="whitespace-nowrap">查看</span>
                  <ChevronRight class="w-4 h-4" />
                </button>
              </div>
            </div>
          </div>

          <div class="px-6 py-3 bg-gray-50 border-t border-gray-200">
            <button class="text-sm text-blue-600 hover:text-blue-700 flex items-center gap-1 font-medium">
              查看全部异常记录
              <ChevronRight class="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue';
import { BarChart3, TrendingDown, TrendingUp, Users, Calendar, AlertTriangle, ChevronRight, Filter } from 'lucide-vue-next';
import { ElMessage } from 'element-plus';
import * as echarts from 'echarts';
import dayjs from 'dayjs';
import { getClassLeaveStats, type ClassLeaveStatsDTO } from '@/api/stats';
import { getMessageList, markAsRead, type Message } from '@/api/messages';

// --- 筛选条件（UI保持不变） ---
const timeRange = ref<'week' | 'month' | 'semester'>('week');
const selectedClass = ref('计算机2101');
const trendPeriod = ref<'7days' | '30days'>('7days');
const trendPeriods = ['7days', '30days'] as const;
const currentTime = ref(dayjs().format('YYYY-MM-DD HH:mm'));

const timeOptions = [
  { value: 'week', label: '本周' },
  { value: 'month', label: '本月' },
  { value: 'semester', label: '本学期' }
] as const;

// ⚠️ 后端 stats 接口需要 classId，但当前后端未提供“按辅导员列出班级”的接口。
// 这里先按后端种子数据做最小映射：CS2101/计科21-1班 -> classId=1
// 其他班级/全院(all) 如后端未来补齐接口，再完善映射或改为动态获取。
const classNameToId: Record<string, number> = {
  '计算机2101': 1
};

// --- 后端真实统计数据 ---
const statsLoading = ref(false);
const statsData = ref<ClassLeaveStatsDTO | null>(null);

// --- 页面展示数据（基于 statsData） ---
const leaveTypeData = ref([
  { name: '病假', value: 0, color: '#ef4444' },
  { name: '事假', value: 0, color: '#f59e0b' },
  { name: '公假', value: 0, color: '#3b82f6' }
]);

const summaryStats = computed(() => {
  const dto = statsData.value;
  return [
    {
      label: '总请假单数',
      value: dto ? String(dto.totalLeaves) : '0',
      change: '', // 后端未提供环比/同比，留空避免误导
      trend: 'up',
      icon: BarChart3,
      bgClass: 'bg-blue-50',
      textClass: 'text-blue-600'
    },
    {
      label: '病假单数',
      value: dto ? String(dto.sickCount) : '0',
      change: '',
      trend: 'up',
      icon: Users,
      bgClass: 'bg-red-50',
      textClass: 'text-red-600'
    },
    {
      label: '事假单数',
      value: dto ? String(dto.affairCount) : '0',
      change: '',
      trend: 'up',
      icon: Users,
      bgClass: 'bg-orange-50',
      textClass: 'text-orange-600'
    },
    {
      label: '公假单数',
      value: dto ? String(dto.publicCount) : '0',
      change: '',
      trend: 'up',
      icon: Users,
      bgClass: 'bg-blue-50',
      textClass: 'text-blue-600'
    },
    {
      label: '驳回/退回单数',
      value: dto ? String(dto.rejectedCount) : '0',
      change: '',
      trend: 'down',
      icon: AlertTriangle,
      bgClass: 'bg-red-50',
      textClass: 'text-red-600'
    }
  ];
});

// --- 异常提醒（对接后端消息中心：/api/messages/list） ---
type AlertLevel = 'critical' | 'warning' | 'info';

interface AnomalyAlert {
  id: string;
  type: AlertLevel;
  message: string;
  time: string;
  category: string;

  // 关联后端 message
  messageId?: number;
  isRead?: boolean;
  relatedId?: number;
  messageType?: string;
  createdAt?: string;
}

const anomalyAlerts = ref<AnomalyAlert[]>([]);
const anomalyLoading = ref(false);

const messageTypeToCategory = (t?: string) => {
  const map: Record<string, string> = {
    LEAVE_STATUS_CHANGE: '请假状态',
    TEACHER_CONFIRM: '教师确认',
    COUNSELOR_APPROVE: '辅导员审批',
  };
  if (!t) return '系统消息';
  return map[t] || '系统消息';
};

const formatRelativeTime = (ts?: string) => {
  if (!ts) return '';
  const d = dayjs(ts);
  if (!d.isValid()) return String(ts);

  const now = dayjs();
  const minutes = now.diff(d, 'minute');
  if (minutes < 1) return '刚刚';
  if (minutes < 60) return `${minutes}分钟前`;

  const hours = now.diff(d, 'hour');
  if (hours < 24) return `${hours}小时前`;

  const days = now.diff(d, 'day');
  if (days < 7) return `${days}天前`;

  return d.format('YYYY-MM-DD');
};

const toAlertType = (m: Message): AlertLevel => {
  // 未读消息用更醒目样式
  if (!m.isRead && String(m.messageType || '').toUpperCase() === 'COUNSELOR_APPROVE') return 'critical';
  if (!m.isRead) return 'warning';
  return 'info';
};

const fetchAnomalyAlerts = async () => {
  anomalyLoading.value = true;
  try {
    const list = await getMessageList({ page: 1, size: 8 });

    const arr = (Array.isArray(list) ? list : []).map((m) => ({
      id: String(m.messageId),
      type: toAlertType(m),
      message: String(m.content || ''),
      time: formatRelativeTime(m.createdAt as any),
      category: messageTypeToCategory(m.messageType),

      messageId: m.messageId,
      isRead: !!m.isRead,
      relatedId: m.relatedId,
      messageType: m.messageType,
      createdAt: (m.createdAt as any) || '',
    }));

    anomalyAlerts.value = arr.length
      ? arr
      : [{ id: 'empty', type: 'info', message: '暂无消息', time: '', category: '系统消息' }];
  } catch (e: any) {
    anomalyAlerts.value = [{ id: 'error', type: 'info', message: '消息加载失败（请确认已登录且后端已启动）', time: '', category: '系统提示' }];
  } finally {
    anomalyLoading.value = false;
  }
};

const handleAlertClick = async (alert: AnomalyAlert) => {
  if (!alert.messageId || alert.isRead) return;

  try {
    await markAsRead(alert.messageId);
    alert.isRead = true;
    alert.type = 'info';
    ElMessage.success('已标记为已读');
  } catch (e: any) {
    ElMessage.error(e?.message || '标记已读失败');
  }
};


// --- 图表引用 ---
const pieChartRef = ref<HTMLElement | null>(null);
const barChartRef = ref<HTMLElement | null>(null);
let pieChartInstance: echarts.ECharts | null = null;
let barChartInstance: echarts.ECharts | null = null;

// --- 样式工具 ---
const getAlertStyle = (type: string) => {
  const map: Record<string, any> = {
    critical: { border: 'border-l-red-500 bg-red-50/50', tag: 'bg-red-100 text-red-700' },
    warning: { border: 'border-l-orange-500 bg-orange-50/50', tag: 'bg-orange-100 text-orange-700' },
    info: { border: 'border-l-blue-500 bg-blue-50/50', tag: 'bg-blue-100 text-blue-700' }
  };
  return map[type] || map.info;
};

const calcDateRange = () => {
  const d = dayjs();
  if (timeRange.value === 'week') {
    // 以周一为一周开始
    const dow = (d.day() + 6) % 7; // Mon=0 ... Sun=6
    const start = d.subtract(dow, 'day');
    const end = start.add(6, 'day');
    return { startDate: start.format('YYYY-MM-DD'), endDate: end.format('YYYY-MM-DD') };
  }
  if (timeRange.value === 'month') {
    return { startDate: d.startOf('month').format('YYYY-MM-DD'), endDate: d.endOf('month').format('YYYY-MM-DD') };
  }
  // semester：后端未提供学期起止（term）查询，本页先用“最近120天”作为近似窗口
  const start = d.subtract(120, 'day');
  return { startDate: start.format('YYYY-MM-DD'), endDate: d.format('YYYY-MM-DD') };
};

const updateCharts = async () => {
  await nextTick();

  // 饼图：类型分布（真实）
  if (pieChartInstance) {
    pieChartInstance.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        name: '请假类型',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 5, borderColor: '#fff', borderWidth: 2 },
        label: { show: false, position: 'center' },
        emphasis: { label: { show: true, fontSize: 18, fontWeight: 'bold' } },
        data: leaveTypeData.value.map(item => ({
          value: item.value,
          name: item.name,
          itemStyle: { color: item.color }
        }))
      }]
    }, true);
  }

  // 柱状图：由于后端仅提供汇总统计，本图先展示“汇总堆叠柱”（不做按天趋势）
  const dto = statsData.value;
  const sick = dto?.sickCount ?? 0;
  const affair = dto?.affairCount ?? 0;
  const pub = dto?.publicCount ?? 0;

  if (barChartInstance) {
    barChartInstance.setOption({
      title: {
        text: '统计汇总',
        subtext: '后端当前仅提供汇总统计（暂无按天趋势接口）',
        left: 'center',
        textStyle: { fontSize: 14 }
      },
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      legend: { bottom: 0 },
      grid: { left: '3%', right: '4%', bottom: '12%', top: '18%', containLabel: true },
      xAxis: { type: 'category', data: ['汇总'] },
      yAxis: { type: 'value' },
      series: [
        { name: '病假', type: 'bar', stack: 'total', data: [sick], itemStyle: { color: '#ef4444' } },
        { name: '事假', type: 'bar', stack: 'total', data: [affair], itemStyle: { color: '#f59e0b' } },
        { name: '公假', type: 'bar', stack: 'total', data: [pub], itemStyle: { color: '#3b82f6' } }
      ]
    }, true);
  }
};

const fetchStats = async () => {
  const classId = classNameToId[selectedClass.value];
  if (!classId) {
    statsData.value = null;
    leaveTypeData.value = [
      { name: '病假', value: 0, color: '#ef4444' },
      { name: '事假', value: 0, color: '#f59e0b' },
      { name: '公假', value: 0, color: '#3b82f6' }
    ];
    await updateCharts();
    if (selectedClass.value !== '计算机2101') {
      ElMessage.warning('当前后端未提供该班级统计（需要 classId 或班级列表接口）');
    }
    return;
  }

  statsLoading.value = true;
  try {
    const { startDate, endDate } = calcDateRange();
    const dto = await getClassLeaveStats({ classId, startDate, endDate });
    statsData.value = dto;

    leaveTypeData.value = [
      { name: '病假', value: dto.sickCount, color: '#ef4444' },
      { name: '事假', value: dto.affairCount, color: '#f59e0b' },
      { name: '公假', value: dto.publicCount, color: '#3b82f6' }
    ];

    currentTime.value = dayjs().format('YYYY-MM-DD HH:mm');
    await updateCharts();
  } catch (e: any) {
    statsData.value = null;
    ElMessage.error(e?.message || '获取统计数据失败');
  } finally {
    statsLoading.value = false;
  }
};

// --- 生命周期：初始化图表实例 + 拉取数据 ---
onMounted(async () => {
  if (pieChartRef.value) pieChartInstance = echarts.init(pieChartRef.value);
  if (barChartRef.value) barChartInstance = echarts.init(barChartRef.value);

  window.addEventListener('resize', handleResize);

  await fetchStats();
  await fetchAnomalyAlerts();
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  pieChartInstance?.dispose();
  barChartInstance?.dispose();
});

const handleResize = () => {
  pieChartInstance?.resize();
  barChartInstance?.resize();
};

// 筛选条件变化时重新拉取
watch([timeRange, selectedClass], () => {
  fetchStats();
});
</script>
