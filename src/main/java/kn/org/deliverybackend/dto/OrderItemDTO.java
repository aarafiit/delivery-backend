package kn.org.deliverybackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {

    private UUID id;

    private UUID orderId;

    private UUID productId;

    private Integer quantity;

    private BigDecimal priceAtOrder;

    private Long variantId;
}
