package Hotel.jwt.service.serviceImpl;

import Hotel.jwt.dto.reservation.ReservationRequest;
import Hotel.jwt.dto.reservation.ReservationWithCustomerRequest;
import Hotel.jwt.entity.Clientes;
import Hotel.jwt.entity.Habitacion;
import Hotel.jwt.entity.Reserva;
import Hotel.jwt.entity.Usuario;
import Hotel.jwt.exception.NotFoundException;
import Hotel.jwt.repository.CustomerRepository;
import Hotel.jwt.repository.ReservationRepository;
import Hotel.jwt.repository.RoomRepository;
import Hotel.jwt.repository.UsuarioRepository;
import Hotel.jwt.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepo;
    private final RoomRepository roomRepo;
    private final CustomerRepository customerRepo;
    private final UsuarioRepository usuarioRepo;


    @Override
    @Transactional
    public Reserva create(ReservationRequest req) {
        Habitacion hab = roomRepo.findById(req.getHabitacionId())
                .orElseThrow(() -> new IllegalArgumentException("Habitación no encontrada"));

        if (!hab.getEstado().equalsIgnoreCase("DISPONIBLE")) {
            throw new IllegalArgumentException("La habitación no está disponible");
        }

        // Calcular noches
        long noches = ChronoUnit.DAYS.between(req.getCheckIn(), req.getCheckOut());
        if (noches <= 0) throw new IllegalArgumentException("Fechas inválidas de reserva");

        BigDecimal total = BigDecimal.valueOf(hab.getPrecioPorNoche() * noches)
                .setScale(2, RoundingMode.HALF_UP);

        Usuario actual = getCurrentUserOrNull();


        Reserva reserva = Reserva.builder()
                .cliente(Clientes.builder().id(req.getClienteId()).build())
                .habitacion(hab)
                .fechaCheckIn(req.getCheckIn())
                .fechaCheckOut(req.getCheckOut())
                .precioTotal(total)
                .estado("RESERVADO")
                .usuario(actual)
                .build();

        hab.setEstado("OCUPADA");
        roomRepo.save(hab);

        return reservationRepo.save(reserva);
    }

    public Reserva create(Reserva r) {
        Habitacion hab = roomRepo.findById(r.getHabitacion().getId())
                .orElseThrow(() -> new IllegalArgumentException("Habitación no encontrada"));

        if (!hab.getEstado().equalsIgnoreCase("DISPONIBLE")) {
            throw new IllegalArgumentException("La habitación no está disponible");
        }

        // Calcular noches
        long noches = ChronoUnit.DAYS.between(r.getFechaCheckIn(), r.getFechaCheckOut());
        if (noches <= 0) throw new IllegalArgumentException("Fechas inválidas de reserva");

        BigDecimal total = BigDecimal.valueOf(hab.getPrecioPorNoche() * noches);
        r.setPrecioTotal(total.setScale(2, BigDecimal.ROUND_HALF_UP));
        r.setEstado("RESERVADO");

        // Marcar habitación ocupada
        hab.setEstado("OCUPADA");
        roomRepo.save(hab);

        return reservationRepo.save(r);
    }

    @Override
    public List<Reserva> listAll() {
        return reservationRepo.findAll();
    }

    @Override
    public List<Reserva> findByRangoFechas(java.time.LocalDate inicio, java.time.LocalDate fin) {
        return reservationRepo.findByRangoFechas(inicio, fin);
    }

    @Override
    @Transactional
    public Reserva checkIn(Long id) {
        Reserva reserva = reservationRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        if ("CHECKED_IN".equalsIgnoreCase(reserva.getEstado())) {
            throw new IllegalStateException("La reserva ya fue registrada como CHECK-IN");
        }

        if (!"RESERVADO".equalsIgnoreCase(reserva.getEstado())) {
            throw new IllegalStateException("Solo se puede hacer CHECK-IN de una reserva BOOKED");
        }

        reserva.setEstado("CHECKED_IN");
        reservationRepo.save(reserva);

        // Marcar habitación como ocupada
        Habitacion hab = reserva.getHabitacion();
        hab.setEstado("OCUPADA");
        roomRepo.save(hab);

        return reserva;
    }

    @Override
    @Transactional
    public Reserva checkOut(Long id) {
        Reserva reserva = reservationRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        if (!"CHECKED_IN".equalsIgnoreCase(reserva.getEstado())) {
            throw new IllegalStateException("Solo se puede hacer CHECK-OUT de una reserva CHECKED_IN");
        }

        reserva.setEstado("CHECKED_OUT");
        reservationRepo.save(reserva);

        // Liberar habitación (pasa a mantenimiento)
        Habitacion hab = reserva.getHabitacion();
        hab.setEstado("MANTENIMIENTO");
        roomRepo.save(hab);

        return reserva;
    }

    @Override
    @Transactional
    public Reserva extendStay(Long id, int horasExtra) {
        Reserva reserva = reservationRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        if (!"CHECKED_IN".equalsIgnoreCase(reserva.getEstado())) {
            throw new IllegalStateException("Solo se puede extender una reserva con CHECK-IN activo");
        }

        if (horasExtra <= 0) {
            throw new IllegalArgumentException("Las horas extra deben ser mayores a cero");
        }

        Habitacion hab = reserva.getHabitacion();
        double precioExtra = hab.getPrecioPorHora() * horasExtra;
        reserva.setPrecioTotal(
                reserva.getPrecioTotal().add(BigDecimal.valueOf(precioExtra)).setScale(2, RoundingMode.HALF_UP)
        );

        reservationRepo.save(reserva);
        return reserva;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reserva> byCustomer(Long clienteId) {
        return reservationRepo.findAll().stream()
                .filter(r -> r.getCliente() != null && r.getCliente().getId().equals(clienteId))
                .toList();
    }

    @Override
    @Transactional
    public Reserva createWithCustomer(ReservationWithCustomerRequest req) {
        // 1) Validaciones básicas
        if (req == null) {
            throw new IllegalArgumentException("El request no puede ser nulo.");
        }
        if (req.getDocumento() == null || req.getDocumento().trim().isEmpty()) {
            throw new IllegalArgumentException("El documento del cliente es obligatorio.");
        }
        if (req.getRoomId() == null) {
            throw new IllegalArgumentException("roomId es obligatorio.");
        }
        if (req.getCheckIn() == null || req.getCheckOut() == null) {
            throw new IllegalArgumentException("checkIn y checkOut son obligatorios.");
        }
        if (!req.getCheckOut().isAfter(req.getCheckIn())) {
            throw new IllegalArgumentException("checkOut debe ser posterior a checkIn.");
        }

        final String documento = req.getDocumento().trim();
        final String tipoDocReq = req.getTipoDocumento() == null ? "" : req.getTipoDocumento().trim().toUpperCase();
        final boolean esJuridica = "RUC".equals(tipoDocReq) || documento.length() == 11;

        // 2) Cliente: crear o actualizar por documento
        Clientes cliente = customerRepo.findByDocumento(documento)
                .orElseGet(() -> Clientes.builder().documento(documento).build());

        // Tipo de persona / tipo de documento
        cliente.setTipoPersona(esJuridica ? "JURIDICA" : "NATURAL");
        cliente.setTipoDocumento(
                tipoDocReq.isEmpty()
                        ? (esJuridica ? "RUC" : "DNI")
                        : tipoDocReq
        );

        // Nombres o razón social según tipo de persona
        if (esJuridica) {
            // No tienes campo separado en el request para razón social, reutilizamos nombresCompletos si viene
            if (req.getNombresCompletos() != null && !req.getNombresCompletos().isBlank()) {
                cliente.setRazonSocial(req.getNombresCompletos().trim());
            }
            // opcional: limpiar nombresCompletos si quieres evitar ambigüedad
            // cliente.setNombresCompletos(null);
        } else {
            if (req.getNombresCompletos() != null && !req.getNombresCompletos().isBlank()) {
                cliente.setNombresCompletos(req.getNombresCompletos().trim());
            }
        }

        if (req.getEmail() != null && !req.getEmail().isBlank()) {
            cliente.setEmail(req.getEmail().trim());
        }
        if (req.getTelefono() != null && !req.getTelefono().isBlank()) {
            cliente.setTelefono(req.getTelefono().trim());
        }
        if (cliente.getEstado() == null || cliente.getEstado().isBlank()) {
            cliente.setEstado("ACTIVO");
        }
        // @PrePersist en Clientes debería setear fechaRegistro si es null.
        cliente = customerRepo.save(cliente);

        // 3) Habitación
        Habitacion hab = roomRepo.findById(req.getRoomId())
                .orElseThrow(() -> new NotFoundException("Habitación no encontrada: " + req.getRoomId()));

        String estadoHab = (hab.getEstado() == null) ? "DISPONIBLE" : hab.getEstado().trim().toUpperCase();
        if (!"DISPONIBLE".equals(estadoHab)) {
            throw new IllegalStateException("La habitación no está disponible (estado actual: " + estadoHab + ")");
        }

        // 4) Calcular precio total (noches x precioPorNoche)
        long nights = java.time.temporal.ChronoUnit.DAYS.between(req.getCheckIn(), req.getCheckOut());
        if (nights <= 0) nights = 1; // por seguridad, mínima 1 noche
        double precioNoche = hab.getPrecioPorNoche() == null ? 0.0 : hab.getPrecioPorNoche();
        java.math.BigDecimal total = java.math.BigDecimal.valueOf(precioNoche)
                .multiply(java.math.BigDecimal.valueOf(nights))
                .setScale(2, java.math.RoundingMode.HALF_UP);

        // 5) Usuario autenticado (puede ser null si la llamada es sin auth)
        Usuario actual = getCurrentUserOrNull();

        // 6) Crear reserva
        String estadoReserva = (req.getEstado() == null || req.getEstado().isBlank())
                ? "RESERVADO"
                : req.getEstado().trim().toUpperCase();

        Reserva r = Reserva.builder()
                .cliente(cliente)
                .habitacion(hab)
                .fechaCheckIn(req.getCheckIn())
                .fechaCheckOut(req.getCheckOut())
                .estado(estadoReserva)
                .precioTotal(total)
                .build();

        // Relacionar usuario que registra la reserva (si agregaste el campo en Reserva)
        // r.setUsuario(actual);

        r = reservationRepo.save(r);

        // 7) Marcar habitación según política. Si reservas ocupan inmediatamente:
        hab.setEstado("OCUPADA");
        roomRepo.save(hab);
        // Si prefieres ocupar solo en check-in, comenta las 2 líneas de arriba.

        return r;
    }

    private Usuario getCurrentUserOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) return null;
        return usuarioRepo.findByUsuario(auth.getName()).orElse(null);
    }



}
