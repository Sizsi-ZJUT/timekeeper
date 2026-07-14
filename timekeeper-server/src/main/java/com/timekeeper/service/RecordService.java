package com.timekeeper.service;

import com.timekeeper.dto.response.CategoryStatsResponse;
import com.timekeeper.dto.response.DailyStatsResponse;
import com.timekeeper.entity.Record;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface RecordService {

    Record add(Long userId, Long categoryId, String categoryName, String categoryColor,
               String mode, String startTime, String endTime, Integer durationSeconds, String note);

    List<Record> listByDate(Long userId, LocalDate date);

    List<Record> listByWeek(Long userId, LocalDate dateInWeek);

    List<Record> listByMonth(Long userId, LocalDate dateInMonth);

    List<Record> listAll(Long userId);

    Record update(Long id, Long userId, Map<String, Object> updates);

    void delete(Long id, Long userId);

    List<DailyStatsResponse> getDailyStats(Long userId, int days);

    List<CategoryStatsResponse> getCategoryStats(Long userId, LocalDate date);
}
