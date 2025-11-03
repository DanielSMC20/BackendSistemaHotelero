package Hotel.jwt.controller;

import Hotel.jwt.dto.common.ApiResponse;
import Hotel.jwt.dto.invoice.CreateInvoiceRequest;
import Hotel.jwt.dto.invoice.InvoiceResponse;
import Hotel.jwt.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/invoices")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class InvoiceController {
    private final InvoiceService service;

    // Recepción SÍ puede emitir boleta/factura
    @PostMapping("/issue")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA','CONTABILIDAD')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> issue(@RequestBody CreateInvoiceRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(service.issue(req)));
    }

    @GetMapping("/by-reservation/{reservationId}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA','CONTABILIDAD')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> byReservation(@PathVariable Long reservationId) {
        return ResponseEntity.ok(ApiResponse.ok(service.getByReservation(reservationId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA','CONTABILIDAD')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(service.get(id)));
    }

    // Marcar pagada: puede recepción/conta/admin
    @PatchMapping("/{id}/paid")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA','CONTABILIDAD')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> markPaid(@PathVariable Long id) {
        // return ResponseEntity.ok(ApiResponse.ok(service.markPaid(id)));
        return ResponseEntity.status(501).body(ApiResponse.error("Implementar service.markPaid(id)"));
    }

    // Anular: SOLO admin/contabilidad
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','CONTABILIDAD')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> cancel(@PathVariable Long id) {
        // return ResponseEntity.ok(ApiResponse.ok(service.cancel(id)));
        return ResponseEntity.status(501).body(ApiResponse.error("Implementar service.cancel(id)"));
    }
}
