package kn.org.deliverybackend.service.impl;

import kn.org.deliverybackend.dto.OrderDTO;
import kn.org.deliverybackend.dto.OrderItemDTO;
import kn.org.deliverybackend.dto.OrderSummaryDTO;
import kn.org.deliverybackend.entity.Order;
import kn.org.deliverybackend.entity.OrderItem;
import kn.org.deliverybackend.entity.Users;
import kn.org.deliverybackend.exception.ResourceNotFoundException;
import kn.org.deliverybackend.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import kn.org.deliverybackend.mapper.OrderItemMapper;
import kn.org.deliverybackend.mapper.OrderMapper;
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
    private final UserRiderRepository userRiderRepository;
    private final ProductRepository productRepository;
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
                                    .map(this::toEnrichedItemDTO)
                                    .collect(Collectors.toList())
                    );
                    usersRepository.findById(userId).ifPresent(user -> {
                        dto.setCustomerName(buildName(user));
                        dto.setCustomerPhone(user.getPhoneNumber());
                    });
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
                        .map(this::toEnrichedItemDTO)
                        .collect(Collectors.toList())
        );

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDTO> getOrdersPaged(int page, int size, String status, String fromDate, String toDate) {
        PageRequest pageable = PageRequest.of(page, size);
        String statusParam = (status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL"))
                ? status.toUpperCase() : null;

        // Pass ISO date strings directly; null means no filter
        String from = (fromDate != null && !fromDate.isBlank()) ? fromDate : null;
        // End of day for toDate
        String to = (toDate != null && !toDate.isBlank()) ? toDate + " 23:59:59" : null;

        return orderRepository.findOrdersFiltered(statusParam, from, to, pageable)
                .map(this::toOrderDTOWithItems);
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

    @Override
    @Transactional(readOnly = true)
    public OrderSummaryDTO getSummary() {
        List<Order> all = orderRepository.findAllOrders();
        long total = all.size();
        long pending = all.stream().filter(o -> "PENDING".equalsIgnoreCase(o.getOrderStatus())).count();
        long outForDelivery = all.stream().filter(o -> "OUT_FOR_DELIVERY".equalsIgnoreCase(o.getOrderStatus())).count();
        long completed = all.stream().filter(o -> "DELIVERED".equalsIgnoreCase(o.getOrderStatus())
                || "COMPLETED".equalsIgnoreCase(o.getOrderStatus())).count();
        return new OrderSummaryDTO(total, pending, outForDelivery, completed);
    }

    private OrderDTO toOrderDTOWithItems(Order order) {
        OrderDTO dto = orderMapper.toDTO(order);
        dto.setOrderItems(
                orderItemRepository.findByOrderId(order.getId())
                        .stream()
                        .map(this::toEnrichedItemDTO)
                        .collect(Collectors.toList())
        );
        // Enrich with customer info
        if (order.getClientId() != null) {
            usersRepository.findById(order.getClientId()).ifPresent(user -> {
                String name = buildName(user);
                dto.setCustomerName(name);
                dto.setCustomerPhone(user.getPhoneNumber());
            });
        }
        // Enrich with rider info
        if (order.getRiderId() != null) {
            userRiderRepository.findById(order.getRiderId()).ifPresent(rider -> {
                dto.setRiderName(rider.getName());
                dto.setRiderPhone(rider.getContactPhone() != null ? rider.getContactPhone() : rider.getContactNo());
                dto.setRiderImageUrl(rider.getImageUrl());
                dto.setRiderVehicleType(rider.getVehicleType());
                dto.setRiderPlateNumber(rider.getPlateNumber());
                dto.setRiderRating(rider.getRating());
            });
        }
        return dto;
    }

    private String buildName(Users user) {
        String first = user.getFirstName() != null ? user.getFirstName() : "";
        String last = user.getLastName() != null ? user.getLastName() : "";
        String full = (first + " " + last).trim();
        return full.isEmpty() ? user.getPhoneNumber() : full;
    }

    private OrderItemDTO toEnrichedItemDTO(OrderItem item) {
        OrderItemDTO dto = orderItemMapper.toDTO(item);
        if (item.getProductId() != null) {
            productRepository.findById(item.getProductId()).ifPresent(product -> {
                dto.setProductName(product.getName());
                dto.setImageUrl(product.getImageUrl());
            });
        }
        return dto;
    }
}
