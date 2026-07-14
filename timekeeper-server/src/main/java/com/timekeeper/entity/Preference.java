package com.timekeeper.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_preference")
public class Preference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private Integer pomodoroWorkMinutes = 25;

    @Column(nullable = false)
    private Integer pomodoroBreakMinutes = 5;

    @Column(nullable = false)
    private Integer defaultCountdownMinutes = 25;

    public Preference() {}

    public Preference(Long userId) {
        this.userId = userId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Integer getPomodoroWorkMinutes() { return pomodoroWorkMinutes; }
    public void setPomodoroWorkMinutes(Integer pomodoroWorkMinutes) { this.pomodoroWorkMinutes = pomodoroWorkMinutes; }

    public Integer getPomodoroBreakMinutes() { return pomodoroBreakMinutes; }
    public void setPomodoroBreakMinutes(Integer pomodoroBreakMinutes) { this.pomodoroBreakMinutes = pomodoroBreakMinutes; }

    public Integer getDefaultCountdownMinutes() { return defaultCountdownMinutes; }
    public void setDefaultCountdownMinutes(Integer defaultCountdownMinutes) { this.defaultCountdownMinutes = defaultCountdownMinutes; }
}
