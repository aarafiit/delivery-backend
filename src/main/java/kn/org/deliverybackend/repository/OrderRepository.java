package kn.org.deliverybackend.repository;

import kn.org.deliverybackend.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Query("SELECT o FROM Order o WHERE o.clientId = :clientId AND o.deleted = false ORDER BY o.createdAt DESC")
    List<Order> findByClientId(UUID clientId);

    @Query("SELECT o FROM Order o WHERE o.id = :orderId AND o.clientId = :clientId AND o.deleted = false")
    Optional<Order> findByIdAndClientId(UUID orderId, UUID clientId);
}
