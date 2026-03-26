package teamfive.category.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import teamfive.category.model.Category;
import teamfive.dto.category.InputCategoryDto;
import teamfive.dto.category.OutputCategoryDto;
import teamfive.dto.category.UpdateCategoryDto;

@Mapper(componentModel = "spring")
public interface SimpleCategoryMapper {

    @Mapping(target = "id", ignore = true)
    Category inputDtoToCategory(InputCategoryDto inputCategoryDto);

    Category updateDtoToCategory(UpdateCategoryDto updateCategoryDto);

    OutputCategoryDto categoryToOutDto(Category category);
}
