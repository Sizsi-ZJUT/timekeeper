package com.timekeeper.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class HomeController {

    /**
     * 根路径 — 欢迎页
     */
    @GetMapping("/")
    public Map<String, Object> home() {
        return Map.of(
            "app", "TimeKeeper API",
            "version", "1.0.0",
            "time", LocalDateTime.now().toString(),
            "api", "/api"
        );
    }

    /**
     * 健康检查
     */
    @GetMapping("/api/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
