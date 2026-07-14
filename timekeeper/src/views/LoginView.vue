<!--
  登录页面

  使用 Element Plus 表单组件，包含：
  - 邮箱 + 密码输入
  - 前端校验（邮箱格式、非空）
  - 调用 auth store login 方法
  - 错误信息展示
  - 跳转注册页链接

  页面不包裹 MainLayout（路由 meta.noLayout = true）
-->
<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()

// 表单引用，用于调用 validate 方法
const formRef = ref(null)

// 表单数据
const form = reactive({
  email: '',
  password: ''
})

// 自定义校验规则
const rules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

/**
 * 提交登录
 * 先通过 Element Plus 表单校验，再调用 store 登录
 */
function handleLogin() {
  formRef.value?.validate((valid) => {
    if (!valid) return
    authStore.login(form.email, form.password)
  })
}

/**
 * 表单值变化时清除之前的错误
 */
function handleInput() {
  authStore.clearError()
}
</script>

<template>
  <div class="auth-container">
    <div class="auth-card">
      <!-- Logo 区域 -->
      <div class="auth-header">
        <h1 class="auth-title">TimeKeeper</h1>
        <p class="auth-desc">登录你的账号</p>
      </div>

      <!-- 错误提示 -->
      <el-alert
        v-if="authStore.error"
        :title="authStore.error"
        type="error"
        show-icon
        :closable="false"
        class="auth-error"
      />

      <!-- 登录表单 -->
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @submit.prevent="handleLogin"
      >
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="form.email"
            placeholder="请输入邮箱"
            :prefix-icon="User"
            size="large"
            @input="handleInput"
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            size="large"
            show-password
            @input="handleInput"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="auth-btn"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 底部链接 -->
      <div class="auth-footer">
        没有账号？<router-link to="/register">去注册</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 外层容器：全屏居中 */
.auth-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #e8f4fd 0%, #f5f7fa 100%);
  padding: 24px;
}

/* 卡片样式 */
.auth-card {
  width: 100%;
  max-width: 400px;
  background: #fff;
  border-radius: 12px;
  padding: 40px 32px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
}

.auth-header {
  text-align: center;
  margin-bottom: 32px;
}

.auth-title {
  font-size: 28px;
  font-weight: 700;
  color: #409eff;
  margin-bottom: 8px;
}

.auth-desc {
  font-size: 14px;
  color: #909399;
}

.auth-error {
  margin-bottom: 16px;
}

/* 登录按钮全宽 */
.auth-btn {
  width: 100%;
}

.auth-footer {
  text-align: center;
  font-size: 14px;
  color: #909399;
}

.auth-footer a {
  color: #409eff;
  text-decoration: none;
}
</style>
