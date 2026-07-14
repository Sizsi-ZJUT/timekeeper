/**
 * 计时器状态管理 (Pinia Store)
 *
 * 管理三种计时模式的完整状态与计时逻辑：
 * - focus: 正计时（从 0 开始向上计数）
 * - countdown: 倒计时（从目标时长向下计数，归零时自动停止）
 * - pomodoro: 番茄钟（25 分钟工作 + 5 分钟休息循环，自动切换阶段）
 *
 * 计时使用 setInterval 每秒 tick，暂停时清除定时器。
 * 计时结束后触发 onComplete 回调，供外部保存记录。
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ElNotification, ElMessageBox } from 'element-plus'
import { usePreferenceStore } from '@/stores/preferences'

const MODE_LABELS = { focus: '正计时', countdown: '倒计时', pomodoro: '番茄钟' }

/**
 * 从偏好 store 读取番茄钟工作时长（秒）
 * 默认 25 分钟
 */
function getPomodoroWork() {
  const prefStore = usePreferenceStore()
  return (prefStore.preferences.pomodoroWorkMinutes || 25) * 60
}

/**
 * 从偏好 store 读取番茄钟休息时长（秒）
 * 默认 5 分钟
 */
function getPomodoroBreak() {
  const prefStore = usePreferenceStore()
  return (prefStore.preferences.pomodoroBreakMinutes || 5) * 60
}

