/**
 * 记录状态管理 (Pinia Store)
 *
 * 管理计时记录数据：支持按日/周/月/全部查询，提供统计接口。
 * 内部维护 records 数组作为当前视图的数据源（由 loadByXxx 方法填充）。
 *
 * 计算属性：
 *   - todayRecords：当前 records 中属于今天的记录（用于计时页实时显示）
 *   - todayTotalSeconds / todayCount：今日总时长和记录条数
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  fetchRecordsByDate, fetchRecordsByWeek, fetchRecordsByMonth, fetchAllRecords,
  createRecord, removeRecord, updateRecord, fetchDailyStats, fetchCategoryStats
} from '@/api/records'

export const useRecordStore = defineStore('records', () => {
  /** @type {import('vue').Ref<Array>} 当前已加载的记录列表 */
  const records = ref([])

  /** 今日记录（从已加载的 records 中筛选） */
  const todayRecords = computed(() => {
    const today = new Date().toDateString()
    return records.value.filter(r => new Date(r.startTime).toDateString() === today)
  })

  /** 今日总专注秒数 */
  const todayTotalSeconds = computed(() =>
    todayRecords.value.reduce((s, r) => s + r.durationSeconds, 0)
  )

  /** 今日记录条数 */
  const todayCount = computed(() => todayRecords.value.length)

  /**
   * 按日期加载记录
   * @param {string} dateStr "YYYY-MM-DD"
   */
  async function loadByDate(dateStr) {
    records.value = await fetchRecordsByDate(dateStr)
  }

  /**
   * 按周加载记录
   * @param {string} dateStr 周内任意一天
   */
  async function loadByWeek(dateStr) {
    records.value = await fetchRecordsByWeek(dateStr)
  }

  /**
   * 按月加载记录
   * @param {string} dateStr 月内任意一天
   */
  async function loadByMonth(dateStr) {
    records.value = await fetchRecordsByMonth(dateStr)
  }

  /** 加载全部记录 */
  async function loadAll() {
    records.value = await fetchAllRecords()
  }

  /**
   * 添加记录，插入到列表头部
   * @param {Object} data 记录数据
   * @returns {Promise<Object>} 创建后的记录
   */
  async function addRecord(data) {
    const created = await createRecord(data)
    records.value.unshift(created)
    return created
  }

  /**
   * 删除记录
   * @param {number} id 记录 ID
   */
  async function deleteRecord(id) {
    await removeRecord(id)
    records.value = records.value.filter(r => r.id !== id)
  }

  /**
   * 修改记录（部分字段更新）
   * @param {number} id 记录 ID
   * @param {Object} data 要更新的字段
   */
  async function updateRecordById(id, data) {
    const updated = await updateRecord(id, data)
    const idx = records.value.findIndex(r => r.id === id)
    if (idx !== -1) records.value[idx] = updated
  }

  /**
   * 从已加载的记录中按日期筛选（本地过滤，不发请求）
   * @param {Date} date
   * @returns {Array}
   */
  function getRecordsByDate(date) {
    const ds = date.toDateString()
    return records.value.filter(r => new Date(r.startTime).toDateString() === ds)
  }

  /**
   * 从已加载的记录中按周筛选（周一到周日）
   * @param {Date} date 周内任意一天
   * @returns {Array}
   */
  function getRecordsByWeek(date) {
    const d = new Date(date)
    const dayOfWeek = d.getDay()
    const monday = new Date(d)
    monday.setDate(d.getDate() - (dayOfWeek === 0 ? 6 : dayOfWeek - 1))
    monday.setHours(0, 0, 0, 0)
    const sunday = new Date(monday)
    sunday.setDate(monday.getDate() + 7)
    return records.value.filter(r => {
      const t = new Date(r.startTime).getTime()
      return t >= monday.getTime() && t < sunday.getTime()
    })
  }

  /**
   * 从已加载的记录中按月筛选
   * @param {Date} date 月内任意一天
   * @returns {Array}
   */
  function getRecordsByMonth(date) {
    const y = date.getFullYear()
    const m = date.getMonth()
    return records.value.filter(r => {
      const t = new Date(r.startTime)
      return t.getFullYear() === y && t.getMonth() === m
    })
  }

  /**
   * 每日统计（后端聚合，图表用）
   * @param {number} [days=7] 最近 N 天
   * @returns {Promise<Array<{date: string, seconds: number}>>}
   */
  async function getDailyStats(days = 7) {
    return fetchDailyStats(days)
  }

  /**
   * 分类统计（后端聚合，饼图用）
   * @param {string} [dateStr] 可选日期筛选
   * @returns {Promise<Array<{name: string, color: string, seconds: number}>>}
   */
  async function getCategoryStats(dateStr) {
    return fetchCategoryStats(dateStr)
  }

  /**
   * 格式化秒数为可读时长字符串
   * @param {number} seconds
   * @returns {string} 如 "1h 30m" 或 "45m" 或 "30s"
   */
  function formatDuration(seconds) {
    if (!seconds || seconds <= 0) return '0s'
    const h = Math.floor(seconds / 3600)
    const m = Math.floor((seconds % 3600) / 60)
    const s = seconds % 60
    const parts = []
    if (h > 0) parts.push(`${h}h`)
    if (m > 0) parts.push(`${m}m`)
    if (parts.length === 0 && s > 0) parts.push(`${s}s`)
    return parts.join(' ') || '0s'
  }

  return {
    records, todayRecords, todayTotalSeconds, todayCount,
    loadByDate, loadByWeek, loadByMonth, loadAll,
    addRecord, deleteRecord, updateRecord: updateRecordById,
    getRecordsByDate, getRecordsByWeek, getRecordsByMonth,
    getDailyStats, getCategoryStats, formatDuration
  }
})
