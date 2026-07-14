/**
 * Auth API — 认证相关接口
 *
 * 注册、登录返回 JWT token + 用户信息。
 * 调用方直接拿到解包后的业务数据（token / email / nickname）。
 */
import api from './client'

/**
 * 登录
 * @param {string} email
 * @param {string} password
 * @returns {Promise<{token: string, email: string, nickname: string}>}
 */
export async function login(email, password) {
  const res = await api.post('/auth/login', { email, password })
  return res.data
}

/**
 * 注册
 * @param {{ email: string, password: string, confirmPassword: string, nickname: string }} data
 * @returns {Promise<{token: string, email: string, nickname: string}>}
 */
export async function register(data) {
  const res = await api.post('/auth/register', data)
  return res.data
}

/**
 * 修改昵称
 * @param {string} nickname
 * @returns {Promise}
 */
export async function updateNickname(nickname) {
  return api.patch('/auth/nickname', { nickname })
}
