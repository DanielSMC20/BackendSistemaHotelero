package Hotel.jwt.dto.invoice;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class InvoiceResponse {
    private Long id;
    private String number;
    private Long reservationId;
    private Double total;
    private LocalDateTime issuedAt;
}