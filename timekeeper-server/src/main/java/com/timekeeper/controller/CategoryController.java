package com.timekeeper.controller;

import com.timekeeper.dto.request.CategoryRequest;
import com.timekeeper.dto.response.ApiResponse;
import com.timekeeper.entity.Category;
import com.timekeeper.entity.User;
import com.timekeeper.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * 获取当前用户的所有分类
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> list(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.listByUser(user.getId())));
    }

    /**
     * 添加分类
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Category>> add(@AuthenticationPrincipal User user,
                                                      @Valid @RequestBody CategoryRequest req) {
        Category cat = categoryService.add(user.getId(), req.getName(), req.getColor());
        return ResponseEntity.ok(ApiResponse.success(cat));
    }

    /**
     * 修改分类
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> update(@AuthenticationPrincipal User user,
                                                         @PathVariable Long id,
                                                         @Valid @RequestBody CategoryRequest req) {
        try {
            Category cat = categoryService.update(id, user.getId(), req.getName(), req.getColor());
            return ResponseEntity.ok(ApiResponse.success(cat));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal User user,
                                                     @PathVariable Long id) {
        try {
            categoryService.delete(id, user.getId());
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }
}
