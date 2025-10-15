package Hotel.jwt.controller;

import Hotel.jwt.entity.Clientes;
import Hotel.jwt.entity.Habitacion;
import Hotel.jwt.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class RoomController {
    private final RoomService service;

    @PostMapping
    public Habitacion create(@RequestBody Habitacion r){ return service.create(r); }

    @GetMapping("/available")
    public List<Habitacion> available(){ return service.listAvailable(); }
    @GetMapping
    public List<Habitacion> listAll() {
        return service.findAll();
    }

}