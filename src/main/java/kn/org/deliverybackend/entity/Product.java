package kn.org.deliverybackend.entity;

import jakarta.persistence.*;
import kn.org.deliverybackend.entity.base.AbstractBaseEntity;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

    @Column(unique = true)
    private String sku;

    private String unit;

    private BigDecimal price;

    private BigDecimal discountPrice;

    private Long shopId;

    // Primary/thumbnail image — kept for backward compatibility (mirrors imageUrls[0])
    private String imageUrl;

    // Full image gallery. Stored in a separate collection table.
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_image", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url", length = 1024)
    @OrderColumn(name = "position")
    private List<String> imageUrls = new ArrayList<>();

    private Boolean isAvailable;

    @Column(name = "stock_quantity", nullable = false, columnDefinition = "int default 0 check (stock_quantity >= 0)")
    private int stockQuantity = 0;

    @Column(name = "low_stock_threshold")
    private Integer lowStockThreshold;

    @Column(name = "avg_rating", nullable = false, columnDefinition = "double precision default 0.0")
    private double avgRating = 0.0;

    @Column(name = "total_reviews", nullable = false, columnDefinition = "int default 0")
    private int totalReviews = 0;

    @PostPersist
    public void generateSku() {
        if (this.sku == null) {
            String prefix = (this.name != null && this.name.length() >= 2)
                    ? this.name.substring(0, 2).toUpperCase().replaceAll("[^A-Z]", "X")
                    : "PR";
            this.sku = prefix + "-" + String.format("%05d", this.id);
        }
    }
}
