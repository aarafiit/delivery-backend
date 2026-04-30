# Requirements Document

## Introduction

This document defines the requirements for a real-time inventory and stock management system integrated into the existing Spring Boot e-commerce/food delivery backend. The system tracks product stock quantities, enforces stock validation during order placement and cart checkout, prevents race conditions under concurrent load, and exposes admin APIs for stock management. It also provides real-time stock status updates to clients via polling and WebSocket.

## Glossary

- **Inventory_System**: The overall stock management subsystem being specified here.
- **Product**: An existing JPA entity (`product` table) representing a sellable item in the platform.
- **StockQuantity**: The integer count of units currently available for a given Product.
- **LowStockThreshold**: An optional integer per-Product that defines the boundary below which stock is considered low.
- **StockStatus**: A computed (non-persisted) enum value derived from StockQuantity — one of `IN_STOCK`, `LOW_STOCK`, or `OUT_OF_STOCK`.
- **Order**: An existing JPA entity (`orders` table) representing a placed customer order.
- **OrderItem**: An existing JPA entity (`order_item` table) representing a line item within an Order, carrying a product ID and quantity.
- **Cart**: An existing JPA entity (`cart` table) representing items a user intends to purchase.
- **OrderService**: A new service responsible for order placement, including stock validation and deduction.
- **InventoryService**: A new service responsible for stock reads, updates, and status computation.
- **AdminStockController**: A new REST controller exposing admin-only stock management endpoints.
- **StockUpdateEvent**: An application event published when a product's StockQuantity changes.
- **GlobalExceptionHandler**: A `@ControllerAdvice` class that maps domain exceptions to HTTP error responses.

---

## Requirements

### Requirement 1: Product Entity Stock Fields

**User Story:** As a system administrator, I want the Product entity to carry stock quantity and a low-stock threshold, so that inventory data is persisted alongside product information.

#### Acceptance Criteria

1. THE Product SHALL include a `stockQuantity` field of type `int` with a default value of `0`.
2. THE Product SHALL include a `lowStockThreshold` field of type `Integer` (nullable) with no default constraint.
3. WHEN the Product entity is persisted or updated, THE Inventory_System SHALL preserve all existing Product fields without modification.
4. THE Product `stockQuantity` field SHALL be constrained to a minimum value of `0` at the database level via a check constraint.

---

### Requirement 2: Stock Status Computation

**User Story:** As a developer, I want stock status to be computed dynamically from the current stock quantity, so that it always reflects the true state without requiring a separate stored column.

#### Acceptance Criteria

1. THE InventoryService SHALL compute `StockStatus` as `OUT_OF_STOCK` when `stockQuantity` equals `0`.
2. THE InventoryService SHALL compute `StockStatus` as `LOW_STOCK` when `stockQuantity` is greater than `0` and less than or equal to `lowStockThreshold` (when `lowStockThreshold` is set).
3. THE InventoryService SHALL compute `StockStatus` as `IN_STOCK` when `stockQuantity` is greater than `lowStockThreshold` (when `lowStockThreshold` is set).
4. WHERE `lowStockThreshold` is null, THE InventoryService SHALL compute `StockStatus` as `IN_STOCK` when `stockQuantity` is greater than `0`.
5. THE `StockStatus` value SHALL NOT be stored as a column in the `product` table.

---

### Requirement 3: Product API Response with Stock Information

**User Story:** As a client application, I want product API responses to include stock quantity and stock status, so that I can display availability information to users.

#### Acceptance Criteria

1. WHEN a client requests any product endpoint, THE Product API SHALL include `stockQuantity` (integer) in the response body.
2. WHEN a client requests any product endpoint, THE Product API SHALL include `stockStatus` (enum string: `IN_STOCK`, `LOW_STOCK`, or `OUT_OF_STOCK`) in the response body.
3. THE `stockStatus` field in the response SHALL be computed by the InventoryService at response time and SHALL NOT be read from a database column.

---

### Requirement 4: Order Placement with Stock Validation and Deduction

**User Story:** As a customer, I want the system to validate and reserve stock when I place an order, so that I never receive a confirmation for items that are unavailable.

#### Acceptance Criteria

1. WHEN an order is placed, THE OrderService SHALL validate that `stockQuantity` is greater than or equal to the requested quantity for every OrderItem before persisting the Order.
2. WHEN stock validation passes for all OrderItems, THE OrderService SHALL deduct the ordered quantity from `stockQuantity` for each corresponding Product within the same `@Transactional` boundary.
3. IF any OrderItem's requested quantity exceeds the available `stockQuantity`, THEN THE OrderService SHALL throw an `InsufficientStockException` and SHALL NOT persist the Order or modify any stock quantities.
4. THE OrderService SHALL use `@Transactional` on the order placement method to ensure atomicity of stock deduction and order persistence.
5. WHEN stock deduction results in `stockQuantity` reaching `0`, THE Inventory_System SHALL NOT allow `stockQuantity` to go below `0`.

---

### Requirement 5: Concurrency Control for Stock Updates

**User Story:** As a system architect, I want concurrent order placements to be handled safely, so that two users ordering the last unit simultaneously cannot both succeed.

#### Acceptance Criteria