export const useTimerStore = defineStore('timer', () => {
  // ---- 状态 ----
  const mode = ref('focus') // 当前实际运行的模式（计时逻辑用）
  const displayMode = ref('focus') // Tab 栏展示的模式（UI 用，运行中可切换但不影响计时）
  const isRunning = ref(false)
  // 计时秒数（正计时=已过秒数，倒计时/番茄钟=剩余秒数）
  const elapsedSeconds = ref(0)
  // 目标秒数（倒计时/番茄钟阶段目标）
  const targetSeconds = ref(getPomodoroWork())
  // 番茄钟当前阶段
  const pomodoroPhase = ref('work') // 'work' | 'break'
  // 番茄钟完成轮数
  const pomodoroCount = ref(0)
  // 计时开始时间戳（用于计算实际时长，避免 setInterval 漂移）
  const startedAt = ref(null)
  // 计时完成回调标记（结束时通知外部保存记录）
  const onCompleteCallback = ref(null)

  // setInterval ID，用于清除定时器
  let tickTimer = null

  // ---- 计算属性 ----

  /**
   * 格式化显示时间 HH:MM:SS
   * 正计时显示已过时间，倒计时/番茄钟显示剩余时间
   */
  const displayTime = computed(() => {
    const secs = elapsedSeconds.value
    const h = Math.floor(secs / 3600)
    const m = Math.floor((secs % 3600) / 60)
    const s = secs % 60
    return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
  })

  /**
   * 进度 0-1，用于圆形表盘
   * 正计时：从 0 到某个最大值（如 1 小时 = 1 圈）
   * 倒计时：从 target 到 0
   * 番茄钟：当前阶段的剩余比例
   */
  const progress = computed(() => {
    if (mode.value === 'focus') {
      // 以 1 小时为 1 圈（超过 1 小时后继续从 0 开始）
      return (elapsedSeconds.value % 3600) / 3600
    }
    return targetSeconds.value > 0
      ? elapsedSeconds.value / targetSeconds.value
      : 0
  })

  // ---- 内部方法 ----

  /**
   * 启动 setInterval 每秒 tick
   * 使用 startedAt 时间戳计算真实流逝时间，避免定时器延迟积累误差
   */
  function startTick() {
    stopTick()
    startedAt.value = Date.now()
    const startElapsed = elapsedSeconds.value

    tickTimer = setInterval(() => {
      const realElapsed = Math.floor((Date.now() - startedAt.value) / 1000)

      if (mode.value === 'focus') {
        // 正计时：递增
        elapsedSeconds.value = startElapsed + realElapsed
      } else {
        // 倒计时 / 番茄钟：递减
        const remaining = startElapsed - realElapsed
        if (remaining <= 0) {
          elapsedSeconds.value = 0
          handleComplete()
          return
        }
        elapsedSeconds.value = remaining
      }
    }, 200) // 200ms 刷新保证显示流畅
  }

  /**
   * 停止定时器
   */
  function stopTick() {
    if (tickTimer) {
      clearInterval(tickTimer)
      tickTimer = null
    }
  }

  /**
   * 计时完成处理（不暂停，自动继续）
   * - 倒计时：通知 + 保存 → 重置到目标时长继续跑
   * - 番茄钟：通知 + 保存 → 自动切换阶段继续跑
   */
  function handleComplete() {
    if (mode.value === 'countdown') {
      ElNotification({
        title: '倒计时结束',
        message: '时间到！自动重新开始',
        type: 'success',
        duration: 5000
      })
      if (onCompleteCallback.value) onCompleteCallback.value()
      elapsedSeconds.value = targetSeconds.value
      startTick()
    } else if (mode.value === 'pomodoro') {
      if (pomodoroPhase.value === 'work') {
        pomodoroCount.value++
        pomodoroPhase.value = 'break'
        targetSeconds.value = getPomodoroBreak()
        elapsedSeconds.value = getPomodoroBreak()
        ElNotification({
          title: '工作结束',
          message: `第 ${pomodoroCount.value} 个番茄完成！休息一下吧`,
          type: 'success',
          duration: 5000
        })
      } else {
        pomodoroPhase.value = 'work'
        targetSeconds.value = getPomodoroWork()
        elapsedSeconds.value = getPomodoroWork()
        ElNotification({
          title: '休息结束',
          message: '开始新的番茄钟！',
          type: 'info',
          duration: 5000
        })
      }
      if (onCompleteCallback.value) onCompleteCallback.value()
      startTick()
    }
  }

  // ---- 公开方法 ----

  /**
   * 开始计时（或继续计时）
   * 若当前正在其他模式下运行，弹出确认框切换
   */
  async function start() {
    // 正在运行中但模式不匹配 → 确认切换
    if (isRunning.value && displayMode.value !== mode.value) {
      try {
        await ElMessageBox.confirm(
          `当前${MODE_LABELS[mode.value]}正在进行中，是否停止并开始${MODE_LABELS[displayMode.value]}？`,
          '切换计时模式',
          { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
        )
      } catch {
        // 取消 → 恢复 displayMode 与当前 mode 一致
        displayMode.value = mode.value
        return
      }
      // 确认 → 停旧模式，切新模式
      stopTick()
      isRunning.value = false
      mode.value = displayMode.value
      resetState()
    }

    if (isRunning.value) return

    // 倒计时/番茄钟未设置时长时的默认值
    if ((mode.value === 'countdown' || mode.value === 'pomodoro') && elapsedSeconds.value <= 0) {
      if (mode.value === 'pomodoro') {
        targetSeconds.value = getPomodoroWork()
        elapsedSeconds.value = getPomodoroWork()
      }
    }

    isRunning.value = true
    startTick()
  }

  /**
   * 暂停计时
   * 若显示模式与运行模式不一致，先保存当前会话再切换到新模式
   */
  function pause() {
    if (!isRunning.value) return
    stopTick()
    isRunning.value = false
    if (displayMode.value !== mode.value) {
      if (onCompleteCallback.value) onCompleteCallback.value()
      mode.value = displayMode.value
      resetState()
    }
  }

  /**
   * 重置计时器（同步显示模式）
   */
  function reset() {
    stopTick()
    isRunning.value = false
    mode.value = displayMode.value
    resetState()
  }

  /**
   * 内部重置：按当前 mode 恢复初始值（不改变 isRunning）
   */
  function resetState() {
    if (mode.value === 'focus') {
      elapsedSeconds.value = 0
    } else if (mode.value === 'countdown') {
      elapsedSeconds.value = targetSeconds.value
    } else if (mode.value === 'pomodoro') {
      pomodoroPhase.value = 'work'
      pomodoroCount.value = 0
      targetSeconds.value = getPomodoroWork()
      elapsedSeconds.value = getPomodoroWork()
    }
  }

  /**
   * 切换计时模式（运行中只切换显示，不中断计时；暂停时立即生效）
   */
  function setMode(m) {
    displayMode.value = m
    if (!isRunning.value) {
      mode.value = m
      resetState()
    }
  }

  /**
   * 设置倒计时目标时长
   */
  function setTarget(seconds) {
    targetSeconds.value = seconds
    elapsedSeconds.value = seconds
  }

  /**
   * 注册计时完成回调（用于自动保存记录）
   */
  function onComplete(fn) {
    onCompleteCallback.value = fn
  }

  return {
    mode, displayMode, isRunning, elapsedSeconds, targetSeconds,
    pomodoroPhase, pomodoroCount,
    displayTime, progress,
    start, pause, reset, setMode, setTarget, onComplete
  }
})
