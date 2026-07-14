<!--
  MainLayout - 主布局组件

  桌面端（≥768px）：左侧可折叠侧边栏 + 右侧内容区
  移动端（<768px）：顶部标题栏 + 内容区 + 底部导航栏

  侧边栏使用 Element Plus 的 el-menu，collapse 模式显示图标
  移动端底部导航用 router-link 实现，active 状态跟随当前路由
-->
<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { Timer, Clock, DataAnalysis, Calendar, Flag, Setting, SwitchButton, User } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const authStore = useAuthStore()

// 侧边栏折叠状态，桌面端可切换以释放横向空间
const isCollapsed = ref(false)
// 768px 为移动端/桌面端断点，与 Element Plus 响应式规范一致
const isMobile = ref(window.innerWidth < 768)

// 导航菜单项定义，icon 为 Element Plus Icons 组件
const menuItems = [
  { path: '/timer', title: '计时器', icon: Timer },
  { path: '/records', title: '历史记录', icon: Clock },
  { path: '/statistics', title: '统计', icon: DataAnalysis },
  { path: '/calendar', title: '日历', icon: Calendar },
  { path: '/goals', title: '目标', icon: Flag },
  { path: '/settings', title: '设置', icon: Setting }
]

// 当前激活菜单项，用于高亮显示
const activeMenu = computed(() => route.path)

function toggleCollapse() {
  isCollapsed.value = !isCollapsed.value
}

/**
 * 退出登录：弹出确认框，确认后调用 store.logout()
 */
function handleLogout() {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    authStore.logout()
  }).catch(() => {
    // 用户取消，不执行任何操作
  })
}

// 监听窗口大小变化，实时切换移动/桌面布局
window.addEventListener('resize', () => {
  isMobile.value = window.innerWidth < 768
})
</script>

<template>
  <div class="layout" :class="{ 'layout--mobile': isMobile }">
    <!-- 移动端顶部栏 -->
    <header v-if="isMobile" class="mobile-header">
      <span class="logo">TimeKeeper</span>
      <el-button
        :icon="SwitchButton"
        text
        size="small"
        class="mobile-logout"
        @click="handleLogout"
      />
    </header>

    <!-- 桌面端侧边栏 -->
    <aside
      v-if="!isMobile"
      class="sidebar"
      :class="{ 'sidebar--collapsed': isCollapsed }"
    >
      <div class="sidebar-header">
        <!-- 折叠时隐藏 logo 文字，仅显示折叠按钮 -->
        <span v-if="!isCollapsed" class="logo">TimeKeeper</span>
        <el-button
          :icon="isCollapsed ? 'Expand' : 'Fold'"
          text
          @click="toggleCollapse"
        />
      </div>

      <!-- router 属性开启路由模式，点击自动导航 -->
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapsed"
        router
        class="sidebar-menu"
      >
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </el-menu>

      <!-- 侧边栏底部：用户信息 + 退出 -->
      <div class="sidebar-footer">
        <div v-if="!isCollapsed" class="user-info">
          <el-icon><User /></el-icon>
          <span class="user-name">{{ authStore.user?.nickname || '用户' }}</span>
        </div>
        <el-button
          :icon="SwitchButton"
          text
          :title="isCollapsed ? '退出登录' : ''"
          @click="handleLogout"
        >
          <template v-if="!isCollapsed">退出</template>
        </el-button>
      </div>
    </aside>

    <!-- 内容区域：通过 slot 接收父组件传入的 router-view -->
    <main class="main">
      <slot />
    </main>

    <!-- 移动端底部导航栏 -->
    <nav v-if="isMobile" class="bottom-nav">
      <router-link
        v-for="item in menuItems"
        :key="item.path"
        :to="item.path"
        class="bottom-nav-item"
        :class="{ active: activeMenu === item.path }"
      >
        <el-icon><component :is="item.icon" /></el-icon>
        <span>{{ item.title }}</span>
      </router-link>
    </nav>
  </div>
</template>

<style scoped>
.layout {
  display: flex;
  min-height: 100vh;
}

/* ---- 侧边栏 ---- */
.sidebar {
  background: #fff;
  border-right: 1px solid #e4e7ed;
  transition: width 0.3s;
  width: 220px;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.sidebar--collapsed {
  width: 64px;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid #e4e7ed;
  height: 60px;
}

.sidebar--collapsed .sidebar-header {
  justify-content: center;
  padding: 16px 8px;
}

.logo {
  font-size: 18px;
  font-weight: 700;
  color: #409eff;
}

.sidebar-menu {
  border-right: none;
  flex: 1;
}

/* ---- 主内容区 ---- */
.main {
  flex: 1;
  background: #f5f7fa;
  overflow-y: auto;
}

/* ---- 移动端 ---- */
.mobile-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 48px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  z-index: 100;
}

/* 移动端需要给主内容区留出顶部栏和底部导航的空间 */
.layout--mobile .main {
  padding-top: 48px;
  padding-bottom: 56px;
}

.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 56px;
  background: #fff;
  border-top: 1px solid #e4e7ed;
  display: flex;
  z-index: 100;
}

.bottom-nav-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #909399;
  text-decoration: none;
  gap: 2px;
}

.bottom-nav-item.active {
  color: #409eff;
}

/* ---- 侧边栏底部用户区 ---- */
.sidebar-footer {
  border-top: 1px solid #e4e7ed;
  padding: 12px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.sidebar--collapsed .sidebar-footer {
  justify-content: center;
  padding: 12px 8px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #303133;
  overflow: hidden;
}

.user-name {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 120px;
}
</style>
