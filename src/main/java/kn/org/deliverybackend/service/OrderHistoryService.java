package kn.org.deliverybackend.service;

import kn.org.deliverybackend.dto.OrderDTO;

import java.util.List;
import java.util.UUID;

public interface OrderHistoryService {

    // C-25: View order history
    List<OrderDTO> getOrderHistory(UUID userId);

    // C-26: View order details
    OrderDTO getOrderDetails(UUID userId, UUID orderId);
}
