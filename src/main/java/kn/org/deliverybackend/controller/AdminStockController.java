package kn.org.deliverybackend.controller;

import jakarta.validation.Valid;
import kn.org.deliverybackend.dto.request.product.AdminStockUpdateRequestDTO;
import kn.org.deliverybackend.dto.response.product.StockResponseDTO;
import kn.org.deliverybackend.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminStockController {

    private final InventoryService inventoryService;

    @PutMapping("/{id}/stock")
    public ResponseEntity<StockResponseDTO> updateStock(
            @PathVariable Long id,
            @Valid @RequestBody AdminStockUpdateRequestDTO request) {
        return ResponseEntity.ok(inventoryService.updateStock(id, request));
    }

    @GetMapping("/{id}/stock")
    public ResponseEntity<StockResponseDTO> getStock(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getStockStatus(id));
    }
}
