package com.timekeeper.dto.response;

/**
 * 每日统计（图表用）
 */
public class DailyStatsResponse {

    private String date;
    private long seconds;

    public DailyStatsResponse(String date, long seconds) {
        this.date = date;
        this.seconds = seconds;
    }

    public String getDate() { return date; }
    public long getSeconds() { return seconds; }
}
