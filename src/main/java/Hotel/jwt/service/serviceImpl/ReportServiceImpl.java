package Hotel.jwt.service.serviceImpl;

import Hotel.jwt.dto.report.DailyRevenueItem;
import Hotel.jwt.dto.report.OccupancyItem;
import Hotel.jwt.repository.PaymentRepository;
import Hotel.jwt.repository.ReservationRepository;
import Hotel.jwt.repository.RoomRepository;
import Hotel.jwt.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final PaymentRepository paymentRepo;
    private final ReservationRepository reservationRepo;
    private final RoomRepository roomRepo;

    @Override
    public List<DailyRevenueItem> revenueByDay(LocalDate start, LocalDate end) {
        LocalDateTime s = start.atStartOfDay();
        LocalDateTime e = end.plusDays(1).atStartOfDay().minusSeconds(1);

        var rows = paymentRepo.sumByDay(s, e);
        List<DailyRevenueItem> out = new ArrayList<>();
        for (Object[] r : rows) {
            // r[0] = java.sql.Date o String (dependiendo del driver), r[1] = BigDecimal/Double
            LocalDate day = ((java.sql.Date) r[0]).toLocalDate();
            Double total = r[1] != null ? Double.valueOf(r[1].toString()) : 0.0;
            out.add(new DailyRevenueItem(day, total));
        }
        return out;
    }

    @Override
    public List<OccupancyItem> occupancyByDay(LocalDate start, LocalDate end) {
        long totalRooms = roomRepo.count();
        List<OccupancyItem> out = new ArrayList<>();

        LocalDate d = start;
        while (!d.isAfter(end)) {
            long occupied = reservationRepo
                    .findByCheckInLessThanEqualAndCheckOutGreaterThanEqual(d, d)
                    .size();

            long available = totalRooms - occupied;
            double rate = totalRooms == 0 ? 0.0 : (double) occupied / (double) totalRooms;

            out.add(new OccupancyItem(d, occupied, available, rate));
            d = d.plusDays(1);
        }
        return out;
    }
}