package Hotel.jwt.dto.payment;

import lombok.Data;

@Data
public class PaymentRequest {
    private Long reservationId;
    private Double amount;
    private String method;     // EFECTIVO/TARJETA/TRANSFERENCIA/YAPE/PLIN
    private String reference;  // opcional
}