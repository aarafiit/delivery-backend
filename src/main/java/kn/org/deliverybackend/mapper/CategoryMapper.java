package kn.org.deliverybackend.mapper;

import kn.org.deliverybackend.dto.CategoryDTO;
import kn.org.deliverybackend.entity.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryDTO toDTO(Category category);

    Category toEntity(CategoryDTO categoryDTO);
}
