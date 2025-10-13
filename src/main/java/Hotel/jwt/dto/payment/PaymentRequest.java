package Hotel.jwt.dto.payment;

import lombok.Data;

@Data
public class PaymentRequest {
    private Long reservationId;
    private Double amount;
    private String method; // CASH, CARD, TRANSFER
}