package Hotel.jwt.service.serviceImpl;

import Hotel.jwt.dto.reservation.ReservationRequest;
import Hotel.jwt.dto.reservation.ReservationWithCustomerRequest;
import Hotel.jwt.entity.Clientes;
import Hotel.jwt.entity.Factura;
import Hotel.jwt.entity.Reserva;
import Hotel.jwt.repository.CustomerRepository;
import Hotel.jwt.repository.InvoiceRepository;
import Hotel.jwt.repository.ReservationRepository;
import Hotel.jwt.repository.RoomRepository;
import Hotel.jwt.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service @RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {
    private final ReservationRepository reservationRepository;
    private final CustomerRepository customerRepository;
    private final RoomRepository roomRepository;
    private final InvoiceRepository facturaRepository;

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
        if (req.getCheckIn() == null || req.getCheckOut() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fechas obligatorias");
        }
        long noches = ChronoUnit.DAYS.between(req.getCheckIn(), req.getCheckOut());
        if (noches <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El check-out debe ser posterior al check-in");
        }

        var existing = customerRepository.findByDocumento(req.getDocumento());
        Clientes cliente = existing.orElse(null);

        if (cliente == null) {
            if (req.getEmail() != null && !req.getEmail().isBlank()
                    && customerRepository.existsByEmail(req.getEmail())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Ya existe un cliente con email " + req.getEmail());
            }

            try {
                cliente = customerRepository.save(
                        Clientes.builder()
                                .documento(req.getDocumento())
                                .nombresCompletos(req.getNombresCompletos())
                                .email(req.getEmail())
                                .telefono(req.getTelefono())
                                .build()
                );
            } catch (DataIntegrityViolationException dup) {
                cliente = customerRepository.findByDocumento(req.getDocumento())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.CONFLICT, "Documento ya registrado"));
            }
        }

        var room = roomRepository.findById(req.getRoomId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Habitación no encontrada"));

        boolean overlap = reservationRepository
                .existsByHabitacion_IdAndCheckOutGreaterThanEqualAndCheckInLessThanEqual(
                        req.getRoomId(), req.getCheckIn(), req.getCheckOut());
        if (overlap) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La habitación ya está reservada en ese rango de fechas");
        }

        double precioTotalDouble = noches * room.getPrecioPorNoche();
        BigDecimal precioTotal = BigDecimal.valueOf(precioTotalDouble).setScale(2, java.math.RoundingMode.HALF_UP);

        var reserva = Reserva.builder()
                .cliente(cliente)
                .habitacion(room)
                .checkIn(req.getCheckIn())
                .checkOut(req.getCheckOut())
                .estado("RESERVADO")
                .precioTotal(precioTotal)
                .build();

        reserva = reservationRepository.save(reserva);

        var factura = Factura.builder()
                .reserva(reserva)
                .total(precioTotal)
                .estado("PENDIENTE")
                .build();
        facturaRepository.save(factura);

        room.setDisponible(false);
        roomRepository.save(room);

        return reserva;
    }
    @Override
    public List<Reserva> listAll() {
        return reservationRepository.findAll();
    }

    @Override
    public List<Reserva> byCustomer(Long clienteId) {
        return reservationRepository.findByCliente_Id(clienteId);
    }
}