package com.timekeeper.service;

import com.timekeeper.dto.request.LoginRequest;
import com.timekeeper.dto.request.RegisterRequest;
import com.timekeeper.dto.response.LoginResponse;

public interface AuthService {

    LoginResponse register(RegisterRequest req);

    LoginResponse login(LoginRequest req);

    LoginResponse updateNickname(Long userId, String nickname);
}
