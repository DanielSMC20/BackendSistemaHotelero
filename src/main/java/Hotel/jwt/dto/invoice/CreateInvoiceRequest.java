package Hotel.jwt.dto.invoice;

import lombok.Data;

@Data
public class CreateInvoiceRequest {
    private Long reservationId;     // reserva a facturar
    private String numberPrefix;    // opcional, ej: "INV" (default INV)
}