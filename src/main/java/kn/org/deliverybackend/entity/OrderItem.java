package kn.org.deliverybackend.entity;

import jakarta.persistence.*;
import kn.org.deliverybackend.entity.base.AbstractBaseEntity;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem extends AbstractBaseEntity<UUID> {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    private UUID orderId;

    private UUID productId;

    private Integer quantity;

    private BigDecimal priceAtOrder;

    private Long varientId;
}
