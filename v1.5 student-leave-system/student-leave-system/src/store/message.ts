// src/store/message.ts
import { defineStore } from 'pinia';
import { getUnreadCount } from '@/api/messages';

export const useMessageStore = defineStore('message', {
  state: () => ({
    unreadCount: 0 as number,
    loading: false,
  }),

  actions: {
    async refreshUnreadCount() {
      this.loading = true;
      try {
        const n = await getUnreadCount();
        const v = Number(n);
        this.unreadCount = Number.isFinite(v) && v >= 0 ? v : 0;
      } catch {
        // 后端未启动/网络错误时，不影响页面显示
        this.unreadCount = this.unreadCount ?? 0;
      } finally {
        this.loading = false;
      }
    },
  },
});
