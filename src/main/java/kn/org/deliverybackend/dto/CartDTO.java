package kn.org.deliverybackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {

    private Long id;

    private UUID userId;

    private UUID productId;

    private Long variantId;

    private Integer quantity;

    private String deliveryInstructions;
}
