# Design Document: Real-Time Inventory / Stock Management System

## Overview

This design adds a production-grade inventory management layer to the existing Spring Boot delivery backend. The system tracks per-product stock quantities, enforces transactional stock deduction during order placement, prevents race conditions via pessimistic locking, exposes admin stock management APIs, validates cart stock before checkout, and pushes real-time stock updates to clients via both polling and WebSocket (STOMP).

The implementation is additive — no existing entity fields, endpoints, or service contracts are removed. All new code follows the project's established patterns: JPA entities extending `AbstractBaseEntity`, MapStruct mappers, service interfaces with `impl/` implementations, and `@RestController` controllers.

---

## Architecture

```mermaid
graph TD
    subgraph Client
        A[Mobile / Web App]
    end

    subgraph REST Layer
        B[ProductController]
        C[AdminStockController]
        D[CartController]
        E[OrderController]
    end

    subgraph Service Layer
        F[InventoryService / Impl]
        G[OrderService / Impl]
        H[CartService / Impl]
        I[ProductService / Impl]
    end

    subgraph Repository Layer
        J[ProductRepository]
        K[OrderRepository]
        L[OrderItemRepository]
        M[CartRepository]
    end

    subgraph Real-Time
        N[StockWebSocketController]
        O[SimpMessagingTemplate]
        P[ApplicationEventPublisher]
        Q[StockUpdateEventListener]
    end

    subgraph DB
        R[(PostgreSQL)]
    end

    A -->|REST poll| B
    A -->|REST admin| C
    A -->|REST cart| D
    A -->|REST order| E
    A -->|STOMP WS| N

    B --> I
    C --> F
    D --> H
    E --> G

    I --> J
    F --> J
    G --> J
    G --> K
    G --> L
    H --> M
    H --> J

    G -->|publishes StockUpdateEvent| P
    F -->|publishes StockUpdateEvent| P
    P --> Q
    Q --> O
    O -->|/topic/stock/{productId}| A

    J --> R
    K --> R
    L --> R
    M --> R
```

### Key Design Decisions

1. **Pessimistic locking over optimistic**: `AbstractBaseEntity` already has `@Version` for optimistic locking, but under high concurrency for a single hot product (e.g., last unit), optimistic locking causes many retries and `ObjectOptimisticLockingFailureException` storms. Pessimistic write locking serializes access at the DB row level, which is the correct choice for inventory deduction.

2. **Event-driven WebSocket push**: Stock changes (from order placement or admin updates) publish a `StockUpdateEvent` via Spring's `ApplicationEventPublisher`. A `@TransactionalEventListener(phase = AFTER_COMMIT)` listener then broadcasts via STOMP. This ensures the WebSocket message is only sent after the DB transaction commits successfully.

3. **Computed StockStatus**: Status is never stored — it is computed in `InventoryService.computeStatus(product)` and injected into response DTOs by the mapper. This avoids stale status columns and eliminates the need for update triggers.

4. **OrderService is new**: The existing codebase has `OrderHistoryService` (read-only). A new `OrderService` handles order placement with stock validation and deduction.

---

## Components and Interfaces

### New Exceptions

```java
// HTTP 409 — thrown when stock is insufficient for an order or cart checkout
InsufficientStockException(Long productId, int requested, int available)

// HTTP 400 — thrown when an admin stock operation has invalid parameters
InvalidStockOperationException(String message)
```

### StockStatus Enum

```java
package kn.org.deliverybackend.enumeration;

public enum StockStatus {
    IN_STOCK,
    LOW_STOCK,
    OUT_OF_STOCK
}
```

### InventoryService Interface

```java
public interface InventoryService {
    StockStatus computeStatus(Product product);
    StockResponseDTO getStockStatus(Long productId);
    StockResponseDTO updateStock(Long productId, AdminStockUpdateRequestDTO request);
    // Used internally by OrderService and CartService
    Product lockAndGetProduct(Long productId); // acquires PESSIMISTIC_WRITE lock
}
```

### OrderService Interface

```java
public interface OrderService {
    OrderDTO placeOrder(PlaceOrderRequestDTO request);
}
```

### CartService (extended)

```java
// Added to existing CartService interface:
CartStockValidationResult validateCartStock(UUID userId);
```

### AdminStockController

```
PUT  /admin/products/{id}/stock   → updateStock(id, AdminStockUpdateRequestDTO)
GET  /admin/products/{id}/stock   → getStockStatus(id)
```

### ProductController (extended)

```
GET  /api/products/{id}/stock-status  → getStockStatus(id)   [polling endpoint]
```

### OrderController (new)

```
POST /app/consumer/{userId}/orders    → placeOrder(userId, PlaceOrderRequestDTO)
```

### CartController (extended)

```
POST /app/consumer/{userId}/cart/validate-stock  → validateCartStock(userId)
```

### WebSocket Configuration

```
Endpoint:    /ws/inventory  (SockJS fallback enabled)
Broker:      /topic
App prefix:  /app
```

---

## Data Models

### Product Entity Changes

