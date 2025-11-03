package Hotel.jwt.controller;

import Hotel.jwt.dto.reservation.ReservationRequest;
import Hotel.jwt.dto.reservation.ReservationWithCustomerRequest;
import Hotel.jwt.entity.Reserva;
import Hotel.jwt.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;


import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ReservationController {

    private final ReservationService service;


    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
    public ResponseEntity<List<Reserva>> listAll() {
        return ResponseEntity.ok(service.listAll());
    }


    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
    public ResponseEntity<Reserva> create(@RequestBody ReservationRequest reserva) {
        return ResponseEntity.ok(service.create(reserva));
    }


    @GetMapping("/by-customer/{id}")
    public ResponseEntity<List<Reserva>> byCustomer(@PathVariable Long id) {
        return ResponseEntity.ok(service.byCustomer(id));
    }


    @PatchMapping("/{id}/checkin")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")

    public ResponseEntity<Reserva> checkIn(@PathVariable Long id) {
        return ResponseEntity.ok(service.checkIn(id));
    }


    @PatchMapping("/{id}/checkout")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")

    public ResponseEntity<Reserva> checkOut(@PathVariable Long id) {
        return ResponseEntity.ok(service.checkOut(id));
    }


    @PatchMapping("/{id}/extend")
    public ResponseEntity<Reserva> extend(
            @PathVariable Long id,
            @RequestParam int horasExtra
    ) {
        return ResponseEntity.ok(service.extendStay(id, horasExtra));
    }

    @GetMapping("/report")
    @PreAuthorize("hasAnyRole('ADMIN','CONTABILIDAD')")
    public ResponseEntity<List<Reserva>> byDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin
    ) {
        return ResponseEntity.ok(service.findByRangoFechas(inicio, fin));
    }

    @PostMapping("/with-customer")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
    public ResponseEntity<Reserva> createWithCustomer(@RequestBody ReservationWithCustomerRequest req) {
        return ResponseEntity.ok(service.createWithCustomer(req));
    }
}
