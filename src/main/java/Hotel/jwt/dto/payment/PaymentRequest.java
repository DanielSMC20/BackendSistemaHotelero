package Hotel.jwt.dto.payment;

import lombok.Data;

@Data
public class PaymentRequest {
    private Long reservationId;
    private Double amount;
    private String method;
    private String reference;     // Ej: número de operación o voucher
    private String registeredBy;
}