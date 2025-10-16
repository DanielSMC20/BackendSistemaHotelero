package Hotel.jwt.dto.invoice;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class InvoiceResponse {
    private Long id;
    private String number;
    private Long reservationId;
    private BigDecimal total;
    private String status;
    private LocalDateTime issuedAt;
}