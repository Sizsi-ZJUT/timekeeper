<!--
  日历视图页面

  GitHub 风格热力图日历：
  - 月份导航（左右箭头切换）
  - 7×6 网格，每天一个格子，颜色深浅表示时长
  - 5 级颜色（0 / <30m / 30m-1h / 1h-2h / 2h+）
  - 点击日期查看当日记录详情
  - 底部月度汇总（总时长 + 记录天数）
  - 颜色图例
-->
<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRecordStore } from '@/stores/records'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'

const recordStore = useRecordStore()

// 当前查看的年月
const currentYear = ref(new Date().getFullYear())
const currentMonth = ref(new Date().getMonth() + 1) // 1-12

function loadMonth() {
  const ds = `${currentYear.value}-${String(currentMonth.value).padStart(2, '0')}-01`
  recordStore.loadByMonth(ds)
}

onMounted(() => {
  loadMonth()
})

watch([currentYear, currentMonth], () => {
  loadMonth()
})

// 选中的日期（用于显示详情）
const selectedDate = ref(null)
const selectedDateRecords = ref([])

// 今天日期
const today = new Date()

/**
 * 月份名称
 */
const monthLabel = computed(() => `${currentYear.value}年${currentMonth.value}月`)

/**
 * 生成日历网格数据
 * 返回 6 行 × 7 列的二维数组
 */
const calendarGrid = computed(() => {
  const firstDay = new Date(currentYear.value, currentMonth.value - 1, 1)
  const daysInMonth = new Date(currentYear.value, currentMonth.value, 0).getDate()
  const startDayOfWeek = firstDay.getDay() // 0=周日

  const grid = []
  let dayCounter = 1
  let weekIndex = 0

  // 最多 6 行
  while (dayCounter <= daysInMonth && weekIndex < 6) {
    const week = []
    for (let dow = 0; dow < 7; dow++) {
      if ((weekIndex === 0 && dow < startDayOfWeek) || dayCounter > daysInMonth) {
        // 空白填充
        week.push(null)
      } else {
        const date = new Date(currentYear.value, currentMonth.value - 1, dayCounter)
        const dayRecords = recordStore.getRecordsByDate(date)
        const seconds = dayRecords.reduce((s, r) => s + r.durationSeconds, 0)
        week.push({
          day: dayCounter,
          date: date,
          seconds,
          isToday: date.toDateString() === today.toDateString(),
          isSelected: selectedDate.value ? date.toDateString() === selectedDate.value.toDateString() : false
        })
        dayCounter++
      }
    }
    if (week.some(d => d !== null)) {
      grid.push(week)
    }
    weekIndex++
  }

  return grid
})

/**
 * 根据时长秒数返回热力图颜色
 * 0 / <30m / 30m-1h / 1h-2h / 2h+
 */
function getHeatColor(seconds) {
  if (!seconds || seconds <= 0) return '#f0f0f0'
  if (seconds < 1800) return '#c6e48b'       // < 30 分钟
  if (seconds < 3600) return '#7bc96f'       // 30 分钟 - 1 小时
  if (seconds < 7200) return '#239a3b'       // 1 - 2 小时
  return '#196127'                            // 2 小时以上
}

/**
 * 获取格子文字颜色
 * 深色背景用白色文字
 */
function getTextColor(seconds) {
  if (!seconds || seconds <= 0) return '#606266'
  if (seconds < 3600) return '#303133'
  return '#fff'
}

/**
 * 点击日期
 */
function selectDate(day) {
  if (!day) return
  // 取消选中
  if (selectedDate.value && day.date.toDateString() === selectedDate.value.toDateString()) {
    selectedDate.value = null
    selectedDateRecords.value = []
    return
  }
  selectedDate.value = day.date
  selectedDateRecords.value = recordStore.getRecordsByDate(day.date)
}

/**
 * 选中日期总时长
 */
const selectedTotal = computed(() =>
  selectedDateRecords.value.reduce((s, r) => s + r.durationSeconds, 0)
)

/**
 * 月度汇总
 */
const monthSummary = computed(() => {
  const monthRecords = recordStore.getRecordsByMonth(new Date(currentYear.value, currentMonth.value - 1, 1))
  const totalSeconds = monthRecords.reduce((s, r) => s + r.durationSeconds, 0)
  // 统计有记录的天数
  const daySet = new Set()
  monthRecords.forEach(r => {
    daySet.add(new Date(r.startTime).toDateString())
  })
  return {
    total: recordStore.formatDuration(totalSeconds),
    days: daySet.size
  }
})

/**
 * 月份导航
 */
function prevMonth() {
  if (currentMonth.value === 1) {
    currentMonth.value = 12
    currentYear.value--
  } else {
    currentMonth.value--
  }
  selectedDate.value = null
  selectedDateRecords.value = []
}

function nextMonth() {
  if (currentMonth.value === 12) {
    currentMonth.value = 1
    currentYear.value++
  } else {
    currentMonth.value++
  }
  selectedDate.value = null
  selectedDateRecords.value = []
}

// 星期头
const weekdays = ['日', '一', '二', '三', '四', '五', '六']

// 图例
const legend = [
  { label: '0', color: '#f0f0f0' },
  { label: '<30m', color: '#c6e48b' },
  { label: '30m-1h', color: '#7bc96f' },
  { label: '1h-2h', color: '#239a3b' },
  { label: '2h+', color: '#196127' }
]

