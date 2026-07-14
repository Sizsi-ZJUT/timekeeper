package com.timekeeper.service;

import com.timekeeper.dto.response.GoalProgressResponse;
import com.timekeeper.entity.Goal;

import java.util.List;

public interface GoalService {

    List<Goal> listByUser(Long userId);

    Goal add(Long userId, Long categoryId, String period, Double targetHours);

    Goal update(Long id, Long userId, Long categoryId, String period, Double targetHours);

    void delete(Long id, Long userId);

    GoalProgressResponse getProgress(Long id, Long userId);
}
