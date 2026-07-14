<!--
  统计仪表盘页面

  功能：
  - 时间周期切换（今天 / 本周 / 本月）
  - 汇总卡片（总时长、日均、记录数）
  - ECharts 分类占比环形饼图
  - ECharts 每日趋势柱状图
  - 分类明细进度条列表
  - 响应式图表自适应

  图表使用 ECharts 原生 API 直接操作 DOM，不引入额外封装
-->
<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { useRecordStore } from '@/stores/records'

const recordStore = useRecordStore()

// ---- 时间筛选 ----
const period = ref('today')

function todayStr() {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function loadByPeriod() {
  const ds = todayStr()
  switch (period.value) {
    case 'today': recordStore.loadByDate(ds); break
    case 'week': recordStore.loadByWeek(ds); break
    case 'month': recordStore.loadByMonth(ds); break
  }
}

/**
 * 根据筛选周期获取记录子集
 */
const filteredRecords = computed(() => {
  const now = new Date()
  switch (period.value) {
    case 'today':
      return recordStore.getRecordsByDate(now)
    case 'week':
      return recordStore.getRecordsByWeek(now)
    case 'month':
      return recordStore.getRecordsByMonth(now)
    default:
      return []
  }
})

// ---- 汇总数据 ----
const summary = computed(() => {
  const records = filteredRecords.value
  const totalSeconds = records.reduce((sum, r) => sum + r.durationSeconds, 0)
  const days = period.value === 'today' ? 1 : period.value === 'week' ? 7 : 30
  return {
    total: recordStore.formatDuration(totalSeconds),
    avg: recordStore.formatDuration(records.length > 0 ? Math.round(totalSeconds / days) : 0),
    count: records.length
  }
})

// ---- 分类数据 + 每日趋势（异步获取）----
const categoryStats = ref([])
const dailyStats = ref([])

async function fetchStats() {
  const ds = todayStr()
  const days = period.value === 'today' ? 7 : period.value === 'week' ? 7 : 30
  try {
    const [catData, dailyData] = await Promise.all([
      recordStore.getCategoryStats(ds),
      recordStore.getDailyStats(days)
    ])
    categoryStats.value = catData
    dailyStats.value = dailyData
  } catch {
    categoryStats.value = []
    dailyStats.value = []
  }
}

// ---- ECharts 实例管理 ----
const pieChartRef = ref(null)
const barChartRef = ref(null)
let pieChart = null
let barChart = null

/**
 * 初始化分类饼图（环形图）
 */
function initPieChart() {
  if (!pieChartRef.value) return
  if (pieChart) pieChart.dispose()

  pieChart = echarts.init(pieChartRef.value)
  updatePieChart()
}

function updatePieChart() {
  if (!pieChart) return

  const data = categoryStats.value
  if (data.length === 0) {
    pieChart.setOption({
      title: { text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#909399', fontSize: 14 } },
      series: []
    })
    return
  }

  pieChart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: (params) => `${params.name}: ${recordStore.formatDuration(params.value)} (${params.percent}%)`
    },
    legend: { bottom: 0, textStyle: { fontSize: 12 } },
    series: [{
      type: 'pie',
      radius: ['50%', '75%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      data: data.map(d => ({ name: d.name, value: d.seconds, itemStyle: { color: d.color } }))
    }]
  })
}

/**
 * 初始化每日趋势柱状图
 */
function initBarChart() {
  if (!barChartRef.value) return
  if (barChart) barChart.dispose()

  barChart = echarts.init(barChartRef.value)
  updateBarChart()
}

function updateBarChart() {
  if (!barChart) return

  const data = dailyStats.value
  const hasData = data.some(d => d.seconds > 0)

  barChart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: (params) => `${params[0].name}: ${recordStore.formatDuration(params[0].value)}`
    },
    grid: { left: '8%', right: '8%', bottom: '10%', top: '10%' },
    xAxis: {
      type: 'category',
      data: data.map(d => d.date),
      axisLabel: { fontSize: 11, color: '#909399' }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        fontSize: 11,
        color: '#909399',
        formatter: (val) => {
          if (val >= 3600) return Math.floor(val / 3600) + 'h'
          if (val >= 60) return Math.floor(val / 60) + 'm'
          return val + 's'
        }
      },
      splitLine: { lineStyle: { color: '#f0f0f0' } }
    },
    series: [{
      type: 'bar',
      data: data.map((d, i) => ({
        value: d.seconds,
        itemStyle: {
          color: hasData && d.seconds > 0
            ? new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#409eff' },
              { offset: 1, color: '#a0cfff' }
            ])
            : '#e4e7ed',
          borderRadius: [4, 4, 0, 0]
        }
      })),
      barWidth: '50%'
    }]
  })
}