Add two fields to the existing `Product` entity:

```java
@Column(name = "stock_quantity", nullable = false, columnDefinition = "int default 0 check (stock_quantity >= 0)")
private int stockQuantity = 0;

@Column(name = "low_stock_threshold")
private Integer lowStockThreshold;
```

`spring.jpa.hibernate.ddl-auto=update` will add these columns automatically on startup.

### New DTOs

#### ProductResponseDTO (updated)

Add to existing `ProductResponseDTO`:
```java
private int stockQuantity;
private StockStatus stockStatus;
```

#### StockResponseDTO

```java
public class StockResponseDTO {
    private Long productId;
    private String productName;
    private int stockQuantity;
    private StockStatus stockStatus;
    private Integer lowStockThreshold;
}
```

#### AdminStockUpdateRequestDTO

```java
public class AdminStockUpdateRequestDTO {
    @NotNull
    private StockOperation operation;  // SET, INCREMENT, DECREMENT

    @NotNull
    @Min(0)
    private Integer quantity;
}

public enum StockOperation { SET, INCREMENT, DECREMENT }
```

#### PlaceOrderRequestDTO

```java
public class PlaceOrderRequestDTO {
    private UUID clientId;
    private String deliveryAddress;
    private String paymentMethod;
    private Long shopId;
    private Double deliveryCharge;
    private Float latitude;
    private Float longitude;
    private List<OrderItemRequestDTO> items;
}

public class OrderItemRequestDTO {
    private Long productId;   // matches Product.id (Long)
    private Integer quantity;
    private Long variantId;
}
```

#### CartStockValidationResult

```java
public class CartStockValidationResult {
    private boolean valid;
    private List<StockMismatchItem> mismatches;
}

public class StockMismatchItem {
    private UUID productId;
    private int requestedQuantity;
    private int availableQuantity;
    private StockStatus stockStatus;
}
```

#### StockUpdateEvent (Spring ApplicationEvent)

```java
public class StockUpdateEvent extends ApplicationEvent {
    private final Long productId;
    private final int stockQuantity;
    private final StockStatus stockStatus;
}
```

#### StockUpdateMessage (WebSocket payload)

```java
public class StockUpdateMessage {
    private Long productId;
    private int stockQuantity;
    private StockStatus stockStatus;
    private Instant timestamp;
}
```

### ProductMapper Changes

MapStruct cannot compute `StockStatus` automatically. The mapper will use a `@Named` method or delegate to `InventoryService`:

```java
@Mapper(componentModel = "spring", uses = {InventoryServiceMapper.class})
public interface ProductMapper {
    @Mapping(target = "stockStatus", source = "product", qualifiedByName = "computeStockStatus")
    ProductResponseDTO toResponseDTO(Product product);
    // ...
}
```

A simpler approach (used in implementation): `ProductServiceImpl` sets `stockStatus` on the DTO after mapping, calling `inventoryService.computeStatus(product)`.

---

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system — essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: StockStatus computation is total, deterministic, and correct

*For any* non-negative `stockQuantity` and any nullable `lowStockThreshold`, `InventoryService.computeStatus(product)` SHALL return:
- `OUT_OF_STOCK` when `stockQuantity == 0`
- `LOW_STOCK` when `stockQuantity > 0` and `lowStockThreshold != null` and `stockQuantity <= lowStockThreshold`
- `IN_STOCK` when `stockQuantity > lowStockThreshold` (threshold set) or `stockQuantity > 0` (threshold null)

The same inputs SHALL always produce the same output.

**Validates: Requirements 2.1, 2.2, 2.3, 2.4**

---

### Property 2: Stock deduction is correct and never produces negative stock

*For any* Product with `stockQuantity == s >= 0` and any valid order deduction of quantity `q` where `0 < q <= s`, after deduction `stockQuantity` SHALL equal `s - q` and SHALL be `>= 0`.

**Validates: Requirements 4.2, 4.5**

---

### Property 3: Insufficient stock always rejects the order atomically

*For any* order where at least one OrderItem requests a quantity greater than the Product's current `stockQuantity`, the OrderService SHALL throw `InsufficientStockException` and the `stockQuantity` of ALL Products referenced in the order SHALL remain unchanged after the failed attempt.

**Validates: Requirements 4.1, 4.3**

---

### Property 4: Admin stock operations produce correct results

*For any* Product with `stockQuantity == s` and any non-negative integer `n` and any positive integer `delta <= s`:
- `SET(n)` SHALL result in `stockQuantity == n`
- `SET(n)` applied twice SHALL result in `stockQuantity == n` (idempotent)
- `INCREMENT(delta)` SHALL result in `stockQuantity == s + delta`
- `DECREMENT(delta)` SHALL result in `stockQuantity == s - delta`
- `INCREMENT(delta)` followed by `DECREMENT(delta)` SHALL result in `stockQuantity == s` (round-trip)

**Validates: Requirements 6.3, 6.4, 6.5**

---

### Property 5: Invalid admin stock operations are always rejected

