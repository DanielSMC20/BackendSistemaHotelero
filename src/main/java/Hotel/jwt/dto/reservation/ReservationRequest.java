package Hotel.jwt.dto.reservation;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ReservationRequest {
    private Long clienteId;
    private Long habitacionId;
    private String phoneCountryCode; // ej. "+51"
    private String telefono;         // número local sin prefijo
    private String telefonoE164;     // opcional si ya llega armado
    private LocalDate checkIn;
    private LocalDate checkOut;
}
