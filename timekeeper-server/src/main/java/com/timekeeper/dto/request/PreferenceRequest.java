package com.timekeeper.dto.request;

public class PreferenceRequest {

    private Integer pomodoroWorkMinutes;
    private Integer pomodoroBreakMinutes;
    private Integer defaultCountdownMinutes;

    public Integer getPomodoroWorkMinutes() { return pomodoroWorkMinutes; }
    public void setPomodoroWorkMinutes(Integer pomodoroWorkMinutes) { this.pomodoroWorkMinutes = pomodoroWorkMinutes; }

    public Integer getPomodoroBreakMinutes() { return pomodoroBreakMinutes; }
    public void setPomodoroBreakMinutes(Integer pomodoroBreakMinutes) { this.pomodoroBreakMinutes = pomodoroBreakMinutes; }

    public Integer getDefaultCountdownMinutes() { return defaultCountdownMinutes; }
    public void setDefaultCountdownMinutes(Integer defaultCountdownMinutes) { this.defaultCountdownMinutes = defaultCountdownMinutes; }
}
