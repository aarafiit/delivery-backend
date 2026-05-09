package kn.org.deliverybackend.controller;

import kn.org.deliverybackend.dto.OrderDTO;
import kn.org.deliverybackend.dto.OrderSummaryDTO;
import kn.org.deliverybackend.service.OrderHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AdminOrderController {

    private final OrderHistoryService orderHistoryService;

    @GetMapping("/summary")
    public ResponseEntity<OrderSummaryDTO> getSummary() {
        return ResponseEntity.ok(orderHistoryService.getSummary());
    }

    @GetMapping
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        return ResponseEntity.ok(orderHistoryService.getAllOrders());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderDTO>> getOrdersByStatus(@PathVariable String status) {
        return ResponseEntity.ok(orderHistoryService.getOrdersByStatus(status));
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable UUID orderId,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(orderHistoryService.updateOrderStatus(orderId, body.get("status")));
    }
}
