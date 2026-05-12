# Implementation Plan: Real-Time Inventory / Stock Management System

## Overview

Incremental implementation of the inventory system on top of the existing Spring Boot backend. Each task builds on the previous, starting with the data layer and working up through services, controllers, and real-time features. All tasks are coding tasks only.

## Tasks

- [x] 1. Add stock fields to Product entity and update ProductResponseDTO
  - Add `stockQuantity` (int, default 0, check constraint `>= 0`) and `lowStockThreshold` (Integer, nullable) to `Product.java`
  - Add `stockQuantity` (int) and `stockStatus` (StockStatus enum) fields to `ProductResponseDTO.java`
  - Create `StockStatus` enum in `kn.org.deliverybackend.enumeration` with values `IN_STOCK`, `LOW_STOCK`, `OUT_OF_STOCK`
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 2.5, 3.1, 3.2_

- [x] 2. Implement InventoryService with stock status computation
  - [x] 2.1 Create `InventoryService` interface and `InventoryServiceImpl` in `service/` and `service/impl/`
    - Implement `computeStatus(Product product)` following the threshold logic from Requirements 2.1–2.4
    - Implement `getStockStatus(Long productId)` returning `StockResponseDTO`
    - Create `StockResponseDTO` in `dto/response/product/`
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 3.3_

  - [ ]* 2.2 Write property test for StockStatus computation (Property 1)
    - Use jqwik `@Property` with `@ForAll @IntRange(min=0) int stockQty` and `@ForAll @Nullable Integer threshold`
    - Verify OUT_OF_STOCK when qty==0, LOW_STOCK when 0 < qty <= threshold, IN_STOCK otherwise
    - Add jqwik dependency to `build.gradle`: `testImplementation 'net.jqwik:jqwik:1.8.4'`
    - `// Feature: inventory-stock-management, Property 1: StockStatus computation is total, deterministic, and correct`
    - _Requirements: 2.1, 2.2, 2.3, 2.4_

- [x] 3. Update ProductService and ProductMapper to include stock fields in responses
  - Update `ProductMapper.toResponseDTO()` to map `stockQuantity` from entity
  - Update `ProductServiceImpl` to call `inventoryService.computeStatus(product)` and set `stockStatus` on the response DTO after mapping
  - Inject `InventoryService` into `ProductServiceImpl`
  - _Requirements: 3.1, 3.2, 3.3_

  - [ ]* 3.1 Write property test for API response stock fields (Property 7)
    - Generate random Products with varied stockQuantity and lowStockThreshold
    - Verify that `stockStatus` in the mapped DTO equals `computeStatus()` result and `stockQuantity` matches entity value
    - `// Feature: inventory-stock-management, Property 7: StockStatus in API response matches computed status`
    - _Requirements: 3.1, 3.2, 3.3_

- [x] 4. Add pessimistic locking to ProductRepository and implement stock deduction
  - Add `findByIdWithLock(@Param("id") Long id)` method to `ProductRepository` using `@Lock(LockModeType.PESSIMISTIC_WRITE)` and `@Query`
  - Implement `lockAndGetProduct(Long productId)` in `InventoryServiceImpl` using the locked repository method
  - _Requirements: 5.1_

