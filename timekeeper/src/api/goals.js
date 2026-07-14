/**
 * Goals API — 目标 CRUD + 进度
 */
import api from './client'

/**
 * 获取当前用户的所有目标
 * @returns {Promise<Array>}
 */
export async function fetchGoals() {
  const res = await api.get('/goals')
  return res.data
}

/**
 * 添加目标
 * @param {{ categoryId: number|null, period: string, targetHours: number }} data
 *   categoryId 为 null 表示「全部」分类
 * @returns {Promise<Object>}
 */
export async function createGoal(data) {
  const res = await api.post('/goals', data)
  return res.data
}

/**
 * 修改目标
 * @param {number} id 目标 ID
 * @param {{ categoryId: number|null, period: string, targetHours: number }} data
 * @returns {Promise<Object>}
 */
export async function updateGoal(id, data) {
  const res = await api.put(`/goals/${id}`, data)
  return res.data
}

/**
 * 删除目标
 * @param {number} id 目标 ID
 */
export async function removeGoal(id) {
  return api.delete(`/goals/${id}`)
}

/**
 * 获取目标进度（实际时长 / 目标时长）
 * @param {number} id 目标 ID
 * @returns {Promise<{currentSeconds: number, targetSeconds: number, percentage: number}>}
 */
export async function fetchGoalProgress(id) {
  const res = await api.get(`/goals/${id}/progress`)
  return res.data
}
