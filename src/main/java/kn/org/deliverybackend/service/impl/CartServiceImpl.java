package kn.org.deliverybackend.service.impl;

import kn.org.deliverybackend.dto.request.cart.AddToCartRequestDTO;
import kn.org.deliverybackend.dto.response.cart.CartItemDTO;
import kn.org.deliverybackend.dto.response.cart.CartResponseDTO;
import kn.org.deliverybackend.entity.Cart;
import kn.org.deliverybackend.enumeration.StockStatus;
import kn.org.deliverybackend.exception.ResourceNotFoundException;
import kn.org.deliverybackend.mapper.CartMapper;
import kn.org.deliverybackend.repository.CartRepository;
import kn.org.deliverybackend.repository.UsersRepository;
import kn.org.deliverybackend.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final UsersRepository usersRepository;
    private final CartMapper cartMapper;

    @Override
    @Transactional
    public CartItemDTO addToCart(UUID userId, AddToCartRequestDTO request) {
        usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        // If product already in cart, add on top of existing quantity; otherwise create new item
        Cart cart = cartRepository.findByUserIdAndProductId(userId, request.getProductId())
                .map(existing -> {
                    existing.setQuantity(existing.getQuantity() + (request.getQuantity() != null && request.getQuantity() > 0 ? request.getQuantity() : 1));
                    return cartRepository.save(existing);
                })
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    newCart.setProductId(request.getProductId()); // Long
                    newCart.setProductName(request.getProductName());
                    newCart.setImageUrl(request.getImageUrl());
                    newCart.setUnitPrice(request.getUnitPrice());
                    newCart.setQuantity(request.getQuantity() != null && request.getQuantity() > 0 ? request.getQuantity() : 1);
                    newCart.setStockStatus(StockStatus.IN_STOCK);
                    return cartRepository.save(newCart);
                });
        return toItemDTO(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponseDTO getCart(UUID userId) {
        usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        return buildCartResponse(cartRepository.findByUserId(userId));
    }

    @Override
    @Transactional
    public CartItemDTO incrementQuantity(UUID userId, UUID cartItemId) {
        Cart cart = cartRepository.findByIdAndUserId(cartItemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + cartItemId));
        cart.setQuantity(cart.getQuantity() + 1); // FR-07-03
        return toItemDTO(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public CartItemDTO decrementQuantity(UUID userId, UUID cartItemId) {
        Cart cart = cartRepository.findByIdAndUserId(cartItemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + cartItemId));
        // At quantity 1, pressing minus removes the item; confirmation handled on client side
        if (cart.getQuantity() <= 1) {
            cart.setDeleted(true);
            cart.setDeletedAt(new Date());
            cartRepository.save(cart);
            return null; // null signals to controller that item was removed → 204
        }
        cart.setQuantity(cart.getQuantity() - 1); // FR-07-04
        return toItemDTO(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public void removeCartItem(UUID userId, UUID cartItemId) {
        Cart cart = cartRepository.findByIdAndUserId(cartItemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + cartItemId));
        cart.setDeleted(true);
        cart.setDeletedAt(new Date());
        cartRepository.save(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public long getCartCount(UUID userId) {
        usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        return cartRepository.countByUserId(userId);
    }

    @Override
    @Transactional
    public CartResponseDTO mergeGuestCart(UUID userId, List<AddToCartRequestDTO> guestItems) {
        usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        for (AddToCartRequestDTO item : guestItems) {
            // If product exists in user cart, add guest quantity on top; otherwise create new item
            cartRepository.findByUserIdAndProductId(userId, item.getProductId())
                    .ifPresentOrElse(
                            existing -> {
                                existing.setQuantity(existing.getQuantity() + (item.getQuantity() != null && item.getQuantity() > 0 ? item.getQuantity() : 1));
                                cartRepository.save(existing);
                            },
                            () -> {
                                Cart newCart = new Cart();
                                newCart.setUserId(userId);
                                newCart.setProductId(item.getProductId()); // Long
                                newCart.setProductName(item.getProductName());
                                newCart.setImageUrl(item.getImageUrl());
                                newCart.setUnitPrice(item.getUnitPrice());
                                newCart.setQuantity(item.getQuantity() != null && item.getQuantity() > 0 ? item.getQuantity() : 1);
                                newCart.setStockStatus(StockStatus.IN_STOCK);
                                cartRepository.save(newCart);
                            }
                    );
        }
        return buildCartResponse(cartRepository.findByUserId(userId));
    }

    private CartItemDTO toItemDTO(Cart cart) {
        return cartMapper.toCartItemDTO(cart);
    }

    // Build cart response with item list, subtotal and grand total
    private CartResponseDTO buildCartResponse(List<Cart> cartItems) {
        List<CartItemDTO> itemDTOs = cartItems.stream()
                .map(this::toItemDTO)
                .toList();
        // OOS items shown but excluded from subtotal (FR-07-13)
        BigDecimal subtotal = itemDTOs.stream()
                .filter(i -> i.getStockStatus() != StockStatus.OUT_OF_STOCK)
                .map(CartItemDTO::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        // Grand total equals subtotal at cart stage; delivery fee added at checkout (FR-07-06)
        return CartResponseDTO.builder()
                .items(itemDTOs)
                .cartSubtotal(subtotal)
                .cartGrandTotal(subtotal)
                .build();
    }
}