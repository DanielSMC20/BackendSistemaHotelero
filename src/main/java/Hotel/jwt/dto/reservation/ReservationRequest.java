package Hotel.jwt.dto.reservation;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ReservationRequest {
    private Long clienteId;
    private Long habitacionId;
    private LocalDate checkIn;
    private LocalDate checkOut;
}
