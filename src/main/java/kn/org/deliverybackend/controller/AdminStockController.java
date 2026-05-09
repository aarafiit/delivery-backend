package kn.org.deliverybackend.controller;

import jakarta.validation.Valid;
import kn.org.deliverybackend.dto.request.product.AdminStockUpdateRequestDTO;
import kn.org.deliverybackend.dto.response.product.InventorySummaryDTO;
import kn.org.deliverybackend.dto.response.product.StockResponseDTO;
import kn.org.deliverybackend.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AdminStockController {

    private final InventoryService inventoryService;

    /** Summary counts for the 3 inventory cards */
    @GetMapping("/inventory/summary")
    public ResponseEntity<InventorySummaryDTO> getSummary() {
        return ResponseEntity.ok(inventoryService.getSummary());
    }

    /** Full inventory list with SKU, unit, threshold */
    @GetMapping("/inventory")
    public ResponseEntity<List<StockResponseDTO>> getAllStock() {
        return ResponseEntity.ok(inventoryService.getAllStockDetails());
    }

    @GetMapping("/products/{id}/stock")
    public ResponseEntity<StockResponseDTO> getStock(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getStockStatus(id));
    }

    @PutMapping("/products/{id}/stock")
    public ResponseEntity<StockResponseDTO> updateStock(
            @PathVariable Long id,
            @Valid @RequestBody AdminStockUpdateRequestDTO request) {
        return ResponseEntity.ok(inventoryService.updateStock(id, request));
    }
}
