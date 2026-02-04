package kn.org.deliverybackend.mapper;

import kn.org.deliverybackend.dto.ProductDTO;
import kn.org.deliverybackend.entity.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductDTO toDTO(Product product);

    Product toEntity(ProductDTO productDTO);
}
