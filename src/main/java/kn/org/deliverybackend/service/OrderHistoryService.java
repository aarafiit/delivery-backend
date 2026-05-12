package kn.org.deliverybackend.service;

import kn.org.deliverybackend.dto.OrderDTO;
import kn.org.deliverybackend.dto.OrderSummaryDTO;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface OrderHistoryService {

    List<OrderDTO> getOrderHistory(UUID userId);
    OrderDTO getOrderDetails(UUID userId, UUID orderId);
    List<OrderDTO> getAllOrders();
    Page<OrderDTO> getOrdersPaged(int page, int size, String status, String fromDate, String toDate);
    List<OrderDTO> getOrdersByStatus(String status);
    OrderDTO updateOrderStatus(UUID orderId, String status);
    OrderSummaryDTO getSummary();
}
