/**
 * Categories API — 分类 CRUD
 */
import api from './client'

/**
 * 获取当前用户的所有分类
 * @returns {Promise<Array<{id: number, name: string, color: string}>>}
 */
export async function fetchCategories() {
  const res = await api.get('/categories')
  return res.data
}

/**
 * 添加分类
 * @param {string} name 分类名称（最长 50）
 * @param {string} color 颜色 hex 值
 * @returns {Promise<{id: number, name: string, color: string}>}
 */
export async function createCategory(name, color) {
  const res = await api.post('/categories', { name, color })
  return res.data
}

/**
 * 修改分类
 * @param {number} id 分类 ID
 * @param {{ name: string, color: string }} data
 * @returns {Promise<{id: number, name: string, color: string}>}
 */
export async function updateCategory(id, data) {
  const res = await api.put(`/categories/${id}`, data)
  return res.data
}

/**
 * 删除分类（关联记录不受影响）
 * @param {number} id 分类 ID
 */
export async function removeCategory(id) {
  return api.delete(`/categories/${id}`)
}
