package kn.org.deliverybackend.service.impl;

import kn.org.deliverybackend.dto.OrderDTO;
import kn.org.deliverybackend.entity.Order;
import kn.org.deliverybackend.exception.ResourceNotFoundException;
import kn.org.deliverybackend.mapper.OrderItemMapper;
import kn.org.deliverybackend.mapper.OrderMapper;
import kn.org.deliverybackend.repository.OrderItemRepository;
import kn.org.deliverybackend.repository.OrderRepository;
import kn.org.deliverybackend.repository.UsersRepository;
import kn.org.deliverybackend.service.OrderHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderHistoryServiceImpl implements OrderHistoryService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UsersRepository usersRepository;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrderHistory(UUID userId) {
        usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return orderRepository.findByClientId(userId)
                .stream()
                .map(order -> {
                    OrderDTO dto = orderMapper.toDTO(order);
                    dto.setOrderItems(
                            orderItemRepository.findByOrderId(order.getId())
                                    .stream()
                                    .map(orderItemMapper::toDTO)
                                    .collect(Collectors.toList())
                    );
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDTO getOrderDetails(UUID userId, UUID orderId) {
        usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        OrderDTO dto = orderRepository.findByIdAndClientId(orderId, userId)
                .map(orderMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        dto.setOrderItems(
                orderItemRepository.findByOrderId(orderId)
                        .stream()
                        .map(orderItemMapper::toDTO)
                        .collect(Collectors.toList())
        );

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAllOrders()
                .stream()
                .map(this::toOrderDTOWithItems)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByStatus(String status) {
        return orderRepository.findByOrderStatus(status)
                .stream()
                .map(this::toOrderDTOWithItems)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderDTO updateOrderStatus(UUID orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        order.setOrderStatus(status);
        Order saved = orderRepository.save(order);
        return toOrderDTOWithItems(saved);
    }

    private OrderDTO toOrderDTOWithItems(Order order) {
        OrderDTO dto = orderMapper.toDTO(order);
        dto.setOrderItems(
                orderItemRepository.findByOrderId(order.getId())
                        .stream()
                        .map(orderItemMapper::toDTO)
                        .collect(Collectors.toList())
        );
        return dto;
    }
}
