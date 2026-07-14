/**
 * 认证状态管理 (Pinia Store)
 *
 * 管理用户登录态：JWT token 存 localStorage，页面刷新后自动恢复。
 *
 * 数据流：
 *   登录/注册 → 后端返回 token + 用户信息 → 存入 localStorage → 跳转 /timer
 *   退出登录 → 清除 localStorage → 跳转 /login
 *
 * 关键设计：
 *   - token 的读写都在 Store 内完成，外部只调用 login/register/logout
 *   - user ref 从 localStorage 初始化，页面刷新不丢失登录态
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { login as apiLogin, register as apiRegister, updateNickname as apiUpdateNickname } from '@/api/auth'

const CURRENT_USER_KEY = 'timekeeper_current_user'
const TOKEN_KEY = 'timekeeper_token'

export const useAuthStore = defineStore('auth', () => {
  const router = useRouter()

  /** @type {import('vue').Ref<{email: string, nickname: string}|null>} */
  const user = ref(loadCurrentUser())
  /** @type {import('vue').Ref<string>} */
  const error = ref('')
  /** 是否已登录：user ref 非 null 即为已登录 */
  const isLoggedIn = computed(() => user.value !== null)

  /** 从 localStorage 恢复用户信息 */
  function loadCurrentUser() {
    const raw = localStorage.getItem(CURRENT_USER_KEY)
    console.log(raw)
    return raw ? JSON.parse(raw) : null
  }

  /** 持久化用户信息到 localStorage */
  function saveCurrentUser(u) {
    localStorage.setItem(CURRENT_USER_KEY, JSON.stringify(u))
  }

  /**
   * 登录：调用后端验证 → 存储 token → 跳转计时页
   * @param {string} email
   * @param {string} password
   * @returns {Promise<boolean>} 是否登录成功
   */
  async function login(email, password) {
    error.value = ''
    if (!email || !password) {
      error.value = '请填写邮箱和密码'
      return false
    }
    try {
      const { token, email: e, nickname } = await apiLogin(email, password)
      localStorage.setItem(TOKEN_KEY, token)
      const u = { email: e, nickname }
      saveCurrentUser(u)
      user.value = u
      router.push('/timer')
      return true
    } catch (err) {
      // 后端返回的 message 已由响应拦截器 ElMessage 弹出，这里只设置 error 供表单显示
      const msg = err.response?.data?.message
      error.value = msg || '登录失败'
      return false
    }
  }

  /**
   * 注册：表单验证 → 调用后端 → 自动登录
   * @param {string} email
   * @param {string} password
   * @param {string} confirmPassword 确认密码
   * @param {string} nickname
   * @returns {Promise<boolean>} 是否注册成功
   */
  async function register(email, password, confirmPassword, nickname) {
    error.value = ''
    if (!email || !password || !confirmPassword || !nickname) {
      error.value = '请填写所有字段'
      return false
    }
    if (password.length < 6 || password.length > 20) {
      error.value = '密码长度需在 6-20 位之间'
      return false
    }
    if (password !== confirmPassword) {
      error.value = '两次输入的密码不一致'
      return false
    }
    try {
      const { token, email: e, nickname: n } = await apiRegister({ email, password, confirmPassword, nickname })
      localStorage.setItem(TOKEN_KEY, token)
      const u = { email: e, nickname: n }
      saveCurrentUser(u)
      user.value = u
      router.push('/timer')
      return true
    } catch (err) {
      const msg = err.response?.data?.message
      error.value = msg || '注册失败'
      return false
    }
  }

  /** 退出登录：清除 token + 用户信息，跳转登录页 */
  function logout() {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(CURRENT_USER_KEY)
    user.value = null
    error.value = ''
    router.push('/login')
  }

  /** 清除错误信息（路由切换时调用） */
  function clearError() {
    error.value = ''
  }

  /**
   * 修改昵称：调用后端 → 更新本地状态
   * @param {string} nickname
   * @returns {Promise<boolean>}
   */
  async function updateNickname(nickname) {
    if (!user.value || !nickname.trim()) return false
    try {
      await apiUpdateNickname(nickname.trim())
      user.value.nickname = nickname.trim()
      saveCurrentUser(user.value)
      return true
    } catch {
      return false
    }
  }

  return {
    user, error, isLoggedIn,
    login, register, logout, clearError, updateNickname
  }
})
