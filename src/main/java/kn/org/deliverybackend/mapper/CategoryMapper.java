package kn.org.deliverybackend.mapper;

import kn.org.deliverybackend.dto.request.category.CategoryRequestDTO;
import kn.org.deliverybackend.dto.response.category.CategoryResponseDTO;
import kn.org.deliverybackend.entity.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryResponseDTO toResponseDTO(Category category);

    Category toEntity(CategoryRequestDTO categoryRequestDTO);
}
