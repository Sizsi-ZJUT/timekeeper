package com.timekeeper.service;

import com.timekeeper.entity.Category;

import java.util.List;

public interface CategoryService {

    List<Category> listByUser(Long userId);

    Category add(Long userId, String name, String color);

    Category update(Long id, Long userId, String name, String color);

    void delete(Long id, Long userId);
}
