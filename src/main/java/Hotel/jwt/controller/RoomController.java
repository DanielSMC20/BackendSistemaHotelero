package Hotel.jwt.controller;

import Hotel.jwt.entity.Habitacion;
import Hotel.jwt.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;


import java.util.List;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class RoomController {

    private final RoomService service;

    /**
     * Crear habitación
     * - Genera código HB-### si no llega
     * - Valida número único
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Habitacion> create(@RequestBody Habitacion h) {
        return ResponseEntity.ok(service.create(h));
    }

    /**
     * Actualizar habitación
     * - Protege número duplicado
     * - Permite cambiar tipo, precios, rango, etc.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Habitacion> update(@PathVariable Long id, @RequestBody Habitacion changes) {
        return ResponseEntity.ok(service.update(id, changes));
    }

    /**
     * Listar todas
     */
    @GetMapping
    public ResponseEntity<List<Habitacion>> listAll() {
        return ResponseEntity.ok(service.findAll());
    }

    /**
     * Listar disponibles
     */
    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCION','LIMPIEZA','GERENCIA','CONTABILIDAD')")
    public ResponseEntity<List<Habitacion>> available() {
        return ResponseEntity.ok(service.listAvailable());
    }

    /**
     * Buscar por número (ej. "101")
     */
    @GetMapping("/number/{numero}")
    public ResponseEntity<Habitacion> byNumber(@PathVariable String numero) {
        return ResponseEntity.ok(service.getByNumero(numero));
    }

    // ===== Acciones rápidas de estado =====
    // (compatibles con front legacy que usa 'disponible' gracias al shim de la entidad)

    @PatchMapping("/{id}/ocupar")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCION')")
    public ResponseEntity<Void> ocupar(@PathVariable Long id) {
        service.marcarOcupada(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/liberar")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCION','LIMPIEZA')")
    public ResponseEntity<Void> liberar(@PathVariable Long id) {
        service.marcarDisponible(id);
        return ResponseEntity.noContent().build();
    }

    // 🔑 Soportar AMBOS paths para no tocar el front:
    @PatchMapping({"/{id}/mantenimiento", "/{id}/mantener"})
    @PreAuthorize("hasAnyRole('ADMIN','LIMPIEZA')")
    public ResponseEntity<Void> mantenimiento(@PathVariable Long id) {
        service.marcarMantenimiento(id);
        return ResponseEntity.noContent().build();
    }

}
