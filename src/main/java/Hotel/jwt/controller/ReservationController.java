package Hotel.jwt.controller;

import Hotel.jwt.dto.reservation.ReservationRequest;
import Hotel.jwt.dto.reservation.ReservationWithCustomerRequest;
import Hotel.jwt.entity.Reserva;
import Hotel.jwt.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ReservationController {
    private final ReservationService service;


    @GetMapping
    public List<Reserva> listAll(){ return service.listAll(); }
    @PostMapping
    public Reserva create(@RequestBody ReservationRequest req){ return service.create(req); }

    @PostMapping("/with-customer")
    public Reserva createWithCustomer(@RequestBody ReservationWithCustomerRequest req){
        return service.createWithCustomer(req);
    }
    @GetMapping("/by-customer/{id}")
    public List<Reserva> byCustomer(@PathVariable Long id){ return service.byCustomer(id); }
}