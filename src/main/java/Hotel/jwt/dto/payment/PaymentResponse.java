package Hotel.jwt.dto.payment;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {
    private Long id;
    private Long reservationId;
    private Double amount;
    private String method;
    private LocalDateTime paidAt;
}