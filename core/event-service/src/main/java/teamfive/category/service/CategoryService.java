package teamfive.category.service;

import teamfive.dto.category.InputCategoryDto;
import teamfive.dto.category.OutputCategoryDto;
import teamfive.dto.category.UpdateCategoryDto;

import java.util.List;

public interface CategoryService {
    OutputCategoryDto createCategory(InputCategoryDto inputCategoryDto);

    void delete(Long categoryId);

    OutputCategoryDto updateCategory(UpdateCategoryDto updateCategoryDto);

    OutputCategoryDto getById(Long categoryId);

    List<OutputCategoryDto> getAll(int from, int size);
}
