package Hotel.jwt.dto.reservation;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ReservationResponse {
    private Long id;
    private Long customerId;
    private String customerName;
    private Long roomId;
    private String roomNumber;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private String status;
}