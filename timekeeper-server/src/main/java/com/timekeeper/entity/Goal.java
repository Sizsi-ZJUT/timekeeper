package com.timekeeper.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_goal")
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = true)
    private Long categoryId;

    @Column(nullable = false, length = 10)
    private String period;

    @Column(nullable = false)
    private Double targetHours;

    @Column(nullable = false)
    private Integer targetSeconds;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Goal() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public Double getTargetHours() { return targetHours; }
    public void setTargetHours(Double targetHours) { this.targetHours = targetHours; }

    public Integer getTargetSeconds() { return targetSeconds; }
    public void setTargetSeconds(Integer targetSeconds) { this.targetSeconds = targetSeconds; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
