package Hotel.jwt.dto.reservation;
import lombok.Data;

import java.time.LocalDate;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ReservationRequest {
    private Long customerId;
    private Long roomId;
    private LocalDate checkIn;
    private LocalDate checkOut;
}