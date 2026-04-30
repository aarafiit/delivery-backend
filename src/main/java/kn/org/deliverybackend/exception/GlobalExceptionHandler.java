package kn.org.deliverybackend.exception;

import kn.org.deliverybackend.dto.response.auth.LoginResponse;
import kn.org.deliverybackend.dto.response.auth.RegistrationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── Stock exceptions ──────────────────────────────────────────────────────

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientStock(
            InsufficientStockException ex) {
        log.warn("Insufficient stock: productId={}, requested={}, available={}",
                ex.getProductId(), ex.getRequested(), ex.getAvailable());
        Map<String, Object> body = new HashMap<>();
        body.put("productId", ex.getProductId());
        body.put("requested", ex.getRequested());
        body.put("available", ex.getAvailable());
        body.put("message", ex.getMessage());
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(InvalidStockOperationException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidStockOperation(
            InvalidStockOperationException ex) {
        log.warn("Invalid stock operation: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ResponseEntity<Map<String, Object>> handlePessimisticLock(
            PessimisticLockingFailureException ex) {
        log.warn("Pessimistic lock contention: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("message", "Item temporarily unavailable, please retry");
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(
            ResourceNotFoundException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }
        Map<String, Object> body = new HashMap<>();
        body.put("errors", fieldErrors);
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // ── Auth exceptions (required by existing tests) ─────────────────────────

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        String path = request.getDescription(false);
        if (path.contains("/login")) {
            String msg = ex.getMessage();
            if (msg != null && msg.toLowerCase().contains("invalid credentials")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(LoginResponse.error(msg));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(LoginResponse.error(msg));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(RegistrationResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(Exception ex, WebRequest request) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        String path = request.getDescription(false);
        if (path.contains("/login")) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(LoginResponse.error("Internal server error"));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(RegistrationResponse.error("Internal server error"));
    }
}