- [x] 5. Implement OrderService with transactional stock validation and deduction
  - [x] 5.1 Create `InsufficientStockException` in `exception/` package
    - Constructor: `InsufficientStockException(Long productId, int requested, int available)`
    - _Requirements: 4.3, 5.3_

  - [x] 5.2 Create `PlaceOrderRequestDTO` and `OrderItemRequestDTO` in `dto/request/order/`
    - Fields per design document Data Models section
    - _Requirements: 4.1_

  - [x] 5.3 Create `OrderService` interface and `OrderServiceImpl`
    - Implement `placeOrder(PlaceOrderRequestDTO request)` with `@Transactional`
    - For each item: call `lockAndGetProduct(productId)`, validate `stockQuantity >= requested`, deduct stock, save Product
    - If any item fails validation: throw `InsufficientStockException` (transaction rolls back automatically)
    - Persist `Order` and `OrderItem` entities after all stock is validated and deducted
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 5.1, 5.2_

  - [ ]* 5.4 Write property test for stock deduction correctness (Property 2)
    - Generate random `stockQuantity >= 1` and `quantity` in `[1, stockQuantity]`
    - Verify post-deduction stock equals original minus quantity and is >= 0
    - `// Feature: inventory-stock-management, Property 2: Stock deduction is correct and never produces negative stock`
    - _Requirements: 4.2, 4.5_

  - [ ]* 5.5 Write property test for order atomicity on insufficient stock (Property 3)
    - Generate a random order where one item's quantity exceeds stock
    - Verify `InsufficientStockException` is thrown and all product stock quantities are unchanged
    - `// Feature: inventory-stock-management, Property 3: Insufficient stock always rejects the order atomically`
    - _Requirements: 4.1, 4.3_

- [x] 6. Create OrderController for order placement
  - Create `OrderController` in `controller/` with `POST /app/consumer/{userId}/orders`
  - Delegate to `OrderService.placeOrder()`
  - _Requirements: 4.1, 4.2, 4.3_

- [ ] 7. Checkpoint — Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 8. Implement admin stock management API
  - [x] 8.1 Create `InvalidStockOperationException` in `exception/` package
    - _Requirements: 6.6, 6.7_

  - [x] 8.2 Create `AdminStockUpdateRequestDTO` in `dto/request/product/` and `StockOperation` enum in `enumeration/`
    - Fields: `StockOperation operation` (SET, INCREMENT, DECREMENT), `Integer quantity` (validated `@Min(0)`)
    - _Requirements: 6.2_

  - [x] 8.3 Implement `updateStock(Long productId, AdminStockUpdateRequestDTO request)` in `InventoryServiceImpl`
    - Handle SET, INCREMENT, DECREMENT with validation per Requirements 6.3–6.7
    - Throw `InvalidStockOperationException` for invalid operations
    - _Requirements: 6.3, 6.4, 6.5, 6.6, 6.7_

  - [x] 8.4 Create `AdminStockController` in `controller/`
    - `PUT /admin/products/{id}/stock` → `inventoryService.updateStock()`
    - `GET /admin/products/{id}/stock` → `inventoryService.getStockStatus()`
    - _Requirements: 6.1, 6.8_

  - [ ]* 8.5 Write property test for admin stock operations (Properties 4 and 5)
    - P4: For any product and any n >= 0, SET(n) results in stockQuantity == n; SET(n) twice still == n; INCREMENT(delta) == s+delta; DECREMENT(delta) == s-delta; INCREMENT then DECREMENT round-trips
    - P5: For any delta > s, DECREMENT throws; for any non-positive qty with INCREMENT/DECREMENT, throws
    - `// Feature: inventory-stock-management, Property 4: Admin stock operations produce correct results`
    - `// Feature: inventory-stock-management, Property 5: Invalid admin stock operations are always rejected`
    - _Requirements: 6.3, 6.4, 6.5, 6.6, 6.7_

- [x] 9. Implement cart stock validation
  - [x] 9.1 Create `CartStockValidationResult` and `StockMismatchItem` DTOs in `dto/response/`
    - _Requirements: 7.3_

  - [x] 9.2 Implement `validateCartStock(UUID userId)` in `CartServiceImpl`
    - Fetch all non-deleted cart items for user
    - For each item, load Product and compare `stockQuantity` vs cart `quantity`
    - Return `CartStockValidationResult` with `valid` flag and list of mismatches
    - _Requirements: 7.1, 7.2, 7.3, 7.4_

  - [x] 9.3 Add `POST /app/consumer/{userId}/cart/validate-stock` to `CartController`
    - Delegate to `cartService.validateCartStock(userId)`
    - _Requirements: 7.5_

  - [ ]* 9.4 Write property test for cart validation accuracy (Property 6)
    - Generate random cart items with random quantities and random product stock levels
    - Verify mismatch list contains exactly the items where cart quantity > stock quantity
    - `// Feature: inventory-stock-management, Property 6: Cart validation result accurately reflects stock state`
    - _Requirements: 7.2, 7.3, 7.4_

