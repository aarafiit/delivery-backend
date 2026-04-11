package kn.org.deliverybackend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

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
}
