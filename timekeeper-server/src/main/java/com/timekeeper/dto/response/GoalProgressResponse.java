package com.timekeeper.dto.response;

/**
 * 目标进度
 */
public class GoalProgressResponse {

    private long currentSeconds;
    private int targetSeconds;
    private int percentage;

    public GoalProgressResponse(long currentSeconds, int targetSeconds, int percentage) {
        this.currentSeconds = currentSeconds;
        this.targetSeconds = targetSeconds;
        this.percentage = percentage;
    }

    public long getCurrentSeconds() { return currentSeconds; }
    public int getTargetSeconds() { return targetSeconds; }
    public int getPercentage() { return percentage; }
}
