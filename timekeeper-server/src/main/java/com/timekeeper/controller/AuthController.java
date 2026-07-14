package com.timekeeper.controller;

import com.timekeeper.dto.request.LoginRequest;
import com.timekeeper.dto.request.RegisterRequest;
import com.timekeeper.dto.request.UpdateNicknameRequest;
import com.timekeeper.dto.response.ApiResponse;
import com.timekeeper.dto.response.LoginResponse;
import com.timekeeper.entity.User;
import com.timekeeper.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 注册
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<LoginResponse>> register(@Valid @RequestBody RegisterRequest req) {
        try {
            LoginResponse resp = authService.register(req);
            return ResponseEntity.ok(ApiResponse.success(resp));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest req) {
        try {
            LoginResponse resp = authService.login(req);
            return ResponseEntity.ok(ApiResponse.success(resp));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 修改昵称
     */
    @PatchMapping("/nickname")
    public ResponseEntity<ApiResponse<LoginResponse>> updateNickname(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateNicknameRequest req) {
        try {
            LoginResponse resp = authService.updateNickname(user.getId(), req.getNickname());
            return ResponseEntity.ok(ApiResponse.success(resp));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }
}
