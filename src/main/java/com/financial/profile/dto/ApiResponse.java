package com.financial.profile.dto;

/**
 * Standard JSON response envelope for all API responses.
 *
 * <p>Analogy: every response is handed back in the same style of envelope,
 * so callers always know where to look for the result ({@code data}),
 * whether it succeeded ({@code success}), and a human-readable note
 * ({@code message}).</p>
 *
 * @param <T> the type of the payload carried in {@code data}
 */
public record ApiResponse<T>(
        boolean success,
        String message,
        T data
) {
    /**
     * Create a successful response with data.
     */
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    /**
     * Create a successful response without data.
     */
    public static <T> ApiResponse<T> ok(String message) {
        return new ApiResponse<>(true, message, null);
    }

    /**
     * Create a failed response with an error message.
     */
    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
