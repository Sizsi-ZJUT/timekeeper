<!--
  注册页面

  使用 Element Plus 表单组件，包含：
  - 昵称 + 邮箱 + 密码 + 确认密码输入
  - 前端校验（邮箱格式、密码长度 6-20 位、两次密码一致）
  - 调用 auth store register 方法
  - 错误信息展示
  - 跳转登录页链接

  页面不包裹 MainLayout（路由 meta.noLayout = true）
-->
<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()

const formRef = ref(null)

const form = reactive({
  nickname: '',
  email: '',
  password: '',
  confirmPassword: ''
})

// 校验确认密码：必须与密码字段一致
const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 1, max: 20, message: '昵称长度在 1-20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度需在 6-20 位之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

/**
 * 提交注册
 * 表单校验通过后调用 store 注册，成功后自动跳转首页
 */
function handleRegister() {
  formRef.value?.validate((valid) => {
    if (!valid) return
    authStore.register(
      form.email,
      form.password,
      form.confirmPassword,
      form.nickname
    )
  })
}

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
        <p class="auth-desc">创建新账号</p>
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

      <!-- 注册表单 -->
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @submit.prevent="handleRegister"
      >
        <el-form-item label="昵称" prop="nickname">
          <el-input
            v-model="form.nickname"
            placeholder="你的昵称"
            :prefix-icon="User"
            size="large"
            @input="handleInput"
          />
        </el-form-item>

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
            placeholder="6-20 位密码"
            :prefix-icon="Lock"
            size="large"
            show-password
            @input="handleInput"
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="再次输入密码"
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
            @click="handleRegister"
          >
            注 册
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 底部链接 -->
      <div class="auth-footer">
        已有账号？<router-link to="/login">去登录</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #e8f4fd 0%, #f5f7fa 100%);
  padding: 24px;
}

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
