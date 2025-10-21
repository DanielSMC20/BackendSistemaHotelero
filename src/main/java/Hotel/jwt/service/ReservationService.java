package Hotel.jwt.service;

import Hotel.jwt.dto.reservation.ReservationWithCustomerRequest;
import Hotel.jwt.entity.Reserva;
import Hotel.jwt.dto.reservation.ReservationRequest;
import java.time.LocalDate;
import java.util.List;

public interface ReservationService {
    Reserva create(ReservationRequest req);
    List<Reserva> listAll();
    List<Reserva> findByRangoFechas(LocalDate inicio, LocalDate fin);
    Reserva createWithCustomer(ReservationWithCustomerRequest request);

    Reserva checkIn(Long id);
    Reserva checkOut(Long id);
    Reserva extendStay(Long id, int horasExtra);
    List<Reserva> byCustomer(Long clienteId);


}
