package kn.org.deliverybackend.mapper;

import kn.org.deliverybackend.dto.CartDTO;
import kn.org.deliverybackend.entity.Cart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartMapper {

    CartDTO toDTO(Cart cart);

    @Mapping(target = "productLongId", ignore = true)
    Cart toEntity(CartDTO cartDTO);
}
