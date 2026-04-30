package kn.org.deliverybackend.service.impl;

import kn.org.deliverybackend.dto.request.product.AdminStockUpdateRequestDTO;
import kn.org.deliverybackend.dto.response.product.StockResponseDTO;
import kn.org.deliverybackend.entity.Product;
import kn.org.deliverybackend.enumeration.StockOperation;
import kn.org.deliverybackend.enumeration.StockStatus;
import kn.org.deliverybackend.event.StockUpdateEvent;
import kn.org.deliverybackend.exception.InvalidStockOperationException;
import kn.org.deliverybackend.exception.ResourceNotFoundException;
import kn.org.deliverybackend.repository.ProductRepository;
import kn.org.deliverybackend.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public StockStatus computeStatus(Product product) {
        int qty = product.getStockQuantity();
        if (qty == 0) {
            return StockStatus.OUT_OF_STOCK;
        }
        Integer threshold = product.getLowStockThreshold();
        if (threshold != null && qty <= threshold) {
            return StockStatus.LOW_STOCK;
        }
        return StockStatus.IN_STOCK;
    }

    @Override
    @Transactional(readOnly = true)
    public StockResponseDTO getStockStatus(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        return new StockResponseDTO(
                product.getId(),
                product.getName(),
                product.getStockQuantity(),
                computeStatus(product),
                product.getLowStockThreshold()
        );
    }

    @Override
    @Transactional
    public Product lockAndGetProduct(Long productId) {
        return productRepository.findByIdWithLock(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
    }

    @Override
    @Transactional
    public StockResponseDTO updateStock(Long productId, AdminStockUpdateRequestDTO request) {
        Product product = productRepository.findByIdWithLock(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        int current = product.getStockQuantity();
        int qty = request.getQuantity();
        StockOperation operation = request.getOperation();

        if (operation == StockOperation.INCREMENT || operation == StockOperation.DECREMENT) {
            if (qty <= 0) {
                throw new InvalidStockOperationException(
                        "Quantity must be greater than 0 for " + operation + " operations");
            }
        }

        int newQty;
        switch (operation) {
            case SET:
                newQty = qty;
                break;
            case INCREMENT:
                newQty = current + qty;
                break;
            case DECREMENT:
                if (qty > current) {
                    throw new InvalidStockOperationException(
                            String.format("Cannot decrement by %d: only %d units available", qty, current));
                }
                newQty = current - qty;
                break;
            default:
                throw new InvalidStockOperationException("Unknown operation: " + operation);
        }

        product.setStockQuantity(newQty);
        productRepository.save(product);

        StockStatus newStatus = computeStatus(product);
        eventPublisher.publishEvent(new StockUpdateEvent(this, product.getId(), newQty, newStatus));

        return new StockResponseDTO(
                product.getId(),
                product.getName(),
                product.getStockQuantity(),
                newStatus,
                product.getLowStockThreshold()
        );
    }
}
