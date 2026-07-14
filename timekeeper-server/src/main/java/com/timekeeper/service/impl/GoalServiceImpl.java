package com.timekeeper.service.impl;

import com.timekeeper.dto.response.GoalProgressResponse;
import com.timekeeper.entity.Goal;
import com.timekeeper.entity.Record;
import com.timekeeper.repository.GoalRepository;
import com.timekeeper.repository.RecordRepository;
import com.timekeeper.service.GoalService;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class GoalServiceImpl implements GoalService {

    private final GoalRepository goalRepository;
    private final RecordRepository recordRepository;

    public GoalServiceImpl(GoalRepository goalRepository, RecordRepository recordRepository) {
        this.goalRepository = goalRepository;
        this.recordRepository = recordRepository;
    }

    @Override
    public List<Goal> listByUser(Long userId) {
        return goalRepository.findByUserId(userId);
    }

    @Override
    public Goal add(Long userId, Long categoryId, String period, Double targetHours) {
        Goal goal = new Goal();
        goal.setUserId(userId);
        goal.setCategoryId(categoryId);
        goal.setPeriod(period);
        goal.setTargetHours(targetHours);
        goal.setTargetSeconds((int) (targetHours * 3600));
        return goalRepository.save(goal);
    }

    @Override
    public Goal update(Long id, Long userId, Long categoryId, String period, Double targetHours) {
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("目标不存在"));
        if (!goal.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }
        if (categoryId != null) goal.setCategoryId(categoryId);
        if (period != null) goal.setPeriod(period);
        if (targetHours != null) {
            goal.setTargetHours(targetHours);
            goal.setTargetSeconds((int) (targetHours * 3600));
        }
        return goalRepository.save(goal);
    }

    @Override
    public void delete(Long id, Long userId) {
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("目标不存在"));
        if (!goal.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }
        goalRepository.delete(goal);
    }

    @Override
    public GoalProgressResponse getProgress(Long id, Long userId) {
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("目标不存在"));
        if (!goal.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }

        LocalDate today = LocalDate.now();
        LocalDateTime start, end;

        if ("weekly".equals(goal.getPeriod())) {
            start = today.with(DayOfWeek.MONDAY).atStartOfDay();
            end = start.plusDays(7);
        } else {
            start = today.atStartOfDay();
            end = today.plusDays(1).atStartOfDay();
        }

        List<Record> records = recordRepository.findByUserIdAndDateRange(userId, start, end);

        long currentSeconds = records.stream()
                .filter(r -> goal.getCategoryId() == null || r.getCategoryId().equals(goal.getCategoryId()))
                .mapToLong(Record::getDurationSeconds)
                .sum();

        int target = goal.getTargetSeconds();
        int percentage = target > 0 ? Math.min(100, (int) (currentSeconds * 100 / target)) : 0;

        return new GoalProgressResponse(currentSeconds, target, percentage);
    }
}
