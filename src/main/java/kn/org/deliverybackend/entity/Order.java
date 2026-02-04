package kn.org.deliverybackend.entity;

import jakarta.persistence.*;
import kn.org.deliverybackend.entity.base.AbstractBaseEntity;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order extends AbstractBaseEntity<UUID> {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    private UUID clientId;

    private UUID riderId;

    private String deliveryAddress;

    private BigDecimal totalAmount;

    private String paymentMethod;

    private UUID paymentId;

    private String orderStatus;

    private Long shopId;

    private Double deliveryCharge;

    private Float latitude;

    private Float longitude;
}