*For any* Product with `stockQuantity == s` and any integer `delta > s`, calling `DECREMENT(delta)` SHALL throw `InvalidStockOperationException` and `stockQuantity` SHALL remain `s`. For any non-positive quantity with `INCREMENT` or `DECREMENT`, the same exception SHALL be thrown.

**Validates: Requirements 6.6, 6.7**

---

### Property 6: Cart validation result accurately reflects stock state

*For any* user's Cart containing items with arbitrary quantities, the `CartStockValidationResult` returned by `validateCartStock` SHALL mark an item as a mismatch if and only if the Product's current `stockQuantity` is strictly less than the Cart item's quantity. Items with sufficient stock SHALL NOT appear in the mismatch list.

**Validates: Requirements 7.2, 7.3, 7.4**

---

### Property 7: StockStatus in API response matches computed status

*For any* Product retrieved via any product endpoint, the `stockStatus` field in the response SHALL equal `InventoryService.computeStatus(product)` evaluated at the time of the request, and `stockQuantity` in the response SHALL equal the persisted value.

**Validates: Requirements 3.1, 3.2, 3.3**

---

### Property 8: Concurrent stock deductions are serialized correctly

*For any* Product with `stockQuantity == 1` and two concurrent order requests each requesting quantity `1`, exactly one SHALL succeed and one SHALL receive `InsufficientStockException`, and the final `stockQuantity` SHALL be `0`.

**Validates: Requirements 5.2**

---

### Property 9: WebSocket events are complete and accurate

*For any* stock change (via order placement or admin update), the published `StockUpdateEvent` SHALL contain the correct `productId`, the updated `stockQuantity`, and the `stockStatus` computed from the updated quantity.

**Validates: Requirements 9.2, 9.3**

---

## Error Handling

A new `GlobalExceptionHandler` (`@RestControllerAdvice`) maps domain exceptions to HTTP responses:

| Exception | HTTP Status | Response Body |
|---|---|---|
| `InsufficientStockException` | 409 Conflict | `{ productId, requested, available, message }` |
| `InvalidStockOperationException` | 400 Bad Request | `{ message }` |
| `ResourceNotFoundException` | 404 Not Found | `{ message }` (already exists, extend it) |
| `MethodArgumentNotValidException` | 400 Bad Request | `{ errors[] }` |
| Lock contention (`PessimisticLockingFailureException`) | 409 Conflict | `{ message: "Item temporarily unavailable, please retry" }` |

All handlers log at `WARN` level with product ID, requested quantity, and available quantity where applicable.

---

## Testing Strategy

### Unit Tests (JUnit 5 + Mockito)

Focus on specific examples, edge cases, and error conditions:

- `InventoryServiceImplTest`: test `computeStatus` for boundary values (0, threshold, threshold+1, null threshold)
- `OrderServiceImplTest`: test order placement with sufficient stock, insufficient stock (single item, multiple items), and zero-stock products
- `CartServiceImplTest`: test `validateCartStock` with all-valid cart, partially invalid cart, fully invalid cart
- `AdminStockControllerTest` (MockMvc): test `PUT /admin/products/{id}/stock` for each operation type and error cases

### Property-Based Tests (jqwik)

jqwik is the standard property-based testing library for Java/JUnit 5. It integrates directly with JUnit 5 via `@Property` annotations and generates random inputs automatically.

Add to `build.gradle`:
```groovy
testImplementation 'net.jqwik:jqwik:1.8.4'
```

Each property test runs a minimum of 100 tries (jqwik default is 1000).

Tag format: `Feature: inventory-stock-management, Property {N}: {property_text}`

**Property test mapping:**

| Property | Test Class | jqwik Arbitraries |
|---|---|---|
| P1: StockStatus computation | `StockStatusPropertyTest` | `@ForAll @IntRange(min=0) int stockQty`, `@ForAll @Nullable Integer threshold` |
| P2: Stock deduction correctness | `StockDeductionPropertyTest` | `@ForAll @IntRange(min=1) int stock`, `@ForAll @IntRange(min=1) int qty` (qty <= stock) |
| P3: Insufficient stock atomicity | `OrderAtomicityPropertyTest` | Random order with one item exceeding stock |
| P4: Admin stock operations | `AdminStockPropertyTest` | `@ForAll @IntRange(min=0) int n`, `@ForAll @IntRange(min=1) int delta` |
| P5: Invalid operations rejected | `AdminStockPropertyTest` | `@ForAll @IntRange(min=1) int delta > stock` |
| P6: Cart validation accuracy | `CartValidationPropertyTest` | Random cart items vs random stock levels |
| P7: API response matches computed | `ProductResponsePropertyTest` | Random products with varied stock/threshold |
| P8: Concurrent deductions serialized | `ConcurrencyPropertyTest` | Two threads, stock=1, qty=1 each |
| P9: WebSocket event correctness | `StockEventPropertyTest` | Random stock changes, verify event fields |

Each property test MUST include a comment:
```java
// Feature: inventory-stock-management, Property N: <property description>
```
