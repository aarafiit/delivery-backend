package kn.org.deliverybackend.repository;

import kn.org.deliverybackend.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {

    // Fetch all active (non-deleted) cart items for a user
    @Query("SELECT c FROM Cart c WHERE c.userId = :userId AND c.deleted = false")
    List<Cart> findByUserId(UUID userId);

    // Fetch a specific cart item by id and userId
    @Query("SELECT c FROM Cart c WHERE c.id = :id AND c.userId = :userId AND c.deleted = false")
    Optional<Cart> findByIdAndUserId(UUID id, UUID userId);

    // Check if a product already exists in the user's cart (used to avoid duplicates)
    @Query("SELECT c FROM Cart c WHERE c.userId = :userId AND c.productId = :productId AND c.deleted = false")
    Optional<Cart> findByUserIdAndProductId(UUID userId, Long productId);

    // Count total active cart items for bottom navigation badge (FR-07-12)
    @Query("SELECT COUNT(c) FROM Cart c WHERE c.userId = :userId AND c.deleted = false")
    long countByUserId(UUID userId);
}