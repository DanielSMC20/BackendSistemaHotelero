package Hotel.jwt.dto.payment;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {
    private Long id;
    private Long reservationId;
    private Double amount;
    private String method;
    private LocalDateTime paidAt;
    private String status;
    private String reference;
    private String registeredBy;

    // Sub-DTO para reportes (ingresos por día)
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DayTotal {
        private LocalDate day;
        private BigDecimal total;
    }
}
