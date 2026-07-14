package com.timekeeper.controller;

import com.timekeeper.dto.request.RecordRequest;
import com.timekeeper.dto.response.ApiResponse;
import com.timekeeper.dto.response.CategoryStatsResponse;
import com.timekeeper.dto.response.DailyStatsResponse;
import com.timekeeper.entity.Record;
import com.timekeeper.entity.User;
import com.timekeeper.service.RecordService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/records")
public class RecordController {

    private final RecordService recordService;

    public RecordController(RecordService recordService) {
        this.recordService = recordService;
    }

    /**
     * 查询记录（可按日期/周/月筛选）
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Record>>> list(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String week,
            @RequestParam(required = false) String month) {
        List<Record> records;
        if (date != null) {
            records = recordService.listByDate(user.getId(), LocalDate.parse(date));
        } else if (week != null) {
            records = recordService.listByWeek(user.getId(), LocalDate.parse(week));
        } else if (month != null) {
            records = recordService.listByMonth(user.getId(), LocalDate.parse(month));
        } else {
            records = recordService.listAll(user.getId());
        }
        return ResponseEntity.ok(ApiResponse.success(records));
    }

    /**
     * 添加记录
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Record>> add(@AuthenticationPrincipal User user,
                                                    @Valid @RequestBody RecordRequest req) {
        Record rec = recordService.add(user.getId(), req.getCategoryId(),
                req.getCategoryName(), req.getCategoryColor(), req.getMode(),
                req.getStartTime(), req.getEndTime(), req.getDurationSeconds(), req.getNote());
        return ResponseEntity.ok(ApiResponse.success(rec));
    }

    /**
     * 修改记录
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Record>> update(@AuthenticationPrincipal User user,
                                                       @PathVariable Long id,
                                                       @RequestBody Map<String, Object> updates) {
        try {
            Record rec = recordService.update(id, user.getId(), updates);
            return ResponseEntity.ok(ApiResponse.success(rec));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 删除记录
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal User user,
                                                     @PathVariable Long id) {
        try {
            recordService.delete(id, user.getId());
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 每日统计
     */
    @GetMapping("/stats/daily")
    public ResponseEntity<ApiResponse<List<DailyStatsResponse>>> dailyStats(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(ApiResponse.success(recordService.getDailyStats(user.getId(), days)));
    }

    /**
     * 分类统计
     */
    @GetMapping("/stats/categories")
    public ResponseEntity<ApiResponse<List<CategoryStatsResponse>>> categoryStats(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String date) {
        LocalDate d = date != null ? LocalDate.parse(date) : null;
        return ResponseEntity.ok(ApiResponse.success(recordService.getCategoryStats(user.getId(), d)));
    }
}
