package Hotel.jwt.service.serviceImpl;

import Hotel.jwt.dto.reservation.ReservationRequest;
import Hotel.jwt.dto.reservation.ReservationWithCustomerRequest;
import Hotel.jwt.entity.Clientes;
import Hotel.jwt.entity.Reserva;
import Hotel.jwt.repository.CustomerRepository;
import Hotel.jwt.repository.ReservationRepository;
import Hotel.jwt.repository.RoomRepository;
import Hotel.jwt.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service @RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {
    private final ReservationRepository reservationRepository;
    private final CustomerRepository customerRepository;
    private final RoomRepository roomRepository;

    @Override
    public Reserva create(ReservationRequest req) {
        var customer = customerRepository.findById(req.getCustomerId()).orElseThrow();
        var room = roomRepository.findById(req.getRoomId()).orElseThrow();

        // Validar solape de fechas (recomendado)
        if (reservationRepository.existsByHabitacion_IdAndCheckOutGreaterThanEqualAndCheckInLessThanEqual(
                req.getRoomId(), req.getCheckIn(), req.getCheckOut())) {
            throw new IllegalStateException("La habitación ya está reservada en ese rango de fechas");
        }

        var r = Reserva.builder()
                .cliente(customer)
                .habitacion(room)
                .checkIn(req.getCheckIn())
                .checkOut(req.getCheckOut())
                .estado("RESERVADO")
                .build();

        // Si manejas 'disponible' como ocupación en curso, solo marcar false si el rango incluye hoy.
        // room.setDisponible(false); roomRepository.save(room);

        return reservationRepository.save(r);
    }

    @Transactional
    @Override
    public Reserva createWithCustomer(ReservationWithCustomerRequest req) {
        // 1) Buscar o crear cliente por documento
        var cliente = customerRepository.findByDocumento(req.getDocumento())
                .orElseGet(() -> customerRepository.save(
                        Clientes.builder()
                                .documento(req.getDocumento())
                                .nombresCompletos(req.getNombresCompletos())
                                .email(req.getEmail())
                                .telefono(req.getTelefono())
                                .build()
                ));

        // 2) Validar habitación
        var room = roomRepository.findById(req.getRoomId()).orElseThrow();

        // 3) Validar solape real de hotel
        boolean overlap = reservationRepository
                .existsByHabitacion_IdAndCheckOutGreaterThanEqualAndCheckInLessThanEqual(
                        req.getRoomId(), req.getCheckIn(), req.getCheckOut());
        if (overlap) throw new IllegalStateException("La habitación ya está reservada en ese rango de fechas");

        // 4) Crear reserva
        var r = Reserva.builder()
                .cliente(cliente)
                .habitacion(room)
                .checkIn(req.getCheckIn())
                .checkOut(req.getCheckOut())
                .estado("RESERVADO")
                .build();

        return reservationRepository.save(r);
    }

    @Override
    public List<Reserva> byCustomer(Long clienteId) {
        return reservationRepository.findByCliente_Id(clienteId);
    }
}