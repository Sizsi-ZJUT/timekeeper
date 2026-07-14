/**
 * Records API — 记录 CRUD + 统计
 */
import api from './client'

/**
 * 按日期查询记录
 * @param {string} dateStr ISO 日期字符串 "YYYY-MM-DD"
 * @returns {Promise<Array>}
 */
export async function fetchRecordsByDate(dateStr) {
  const res = await api.get('/records', { params: { date: dateStr } })
  return res.data
}

/**
 * 按周查询记录（传入周内任意一天）
 * @param {string} dateStr ISO 日期字符串
 * @returns {Promise<Array>}
 */
export async function fetchRecordsByWeek(dateStr) {
  const res = await api.get('/records', { params: { week: dateStr } })
  return res.data
}

/**
 * 按月查询记录（传入月内任意一天）
 * @param {string} dateStr ISO 日期字符串
 * @returns {Promise<Array>}
 */
export async function fetchRecordsByMonth(dateStr) {
  const res = await api.get('/records', { params: { month: dateStr } })
  return res.data
}

/**
 * 加载当前用户全部记录
 * @returns {Promise<Array>}
 */
export async function fetchAllRecords() {
  const res = await api.get('/records')
  return res.data
}

/**
 * 添加计时记录
 * @param {{
 *   categoryId: number, categoryName: string, categoryColor: string,
 *   mode: string, startTime: string, endTime: string,
 *   durationSeconds: number, note?: string
 * }} data
 * @returns {Promise<Object>}
 */
export async function createRecord(data) {
  const res = await api.post('/records', data)
  return res.data
}

/**
 * 删除记录
 * @param {number} id 记录 ID
 */
export async function removeRecord(id) {
  return api.delete(`/records/${id}`)
}

/**
 * 修改记录（支持部分字段更新）
 * @param {number} id 记录 ID
 * @param {Object} data 要更新的字段键值对
 * @returns {Promise<Object>}
 */
export async function updateRecord(id, data) {
  const res = await api.put(`/records/${id}`, data)
  return res.data
}

/**
 * 每日统计（图表用）
 * @param {number} [days=7] 最近 N 天
 * @returns {Promise<Array<{date: string, seconds: number}>>}
 */
export async function fetchDailyStats(days = 7) {
  const res = await api.get('/records/stats/daily', { params: { days } })
  return res.data
}

/**
 * 分类统计（饼图用）
 * @param {string} [dateStr] 可选日期，不传则统计全部时间
 * @returns {Promise<Array<{name: string, color: string, seconds: number}>>}
 */
export async function fetchCategoryStats(dateStr) {
  const res = await api.get('/records/stats/categories', { params: { date: dateStr } })
  return res.data
}
