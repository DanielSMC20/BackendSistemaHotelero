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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepo;
    private final ReservationRepository reservationRepo;
    private final UsuarioRepository usuarioRepo; // ← Repositorio para tus usuarios del sistema

    @Override
    @Transactional
    public PaymentResponse record(PaymentRequest req) {
        // 1) Validar reserva
        Reserva res = reservationRepo.findById(req.getReservationId())
                .orElseThrow(() -> new NotFoundException("Reserva no encontrada: " + req.getReservationId()));

        // 2) Obtener usuario autenticado y sus nombres/apellidos
        String username = getAuthenticatedUsername();
        Usuario empleado = usuarioRepo.findByUsuario(username)
                .orElseThrow(() -> new NotFoundException("Usuario del sistema no encontrado: " + username));

        String nombreCompletoEmpleado = buildNombreCompleto(empleado);

        // 3) Construir Pago
        Pago p = Pago.builder()
                .reserva(res)
                .monto(req.getAmount())
                .metodo(req.getMethod())
                .estado("COMPLETADO")
                .referencia(req.getReference())
                .registradoPor(nombreCompletoEmpleado) // ← Nombres y apellidos del usuario del hotel
                .build();

        // (Opcional) si deseas setear manualmente el pagadoEn además del @PrePersist
        if (p.getPagadoEn() == null) {
            p.setPagadoEn(LocalDateTime.now());
        }

        // 4) Guardar y responder
        p = paymentRepo.save(p);

        return PaymentResponse.builder()
                .id(p.getId())
                .reservationId(res.getId())
                .amount(p.getMonto())
                .method(p.getMetodo())
                .paidAt(p.getPagadoEn())
                .status(p.getEstado())
                .reference(p.getReferencia())
                .registeredBy(p.getRegistradoPor()) // ← nombres del empleado
                .build();
    }

    @Override
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

    // ───────────────────────── helpers ─────────────────────────

    private String getAuthenticatedUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            throw new NotFoundException("No hay un usuario autenticado en el contexto.");
        }
        return auth.getName(); // ← debe coincidir con Usuario.usuario
    }

    private String buildNombreCompleto(Usuario u) {
        String nombres = u.getNombres() == null ? "" : u.getNombres().trim();
        String apellidos = u.getApellidos() == null ? "" : u.getApellidos().trim();
        String full = (nombres + " " + apellidos).trim();
        return full.isEmpty() ? u.getUsuario() : full;
    }
}
