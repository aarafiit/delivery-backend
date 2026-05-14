package kn.org.deliverybackend.entity;

import jakarta.persistence.*;
import kn.org.deliverybackend.entity.base.AbstractBaseEntity;
import kn.org.deliverybackend.enumeration.StockStatus;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "cart")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cart extends AbstractBaseEntity<UUID> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private UUID userId;

    private Long productId;

    @Column(length = 150)
    private String productName;

    @Column(length = 500)
    private String imageUrl;

    @Column(precision = 19, scale = 4)
    private BigDecimal unitPrice;

    private Long variantId;

    private Integer quantity;

    @Column(length = 500)
    private String deliveryInstructions;

    @Enumerated(EnumType.STRING)
    private StockStatus stockStatus;
}