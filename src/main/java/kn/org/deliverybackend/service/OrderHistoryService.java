package kn.org.deliverybackend.service;

import kn.org.deliverybackend.dto.OrderDTO;

import java.util.List;
import java.util.UUID;

public interface OrderHistoryService {

    // C-25: View order history
    List<OrderDTO> getOrderHistory(UUID userId);

    // C-26: View order details
    OrderDTO getOrderDetails(UUID userId, UUID orderId);

    // Admin: Get all orders
    List<OrderDTO> getAllOrders();

    // Admin: Get orders by status
    List<OrderDTO> getOrdersByStatus(String status);

    // Admin: Update order status
    OrderDTO updateOrderStatus(UUID orderId, String status);
}
