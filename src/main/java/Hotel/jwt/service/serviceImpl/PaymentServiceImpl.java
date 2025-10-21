package Hotel.jwt.service.serviceImpl;

import Hotel.jwt.dto.payment.PaymentRequest;
import Hotel.jwt.dto.payment.PaymentResponse;
import Hotel.jwt.entity.Pago;
import Hotel.jwt.entity.Reserva;
import Hotel.jwt.entity.Usuario;
import Hotel.jwt.exception.NotFoundException;
import Hotel.jwt.repository.PaymentRepository;
import Hotel.jwt.repository.ReservationRepository;
import Hotel.jwt.repository.UsuarioRepository;
import Hotel.jwt.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final Set<String> METODOS =
            Set.of("EFECTIVO", "TARJETA", "TRANSFERENCIA", "YAPE", "PLIN");

    private final PaymentRepository paymentRepo;
    private final ReservationRepository reservationRepo;
    private final UsuarioRepository usuarioRepo;

    // =================== API ===================

    @Override
    @Transactional
    public PaymentResponse record(PaymentRequest req) {
        // 1) Validar reserva
        Reserva res = reservationRepo.findById(req.getReservationId())
                .orElseThrow(() -> new NotFoundException("Reserva no encontrada: " + req.getReservationId()));

        // 2) Usuario autenticado
        String username = getAuthenticatedUsername();
        Usuario empleado = usuarioRepo.findByUsuario(username)
                .orElseThrow(() -> new NotFoundException("Usuario del sistema no encontrado: " + username));
        String nombreCompletoEmpleado = buildNombreCompleto(empleado);

        // 3) Validaciones de negocio
        double monto = normalizeAmount(req.getAmount());
        validarMetodo(req.getMethod());
        if (req.getReference() != null && !req.getReference().isBlank()
                && paymentRepo.existsByReferencia(req.getReference())) {
            throw new IllegalArgumentException("Ya existe un pago con referencia " + req.getReference());
        }

        // 4) Crear pago
        Pago p = Pago.builder()
                .reserva(res)
                .monto(monto)
                .metodo(req.getMethod().toUpperCase())
                .estado("COMPLETADO")
                .referencia(req.getReference())
                .registradoPor(nombreCompletoEmpleado)
                .build();

        if (p.getPagadoEn() == null) {
            p.setPagadoEn(LocalDateTime.now());
        }

        p = paymentRepo.save(p);

        // 5) (Sin tocar entidad Reserva) – calcular estado de pago "al vuelo"
        //    Suma pagos completados de la reserva y compara contra precioTotal
        BigDecimal pagado = getTotalPagado(res.getId());             // suma pagos COMPLETADO
        BigDecimal total  = safeTotal(res.getPrecioTotal());         // precioTotal (BigDecimal)
        // Si lo necesitas en logs o respuesta, puedes derivar:
        // String estadoPago = derivePaymentState(pagado, total);

        // 6) Respuesta
        return PaymentResponse.builder()
                .id(p.getId())
                .reservationId(res.getId())
                .amount(p.getMonto())
                .method(p.getMetodo())
                .paidAt(p.getPagadoEn())
                .status(p.getEstado())
                .reference(p.getReferencia())
                .registeredBy(p.getRegistradoPor())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> byReservation(Long reservaId) {
        return paymentRepo.findByReserva_Id(reservaId).stream()
                .map(p -> PaymentResponse.builder()
                        .id(p.getId())
                        .reservationId(p.getReserva().getId())
                        .amount(p.getMonto())
                        .method(p.getMetodo())
                        .paidAt(p.getPagadoEn())
                        .status(p.getEstado())
                        .reference(p.getReferencia())
                        .registeredBy(p.getRegistradoPor())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse.DayTotal> sumByDay(LocalDateTime inicio, LocalDateTime fin) {
        return paymentRepo.sumByDay(inicio, fin).stream().map(row -> {
            // row[0] = java.sql.Date o String (depende del driver), row[1] = BigDecimal/Number
            LocalDate dia = (row[0] instanceof java.sql.Date d)
                    ? d.toLocalDate()
                    : LocalDate.parse(row[0].toString());
            BigDecimal total = (row[1] instanceof BigDecimal bd)
                    ? bd
                    : new BigDecimal(row[1].toString());
            return new PaymentResponse.DayTotal(dia, total.setScale(2, RoundingMode.HALF_UP));
        }).toList();
    }

    // =================== Helpers ===================

    private String getAuthenticatedUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            throw new NotFoundException("No hay un usuario autenticado en el contexto.");
        }
        return auth.getName();
    }

    private String buildNombreCompleto(Usuario u) {
        String nombres = u.getNombres() == null ? "" : u.getNombres().trim();
        String apellidos = u.getApellidos() == null ? "" : u.getApellidos().trim();
        String full = (nombres + " " + apellidos).trim();
        return full.isEmpty() ? u.getUsuario() : full;
    }

    private double normalizeAmount(Double amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("El monto debe ser > 0");
        }
        return BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private void validarMetodo(String method) {
        if (method == null || !METODOS.contains(method.toUpperCase())) {
            throw new IllegalArgumentException("Método de pago inválido: " + method);
        }
    }

    private BigDecimal getTotalPagado(Long reservaId) {
        Double sum = paymentRepo.sumCompletadosByReserva(reservaId);
        return BigDecimal.valueOf(sum == null ? 0d : sum)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal safeTotal(BigDecimal total) {
        return (total == null ? BigDecimal.ZERO : total).setScale(2, RoundingMode.HALF_UP);
    }

    @SuppressWarnings("unused")
    private String derivePaymentState(BigDecimal pagado, BigDecimal total) {
        if (pagado.compareTo(total) >= 0) return "COMPLETO";
        if (pagado.compareTo(BigDecimal.ZERO) > 0) return "PARCIAL";
        return "PENDIENTE";
    }

    @Override
    public Pago registrarPago(Pago pago) {
        pago.setEstado("COMPLETADO");
        pago.setPagadoEn(LocalDateTime.now());
        return paymentRepo.save(pago);
    }

    @Override
    public Pago obtenerPago(Long id) {
        return paymentRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pago no encontrado"));
    }

    @Override
    public List<Pago> listarPagos() {
        return paymentRepo.findAll();
    }

    @Override
    @Transactional
    public Pago marcarPagado(Long id) {
        Pago pago = obtenerPago(id);
        if ("REEMBOLSADO".equalsIgnoreCase(pago.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Pago reembolsado, no puede marcarse como completado");
        }
        if ("COMPLETADO".equalsIgnoreCase(pago.getEstado())) {
            return pago; // idempotente
        }

        pago.setEstado("COMPLETADO");
        pago.setPagadoEn(LocalDateTime.now());
        return paymentRepo.save(pago);
    }

    @Override
    @Transactional
    public Pago marcarReembolsado(Long id, String referencia) {
        Pago pago = obtenerPago(id);
        pago.marcarReembolsado(referencia);
        return paymentRepo.save(pago);
    }

    @Override
    @Transactional
    public Pago marcarFallido(Long id, String referencia) {
        Pago pago = obtenerPago(id);
        pago.marcarFallido(referencia);
        return paymentRepo.save(pago);
    }
}
