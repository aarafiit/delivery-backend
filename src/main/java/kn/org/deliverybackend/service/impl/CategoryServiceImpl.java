package kn.org.deliverybackend.service.impl;

import kn.org.deliverybackend.dto.request.category.CategoryRequestDTO;
import kn.org.deliverybackend.dto.response.category.CategoryResponseDTO;
import kn.org.deliverybackend.entity.Category;
import kn.org.deliverybackend.mapper.CategoryMapper;
import kn.org.deliverybackend.repository.CategoryRepository;
import kn.org.deliverybackend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryResponseDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponseDTO getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(categoryMapper::toResponseDTO)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }

    @Override
    public CategoryResponseDTO createCategory(CategoryRequestDTO categoryRequestDTO) {
        Category category = categoryMapper.toEntity(categoryRequestDTO);
        return categoryMapper.toResponseDTO(categoryRepository.save(category));
    }

    @Override
    public CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO categoryRequestDTO) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        category.setName(categoryRequestDTO.getName());
        category.setIsActive(categoryRequestDTO.getIsActive());
        return categoryMapper.toResponseDTO(categoryRepository.save(category));
    }

    @Override
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
}
