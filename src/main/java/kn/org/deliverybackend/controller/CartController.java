package kn.org.deliverybackend.controller;

import kn.org.deliverybackend.dto.CartDTO;
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

    // C-12: Get cart / Add to cart
    @GetMapping("/{userId}/cart")
    public ResponseEntity<List<CartDTO>> getCart(@PathVariable UUID userId) {
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PostMapping("/{userId}/cart")
    public ResponseEntity<CartDTO> addToCart(
            @PathVariable UUID userId,
            @RequestBody CartDTO cartDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addToCart(userId, cartDTO));
    }

    // C-13: Update cart item quantity
    @PutMapping("/{userId}/cart/{cartItemId}")
    public ResponseEntity<CartDTO> updateCartItem(
            @PathVariable UUID userId,
            @PathVariable Long cartItemId,
            @RequestBody CartDTO cartDTO) {
        return ResponseEntity.ok(cartService.updateCartItem(userId, cartItemId, cartDTO));
    }

    // C-14: Remove item from cart
    @DeleteMapping("/{userId}/cart/{cartItemId}")
    public ResponseEntity<Void> removeCartItem(
            @PathVariable UUID userId,
            @PathVariable Long cartItemId) {
        cartService.removeCartItem(userId, cartItemId);
        return ResponseEntity.noContent().build();
    }

    // C-15: Delivery instructions
    @PatchMapping("/{userId}/cart/{cartItemId}/delivery-instructions")
    public ResponseEntity<CartDTO> updateDeliveryInstructions(
            @PathVariable UUID userId,
            @PathVariable Long cartItemId,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(
                cartService.updateDeliveryInstructions(userId, cartItemId, body.get("deliveryInstructions"))
        );
    }
}
