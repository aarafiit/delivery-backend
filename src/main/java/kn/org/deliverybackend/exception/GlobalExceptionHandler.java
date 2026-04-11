package kn.org.deliverybackend.exception;

import kn.org.deliverybackend.dto.response.auth.LoginResponse;
import kn.org.deliverybackend.dto.response.auth.RegistrationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

/**
 * Global exception handler for the authentication system.
 * Handles validation errors (400), conflicts (400), authentication failures (401),
 * and server errors (500) with consistent error response format.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles IllegalArgumentException for validation and conflict errors.
     * Returns 400 for validation errors and conflicts (username/email already exists).
     * Returns 401 for authentication failures (invalid credentials).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(
            IllegalArgumentException ex,
            WebRequest request) {

        String errorMessage = ex.getMessage();
        String path = request.getDescription(false).replace("uri=", "");

        // Determine if this is an authentication failure (401) or validation/conflict error (400)
        if (errorMessage.contains("Invalid credentials")) {
            // Authentication failure - return 401
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse(errorMessage, path));
        } else {
            // Validation or conflict error - return 400
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(errorMessage, path));
        }
    }

    /**
     * Handles all other unexpected exceptions as server errors.
     * Returns 500 with generic error message to avoid exposing internal details.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(
            Exception ex,
            WebRequest request) {

        String path = request.getDescription(false).replace("uri=", "");

        // Log the actual exception for debugging (in production, use proper logging)
        System.err.println("Internal server error: " + ex.getMessage());
        ex.printStackTrace();

        // Return generic error message to client
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Internal server error", path));
    }

    /**
     * Creates an appropriate error response based on the request path.
     * Returns LoginResponse for /login endpoint, RegistrationResponse for /register endpoint.
     */
    private Object createErrorResponse(String errorMessage, String path) {
        if (path.contains("/login")) {
            return LoginResponse.error(errorMessage);
        } else if (path.contains("/register")) {
            return RegistrationResponse.error(errorMessage);
        } else {
            // Generic error response for other endpoints
            return new ErrorResponse(false, errorMessage);
        }
    }

    /**
     * Generic error response DTO for endpoints without specific response types.
     */
    private static class ErrorResponse {
        private final boolean success;
        private final String error;

        public ErrorResponse(boolean success, String error) {
            this.success = success;
            this.error = error;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getError() {
            return error;
        }
    }
}
