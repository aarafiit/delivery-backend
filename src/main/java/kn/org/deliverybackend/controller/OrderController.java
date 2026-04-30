package kn.org.deliverybackend.controller;

import jakarta.validation.Valid;
import kn.org.deliverybackend.dto.OrderDTO;
import kn.org.deliverybackend.dto.request.order.PlaceOrderRequestDTO;
import kn.org.deliverybackend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/app/consumer")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/{userId}/orders")
    public ResponseEntity<OrderDTO> placeOrder(
            @PathVariable UUID userId,
            @Valid @RequestBody PlaceOrderRequestDTO request) {
        request.setClientId(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.placeOrder(request));
    }
}
