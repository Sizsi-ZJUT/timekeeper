/**
 * 目标状态管理 (Pinia Store)
 *
 * 管理用户的时长目标（每日/每周），支持按分类或全部设置目标。
 * getProgress 异步获取后端计算的完成进度。
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { fetchGoals, createGoal, updateGoal, removeGoal, fetchGoalProgress } from '@/api/goals'
import { useCategoryStore } from '@/stores/categories'

export const useGoalStore = defineStore('goals', () => {
  const goals = ref([])

  /** 从后端加载所有目标 */
  async function loadGoals() {
    goals.value = await fetchGoals()
  }

  /**
   * 添加目标
   * @param {{ categoryId: number|null, period: string, targetHours: number }} data
   * @returns {Promise<Object>}
   */
  async function addGoal(data) {
    const created = await createGoal(data)
    goals.value.push(created)
    return created
  }

  /**
   * 修改目标
   * @param {number} id
   * @param {{ categoryId: number|null, period: string, targetHours: number }} data
   */
  async function updateGoalById(id, data) {
    const updated = await updateGoal(id, data)
    const idx = goals.value.findIndex(g => g.id === id)
    if (idx !== -1) goals.value[idx] = updated
  }

  /**
   * 删除目标
   * @param {number} id
   */
  async function deleteGoal(id) {
    await removeGoal(id)
    goals.value = goals.value.filter(g => g.id !== id)
  }

  /**
   * 获取目标进度（异步，返回 currentSeconds / targetSeconds / percentage）
   * @param {Object} goal 目标对象（需含 id 字段）
   * @returns {Promise<{currentSeconds: number, targetSeconds: number, percentage: number}>}
   */
  async function getProgress(goal) {
    return fetchGoalProgress(goal.id)
  }

  /**
   * 获取目标关联的分类信息（名称 + 颜色）
   * categoryId 为 null 时返回「全部」分类的默认样式
   * @param {Object} goal
   * @returns {{ name: string, color: string }}
   */
  function getGoalCategoryInfo(goal) {
    if (!goal.categoryId) {
      return { name: '全部', color: '#409eff' }
    }
    const categoryStore = useCategoryStore()
    const cat = categoryStore.categories.find(c => c.id === goal.categoryId)
    return cat ? { name: cat.name, color: cat.color } : { name: '未知', color: '#909399' }
  }

  return { goals, loadGoals, addGoal, updateGoal: updateGoalById, deleteGoal, getProgress, getGoalCategoryInfo }
})
