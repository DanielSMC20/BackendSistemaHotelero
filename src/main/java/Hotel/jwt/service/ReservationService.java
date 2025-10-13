package Hotel.jwt.service;

import Hotel.jwt.dto.reservation.ReservationRequest;
import Hotel.jwt.dto.reservation.ReservationWithCustomerRequest;
import Hotel.jwt.entity.Reserva;

import java.util.List;

public interface ReservationService {
    Reserva create(ReservationRequest req);
    List<Reserva> byCustomer(Long customerId);
    Reserva createWithCustomer(ReservationWithCustomerRequest req);

}