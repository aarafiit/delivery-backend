package kn.org.deliverybackend.controller;

import kn.org.deliverybackend.dto.DashboardStatsDTO;
import kn.org.deliverybackend.repository.OrderRepository;
import kn.org.deliverybackend.repository.UserRiderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AdminDashboardController {

    private final OrderRepository orderRepository;
    private final UserRiderRepository userRiderRepository;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getStats() {
        long totalOrders = orderRepository.findAllOrders().size();

        double totalRevenue = orderRepository.findAllOrders().stream()
                .mapToDouble(o -> o.getTotalAmount() != null ? o.getTotalAmount().doubleValue() : 0)
                .sum();

        long activeRiders = userRiderRepository.findAll().stream()
                .filter(r -> "ACTIVE".equalsIgnoreCase(r.getStatus()))
                .count();

        long pendingOrders = orderRepository.findByOrderStatus("PENDING").size();

        return ResponseEntity.ok(new DashboardStatsDTO(totalOrders, totalRevenue, activeRiders, pendingOrders));
    }
}

