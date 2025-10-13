package Hotel.jwt.exception;
import Hotel.jwt.dto.common.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> notFound(NotFoundException ex, HttpServletRequest req){
        var err = ErrorResponse.of(HttpStatus.NOT_FOUND.value(), "Recurso no encontrado", ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> business(BusinessException ex, HttpServletRequest req){
        var err = ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), "Error de negocio", ex.getMessage(), req.getRequestURI());
        return ResponseEntity.badRequest().body(err);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex, HttpServletRequest req){
        var msg = ex.getBindingResult().getAllErrors().stream()
                .findFirst().map(e -> e.getDefaultMessage()).orElse("Error de validación");
        var err = ErrorResponse.of(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Error de validación", msg, req.getRequestURI());
        return ResponseEntity.unprocessableEntity().body(err);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> generic(Exception ex, HttpServletRequest req){
        var err = ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error interno", ex.getMessage(), req.getRequestURI());
        return ResponseEntity.internalServerError().body(err);
    }
}