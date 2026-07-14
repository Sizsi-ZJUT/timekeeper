package com.timekeeper.controller;

import com.timekeeper.dto.request.PreferenceRequest;
import com.timekeeper.dto.response.ApiResponse;
import com.timekeeper.entity.Preference;
import com.timekeeper.entity.User;
import com.timekeeper.service.PreferenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/preferences")
public class PreferenceController {

    private final PreferenceService preferenceService;

    public PreferenceController(PreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    /**
     * 获取偏好
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Preference>> get(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(preferenceService.getByUser(user.getId())));
    }

    /**
     * 更新偏好
     */
    @PutMapping
    public ResponseEntity<ApiResponse<Preference>> update(@AuthenticationPrincipal User user,
                                                           @RequestBody PreferenceRequest req) {
        Preference pref = preferenceService.update(user.getId(),
                req.getPomodoroWorkMinutes(), req.getPomodoroBreakMinutes(),
                req.getDefaultCountdownMinutes());
        return ResponseEntity.ok(ApiResponse.success(pref));
    }

    /**
     * 恢复默认偏好
     */
    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<Preference>> reset(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(preferenceService.reset(user.getId())));
    }
}