- [x] 10. Add polling endpoint for stock status
  - Add `GET /api/products/{id}/stock-status` to `ProductController`
  - Delegate to `inventoryService.getStockStatus(id)`
  - _Requirements: 8.1, 8.2, 8.3_

- [x] 11. Implement GlobalExceptionHandler
  - Create `GlobalExceptionHandler` in `exception/` with `@RestControllerAdvice`
  - Map `InsufficientStockException` → 409 with `{ productId, requested, available, message }`
  - Map `InvalidStockOperationException` → 400 with `{ message }`
  - Map `PessimisticLockingFailureException` → 409 with `{ message: "Item temporarily unavailable, please retry" }`
  - Map `ResourceNotFoundException` → 404 (extend or replace existing handler if one exists)
  - Map `MethodArgumentNotValidException` → 400 with field errors
  - Log all stock exceptions at WARN level with product ID, requested, and available quantities
  - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_

  - [ ]* 11.1 Write unit tests for GlobalExceptionHandler error responses
    - Test each exception type maps to the correct HTTP status and response body shape
    - _Requirements: 10.1, 10.2, 10.3, 10.4_

- [x] 12. Checkpoint — Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 13. Implement WebSocket real-time stock updates
  - [x] 13.1 Add Spring WebSocket dependency to `build.gradle`
    - `implementation 'org.springframework.boot:spring-boot-starter-websocket'`

  - [x] 13.2 Create `WebSocketConfig` in `config/`
    - Configure STOMP endpoint at `/ws/inventory` with SockJS fallback
    - Set message broker prefix `/topic` and app destination prefix `/app`
    - _Requirements: 9.1_

  - [x] 13.3 Create `StockUpdateEvent` (extends `ApplicationEvent`) and `StockUpdateMessage` DTO
    - Fields: `productId`, `stockQuantity`, `stockStatus`, `timestamp`
    - _Requirements: 9.3_

  - [x] 13.4 Create `StockUpdateEventListener` in a new `event/` package
    - Annotate with `@Component`, listen with `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`
    - Inject `SimpMessagingTemplate`, send `StockUpdateMessage` to `/topic/stock/{productId}`
    - _Requirements: 9.2, 9.4_

  - [x] 13.5 Publish `StockUpdateEvent` from `OrderServiceImpl` after successful stock deduction and from `InventoryServiceImpl` after admin stock update
    - Inject `ApplicationEventPublisher` into both services
    - _Requirements: 9.2_

  - [ ]* 13.6 Write property test for WebSocket event correctness (Property 9)
    - For any stock change, verify the published `StockUpdateEvent` contains correct `productId`, updated `stockQuantity`, and `stockStatus` matching `computeStatus()`
    - `// Feature: inventory-stock-management, Property 9: WebSocket events are complete and accurate`
    - _Requirements: 9.2, 9.3_

- [ ] 14. Write concurrency property test (Property 8)
  - [ ]* 14.1 Write property test for concurrent stock deductions (Property 8)
    - Use two threads simultaneously calling `placeOrder` for the same product with `stockQuantity == 1`, each requesting quantity 1
    - Verify exactly one succeeds and one throws `InsufficientStockException`, and final `stockQuantity == 0`
    - `// Feature: inventory-stock-management, Property 8: Concurrent stock deductions are serialized correctly`
    - _Requirements: 5.2_

- [x] 15. Final checkpoint — Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for a faster MVP
- jqwik property tests run 1000 iterations by default — no configuration needed
- `spring.jpa.hibernate.ddl-auto=update` will add the new `stock_quantity` and `low_stock_threshold` columns automatically on startup
- The `@Version` field in `AbstractBaseEntity` provides optimistic locking as a safety net; pessimistic locking in `ProductRepository` is the primary concurrency control for stock deduction
- All property tests must include the comment tag: `// Feature: inventory-stock-management, Property N: <description>`
