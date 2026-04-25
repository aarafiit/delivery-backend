package kn.org.deliverybackend.controller;

import kn.org.deliverybackend.dto.request.cart.AddToCartRequestDTO;
import kn.org.deliverybackend.dto.response.cart.CartItemDTO;
import kn.org.deliverybackend.dto.response.cart.CartResponseDTO;
import kn.org.deliverybackend.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/app/consumer")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // Add a product to the cart; increments quantity if the product already exists
    @PostMapping("/{userId}/cart")
    public ResponseEntity<CartItemDTO> addToCart(
            @PathVariable UUID userId,
            @RequestBody AddToCartRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cartService.addToCart(userId, request));
    }

    @GetMapping("/{userId}/cart")
    public ResponseEntity<CartResponseDTO> getCart(@PathVariable UUID userId) {
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    // Increment cart item quantity by 1 (FR-07-03)
    @PatchMapping("/{userId}/cart/{cartItemId}/increment")
    public ResponseEntity<CartItemDTO> increment(
            @PathVariable UUID userId,
            @PathVariable UUID cartItemId) {
        return ResponseEntity.ok(cartService.incrementQuantity(userId, cartItemId));
    }

    // Decrement cart item quantity by 1; removes item if quantity reaches 0 (FR-07-04)
    @PatchMapping("/{userId}/cart/{cartItemId}/decrement")
    public ResponseEntity<CartItemDTO> decrement(
            @PathVariable UUID userId,
            @PathVariable UUID cartItemId) {
        CartItemDTO result = cartService.decrementQuantity(userId, cartItemId);
        return result == null
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(result);
    }

    // Hard remove a cart item via swipe-to-delete gesture (FR-07-14)
    @DeleteMapping("/{userId}/cart/{cartItemId}")
    public ResponseEntity<Void> removeCartItem(
            @PathVariable UUID userId,
            @PathVariable UUID cartItemId) {
        cartService.removeCartItem(userId, cartItemId);
        return ResponseEntity.noContent().build();
    }

    // Return total number of items in cart for the bottom navigation badge (FR-07-12)
    @GetMapping("/{userId}/cart/count")
    public ResponseEntity<Map<String, Long>> getCartCount(@PathVariable UUID userId) {
        return ResponseEntity.ok(Map.of("count", cartService.getCartCount(userId)));
    }

    // Merge locally stored guest cart items into user account cart after OTP login (FR-07-10)
    @PostMapping("/{userId}/cart/merge")
    public ResponseEntity<CartResponseDTO> mergeGuestCart(
            @PathVariable UUID userId,
            @RequestBody List<AddToCartRequestDTO> guestItems) {
        return ResponseEntity.ok(cartService.mergeGuestCart(userId, guestItems));
    }
}