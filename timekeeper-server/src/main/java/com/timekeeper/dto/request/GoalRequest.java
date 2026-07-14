package com.timekeeper.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class GoalRequest {

    private Long categoryId;

    @NotNull(message = "周期不能为空")
    private String period;

    @NotNull(message = "目标时长不能为空")
    @Min(value = 1, message = "目标时长至少1小时")
    private Double targetHours;

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public Double getTargetHours() { return targetHours; }
    public void setTargetHours(Double targetHours) { this.targetHours = targetHours; }
}
