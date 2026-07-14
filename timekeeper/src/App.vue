<!--
  根组件
  根据路由 meta.noLayout 决定是否包裹 MainLayout
  - 登录/注册页：noLayout=true，不显示侧边栏
  - 其他页面：显示完整布局（侧边栏 + 内容区）
-->
<script setup>
import { useRoute } from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'

const route = useRoute()
</script>

<template>
  <!-- 需要布局的页面用 MainLayout 包裹 -->
  <MainLayout v-if="!route.meta.noLayout">
    <router-view />
  </MainLayout>
  <!-- 不需要布局的页面（登录/注册）直接渲染 -->
  <router-view v-else />
</template>
