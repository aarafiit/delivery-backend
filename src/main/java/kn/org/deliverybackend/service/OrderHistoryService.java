package kn.org.deliverybackend.service;

import kn.org.deliverybackend.dto.OrderDTO;
import kn.org.deliverybackend.dto.OrderSummaryDTO;

import java.util.List;
import java.util.UUID;

public interface OrderHistoryService {

    List<OrderDTO> getOrderHistory(UUID userId);

    OrderDTO getOrderDetails(UUID userId, UUID orderId);

    List<OrderDTO> getAllOrders();

    List<OrderDTO> getOrdersByStatus(String status);

    OrderDTO updateOrderStatus(UUID orderId, String status);

    OrderSummaryDTO getSummary();
}
