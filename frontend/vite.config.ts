import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      // 这里就是告诉 Vite：看到 "@" 就自动找到 "src" 目录
      '@': path.resolve(__dirname, 'src')
    }
  },
  // ✅ 开发环境代理：让前端直接请求 /api /admin，不用额外配 .env
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/admin': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      }
    }
  }
})
