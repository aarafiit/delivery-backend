package kn.org.deliverybackend.mapper;

import kn.org.deliverybackend.dto.CartDTO;
import kn.org.deliverybackend.entity.Cart;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CartMapper {

    CartDTO toDTO(Cart cart);

    Cart toEntity(CartDTO cartDTO);
}
