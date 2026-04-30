package kn.org.deliverybackend.service.impl;

import kn.org.deliverybackend.dto.OrderDTO;
import kn.org.deliverybackend.dto.OrderItemDTO;
import kn.org.deliverybackend.dto.request.order.OrderItemRequestDTO;
import kn.org.deliverybackend.dto.request.order.PlaceOrderRequestDTO;
import kn.org.deliverybackend.entity.Order;
import kn.org.deliverybackend.entity.OrderItem;
import kn.org.deliverybackend.entity.Product;
import kn.org.deliverybackend.event.StockUpdateEvent;
import kn.org.deliverybackend.exception.InsufficientStockException;
import kn.org.deliverybackend.repository.OrderItemRepository;
import kn.org.deliverybackend.repository.OrderRepository;
import kn.org.deliverybackend.repository.ProductRepository;
import kn.org.deliverybackend.service.InventoryService;
import kn.org.deliverybackend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public OrderDTO placeOrder(PlaceOrderRequestDTO request) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<Product> lockedProducts = new ArrayList<>();
        List<OrderItemRequestDTO> items = request.getItems();

        // Phase 1: lock all products and validate stock
        for (OrderItemRequestDTO item : items) {
            Product product = inventoryService.lockAndGetProduct(item.getProductId());
            int available = product.getStockQuantity();
            int requested = item.getQuantity();

            if (available < requested) {
                throw new InsufficientStockException(product.getId(), requested, available);
            }
            lockedProducts.add(product);
            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(requested)));
        }

        // Phase 2: deduct stock and save products
        for (int i = 0; i < items.size(); i++) {
            Product product = lockedProducts.get(i);
            int newQty = product.getStockQuantity() - items.get(i).getQuantity();
            product.setStockQuantity(newQty);
            productRepository.save(product);
            eventPublisher.publishEvent(new StockUpdateEvent(this, product.getId(), newQty,
                    inventoryService.computeStatus(product)));
        }

        // Phase 3: persist Order
        Order order = new Order();
        order.setClientId(request.getClientId());
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setShopId(request.getShopId());
        order.setDeliveryCharge(request.getDeliveryCharge());
        order.setLatitude(request.getLatitude());
        order.setLongitude(request.getLongitude());
        order.setTotalAmount(totalAmount);
        order.setOrderStatus("PENDING");
        Order savedOrder = orderRepository.save(order);

        // Phase 4: persist OrderItems and build response
        List<OrderItemDTO> orderItemDTOs = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            OrderItemRequestDTO itemReq = items.get(i);
            Product product = lockedProducts.get(i);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(savedOrder.getId());
            orderItem.setQuantity(itemReq.getQuantity());
            orderItem.setPriceAtOrder(product.getPrice());
            orderItem.setVarientId(itemReq.getVariantId());
            OrderItem savedItem = orderItemRepository.save(orderItem);

            OrderItemDTO dto = new OrderItemDTO();
            dto.setId(savedItem.getId());
            dto.setOrderId(savedOrder.getId());
            dto.setQuantity(savedItem.getQuantity());
            dto.setPriceAtOrder(savedItem.getPriceAtOrder());
            dto.setVariantId(savedItem.getVarientId());
            orderItemDTOs.add(dto);
        }

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(savedOrder.getId());
        orderDTO.setClientId(savedOrder.getClientId());
        orderDTO.setDeliveryAddress(savedOrder.getDeliveryAddress());
        orderDTO.setPaymentMethod(savedOrder.getPaymentMethod());
        orderDTO.setShopId(savedOrder.getShopId());
        orderDTO.setDeliveryCharge(savedOrder.getDeliveryCharge());
        orderDTO.setLatitude(savedOrder.getLatitude());
        orderDTO.setLongitude(savedOrder.getLongitude());
        orderDTO.setTotalAmount(savedOrder.getTotalAmount());
        orderDTO.setOrderStatus(savedOrder.getOrderStatus());
        orderDTO.setOrderItems(orderItemDTOs);
        return orderDTO;
    }
}
