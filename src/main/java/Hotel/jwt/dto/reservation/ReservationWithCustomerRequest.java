package Hotel.jwt.dto.reservation;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ReservationWithCustomerRequest {
    // Cliente
    private String documento;           // CLAVE para buscar/reutilizar
    private String nombresCompletos;
    private String email;
    private String telefono;

    // Reserva
    private Long roomId;
    private LocalDate checkIn;
    private LocalDate checkOut;
}