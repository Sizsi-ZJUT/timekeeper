/**
 * 计时偏好状态管理 (Pinia Store)
 *
 * 管理番茄钟和倒计时的默认配置。
 * 后端在用户首次访问时自动创建默认值，前端仅在网络异常时使用本地默认值兜底。
 *
 * 默认值：
 *   - 番茄工作：25 分钟
 *   - 番茄休息：5 分钟
 *   - 倒计时：  25 分钟
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { fetchPreferences, updatePreferences, resetPreferences } from '@/api/preferences'

/** 前端默认值 —— 仅在后端请求失败时使用 */
const DEFAULTS = {
  pomodoroWorkMinutes: 25,
  pomodoroBreakMinutes: 5,
  defaultCountdownMinutes: 25
}

export const usePreferenceStore = defineStore('preferences', () => {
  const preferences = ref({ ...DEFAULTS })

  /** 从后端加载偏好，失败则保持默认值 */
  async function loadPreferences() {
    try {
      preferences.value = await fetchPreferences()
    } catch {
      // 网络异常保持默认值
    }
  }

  /**
   * 更新偏好（只传需修改的字段）
   * @param {Object} data
   */
  async function updatePrefs(data) {
    preferences.value = await updatePreferences(data)
  }

  /** 恢复默认偏好（25 / 5 / 25） */
  async function resetPrefs() {
    preferences.value = await resetPreferences()
  }

  return { preferences, loadPreferences, updatePreferences: updatePrefs, resetPreferences: resetPrefs }
})