// 监听数据变化更新图表
watch([categoryStats, dailyStats], () => {
  nextTick(() => {
    updatePieChart()
    updateBarChart()
  })
})

// 切换周期时重新加载
watch(period, () => {
  loadByPeriod()
  fetchStats()
})

// 窗口自适应
function handleResize() {
  pieChart?.resize()
  barChart?.resize()
}

onMounted(async () => {
  loadByPeriod()
  await fetchStats()
  nextTick(() => {
    initPieChart()
    initBarChart()
  })
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  pieChart?.dispose()
  barChart?.dispose()
})
</script>

<template>
  <div class="page stats-page">
    <h1>统计仪表盘</h1>

    <!-- 时间筛选 -->
    <div class="period-filter">
      <el-radio-group v-model="period" size="small">
        <el-radio-button value="today">今天</el-radio-button>
        <el-radio-button value="week">本周</el-radio-button>
        <el-radio-button value="month">本月</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 汇总卡片 -->
    <div class="summary-cards">
      <div class="summary-card">
        <div class="card-value">{{ summary.total }}</div>
        <div class="card-label">总时长</div>
      </div>
      <div class="summary-card">
        <div class="card-value">{{ summary.avg }}</div>
        <div class="card-label">日均</div>
      </div>
      <div class="summary-card">
        <div class="card-value">{{ summary.count }}</div>
        <div class="card-label">记录数</div>
      </div>
    </div>

    <!-- 图表区 -->
    <div class="charts-row">
      <div class="chart-box">
        <h3 class="chart-title">分类占比</h3>
        <div ref="pieChartRef" class="chart-container"></div>
      </div>
      <div class="chart-box">
        <h3 class="chart-title">每日趋势</h3>
        <div ref="barChartRef" class="chart-container"></div>
      </div>
    </div>

    <!-- 分类明细 -->
    <div class="category-breakdown">
      <h3 class="chart-title">分类明细</h3>
      <div v-if="categoryStats.length === 0" class="empty-hint">暂无数据</div>
      <div v-for="cat in categoryStats" :key="cat.name" class="breakdown-item">
        <div class="breakdown-header">
          <span class="breakdown-dot" :style="{ background: cat.color }"></span>
          <span class="breakdown-name">{{ cat.name }}</span>
          <span class="breakdown-time">{{ recordStore.formatDuration(cat.seconds) }}</span>
          <span class="breakdown-pct">
            {{ summary.count > 0 ? Math.round(cat.seconds / filteredRecords.reduce((s, r) => s + r.durationSeconds, 0) * 100) : 0 }}%
          </span>
        </div>
        <el-progress
          :percentage="filteredRecords.reduce((s, r) => s + r.durationSeconds, 0) > 0
            ? Math.round(cat.seconds / filteredRecords.reduce((s, r) => s + r.durationSeconds, 0) * 100)
            : 0"
          :color="cat.color"
          :stroke-width="8"
          :show-text="false"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.stats-page {
  max-width: 900px;
}

.period-filter {
  margin-bottom: 20px;
}

/* ---- 汇总卡片 ---- */
.summary-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.summary-card {
  background: #fff;
  border-radius: 10px;
  padding: 20px;
  text-align: center;
  border: 1px solid #e4e7ed;
}

.card-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 4px;
}

.card-label {
  font-size: 13px;
  color: #909399;
}

/* ---- 图表区 ---- */
.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 24px;
}

.chart-box {
  background: #fff;
  border-radius: 10px;
  padding: 16px;
  border: 1px solid #e4e7ed;
}

.chart-title {
  font-size: 15px;
  font-weight: 500;
  margin-bottom: 12px;
  color: #303133;
}

.chart-container {
  width: 100%;
  height: 280px;
}

/* ---- 分类明细 ---- */
.category-breakdown {
  background: #fff;
  border-radius: 10px;
  padding: 20px;
  border: 1px solid #e4e7ed;
}

.breakdown-item {
  margin-bottom: 16px;
}

.breakdown-item:last-child {
  margin-bottom: 0;
}

.breakdown-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
  font-size: 14px;
}

.breakdown-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.breakdown-name {
  font-weight: 500;
  color: #303133;
}

.breakdown-time {
  margin-left: auto;
  color: #606266;
}

.breakdown-pct {
  color: #909399;
  font-size: 13px;
  min-width: 36px;
  text-align: right;
}

.empty-hint {
  text-align: center;
  color: #909399;
  padding: 20px 0;
}

/* 移动端：图表单列 */
@media (max-width: 640px) {
  .charts-row {
    grid-template-columns: 1fr;
  }

  .summary-cards {
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;
  }

  .card-value {
    font-size: 22px;
  }
}
</style>
