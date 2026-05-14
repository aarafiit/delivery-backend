package kn.org.deliverybackend.dto.request.cart;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddToCartRequestDTO {

    @NotNull
    private Long productId;

    @NotNull
    private String productName;

    @NotNull
    private String imageUrl;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal unitPrice;

    @Min(1)
    private Integer quantity; // defaults to 1 in service if null
}