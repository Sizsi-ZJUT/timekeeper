/**
 * Vite 构建配置文件
 *
 * 功能：
 * - Vue SFC 编译
 * - PWA 离线支持（自动更新 Service Worker）
 * - @ 路径别名 → src/ 目录
 */
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { VitePWA } from 'vite-plugin-pwa'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [
    vue(),
    // PWA 配置：生成 Service Worker 和 manifest，支持离线访问
    VitePWA({
      registerType: 'autoUpdate', // 检测到新版本自动更新
      manifest: {
        name: 'TimeKeeper',
        short_name: 'TimeKeeper',
        description: '时间管理软件 - 记录学习与专注时长',
        theme_color: '#409eff',
        icons: [
          { src: 'pwa-192x192.png', sizes: '192x192', type: 'image/png' },
          { src: 'pwa-512x512.png', sizes: '512x512', type: 'image/png' }
        ]
      },
      workbox: {
        globPatterns: ['**/*.{js,css,html,ico,png,svg}']
      }
    })
  ],
  resolve: {
    alias: {
      // @ 指向 src 目录，避免深层相对路径 import
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  }
})
