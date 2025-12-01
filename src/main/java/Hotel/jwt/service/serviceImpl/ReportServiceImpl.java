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

        var reservas = reservationRepo.findByRangoFechas(start, end);

        return reservas.stream()
                .map(r -> {
                    var paymentOpt = paymentRepo.findTopByReservaOrderByPagadoEnDesc(r);
                    Long paymentId = paymentOpt.map(p -> p.getId()).orElse(null);

                    String atendidoPor = "-";
                    if (r.getUsuario() != null) {
                        atendidoPor = r.getUsuario().getNombres() + " " + r.getUsuario().getApellidos();
                    }

                    return new DailyRevenueItem(
                            r.getHabitacion() != null ? r.getHabitacion().getNumero() : "-",
                            r.getCliente() != null ? r.getCliente().getNombresCompletos() : "-",
                            r.getCliente() != null ? r.getCliente().getDocumento() : "-",
                            r.getFechaCheckIn(),
                            r.getFechaCheckOut(),
                            r.getPrecioTotal(),
                            atendidoPor,
                            paymentId
                    );
                })
                .collect(Collectors.toList());
    }


    @Override
    public List<OccupancyItem> occupancyByDay(LocalDate start, LocalDate end) {
        // Normalizar fechas igual que en revenueByDay
        if (start == null && end == null) {
            LocalDate hoy = LocalDate.now();
            start = hoy;
            end   = hoy;
        } else if (start == null) {
            start = end;
        } else if (end == null) {
            end = start;
        }

        if (start.isAfter(end)) {
            LocalDate tmp = start;
            start = end;
            end   = tmp;
        }

        long totalRooms = roomRepo.count();
        List<OccupancyItem> out = new ArrayList<>();

        LocalDate d = start;
        while (!d.isAfter(end)) {
            long occupied = reservationRepo
                    .findByFechaCheckOutBetween(d, d)  // 👈 mismo día
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


        var reservas = reservationRepo.findByRangoFechas(start, end);

        return reservas.stream()
                .map(r -> {
                    var paymentOpt = paymentRepo.findTopByReservaOrderByPagadoEnDesc(r);
                    Long paymentId = paymentOpt.map(p -> p.getId()).orElse(null);

                    return new DailyRevenueItem(
                            r.getHabitacion()!=null ? r.getHabitacion().getNumero() : "-",
                            r.getCliente()!=null ? r.getCliente().getNombresCompletos() : "-",
                            r.getCliente()!=null ? r.getCliente().getDocumento() : "-",
                            r.getFechaCheckIn(),
                            r.getFechaCheckOut(),
                            r.getPrecioTotal(),
                            r.getUsuario() != null ? r.getUsuario().getUsuario() : "-",
                            paymentId
                    );
                })
                .toList();
    }

}