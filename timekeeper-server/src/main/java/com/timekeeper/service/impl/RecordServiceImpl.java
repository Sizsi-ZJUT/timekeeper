package com.timekeeper.service.impl;

import com.timekeeper.dto.response.CategoryStatsResponse;
import com.timekeeper.dto.response.DailyStatsResponse;
import com.timekeeper.entity.Record;
import com.timekeeper.repository.RecordRepository;
import com.timekeeper.service.RecordService;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecordServiceImpl implements RecordService {

    private final RecordRepository recordRepository;

    public RecordServiceImpl(RecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    @Override
    public Record add(Long userId, Long categoryId, String categoryName, String categoryColor,
                      String mode, String startTime, String endTime, Integer durationSeconds, String note) {
        Record rec = new Record();
        rec.setUserId(userId);
        rec.setCategoryId(categoryId);
        rec.setCategoryName(categoryName != null && !categoryName.isEmpty() ? categoryName : "未分类");
        rec.setCategoryColor(categoryColor != null && !categoryColor.isEmpty() ? categoryColor : "#409eff");
        rec.setMode(mode);
        rec.setStartTime(parseDateTime(startTime));
        rec.setEndTime(parseDateTime(endTime));
        rec.setDurationSeconds(durationSeconds);
        rec.setNote(note != null ? note : "");
        return recordRepository.save(rec);
    }

    @Override
    public List<Record> listByDate(Long userId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        return recordRepository.findByUserIdAndDateRange(userId, start, end);
    }

    @Override
    public List<Record> listByWeek(Long userId, LocalDate dateInWeek) {
        LocalDate monday = dateInWeek.with(DayOfWeek.MONDAY);
        LocalDate sunday = monday.plusDays(7);
        return recordRepository.findByUserIdAndDateRange(userId, monday.atStartOfDay(), sunday.atStartOfDay());
    }

    @Override
    public List<Record> listByMonth(Long userId, LocalDate dateInMonth) {
        LocalDate firstDay = dateInMonth.withDayOfMonth(1);
        LocalDate firstDayNext = firstDay.plusMonths(1);
        return recordRepository.findByUserIdAndDateRange(userId, firstDay.atStartOfDay(), firstDayNext.atStartOfDay());
    }

    @Override
    public List<Record> listAll(Long userId) {
        return recordRepository.findByUserIdOrderByStartTimeDesc(userId);
    }

    @Override
    public Record update(Long id, Long userId, Map<String, Object> updates) {
        Record rec = recordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("记录不存在"));
        if (!rec.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }
        if (updates.containsKey("note")) {
            rec.setNote((String) updates.get("note"));
        }
        if (updates.containsKey("categoryId")) {
            rec.setCategoryId(((Number) updates.get("categoryId")).longValue());
        }
        if (updates.containsKey("categoryName")) {
            rec.setCategoryName((String) updates.get("categoryName"));
        }
        if (updates.containsKey("categoryColor")) {
            rec.setCategoryColor((String) updates.get("categoryColor"));
        }
        if (updates.containsKey("startTime")) {
            rec.setStartTime(parseDateTime((String) updates.get("startTime")));
        }
        if (updates.containsKey("endTime")) {
            rec.setEndTime(parseDateTime((String) updates.get("endTime")));
        }
        if (updates.containsKey("durationSeconds")) {
            rec.setDurationSeconds((Integer) updates.get("durationSeconds"));
        }
        return recordRepository.save(rec);
    }

    @Override
    public void delete(Long id, Long userId) {
        Record rec = recordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("记录不存在"));
        if (!rec.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }
        recordRepository.delete(rec);
    }

    @Override
    public List<DailyStatsResponse> getDailyStats(Long userId, int days) {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(days - 1);
        List<Record> records = recordRepository.findByUserIdAndDateRangeUnordered(
                userId, start.atStartOfDay(), today.plusDays(1).atStartOfDay());

        Map<String, Long> dayMap = new LinkedHashMap<>();
        for (int i = 0; i < days; i++) {
            LocalDate d = start.plusDays(i);
            dayMap.put(d.format(DateTimeFormatter.ofPattern("M/d")), 0L);
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("M/d");
        for (Record r : records) {
            String key = r.getStartTime().format(fmt);
            dayMap.merge(key, (long) r.getDurationSeconds(), Long::sum);
        }

        return dayMap.entrySet().stream()
                .map(e -> new DailyStatsResponse(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryStatsResponse> getCategoryStats(Long userId, LocalDate date) {
        LocalDateTime start = date != null ? date.atStartOfDay() : LocalDate.now().atStartOfDay();
        LocalDateTime end = date != null ? date.plusDays(1).atStartOfDay() : LocalDate.now().plusDays(1).atStartOfDay();
        List<Record> records = recordRepository.findByUserIdAndDateRange(userId, start, end);

        Map<String, CategoryStatsResponse> catMap = new LinkedHashMap<>();
        for (Record r : records) {
            catMap.merge(r.getCategoryName(),
                    new CategoryStatsResponse(r.getCategoryName(), r.getCategoryColor(), r.getDurationSeconds()),
                    (a, b) -> new CategoryStatsResponse(a.getName(), a.getColor(), a.getSeconds() + b.getSeconds()));
        }

        return catMap.values().stream()
                .sorted((a, b) -> Long.compare(b.getSeconds(), a.getSeconds()))
                .collect(Collectors.toList());
    }

    /**
     * 解析 ISO 时间字符串，兼容 JS toISOString() 的 Z 后缀和普通本地时间
     */
    private static LocalDateTime parseDateTime(String dt) {
        if (dt == null || dt.isEmpty()) return null;
        try {
            return OffsetDateTime.parse(dt).toLocalDateTime();
        } catch (DateTimeException e) {
            return LocalDateTime.parse(dt, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
    }

    public static String formatDuration(long seconds) {
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        if (h > 0) return h + "h " + m + "m";
        return m + "m";
    }
}
