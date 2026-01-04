<template>
  <!-- 登录页不渲染主框架（避免未登录时就开始调 unread-count 之类接口） -->
  <div v-if="isLoginPage" class="min-h-screen bg-gray-50">
    <router-view />
  </div>

  <div v-else class="h-screen flex flex-col bg-gray-50">
    <header class="h-16 bg-white border-b border-gray-200 flex items-center justify-between px-6 shadow-sm flex-shrink-0 z-20">
      <div class="flex items-center gap-4">
        <h1 class="text-blue-600 font-bold text-lg hidden md:block">大学生请假管理平台</h1>
      </div>

      <div class="flex items-center gap-4">
        <div class="text-right hidden sm:block">
          <div class="text-sm font-medium">{{ userStore.userInfo.name }}</div>
          <div class="text-xs text-gray-500">{{ roleNames[userStore.currentRole] }}</div>
        </div>

        <div :class="['w-9 h-9 rounded-full flex items-center justify-center text-white font-medium shadow-sm', userStore.userInfo.avatarColor]">
          {{ userStore.userInfo.name.charAt(0) }}
        </div>

        <el-button size="small" @click="handleLogout">退出</el-button>
      </div>
    </header>

    <div class="flex flex-1 overflow-hidden">
      <aside class="w-64 bg-white border-r border-gray-200 flex flex-col transition-all duration-300">
        <nav class="p-4 space-y-1">
          <router-link
            v-for="item in currentMenus"
            :key="item.path"
            :to="item.path"
            class="flex items-center gap-3 px-4 py-3 rounded-lg transition-all duration-200 text-sm font-medium group"
            :class="route.path === item.path ? 'bg-blue-50 text-blue-700 shadow-sm border border-blue-100' : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'"
          >
            <component
              :is="item.icon"
              class="w-5 h-5 transition-colors"
              :class="route.path === item.path ? 'text-blue-600' : 'text-gray-400 group-hover:text-gray-600'"
            />
            <span class="flex-1">{{ item.label }}</span>

            <span
              v-if="item.path === '/counselor/statistics' && unreadCount > 0"
              class="ml-auto px-2 py-0.5 text-xs font-semibold rounded-full bg-red-500 text-white"
            >
              {{ unreadCount > 99 ? '99+' : unreadCount }}
            </span>
          </router-link>
        </nav>
      </aside>

      <main class="flex-1 overflow-auto bg-gray-50/50 p-6 relative">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onUnmounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/store/user';
import { useMessageStore } from '@/store/message';
import { roleMenus } from '@/config/menu';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const messageStore = useMessageStore();

const isLoginPage = computed(() => route.path === '/login');

const roleNames = {
  student: '学生',
  counselor: '辅导员',
  teacher: '任课老师',
  admin: '管理员',
} as const;

const unreadCount = computed(() => messageStore.unreadCount);
const currentMenus = computed(() => roleMenus[userStore.currentRole] || []);

let unreadTimer: number | null = null;

const stopUnreadPolling = () => {
  if (unreadTimer) {
    clearInterval(unreadTimer);
    unreadTimer = null;
  }
};

const startUnreadPolling = () => {
  stopUnreadPolling();
  unreadTimer = window.setInterval(() => {
    messageStore.refreshUnreadCount();
  }, 10000);
};

// ✅ 根据是否登录页/是否有 token 决定是否轮询消息
watch([isLoginPage, () => userStore.token], async ([loginPage, token]) => {
  stopUnreadPolling();
  if (!loginPage && token) {
    await messageStore.refreshUnreadCount();
    startUnreadPolling();
  }
}, { immediate: true });

onUnmounted(() => {
  stopUnreadPolling();
});

const handleLogout = async () => {
  await userStore.logout();
  stopUnreadPolling();
  router.replace('/login');
};
</script>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
