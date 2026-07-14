package com.timekeeper.config;

import com.timekeeper.entity.Category;
import com.timekeeper.entity.Preference;
import com.timekeeper.repository.CategoryRepository;
import com.timekeeper.repository.PreferenceRepository;
import com.timekeeper.repository.UserRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 启动时确保数据库中有初始数据：
 * 为没有分类的用户创建 6 个预设分类，
 * 为没有偏好的用户创建默认偏好。
 * 实际场景中，这些应该在注册时自动创建。
 */
@Component
public class DataInitializer {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PreferenceRepository preferenceRepository;

    private static final String[][] DEFAULT_CATEGORIES = {
        {"学习", "#409eff"},
        {"工作", "#67c23a"},
        {"阅读", "#e6a23c"},
        {"运动", "#f56c6c"},
        {"娱乐", "#909399"},
        {"其他", "#8e44ad"}
    };

    public DataInitializer(UserRepository userRepository,
                           CategoryRepository categoryRepository,
                           PreferenceRepository preferenceRepository) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.preferenceRepository = preferenceRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        userRepository.findAll().forEach(user -> {
            // 为没有分类的用户创建预设分类
            if (categoryRepository.countByUserId(user.getId()) == 0) {
                for (String[] cat : DEFAULT_CATEGORIES) {
                    categoryRepository.save(new Category(user.getId(), cat[0], cat[1]));
                }
            }
            // 为没有偏好的用户创建默认偏好
            if (preferenceRepository.findByUserId(user.getId()).isEmpty()) {
                preferenceRepository.save(new Preference(user.getId()));
            }
        });
    }
}
