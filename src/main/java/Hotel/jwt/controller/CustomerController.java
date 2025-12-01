package Hotel.jwt.controller;

import Hotel.jwt.dto.common.ApiResponse;
import Hotel.jwt.dto.customer.CustomerRequest;
import Hotel.jwt.entity.Clientes;
import Hotel.jwt.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService service;

    @PostMapping
    public Clientes create(@RequestBody Clientes c){ return service.create(c); }

    @PostMapping("/from-reservation-style")
    public Clientes createFromReservationStyle(@RequestBody CustomerRequest req) {
        return service.createFromRequest(req);
    }
    @GetMapping("/search")
    public List<Clientes> search(@RequestParam String q){ return service.searchByName(q); }

    @GetMapping
    public List<Clientes> listAll() {
        return service.findAll();
    }

    @GetMapping("/documento/{documento}")
    public ApiResponse<Clientes> getByDocument(@PathVariable String documento) {
        Clientes cliente = service.findByDocumento(documento);
        return ApiResponse.ok(cliente);
    }
    @PutMapping("/documento/{documento}")
    public ApiResponse<Clientes> updateByDocumento(
            @PathVariable String documento,
            @RequestBody Clientes clienteActualizado) {

        Clientes updated = service.updateByDocumento(documento, clienteActualizado);
        return ApiResponse.ok(updated);
    }

}