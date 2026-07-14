/**
 * 分类状态管理 (Pinia Store)
 *
 * 管理用户的计时分类（学习、运动、工作等），每个分类有名称和颜色。
 * 维护一个当前选中分类，供计时页面使用。
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { fetchCategories, createCategory, updateCategory, removeCategory } from '@/api/categories'

export const useCategoryStore = defineStore('categories', () => {
  const categories = ref([])
  /** 当前选中分类 ID，用于计时页面切换分类 */
  const selectedCategoryId = ref('')

  /** 根据 selectedCategoryId 实时计算选中分类对象 */
  const selectedCategory = computed(() =>
    categories.value.find(c => String(c.id) === String(selectedCategoryId.value)) || null
  )

  /**
   * 从后端加载分类列表，首次加载自动选中第一个
   */
  async function loadCategories() {
    try {
      categories.value = await fetchCategories()
      if (!selectedCategoryId.value && categories.value.length > 0) {
        selectedCategoryId.value = String(categories.value[0].id)
      }
    } catch {
      // 静默失败：网络异常时保持旧数据，不中断用户操作
    }
  }

  /**
   * 添加分类
   * @param {string} name
   * @param {string} color
   * @returns {Promise<Object>} 创建的分类对象
   */
  async function addCategory(name, color) {
    const data = await createCategory(name, color)
    categories.value.push(data)
    if (!selectedCategoryId.value) {
      selectedCategoryId.value = String(data.id)
    }
    return data
  }

  /**
   * 修改分类名称或颜色
   * @param {number} id
   * @param {{ name: string, color: string }} data
   */
  async function updateCategoryById(id, data) {
    const updated = await updateCategory(id, data)
    const idx = categories.value.findIndex(c => c.id === id)
    if (idx !== -1) {
      categories.value[idx] = updated
    }
  }

  /**
   * 删除分类（关联记录不受影响）
   * @param {number} id
   */
  async function deleteCategory(id) {
    await removeCategory(id)
    categories.value = categories.value.filter(c => c.id !== id)
    // 如果删除的是当前选中分类，自动切换到第一个
    if (String(selectedCategoryId.value) === String(id) && categories.value.length > 0) {
      selectedCategoryId.value = String(categories.value[0].id)
    }
  }

  /** 手动切换选中分类 */
  function selectCategory(id) {
    selectedCategoryId.value = id
  }

  return {
    categories, selectedCategoryId, selectedCategory,
    loadCategories, addCategory, updateCategory: updateCategoryById, deleteCategory, selectCategory
  }
})
