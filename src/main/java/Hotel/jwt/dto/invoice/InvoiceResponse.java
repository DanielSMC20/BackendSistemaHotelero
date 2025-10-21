// InvoiceResponse.java
package Hotel.jwt.dto.invoice;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class InvoiceResponse {
    private Long id;
    private Long reservationId;

    private String type;
    private String serie;
    private String number;

    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal total;

    private String status; // PENDIENTE | PAGADA | ANULADA
    private LocalDateTime issuedAt;
    private LocalDateTime paidAt;
}
