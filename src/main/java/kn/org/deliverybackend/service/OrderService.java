package kn.org.deliverybackend.service;

import kn.org.deliverybackend.dto.OrderDTO;
import kn.org.deliverybackend.dto.request.order.PlaceOrderRequestDTO;

public interface OrderService {
    OrderDTO placeOrder(PlaceOrderRequestDTO request);
}
