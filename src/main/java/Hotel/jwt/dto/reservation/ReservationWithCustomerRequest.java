package Hotel.jwt.dto.reservation;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ReservationWithCustomerRequest {

    private String documento;
    private String nombresCompletos;
    private String email;
    private String telefono;

    private Long roomId;
    private LocalDate checkIn;
    private LocalDate checkOut;
}