// 模式标签
const modeLabel = { focus: '正计时', countdown: '倒计时', pomodoro: '番茄钟' }
// 模式 tag 类型
const modeTagType = { focus: 'primary', countdown: 'danger', pomodoro: 'warning' }
</script>

<template>
  <div class="page calendar-page">
    <h1>日历视图</h1>

    <!-- 月份导航 -->
    <div class="month-nav">
      <el-button :icon="ArrowLeft" circle size="small" @click="prevMonth" />
      <span class="month-label">{{ monthLabel }}</span>
      <el-button :icon="ArrowRight" circle size="small" @click="nextMonth" />
    </div>

    <!-- 颜色图例 -->
    <div class="legend">
      <span class="legend-text">少</span>
      <span
        v-for="item in legend"
        :key="item.color"
        class="legend-block"
        :style="{ background: item.color }"
        :title="item.label"
      ></span>
      <span class="legend-text">多</span>
    </div>

    <!-- 日历网格 -->
    <div class="calendar-grid">
      <!-- 星期头 -->
      <div class="calendar-header">
        <div v-for="d in weekdays" :key="d" class="calendar-cell header-cell">
          {{ d }}
        </div>
      </div>

      <!-- 日期行 -->
      <div v-for="(week, wi) in calendarGrid" :key="wi" class="calendar-row">
        <div
          v-for="(day, di) in week"
          :key="di"
          class="calendar-cell day-cell"
          :class="{
            'day-cell--empty': !day,
            'day-cell--today': day?.isToday,
            'day-cell--selected': day?.isSelected,
            'day-cell--has-record': day?.seconds > 0
          }"
          :style="day ? {
            background: getHeatColor(day.seconds),
            color: getTextColor(day.seconds)
          } : {}"
          @click="day && selectDate(day)"
        >
          {{ day?.day || '' }}
        </div>
      </div>
    </div>

    <!-- 当日详情 -->
    <div v-if="selectedDate" class="day-detail">
      <h3>
        {{ selectedDate.getMonth() + 1 }}月{{ selectedDate.getDate() }}日 详情
        <span class="day-total">{{ recordStore.formatDuration(selectedTotal) }}</span>
      </h3>

      <div v-if="selectedDateRecords.length === 0" class="empty-hint">
        当天暂无记录
      </div>

      <div v-for="rec in selectedDateRecords" :key="rec.id" class="detail-item">
        <span class="detail-dot" :style="{ background: rec.categoryColor }"></span>
        <span class="detail-cat">{{ rec.categoryName }}</span>
        <el-tag :type="modeTagType[rec.mode] || 'info'" size="small">
          {{ modeLabel[rec.mode] || rec.mode }}
        </el-tag>
        <span class="detail-time">{{ recordStore.formatDuration(rec.durationSeconds) }}</span>
        <span v-if="rec.note" class="detail-note">{{ rec.note }}</span>
      </div>
    </div>

    <!-- 月度汇总 -->
    <div class="month-summary">
      <span>本月总计：<strong>{{ monthSummary.total }}</strong></span>
      <el-divider direction="vertical" />
      <span>有记录：<strong>{{ monthSummary.days }}</strong> 天</span>
    </div>
  </div>
</template>

<style scoped>
.calendar-page {
  max-width: 600px;
}

/* ---- 月份导航 ---- */
.month-nav {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-bottom: 12px;
}

.month-label {
  font-size: 18px;
  font-weight: 600;
  min-width: 120px;
  text-align: center;
}

/* ---- 图例 ---- */
.legend {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  margin-bottom: 16px;
}

.legend-block {
  width: 20px;
  height: 14px;
  border-radius: 3px;
}

.legend-text {
  font-size: 12px;
  color: #909399;
  margin: 0 4px;
}

/* ---- 日历网格 ---- */
.calendar-grid {
  background: #fff;
  border-radius: 10px;
  border: 1px solid #e4e7ed;
  overflow: hidden;
}

.calendar-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.header-cell {
  padding: 10px 0;
  text-align: center;
  font-size: 13px;
  font-weight: 500;
  color: #606266;
}

.calendar-row {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
}

.day-cell {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  cursor: pointer;
  transition: transform 0.15s, box-shadow 0.15s;
  border-radius: 6px;
  margin: 3px;
  user-select: none;
}

.day-cell--empty {
  cursor: default;
}

.day-cell--has-record:hover {
  transform: scale(1.1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.day-cell--today {
  box-shadow: inset 0 0 0 2px #409eff;
}

.day-cell--selected {
  box-shadow: inset 0 0 0 3px #e6a23c !important;
  transform: scale(1.05);
}

/* ---- 当日详情 ---- */
.day-detail {
  margin-top: 20px;
  background: #fff;
  border-radius: 10px;
  border: 1px solid #e4e7ed;
  padding: 20px;
}

.day-detail h3 {
  font-size: 16px;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.day-total {
  color: #409eff;
  font-size: 14px;
  font-weight: 400;
  margin-left: auto;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.detail-cat {
  font-size: 14px;
  font-weight: 500;
}

.detail-time {
  font-weight: 600;
  margin-left: auto;
}

.detail-note {
  font-size: 12px;
  color: #909399;
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.empty-hint {
  text-align: center;
  color: #909399;
  padding: 16px 0;
}

/* ---- 月度汇总 ---- */
.month-summary {
  margin-top: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 16px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  font-size: 14px;
  color: #606266;
}

/* 移动端 */
@media (max-width: 480px) {
  .day-cell {
    font-size: 12px;
    margin: 2px;
    border-radius: 4px;
  }

  .detail-note {
    display: none;
  }
}
</style>
