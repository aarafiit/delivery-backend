package kn.org.deliverybackend.mapper;

import kn.org.deliverybackend.dto.response.cart.CartItemDTO;
import kn.org.deliverybackend.entity.Cart;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface CartMapper {

    @Mapping(target = "cartItemId", source = "id")
    @Mapping(target = "lineTotal", ignore = true)
    CartItemDTO toCartItemDTO(Cart cart);

    @AfterMapping
    default void computeLineTotal(Cart cart, @MappingTarget CartItemDTO dto) {
        if (cart.getUnitPrice() != null && cart.getQuantity() != null) {
            dto.setLineTotal(cart.getUnitPrice().multiply(BigDecimal.valueOf(cart.getQuantity())));
        }
    }
}
