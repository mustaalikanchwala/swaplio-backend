package com.swaplio.swaplio_backend.service;

import com.swaplio.swaplio_backend.dto.CategoryResponse;
import com.swaplio.swaplio_backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategory(){
        return categoryRepository.findAll().stream().map(CategoryResponse::toResponse).toList();
    }
}
