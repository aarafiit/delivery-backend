package kn.org.deliverybackend.dto.request.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class PlaceOrderRequestDTO {

    private UUID clientId;

    private String deliveryAddress;

    private String paymentMethod;

    private Long shopId;

    private Double deliveryCharge;

    private Float latitude;

    private Float longitude;

    @NotEmpty
    @Valid
    private List<OrderItemRequestDTO> items;
}
