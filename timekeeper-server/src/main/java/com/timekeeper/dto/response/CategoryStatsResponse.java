package com.timekeeper.dto.response;

/**
 * 分类统计（饼图用）
 */
public class CategoryStatsResponse {

    private String name;
    private String color;
    private long seconds;

    public CategoryStatsResponse(String name, String color, long seconds) {
        this.name = name;
        this.color = color;
        this.seconds = seconds;
    }

    public String getName() { return name; }
    public String getColor() { return color; }
    public long getSeconds() { return seconds; }
}
