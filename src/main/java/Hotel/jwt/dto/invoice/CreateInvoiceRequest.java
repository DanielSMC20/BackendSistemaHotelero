// CreateInvoiceRequest.java
package Hotel.jwt.dto.invoice;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateInvoiceRequest {
    private Long reservationId;

    // "BOLETA" | "FACTURA"
    private String type;

    // Ej. "B001" o "F001" (opcional; si no lo mandas, usaré "B001" por BOLETA y "F001" por FACTURA)
    private String serie;

    // Si mandas un monto manual (p.ej. incluye consumos extra), lo uso; si no, uso precio de la Reserva
    private BigDecimal overrideTotal;

    // Por defecto 0.18 (IGV Perú). Si lo mandas nulo, uso 0.18.
    private BigDecimal taxRate;

    // Opcional: marcar pagada al emitir
    private boolean markPaid;
}
