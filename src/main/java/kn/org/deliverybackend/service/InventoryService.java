package kn.org.deliverybackend.service;

import kn.org.deliverybackend.dto.request.product.AdminStockUpdateRequestDTO;
import kn.org.deliverybackend.dto.response.product.StockResponseDTO;
import kn.org.deliverybackend.entity.Product;
import kn.org.deliverybackend.enumeration.StockStatus;

public interface InventoryService {
    StockStatus computeStatus(Product product);
    StockResponseDTO getStockStatus(Long productId);
    StockResponseDTO updateStock(Long productId, AdminStockUpdateRequestDTO request);
    Product lockAndGetProduct(Long productId);
}
