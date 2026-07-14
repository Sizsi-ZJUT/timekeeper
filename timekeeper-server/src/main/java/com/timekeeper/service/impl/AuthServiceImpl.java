package com.timekeeper.service.impl;

import com.timekeeper.dto.request.LoginRequest;
import com.timekeeper.dto.request.RegisterRequest;
import com.timekeeper.dto.response.LoginResponse;
import com.timekeeper.entity.Category;
import com.timekeeper.entity.Preference;
import com.timekeeper.entity.User;
import com.timekeeper.repository.CategoryRepository;
import com.timekeeper.repository.PreferenceRepository;
import com.timekeeper.repository.UserRepository;
import com.timekeeper.security.JwtTokenProvider;
import com.timekeeper.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final CategoryRepository categoryRepository;
    private final PreferenceRepository preferenceRepository;

    private static final String[][] DEFAULT_CATEGORIES = {
        {"学习", "#409eff"}, {"工作", "#67c23a"}, {"阅读", "#e6a23c"},
        {"运动", "#f56c6c"}, {"娱乐", "#909399"}, {"其他", "#8e44ad"}
    };

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           JwtTokenProvider tokenProvider, CategoryRepository categoryRepository,
                           PreferenceRepository preferenceRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.categoryRepository = categoryRepository;
        this.preferenceRepository = preferenceRepository;
    }

    @Override
    public LoginResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("该邮箱已被注册");
        }
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new RuntimeException("两次输入的密码不一致");
        }

        User user = new User();
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setNickname(req.getNickname());

        user = userRepository.save(user);

        for (String[] cat : DEFAULT_CATEGORIES) {
            categoryRepository.save(new Category(user.getId(), cat[0], cat[1]));
        }
        preferenceRepository.save(new Preference(user.getId()));

        String token = tokenProvider.generateToken(user.getId(), user.getEmail());
        return new LoginResponse(token, user.getEmail(), user.getNickname());
    }

    @Override
    public LoginResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("账号或密码错误"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("账号或密码错误");
        }

        String token = tokenProvider.generateToken(user.getId(), user.getEmail());
        return new LoginResponse(token, user.getEmail(), user.getNickname());
    }

    @Override
    public LoginResponse updateNickname(Long userId, String nickname) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        user.setNickname(nickname.trim());
        user = userRepository.save(user);

        return new LoginResponse(null, user.getEmail(), user.getNickname());
    }
}
