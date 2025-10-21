package Hotel.jwt.service.serviceImpl;

import Hotel.jwt.dto.report.DailyRevenueItem;
import Hotel.jwt.dto.report.OccupancyItem;
import Hotel.jwt.entity.Reserva;
import Hotel.jwt.repository.PaymentRepository;
import Hotel.jwt.repository.ReservationRepository;
import Hotel.jwt.repository.RoomRepository;
import Hotel.jwt.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final PaymentRepository paymentRepo;
    private final ReservationRepository reservationRepo;
    private final RoomRepository roomRepo;

    @Override
    public List<DailyRevenueItem> revenueByDay(LocalDate start, LocalDate end) {
        // OJO: el orden correcto (end, start)
        var reservas = reservationRepo
                .findByFechaCheckOutBetween(end, start);

        return reservas.stream()
                .map(r -> new DailyRevenueItem(
                        r.getHabitacion() != null ? r.getHabitacion().getNumero() : "-",
                        r.getCliente() != null ? r.getCliente().getNombresCompletos() : "-",
                        r.getCliente() != null ? r.getCliente().getDocumento() : "-",
                        r.getFechaCheckIn(),
                        r.getFechaCheckOut(),
                        r.getPrecioTotal()
                ))
                .collect(Collectors.toList());
    }


    @Override
    public List<OccupancyItem> occupancyByDay(LocalDate start, LocalDate end) {
        long totalRooms = roomRepo.count();
        List<OccupancyItem> out = new ArrayList<>();

        LocalDate d = start;
        while (!d.isAfter(end)) {
            long occupied = reservationRepo
                    .findByFechaCheckOutBetween(d, d)
                    .size();

            long available = totalRooms - occupied;
            double rate = totalRooms == 0 ? 0.0 : (double) occupied / (double) totalRooms;

            out.add(new OccupancyItem(d, occupied, available, rate));
            d = d.plusDays(1);
        }
        return out;
    }
    @Override
    public List<DailyRevenueItem> revenueByMonth(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end   = ym.atEndOfMonth();

        var reservas = reservationRepo.findByFechaCheckOutBetween(start, end);

        return reservas.stream()
                .map(r -> new DailyRevenueItem(
                        r.getHabitacion()!=null ? r.getHabitacion().getNumero() : "-",
                        r.getCliente()!=null ? r.getCliente().getNombresCompletos() : "-",
                        r.getCliente()!=null ? r.getCliente().getDocumento() : "-",
                        r.getFechaCheckIn(),
                        r.getFechaCheckOut(),
                        r.getPrecioTotal()
                ))
                .toList();
    }
}