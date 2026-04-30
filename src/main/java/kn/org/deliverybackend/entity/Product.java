package kn.org.deliverybackend.entity;

import jakarta.persistence.*;
import kn.org.deliverybackend.entity.base.AbstractBaseEntity;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product extends AbstractBaseEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_id")
    private Long categoryId;

    private String name;

    private String description;

    private BigDecimal price;

    private BigDecimal discountPrice;

    private Long shopId;

    private String imageUrl;

    private Boolean isAvailable;

    @Column(name = "stock_quantity", nullable = false, columnDefinition = "int default 0 check (stock_quantity >= 0)")
    private int stockQuantity = 0;

    @Column(name = "low_stock_threshold")
    private Integer lowStockThreshold;
}
