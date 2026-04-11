package kn.org.deliverybackend.service;

import java.util.List;
import java.util.UUID;

import kn.org.deliverybackend.dto.CartDTO;

public interface CartService {

    List<CartDTO> getCart(UUID userId);

    CartDTO addToCart(UUID userId, CartDTO cartDTO);

    CartDTO updateCartItem(UUID userId, Long cartItemId, CartDTO cartDTO);

    void removeCartItem(UUID userId, Long cartItemId);

    CartDTO updateDeliveryInstructions(UUID userId, Long cartItemId, String deliveryInstructions);
}
