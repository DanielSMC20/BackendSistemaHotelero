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

        if (!"DISPONIBLE".equalsIgnoreCase(nullTo(hab.getEstado(), "DISPONIBLE"))) {
            throw new IllegalArgumentException("La habitación no está disponible");
        }

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

        // ⚠️ Importante: NO OCUPAR la habitación aquí. Se ocupa en check-in.
        // hab.setEstado("OCUPADA"); roomRepo.save(hab);

        return reservationRepo.save(reserva);
    }

    public Reserva create(Reserva r) {
        Habitacion hab = roomRepo.findById(r.getHabitacion().getId())
                .orElseThrow(() -> new IllegalArgumentException("Habitación no encontrada"));

        if (!"DISPONIBLE".equalsIgnoreCase(nullTo(hab.getEstado(), "DISPONIBLE"))) {
            throw new IllegalArgumentException("La habitación no está disponible");
        }

        long noches = ChronoUnit.DAYS.between(r.getFechaCheckIn(), r.getFechaCheckOut());
        if (noches <= 0) throw new IllegalArgumentException("Fechas inválidas de reserva");

        BigDecimal total = BigDecimal.valueOf(hab.getPrecioPorNoche() * noches);
        r.setPrecioTotal(total.setScale(2, RoundingMode.HALF_UP));
        r.setEstado("RESERVADO");

        // ⚠️ No ocupar aquí.
        // hab.setEstado("OCUPADA"); roomRepo.save(hab);

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
            throw new IllegalStateException("Solo se puede hacer CHECK-IN de una reserva RESERVADO");
        }

        reserva.setEstado("CHECKED_IN");
        reservationRepo.save(reserva);

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

        Habitacion hab = reserva.getHabitacion();
        hab.setEstado("MANTENIMIENTO"); // o "DISPONIBLE" según tu política
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
        if (horasExtra <= 0) throw new IllegalArgumentException("Las horas extra deben ser mayores a cero");

        Habitacion hab = reserva.getHabitacion();
        double precioExtra = hab.getPrecioPorHora() * horasExtra;
        reserva.setPrecioTotal(
                reserva.getPrecioTotal().add(BigDecimal.valueOf(precioExtra)).setScale(2, RoundingMode.HALF_UP)
        );

        return reservationRepo.save(reserva);
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
        if (req == null) throw new IllegalArgumentException("El request no puede ser nulo.");
        if (!StringUtils.hasText(req.getDocumento()))
            throw new IllegalArgumentException("El documento del cliente es obligatorio.");
        if (req.getRoomId() == null)
            throw new IllegalArgumentException("roomId es obligatorio.");

        // ¿reserva por horas?
        boolean porHoras = Boolean.TRUE.equals(req.getReservaPorHoras());

        // ===== Validación de fechas según el modo =====
        if (!porHoras) {
            // MODO NOCHES: validación original
            if (req.getCheckIn() == null || req.getCheckOut() == null)
                throw new IllegalArgumentException("checkIn y checkOut son obligatorios.");
            if (!req.getCheckOut().isAfter(req.getCheckIn()))
                throw new IllegalArgumentException("checkOut debe ser posterior a checkIn.");
        } else {
            // MODO HORAS: el cliente ya está en el local
            if (req.getCheckIn() == null) {
                throw new IllegalArgumentException("checkIn es obligatorio para reservas por horas.");
            }
            // checkOut lo podemos ignorar o usar solo como referencia; no aplicamos regla estricta
        }

        // ========= CLIENTE =========
        final String documento = req.getDocumento().trim();
        final String tipoDocReq = req.getTipoDocumento() == null
                ? ""
                : req.getTipoDocumento().trim().toUpperCase();
        final boolean esJuridica = "RUC".equals(tipoDocReq) || documento.length() == 11;

        Clientes cliente = customerRepo.findByDocumento(documento)
                .orElseGet(() -> Clientes.builder().documento(documento).build());

        cliente.setTipoPersona(esJuridica ? "JURIDICA" : "NATURAL");
        cliente.setTipoDocumento(
                tipoDocReq.isEmpty()
                        ? (esJuridica ? "RUC" : "DNI")
                        : tipoDocReq
        );

        if (esJuridica) {
            if (StringUtils.hasText(req.getNombresCompletos())) {
                cliente.setRazonSocial(req.getNombresCompletos().trim());
            }
        } else {
            if (StringUtils.hasText(req.getNombresCompletos())) {
                cliente.setNombresCompletos(req.getNombresCompletos().trim());
            }
        }

        if (StringUtils.hasText(req.getEmail())) {
            cliente.setEmail(req.getEmail().trim());
        }

        // ======= TELÉFONO: normalización con prefijo y E.164 =======
        normalizeAndSetPhone(
                cliente,
                req.getPhoneCountryCode(),   // puede ser null
                req.getTelefono(),           // número local
                req.getTelefonoE164()        // si ya viene armado
        );

        if (!StringUtils.hasText(cliente.getEstado())) {
            cliente.setEstado("ACTIVO");
        }
        cliente = customerRepo.save(cliente);

        // ========= HABITACIÓN =========
        Habitacion hab = roomRepo.findById(req.getRoomId())
                .orElseThrow(() -> new NotFoundException("Habitación no encontrada: " + req.getRoomId()));

        String estadoHab = nullTo(hab.getEstado(), "DISPONIBLE").trim().toUpperCase();
        if (!"DISPONIBLE".equals(estadoHab)) {
            throw new IllegalStateException("La habitación no está disponible (estado actual: " + estadoHab + ")");
        }

        // ========= PRECIO =========
        BigDecimal total;

        if (porHoras) {
            // ----- Reserva por HORAS (cliente ya llegó) -----
            Integer horas = req.getHoras();
            if (horas == null || horas <= 0) {
                throw new IllegalArgumentException("Las horas de reserva deben ser mayores a cero.");
            }

            double precioHora = hab.getPrecioPorHora() == null ? 0.0 : hab.getPrecioPorHora();
            total = BigDecimal.valueOf(precioHora)
                    .multiply(BigDecimal.valueOf(horas))
                    .setScale(2, RoundingMode.HALF_UP);

        } else {
            // ----- Reserva por NOCHES (lógica original) -----
            long nights = ChronoUnit.DAYS.between(req.getCheckIn(), req.getCheckOut());
            if (nights <= 0) nights = 1;
            double precioNoche = hab.getPrecioPorNoche() == null ? 0.0 : hab.getPrecioPorNoche();
            total = BigDecimal.valueOf(precioNoche)
                    .multiply(BigDecimal.valueOf(nights))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        // ========= USUARIO QUE REGISTRA =========
        Usuario actual = getCurrentUserOrNull();

        // Estado de reserva:
        // - Por noches: RESERVADO (espera check-in)
        // - Por horas: CHECKED_IN (ya llegó y se ocupa al toque)
        String estadoReserva;
        if (porHoras) {
            estadoReserva = "CHECKED_IN";
        } else {
            estadoReserva = !StringUtils.hasText(req.getEstado())
                    ? "RESERVADO"
                    : req.getEstado().trim().toUpperCase();
        }

        // ========= FECHAS QUE SE GUARDAN EN LA RESERVA =========
        // Sigues usando LocalDate en la entidad:
        // - Por noches: se respeta checkIn/checkOut del request.
        // - Por horas: usamos checkIn del request y, si quieres, mismo día en checkOut.
        java.time.LocalDate fechaCheckIn = req.getCheckIn();
        java.time.LocalDate fechaCheckOut;

        if (porHoras) {
            // Puedes dejarlo igual al checkIn (misma fecha)
            fechaCheckOut = req.getCheckIn();
        } else {
            fechaCheckOut = req.getCheckOut();
        }

        // ========= CONSTRUIR RESERVA =========
        Reserva r = Reserva.builder()
                .cliente(cliente)
                .habitacion(hab)
                .fechaCheckIn(fechaCheckIn)
                .fechaCheckOut(fechaCheckOut)
                .estado(estadoReserva)
                .precioTotal(total)
                .usuario(actual)
                .build();

        r = reservationRepo.save(r);

        // ========= OCUPAR HABITACIÓN SOLO EN MODO HORAS =========
        if (porHoras) {
            hab.setEstado("OCUPADA");
            roomRepo.save(hab);
        }
        // Para reservas por noches se mantiene la política:
        // "NO ocupar hasta check-in"

        return r;
    }


    private Usuario getCurrentUserOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) return null;
        return usuarioRepo.findByUsuario(auth.getName()).orElse(null);
    }

    // ====================== helpers ======================

    private static String nullTo(String v, String def) { return v == null ? def : v; }

    /**
     * Normaliza y setea teléfono en cliente usando phoneCountryCode + telefono o telefonoE164.
     * Regla: si viene E.164 lo valida y lo usa; si no, arma E.164 con code + local.
     */
    private void normalizeAndSetPhone(Clientes cliente, String phoneCountryCode, String telefonoLocal, String telefonoE164) {
        String e164 = safe(telefonoE164);
        String code = safe(phoneCountryCode);
        String local = digitsOnly(telefonoLocal);

        if (StringUtils.hasText(e164)) {
            e164 = normalizeE164(e164);
            // Si además viene code/local, guardamos ambos. Si no, derivamos local usando code si está.
            if (!StringUtils.hasText(code)) {
                // Heurística simple: si e164 inicia con +51, code="+51"
                // Puedes mejorar consultando una tabla de phone-codes.
                code = guessDialCode(e164);
            } else {
                code = normalizeCode(code);
            }
            if (!StringUtils.hasText(local) && StringUtils.hasText(code) && e164.startsWith(code)) {
                local = e164.substring(code.length());
            }
            cliente.setTelefonoCodigoPais(code);
            cliente.setTelefono(local);
            cliente.setTelefonoE164(e164);
            return;
        }

        // Si no vino E164, pero sí local (y quizás code)
        if (StringUtils.hasText(local)) {
            code = normalizeCode(StringUtils.hasText(code) ? code : "+51");
            e164 = code + local;
            e164 = normalizeE164(e164);
            cliente.setTelefonoCodigoPais(code);
            cliente.setTelefono(local);
            cliente.setTelefonoE164(e164);
        }
    }

    private static String safe(String v) { return v == null ? "" : v.trim(); }
    private static String digitsOnly(String v) { return safe(v).replaceAll("\\D+", ""); }
    private static String normalizeCode(String code) {
        String c = safe(code).replaceAll("[^+\\d]", "");
        return c.startsWith("+") ? c : "+" + c;
    }
    private static String normalizeE164(String val) {
        String out = safe(val).replaceAll("[^+\\d]", "");
        if (!out.startsWith("+")) out = "+" + out;
        // Validación simple E.164: + y 8..15 dígitos
        if (!out.matches("^\\+[1-9]\\d{7,14}$")) {
            throw new IllegalArgumentException("Formato de teléfono inválido (E.164).");
        }
        return out;
    }
    private static String guessDialCode(String e164) {
        // Heurística básica. Ideal: consultar tabla de prefijos y elegir el más largo que matchee.
        if (e164.startsWith("+51")) return "+51";
        if (e164.startsWith("+54")) return "+54";
        if (e164.startsWith("+55")) return "+55";
        if (e164.startsWith("+56")) return "+56";
        if (e164.startsWith("+57")) return "+57";
        if (e164.startsWith("+58")) return "+58";
        if (e164.startsWith("+593")) return "+593";
        if (e164.startsWith("+34")) return "+34";
        if (e164.startsWith("+1")) return "+1";
        // fallback genérico de 2 dígitos
        return "+" + e164.substring(1, Math.min(3, e164.length()));
    }
}
