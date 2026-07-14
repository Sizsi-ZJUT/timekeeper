/**
 * Preferences API — 计时偏好读写
 *
 * 用户首次访问时后端自动创建默认值（番茄工作 25min / 休息 5min / 倒计时 25min）。
 */
import api from './client'

/**
 * 获取当前用户的计时偏好
 * @returns {Promise<{pomodoroWorkMinutes: number, pomodoroBreakMinutes: number, defaultCountdownMinutes: number}>}
 */
export async function fetchPreferences() {
  const res = await api.get('/preferences')
  return res.data
}

/**
 * 更新偏好（只传需要修改的字段）
 * @param {{ pomodoroWorkMinutes?: number, pomodoroBreakMinutes?: number, defaultCountdownMinutes?: number }} data
 * @returns {Promise<Object>}
 */
export async function updatePreferences(data) {
  const res = await api.put('/preferences', data)
  return res.data
}

/**
 * 恢复默认偏好（25 / 5 / 25）
 * @returns {Promise<Object>}
 */
export async function resetPreferences() {
  const res = await api.post('/preferences/reset')
  return res.data
}
