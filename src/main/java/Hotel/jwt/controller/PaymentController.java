package Hotel.jwt.controller;

import Hotel.jwt.dto.common.ApiResponse;
import Hotel.jwt.dto.payment.PaymentRequest;
import Hotel.jwt.dto.payment.PaymentResponse;
import Hotel.jwt.entity.Pago;
import Hotel.jwt.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/payments")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService service;

    // Registrar pago "local" (se graba COMPLETADO)
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA','CONTABILIDAD','GERENCIA')")
    public ResponseEntity<ApiResponse<PaymentResponse>> record(@RequestBody PaymentRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(service.record(req)));
    }

    // Listar pagos de una reserva
    @GetMapping("/by-reservation/{reservaId}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA','CONTABILIDAD','GERENCIA')")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> byReservation(@PathVariable Long reservaId) {
        return ResponseEntity.ok(ApiResponse.ok(service.byReservation(reservaId)));
    }

    // Marcar pago COMLETO / REEMBOLSADO / FALLIDO (por si gestionas estados manuales)
    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA','CONTABILIDAD','GERENCIA')")
    public ResponseEntity<ApiResponse<Pago>> complete(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(service.marcarPagado(id)));
    }

    @PatchMapping("/{id}/refund")
    @PreAuthorize("hasAnyRole('ADMIN','CONTABILIDAD','GERENCIA')")
    public ResponseEntity<ApiResponse<Pago>> refund(@PathVariable Long id, @RequestParam String ref) {
        return ResponseEntity.ok(ApiResponse.ok(service.marcarReembolsado(id, ref)));
    }

    @PatchMapping("/{id}/fail")
    @PreAuthorize("hasAnyRole('ADMIN','CONTABILIDAD','GERENCIA')")
    public ResponseEntity<ApiResponse<Pago>> fail(@PathVariable Long id, @RequestParam String ref) {
        return ResponseEntity.ok(ApiResponse.ok(service.marcarFallido(id, ref)));
    }

    // Para el dashboard (ingresos por día)
    @GetMapping("/revenue")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCION','CONTABILIDAD','GERENCIA')")
    public ResponseEntity<ApiResponse<List<PaymentResponse.DayTotal>>> revenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        LocalDateTime s = start.atStartOfDay();
        LocalDateTime e = end.plusDays(1).atStartOfDay().minusSeconds(1);
        return ResponseEntity.ok(ApiResponse.ok(service.sumByDay(s, e)));
    }

    @GetMapping("/{id}/comprobante")
    public ResponseEntity<byte[]> descargarComprobante(@PathVariable Long id) {
        Pago pago = service.obtenerPago(id);

        byte[] pdf = pago.getComprobantePdf();
        if (pdf == null || pdf.length == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=boleta_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
