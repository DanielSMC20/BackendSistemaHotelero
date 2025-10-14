package Hotel.jwt.controller;

import Hotel.jwt.dto.common.ApiResponse;
import Hotel.jwt.dto.payment.PaymentRequest;
import Hotel.jwt.dto.payment.PaymentResponse;
import Hotel.jwt.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService service;

    @PostMapping
    public ApiResponse<PaymentResponse> record(@RequestBody PaymentRequest req){
        return ApiResponse.ok(service.record(req));
    }

    @GetMapping("/by-reservation/{reservationId}")
    public ApiResponse<List<PaymentResponse>> byReservation(@PathVariable Long reservationId){
        return ApiResponse.ok(service.byReservation(reservationId));
    }
}