package com.timekeeper.service.impl;

import com.timekeeper.entity.Category;
import com.timekeeper.repository.CategoryRepository;
import com.timekeeper.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Category> listByUser(Long userId) {
        return categoryRepository.findByUserId(userId);
    }

    @Override
    public Category add(Long userId, String name, String color) {
        Category cat = new Category(userId, name, color);
        return categoryRepository.save(cat);
    }

    @Override
    public Category update(Long id, Long userId, String name, String color) {
        Category cat = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在"));
        if (!cat.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }
        cat.setName(name);
        cat.setColor(color);
        return categoryRepository.save(cat);
    }

    @Override
    public void delete(Long id, Long userId) {
        Category cat = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在"));
        if (!cat.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }
        categoryRepository.delete(cat);
    }
}
