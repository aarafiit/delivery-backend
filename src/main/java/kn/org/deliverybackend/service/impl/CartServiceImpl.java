package kn.org.deliverybackend.service.impl;

import kn.org.deliverybackend.dto.CartDTO;
import kn.org.deliverybackend.dto.response.cart.CartStockValidationResult;
import kn.org.deliverybackend.dto.response.cart.StockMismatchItem;
import kn.org.deliverybackend.entity.Cart;
import kn.org.deliverybackend.entity.Product;
import kn.org.deliverybackend.exception.ResourceNotFoundException;
import kn.org.deliverybackend.enumeration.StockStatus;
import kn.org.deliverybackend.mapper.CartMapper;
import kn.org.deliverybackend.repository.CartRepository;
import kn.org.deliverybackend.repository.ProductRepository;
import kn.org.deliverybackend.repository.UsersRepository;
import kn.org.deliverybackend.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final UsersRepository usersRepository;
    private final CartMapper cartMapper;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CartDTO> getCart(UUID userId) {
        usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return cartRepository.findByUserId(userId)
                .stream()
                .map(cartMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CartDTO addToCart(UUID userId, CartDTO cartDTO) {
        usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // If product already in cart, increment quantity
        return cartRepository.findByUserIdAndProductId(userId, cartDTO.getProductId())
                .map(existing -> {
                    existing.setQuantity(existing.getQuantity() + cartDTO.getQuantity());
                    return cartMapper.toDTO(cartRepository.save(existing));
                })
                .orElseGet(() -> {
                    Cart cart = cartMapper.toEntity(cartDTO);
                    cart.setUserId(userId);
                    return cartMapper.toDTO(cartRepository.save(cart));
                });
    }

    @Override
    @Transactional
    public CartDTO updateCartItem(UUID userId, Long cartItemId, CartDTO cartDTO) {
        usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Cart cart = cartRepository.findByIdAndUserId(cartItemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));

        if (cartDTO.getQuantity() != null) {
            cart.setQuantity(cartDTO.getQuantity());
        }
        if (cartDTO.getDeliveryInstructions() != null) {
            cart.setDeliveryInstructions(cartDTO.getDeliveryInstructions());
        }

        return cartMapper.toDTO(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public void removeCartItem(UUID userId, Long cartItemId) {
        usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Cart cart = cartRepository.findByIdAndUserId(cartItemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));

        cart.setDeleted(true);
        cart.setDeletedAt(new Date());
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public CartDTO updateDeliveryInstructions(UUID userId, Long cartItemId, String deliveryInstructions) {
        usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Cart cart = cartRepository.findByIdAndUserId(cartItemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));

        cart.setDeliveryInstructions(deliveryInstructions);
        return cartMapper.toDTO(cartRepository.save(cart));
    }

    @Override
    @Transactional(readOnly = true)
    public CartStockValidationResult validateCartStock(UUID userId) {
        List<Cart> cartItems = cartRepository.findByUserId(userId);
        List<StockMismatchItem> mismatches = new ArrayList<>();

        for (Cart item : cartItems) {
            if (item.getProductLongId() == null) {
                continue; // skip items without a product long id reference
            }
            Product product = productRepository.findById(item.getProductLongId()).orElse(null);
            if (product == null) {
                // Product no longer exists — treat as out of stock
                mismatches.add(new StockMismatchItem(
                        item.getProductId(),
                        item.getQuantity(),
                        0,
                        StockStatus.OUT_OF_STOCK
                ));
                continue;
            }

            int available = product.getStockQuantity();
            int requested = item.getQuantity();

            if (requested > available) {
                StockStatus status = available == 0 ? StockStatus.OUT_OF_STOCK : StockStatus.LOW_STOCK;
                mismatches.add(new StockMismatchItem(
                        item.getProductId(),
                        requested,
                        available,
                        status
                ));
            }
        }

        return new CartStockValidationResult(mismatches.isEmpty(), mismatches);
    }
}
