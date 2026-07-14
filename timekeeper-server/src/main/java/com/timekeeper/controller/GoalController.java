package com.timekeeper.controller;

import com.timekeeper.dto.request.GoalRequest;
import com.timekeeper.dto.response.ApiResponse;
import com.timekeeper.dto.response.GoalProgressResponse;
import com.timekeeper.entity.Goal;
import com.timekeeper.entity.User;
import com.timekeeper.service.GoalService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    /**
     * 获取所有目标
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Goal>>> list(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(goalService.listByUser(user.getId())));
    }

    /**
     * 添加目标
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Goal>> add(@AuthenticationPrincipal User user,
                                                  @Valid @RequestBody GoalRequest req) {
        Goal goal = goalService.add(user.getId(), req.getCategoryId(), req.getPeriod(), req.getTargetHours());
        return ResponseEntity.ok(ApiResponse.success(goal));
    }

    /**
     * 修改目标
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Goal>> update(@AuthenticationPrincipal User user,
                                                     @PathVariable Long id,
                                                     @Valid @RequestBody GoalRequest req) {
        try {
            Goal goal = goalService.update(id, user.getId(), req.getCategoryId(), req.getPeriod(), req.getTargetHours());
            return ResponseEntity.ok(ApiResponse.success(goal));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 删除目标
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal User user,
                                                     @PathVariable Long id) {
        try {
            goalService.delete(id, user.getId());
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 目标进度
     */
    @GetMapping("/{id}/progress")
    public ResponseEntity<ApiResponse<GoalProgressResponse>> progress(@AuthenticationPrincipal User user,
                                                                       @PathVariable Long id) {
        try {
            GoalProgressResponse resp = goalService.getProgress(id, user.getId());
            return ResponseEntity.ok(ApiResponse.success(resp));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }
}