1. THE ProductRepository SHALL expose a method that acquires a pessimistic write lock (`PESSIMISTIC_WRITE`) on a Product row before reading its `stockQuantity` during order placement.
2. WHEN two concurrent requests attempt to deduct stock from the same Product simultaneously, THE Inventory_System SHALL serialize the deductions so that only one succeeds if insufficient stock remains for both.
3. WHEN a pessimistic lock cannot be acquired within the transaction timeout, THE Inventory_System SHALL propagate a lock acquisition failure as an `InsufficientStockException` to the caller.

---

### Requirement 6: Admin Stock Management API

**User Story:** As an administrator, I want a dedicated API to update product stock quantities, so that I can restock items or make manual corrections without modifying other product fields.

#### Acceptance Criteria

1. THE AdminStockController SHALL expose `PUT /admin/products/{id}/stock` to update the `stockQuantity` of a Product.
2. WHEN a valid stock update request is received, THE AdminStockController SHALL accept a `quantity` field (integer) and an `operation` field (`SET`, `INCREMENT`, or `DECREMENT`).
3. WHEN `operation` is `SET`, THE InventoryService SHALL set `stockQuantity` to the provided `quantity` value.
4. WHEN `operation` is `INCREMENT`, THE InventoryService SHALL add the provided `quantity` to the current `stockQuantity`.
5. WHEN `operation` is `DECREMENT`, THE InventoryService SHALL subtract the provided `quantity` from the current `stockQuantity`.
6. IF a `DECREMENT` operation would result in `stockQuantity` falling below `0`, THEN THE InventoryService SHALL throw an `InvalidStockOperationException`.
7. IF the provided `quantity` is less than or equal to `0` for `INCREMENT` or `DECREMENT` operations, THEN THE InventoryService SHALL throw an `InvalidStockOperationException`.
8. THE AdminStockController SHALL also expose `GET /admin/products/{id}/stock` to retrieve the current `stockQuantity` and computed `StockStatus` for a Product.

---

### Requirement 7: Cart Stock Validation Before Checkout

**User Story:** As a customer, I want the system to validate that all items in my cart are still in stock before I proceed to checkout, so that I am informed of any stock changes before placing my order.

#### Acceptance Criteria

1. THE CartService SHALL expose a `validateCartStock(UUID userId)` method that checks the current `stockQuantity` for every Cart item belonging to the user.
2. WHEN `validateCartStock` is called and all Cart items have sufficient stock, THE CartService SHALL return a successful validation result.
3. WHEN `validateCartStock` is called and one or more Cart items have insufficient stock, THE CartService SHALL return a `CartStockValidationResult` containing the list of affected product IDs and their available quantities.
4. IF a Cart item references a Product whose `stockQuantity` is `0`, THEN THE CartService SHALL mark that item as `OUT_OF_STOCK` in the validation result.
5. THE CartService SHALL expose `POST /app/consumer/{userId}/cart/validate-stock` as the HTTP endpoint for cart stock validation.

---

### Requirement 8: Real-Time Stock Updates — Polling

**User Story:** As a client application, I want to poll for current stock status of specific products, so that I can refresh displayed availability without requiring a persistent connection.

#### Acceptance Criteria

1. THE Product API SHALL expose `GET /api/products/{id}/stock-status` returning the current `stockQuantity` and `StockStatus` for a single Product.
2. WHEN a client polls `GET /api/products/{id}/stock-status`, THE Inventory_System SHALL return the latest persisted `stockQuantity` and the computed `StockStatus` within 200ms under normal load.
3. THE polling endpoint SHALL be accessible without authentication for read operations.

---

### Requirement 9: Real-Time Stock Updates — WebSocket

**User Story:** As a client application, I want to receive push notifications when stock levels change, so that I can update the UI immediately without polling.

#### Acceptance Criteria

1. THE Inventory_System SHALL provide a STOMP-over-WebSocket endpoint at `/ws/inventory` for clients to connect to.
2. WHEN a Product's `stockQuantity` changes, THE Inventory_System SHALL publish a `StockUpdateEvent` to the `/topic/stock/{productId}` destination.
3. THE `StockUpdateEvent` message SHALL contain `productId`, `stockQuantity`, and `stockStatus`.
4. WHEN a client subscribes to `/topic/stock/{productId}`, THE Inventory_System SHALL deliver stock update messages to that client whenever the corresponding Product's stock changes.

---

### Requirement 10: Edge Case and Error Handling

**User Story:** As a developer, I want all stock-related error conditions to produce clear, structured HTTP error responses, so that client applications can handle failures gracefully.

#### Acceptance Criteria

1. IF an order is placed for a Product with `stockQuantity` equal to `0`, THEN THE GlobalExceptionHandler SHALL return HTTP `409 Conflict` with a structured error body containing the product ID and a message indicating out-of-stock status.
2. IF a stock update request provides a negative or zero quantity for `INCREMENT` or `DECREMENT`, THEN THE GlobalExceptionHandler SHALL return HTTP `400 Bad Request`.
3. IF concurrent order placement causes a lock contention failure, THEN THE GlobalExceptionHandler SHALL return HTTP `409 Conflict` with a message indicating the item is temporarily unavailable.
4. IF a cart validation detects stock mismatch at checkout, THEN THE GlobalExceptionHandler SHALL return HTTP `409 Conflict` with the list of affected products and their available quantities.
5. WHEN any stock-related exception is thrown, THE GlobalExceptionHandler SHALL log the exception with sufficient context (product ID, requested quantity, available quantity) for debugging.
