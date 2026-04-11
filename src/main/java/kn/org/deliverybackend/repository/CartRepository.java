package kn.org.deliverybackend.repository;

import kn.org.deliverybackend.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    @Query("SELECT c FROM Cart c WHERE c.userId = :userId AND c.deleted = false")
    List<Cart> findByUserId(UUID userId);

    @Query("SELECT c FROM Cart c WHERE c.id = :id AND c.userId = :userId AND c.deleted = false")
    Optional<Cart> findByIdAndUserId(Long id, UUID userId);

    @Query("SELECT c FROM Cart c WHERE c.userId = :userId AND c.productId = :productId AND c.deleted = false")
    Optional<Cart> findByUserIdAndProductId(UUID userId, UUID productId);
}
