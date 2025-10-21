package Hotel.jwt.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;          // true = OK
    private String message;           // "OK" o descripción de error
    private T data;                   // payload
    private LocalDateTime timestamp;  // marca de tiempo

    // ===== Helpers estáticos =====

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("OK")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /** Error / fallo con mensaje (sin data) */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /** Alias semántico por si quieres diferenciar "fail" de "error" */
    public static <T> ApiResponse<T> fail(String message) {
        return error(message);
    }
}
