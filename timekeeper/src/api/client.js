/**
 * Axios 实例 — 全局 API 客户端
 *
 * 职责：
 * - baseURL 指向后端 9090 端口，所有请求统一前缀 /api
 * - 请求拦截器：自动从 localStorage 读取 JWT，附加到 Authorization header
 * - 响应拦截器：自动解包 response.data（上层拿到的直接是响应体 JSON）
 * - 统一错误处理：401 清空登录态并跳转登录页，其他错误 ElMessage 提示
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: 'http://localhost:9090/api',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
})

/**
 * 请求拦截器 — 自动附加 JWT
 *
 * 每次请求前从 localStorage 读取 timekeeper_token，
 * 若存在则设置 Authorization: Bearer <token> header。
 */
api.interceptors.request.use(config => {
  const token = localStorage.getItem('timekeeper_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

/**
 * 响应拦截器 — 统一解包与错误处理
 *
 * 成功时：返回 response.data，上层无需再 .data
 * 失败时：
 *   - 401：清除本地登录态，跳转 /login
 *   - 其他 4xx/5xx：弹出后端返回的 message
 *   - 网络错误（无 response）：提示检查后端服务
 */
api.interceptors.response.use(
  response => response.data,
  error => {
    if (error.response) {
      const { status, data } = error.response
      if (status === 401) {
        localStorage.removeItem('timekeeper_token')
        localStorage.removeItem('timekeeper_current_user')
        window.location.href = '/login'
      } else if (data && data.message) {
        ElMessage.error(data.message)
      }
    } else {
      ElMessage.error('网络错误，请检查后端服务是否启动')
    }
    return Promise.reject(error)
  }
)

export default api
