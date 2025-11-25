package Hotel.jwt.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyRevenueItem {
    private String roomNumber;
    private String guestName;
    private String dni;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private BigDecimal price;
    private String usuarioRegistro;
    private Long paymentId;


}
