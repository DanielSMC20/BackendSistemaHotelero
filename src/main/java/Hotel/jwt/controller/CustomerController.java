package Hotel.jwt.controller;

import Hotel.jwt.entity.Clientes;
import Hotel.jwt.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService service;

    @PostMapping
    public Clientes create(@RequestBody Clientes c){ return service.create(c); }

    @GetMapping("/search")
    public List<Clientes> search(@RequestParam String q){ return service.searchByName(q); }
}