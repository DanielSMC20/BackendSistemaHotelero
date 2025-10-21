// Hotel/jwt/dto/reservation/ReservationWithCustomerRequest.java
package Hotel.jwt.dto.reservation;

import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReservationWithCustomerRequest {

    // Cliente
    private String documento;          // DNI (8) o RUC (11)
    private String tipoDocumento;      // "DNI" | "RUC" | otros futuros
    private String nombresCompletos;
    private String email;
    private String telefono;

    // Reserva
    private Long roomId;               // id de la habitación
    private LocalDate checkIn;         // yyyy-MM-dd
    private LocalDate checkOut;        // yyyy-MM-dd
    private String estado;             // opcional; por defecto "RESERVADO"
}
