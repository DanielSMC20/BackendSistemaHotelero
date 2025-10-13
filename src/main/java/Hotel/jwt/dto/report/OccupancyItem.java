package Hotel.jwt.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class OccupancyItem {
    private LocalDate day;
    private Long occupied;   // habitaciones ocupadas ese día
    private Long available;  // habitaciones disponibles totales
    private Double rate;     // ocupación (0..1)
}