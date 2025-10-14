package Hotel.jwt.controller;
import Hotel.jwt.dto.common.ApiResponse;
import Hotel.jwt.dto.invoice.CreateInvoiceRequest;
import Hotel.jwt.dto.invoice.InvoiceResponse;
import Hotel.jwt.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/invoices")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService service;

    @PostMapping("/issue")
    public ApiResponse<InvoiceResponse> issue(@RequestBody CreateInvoiceRequest req) {
        return ApiResponse.ok(service.issue(req));
    }

    @GetMapping("/by-reservation/{reservationId}")
    public ApiResponse<InvoiceResponse> byReservation(@PathVariable Long reservationId) {
        return ApiResponse.ok(service.getByReservation(reservationId));
    }

    @GetMapping("/{id}")
    public ApiResponse<InvoiceResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(service.get(id));
    }
}