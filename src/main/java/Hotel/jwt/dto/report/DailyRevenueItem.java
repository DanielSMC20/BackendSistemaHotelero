package Hotel.jwt.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class DailyRevenueItem {
    private LocalDate day;
    private Double total;
}